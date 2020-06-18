package com.sergsave.purryourcat.fragments

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.*
import android.view.MotionEvent.ACTION_MOVE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.android.synthetic.main.fragment_purring.*
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.*
import com.sergsave.purryourcat.models.CatData
import java.util.Timer
import kotlin.concurrent.schedule

class PurringFragment : Fragment() {

    interface OnEditRequestedListener {
        fun onEditRequested()
    }

    interface OnImageLoadedListener {
        fun onImageLoaded()
    }

    private var transitionName: String? = null
    private var catData: CatData? = null
    private var onEditListener: OnEditRequestedListener? = null
    private var onLoadListener: OnImageLoadedListener? = null
    private var mediaPlayer: MediaPlayer? = null
    private var playerTimeoutTimer: Timer? = null

    private var visualizer: Visualizer? = null

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStop() {
        visualizer?.setEnabled(false)
        // TODO: save media player state on orientation change
        if(mediaPlayer?.isPlaying ?:false) mediaPlayer?.pause()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFadeThrough.create(requireContext())
        arguments?.let {
            transitionName = it.getString(ARG_TRANSITION_NAME)
            catData = it.getParcelable<CatData>(ARG_CAT_DATA)
        }

        // TODO: rollback on destroy?
        activity?.setVolumeControlStream(AudioManager.STREAM_MUSIC)

        catData?.purrAudioUri?.let {
            mediaPlayer = MediaPlayer.create(requireContext(), it).apply { setLooping(true) }
        }

        // TODO: Separated class or service
        val sessionId = mediaPlayer?.getAudioSessionId()
        if(sessionId == null)
            return

        visualizer = Visualizer(sessionId)
        val listener = object: Visualizer.OnDataCaptureListener {
            override fun onWaveFormDataCapture(visualizer: Visualizer?, waveform: ByteArray?,
                                               samplingRate: Int)
            {
                if(visualizer == null || visualizer.enabled.not())
                    return

                var measurement = Visualizer.MeasurementPeakRms()
                visualizer.getMeasurementPeakRms(measurement)
                val threshold = -4500

                if(measurement.mRms > threshold)
                    vibrate(20)
            }

            override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?,
                                          samplingRate: Int) {}
        }

        visualizer?.apply {
            setDataCaptureListener(listener, Visualizer.getMaxCaptureRate(), true, false)
            val captureSize = 256
            setCaptureSize(captureSize)
            setMeasurementMode(Visualizer.MEASUREMENT_MODE_PEAK_RMS)
            setEnabled(true)
        }
    }

    private fun vibrate(durationMs: Long) {
        val vibrator = activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        val pattern : LongArray = longArrayOf(0, durationMs)
        if (vibrator?.hasVibrator() ?: false) {
            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                vibrator?.vibrate(pattern, -1)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_purring, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = getActivity() as AppCompatActivity?
        activity?.getSupportActionBar()?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setTitle(getResources().getString(R.string.purring_title))
        }

        setHasOptionsMenu(true)

        // Shared element transition
        photo_image.setTransitionName(transitionName)
        photo_image.setOnTouchListener { _, event ->
            if(event.getAction() == ACTION_MOVE)
                playAudio()
            true
        }

        ImageUtils.loadInto(context, catData?.photoUri, photo_image, {
            onLoadListener?.onImageLoaded()
        })
    }

    fun setOnEditRequestedListener(listener: OnEditRequestedListener) {
        onEditListener = listener
    }

    fun setOnImageLoadedListener(listener: OnImageLoadedListener) {
        onLoadListener = listener
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_purring, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_edit -> {
            onEditListener?.onEditRequested()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun playAudio() {
        if(mediaPlayer == null)
            return
        mediaPlayer?.start()

        playerTimeoutTimer?.cancel()
        playerTimeoutTimer?.purge()
        playerTimeoutTimer = Timer("AudioTimeout", false)
        playerTimeoutTimer?.schedule(AUDIO_TIMEOUT.toLong()) { mediaPlayer?.pause() }
    }

    companion object {
        private val AUDIO_TIMEOUT = 2000

        private val ARG_TRANSITION_NAME = "TransitionName"
        private val ARG_CAT_DATA = "CatData"

        @JvmStatic
        fun newInstance(sharedElementTransitionName: String?, catData: CatData) =
            PurringFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TRANSITION_NAME, sharedElementTransitionName)
                    putParcelable(ARG_CAT_DATA, catData)
                }
            }
    }
}
