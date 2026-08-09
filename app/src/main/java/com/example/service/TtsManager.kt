package com.example.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

data class VoiceProfile(
    val id: String,
    val displayName: String,
    val locale: String,
    val isNetworkRequired: Boolean = false
)

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
    private var voiceName: String = ""

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            applyConfiguration()

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

    fun configure(
        speechRate: Float = 1.0f,
        pitch: Float = 1.0f,
        languageCode: String = "en-US",
        voiceName: String = ""
    ) {
        this.speechRate = speechRate
        this.pitch = pitch
        this.voiceName = voiceName
        this.language = when (languageCode) {
            "en-GB" -> Locale.UK
            "fil-PH" -> Locale("fil", "PH")
            "en-US" -> Locale.US
            else -> Locale.US
        }
        if (isInitialized) {
            applyConfiguration()
        }
    }

    private fun applyConfiguration() {
        tts?.setSpeechRate(speechRate)
        tts?.setPitch(pitch)

        try {
            val audioAttributes = android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            tts?.setAudioAttributes(audioAttributes)
        } catch (e: Exception) {
            Log.w("TtsManager", "Could not set audio attributes for TTS: ${e.message}")
        }

        val voices = tts?.voices
        if (!voices.isNullOrEmpty()) {
            if (voiceName.isNotBlank()) {
                val matchingVoice = voices.find { it.name == voiceName }
                if (matchingVoice != null) {
                    tts?.voice = matchingVoice
                    return
                }
            }

            // Automatically select the highest quality natural human voice matching target locale
            val bestVoice = findBestNaturalVoice(voices, language)
            if (bestVoice != null) {
                tts?.voice = bestVoice
                return
            }
        }

        tts?.language = language
    }

    private fun findBestNaturalVoice(voices: Set<android.speech.tts.Voice>, targetLocale: Locale): android.speech.tts.Voice? {
        val matchingVoices = voices.filter { voice ->
            voice.locale != null && (
                voice.locale.language.equals(targetLocale.language, ignoreCase = true) ||
                voice.locale.toLanguageTag().equals(targetLocale.toLanguageTag(), ignoreCase = true)
            )
        }

        if (matchingVoices.isEmpty()) return null

        return matchingVoices.maxByOrNull { voice ->
            var score = 0

            score += when (voice.quality) {
                android.speech.tts.Voice.QUALITY_VERY_HIGH -> 500
                android.speech.tts.Voice.QUALITY_HIGH -> 400
                android.speech.tts.Voice.QUALITY_NORMAL -> 300
                else -> 100
            }

            val name = voice.name.lowercase()
            if (name.contains("neural") || name.contains("wavenet") || name.contains("studio")) score += 300
            if (name.contains("natural") || name.contains("premium")) score += 200
            if (name.contains("-x-")) score += 100

            if (voice.locale.country.equals(targetLocale.country, ignoreCase = true) && targetLocale.country.isNotBlank()) {
                score += 150
            }

            if (!voice.isNetworkConnectionRequired) score += 50

            score
        }
    }

    fun getAvailableVoiceProfiles(): List<VoiceProfile> {
        val result = mutableListOf<VoiceProfile>()
        result.add(
            VoiceProfile(
                id = "",
                displayName = "Natural Human Voice (Auto-Select)",
                locale = language.toLanguageTag()
            )
        )

        val voices = tts?.voices
        if (!voices.isNullOrEmpty()) {
            val sortedVoices = voices.sortedWith { v1, v2 ->
                val score1 = calculateVoiceQualityScore(v1, language)
                val score2 = calculateVoiceQualityScore(v2, language)
                score2.compareTo(score1)
            }

            sortedVoices.forEach { voice ->
                val langTag = voice.locale?.toLanguageTag() ?: "en-US"
                val displayLang = voice.locale?.displayName ?: "Standard"
                var friendlyName = voice.name
                    .replace("en-us-x-", "US Natural ")
                    .replace("en-gb-x-", "UK Natural ")
                    .replace("fil-ph-x-", "Tagalog Natural ")
                    .replace("-local", " (Local)")
                    .replace("-network", " (High Fidelity)")

                if (friendlyName.length > 32) {
                    friendlyName = friendlyName.take(32) + "..."
                }

                val qualityTag = if (voice.quality >= android.speech.tts.Voice.QUALITY_HIGH) " ✨ High Quality" else ""

                result.add(
                    VoiceProfile(
                        id = voice.name,
                        displayName = "$friendlyName [$displayLang]$qualityTag",
                        locale = langTag,
                        isNetworkRequired = voice.isNetworkConnectionRequired
                    )
                )
            }
        } else {
            // Built-in presets fallback
            result.add(
                VoiceProfile(
                    id = "en-us-standard",
                    displayName = "English (US) Natural Voice",
                    locale = "en-US"
                )
            )
            result.add(
                VoiceProfile(
                    id = "en-gb-standard",
                    displayName = "English (UK) Natural Voice",
                    locale = "en-GB"
                )
            )
            result.add(
                VoiceProfile(
                    id = "fil-ph-standard",
                    displayName = "Tagalog / Filipino Natural Voice",
                    locale = "fil-PH"
                )
            )
        }
        return result.distinctBy { it.id }
    }

    private fun calculateVoiceQualityScore(voice: android.speech.tts.Voice, targetLocale: Locale): Int {
        var score = 0
        if (voice.locale != null && voice.locale.language.equals(targetLocale.language, ignoreCase = true)) {
            score += 200
        }
        score += when (voice.quality) {
            android.speech.tts.Voice.QUALITY_VERY_HIGH -> 500
            android.speech.tts.Voice.QUALITY_HIGH -> 400
            android.speech.tts.Voice.QUALITY_NORMAL -> 300
            else -> 100
        }
        val name = voice.name.lowercase()
        if (name.contains("neural") || name.contains("wavenet") || name.contains("studio")) score += 300
        if (name.contains("natural") || name.contains("premium")) score += 200
        if (name.contains("-x-")) score += 100
        return score
    }

    private fun formatTextForNaturalSpeech(rawText: String): String {
        if (rawText.isBlank()) return rawText
        return rawText
            .replace("\n", ". ")
            .replace("  ", " ")
            .replace(Regex("(\\d+):(\\d+)"), "$1, verse $2")
            .trim()
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

        val textToSpeak = formatTextForNaturalSpeech(text)

        if (playBell) {
            ChurchBellPlayer.playChurchBell(context) {
                if (isInitialized) {
                    tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                } else {
                    pendingSpeechText = textToSpeak
                    pendingUtteranceId = utteranceId
                }
            }
        } else {
            if (isInitialized) {
                tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            } else {
                pendingSpeechText = textToSpeak
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
