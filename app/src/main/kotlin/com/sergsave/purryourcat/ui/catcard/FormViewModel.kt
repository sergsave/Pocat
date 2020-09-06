package com.sergsave.purryourcat.ui.catcard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.data.CatDataRepository
import com.sergsave.purryourcat.content.ContentRepository
import com.sergsave.purryourcat.helpers.FileUtils
import com.sergsave.purryourcat.models.CatData
import io.reactivex.rxjava3.disposables.CompositeDisposable

class FormViewModel(
    private var catDataRepository: CatDataRepository,
    private var contentRepository: ContentRepository,
    private var catId: String? = null
) : AndroidViewModel() {
    private var backup: CatData? = null
    private var disposable = CompositeDisposable()

    init {
        val disposable = catDataRepository.read().subscribe { cats ->
            catId?.let { updateData(cats.get(it)) }
        }
        this.disposable.add(disposable)

        viewModel.saveBackup()
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }

    private val _name = MutableLiveData<String>()
    val name: LiveData<String>
        get() = _name

    private val _photoUri = MutableLiveData<Uri>()
    val photoUri: LiveData<Uri>
        get() = _photoUri

    private val _audioUri = MutableLiveData<Uri>()
    val audioUri: LiveData<Uri>
        get() = _audioUri

    val audioName: LiveData<String> = Transformations.map(_audioUri) {
        nameFromUri(it)
    }

    private val _snackbarText = MutableLiveData<Event<String>>()
    val snackbarMessage: LiveData<Event<String>>
        get() = _snackbarText

    private val _snackbarText = MutableLiveData<Event<String>>()
    val snackbarMessage: LiveData<Event<String>>
        get() = _snackbarText

    private val _unsavedChangesMessage = MutableLiveData<Event<Unit>>()
    val unsavedChangesMessage: LiveData<Event<Unit>>
        get() = _unsavedChangesMessage

    private val _notValidDataMessage = MutableLiveData<Event<Unit>>()
    val notValidDataMessage: LiveData<Event<Unit>>
        get() = _notValidDataMessage

    fun getToolbarTitle() {
        val contenxt = getApplication<Application>().applicationContext
        return if(catId == null) context.getString(R.string.add_new_cat)
            else context.getString(R.string.edit_cat)
    }

    fun changeName(name: String) {
        _name.value = name
    }

    fun changePhoto(uri: Uri?) {
        if(checkFileSize(uri, contentRepository.maxImageFileSize).not()) {
            _photoUri.value = _photoUri.value
            return
        }

        if(uri != _photoUri.value) {
            disposable.add(contentRepository.addImage(uri).subscribe {
                _photoUri.value = it
            })
        }
    }

    fun changeAudio(uri: Uri?) {
        if(checkFileSize(uri, contentRepository.maxAudioFileSize).not()) {
            _audioUri.value = _audioUri.value
            return
        }

        if(uri != _audioUri.value) {
            disposable.add(contentRepository.addAduio(uri).subscribe {
                _audioUri.value = it
            })
        }
    }

    fun handleApplyPressed(): Boolean {
        if (isCurrentDataValid()) {
            _notValidDataMessage.value = Event(Unit)
            return false
        }

        cleanBackup()
        syncDataWithRepo()
        return true
    }

    fun handleBackPressed(): Boolean {
        if(viewModel.wereChangesAfterBackup().not())
            return true

        _unsavedChangesMessage.value = Event(Unit)
        return false
    }

    fun onDiscardChanges(): Boolean {
        restoreFromBackup()
    }

    private fun checkFileSize(uri: Uri, size: Long): Boolean {
        val context = getApplication<Application>().applicationContext
        val size = FileUtils.getContentFileSize(context, uri)

        size < contentRepository.maxImage

        val formattedSize = Formatter.formatShortFileSize(context, size)
        val message = context.resources.getString(R.string.file_size_exceeded_message_text, formattedSize)

        _snackbarText.value = message
    }

    private fun nameFromUri(uri: Uri?): String {
        return FileUtils.getContentFileName(getApplication<Application>().applicationContext, uri)
    }

    private fun syncDataWithRepo() {
        catId?.let { id ->
            disposable.add(catDataRepository.update(id, currentData()).subscribe { _ -> })
        } ?: run {
            disposable.add(catDataRepository.add(currentData()).subscribe { id ->
                updateData(catDataRepository.read(id))
                catId = id
            })
        }
    }

    private fun currentData() = CatData(_name.value, _photoUri.value, _audioUri.value)

    private fun updateData(data: CatData?) {
        _name.value = data?.name
        _photoUri.value = data?.photoUri
        _audioUri.value = data?.purrAudioUri
    }

    private fun isCurrentDataValid(): Boolean {
        return _name.value != null && _photoUri.value != null && _audioUri.value != null
    }

    private fun saveBackup() {
        backup = currentData()
    }

    private fun restoreFromBackup() {
        backup?.let { updateData(it) }
        backup = null
    }

    private fun wereChangesAfterBackup(): Boolean {
        return currentData() != backup
    }

    private fun hasBackup(): Boolean {
        return backup != null
    }

    private fun cleanBackup() {
        backup = null
    }
}

class FormViewModelFactory(
    private var catDataRepository: CatDataRepository,
    private var contentRepository: ContentRepository,
    private val catId: String?
): Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(FormViewModel::class.java)) {
            FormViewModel(catDataRepository, contentRepository, catId) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}