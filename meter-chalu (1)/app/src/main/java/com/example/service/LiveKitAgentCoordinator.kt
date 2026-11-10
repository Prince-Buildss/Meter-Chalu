package com.example.service

import android.content.Context
import android.media.AudioManager
import android.util.Log
import com.example.data.ClosenessData
import com.example.data.ClosenessWord
import com.example.data.ConversationMessage
import com.example.data.FixItem
import com.example.data.HomeReport
import com.example.data.MessageSender
import com.example.data.PreferencesManager
import com.example.data.ReadinessSummary
import com.example.data.RecentSession
import com.example.data.ScenariosRepository
import com.example.data.TargetSentence
import io.livekit.android.LiveKit
import io.livekit.android.events.RoomEvent
import io.livekit.android.room.Room
import io.livekit.android.room.participant.RemoteParticipant
import io.livekit.android.room.track.DataPublishReliability
import io.livekit.android.room.track.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

enum class AgentCallState {
    DISCONNECTED,
    CONNECTING,
    SPEAKING,
    THINKING,
    LISTENING,
    ERROR
}

data class ConversationUiState(
    val callState: AgentCallState = AgentCallState.DISCONNECTED,
    val isMuted: Boolean = false,
    val audioLevel: Float = 0f,
    val targetSentence: TargetSentence? = null,
    val closenessData: ClosenessData? = null,
    val messages: List<ConversationMessage> = emptyList(),
    val errorMessage: String? = null,
    val activeScenarioId: String? = null,
    val latestReadiness: ReadinessSummary? = null,
    val homeReport: HomeReport? = null,
    val isReportLoading: Boolean = false,
    val connectionStatusText: String = "",
    val isLiveMode: Boolean = true,
    val debugRoomInfo: String = "",
    // Real LiveKit debug strip metrics
    val roomConnectionState: String = "DISCONNECTED",
    val isMicPublished: Boolean = false,
    val remoteParticipantsText: String = "",
    val isAgentAudioSubscribed: Boolean = false,
    val agentStateText: String = "",
    val lastError: String? = null
)

