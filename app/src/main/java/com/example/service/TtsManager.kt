package com.example.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TtsManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingSpeechText: String? = null
    private var pendingUtteranceId: String? = null
    private var pendingOnStart: (() -> Unit)? = null
    private var pendingOnDone: (() -> Unit)? = null

    private var speechRate: Float = 1.0f
    private var pitch: Float = 1.0f
    private var language: Locale = Locale.US

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.language = language
            tts?.setSpeechRate(speechRate)
            tts?.setPitch(pitch)

            // Setup UtteranceListener
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.d("TtsManager", "TTS Started speaking: $utteranceId")
                    val onStartCb = pendingOnStart
                    pendingOnStart = null
                    onStartCb?.invoke()
                }

                override fun onDone(utteranceId: String?) {
                    Log.d("TtsManager", "TTS Finished speaking: $utteranceId")
                    val onDoneCb = pendingOnDone
                    pendingOnDone = null
                    onDoneCb?.invoke()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    Log.e("TtsManager", "TTS Error speaking: $utteranceId")
                    val onStartCb = pendingOnStart
                    pendingOnStart = null
                    onStartCb?.invoke()

                    val onDoneCb = pendingOnDone
                    pendingOnDone = null
                    onDoneCb?.invoke()
                }
            })

            // Speak pending if queued during initialization
            pendingSpeechText?.let { text ->
                speak(
                    text = text,
                    utteranceId = pendingUtteranceId ?: "UTTERANCE_VERSE",
                    playBell = false,
                    onStart = pendingOnStart,
                    onDone = pendingOnDone
                )
                pendingSpeechText = null
            }
        } else {
            Log.e("TtsManager", "TTS Initialization failed with status $status")
            val onStartCb = pendingOnStart
            pendingOnStart = null
            onStartCb?.invoke()

            val onDoneCb = pendingOnDone
            pendingOnDone = null
            onDoneCb?.invoke()
        }
    }

    fun configure(speechRate: Float = 1.0f, pitch: Float = 1.0f, languageCode: String = "en-US") {
        this.speechRate = speechRate
        this.pitch = pitch
        this.language = when (languageCode) {
            "en-GB" -> Locale.UK
            "en-US" -> Locale.US
            else -> Locale.US
        }
        if (isInitialized) {
            tts?.setSpeechRate(speechRate)
            tts?.setPitch(pitch)
            tts?.language = language
        }
    }

    fun speak(
        text: String,
        utteranceId: String = "UTTERANCE_VERSE",
        playBell: Boolean = true,
        onStart: (() -> Unit)? = null,
        onDone: (() -> Unit)? = null
    ) {
        this.pendingOnStart = onStart
        this.pendingOnDone = onDone
        ChurchBellPlayer.stop()

        if (playBell) {
            ChurchBellPlayer.playChurchBell(context) {
                if (isInitialized) {
                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                } else {
                    pendingSpeechText = text
                    pendingUtteranceId = utteranceId
                }
            }
        } else {
            if (isInitialized) {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            } else {
                pendingSpeechText = text
                pendingUtteranceId = utteranceId
            }
        }
    }

    fun stop() {
        ChurchBellPlayer.stop()
        if (isInitialized) {
            tts?.stop()
        }
    }

    fun shutdown() {
        ChurchBellPlayer.stop()
        if (isInitialized) {
            tts?.stop()
            tts?.shutdown()
            isInitialized = false
        }
    }
}
