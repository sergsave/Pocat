package com.sergsave.pocat.screens.catcard

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
import android.view.MotionEvent.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.sergsave.pocat.MyApplication
import com.sergsave.pocat.R
import com.sergsave.pocat.helpers.*
import com.sergsave.pocat.models.Card
import com.sergsave.pocat.models.CatData
import com.sergsave.pocat.screens.catcard.PurringViewModel.MenuState
import com.sergsave.pocat.vibration.RingdroidSoundBeatDetector
import com.sergsave.pocat.vibration.RythmOfSoundVibrator
import com.sergsave.pocat.vibration.RythmOfSoundVibrator.OnPrepareFinishedListener
import com.sergsave.pocat.vibration.SoundBeatDetector
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
    private var fadeOutEffect: FadeOutSoundEffect? = null
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
        if(event.action == ACTION_DOWN)
            viewModel.onTouchStarted()

        if(event.action == ACTION_UP)
            viewModel.onTouchFinished()

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
            if(viewModel.sharingLoaderIsVisible.value == true) {
                it.setActionView(R.layout.view_loader)
                it.actionView?.setOnClickListener { viewModel.onSharingLoaderClicked() }
            }
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
        fadeOutEffect = mediaPlayer?.let { FadeOutSoundEffect(it, FADE_DURATION) }

        if(viewModel.isVibrationEnabled.not())
            return

        vibrator = createVibrator(audioUri).apply { prepareAsync() }
    }

    private fun deinitAudio() {
        playerTimeoutHandler?.removeCallbacksAndMessages(null)
        stopAudio(withFade = false)
        vibrator?.release()
        mediaPlayer?.release()
        vibrator = null
        mediaPlayer = null
        activity?.volumeControlStream = AudioManager.USE_DEFAULT_STREAM_TYPE
    }

    private fun createVibrator(audioUri: Uri): RythmOfSoundVibrator {
        val detector = RingdroidSoundBeatDetector(requireContext(), audioUri,
            { mediaPlayer?.currentPosition }
        )
        return RythmOfSoundVibrator(requireContext(), detector)
    }

    private fun playAudio() {
        if(mediaPlayer == null)
            return

        fadeOutEffect?.stop()
        mediaPlayer?.setVolume(1f, 1f)

        if(mediaPlayer?.isPlaying == false)
            mediaPlayer?.start()

        vibrator?.start()

        playerTimeoutHandler?.removeCallbacksAndMessages(null)
        playerTimeoutHandler = Handler(Looper.getMainLooper()).apply {
            postDelayed({ stopAudio(withFade = true) }, AUDIO_TIMEOUT)
        }
    }

    private fun stopAudio(withFade: Boolean){
        val pausePlayer = { if(mediaPlayer?.isPlaying == true) mediaPlayer?.pause() }

        if (withFade)
            fadeOutEffect?.start(pausePlayer)
        else {
            fadeOutEffect?.stop()
            pausePlayer()
        }
        vibrator?.stop()
    }

    companion object {
        private const val AUDIO_TIMEOUT = 1500L
        private const val FADE_DURATION = 1000L

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
