package com.sergsave.purryourcat.ui.soundselection

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.text.format.Formatter
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sergsave.purryourcat.Constants
import com.sergsave.purryourcat.MyApplication
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.*
import kotlinx.android.synthetic.main.activity_sound_selection.*

class SoundSelectionActivity : AppCompatActivity() {
    private val viewModel: SoundSelectionViewModel by viewModels {
        val uri = intent.getParcelableExtra<Uri>(Constants.AUDIO_URI_INTENT_KEY)
        (application as MyApplication).appContainer
            .provideSoundSelectionViewModelFactory(uri)
    }

    private var mediaPlayer: MediaPlayer? = null
    private val adapter = SoundSelectionAdapter()

    override fun onStart() {
        super.onStart()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStop() {
        stopAudio()
        volumeControlStream = AudioManager.USE_DEFAULT_STREAM_TYPE
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sound_selection)

        setToolbarAsActionBar(toolbar, showBackButton = true)

        from_device_button.setOnClickListener {
            viewModel.onUserButtonClicked()
            addUserAudio()
        }

        setupRecyclerView()
        setupViewModel()
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)

        recycler_view.apply {
            this.adapter = this@SoundSelectionActivity.adapter
            this.layoutManager = layoutManager
            addItemDecoration(decoration)
            itemAnimator = null
            isNestedScrollingEnabled = false
        }

        adapter.onSelectedChangedListener = object: SoundSelectionAdapter.OnCurrentUriChanged {
            override fun onCurrentUriChanged(from: Uri?, to: Uri?) = viewModel.onAudioSelected(to)
        }

        adapter.onPlayingChangedListener = object: SoundSelectionAdapter.OnCurrentUriChanged {
            override fun onCurrentUriChanged(from: Uri?, to: Uri?) = viewModel.onAudioPlayStarted(to)
        }
    }

    private fun setupViewModel() {
        viewModel.apply {
            val lifecycleOwner = this@SoundSelectionActivity

            userAudio.observe(lifecycleOwner, Observer {
                val sections = mutableListOf<SoundSelectionAdapter.Section>()

                it?.let {
                    val header = getString(R.string.user_file_title)
                    sections += SoundSelectionAdapter.Section(header, setOf(it))
                }

                val header = getString(R.string.samples_title)
                sections += SoundSelectionAdapter.Section(header, viewModel.audioSamples)

                assert(adapter.updateSections(sections)) { "samples must be unique" }
            })

            selectedAudio.observe(lifecycleOwner, Observer {
                adapter.selected = it
                val intent = Intent().apply { data = it }
                setResult(Activity.RESULT_OK, intent)
            })

            playingAudio.observe(lifecycleOwner, Observer {
                adapter.playing = it
                stopAudio()
                it?.let { startAudio(it) }
            })

            fileSizeExceededMessageEvent.observe(lifecycleOwner, EventObserver {
                val formattedSize = Formatter.formatShortFileSize(this@SoundSelectionActivity, it)
                val message = getString(
                    R.string.file_size_exceeded_message_text,
                    formattedSize
                )

                Snackbar.make(main_layout, message, Snackbar.LENGTH_LONG).show()
            })
        }
    }

    private fun stopAudio() {
        if(mediaPlayer?.isPlaying == true)
            mediaPlayer?.pause()

        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun startAudio(uri: Uri) {
        mediaPlayer = MediaPlayer.create(this, uri)?.apply { isLooping = true }
        mediaPlayer?.start()
    }

    private fun addUserAudio() {
        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO)

        if(permissions.any { !PermissionUtils.checkPermission(this, it) })
            PermissionUtils.requestPermissions(this, permissions, PERMISSIONS_AUDIO_CODE)
        else
            sendAudioIntent()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(!PermissionUtils.checkRequestResult(grantResults))
            return

        if(requestCode == PERMISSIONS_AUDIO_CODE)
            sendAudioIntent()
    }

    private fun sendAudioIntent() {
        val pickIntent = Intent(Intent.ACTION_PICK)
        pickIntent.type = "audio/*"

        val recorderIntent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)

        val title = resources.getString(R.string.add_audio_with)
        val chooser = Intent.createChooser(pickIntent, title)
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(recorderIntent))
        startActivityForResult(chooser, PICK_AUDIO_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || requestCode != PICK_AUDIO_CODE)
            return

        data?.data?.let{ viewModel.onUserAudioAdded(it) }
    }

    companion object {
        private const val PERMISSIONS_AUDIO_CODE = 1000
        private const val PICK_AUDIO_CODE = 1001
    }
}