class LiveKitAgentCoordinator(
    private val context: Context,
    private val prefs: PreferencesManager,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "LiveKitAgent"
    }

    private val _uiState = MutableStateFlow(ConversationUiState())
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // Real LiveKit SDK Room instance (strictly one instance at a time)
    private var currentRoom: Room? = null
    private var roomEventsJob: Job? = null
    private var audioMonitorJob: Job? = null

    // Mock mode engine (only active when isMockMode is true)
    private var voiceEngine: VoiceEngine? = null
    private var mockLoopJob: Job? = null
    private var simulatedAudioJob: Job? = null
    private var agentJoinTimeoutJob: Job? = null

    init {
        loadCachedReport()
        fetchHomeReport()
    }

    private fun loadCachedReport() {
        val cachedJson = prefs.cachedReportJson
        if (!cachedJson.isNullOrBlank()) {
            parseAndApplyReportJson(cachedJson)
        } else if (prefs.isMockMode) {
            _uiState.value = _uiState.value.copy(homeReport = HomeReport(name = prefs.driverName))
        }
    }

    fun fetchHomeReport() {
        scope.launch(Dispatchers.IO) {
            val isMock = prefs.isMockMode
            val base = prefs.backendUrl
            val roomName = prefs.stableRoomName

            if (!isMock && base.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(isReportLoading = true)
                try {
                    val url = "$base/report?room=$roomName"
                    Log.i(TAG, "GET $url")
                    val request = Request.Builder().url(url).get().build()
                    val response = httpClient.newCall(request).execute()
                    Log.i(TAG, "GET report HTTP ${response.code}")
                    if (response.isSuccessful) {
                        val body = response.body?.string() ?: ""
                        prefs.cachedReportJson = body
                        parseAndApplyReportJson(body)
                    } else {
                        Log.w(TAG, "Failed to fetch report, code ${response.code}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching report from backend", e)
                } finally {
                    _uiState.value = _uiState.value.copy(isReportLoading = false)
                }
            } else if (isMock) {
                val mockReport = HomeReport(
                    name = prefs.driverName,
                    percent = prefs.overallReadiness,
                    per_scenario = prefs.getAllScores()
                )
                _uiState.value = _uiState.value.copy(homeReport = mockReport)
            }
        }
    }

    fun testBackendHealth(candidateUrl: String, callback: (Boolean, String) -> Unit) {
        scope.launch(Dispatchers.IO) {
            var url = candidateUrl.trim().trimEnd('/')
            if (url.endsWith("/token")) {
                url = url.substringBeforeLast("/token").trimEnd('/')
            }
            if (url.isEmpty()) {
                callback(false, "URL is empty")
                return@launch
            }
            if (!url.startsWith("https://")) {
                callback(false, "Must start with https://")
                return@launch
            }

            try {
                val healthUrl = "$url/health"
                Log.i(TAG, "Testing health: GET $healthUrl")
                val request = Request.Builder().url(healthUrl).get().build()
                val response = httpClient.newCall(request).execute()
                val code = response.code
                val isOk = response.isSuccessful
                val msg = if (isOk) "OK ($code)" else "HTTP $code: ${response.message}"
                Log.i(TAG, "Health test response: $msg")
                callback(isOk, msg)
            } catch (e: Exception) {
                Log.e(TAG, "Health test error", e)
                callback(false, e.message ?: "Connection failed")
            }
        }
    }

    fun deleteDriverData(onComplete: (Boolean) -> Unit) {
        scope.launch(Dispatchers.IO) {
            val isMock = prefs.isMockMode
            val base = prefs.backendUrl
            val roomName = prefs.stableRoomName
            var success = true

            if (!isMock && base.isNotEmpty()) {
                try {
                    val url = "$base/driver?room=$roomName"
                    Log.i(TAG, "DELETE $url")
                    val request = Request.Builder().url(url).delete().build()
                    val resp = httpClient.newCall(request).execute()
                    success = resp.isSuccessful
                    Log.i(TAG, "DELETE driver HTTP ${resp.code}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to delete driver data on backend", e)
                    success = false
                }
            }

            prefs.clearDriverReportAndReadiness()
            _uiState.value = _uiState.value.copy(
                homeReport = null,
                latestReadiness = null
            )
            scope.launch(Dispatchers.Main) {
                onComplete(success)
            }
        }
    }

    private fun initVoiceEngineIfMock(isMock: Boolean) {
        if (isMock) {
            if (voiceEngine == null) {
                voiceEngine = VoiceEngine(
                    context = context,
                    onSpeechRecognized = { text ->
                        handleDriverSpeech(text)
                    },
                    onAgentTtsFinished = {
                        onAgentFinishedSpeaking()
                    },
                    onAudioLevelChanged = { lvl ->
                        _uiState.value = _uiState.value.copy(audioLevel = lvl)
                    }
                )
            }
        } else {
            // Live mode: VoiceEngine stays for mock mode only, never initialized in live
            voiceEngine?.destroy()
            voiceEngine = null
        }
    }

    fun startConversation(selectedScenarioId: String? = null) {
        mockLoopJob?.cancel()
        simulatedAudioJob?.cancel()
        agentJoinTimeoutJob?.cancel()
        audioMonitorJob?.cancel()
        roomEventsJob?.cancel()

        // Release any existing Room instance to guarantee exactly ONE
        currentRoom?.let { r ->
            scope.launch(Dispatchers.IO) {
                try {
                    r.disconnect()
                    r.release()
                } catch (e: Exception) {
                    Log.w(TAG, "Cleanup old room: ${e.message}")
                }
            }
        }
        currentRoom = null

        val isMock = prefs.isMockMode
        val base = prefs.backendUrl
        val roomName = prefs.stableRoomName
        val driverName = prefs.driverName
        val helperLanguage = prefs.helperLanguage

        _uiState.value = ConversationUiState(
            callState = AgentCallState.CONNECTING,
            isMuted = false,
            audioLevel = 0f,
            targetSentence = null,
            closenessData = null,
            messages = emptyList(),
            errorMessage = null,
            activeScenarioId = selectedScenarioId,
            isLiveMode = !isMock,
            connectionStatusText = if (isMock) "MOCK mode started" else "Requesting token...",
            debugRoomInfo = "Room: $roomName • Backend: ${base.ifEmpty { "(none)" }}",
            roomConnectionState = if (isMock) "MOCK" else "CONNECTING",
            isMicPublished = false,
            remoteParticipantsText = "",
            isAgentAudioSubscribed = false,
            agentStateText = "",
            lastError = null
        )

        initVoiceEngineIfMock(isMock)

        if (isMock) {
            // MOCK MODE
            scope.launch {
                delay(800)
                _uiState.value = _uiState.value.copy(connectionStatusText = "Mock connected")
                startMockConversation(selectedScenarioId)
            }
            return
        }

        // LIVE MODE using Official LiveKit Android SDK
        scope.launch(Dispatchers.IO) {
            if (base.isEmpty()) {
                val err = "Backend URL is empty. Check Settings (सेटिंग्ज)."
                Log.w(TAG, err)
                _uiState.value = _uiState.value.copy(
                    callState = AgentCallState.ERROR,
                    connectionStatusText = "Error: Backend URL is empty",
                    errorMessage = err,
                    lastError = err
                )
                return@launch
            }

            if (!base.startsWith("https://")) {
                val err = "Backend URL must be HTTPS (https:// आवश्यक आहे). Backend URL check करें"
                Log.w(TAG, err)
                _uiState.value = _uiState.value.copy(
                    callState = AgentCallState.ERROR,
                    connectionStatusText = "Error: Not HTTPS",
                    errorMessage = err,
                    lastError = err
                )
                return@launch
            }

            try {
                // Step 1: Requesting token...
                _uiState.value = _uiState.value.copy(connectionStatusText = "Requesting token...")

                val metadataJson = JSONObject().apply {
                    put("name", driverName)
                    put("helper_language", helperLanguage)
                    if (!selectedScenarioId.isNullOrBlank()) {
                        put("scenario", selectedScenarioId)
                    }
                }

                val requestBodyJson = JSONObject().apply {
                    put("roomName", roomName)
                    put("participantName", driverName)
                    put("agentName", "saathi")
                    put("metadata", metadataJson)
                }

                val tokenUrl = "$base/token"
                Log.i(TAG, "POST $tokenUrl with Body: ${requestBodyJson.toString()}")

                val request = Request.Builder()
                    .url(tokenUrl)
                    .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = httpClient.newCall(request).execute()
                val statusCode = response.code
                val responseBodyStr = response.body?.string() ?: ""

                Log.i(TAG, "HTTP $statusCode from $tokenUrl")

                if (!response.isSuccessful) {
                    val err = "HTTP $statusCode (${response.message}): $responseBodyStr. Backend URL check करें"
                    Log.e(TAG, "Token request failed: $err")
                    _uiState.value = _uiState.value.copy(
                        callState = AgentCallState.ERROR,
                        connectionStatusText = "Token failed: HTTP $statusCode",
                        errorMessage = err,
                        lastError = err
                    )
                    return@launch
                }

                // Step 2: Token OK
                val respJson = JSONObject(responseBodyStr)
                val serverUrl = respJson.optString("serverUrl")
                val participantToken = respJson.optString("participantToken")
                Log.i(TAG, "Token OK! serverUrl=$serverUrl, token=[TOKEN_HIDDEN]")

                _uiState.value = _uiState.value.copy(connectionStatusText = "Token OK")

                if (serverUrl.isBlank() || participantToken.isBlank()) {
                    val err = "Invalid token response: serverUrl or participantToken missing"
                    Log.e(TAG, err)
                    _uiState.value = _uiState.value.copy(
                        callState = AgentCallState.ERROR,
                        connectionStatusText = "Invalid token payload",
                        errorMessage = err,
                        lastError = err
                    )
                    return@launch
                }

                // Step 3: Connecting to room with LiveKit Android SDK
                _uiState.value = _uiState.value.copy(connectionStatusText = "Connecting to room...")

                connectLiveKitRoom(serverUrl, participantToken)

            } catch (e: Exception) {
                val err = "Token request failed: ${e.javaClass.simpleName}: ${e.message}. Backend URL check करें"
                Log.e(TAG, "Exception in token request", e)
                _uiState.value = _uiState.value.copy(
                    callState = AgentCallState.ERROR,
                    connectionStatusText = "Connection error",
                    errorMessage = err,
                    lastError = err
                )
            }
        }
    }

    private fun connectLiveKitRoom(serverUrl: String, participantToken: String) {
        scope.launch(Dispatchers.IO) {
            try {
                Log.i(TAG, "Creating LiveKit Room instance via LiveKit.create(applicationContext)...")
                val room = LiveKit.create(context.applicationContext)
                currentRoom = room

                // Configure Audio for Loudspeaker
                try {
                    room.audioHandler.start()
                    val audioSwitch = room.audioSwitchHandler
                    val speaker = audioSwitch?.availableAudioDevices?.firstOrNull {
                        it is com.twilio.audioswitch.AudioDevice.Speakerphone
                    }
                    if (speaker != null) {
                        audioSwitch?.selectDevice(speaker)
                        Log.i(TAG, "AudioSwitch: Speakerphone selected")
                    }

                    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                    audioManager?.isSpeakerphoneOn = true
                    audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
                } catch (audioEx: Exception) {
                    Log.w(TAG, "Loudspeaker setup warning", audioEx)
                }

                // Observe Room Events
                observeRoomEvents(room)

                // 15-second Agent Join Watchdog
                agentJoinTimeoutJob?.cancel()
                agentJoinTimeoutJob = scope.launch {
                    delay(15000)
                    if (currentRoom != null && currentRoom?.remoteParticipants.isNullOrEmpty()) {
                        Log.w(TAG, "Watchdog triggered: Agent did not join within 15s")
                        _uiState.value = _uiState.value.copy(
                            connectionStatusText = "Agent did not join. Check that the backend worker is running."
                        )
                    }
                }

                // Monitor real audio level for the orb (NOT Math.random)
                startRealAudioMonitor(room)

                // Connect Room
                Log.i(TAG, "Connecting to LiveKit room at $serverUrl ...")
                room.connect(serverUrl, participantToken)
                Log.i(TAG, "room.connect completed. Enabling microphone...")

                // Publish local microphone
                room.localParticipant.setMicrophoneEnabled(true)
                _uiState.value = _uiState.value.copy(
                    isMicPublished = true,
                    roomConnectionState = "CONNECTED",
                    connectionStatusText = "Room connected"
                )
                Log.i(TAG, "Microphone enabled successfully.")

                // Check if agent is already in the room
                if (room.remoteParticipants.isNotEmpty()) {
                    agentJoinTimeoutJob?.cancel()
                    _uiState.value = _uiState.value.copy(connectionStatusText = "Agent joined")
                    updateRemoteParticipantsDebug(room)
                }

            } catch (e: Exception) {
                val err = "LiveKit connection failed: ${e.message}"
                Log.e(TAG, err, e)
                _uiState.value = _uiState.value.copy(
                    callState = AgentCallState.ERROR,
                    connectionStatusText = "Connection error",
                    errorMessage = err,
                    lastError = err,
                    roomConnectionState = "ERROR"
                )
            }
        }
    }

    @OptIn(io.livekit.android.annotations.Beta::class)
    private fun observeRoomEvents(room: Room) {
        roomEventsJob?.cancel()
        roomEventsJob = scope.launch {
            room.events.events.collect { event ->
                Log.i(TAG, "Event: ${event.javaClass.simpleName}")
                when (event) {
                    is RoomEvent.Connected -> {
                        Log.i(TAG, "RoomEvent.Connected")
                        _uiState.value = _uiState.value.copy(
                            roomConnectionState = "CONNECTED",
                            connectionStatusText = "Room connected"
                        )
                        updateRemoteParticipantsDebug(room)
                    }

                    is RoomEvent.ParticipantConnected -> {
                        val identity = event.participant.identity?.value ?: "unknown"
                        Log.i(TAG, "RoomEvent.ParticipantConnected: $identity (${event.participant.kind})")
                        // Real remote participant joined -> cancel 15s watchdog
                        agentJoinTimeoutJob?.cancel()
                        _uiState.value = _uiState.value.copy(
                            connectionStatusText = "Agent joined"
                        )
                        updateRemoteParticipantsDebug(room)
                    }

                    is RoomEvent.ParticipantDisconnected -> {
                        val identity = event.participant.identity?.value ?: "unknown"
                        Log.i(TAG, "RoomEvent.ParticipantDisconnected: $identity")
                        updateRemoteParticipantsDebug(room)
                    }

                    is RoomEvent.TrackSubscribed -> {
                        Log.i(TAG, "RoomEvent.TrackSubscribed: ${event.track.kind} from ${event.participant.identity?.value}")
                        if (event.track.kind == Track.Kind.AUDIO) {
                            _uiState.value = _uiState.value.copy(
                                isAgentAudioSubscribed = true
                            )
                        }
                    }

                    is RoomEvent.ParticipantAttributesChanged -> {
                        val attributes = event.participant.attributes
                        val agentState = attributes["lk.agent.state"]
                        Log.i(TAG, "RoomEvent.ParticipantAttributesChanged: lk.agent.state=$agentState")
                        if (!agentState.isNullOrBlank()) {
                            val callState = when (agentState.lowercase()) {
                                "speaking" -> AgentCallState.SPEAKING
                                "thinking" -> AgentCallState.THINKING
                                "listening" -> AgentCallState.LISTENING
                                else -> _uiState.value.callState
                            }
                            val statusText = when (agentState.lowercase()) {
                                "speaking" -> "Agent speaking"
                                "thinking" -> "Agent thinking"
                                "listening" -> "Agent listening"
                                else -> "Agent $agentState"
                            }
                            _uiState.value = _uiState.value.copy(
                                callState = callState,
                                agentStateText = agentState,
                                connectionStatusText = statusText
                            )
                        }
                    }

                    is RoomEvent.TranscriptionReceived -> {
                        val isAgent = event.participant != room.localParticipant
                        event.transcriptionSegments.forEach { seg ->
                            if (seg.text.isNotBlank()) {
                                Log.i(TAG, "TranscriptionReceived: ${seg.text} (isAgent=$isAgent)")
                                appendTranscriptLine(seg.text, isAgent = isAgent)
                            }
                        }
                    }

                    is RoomEvent.DataReceived -> {
                        val topic = event.topic ?: ""
                        val payload = event.data.toString(Charsets.UTF_8)
                        Log.i(TAG, "RoomEvent.DataReceived on topic [$topic]: $payload")

                        if (topic == "lk.transcription") {
                            try {
                                val tJson = JSONObject(payload)
                                val text = tJson.optString("text", "")
                                val isAgent = event.participant != null
                                if (text.isNotBlank()) {
                                    appendTranscriptLine(text, isAgent = isAgent)
                                }
                            } catch (e: Exception) {
                                Log.w(TAG, "Error parsing lk.transcription JSON", e)
                            }
                        } else {
                            onDataReceived(topic, payload)
                        }
                    }

                    is RoomEvent.Disconnected -> {
                        Log.i(TAG, "RoomEvent.Disconnected: reason=${event.reason}")
                        _uiState.value = _uiState.value.copy(
                            roomConnectionState = "DISCONNECTED",
                            connectionStatusText = "Connection dropped",
                            callState = AgentCallState.DISCONNECTED,
                            lastError = "Disconnected: ${event.reason}"
                        )
                    }

                    else -> {}
                }
            }
        }
    }

    private fun updateRemoteParticipantsDebug(room: Room) {
        val remotes = room.remoteParticipants.values.mapNotNull { it.identity?.value }
        val remotesStr = if (remotes.isNotEmpty()) remotes.joinToString(", ") else "(none)"
        _uiState.value = _uiState.value.copy(
            remoteParticipantsText = remotesStr
        )
    }

    private fun startRealAudioMonitor(room: Room) {
        audioMonitorJob?.cancel()
        audioMonitorJob = scope.launch {
            while (currentRoom != null) {
                // Audio level for the orb comes from REAL WebRTC audio level of agent participant
                val agentParticipant = room.remoteParticipants.values.firstOrNull()
                val realLvl = agentParticipant?.audioLevel ?: 0f
                _uiState.value = _uiState.value.copy(audioLevel = realLvl)
                delay(100)
            }
            _uiState.value = _uiState.value.copy(audioLevel = 0f)
        }
    }

    private fun appendTranscriptLine(text: String, isAgent: Boolean) {
        val newMsg = ConversationMessage(
            id = UUID.randomUUID().toString(),
            sender = if (isAgent) MessageSender.AGENT else MessageSender.DRIVER,
            textMarathi = text
        )
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + newMsg
        )
    }

    private fun startMockConversation(scenarioId: String?) {
        val sc = ScenariosRepository.ALL[scenarioId ?: "dadar"] ?: ScenariosRepository.ALL["dadar"]!!
        val openingGreeting = if (scenarioId != null) {
            sc.openingMarathi
        } else {
            "नमस्कार भाऊ! सांगा, आज कोणत्या प्रसंगाचा सराव करायचा आहे? दादर स्टेशन, भाडे, रस्ता दाखवणे, की आरटीओ?"
        }
        val openingMeaning = if (scenarioId != null) {
            sc.openingMeaning
        } else {
            "नमस्ते भाई! बताइए, आज किस परिस्थिति का अभ्यास करना है?"
        }

        agentSays(openingGreeting, openingMeaning)
    }

    private fun agentSays(marathi: String, meaning: String?) {
        _uiState.value = _uiState.value.copy(
            callState = AgentCallState.SPEAKING,
            connectionStatusText = "Agent speaking"
        )

        val newMsg = ConversationMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.AGENT,
            textMarathi = marathi,
            meaning = meaning
        )
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + newMsg
        )

        startOrbWaveformAnimation()

        if (prefs.isMockMode) {
            voiceEngine?.speakPassenger(marathi)
        }
    }

    private fun startOrbWaveformAnimation() {
        simulatedAudioJob?.cancel()
        simulatedAudioJob = scope.launch {
            while (_uiState.value.callState == AgentCallState.SPEAKING || _uiState.value.callState == AgentCallState.LISTENING) {
                val randLevel = (0.25f + Math.random().toFloat() * 0.7f)
                _uiState.value = _uiState.value.copy(audioLevel = randLevel)
                delay(120)
            }
            _uiState.value = _uiState.value.copy(audioLevel = 0f)
        }
    }

    private fun onAgentFinishedSpeaking() {
        if (_uiState.value.callState == AgentCallState.SPEAKING) {
            _uiState.value = _uiState.value.copy(
                callState = AgentCallState.LISTENING,
                connectionStatusText = "Agent listening",
                audioLevel = 0f
            )
            if (prefs.isMockMode && !_uiState.value.isMuted) {
                voiceEngine?.startListening()
            }
        }
    }

    fun handleDriverSpeech(spokenText: String) {
        if (spokenText.isBlank()) return
        voiceEngine?.stopListening()

        val driverMsg = ConversationMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.DRIVER,
            textMarathi = spokenText
        )
        _uiState.value = _uiState.value.copy(
            callState = AgentCallState.THINKING,
            connectionStatusText = "Agent thinking",
            messages = _uiState.value.messages + driverMsg,
            audioLevel = 0f
        )

        if (prefs.isMockMode) {
            simulateMockResponse(spokenText)
        }
    }

    private fun simulateMockResponse(driverSpoken: String) {
        mockLoopJob?.cancel()
        mockLoopJob = scope.launch {
            delay(1200)

            val currentTarget = _uiState.value.targetSentence
            if (currentTarget == null) {
                val target = TargetSentence(
                    marathi = "नमस्कार भाऊ, दादर स्टेशनला चलाल का?",
                    meaning = "नमस्ते भाई, दादर स्टेशन चलोगे क्या?"
                )
                onDataReceived("target", JSONObject().apply {
                    put("marathi", target.marathi)
                    put("meaning", target.meaning)
                }.toString())

                agentSays("छान! आता माझ्या मागे हे वाक्य म्हणा:", "अच्छा! अब मेरे पीछे यह वाक्य बोलिए:")
            } else {
                val score = (74 + (Math.random() * 20).toInt()).coerceIn(70, 95)
                val mastered = score >= 75
                val wordsList = currentTarget.marathi.split(" ").mapIndexed { index, w ->
                    val st = if (index % 3 == 0 && score < 80) "almost" else "great"
                    ClosenessWord(w, st)
                }

                val closenessJson = JSONObject().apply {
                    put("score", score)
                    put("heard", driverSpoken)
                    put("mastered", mastered)
                    put("words", JSONArray().apply {
                        wordsList.forEach { cw ->
                            put(JSONObject().apply {
                                put("w", cw.w)
                                put("state", cw.state)
                            })
                        }
                    })
                }

                onDataReceived("closeness", closenessJson.toString())

                delay(600)
                val praise = if (mastered) {
                    "शाबास भाऊ! उच्चार खूप छान झाला. आता पुढचं वाक्य घेऊया."
                } else {
                    "छान प्रयत्न! 'चलाल का' चा उच्चार आणखी थोडा स्पष्ट करा."
                }
                agentSays(praise, "प्रशंसा / सुधारणा")
            }
        }
    }

    // 3. SHORTCUT CHIPS: Publish on data channel with topic = "client"
    fun sendShortcutScenario(scenarioId: String) {
        val validIds = listOf("dadar", "fare", "directions", "elderly", "rto")
        if (!validIds.contains(scenarioId)) return

        val payload = JSONObject().apply {
            put("scenario", scenarioId)
        }.toString()

        _uiState.value = _uiState.value.copy(activeScenarioId = scenarioId)

        val room = currentRoom
        if (room != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    room.localParticipant.publishData(
                        data = payload.toByteArray(Charsets.UTF_8),
                        reliability = DataPublishReliability.RELIABLE,
                        topic = "client"
                    )
                    Log.i(TAG, "Published topic 'client': $payload")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to publish shortcut scenario", e)
                }
            }
        }

        if (prefs.isMockMode) {
            val sc = ScenariosRepository.ALL[scenarioId] ?: ScenariosRepository.ALL["dadar"]!!
            val target = TargetSentence(
                marathi = sc.openingMarathi,
                meaning = sc.openingMeaning
            )
            onDataReceived("target", JSONObject().apply {
                put("marathi", target.marathi)
                put("meaning", target.meaning)
            }.toString())

            agentSays("होय! ${sc.titleMarathi} सुरू करूया: “${sc.openingMarathi}”", sc.openingMeaning)
        }
    }

    // 4. MUTE: room.localParticipant.setMicrophoneEnabled(!muted)
    fun toggleMute() {
        val newMuted = !_uiState.value.isMuted
        _uiState.value = _uiState.value.copy(isMuted = newMuted)

        val room = currentRoom
        if (room != null && !prefs.isMockMode) {
            scope.launch {
                try {
                    room.localParticipant.setMicrophoneEnabled(!newMuted)
                    _uiState.value = _uiState.value.copy(isMicPublished = !newMuted)
                    Log.i(TAG, "Microphone enabled set to: ${!newMuted}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to toggle microphone", e)
                }
            }
        } else if (prefs.isMockMode) {
            if (newMuted) {
                voiceEngine?.stopListening()
                _uiState.value = _uiState.value.copy(callState = AgentCallState.LISTENING)
            } else if (_uiState.value.callState == AgentCallState.LISTENING) {
                voiceEngine?.startListening()
            }
        }
    }

    // 4. END: room.disconnect() and room.release(), cancel jobs, fetchHomeReport()
    fun endConversation() {
        mockLoopJob?.cancel()
        simulatedAudioJob?.cancel()
        agentJoinTimeoutJob?.cancel()
        audioMonitorJob?.cancel()
        roomEventsJob?.cancel()

        val room = currentRoom
        currentRoom = null
        if (room != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    Log.i(TAG, "Disconnecting and releasing LiveKit Room...")
                    room.disconnect()
                    room.release()
                } catch (e: Exception) {
                    Log.w(TAG, "Error releasing LiveKit Room", e)
                }
            }
        }

        voiceEngine?.stopListening()
        voiceEngine?.stopSpeaking()

        if (prefs.isMockMode) {
            val calculatedScore = (76 + (Math.random() * 18).toInt()).coerceIn(70, 94)
            val allScores = prefs.getAllScores().toMutableMap()
            val scId = _uiState.value.activeScenarioId ?: "dadar"
            allScores[scId] = calculatedScore

            val summaryJson = JSONObject().apply {
                put("percent", ((allScores.values.sum()) / allScores.size).coerceIn(70, 95))
                put("per_scenario", JSONObject(allScores as Map<*, *>))
                put("ready", true)
                put("note", "Practice aid only. This is not an official test and does not certify anyone.")
                put("scenario", scId)
                put("score", calculatedScore)
                put("detected_language", "hi-IN")
            }.toString()

            onDataReceived("readiness", summaryJson)

            val reportJson = JSONObject().apply {
                put("name", prefs.driverName)
                put("percent", prefs.overallReadiness)
                put("per_scenario", JSONObject(allScores as Map<*, *>))
                put("ready", true)
                put("note", "Practice aid only. This is not an official test and does not certify anyone.")
                put("sessions", 13)
                put("streak_days", 4)
                put("days_since_last", 0)
                put("closeness_avg_recent", 84)
                put("closeness_trend", 1)
                put("strong_in", JSONArray(listOf("dadar", "directions")))
                put("fix_these", JSONArray().apply {
                    put(JSONObject().apply {
                        put("wrong", "दादर स्टेशन जाओगे?")
                        put("right", "दादर स्टेशनला चलाल का?")
                        put("meaning", "दादर स्टेशन चलोगे क्या?")
                        put("tip", "'स्टेशनला' आणि 'चलाल का' वापरल्याने वाक्य नम्र बनते.")
                        put("times", 2)
                    })
                })
                put("fixed_count", 9)
                put("recent", JSONArray().apply {
                    put(JSONObject().apply {
                        put("date", "आत्ताच")
                        put("scenario", scId)
                        put("score", calculatedScore)
                        put("closeness", 86)
                    })
                })
                put("carry_forward", "आजचा सराव उत्कृष्ट झाला, उद्याही सराव करा!")
            }.toString()

            onDataReceived("report", reportJson)
        }

        _uiState.value = _uiState.value.copy(
            callState = AgentCallState.DISCONNECTED,
            roomConnectionState = "DISCONNECTED",
            isMicPublished = false,
            isAgentAudioSubscribed = false
        )
        fetchHomeReport()
    }

    // 2. DataReceived handler for topics "target", "closeness", "readiness", "report"
    fun onDataReceived(topic: String, payloadJson: String) {
        try {
            val json = JSONObject(payloadJson)
            when (topic) {
                "target" -> {
                    val marathi = json.optString("marathi")
                    val meaning = json.optString("meaning")
                    _uiState.value = _uiState.value.copy(
                        targetSentence = TargetSentence(marathi, meaning)
                    )
                }

                "closeness" -> {
                    val score = json.optInt("score", 75)
                    val heard = json.optString("heard", "")
                    val mastered = json.optBoolean("mastered", score >= 75)
                    val wordsArr = json.optJSONArray("words")
                    val wordsList = mutableListOf<ClosenessWord>()
                    if (wordsArr != null) {
                        for (i in 0 until wordsArr.length()) {
                            val wObj = wordsArr.optJSONObject(i)
                            if (wObj != null) {
                                wordsList.add(
                                    ClosenessWord(
                                        w = wObj.optString("w"),
                                        state = wObj.optString("state", "almost")
                                    )
                                )
                            }
                        }
                    }

                    val closeness = ClosenessData(score, wordsList, heard, mastered)
                    _uiState.value = _uiState.value.copy(closenessData = closeness)
                }

                "readiness" -> {
                    val percent = json.optInt("percent", 80)
                    val ready = json.optBoolean("ready", percent >= 70)
                    val note = json.optString("note", "Practice aid only. This is not an official test.")
                    val scenario = json.optString("scenario", "conversation")
                    val score = json.optInt("score", 85)
                    val detected = json.optString("detected_language", "hi-IN")

                    val perMap = mutableMapOf<String, Int>()
                    val perObj = json.optJSONObject("per_scenario")
                    if (perObj != null) {
                        val keys = perObj.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            perMap[k] = perObj.optInt(k, 70)
                        }
                    }

                    val summary = ReadinessSummary(
                        percent = percent,
                        per_scenario = perMap,
                        ready = ready,
                        note = note,
                        scenario = scenario,
                        score = score,
                        detected_language = detected
                    )

                    prefs.updateFromReadiness(summary)

                    val summaryMsg = ConversationMessage(
                        id = UUID.randomUUID().toString(),
                        sender = MessageSender.SUMMARY_CARD,
                        textMarathi = "आजचा सराव पूर्ण!",
                        readiness = summary
                    )
                    _uiState.value = _uiState.value.copy(
                        latestReadiness = summary,
                        messages = _uiState.value.messages + summaryMsg
                    )
                }

                "report" -> {
                    prefs.cachedReportJson = payloadJson
                    parseAndApplyReportJson(payloadJson)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse data message for topic $topic", e)
        }
    }

    private fun parseAndApplyReportJson(jsonStr: String) {
        try {
            val json = JSONObject(jsonStr)
            val name = json.optString("name", prefs.driverName)
            val percent = json.optInt("percent", prefs.overallReadiness)
            val ready = json.optBoolean("ready", percent >= 70)
            val note = json.optString("note", "Practice aid only.")
            val sessions = json.optInt("sessions", 0)
            val streakDays = json.optInt("streak_days", 0)
            val daysSinceLast = json.optInt("days_since_last", 0)
            val closenessAvgRecent = json.optInt("closeness_avg_recent", 80)
            val closenessTrend = json.optInt("closeness_trend", 0)
            val fixedCount = json.optInt("fixed_count", 0)
            val carryForward = json.optString("carry_forward", "")

            val perMap = mutableMapOf<String, Int>()
            val perObj = json.optJSONObject("per_scenario")
            if (perObj != null) {
                val it = perObj.keys()
                while (it.hasNext()) {
                    val k = it.next()
                    perMap[k] = perObj.optInt(k, 0)
                }
            }

            val strongList = mutableListOf<String>()
            val strongArr = json.optJSONArray("strong_in")
            if (strongArr != null) {
                for (i in 0 until strongArr.length()) {
                    strongList.add(strongArr.optString(i))
                }
            }

            val fixList = mutableListOf<FixItem>()
            val fixArr = json.optJSONArray("fix_these")
            if (fixArr != null) {
                for (i in 0 until fixArr.length()) {
                    val item = fixArr.optJSONObject(i)
                    if (item != null) {
                        fixList.add(
                            FixItem(
                                wrong = item.optString("wrong"),
                                right = item.optString("right"),
                                meaning = item.optString("meaning"),
                                tip = item.optString("tip"),
                                times = item.optInt("times", 1)
                            )
                        )
                    }
                }
            }

            val recentList = mutableListOf<RecentSession>()
            val recArr = json.optJSONArray("recent")
            if (recArr != null) {
                for (i in 0 until recArr.length()) {
                    val item = recArr.optJSONObject(i)
                    if (item != null) {
                        recentList.add(
                            RecentSession(
                                date = item.optString("date"),
                                scenario = item.optString("scenario"),
                                score = item.optInt("score"),
                                closeness = item.optInt("closeness")
                            )
                        )
                    }
                }
            }

            val report = HomeReport(
                name = name,
                percent = percent,
                per_scenario = perMap,
                ready = ready,
                note = note,
                sessions = sessions,
                streak_days = streakDays,
                days_since_last = daysSinceLast,
                closeness_avg_recent = closenessAvgRecent,
                closeness_trend = closenessTrend,
                strong_in = strongList,
                fix_these = fixList,
                fixed_count = fixedCount,
                recent = recentList,
                carry_forward = carryForward
            )

            _uiState.value = _uiState.value.copy(homeReport = report)
            prefs.overallReadiness = percent
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse report json", e)
        }
    }

    fun release() {
        endConversation()
        voiceEngine?.destroy()
    }
}
