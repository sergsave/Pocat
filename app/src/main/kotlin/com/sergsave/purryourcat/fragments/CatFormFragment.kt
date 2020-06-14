package com.sergsave.purryourcat.fragments

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import com.google.android.material.transition.MaterialFadeThrough
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.activities.CatDataViewModel
import com.sergsave.purryourcat.helpers.*
import com.sergsave.purryourcat.models.CatData
import kotlinx.android.synthetic.main.view_form_fields.view.*
import kotlinx.android.synthetic.main.fragment_cat_form.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel

class CatFormFragment : Fragment() {

    interface OnApplyListener {
        fun onApply()
    }

    enum class Mode {
        CREATE, EDIT
    }

    private val model: CatDataViewModel by activityViewModels()
    private var cameraImageUri: Uri? = null
    private var onApplyListener: OnApplyListener? = null
    private lateinit var mode: Mode

    override fun onDestroy() {
        // TODO? Save keyboard visible after orientation change
        hideKeyboard()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(context != null)
            enterTransition = MaterialFadeThrough.create(requireContext())

        cameraImageUri = savedInstanceState?.getParcelable(CAMERA_IMAGE_URI_BUNDLE_KEY)

        arguments?.let {
            val ordinal = it.getInt(ARG_MODE)
            mode = Mode.values().get(ordinal)
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
        model.data.observe(this, Observer<CatData> { cat ->
            ImageUtils.loadInto(context, cat?.photoUri, photo_image as ImageView)
            form_layout.sound_edit_text.setText(cat?.purrAudioUri?.getLastPathSegment())
        })

        setupToolbar(mode)

        setHasOptionsMenu(true)

        fab.setOnClickListener{ addPhoto() }
        photo_image.setOnClickListener{ addPhoto() }

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

        val catData = model.data.value
        if(catData == null)
            return

        when(requestCode) {
            IMAGE_PICK_CODE -> {
                // Null data - image from camera
                val uri = data?.data ?: cameraImageUri
                catData.photoUri = saveFileOnInternal(uri)
            }
            AUDIO_PICK_CODE -> {
                val uri = data?.data
                catData.purrAudioUri = uri
            }
        }

        model.change(catData)
    }

    // TODO: Filesystem utils
    private fun saveFileOnInternal(uri: Uri?) : Uri? {
        if(uri == null)
            return null

        val path = getRealPathFromURI(uri)

        if(context == null || path == null)
            return null

        val name = path.substring(path.lastIndexOf("/") + 1)

        val file = File(requireContext().filesDir, name)
        copyFile(File(path), file)
        return Uri.fromFile(file)
    }

    private fun copyFile(sourceFile: File, destFile: File) {
        if (!sourceFile.exists()) {
            return
        }
        var source: FileChannel?
        var destination: FileChannel?
        source = FileInputStream(sourceFile).getChannel()
        destination = FileOutputStream(destFile).getChannel()
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size())
        }
        if (source != null) {
            source.close()
        }
        if (destination != null) {
            destination.close()
        }
    }

    fun getRealPathFromURI(contentUri: Uri): String? {
        var res: String? = null
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = requireContext().getContentResolver().query(contentUri, proj, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            res = cursor.getString(column_index)
        }
        cursor?.close()
        return res
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
        private val CAMERA_IMAGE_URI_BUNDLE_KEY = "CameraImageUri"

        private val IMAGE_PERMISSIONS_CODE = 1000
        private val AUDIO_PERMISSIONS_CODE = 1001
        private val IMAGE_PICK_CODE = 1002
        private val AUDIO_PICK_CODE = 1003

        private val ARG_MODE = "Mode"

        @JvmStatic
        fun newInstance(mode: Mode) =
            CatFormFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_MODE, mode.ordinal)
                }
            }
    }
}
