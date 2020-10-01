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
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.sergsave.purryourcat.Constants
import com.sergsave.purryourcat.MyApplication
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.EventObserver
import com.sergsave.purryourcat.helpers.ImageUtils
import com.sergsave.purryourcat.helpers.PermissionUtils
import com.sergsave.purryourcat.ui.catcard.FormViewModel.SoundButtonType
import com.sergsave.purryourcat.ui.soundselection.SoundSelectionActivity
import kotlinx.android.synthetic.main.fragment_cat_form.*
import kotlinx.android.synthetic.main.view_form_fields.view.*

class FormFragment : Fragment() {

    private var cameraImageUri: Uri? = null
    private val navigation: NavigationViewModel by activityViewModels()
    private val viewModel: FormViewModel by viewModels {
        (requireActivity().application as MyApplication).appContainer
            .provideFormViewModelFactory(arguments?.getString(ARG_CAT_ID))
    }

    override fun onDestroy() {
        // TODO? Save keyboard visible after orientation change
        hideKeyboard()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState?.let {
            cameraImageUri = it.getParcelable(BUNDLE_KEY_CAMERA_IMAGE_URI)
            restoreDialogsState()
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

        navigation.backPressedEvent.observe(viewLifecycleOwner, EventObserver {
            if(viewModel.handleBackPressed())
                navigation.goToBackScreen()
        })

        viewModel.apply {
            name.observe(viewLifecycleOwner, Observer {
                form_layout.name_edit_text.apply { if (isFocused.not()) setText(it) }
            })

            photoUri.observe(viewLifecycleOwner, Observer {
                ImageUtils.loadInto(context, it, photo_image)
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

            notValidDataMessageEvent.observe(viewLifecycleOwner, EventObserver {
                NotValidDataDialog().show(childFragmentManager, null)
            })

            fileSizeExceededMessageEvent.observe(viewLifecycleOwner, EventObserver {
                if(context != null) {
                    val formattedSize = Formatter.formatShortFileSize(requireContext(), it)
                    val message = requireContext().resources.getString(
                        R.string.file_size_exceeded_message_text,
                        formattedSize
                    )

                    Snackbar.make(main_layout, message, Snackbar.LENGTH_LONG).show()
                }
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

        val intent = Intent(requireContext(), SoundSelectionActivity::class.java)
        intent.putExtra(Constants.AUDIO_URI_INTENT_KEY, viewModel.audioUri.value)
        startActivityForResult(intent, PICK_AUDIO_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(!PermissionUtils.checkRequestResult(grantResults))
            return

        if(requestCode == PERMISSIONS_IMAGE_CODE)
            sendPhotoIntent()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK)
            return

        when(requestCode) {
            PICK_IMAGE_CODE -> {
                // Null data - image from camera
                val uri = data?.data ?: cameraImageUri
                uri?.let{ viewModel.changePhoto(it) }
            }
            PICK_AUDIO_CODE -> {
                data?.data?.let { viewModel.changeAudio(it) }
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
        private const val ARG_CAT_ID = "ArgCatId"

        @JvmStatic
        fun newInstance(catId: String?) =
            FormFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CAT_ID, catId)
                }
            }
    }
}