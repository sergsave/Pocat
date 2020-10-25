package com.sergsave.purryourcat.ui.catcard

import android.Manifest
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.Transition.TransitionListener
import android.transition.Transition
import android.view.*
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.sergsave.purryourcat.MyApplication
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.*
import com.sergsave.purryourcat.models.Card
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.ui.catcard.PurringViewModel.MenuState
import com.sergsave.purryourcat.vibration.AndroidVisualizerBeatDetector
import com.sergsave.purryourcat.vibration.RythmOfSoundVibrator
import com.sergsave.purryourcat.vibration.SoundBeatDetector
import kotlinx.android.synthetic.main.fragment_purring.*

class PurringFragment : Fragment() {

    private val navigation: NavigationViewModel by activityViewModels()
    private val viewModel: PurringViewModel by viewModels {
        val card = arguments?.getParcelable<Card>(ARG_CARD)
        assert(card != null)
        val dummy = Card(null, CatData(), false, false)
        (requireActivity().application as MyApplication).appContainer
            .providePurringViewModelFactory(card ?: dummy)
    }

    private var mediaPlayer: MediaPlayer? = null
    private var playerTimeoutHandler: Handler? = null
    private var vibrator: RythmOfSoundVibrator? = null
    private var transitionListener: TransitionListener? = null

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

    override fun onDetach() {
        // Avoid fragment leakage
        transitionListener?.let {
            activity?.window?.sharedElementEnterTransition?.removeListener(it)
        }
        super.onDetach()
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
        val transitionName = arguments?.getString(ARG_TRANSITION_NAME)
        photo_image.transitionName = transitionName
        // Otherwise show tutorial later, on transition end
        if(transitionName == null)
            showTutorialIfNeeded()

        photo_image.setOnTouchListener { _, event -> onTouchEvent(event) }

        setHasOptionsMenu(true)

        navigation.apply {
            backPressedEvent.observe(viewLifecycleOwner, EventObserver {
                navigation.goToBackScreen()
            })

            tutorialFinishedEvent.observe(viewLifecycleOwner, EventObserver {
                viewModel.isTutorialAchieved = true
            })
        }

        viewModel.apply {
            catData.observe(viewLifecycleOwner, Observer {
                ImageUtils.loadInto(context, it.photoUri, photo_image) {
                    startTransition()
                }
                initAudio(it.purrAudioUri)
                setTitle(it.name)
            })

            editCatEvent.observe(viewLifecycleOwner, EventObserver { id ->
                navigation.editCat(id)
            })

            menuState.observe(viewLifecycleOwner, Observer {
                activity?.invalidateOptionsMenu()
            })

            sharingLoaderIsVisible.observe(viewLifecycleOwner, Observer {
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

    private fun setTitle(title: String?) {
        title?.let { (activity as? AppCompatActivity)?.supportActionBar?.title = it }
    }

    private fun startTransition() {
        activity?.supportStartPostponedEnterTransition()

        transitionListener = object: SupportTransitionListenerAdapter() {
            override fun onTransitionEnd(transition: Transition?) = showTutorialIfNeeded()
        }

        activity?.window?.sharedElementEnterTransition?.addListener(transitionListener)
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(main_layout, message, Snackbar.LENGTH_LONG).show()
    }

    private fun checkVolumeLevel(): Boolean {
        val audioManager = activity?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        return audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) != 0
    }

    private fun onTouchEvent(event: MotionEvent): Boolean {
        if(event.action != ACTION_DOWN && event.action != ACTION_MOVE)
            return false

        if(event.action == ACTION_DOWN && checkVolumeLevel().not()) {
            showSnackbar(getString(R.string.make_louder))
            return true
        }

        playAudio()
        return true
    }

    private fun showTutorialIfNeeded() {
        if(viewModel.isTutorialAchieved.not())
            navigation.showTutorial()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(viewModel.menuId, menu)

        viewModel.menuState.value?.hidedActionIds?.forEach { menu.findItem(it)?.isVisible = false  }
        viewModel.menuState.value?.visibleActionIds?.forEach { menu.findItem(it)?.isVisible = true  }

        menu.findItem(viewModel.shareActionId)?.let {
            if(viewModel.sharingLoaderIsVisible.value == true)
                it.setActionView(R.layout.view_loader)
            else
                it.setActionView(null)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(viewModel.onActionSelected(item.itemId))
            return true
        else
            return super.onOptionsItemSelected(item)
    }

    private fun initAudio(audioUri: Uri?) {
        if(audioUri == null || context == null || mediaPlayer != null)
            return

        activity?.volumeControlStream = AudioManager.STREAM_MUSIC
        mediaPlayer = MediaPlayer.create(requireContext(), audioUri)?.apply { isLooping = true }

        if(viewModel.isVibrationEnabled.not())
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
        private const val ARG_CARD = "CatCard"

        @JvmStatic
        fun newInstance(card: Card, sharedElementTransitionName: String?) =
            PurringFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TRANSITION_NAME, sharedElementTransitionName)
                    putParcelable(ARG_CARD, card)
                }
            }
    }
}
