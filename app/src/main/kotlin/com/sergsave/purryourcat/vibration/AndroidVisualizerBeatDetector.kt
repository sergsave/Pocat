package com.sergsave.purryourcat.vibration

import android.Manifest
import android.content.Context
import android.media.audiofx.Visualizer
import com.sergsave.purryourcat.fragments.CatFormFragment
import com.sergsave.purryourcat.helpers.PermissionUtils

// Note. Need RECORD_AUDIO Permission.
class AndroidVisualizerBeatDetector(
    private val context: Context,
    private val mediaPlayerSessionId: Int): ISoundBeatDetector {

    private var listener: (()->Unit)? = null
    private var visualizer: Visualizer? = null
    private val visualizerListener = object: Visualizer.OnDataCaptureListener {
        override fun onWaveFormDataCapture(visualizer: Visualizer?,
                                           waveform: ByteArray?, samplingRate: Int)
        {
            // Check "this" visualizer, because callback may call after release
            if(this@AndroidVisualizerBeatDetector.visualizer?.enabled?.not() ?: true)
                return

            var measurement = Visualizer.MeasurementPeakRms()
            visualizer?.getMeasurementPeakRms(measurement)
            val threshold = -4500

            if(measurement.mRms > threshold)
                listener?.invoke()
        }

        override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?,
                                      samplingRate: Int) {}
    }

    override fun start() {
        if(checkPermission().not())
            return

        if(visualizer == null) {
            visualizer = Visualizer(mediaPlayerSessionId).apply {
                setDataCaptureListener(
                    visualizerListener,
                    Visualizer.getMaxCaptureRate(),
                    true,
                    false
                )
                val captureSize = 256
                setCaptureSize(captureSize)
                setMeasurementMode(Visualizer.MEASUREMENT_MODE_PEAK_RMS)
            }
        }

        visualizer?.setEnabled(true)
    }

    override fun stop() {
        if(checkPermission())
            visualizer?.setEnabled(false)
    }

    override fun release() {
        stop()
        val _visualizer = visualizer
        visualizer = null

        if(checkPermission())
            _visualizer?.release()
    }

    private fun checkPermission() =
        PermissionUtils.checkPermission(context, Manifest.permission.RECORD_AUDIO)

    override fun setOnBeatDetectedListener(listener: ()->Unit) {
        this.listener = listener
    }
}