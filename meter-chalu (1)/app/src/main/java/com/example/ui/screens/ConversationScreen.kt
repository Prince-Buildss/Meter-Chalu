package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.ConversationMessage
import com.example.data.MessageSender
import com.example.service.AgentCallState
import com.example.service.LiveKitAgentCoordinator
import com.example.ui.components.ClosenessMeter
import com.example.ui.components.ConversationOrb
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberText
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderHighlight
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GreenAccent
import com.example.ui.theme.GreenText
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RickshawAmber
import com.example.ui.theme.RickshawAmberBorder
import com.example.ui.theme.RickshawAmberSurface
import com.example.ui.theme.RickshawBlack
import com.example.ui.theme.RickshawGreen
import com.example.ui.theme.RickshawGreenBorder
import com.example.ui.theme.RickshawGreenSurface
import com.example.ui.theme.RickshawYellow
import com.example.ui.theme.RickshawYellowDark
import com.example.ui.theme.RickshawYellowLight
import com.example.ui.theme.RoadGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YellowGradient

@Composable
fun ConversationScreen(
    coordinator: LiveKitAgentCoordinator,
    helperLanguage: String = "hi",
    onEndCall: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val uiState by coordinator.uiState.collectAsState()
    val listState = rememberLazyListState()

    var showDebugInfo by remember { mutableStateOf(false) }

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            coordinator.startConversation(uiState.activeScenarioId)
        }
    }

    LaunchedEffect(Unit) {
        if (!hasMicPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Auto scroll chat transcript to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    BackHandler {
        coordinator.endConversation()
        onEndCall()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .testTag("conversation_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP HEADER: Mode Badge + Call Status + Info Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Small Tasteful LIVE or MOCK pill
                Surface(
                    color = if (uiState.isLiveMode) RickshawGreenSurface else RickshawAmberSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (uiState.isLiveMode) RickshawGreenBorder else RickshawAmberBorder
                    )
                ) {
                    Text(
                        text = if (uiState.isLiveMode) "LIVE" else "MOCK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (uiState.isLiveMode) GreenText else AmberText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (uiState.callState == AgentCallState.ERROR) AmberAccent else GreenAccent)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = if (uiState.callState == AgentCallState.CONNECTING) "जोडत आहे..." else "कॉल चालू",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "मीटर चालू • साथी",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RickshawYellow
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Subtle Info Icon to reveal technical debug strip
                IconButton(
                    onClick = { showDebugInfo = !showDebugInfo },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Toggle Debug Info",
                        tint = if (showDebugInfo) RickshawYellow else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // OPTIONAL TECHNICAL DEBUG STRIP (Hidden behind Info toggle)
        AnimatedVisibility(visible = showDebugInfo) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                color = DarkSurface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.connectionStatusText.ifEmpty { "Ready" },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.connectionStatusText.contains("not join") || uiState.connectionStatusText.contains("error") || uiState.connectionStatusText.contains("failed") || uiState.callState == AgentCallState.ERROR) AmberAccent else GreenAccent
                        )
                        Text(
                            text = "Room: ${uiState.roomConnectionState}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.roomConnectionState == "CONNECTED") GreenAccent else RoadGray
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Mic: ${if (uiState.isMicPublished) "Published" else "No"}",
                            fontSize = 11.sp,
                            color = if (uiState.isMicPublished) GreenAccent else TextMuted
                        )
                        Text(
                            text = "Audio sub: ${if (uiState.isAgentAudioSubscribed) "Yes" else "No"}",
                            fontSize = 11.sp,
                            color = if (uiState.isAgentAudioSubscribed) GreenAccent else TextMuted
                        )
                        Text(
                            text = "Agent state: ${uiState.agentStateText.ifEmpty { "none" }}",
                            fontSize = 11.sp,
                            color = RickshawYellow
                        )
                    }

                    if (uiState.remoteParticipantsText.isNotEmpty()) {
                        Text(
                            text = "Remote: ${uiState.remoteParticipantsText}",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }

                    if (!uiState.lastError.isNullOrBlank()) {
                        Text(
                            text = "Err: ${uiState.lastError}",
                            fontSize = 11.sp,
                            color = AmberAccent,
                            maxLines = 2
                        )
                    }
                }
            }
        }

        // MIC PERMISSION BANNER (Friendly In-Screen Error Banner)
        if (!hasMicPermission) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = RickshawAmberSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, RickshawAmberBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MicOff,
                        contentDescription = "Mic Permission Needed",
                        tint = AmberAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "माइक परवानगी आवश्यक आहे (Mic needed)",
                        fontSize = 13.sp,
                        color = PureWhite,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Allow", fontSize = 12.sp, color = RickshawBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // SHORTCUT HINT PILLS ("किंवा म्हणा (or say):")
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "किंवा म्हणा:",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.padding(end = 6.dp)
            )

            val hints = listOf(
                "दादर" to "dadar",
                "भाडे" to "fare",
                "दिशा" to "directions",
                "म्हातारे" to "elderly",
                "आरटीओ" to "rto"
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(hints) { (label, id) ->
                    val isSelected = uiState.activeScenarioId == id
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) RickshawYellow else DarkSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) RickshawYellowLight else DarkBorderSubtle
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                coordinator.sendShortcutScenario(id)
                            }
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) RickshawBlack else PureWhite,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // TARGET SENTENCE CARD & CLOSENESS METER PINNED ABOVE ORB
        AnimatedVisibility(
            visible = uiState.targetSentence != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ClosenessMeter(
                targetSentence = uiState.targetSentence,
                closenessData = uiState.closenessData,
                isDriverSpeaking = uiState.callState == AgentCallState.LISTENING && uiState.audioLevel > 0.15f,
                audioLevel = uiState.audioLevel,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        // ERROR STATE WITH RETRY (Full-screen friendly style)
        if (uiState.callState == AgentCallState.ERROR) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = RickshawAmberSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, RickshawAmberBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = AmberAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = uiState.errorMessage ?: "त्रुटि झाली. पुन्हा प्रयत्न करा.",
                        fontSize = 14.sp,
                        color = PureWhite,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            coordinator.startConversation(uiState.activeScenarioId)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry",
                            tint = RickshawBlack,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Retry", fontSize = 13.sp, color = RickshawBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // CENTER ANIMATED VOICE ORB (Reacts to real WebRTC audioLevel)
        ConversationOrb(
            state = uiState.callState,
            audioLevel = uiState.audioLevel,
            helperLanguage = helperLanguage,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // SCROLLING CHAT TRANSCRIPT:
        // Agent: calm left-aligned text blocks with large Marathi type (26sp), matra-safe line height.
        // Driver: right-aligned soft bubbles.
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("chat_transcript_list")
        ) {
            items(uiState.messages, key = { it.id }) { msg ->
                when (msg.sender) {
                    MessageSender.AGENT -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 18.dp),
                                color = DarkSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle),
                                modifier = Modifier.fillMaxWidth(0.92f)
                            ) {
                                Row(modifier = Modifier.padding(14.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(28.dp)
                                            .clip(RoundedCornerShape(1.5.dp))
                                            .background(RickshawYellow)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "साथी (Agent)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RickshawYellow,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = msg.textMarathi,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PureWhite,
                                            lineHeight = 34.sp
                                        )
                                        if (!msg.meaning.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "मतलब: ${msg.meaning}",
                                                fontSize = 13.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    MessageSender.DRIVER -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Surface(
                                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomEnd = 18.dp, bottomStart = 18.dp),
                                color = DarkSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "तुम्ही (You)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = msg.textMarathi,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = TextPrimary,
                                        lineHeight = 24.sp
                                    )
                                }
                            }
                        }
                    }

                    MessageSender.SUMMARY_CARD -> {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = DarkSurface,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, RickshawYellow),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "📊 सराव फेरी अहवाल (Round Summary)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RickshawYellow
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.textMarathi,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                                if (msg.readiness != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "तयारी: ${msg.readiness.percent}% • गुण: ${msg.readiness.score}/100",
                                        fontSize = 13.sp,
                                        color = GreenAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // BOTTOM CONTROLS: Large Thumb-Accessible Mute & End Call Buttons (58dp height)
        // End button: distinct dark surface with warm yellow/amber outline (NEVER RED!)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute / Unmute Button
            val muteInteraction = remember { MutableInteractionSource() }
            val isMutePressed by muteInteraction.collectIsPressedAsState()
            val muteScale by androidx.compose.animation.core.animateFloatAsState(
                targetValue = if (isMutePressed) 0.95f else 1.0f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                label = "mute_scale"
            )

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .scale(muteScale)
                    .clip(RoundedCornerShape(18.dp))
                    .testTag("btn_mute_toggle")
                    .clickable(
                        interactionSource = muteInteraction,
                        indication = null,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            coordinator.toggleMute()
                        }
                    ),
                color = if (uiState.isMuted) RickshawAmberSurface else DarkSurfaceElevated,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (uiState.isMuted) RickshawAmberBorder else DarkBorder
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (uiState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute",
                        tint = if (uiState.isMuted) AmberText else PureWhite,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isMuted) "अनम्यूट करा" else "म्यूट करा",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isMuted) AmberText else PureWhite
                    )
                }
            }

            // End Call Button (NEVER RED - Refined Dark with Yellow/Amber Outline)
            val endInteraction = remember { MutableInteractionSource() }
            val isEndPressed by endInteraction.collectIsPressedAsState()
            val endScale by androidx.compose.animation.core.animateFloatAsState(
                targetValue = if (isEndPressed) 0.95f else 1.0f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                label = "end_scale"
            )

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .scale(endScale)
                    .clip(RoundedCornerShape(18.dp))
                    .testTag("btn_end_call")
                    .clickable(
                        interactionSource = endInteraction,
                        indication = null,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            coordinator.endConversation()
                            onEndCall()
                        }
                    ),
                color = DarkSurfaceElevated,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, RickshawYellow)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = RickshawYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "कॉल संपवा",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = RickshawYellow
                    )
                }
            }
        }
    }
}
