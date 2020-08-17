package com.sergsave.purryourcat.fragments

import android.Manifest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.*
import android.view.MotionEvent.ACTION_MOVE
import androidx.fragment.app.Fragment
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.ImageUtils
import com.sergsave.purryourcat.helpers.PermissionUtils
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.preference.PreferenceReader
import com.sergsave.purryourcat.vibration.*
import kotlinx.android.synthetic.main.fragment_purring.*
import java.util.*
import kotlin.concurrent.schedule

// TODO: Implement sound listener version without permission.

class PurringFragment : Fragment() {

    interface OnImageLoadedListener {
        fun onImageLoaded()
    }

    var onImageLoadedListener: OnImageLoadedListener? = null

    private var transitionName: String? = null
    private var catData: CatData? = null
    private var mediaPlayer: MediaPlayer? = null
    private var playerTimeoutTimer: Timer? = null
    private var vibrator: RythmOfSoundVibrator? = null

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            transitionName = it.getString(ARG_TRANSITION_NAME)
            catData = it.getParcelable<CatData>(ARG_CAT_DATA)
        }
    }

    override fun onStop() {
        val player = mediaPlayer
        mediaPlayer = null

        player?.release()
        vibrator?.release()

        activity?.setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE)

        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        val audioUri = catData?.purrAudioUri
        if(audioUri == null || context == null)
            return

        activity?.setVolumeControlStream(AudioManager.STREAM_MUSIC)
        mediaPlayer = MediaPlayer.create(requireContext(), audioUri)?.apply { setLooping(true) }

        if(PreferenceReader(requireContext()).isVibrationEnabled.not())
            return

        prepareBeatDetectorAsync{ detector ->
            if(detector != null && context != null)
                vibrator = RythmOfSoundVibrator(requireContext(), detector)
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

        // Shared element transition
        photo_image.setTransitionName(transitionName)
        photo_image.setOnTouchListener { _, event ->
            if(event.getAction() == ACTION_MOVE)
                playAudio()
            true
        }

        ImageUtils.loadInto(context, catData?.photoUri, photo_image, {
            onImageLoadedListener?.onImageLoaded()
        })
    }

    private fun prepareBeatDetectorAsync(callback: (SoundBeatDetector?)->Unit ) {
        val sessionId = mediaPlayer?.getAudioSessionId()
        if(sessionId == null || context == null) {
            callback(null)
            return
        }

        val make = { context?.let { AndroidVisualizerBeatDetector(it, sessionId) } }
        val permission = Manifest.permission.RECORD_AUDIO
        if(PermissionUtils.checkPermission(requireContext(), permission))
            callback(make())
        else {
            PermissionUtils.requestPermissions(this, arrayOf(permission), PERMISSION_RECORD_AUDIO_CODE)
            onPermissionResultCallback = { res -> callback(if(res) make() else null) }
        }
    }

    private var onPermissionResultCallback: ((Boolean)->Unit)? = null

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == PERMISSION_RECORD_AUDIO_CODE &&
            PermissionUtils.checkRequestResult(grantResults))
            onPermissionResultCallback?.invoke(true)
        else
            onPermissionResultCallback?.invoke(false)

        onPermissionResultCallback = null
    }

    private fun playAudio() {
        if(mediaPlayer == null)
            return
        mediaPlayer?.start()
        vibrator?.start()

        playerTimeoutTimer?.cancel()
        playerTimeoutTimer?.purge()
        playerTimeoutTimer = Timer("AudioTimeout", false)
        playerTimeoutTimer?.schedule(AUDIO_TIMEOUT.toLong()) {
            mediaPlayer?.pause()
            vibrator?.stop()
        }
    }

    companion object {
        private val AUDIO_TIMEOUT = 2000
        private val PERMISSION_RECORD_AUDIO_CODE = 1000

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
