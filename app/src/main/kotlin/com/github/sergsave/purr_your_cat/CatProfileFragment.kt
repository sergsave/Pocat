package com.github.sergsave.purr_your_cat

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.android.synthetic.main.fragment_cat_profile.*
import kotlinx.android.synthetic.main.form_profile.view.*

class CatProfileFragment : Fragment() {

    interface OnApplyListener {
        fun onApply()
    }

    private var catData: CatData? = null
    private var cameraImageUri: Uri? = null
    private var onApplyListener: OnApplyListener? = null

    override fun onDestroy() {
        // TODO? Save keyboard visible after orientation change
        hideKeyboard()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(context != null)
            enterTransition = MaterialFadeThrough.create(requireContext())

        arguments?.let {
            catData = it.getParcelable(ARG_CAT_DATA) as CatData?
        }

        cameraImageUri = savedInstanceState?.getParcelable(CAMERA_IMAGE_URI_BUNDLE_KEY)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cat_profile, container, false)
    }

    // TODO? or in onViewCreate
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mode = if(catData == null) Mode.CREATE else Mode.EDIT
        setupToolbar(mode)

        setHasOptionsMenu(true)

        fab.setOnClickListener{ addPhoto() }
        photo_image.setOnClickListener{ addPhoto() }

        // Restore photo
        photo_image.setOnSizeReadyListener{ width, height ->
            setPhotoImage(catData?.photoUri, width, height)
        }

        form_layout.name_edit_text.setImeOptions(EditorInfo.IME_ACTION_DONE)
        form_layout.name_edit_text.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                v.clearFocus()
                hideKeyboard()
            }
            false
        }

        form_layout.sound_edit_text.setOnClickListener { addAudio() }
        form_layout.apply_button.setOnClickListener { onApplyListener?.onApply() }
    }

    fun setOnApplyListener(listener: OnApplyListener) {
        onApplyListener = listener
    }

    private enum class Mode {
        CREATE, EDIT
    }

    private fun setupToolbar(mode: Mode) {
        val title = when (mode) {
            Mode.CREATE -> getResources().getString(R.string.add_new_cat)
            Mode.EDIT -> getResources().getString(R.string.edit_cat)
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
            PermissionUtils.requestPermissions(this, permissions, IMAGE_PERMISSIONS_CODE)
        else
            sendPhotoIntent()
    }

    private fun addAudio() {
        if(context == null)
            return

        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO)

        if(permissions.any { !PermissionUtils.checkPermission(requireContext(), it) })
            PermissionUtils.requestPermissions(this, permissions, AUDIO_PERMISSIONS_CODE)
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
            IMAGE_PERMISSIONS_CODE -> sendPhotoIntent()
            AUDIO_PERMISSIONS_CODE -> sendAudioIntent()
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

        val title = getResources().getString(R.string.add_photo_with)
        val chooser = Intent.createChooser(pickIntent, title)
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
        startActivityForResult(chooser, IMAGE_PICK_CODE)
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

        val title = getResources().getString(R.string.add_audio_with)
        val chooser = Intent.createChooser(pickIntent, title)
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(recorderIntent))
        startActivityForResult(chooser, AUDIO_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK)
            return

        when(requestCode) {
            IMAGE_PICK_CODE -> {
                // Null data - image from camera
                val uri = data?.data ?: cameraImageUri
                setPhotoImage(uri, photo_image.width, photo_image.height)
                catData?.photoUri = uri
            }
            AUDIO_PICK_CODE -> {
                val uri = data?.data
                form_layout.sound_edit_text.setText(uri?.getLastPathSegment())
                catData?.purrAudioUri = uri
            }
        }
    }

    private fun setPhotoImage(uri: Uri?, width: Int, height: Int) {
        if(uri == null || context == null)
            return

        val bm = ImageUtils.getScaledBitmapFromUri(requireContext(), uri, width, height)

        if(bm != null) {
            val imageView = photo_image as ImageView
            imageView.setImageBitmap(bm)
        }
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(view?.getWindowToken(), 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(CAMERA_IMAGE_URI_BUNDLE_KEY, cameraImageUri)
    }

    companion object {
        private val ARG_CAT_DATA = "CatData"

        private val CAMERA_IMAGE_URI_BUNDLE_KEY = "CameraImageUri"

        private val IMAGE_PERMISSIONS_CODE = 1000
        private val AUDIO_PERMISSIONS_CODE = 1001
        private val IMAGE_PICK_CODE = 1002
        private val AUDIO_PICK_CODE = 1003

        @JvmStatic
        fun newInstance(catData: CatData?) =
            CatProfileFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_CAT_DATA, catData)
                }
            }
    }
}
