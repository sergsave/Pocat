package com.sergsave.purryourcat.vibration

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import io.reactivex.disposables.Disposable

class RythmOfSoundVibrator(
    private val context: Context,
    private val beatDetector: SoundBeatDetector) {

    interface OnPrepareFinishedListener {
        fun onSuccess()
        fun onFailed()
    }

    var onPrepareFinishedListener: OnPrepareFinishedListener? = null

    private var prepareDisposable: Disposable? = null
    private var workDisposable: Disposable? = null

    fun prepareAsync() {
        if (prepareDisposable != null)
            return

        prepareDisposable = beatDetector
            .prepare()
            .subscribe (
                {
                    onPrepareFinishedListener?.onSuccess()
                },
                {
                    release()
                    onPrepareFinishedListener?.onFailed()
                }
            )

    }

    // Do nothing in non prepared state
    fun start() {
        if (prepareDisposable == null || workDisposable != null)
            return

        val duration = beatDetector.detectionPeriodMs / 3
        workDisposable = beatDetector.detect().subscribe {
            vibrate(duration)
        }
    }

    fun stop() {
        workDisposable?.dispose()
        workDisposable = null
    }

    fun release() {
        stop()
        beatDetector.release()
        prepareDisposable?.dispose()
        prepareDisposable = null
    }

    private fun vibrate(durationMs: Long) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        val pattern : LongArray = longArrayOf(0, durationMs)
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION") vibrator.vibrate(pattern, -1)
            }
        }
    }
}