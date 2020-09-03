package com.sergsave.purryourcat.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import com.sergsave.purryourcat.data.CatDataRepository
import com.sergsave.purryourcat.content.ContentRepository
import com.sergsave.purryourcat.models.combineContent
import com.sergsave.purryourcat.models.CatData
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.Singles

class CatCardViewModel(
    private var catDataRepository: CatDataRepository,
    private var contentRepository: ContentRepository,
    private var catId: String? = null
) : ViewModel() {
    private val _data = MutableLiveData<CatData>()
    private var backup: CatData? = null
    private var disposable = CompositeDisposable()

    init {
        val disposable = catDataRepository.read().subscribe { cats ->
            _data.value = catId?.let{ cats.get(it) } ?: CatData()
        }
        this.disposable.add(disposable)
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }

    val data : LiveData<CatData>
        get() = _data

    val maxAudioFileSize = contentRepository.maxAudioFileSize
    val maxImageFileSize = contentRepository.maxImageFileSize

    fun syncDataWithRepo() {
        val data = _data.value
        if(data == null)
            return

        catId?.let { id ->
            disposable.add(catDataRepository.update(id, data).subscribe { _ -> })
        } ?: run {
            disposable.add(catDataRepository.add(data).subscribe { id -> updateData(id) })
        }
    }

    private fun updateData(id: String) {
        val disposable = catDataRepository.read()
            .take(1)
            .subscribe { cats ->
                _data.value = cats.get(id) ?: CatData()
                catId = id
            }
        this.disposable.add(disposable)
    }

    fun change(data: CatData) {
        _data.value = _data.value?.copy(name = data.name)

        if(data.photoUri != _data.value?.photoUri) {
            disposable.add(contentRepository.addImage(data.photoUri).subscribe { uri ->
                _data.value = _data.value?.copy(photoUri = uri)
            })
        }

        if(data.purrAudioUri != _data.value?.purrAudioUri) {
            disposable.add(contentRepository.addAudio(data.purrAudioUri).subscribe { uri ->
                _data.value = _data.value?.copy(purrAudioUri = uri)
            })
        }
    }

    fun saveBackup() {
        backup = _data.value
    }

    fun restoreFromBackup() {
        backup?.let { _data.value = it }
        backup = null
    }

    fun wereChangesAfterBackup(): Boolean {
        return _data.value != backup
    }

    fun hasBackup(): Boolean {
        return backup != null
    }

    fun cleanBackup() {
        backup = null
    }
}

class CatCardViewModelFactory(
    private var catDataRepository: CatDataRepository,
    private var contentRepository: ContentRepository,
    private val catId: String?
): Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(CatCardViewModel::class.java)) {
            CatCardViewModel(catDataRepository, contentRepository, catId) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}