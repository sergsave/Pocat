package com.sergsave.purryourcat.vibration

import android.Manifest
import android.content.Context
import android.media.audiofx.Visualizer
import com.sergsave.purryourcat.helpers.PermissionUtils

// Note. Need RECORD_AUDIO Permission.
class AndroidVisualizerBeatDetector(
    private val context: Context,
    private val mediaPlayerSessionId: Int): SoundBeatDetector {

    private var listener: (()->Unit)? = null
    private var _visualizer: Visualizer? = null

    override fun start() {

        val visualizerListener = object: Visualizer.OnDataCaptureListener {
            override fun onWaveFormDataCapture(visualizer: Visualizer?,
                                               waveform: ByteArray?, samplingRate: Int)
            {
                if(visualizer?.enabled != true)
                    return

                val measurement = Visualizer.MeasurementPeakRms()
                visualizer.getMeasurementPeakRms(measurement)
                val threshold = -4500

                if(measurement.mRms > threshold)
                    listener?.invoke()
            }

            override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?,
                                          samplingRate: Int) {}
        }

        if(checkPermission().not())
            return

        if(_visualizer == null) {
            _visualizer = Visualizer(mediaPlayerSessionId).apply {
                val captureSize = 256
                enabled = false // visualizer bug
                setCaptureSize(captureSize)
                measurementMode = Visualizer.MEASUREMENT_MODE_PEAK_RMS
            }
        }

        _visualizer?.setDataCaptureListener(visualizerListener, Visualizer.getMaxCaptureRate(),
            true, false)
        _visualizer?.enabled = true
    }

    override fun stop() {
        if(checkPermission()) {
            _visualizer?.enabled = false
            // https://issuetracker.google.com/issues/36956120
            _visualizer?.setDataCaptureListener(null, 0, false, false)
        }
    }

    override fun release() {
        stop()

        if(checkPermission()) {
            _visualizer?.setDataCaptureListener(null, 0, false, false)
            _visualizer?.release()
        }

        _visualizer = null
    }

    private fun checkPermission() =
        PermissionUtils.checkPermission(context, Manifest.permission.RECORD_AUDIO)

    override fun setOnBeatDetectedListener(listener: ()->Unit) {
        this.listener = listener
    }
}