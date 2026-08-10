package com.example.service

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class ElevenLabsTtsManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun synthesizeAndPlay(
        text: String,
        apiKey: String,
        voiceId: String,
        onCompletion: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ): Boolean = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            withContext(Dispatchers.Main) {
                onError?.invoke("ElevenLabs API key is missing.")
            }
            return@withContext false
        }

        val effectiveVoiceId = voiceId.ifBlank { "21m00Tcm4TlvDq8ikWAM" } // Default fallback voice
        val url = "https://api.elevenlabs.io/v1/text-to-speech/$effectiveVoiceId"

        val jsonBody = JSONObject().apply {
            put("text", text)
            put("model_id", "eleven_multilingual_v2")
            put("voice_settings", JSONObject().apply {
                put("stability", 0.5)
                put("similarity_boost", 0.75)
                put("style", 0.0)
                put("use_speaker_boost", true)
            })
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .addHeader("xi-api-key", apiKey.trim())
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "audio/mpeg")
            .post(requestBody)
            .build()

        try {
            stop() // Stop any current audio

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: "HTTP ${response.code}"
                Log.e("ElevenLabsTts", "ElevenLabs API Error: $errBody")
                withContext(Dispatchers.Main) {
                    onError?.invoke("ElevenLabs API Error (${response.code}): $errBody")
                }
                return@withContext false
            }

            val audioBytes = response.body?.bytes()
            if (audioBytes == null || audioBytes.isEmpty()) {
                withContext(Dispatchers.Main) {
                    onError?.invoke("Empty audio response received from ElevenLabs.")
                }
                return@withContext false
            }

            val tempFile = File(context.cacheDir, "elevenlabs_temp_voice.mp3")
            FileOutputStream(tempFile).use { fos ->
                fos.write(audioBytes)
            }

            withContext(Dispatchers.Main) {
                try {
                    mediaPlayer = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build()
                        )
                        setDataSource(tempFile.absolutePath)
                        prepare()
                        setOnCompletionListener {
                            onCompletion?.invoke()
                            stop()
                        }
                        start()
                    }
                } catch (e: Exception) {
                    Log.e("ElevenLabsTts", "Error playing audio file", e)
                    onError?.invoke("Playback error: ${e.message}")
                }
            }
            return@withContext true
        } catch (e: Exception) {
            Log.e("ElevenLabsTts", "Exception during ElevenLabs synthesis", e)
            withContext(Dispatchers.Main) {
                onError?.invoke("Synthesis failed: ${e.localizedMessage}")
            }
            return@withContext false
        }
    }

    fun stop() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.w("ElevenLabsTts", "Error stopping player", e)
        } finally {
            mediaPlayer = null
        }
    }
}
