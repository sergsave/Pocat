package com.sergsave.purryourcat.ui.catcard

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sergsave.purryourcat.data.CatDataRepository
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.preference.PreferenceReader
import com.sergsave.purryourcat.sharing.Pack
import com.sergsave.purryourcat.sharing.SharingManager
import com.sergsave.purryourcat.helpers.Event
import com.sergsave.purryourcat.helpers.DisposableViewModel

class PurringViewModel(
    private val catDataRepository: CatDataRepository,
    private val sharingManager: SharingManager,
    private val preferences: PreferenceReader,
    private val inputData: InputData
) : DisposableViewModel() {

    sealed class InputData {
        data class Saved(val catId: String): InputData()
        data class Unsaved(val catData: CatData): InputData()
    }

    enum class MenuState {
        SHOW_SAVED,
        SHOW_UNSAVED,
        SHARING
    }

    private val _catData = MutableLiveData<CatData>()
    val catData: LiveData<CatData>
        get() = _catData

    private val _menuState = MutableLiveData<MenuState>()
    val menuState: LiveData<MenuState>
        get() = _menuState

    private val _dataSavedEvent = MutableLiveData<Event<Unit>>()
    val dataSavedEvent: LiveData<Event<Unit>>
        get() = _dataSavedEvent

    private val _sharingFailedEvent = MutableLiveData<Event<String>>()
    val sharingFailedEvent: LiveData<Event<String>>
        get() = _sharingFailedEvent

    // Use intent is safe here because we don't save reference to any context.
    private val _sharingSuccessEvent = MutableLiveData<Event<Intent>>()
    val sharingSuccessEvent: LiveData<Event<Intent>>
        get() = _sharingSuccessEvent

    init {
        when(inputData) {
            is InputData.Saved -> {
                _menuState.value = MenuState.SHOW_SAVED

                val disposable = catDataRepository.read().subscribe { cats ->
                    cats.get(inputData.catId)?.let { _catData.value = it }
                }
                addDisposable(disposable)
            }
            is InputData.Unsaved -> {
                _menuState.value = MenuState.SHOW_UNSAVED
                _catData.value = inputData.catData
            }
        }
    }

    fun startSharing() {
        val pack = _catData.value?.let { Pack(it) }
        val single = pack?.let { sharingManager.makeTakeObservable(it) }
        if(single == null)
            return

        _menuState.value = MenuState.SHARING
        val disposable = single
            .doOnEvent{ _,_ -> _menuState.value = MenuState.SHOW_SAVED }
            .subscribe(
            { data -> _sharingSuccessEvent.value = Event(data) },
            { throwable -> throwable.message?.let { _sharingFailedEvent.value = Event(it) } }
        )

        addDisposable(disposable)
    }

    fun saveData() {
        _menuState.value = MenuState.SHOW_SAVED
        _dataSavedEvent.value = Event(Unit)
        if(inputData is InputData.Unsaved)
            addDisposable(catDataRepository.add(inputData.catData).subscribe { _ -> })
    }

    fun isVibrationEnabled(): Boolean {
        return preferences.isVibrationEnabled
    }
}