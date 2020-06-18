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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.MutableLiveData
import com.google.android.material.transition.MaterialFadeThrough
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.*
import com.sergsave.purryourcat.models.CatData
import kotlinx.android.synthetic.main.fragment_cat_form.*
import kotlinx.android.synthetic.main.view_form_fields.view.*
import java.io.File

class CatFormFragment : Fragment() {

    interface OnApplyListener {
        fun onApply()
    }

    enum class Mode {
        CREATE, EDIT
    }

    class CatDataChange(val from: CatData?, val to: CatData?)
    val catDataChange : CatDataChange
        get() = CatDataChange(originalCatData, catLiveData.value)

    private var cameraImageUri: Uri? = null
    private var onApplyListener: OnApplyListener? = null
    private var mode = Mode.CREATE
    private var originalCatData: CatData? = null
    private val catLiveData = MutableLiveData<CatData>()

    override fun onDestroy() {
        // TODO? Save keyboard visible after orientation change
        hideKeyboard()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(context != null)
            enterTransition = MaterialFadeThrough.create(requireContext())

        savedInstanceState?.let {
            cameraImageUri = it.getParcelable(BUNDLE_KEY_CAMERA_IMAGE_URI)
            catLiveData.value = it.getParcelable(BUNDLE_KEY_CAT_DATA)
        }

        arguments?.let {
            val ordinal = it.getInt(ARG_MODE)
            mode = Mode.values().get(ordinal)

            val catData = it.getParcelable<CatData>(ARG_CAT_DATA)
            originalCatData = catData

            if(savedInstanceState == null)
                catLiveData.value = catData
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

        // TODO. TOP 5 errors with model view !!
        catLiveData.observe(this, Observer<CatData> { cat ->
            val context = context
            if (context != null) {
                ImageUtils.loadInto(context, cat?.photoUri, photo_image as ImageView)

                cat?.purrAudioUri?.let {
                    form_layout.sound_edit_text.setText(FileUtils.getContentFileName(context, it))
                }
                cat?.name?.let {
                    form_layout.name_edit_text.apply { if (isFocused.not()) setText(it) }
                }
            }
        })

        setupToolbar(mode)

        setHasOptionsMenu(true)

        fab.setOnClickListener { addPhoto() }
        photo_image.setOnClickListener { addPhoto() }

        form_layout.name_edit_text.setImeOptions(EditorInfo.IME_ACTION_DONE)
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
                catLiveData.value = catLiveData.value?.copy(name = name)
            }
        })

        form_layout.sound_edit_text.setOnClickListener { addAudio() }
        form_layout.apply_button.setOnClickListener {
            onApplyListener?.onApply()
        }
    }

    fun setOnApplyListener(listener: OnApplyListener) {
        onApplyListener = listener
    }

    private fun setupToolbar(mode: Mode) {
        val title = when (mode) {
            Mode.CREATE -> resources.getString(R.string.add_new_cat)
            Mode.EDIT -> resources.getString(R.string.edit_cat)
        }

        val activity = getActivity() as AppCompatActivity?
        activity?.getSupportActionBar()?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setTitle(title)
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

        var catDataCopy = when(requestCode) {
            PICK_IMAGE_CODE -> {
                // Null data - image from camera
                val uri = data?.data ?: cameraImageUri
                catLiveData.value?.copy(photoUri = saveFileOnInternal(context, uri))
            }
            PICK_AUDIO_CODE -> {
                val uri = data?.data
                catLiveData.value?.copy(purrAudioUri = saveFileOnInternal(context, uri))
            }
            else -> catLiveData.value
        }

        catLiveData.value = catDataCopy
    }

    private fun saveFileOnInternal(context: Context?, uri: Uri?) : Uri? {
        if(uri == null || context == null)
            return null

        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = FileUtils.getContentFileName(context, uri)

        if(inputStream == null || fileName == null)
            return null

        val file = File(context.filesDir, fileName)

        FileUtils.copyStreamToFile(inputStream, file)
        return Uri.fromFile(file)
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(view?.getWindowToken(), 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(BUNDLE_KEY_CAMERA_IMAGE_URI, cameraImageUri)
        outState.putParcelable(BUNDLE_KEY_CAT_DATA, catLiveData.value)
    }

    companion object {
        private val BUNDLE_KEY_CAMERA_IMAGE_URI = "BundleCameraImageUri"
        private val BUNDLE_KEY_CAT_DATA = "BundleCatData"

        private val PERMISSIONS_IMAGE_CODE = 1000
        private val PERMISSIONS_AUDIO_CODE = 1001
        private val PICK_IMAGE_CODE = 1002
        private val PICK_AUDIO_CODE = 1003

        private val ARG_MODE = "ArgMode"
        private val ARG_CAT_DATA = "ArgCatData"

        @JvmStatic
        fun newInstance(mode: Mode, catData: CatData) =
            CatFormFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_MODE, mode.ordinal)
                    putParcelable(ARG_CAT_DATA, catData)
                }
            }
    }
}
