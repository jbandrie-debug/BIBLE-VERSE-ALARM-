package com.example.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

object ChurchBellPlayer {

    private const val SAMPLE_RATE = 44100
    @Volatile
    private var isPlaying = false
    private var currentAudioTrack: AudioTrack? = null

    /**
     * Plays a loud, majestic church bell sound.
     * @param context Optional context to ensure audio stream volume is unmuted.
     * @param onDone Callback invoked when the church bell sound finishes playing.
     */
    fun playChurchBell(context: Context? = null, onDone: (() -> Unit)? = null) {
        stop() // Stop any previous playback
        isPlaying = true

        // Ensure stream volumes are unmuted/loud
        context?.let { ctx ->
            try {
                val audioManager = ctx.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                audioManager?.let { am ->
                    val maxMusic = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val maxAlarm = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                    if (am.getStreamVolume(AudioManager.STREAM_MUSIC) < maxMusic / 2) {
                        am.setStreamVolume(AudioManager.STREAM_MUSIC, maxMusic, 0)
                    }
                    if (am.getStreamVolume(AudioManager.STREAM_ALARM) < maxAlarm / 2) {
                        am.setStreamVolume(AudioManager.STREAM_ALARM, maxAlarm, 0)
                    }
                }
            } catch (e: Exception) {
                Log.e("ChurchBellPlayer", "Error adjusting stream volume", e)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            var audioTrack: AudioTrack? = null
            try {
                val pcmData = generateChurchBellPcm()

                val minBufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val bufferSize = (pcmData.size * 2).coerceAtLeast(minBufferSize)
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()

                val audioFormat = AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()

                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                currentAudioTrack = audioTrack

                audioTrack.play()
                audioTrack.write(pcmData, 0, pcmData.size)

                // Calculate total duration in milliseconds
                val durationMs = (pcmData.size.toDouble() / SAMPLE_RATE * 1000).toLong()

                var elapsed = 0L
                val interval = 50L
                while (elapsed < durationMs && isPlaying) {
                    kotlinx.coroutines.delay(interval)
                    elapsed += interval
                }

            } catch (e: Exception) {
                Log.e("ChurchBellPlayer", "Error playing church bell", e)
            } finally {
                try {
                    audioTrack?.stop()
                    audioTrack?.release()
                } catch (e: Exception) {
                    Log.e("ChurchBellPlayer", "Error releasing audio track", e)
                }
                if (currentAudioTrack == audioTrack) {
                    currentAudioTrack = null
                }
                isPlaying = false

                withContext(Dispatchers.Main) {
                    onDone?.invoke()
                }
            }
        }
    }

    fun stop() {
        isPlaying = false
        try {
            currentAudioTrack?.stop()
            currentAudioTrack?.release()
        } catch (e: Exception) {
            Log.e("ChurchBellPlayer", "Error stopping church bell player", e)
        }
        currentAudioTrack = null
    }

    /**
     * Synthesizes PCM 16-bit audio for two loud, resonant cathedral church bell tolls ("DONG... DONG...").
     */
    private fun generateChurchBellPcm(): ShortArray {
        val totalSeconds = 2.2
        val totalSamples = (SAMPLE_RATE * totalSeconds).toInt()
        val pcm = ShortArray(totalSamples)

        val tollStartTime1 = 0.0
        val tollStartTime2 = 0.90
        val baseFreq = 392.0 // G4 pitch (resonant cathedral bell)

        // Partials: (frequencyMultiplier, weight, decayRate)
        val partials = arrayOf(
            doubleArrayOf(0.5, 0.55, 1.1),   // Deep Hum tone (slow decay)
            doubleArrayOf(1.0, 0.90, 1.8),   // Fundamental Prime
            doubleArrayOf(1.2, 0.75, 2.4),   // Minor 3rd (Tierce) - classic sacred bell resonance
            doubleArrayOf(1.5, 0.50, 3.5),   // 5th (Quint)
            doubleArrayOf(2.0, 0.60, 4.2),   // Nominal (Octave)
            doubleArrayOf(2.76, 0.45, 6.5),  // High shimmer
            doubleArrayOf(4.0, 0.35, 12.0)   // Sharp metallic strike transient
        )

        var maxAmp = 0.0
        val tempRaw = DoubleArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sampleVal = 0.0

            // Toll 1
            if (t >= tollStartTime1) {
                val dt = t - tollStartTime1
                val attack = min(1.0, dt / 0.003) // 3ms attack
                for (p in partials) {
                    val freq = baseFreq * p[0]
                    val weight = p[1]
                    val decay = p[2]
                    sampleVal += attack * weight * exp(-decay * dt) * sin(2.0 * Math.PI * freq * dt)
                }
            }

            // Toll 2
            if (t >= tollStartTime2) {
                val dt = t - tollStartTime2
                val attack = min(1.0, dt / 0.003)
                for (p in partials) {
                    val freq = baseFreq * p[0]
                    val weight = p[1]
                    val decay = p[2]
                    sampleVal += attack * weight * exp(-decay * dt) * sin(2.0 * Math.PI * freq * dt)
                }
            }

            tempRaw[i] = sampleVal
            if (Math.abs(sampleVal) > maxAmp) {
                maxAmp = Math.abs(sampleVal)
            }
        }

        // Scale amplitude to 95% maximum volume for loud, crystal-clear, distortion-free sound
        val targetScale = if (maxAmp > 0) (32767.0 * 0.95) / maxAmp else 1.0
        for (i in 0 until totalSamples) {
            pcm[i] = (tempRaw[i] * targetScale).toInt().coerceIn(-32768, 32767).toShort()
        }

        return pcm
    }
}
