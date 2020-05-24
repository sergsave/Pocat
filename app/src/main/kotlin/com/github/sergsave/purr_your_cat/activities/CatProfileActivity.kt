package com.github.sergsave.purr_your_cat

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_cat_profile.*


class CatProfileActivity : AppCompatActivity() {

    enum class Mode {
        CREATE, EDIT;

        fun attachTo(intent: Intent) {
            intent.putExtra(KEY, ordinal)
        }

        companion object {
            private val KEY = "CatProfileActivityMode"
            private val values = values()

            fun detachFrom(intent: Intent) : Mode? {
                if(!intent.hasExtra(KEY))
                    return null

                val value = intent.getIntExtra(KEY, -1)
                return values.firstOrNull { it.ordinal == value}
            }
        }
    }

    private var cameraImageUri: Uri? = null
    private var currentImageUri: Uri? = null
    private var currentAudioUri: Uri? = null

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cat_profile)

        restoreState(savedInstanceState)

        val mode = Mode.detachFrom(getIntent())
        setupToolbar(mode)

        fab.setOnClickListener{ addPhoto() }
        photo_image.setOnClickListener{ addPhoto() }

        // Restore photo
        photo_layout.setOnSizeReadyListener{ width, height ->
            setPhotoImage(currentImageUri, width, height)
        }

        // Clear focus on Ok in keyboard
        name_edit_text.setImeOptions(EditorInfo.IME_ACTION_DONE)
        name_edit_text.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                clearFocus(v)
            }
            false
        }
        
        sound_edit_text.setOnClickListener { addAudio() }

        apply_button.setOnClickListener {
            val intent = Intent(this, PurringActivity::class.java)
            intent.putExtra("cat_name", "cat name")
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)

            finish()
        }
    }

    private fun setupToolbar(mode: Mode?) {
        val title = when(mode) {
            Mode.CREATE -> getResources().getString(R.string.add_new_cat)
            Mode.EDIT -> getResources().getString(R.string.edit_cat)
            else -> ""
        }

        setSupportActionBar(toolbar)
        val actionBar = getSupportActionBar()

        actionBar?.title = title
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener{ finish() }
    }

    private fun addPhoto() {
        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA)

        if(permissions.any { !PermissionUtils.checkPermission(this, it) })
            PermissionUtils.requestPermissions(this, permissions, IMAGE_PERMISSIONS_CODE)
        else
            sendPhotoIntent()
    }

    private fun addAudio() {
        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO)

        if(permissions.any { !PermissionUtils.checkPermission(this, it) })
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

        // TODO: Rename file! For accessibility in gallery
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        cameraImageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

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

        // TODO: Rename file! For accessibility in gallery
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Audio")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Recorder")
        val recorderAudioUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

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
                setPhotoImage(uri, photo_layout.width, photo_layout.height)
                currentImageUri = uri
            }
            AUDIO_PICK_CODE -> {
                val uri = data?.data
                sound_edit_text.setText(uri?.getLastPathSegment())
                currentAudioUri = uri
            }
        }
    }

    private fun setPhotoImage(uri: Uri?, width: Int, height: Int) {
        if(uri == null)
            return

        val bm = ImageUtils.getScaledBitmapFromUri(this, uri, width, height)

        if(bm == null)
            return

        // Implement blurred image frame
        photo_image.setImageBitmap(bm)

        val blurred = ImageUtils.blur(this, bm, ImageUtils.BlurIntensity.HIGH)
        photo_frame_image.setImageBitmap(blurred)
    }

    private fun clearFocus(v: View) {
        v.clearFocus()
        val imm: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
    }

    // Clear focus on tap outside
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val ret = super.dispatchTouchEvent(event)

        if (event.action != MotionEvent.ACTION_DOWN)
            return ret

        val v: View? = currentFocus
        if (v !is EditText)
            return ret

        val outRect = Rect()
        v.getGlobalVisibleRect(outRect)
        if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt()))
            clearFocus(v)

        return ret
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val uriMap = mapOf<String, Uri?>(
            CAMERA_IMAGE_URI_KEY to cameraImageUri,
            CURRENT_IMAGE_URI_KEY to currentImageUri,
            CURRENT_AUDIO_URI_KEY to currentAudioUri
        )

        uriMap.forEach { (key, value) -> outState.putString(key, value?.toString()) }
    }

    private fun restoreState(savedInstanceState: Bundle?) {

        val parse : (String)->Uri? = { key ->
            val path = savedInstanceState?.getString(key)
            if(path != null)
                Uri.parse(path)
            else
                null
        }

        cameraImageUri = parse(CAMERA_IMAGE_URI_KEY)
        currentImageUri = parse(CURRENT_IMAGE_URI_KEY)
        currentAudioUri = parse(CURRENT_AUDIO_URI_KEY)
    }

    companion object {
        private val IMAGE_PERMISSIONS_CODE = 1000
        private val AUDIO_PERMISSIONS_CODE = 1001
        private val IMAGE_PICK_CODE = 1002
        private val AUDIO_PICK_CODE = 1003

        private val CAMERA_IMAGE_URI_KEY = "CameraUri"
        private val CURRENT_IMAGE_URI_KEY = "ImageUri"
        private val CURRENT_AUDIO_URI_KEY = "AudioUri"
    }
}
