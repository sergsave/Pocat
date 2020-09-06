package com.sergsave.purryourcat.ui.catcard

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.ImageUtils
import com.sergsave.purryourcat.helpers.PermissionUtils
import kotlinx.android.synthetic.main.fragment_cat_form.*
import kotlinx.android.synthetic.main.view_form_fields.view.*

class FormFragment : Fragment() {

    private var cameraImageUri: Uri? = null
    private val navigation: NavigationViewModel by activityViewModels()
    private val viewModel: FormViewModel

    override fun onDestroy() {
        // TODO? Save keyboard visible after orientation change
        hideKeyboard()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViewModel()
        setupNavigation()

        savedInstanceState?.let {
            cameraImageUri = it.getParcelable(BUNDLE_KEY_CAMERA_IMAGE_URI)
            restoreAlertDialogsState()
        }
    }

    private fun initViewModel() {
        val factory = (application as MyApplication).appContainer
            .provideFormViewModelFactory(catRepoId)
        viewModel = ViewModelProvider(this, factory).get(FormViewModel::class.java)
    }

    private fun setupNavigation() {
        navigation.onBackButtonPressed().observe {
            if(viewModel.handleBackPressed())
                navigation.goToBackScreen()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cat_form, container, false)
    }

    // TODO? or in onViewCreate
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fab.setOnClickListener { addPhoto() }
        photo_image.setOnClickListener { addPhoto() }

        viewModel.name.observe(viewLifecycleOwner, Observer<String> {
            form_layout.name_edit_text.apply { if (isFocused.not()) setText(it) }
        })

        viewModel.photoUri.observe(viewLifecycleOwner, Observer<Uri> {
            ImageUtils.loadInto(context, it, photo_image as ImageView)
        })

        viewModel.audioName.observe(viewLifecycleOwner, Observer<Uri> {
            form_layout.sound_edit_text.setText(it)
        })

        viewModel.unsavedChangesMessage.observe(viewLifecycleOwner, Observer {
            UnsavedChangesDialog().apply {
                initUnsavedChangesDialog(this)
                show(childFragmentManager, UNSAVED_DIALOG_TAG)
            }
        })

        viewModel.notValidDataMessage.observe(viewLifecycleOwner, Observer {
            NotValidDataDialog().show(childFragmentManager)
        })

        viewModel.snackbarMessage.observe(viewLifecycleOwner, Observer {
            Snackbar.make(container, it, Snackbar.LENGTH_LONG).show()
        })

        (activity as? AppCompatActivity).supportActionBar?.title = viewModel.getToolbarTitle()

        setupFormLayout()
    }

    private fun initUnsavedChangesDialog(dialog: UnsavedChangesDialog) {
        dialog.onDiscardChangesListener = {
            viewModel.onDiscardChanges()
            navigation.goToBackScreen()
        }
    }

    private fun setupFormLayout {
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

                val name: String? = if(text.isNotEmpty()) text else null
                requestChange(currentData().copy(name = name))
            }
        })

        form_layout.sound_edit_text.setOnClickListener { addAudio() }
        form_layout.apply_button.setOnClickListener {
            if(viewModel.handleApplyPressed())
                navigation.openCat()
        }
    }

    private fun addPhoto() {
        if(context == null)
            return

        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA)

        if(permissions.any { !PermissionUtils.checkPermission(requireContext(), it) })
            PermissionUtils.requestPermissions(this, permissions, PERMISSIONS_IMAGE_CODE)
        else
            sendPhotoIntent()
    }

    private fun addAudio() {
        if(context == null)
            return

        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO)

        if(permissions.any { !PermissionUtils.checkPermission(requireContext(), it) })
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

        when(requestCode){
            PERMISSIONS_IMAGE_CODE -> sendPhotoIntent()
            PERMISSIONS_AUDIO_CODE -> sendAudioIntent()
        }
    }

    private fun sendPhotoIntent()
    {
        val pickIntent = Intent(Intent.ACTION_PICK)
        pickIntent.type = "image/*"

        // TODO: Rename file? For accessibility in gallery
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")

        cameraImageUri = activity?.contentResolver?.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)

        val title = resources.getString(R.string.add_photo_with)
        val chooser = Intent.createChooser(pickIntent, title)
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
        startActivityForResult(chooser, PICK_IMAGE_CODE)
    }

    private fun sendAudioIntent() {
        val pickIntent = Intent(Intent.ACTION_PICK)
        pickIntent.type = "audio/*"

        // TODO? Rename file
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Audio")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Recorder")

        val recorderAudioUri = activity?.contentResolver?.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val recorderIntent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
        recorderIntent.putExtra(MediaStore.EXTRA_OUTPUT, recorderAudioUri)

        val title = resources.getString(R.string.add_audio_with)
        val chooser = Intent.createChooser(pickIntent, title)
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(recorderIntent))
        startActivityForResult(chooser, PICK_AUDIO_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK)
            return

        when(requestCode) {
            PICK_IMAGE_CODE -> {
                // Null data - image from camera
                val uri = data?.data ?: cameraImageUri
                viewModel.chagePhoto(uri)
            }
            PICK_AUDIO_CODE -> {
                viewModel.chageAudio(data?.data)
            }
        }
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(BUNDLE_KEY_CAMERA_IMAGE_URI, cameraImageUri)
    }

    private fun restoreDialogsState() {
        val backDialog = supportFragmentManager.findFragmentByTag(UNSAVED_DIALOG_TAG)
                as? UnsavedChangesDialog
        backDialog?.let{ initBackDialog(it) }
    }

    companion object {
        private const val BUNDLE_KEY_CAMERA_IMAGE_URI = "BundleCameraImageUri"

        private const val PERMISSIONS_IMAGE_CODE = 1000
        private const val PERMISSIONS_AUDIO_CODE = 1001
        private const val PICK_IMAGE_CODE = 1002
        private const val PICK_AUDIO_CODE = 1003

        private const val UNSAVED_DIALOG_TAG = "UnsavedDialog"
        private const val ARG_CAT_ID = "ArgCatId"

        @JvmStatic
        fun newInstance(catId: String?) =
            CatFormFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_CAT_ID, catId)
                }
            }
    }
}