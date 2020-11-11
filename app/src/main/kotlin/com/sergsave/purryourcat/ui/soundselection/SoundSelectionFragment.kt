package com.sergsave.purryourcat.ui.soundselection

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import com.google.android.material.snackbar.Snackbar
import com.sergsave.purryourcat.ui.soundselection.SoundSelectionViewModel.Message
import com.sergsave.purryourcat.MyApplication
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.*
import com.sergsave.purryourcat.helpers.PermissionDenyTypeQualifier.Type.DENIED_PERMANENTLY
import com.sergsave.purryourcat.ui.dialogs.StoragePermissionPermanentlyDeniedDialog

class SoundSelectionFragment: PreferenceFragmentCompat() {
    private val viewModel: SoundSelectionViewModel by viewModels {
        (requireActivity().application as MyApplication).appContainer
            .provideSoundSelectionViewModelFactory()
    }
    private lateinit var permissionDenyQualifier: PermissionDenyTypeQualifier

    // Use preferences for beautiful UI purpose only
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.sound_selection_preferences, rootKey)

        val registerPreference = { keyId: Int, summary: Message, onClickListener: () -> Unit ->
            findPreference<Preference>(getString(keyId))?.apply {
                this.summary = getString(summary.stringId, *summary.stringArgs)
                onPreferenceClickListener = object: Preference.OnPreferenceClickListener {
                    override fun onPreferenceClick(preference: Preference?): Boolean {
                        onClickListener()
                        return true
                    }
                }
            }
        }

        registerPreference(R.string.samples_preference_key, viewModel.samplesSummary,
            { addAudioFromSamples() })
        registerPreference(R.string.recorder_preference_key, viewModel.recorderSummary,
            { addAudioFromRecorder() })
        registerPreference(R.string.pick_audio_preference_key, viewModel.pickAudioSummary,
            { addAudioFromDevice() })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionDenyQualifier = PermissionDenyTypeQualifier(
            requireActivity() as AppCompatActivity, "PermissionQualifier")

        savedInstanceState?.let { permissionDenyQualifier.onRestoreInstanceState(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
    }

    private fun setupViewModel() {
        viewModel.apply {
            validationSuccessEvent.observe(viewLifecycleOwner, EventObserver {
                activity?.setResult(Activity.RESULT_OK, Intent().apply {
                    data = it
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                })
                activity?.finish()
            })

            validationFailedEvent.observe(viewLifecycleOwner, EventObserver {
                val message = getString(it.stringId, *it.stringArgs)
                view?.let { Snackbar.make(it, message, Snackbar.LENGTH_LONG).show() }
            })
        }
    }

    private fun addAudioFromSamples() {
        sendSamplesIntent()
    }

    private fun requestReadStoragePermission(requestCode: Int) {
        val permission = READ_EXTERNAL_STORAGE
        requestPermissions(arrayOf(permission), requestCode)
        permissionDenyQualifier.onRequestPermission(permission)
    }

    private fun addAudioFromRecorder() {
        // Maybe some kind of recorder return uri with "file" scheme...
        if (isPermissionGranted(READ_EXTERNAL_STORAGE))
            sendRecorderIntent()
        else
            requestReadStoragePermission(PERMISSIONS_RECORDER_CODE)
    }

    private fun addAudioFromDevice() {
        if (isPermissionGranted(READ_EXTERNAL_STORAGE))
            sendPickAudioIntent()
        else
            requestReadStoragePermission(PICK_AUDIO_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (checkPermissionRequestResult(grantResults)) {
            when (requestCode) {
                PERMISSIONS_RECORDER_CODE -> sendRecorderIntent()
                PERMISSIONS_PICK_CODE -> sendPickAudioIntent()
            }
            return
        }

        val permission = READ_EXTERNAL_STORAGE
        if (permissionDenyQualifier.handleRequestPermissionResult(permission) == DENIED_PERMANENTLY)
            StoragePermissionPermanentlyDeniedDialog().show(childFragmentManager, null)
    }

    private fun sendSamplesIntent() {
        val intent = Intent(requireContext(), SamplesListActivity::class.java)
        startActivityForResult(intent, SAMPLES_CODE)
    }

    private fun sendRecorderIntent() {
        val intent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
        if (intent.resolveActivity(requireActivity().packageManager) != null)
            startActivityForResult(intent, RECORDER_CODE)
    }

    private fun sendPickAudioIntent() {
        val type = "audio/*"
        val intents = listOf(
            Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI).also {
                it.type = type
            },
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = type
            }
        ).filter { it.resolveActivity(requireActivity().packageManager) != null }

        val title = getString(R.string.add_audio_with)
        createIntentChooser(intents, title)?.let {
            startActivityForResult(it, PICK_AUDIO_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK)
            return

        when(requestCode) {
            SAMPLES_CODE, RECORDER_CODE, PICK_AUDIO_CODE -> data?.data?.let {
                viewModel.validateResult(it)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        permissionDenyQualifier.onSaveInstanceState(outState)
    }

    companion object {
        private const val PERMISSIONS_RECORDER_CODE = 1000
        private const val PERMISSIONS_PICK_CODE = 1001

        private const val SAMPLES_CODE = 1002
        private const val RECORDER_CODE = 1003
        private const val PICK_AUDIO_CODE = 1004
    }
}