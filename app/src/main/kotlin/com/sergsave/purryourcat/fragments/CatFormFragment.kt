package com.sergsave.purryourcat.fragments

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
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.FileUtils
import com.sergsave.purryourcat.helpers.ImageUtils
import com.sergsave.purryourcat.helpers.PermissionUtils
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.viewmodels.CatDataViewModel
import kotlinx.android.synthetic.main.fragment_cat_form.*
import kotlinx.android.synthetic.main.view_form_fields.view.*

class CatFormFragment : Fragment() {

    interface OnApplyListener {
        fun onApply()
    }

    interface OnDataChangeRequestedListener {
        fun onDataChangeRequested(catData: CatData)
    }

    var onApplyListener: OnApplyListener? = null
    var onDataChangeRequestedListener: OnDataChangeRequestedListener? = null

    private var cameraImageUri: Uri? = null
    private val viewModel: CatDataViewModel by activityViewModels()

    override fun onDestroy() {
        // TODO? Save keyboard visible after orientation change
        hideKeyboard()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState?.let {
            cameraImageUri = it.getParcelable(BUNDLE_KEY_CAMERA_IMAGE_URI)
        }
    }
    // TODO: photo loader
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

        viewModel.data.observe(viewLifecycleOwner, Observer<CatData> { catData ->
            val context = context
            if (context != null) {
                val imageView = photo_image as ImageView
                val photoUri = catData?.photoUri
                if(photoUri?.toString() != imageView.tag) {
                    imageView.tag = photoUri?.toString()
                    ImageUtils.loadInto(context, photoUri, imageView)
                }

                catData?.purrAudioUri?.let {
                    form_layout.sound_edit_text.setText(FileUtils.getContentFileName(context, it))
                }

                catData?.name?.let {
                    form_layout.name_edit_text.apply { if (isFocused.not()) setText(it) }
                }
            }
        })

        fab.setOnClickListener { addPhoto() }
        photo_image.setOnClickListener { addPhoto() }

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
            onApplyListener?.onApply()
        }
    }

    private fun requestChange(catData: CatData) {
        onDataChangeRequestedListener?.onDataChangeRequested(catData)
    }

    private fun currentData() = viewModel.data.value ?: CatData()

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

    private fun checkFileSize(uri: Uri?, maxSize: Long): Boolean {
        if(context == null || uri == null)
            return false

        return FileUtils.getContentFileSize(requireContext(), uri) < maxSize
    }

    private fun showExceededFileSizeSnackbar(maxSize: Long) {
        val formattedSize = Formatter.formatShortFileSize(context, maxSize)
        val message = resources.getString(R.string.file_size_exceeded_message_text, formattedSize)
        Snackbar.make(main_layout, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK)
            return

        val checkFile: (Uri?, String) -> Boolean = { uri, maxSizeArgKey ->
            val maxSize = arguments?.getLong(maxSizeArgKey) ?: 0
            if(checkFileSize(uri, maxSize))
                true
            else {
                showExceededFileSizeSnackbar(maxSize)
                false
            }
        }

        when(requestCode) {
            PICK_IMAGE_CODE -> {
                // Null data - image from camera
                val uri = data?.data ?: cameraImageUri
                (photo_image as ImageView).setImageURI(null)
                if(checkFile(uri, ARG_MAX_IMAGE_SIZE))
                    requestChange(currentData().copy(photoUri = uri))
            }
            PICK_AUDIO_CODE -> {
                val uri = data?.data
                if(checkFile(uri, ARG_MAX_AUDIO_SIZE))
                    requestChange(currentData().copy(purrAudioUri = uri))
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

    companion object {
        private const val BUNDLE_KEY_CAMERA_IMAGE_URI = "BundleCameraImageUri"

        private const val PERMISSIONS_IMAGE_CODE = 1000
        private const val PERMISSIONS_AUDIO_CODE = 1001
        private const val PICK_IMAGE_CODE = 1002
        private const val PICK_AUDIO_CODE = 1003

        private const val ARG_MAX_IMAGE_SIZE = "ArgMaxImageSize"
        private const val ARG_MAX_AUDIO_SIZE = "ArgMaxAudioSize"

        @JvmStatic
        fun newInstance(maxImageFileSize: Long, maxAudioFileSize: Long) =
            CatFormFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_MAX_IMAGE_SIZE, maxImageFileSize)
                    putLong(ARG_MAX_AUDIO_SIZE, maxAudioFileSize)
                }
            }
    }
}
