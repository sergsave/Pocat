package com.sergsave.purryourcat.ui.soundselection

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.media.AudioManager
import android.media.MediaPlayer
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.sergsave.purryourcat.MyApplication
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.setToolbarAsActionBar
import kotlinx.android.synthetic.main.activity_samples_list.*

class SamplesListActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private val adapter = SamplesListAdapter()

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

        setContentView(R.layout.activity_samples_list)
        setToolbarAsActionBar(toolbar, showBackButton = true)

        setupRecyclerView(savedInstanceState)
    }

    private fun setupRecyclerView(savedInstanceState: Bundle?) {
        val layoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)

        val samples = (application as MyApplication)
            .appContainer
            .soundSampleProvider
            .provide()
            .map { Pair(it.second, it.first) }

        adapter.apply {
            this.samples = linkedMapOf(*samples.toTypedArray())

            onSelectedChangedListener = { invalidateOptionsMenu() }
            onPlayingChangedListener = { uri ->
                stopAudio()
                uri?.let { startAudio(it) }
            }

            selected = savedInstanceState?.getParcelable<Uri>(SELECTED_URI_KEY)
            playing = savedInstanceState?.getParcelable<Uri>(PLAYING_URI_KEY)
        }

        recycler_view.apply {
            this.adapter = this@SamplesListActivity.adapter
            this.layoutManager = layoutManager
            addItemDecoration(decoration)
            itemAnimator = null
        }
    }

    private fun stopAudio() {
        if(mediaPlayer?.isPlaying == true)
            mediaPlayer?.pause()

        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun startAudio(uri: Uri) {
        mediaPlayer = MediaPlayer.create(this, uri)?.apply {
            isLooping = true
            start()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_sound_samples_list, menu)
        menu?.findItem(R.id.action_ok)?.isVisible = adapter.selected != null
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId != R.id.action_ok)
            return super.onOptionsItemSelected(item)

        setResult(Activity.RESULT_OK, Intent().apply { data = adapter.selected })
        finish()
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(SELECTED_URI_KEY, adapter.selected)
        outState.putParcelable(PLAYING_URI_KEY, adapter.playing)
    }

    companion object {
        private const val SELECTED_URI_KEY = "Selected"
        private const val PLAYING_URI_KEY = "Playing"
    }
}