package com.sergsave.pocat.screens.catcard

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.sergsave.pocat.BuildConfig
import com.sergsave.pocat.MyApplication
import com.sergsave.pocat.R
import com.sergsave.pocat.dialogs.PermissionPermanentlyDeniedDialog
import com.sergsave.pocat.helpers.*
import com.sergsave.pocat.helpers.PermissionDenyTypeQualifier.Type.DENIED_PERMANENTLY
import com.sergsave.pocat.models.Card
import com.sergsave.pocat.screens.catcard.FormViewModel.SoundButtonType
import com.sergsave.pocat.screens.soundselection.SoundSelectionActivity
import kotlinx.android.synthetic.main.fragment_cat_form.*
import kotlinx.android.synthetic.main.view_form_fields.view.*

class FormFragment : Fragment() {

    private var cameraImageUri: Uri? = null
    private val navigation: NavigationViewModel by activityViewModels()
    private val viewModel: FormViewModel by viewModels {
        (requireActivity().application as MyApplication).appContainer
            .provideFormViewModelFactory(arguments?.getParcelable<Card>(ARG_CARD))
    }
    private lateinit var permissionDenyQualifier: PermissionDenyTypeQualifier

    override fun onDestroy() {
        // TODO? Save keyboard visible after orientation change
        hideKeyboard()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionDenyQualifier = PermissionDenyTypeQualifier(
            requireActivity() as AppCompatActivity, "PermissionQualifier")

        savedInstanceState?.let {
            cameraImageUri = it.getParcelable(BUNDLE_KEY_CAMERA_IMAGE_URI)
            restoreDialogState()
            permissionDenyQualifier.onRestoreInstanceState(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cat_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fab.setOnClickListener { addPhoto() }
        photo_image.setOnClickListener { addPhoto() }

        navigation.backPressedEvent.observe(viewLifecycleOwner, EventObserver {
            if(viewModel.handleBackPressed())
                navigation.goToBackScreen()
        })

        viewModel.apply {
            name.observe(viewLifecycleOwner, Observer {
                form_layout.name_edit_text.apply { if (!isFocused) setText(it) }
            })

            photoUri.observe(viewLifecycleOwner, Observer {
                ImageUtils.loadInto(requireContext(), it, photo_image)
            })

            soundButtonType.observe(viewLifecycleOwner, Observer {
                form_layout.add_sound_button.visibility = View.GONE
                form_layout.sound_is_added_button.visibility = View.GONE

                val currentButton: View? = when(it) {
                    SoundButtonType.ADD_SOUND -> form_layout.add_sound_button
                    SoundButtonType.SOUND_IS_ADDED -> form_layout.sound_is_added_button
                    else -> null
                }
                currentButton?.visibility = View.VISIBLE
            })

            unsavedChangesMessageEvent.observe(viewLifecycleOwner, EventObserver {
                UnsavedChangesDialog().also {
                    initUnsavedChangesDialog(it)
                    it.show(childFragmentManager, UNSAVED_DIALOG_TAG)
                }
            })

            snackbarMessageEvent.observe(viewLifecycleOwner, EventObserver {
                Snackbar.make(main_layout, getString(it), Snackbar.LENGTH_LONG).show()
            })

            openCardEvent.observe(viewLifecycleOwner, EventObserver {
                navigation.openCat(it)
            })
        }
        (activity as? AppCompatActivity)?.supportActionBar?.title =
            resources.getString(viewModel.toolbarTitleStringId)

        setupFormLayout()
    }

    private fun initUnsavedChangesDialog(dialog: UnsavedChangesDialog) {
        dialog.onDiscardChangesListener = {
            viewModel.onDiscardChanges()
            navigation.goToBackScreen()
        }
    }

    private fun setupFormLayout() {
        form_layout.name_edit_text.imeOptions = EditorInfo.IME_ACTION_DONE
        form_layout.name_edit_text.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                v.clearFocus()
                hideKeyboard()
            }
            false
        }

        form_layout.name_edit_text.addTextChangedListener(object : TextWatcher {
            private var prevText: String? = null

            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                prevText = s.toString()
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s.toString()
                if(text == prevText)
                    return

                viewModel.changeName(text)
            }
        })

        // Hack. 2 buttons because it's impossible change app:iconGravity from code
        form_layout.add_sound_button.setOnClickListener { addAudio() }
        form_layout.sound_is_added_button.setOnClickListener { addAudio() }

