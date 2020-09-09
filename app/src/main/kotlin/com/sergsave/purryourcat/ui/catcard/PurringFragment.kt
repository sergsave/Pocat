package com.sergsave.purryourcat.ui.catcard

import android.Manifest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.content.Intent
import android.view.*
import android.view.MotionEvent.ACTION_MOVE
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.sergsave.purryourcat.ui.catcard.PurringViewModel.MenuState
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.MyApplication
import com.sergsave.purryourcat.helpers.ImageUtils
import com.sergsave.purryourcat.helpers.PermissionUtils
import com.sergsave.purryourcat.helpers.EventObserver
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.vibration.*
import kotlinx.android.synthetic.main.fragment_purring.*
import java.util.*
import kotlin.concurrent.schedule

// TODO: Implement sound listener version without permission.

class PurringFragment : Fragment() {

    private val navigation: NavigationViewModel by activityViewModels()
    private val viewModel: PurringViewModel by viewModels {
        val inputData = arguments?.getString(ARG_CAT_ID)?.let {
            PurringViewModel.InputData.Saved(it)
        } ?: run {
            val data = arguments?.getParcelable<CatData>(ARG_CAT_DATA) ?: CatData()
            PurringViewModel.InputData.Unsaved(data)
        }
        (requireActivity().application as MyApplication).appContainer
            .providePurringViewModelFactory(inputData)
    }

    private var mediaPlayer: MediaPlayer? = null
    private var playerTimeoutTimer: Timer? = null
    private var vibrator: RythmOfSoundVibrator? = null

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStop() {
        deinitAudio()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        initAudio(viewModel.catData.value?.purrAudioUri) // In case of restart
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
        photo_image.transitionName = arguments?.getString(ARG_TRANSITION_NAME)
        photo_image.setOnTouchListener { _, event ->
            if(event.action == ACTION_MOVE)
                playAudio()
            true
        }

        setHasOptionsMenu(true)

        (activity as? AppCompatActivity)?.supportActionBar?.title =
            resources.getString(R.string.purring_title)

        navigation.backPressedEvent.observe(viewLifecycleOwner, EventObserver {
            navigation.goToBackScreen()
        })

        viewModel.apply {
            catData.observe(viewLifecycleOwner, Observer {
                ImageUtils.loadInto(context, it.photoUri, photo_image) {
                    navigation.startSharedElementTransition()
                }

                initAudio(it.purrAudioUri)
            })

            menuState.observe(viewLifecycleOwner, Observer {
                activity?.invalidateOptionsMenu()
            })

            sharingSuccessEvent.observe(viewLifecycleOwner, EventObserver {
                startActivity(it)
            })

            sharingFailedEvent.observe(viewLifecycleOwner, EventObserver {
                Snackbar.make(main_layout, it, Snackbar.LENGTH_LONG).show()
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val state = viewModel.menuState.value

        val menuId = when(state) {
            MenuState.SHOW_SAVED, MenuState.SHARING -> R.menu.menu_show_saved_cat
            MenuState.SHOW_UNSAVED -> R.menu.menu_show_not_saved_cat
            else -> return
        }

        inflater.inflate(menuId, menu)

        menu.findItem(R.id.action_share)?.apply {
            if(viewModel.menuState.value == MenuState.SHARING)
                setActionView(R.layout.view_loader)
            else
                setActionView(null)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = arguments?.getString(ARG_CAT_ID)
        when (item.itemId) {
            R.id.action_edit -> id?.let{ navigation.editCat(it) }
            R.id.action_share -> viewModel.startSharing()
            R.id.action_save -> viewModel.saveData()
            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }

    fun initAudio(audioUri: Uri?) {
        if(audioUri == null || context == null)
            return

        activity?.volumeControlStream = AudioManager.STREAM_MUSIC
        if(mediaPlayer == null)
            mediaPlayer = MediaPlayer.create(requireContext(), audioUri)?.apply { isLooping = true }

        if(viewModel.isVibrationEnabled().not() || vibrator != null)
            return

        prepareBeatDetectorAsync{ detector ->
            if(detector != null && context != null)
                vibrator = RythmOfSoundVibrator(requireContext(), detector)
        }
    }

    fun deinitAudio() {
        val player = mediaPlayer
        mediaPlayer = null

        player?.release()
        vibrator?.release()

        activity?.volumeControlStream = AudioManager.USE_DEFAULT_STREAM_TYPE
    }

    private fun prepareBeatDetectorAsync(callback: (SoundBeatDetector?)->Unit ) {
        val sessionId = mediaPlayer?.audioSessionId
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
        private const val AUDIO_TIMEOUT = 2000
        private const val PERMISSION_RECORD_AUDIO_CODE = 1000

        private const val ARG_TRANSITION_NAME = "TransitionName"
        private const val ARG_CAT_DATA = "CatData"
        private const val ARG_CAT_ID = "CatId"

        private fun makeFragment(catId: String?, catData: CatData?, transition: String?) =
            PurringFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TRANSITION_NAME, transition)
                    putParcelable(ARG_CAT_DATA, catData)
                    putString(ARG_CAT_ID, catId)
                }
            }

        @JvmStatic
        fun newInstance(catData: CatData, sharedElementTransitionName: String?) =
            makeFragment(null, catData, sharedElementTransitionName)

        @JvmStatic
        fun newInstance(catId: String, sharedElementTransitionName: String?) =
            makeFragment(catId, null, sharedElementTransitionName)
    }
}
