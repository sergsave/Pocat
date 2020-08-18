package com.sergsave.purryourcat.vibration

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

class RythmOfSoundVibrator(
    private val context: Context,
    private val beatDetector: SoundBeatDetector) {

    init {
        val vibrationDuration: Long = 20
        beatDetector.setOnBeatDetectedListener { vibrate(vibrationDuration) }
    }

    fun start() = beatDetector.start()
    fun stop() = beatDetector.stop()
    fun release() = beatDetector.release()

    private fun vibrate(durationMs: Long) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        val pattern : LongArray = longArrayOf(0, durationMs)
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                vibrator.vibrate(pattern, -1)
            }
        }
    }
}