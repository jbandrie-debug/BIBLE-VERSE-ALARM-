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
        this.language = when {
            languageCode.contains("GB", ignoreCase = true) -> Locale.UK
            languageCode.contains("fil", ignoreCase = true) || languageCode.contains("tl", ignoreCase = true) || languageCode.contains("tagalog", ignoreCase = true) -> Locale("fil", "PH")
            else -> Locale.US
        }
        if (isInitialized) {
            applyConfiguration()
        }
    }

    private fun applyConfiguration() {
        var effectivePitch = pitch
        var effectiveRate = speechRate

        when (voiceName) {
            "preset-old-male" -> {
                effectivePitch = 0.78f // Deep, warm elder male / pastor tone
                effectiveRate = 0.88f  // Calm, reverent reading speed
            }
            "preset-soft-female" -> {
                effectivePitch = 1.08f // Soft, warm female tone
                effectiveRate = 0.92f  // Gentle devotional reading speed
            }
            "preset-soft-warm" -> {
                effectivePitch = 0.95f // Soft, balanced warm pitch
                effectiveRate = 0.90f  // Smooth inspirational reading
            }
            "preset-tagalog-male" -> {
                effectivePitch = 0.82f // Deep Tagalog fatherly voice
                effectiveRate = 0.88f  // Reverent Tagalog reading
            }
            "preset-tagalog-female" -> {
                effectivePitch = 1.06f // Soft Tagalog female voice
                effectiveRate = 0.92f  // Gentle Tagalog reading
            }
        }

        tts?.setSpeechRate(effectiveRate)
        tts?.setPitch(effectivePitch)

        try {
            val audioAttributes = android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            tts?.setAudioAttributes(audioAttributes)
        } catch (e: Exception) {
            Log.w("TtsManager", "Could not set audio attributes for TTS: ${e.message}")
        }

        // Set default language first
        tts?.language = language

        val voices = tts?.voices
        if (!voices.isNullOrEmpty()) {
            var selectedVoice: android.speech.tts.Voice? = null

            if (voiceName.isNotBlank()) {
                selectedVoice = voices.find { it.name == voiceName }
                    ?: when (voiceName) {
                        "preset-old-male" -> findBestVoiceByGenderAndLocale(voices, language, preferFemale = false)
                        "preset-soft-female" -> findBestVoiceByGenderAndLocale(voices, language, preferFemale = true)
                        "preset-soft-warm" -> findBestNaturalVoice(voices, language)
                        "preset-tagalog-male" -> findBestVoiceByGenderAndLocale(voices, Locale("fil", "PH"), preferFemale = false)
                        "preset-tagalog-female" -> findBestVoiceByGenderAndLocale(voices, Locale("fil", "PH"), preferFemale = true)
                        "en-us-standard" -> findBestNaturalVoice(voices, Locale.US)
                        "en-gb-standard" -> findBestNaturalVoice(voices, Locale.UK)
                        "fil-ph-standard" -> findBestNaturalVoice(voices, Locale("fil", "PH")) ?: findBestNaturalVoice(voices, Locale("tl", "PH"))
                        else -> null
                    }
            }

            if (selectedVoice == null) {
                selectedVoice = findBestNaturalVoice(voices, language)
            }

            if (selectedVoice != null) {
                if (selectedVoice.locale != null) {
                    try {
                        tts?.language = selectedVoice.locale
                    } catch (e: Exception) {
                        Log.w("TtsManager", "Could not set locale ${selectedVoice.locale}: ${e.message}")
                    }
                }
                tts?.voice = selectedVoice
            }
        }
    }

    private fun findBestVoiceByGenderAndLocale(
        voices: Set<android.speech.tts.Voice>,
        targetLocale: Locale,
        preferFemale: Boolean
    ): android.speech.tts.Voice? {
        val isFilipinoTarget = targetLocale.language.equals("fil", ignoreCase = true) ||
                targetLocale.language.equals("tl", ignoreCase = true) ||
                targetLocale.toLanguageTag().contains("fil", ignoreCase = true)

        val matchingVoices = voices.filter { voice ->
            if (voice.locale == null) return@filter false
            val lang = voice.locale.language.lowercase()
            val name = voice.name.lowercase()
            if (isFilipinoTarget) {
                lang == "fil" || lang == "tl" || name.contains("fil") || name.contains("tagalog") || name.contains("ph")
            } else {
                lang.equals(targetLocale.language, ignoreCase = true) ||
                voice.locale.toLanguageTag().equals(targetLocale.toLanguageTag(), ignoreCase = true)
            }
        }

        val candidates = if (matchingVoices.isNotEmpty()) matchingVoices else voices.toList()

        return candidates.maxByOrNull { voice ->
            var score = calculateVoiceQualityScore(voice, targetLocale)
            val name = voice.name.lowercase()

            val isFemaleVoice = name.contains("female") || name.contains("woman") || name.contains("f_") ||
                    name.contains("-f-") || name.contains("soft") || name.contains("-sfg") || name.contains("-sfe")
            val isMaleVoice = name.contains("male") || name.contains("man") || name.contains("m_") ||
                    name.contains("-m-") || name.contains("deep") || name.contains("-iom") || name.contains("-iog")

            if (preferFemale) {
                if (isFemaleVoice) score += 500
                if (isMaleVoice) score -= 300
            } else {
                if (isMaleVoice) score += 500
                if (isFemaleVoice) score -= 300
            }

            score
        }
    }

    private fun findBestNaturalVoice(voices: Set<android.speech.tts.Voice>, targetLocale: Locale): android.speech.tts.Voice? {
        val isFilipinoTarget = targetLocale.language.equals("fil", ignoreCase = true) ||
                targetLocale.language.equals("tl", ignoreCase = true) ||
                targetLocale.toLanguageTag().contains("fil", ignoreCase = true)

        val matchingVoices = voices.filter { voice ->
            if (voice.locale == null) return@filter false
            val lang = voice.locale.language.lowercase()
            val name = voice.name.lowercase()
            if (isFilipinoTarget) {
                lang == "fil" || lang == "tl" || name.contains("fil") || name.contains("tagalog") || name.contains("ph")
            } else {
                lang.equals(targetLocale.language, ignoreCase = true) ||
                voice.locale.toLanguageTag().equals(targetLocale.toLanguageTag(), ignoreCase = true)
            }
        }

        val candidates = if (matchingVoices.isNotEmpty()) matchingVoices else voices.toList()

        return candidates.maxByOrNull { voice ->
            calculateVoiceQualityScore(voice, targetLocale)
        }
    }

    fun getAvailableVoiceProfiles(): List<VoiceProfile> {
        val result = mutableListOf<VoiceProfile>()
        result.add(
            VoiceProfile(
                id = "preset-soft-warm",
                displayName = "🕊️ Soft & Warm Natural Voice (Inspirational) ✨",
                locale = language.toLanguageTag()
            )
        )
        result.add(
            VoiceProfile(
                id = "preset-old-male",
                displayName = "👴 Deep Old Male / Elder Pastor (Warm & Resonant) ✨",
                locale = language.toLanguageTag()
            )
        )
        result.add(
            VoiceProfile(
                id = "preset-soft-female",
                displayName = "👩 Soft & Warm Female Devotional Voice ✨",
                locale = language.toLanguageTag()
            )
        )
        result.add(
            VoiceProfile(
                id = "preset-tagalog-female",
                displayName = "🇵🇭 Tagalog / Filipino Soft Female Voice ✨",
                locale = "fil-PH"
            )
        )
        result.add(
            VoiceProfile(
                id = "preset-tagalog-male",
                displayName = "🇵🇭 Tagalog / Filipino Fatherly Male Voice ✨",
                locale = "fil-PH"
            )
        )
        result.add(
            VoiceProfile(
                id = "fil-ph-standard",
                displayName = "🇵🇭 Tagalog / Filipino Realistic Natural Voice ✨",
                locale = "fil-PH"
            )
        )
        result.add(
            VoiceProfile(
                id = "en-us-standard",
                displayName = "🇺🇸 English (US) Realistic Human Voice ✨",
                locale = "en-US"
            )
        )
        result.add(
            VoiceProfile(
                id = "en-gb-standard",
                displayName = "🇬🇧 English (UK) Realistic Human Voice ✨",
                locale = "en-GB"
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
                    .replace("tl-ph-x-", "Tagalog Natural ")
                    .replace("-local", " (Local)")
                    .replace("-network", " (High Fidelity)")

                if (friendlyName.length > 32) {
                    friendlyName = friendlyName.take(32) + "..."
                }

                val isHighQuality = voice.quality >= android.speech.tts.Voice.QUALITY_HIGH ||
                        voice.name.lowercase().contains("neural") ||
                        voice.name.lowercase().contains("wavenet") ||
                        voice.name.lowercase().contains("natural")

                val qualityTag = if (isHighQuality) " ✨ High Quality Human Voice" else ""

                result.add(
                    VoiceProfile(
                        id = voice.name,
                        displayName = "$friendlyName [$displayLang]$qualityTag",
                        locale = langTag,
                        isNetworkRequired = voice.isNetworkConnectionRequired
                    )
                )
            }
        }
        return result.distinctBy { it.id }
    }

    private fun calculateVoiceQualityScore(voice: android.speech.tts.Voice, targetLocale: Locale): Int {
        var score = 0
        val isFilipinoTarget = targetLocale.language.equals("fil", ignoreCase = true) ||
                targetLocale.language.equals("tl", ignoreCase = true)

        if (voice.locale != null) {
            val voiceLang = voice.locale.language.lowercase()
            if (isFilipinoTarget && (voiceLang == "fil" || voiceLang == "tl" || voice.name.lowercase().contains("tagalog"))) {
                score += 500
            } else if (!isFilipinoTarget && voiceLang.equals(targetLocale.language, ignoreCase = true)) {
                score += 300
            }
        }

        score += when (voice.quality) {
            android.speech.tts.Voice.QUALITY_VERY_HIGH -> 600
            android.speech.tts.Voice.QUALITY_HIGH -> 500
            android.speech.tts.Voice.QUALITY_NORMAL -> 300
            else -> 100
        }

        val name = voice.name.lowercase()
        if (name.contains("neural") || name.contains("wavenet") || name.contains("studio")) score += 400
        if (name.contains("natural") || name.contains("premium") || name.contains("high")) score += 300
        if (name.contains("-x-")) score += 100

        if (!voice.isNetworkConnectionRequired) score += 50

        return score
    }

    private fun formatTextForNaturalSpeech(rawText: String): String {
        if (rawText.isBlank()) return rawText
        var text = rawText
            .replace("\n", ". ")
            .replace("  ", " ")

        // Detect if Tagalog / Filipino content
        val isTagalogText = text.contains("Juan", ignoreCase = true) ||
                text.contains("Mateo", ignoreCase = true) ||
                text.contains("Marcos", ignoreCase = true) ||
                text.contains("Lucas", ignoreCase = true) ||
                text.contains("Panginoon", ignoreCase = true) ||
                text.contains("Diyos", ignoreCase = true) ||
                text.contains("Panalangin", ignoreCase = true) ||
                text.contains("Salamat", ignoreCase = true) ||
                text.contains("Mga", ignoreCase = true) ||
                language.language.equals("fil", ignoreCase = true) ||
                language.language.equals("tl", ignoreCase = true)

        if (isTagalogText) {
            // Convert chapter:verse formatting naturally into Tagalog speech phrasing
            text = text.replace(Regex("(\\d+):(\\d+)"), "kapitulo $1, bersikulo $2")
        } else {
            text = text.replace(Regex("(\\d+):(\\d+)"), "chapter $1, verse $2")
        }

        return text.trim()
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
                    applyConfiguration()
                    tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                } else {
                    pendingSpeechText = textToSpeak
                    pendingUtteranceId = utteranceId
                }
            }
        } else {
            if (isInitialized) {
                applyConfiguration()
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