        form_layout.apply_button.setOnClickListener {
            viewModel.onApplyPressed()
        }
    }

    private fun addPhoto() {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE

        if (isPermissionGranted(permission))
            sendPhotoIntent()
        else {
            requestPermissions(arrayOf(permission), PERMISSIONS_IMAGE_CODE)
            permissionDenyQualifier.onRequestPermission(permission)
        }

    }

    private fun addAudio() {
        val intent = Intent(requireContext(), SoundSelectionActivity::class.java)
        startActivityForResult(intent, PICK_AUDIO_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(checkPermissionRequestResult(grantResults)) {
            when(requestCode) {
                PERMISSIONS_IMAGE_CODE -> sendPhotoIntent()
            }
            return
        }

        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (permissionDenyQualifier.handleRequestPermissionResult(permission) == DENIED_PERMANENTLY) {
            PermissionPermanentlyDeniedDialog.newInstance(R.string.storage_permission)
                .show(childFragmentManager, null)
        }
    }

    private fun createPickIntents(): List<Intent> {
        val type = "image/*"
        return listOf(
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also {
                it.type = type
            },
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = type
            }
        )
    }

    private fun createCameraIntent(imageUri: Uri?): Intent? {
        return imageUri?.let { uri ->
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply { putExtra(MediaStore.EXTRA_OUTPUT, uri) }
        }
    }

    @SuppressLint("MissingPermission")
    private fun resolveCameraImageUri(): Uri? {
        val picturesDir = Environment.DIRECTORY_PICTURES
        val subDir = getString(R.string.app_name)
        val providerAuth = "${BuildConfig.APPLICATION_ID}.fileprovider"
        return FileUtils.provideContentUriInPublicStorage(requireContext(),
            picturesDir, subDir, providerAuth)
    }

    private fun sendPhotoIntent() {
        cameraImageUri = resolveCameraImageUri()
        val cameraIntent = createCameraIntent(cameraImageUri)

        val intents = (createPickIntents() + listOf(cameraIntent)).filterNotNull().filter {
            it.resolveActivity(requireContext().packageManager) != null
        }

        val title = resources.getString(R.string.add_photo_with)
        createIntentChooser(intents, title)?.let { startActivityForResult(it, PICK_IMAGE_CODE) }
    }

    @SuppressLint("MissingPermission")
    private fun releaseContent(uri: Uri?) {
        if (uri != null && isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            FileUtils.releaseContentUri(requireContext(), uri)
    }

    private fun handlePickImageResult(resultCode: Int, data: Intent?) {
        val dataUri = data?.data
        val pickedFromCamera = resultCode == Activity.RESULT_OK && dataUri == null

        if (resultCode == Activity.RESULT_OK)
            viewModel.changePhoto(if (pickedFromCamera) cameraImageUri else dataUri)

        if (!pickedFromCamera) {
            releaseContent(cameraImageUri)
            cameraImageUri = null
        }
    }

    private fun handlePickAudioResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK)
            viewModel.changeAudio(data?.data)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            PICK_IMAGE_CODE -> handlePickImageResult(resultCode, data)
            PICK_AUDIO_CODE -> handlePickAudioResult(resultCode, data)
        }
    }

    private fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(BUNDLE_KEY_CAMERA_IMAGE_URI, cameraImageUri)
        permissionDenyQualifier.onSaveInstanceState(outState)
    }

    private fun restoreDialogState() {
        val dialog = childFragmentManager.findFragmentByTag(UNSAVED_DIALOG_TAG)
                as? UnsavedChangesDialog
        dialog?.let{ initUnsavedChangesDialog(it) }
    }

    companion object {
        private const val BUNDLE_KEY_CAMERA_IMAGE_URI = "BundleCameraImageUri"

        private const val PERMISSIONS_IMAGE_CODE = 1000
        private const val PICK_IMAGE_CODE = 1001
        private const val PICK_AUDIO_CODE = 1002

        private const val UNSAVED_DIALOG_TAG = "UnsavedDialog"
        private const val ARG_CARD = "ArgCard"

        // Cat from card should be saved in persistent repo
        @JvmStatic
        fun newInstance(card: Card?) =
            FormFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_CARD, card)
                }
            }
    }
}