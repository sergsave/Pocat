package com.sergsave.purryourcat.ui.catcard

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import com.sergsave.purryourcat.data.CatDataRepository
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.preference.PreferenceReader
import com.sergsave.purryourcat.sharing.Pack
import com.sergsave.purryourcat.sharing.SharingManager


class PurringViewModel(
    private var catDataRepository: CatDataRepository,
    private var sharingManager: SharingManager,
    private var preferences: PreferenceReader
    private var inputData: InputData
) : ViewModel() {

    sealed class InputData
    data class SavedData(catId: String): InputData
    data class UnsavedData(catData: CatData): InputData

    private var disposable = CompositeDisposable()

    enum class MenuState {
        SHOW_SAVED,
        SHOW_UNSAVED,
        SHARING
    }

    init {
        when(inputData) {
            is SavedData -> {
                _menuState = MenuState.SHOW_SAVED

                val disposable = catDataRepository.read().subscribe { cats ->
                    cats.get(id)?.let { _catData = it }
                }
                this.disposable.add(disposable)
            }
            is UnsavedData -> {
                _menuState = MenuState.SHOW_UNSAVED
                _catData = data
            }
        }
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }

    private val _catData = MutableLiveData<CatData>()
    val catData: LiveData<CatData>
        get() = _catData

    private val _menuState = MutableLiveData<MenuState>()
    val menuState: LiveData<Event<MenuState>>
        get() = _menuState

    private val _sharingFailedEvent = MutableLiveData<Event<String>>()
    val sharingFailedEvent: LiveData<Event<String>>
        get() = _sharingFailedEvent

    // Use intent is safe here because we don't save reference to any context.
    private val _sharingSuccessEvent = MutableLiveData<Event<Intent>>()
    val sharingSuccessEvent: LiveData<Event<Unit>>
        get() = _sharingSuccessEvent

    fun startSharing() {
        val pack = Pack(CatData(_name.value, _photoUri.value, _audioUri.value))
        val single = sharingManager.makeTakeObservable(pack)
        if(single == null)
            return

        val disposable = single.subscribe {
            { data -> _sharingSuccessEvent.value = Event(data) },
            { throwable -> _sharingFailedEvent.value = throwable.message }
        }

        this.disposable.add(disposable)
    }

    fun saveData() {
        if(inputData is Unsaved)
            disposable.add(catDataRepository.add(inputData.catData).subscribe { _ -> })
    }

    fun isVibrationEnabled() {
        if(preferences.isVibrationEnabled.not())
            return
    }
}

class PurringViewModelFactory(
    private var catDataRepository: CatDataRepository,
    private var contentRepository: ContentRepository,
    private val inputData: PurringViewModel.InputData
): Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(PurringViewModel::class.java)) {
            PurringViewModel(catDataRepository, contentRepository, catId) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}