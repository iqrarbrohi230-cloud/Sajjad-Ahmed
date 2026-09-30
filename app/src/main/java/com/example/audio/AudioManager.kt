package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class AudioManager {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var musicJob: Job? = null

    var isSoundEnabled: Boolean = true
    var isMusicEnabled: Boolean = true
        set(value) {
            field = value
            if (value) startBackgroundMusic() else stopBackgroundMusic()
        }

    fun playClick() {
        if (!isSoundEnabled) return
        playTone(frequency = 750.0, durationMs = 30, amplitude = 0.3f, isSine = true)
    }

    fun playDotSelect(colorId: Int) {
        if (!isSoundEnabled) return
        val baseFreq = 440.0 + (colorId * 45.0)
        playTone(frequency = baseFreq, durationMs = 70, amplitude = 0.45f, isSine = true)
    }

    fun playStep(stepIndex: Int) {
        if (!isSoundEnabled) return
        val freq = (350.0 + (stepIndex * 22.0)).coerceAtMost(900.0)
        playTone(frequency = freq, durationMs = 40, amplitude = 0.35f, isSine = true)
    }

    fun playConnect() {
        if (!isSoundEnabled) return
        scope.launch {
            playToneSync(frequency = 587.33, durationMs = 70, amplitude = 0.5f) // D5
            delay(40)
            playToneSync(frequency = 880.0, durationMs = 120, amplitude = 0.6f) // A5
        }
    }

    fun playWin() {
        if (!isSoundEnabled) return
        scope.launch {
            val notes = listOf(523.25, 659.25, 783.99, 1046.50, 1318.51) // C5, E5, G5, C6, E6
            for (freq in notes) {
                playToneSync(frequency = freq, durationMs = 100, amplitude = 0.65f)
                delay(70)
            }
        }
    }

    fun playError() {
        if (!isSoundEnabled) return
        playTone(frequency = 160.0, durationMs = 90, amplitude = 0.4f, isSine = false)
    }

    fun playHint() {
        if (!isSoundEnabled) return
        scope.launch {
            val shimmer = listOf(880.0, 1174.66, 1396.91, 1760.0)
            for (f in shimmer) {
                playToneSync(frequency = f, durationMs = 60, amplitude = 0.45f)
                delay(40)
            }
        }
    }

    fun startBackgroundMusic() {
        if (!isMusicEnabled || musicJob?.isActive == true) return
        musicJob = scope.launch {
            // Calm pentatonic chords sequence (ambient zen puzzle pad)
            val progression = listOf(
                listOf(261.63, 329.63, 392.0), // C Maj
                listOf(220.0, 261.63, 329.63), // A min
                listOf(174.61, 220.0, 261.63), // F Maj
                listOf(196.0, 246.94, 293.66)  // G Maj
            )
            var idx = 0
            while (isActive && isMusicEnabled) {
                val chord = progression[idx % progression.size]
                idx++
                for (note in chord) {
                    if (!isActive || !isMusicEnabled) break
                    playToneSync(frequency = note, durationMs = 350, amplitude = 0.15f)
                    delay(400)
                }
                delay(800)
            }
        }
    }

    fun stopBackgroundMusic() {
        musicJob?.cancel()
        musicJob = null
    }

    private fun playTone(
        frequency: Double,
        durationMs: Int,
        amplitude: Float,
        isSine: Boolean = true
    ) {
        scope.launch {
            playToneSync(frequency, durationMs, amplitude, isSine)
        }
    }

    private fun playToneSync(
        frequency: Double,
        durationMs: Int,
        amplitude: Float,
        isSine: Boolean = true
    ) {
        try {
            val sampleRate = 44100
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val time = i.toDouble() / sampleRate
                val rawWave = if (isSine) {
                    sin(2.0 * Math.PI * frequency * time)
                } else {
                    // Triangle / soft square
                    val t = (time * frequency) % 1.0
                    if (t < 0.5) 4.0 * t - 1.0 else 3.0 - 4.0 * t
                }

                // Envelope: quick attack, smooth linear decay
                val envelope = when {
                    i < sampleRate * 0.008 -> (i / (sampleRate * 0.008))
                    else -> 1.0 - (i.toDouble() / numSamples)
                }.coerceIn(0.0, 1.0)

                val sample = (rawWave * envelope * amplitude * Short.MAX_VALUE).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            Thread.sleep(durationMs.toLong() + 20)
            audioTrack.release()
        } catch (_: Exception) {
            // Ignore audio interruptions safely
        }
    }
}
