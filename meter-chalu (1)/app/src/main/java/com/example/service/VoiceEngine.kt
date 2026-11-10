package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class VoiceEngine(
    private val context: Context,
    private val onSpeechRecognized: (String) -> Unit,
    private val onAgentTtsFinished: () -> Unit,
    private val onAudioLevelChanged: (Float) -> Unit
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var currentRate = 1.0f

    init {
        tts = TextToSpeech(context.applicationContext, this)
        initSpeechRecognizer()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            // Try Marathi locale, fallback to Hindi or Default
            val mrLocale = Locale("mr", "IN")
            val result = tts?.setLanguage(mrLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale("hi", "IN"))
            }
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    onAgentTtsFinished()
                }
                override fun onError(utteranceId: String?) {
                    onAgentTtsFinished()
                }
            })
        }
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {
                    val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                    onAudioLevelChanged(normalized)
                }
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    isListening = false
                    Log.w("VoiceEngine", "Speech recognizer error: $error")
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val text = matches[0]
                        onSpeechRecognized(text)
                    }
                    isListening = false
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        onAudioLevelChanged(0.8f)
                    }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    fun speakPassenger(text: String, slow: Boolean = false) {
        if (!isTtsReady) {
            onAgentTtsFinished()
            return
        }
        currentRate = if (slow) 0.65f else 0.95f
        tts?.setSpeechRate(currentRate)
        tts?.setPitch(1.0f)
        val mrLocale = Locale("mr", "IN")
        val res = tts?.setLanguage(mrLocale)
        if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts?.setLanguage(Locale("hi", "IN"))
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "passenger_turn")
    }

    fun speakCoach(text: String) {
        if (!isTtsReady) {
            onAgentTtsFinished()
            return
        }
        tts?.setSpeechRate(0.95f)
        tts?.setPitch(1.1f)
        tts?.setLanguage(Locale("hi", "IN"))
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "coach_feedback")
    }

    fun startListening() {
        if (speechRecognizer == null) initSpeechRecognizer()
        if (isListening) return

        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "mr-IN")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "mr-IN")
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            speechRecognizer?.startListening(intent)
            isListening = true
        } catch (e: Exception) {
            Log.e("VoiceEngine", "Could not start speech listening", e)
            isListening = false
        }
    }

    fun stopListening() {
        if (isListening) {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.e("VoiceEngine", "Error stopping recognizer", e)
            }
            isListening = false
        }
    }

    fun stopSpeaking() {
        tts?.stop()
    }

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
    }
}
