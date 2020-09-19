package com.sergsave.purryourcat.ui.catcard

import android.Manifest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.MotionEvent.ACTION_MOVE
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.sergsave.purryourcat.MyApplication
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.EventObserver
import com.sergsave.purryourcat.helpers.ImageUtils
import com.sergsave.purryourcat.helpers.PermissionUtils
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.ui.catcard.PurringViewModel.MenuState
import com.sergsave.purryourcat.vibration.AndroidVisualizerBeatDetector
import com.sergsave.purryourcat.vibration.RythmOfSoundVibrator
import com.sergsave.purryourcat.vibration.SoundBeatDetector
import kotlinx.android.synthetic.main.fragment_purring.*

class PurringFragment : Fragment() {

    private val navigation: NavigationViewModel by activityViewModels()
    private val viewModel: PurringViewModel by viewModels {
        val cat = arguments?.getString(ARG_CAT_ID)?.let {
            PurringViewModel.Cat.Saved(it)
        } ?: run {
            val data = arguments?.getParcelable(ARG_CAT_DATA) ?: CatData()
            PurringViewModel.Cat.Unsaved(data)
        }
        (requireActivity().application as MyApplication).appContainer
            .providePurringViewModelFactory(cat)
    }

    private var mediaPlayer: MediaPlayer? = null
    private var playerTimeoutHandler: Handler? = null
    private var vibrator: RythmOfSoundVibrator? = null

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStop() {
        // Release resources for background mode
        deinitAudio()
        super.onStop()
    }

    override fun onStart() {
        // In case of restart
        super.onStart()
        initAudio(viewModel.catData.value?.purrAudioUri)
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

        val showSnackbar = { message: String ->
            Snackbar.make(main_layout, message, Snackbar.LENGTH_LONG).show()
        }

        viewModel.apply {
            catData.observe(viewLifecycleOwner, Observer {
                ImageUtils.loadInto(context, it.photoUri, photo_image) {
                    activity?.supportStartPostponedEnterTransition()
                }

                initAudio(it.purrAudioUri)
            })

            editCatEvent.observe(viewLifecycleOwner, EventObserver { id ->
                navigation.editCat(id)
            })

            menuState.observe(viewLifecycleOwner, Observer {
                activity?.invalidateOptionsMenu()
            })

            sharingSuccessEvent.observe(viewLifecycleOwner, EventObserver {
                startActivity(it)
            })

            sharingFailedStringIdEvent.observe(viewLifecycleOwner, EventObserver {
                showSnackbar(resources.getString(it))
            })

            dataSavedEvent.observe(viewLifecycleOwner, EventObserver {
                val message = resources.getString(R.string.save_snackbar_message_text)
                showSnackbar(message)
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        val menuId = when(viewModel.menuState.value) {
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
        when (item.itemId) {
            R.id.action_edit -> viewModel.onEditPressed()
            R.id.action_share -> viewModel.onSharePressed()
            R.id.action_save -> viewModel.onSavePressed()
            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }

    private fun initAudio(audioUri: Uri?) {
        if(audioUri == null || context == null || mediaPlayer != null)
            return

        activity?.volumeControlStream = AudioManager.STREAM_MUSIC
        mediaPlayer = MediaPlayer.create(requireContext(), audioUri)?.apply { isLooping = true }

        if(viewModel.isVibrationEnabled().not())
            return

        prepareBeatDetectorAsync{ detector ->
            if(detector != null && context != null)
                vibrator = RythmOfSoundVibrator(requireContext(), detector)
        }
    }

    private fun deinitAudio() {
        playerTimeoutHandler?.removeCallbacksAndMessages(null)
        stopAudio()
        vibrator?.release()
        mediaPlayer?.release()
        vibrator = null
        mediaPlayer = null
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

        if(mediaPlayer?.isPlaying == false)
            mediaPlayer?.start()

        vibrator?.start()

        playerTimeoutHandler?.removeCallbacksAndMessages(null)
        playerTimeoutHandler = Handler(Looper.getMainLooper()).apply {
            postDelayed({ stopAudio() }, AUDIO_TIMEOUT)
        }
    }

    private fun stopAudio(){
        if(mediaPlayer?.isPlaying == true)
            mediaPlayer?.pause()
        vibrator?.stop()
    }

    companion object {
        private const val AUDIO_TIMEOUT = 2000L
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
