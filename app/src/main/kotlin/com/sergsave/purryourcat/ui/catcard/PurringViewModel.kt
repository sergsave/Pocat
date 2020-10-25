package com.sergsave.purryourcat.ui.catcard

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sergsave.purryourcat.data.CatDataRepository
import com.sergsave.purryourcat.helpers.DisposableViewModel
import com.sergsave.purryourcat.helpers.Event
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.preference.PreferenceManager
import com.sergsave.purryourcat.sharing.Pack
import com.sergsave.purryourcat.sharing.SharingManager

class PurringViewModel(
    private val catDataRepository: CatDataRepository,
    private val sharingManager: SharingManager,
    private val preferences: PreferenceManager,
    private val sharingErrorStringId: Int,
    private var cat: Cat
) : DisposableViewModel() {

    sealed class Cat {
        data class Saved(val catId: String): Cat()
        data class Unsaved(val catData: CatData): Cat()
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

    private val _sharingFailedStringIdEvent = MutableLiveData<Event<Int>>()
    val sharingFailedStringIdEvent: LiveData<Event<Int>>
        get() = _sharingFailedStringIdEvent

    // Use intent is safe here because we don't save reference to any context.
    private val _sharingSuccessEvent = MutableLiveData<Event<Intent>>()
    val sharingSuccessEvent: LiveData<Event<Intent>>
        get() = _sharingSuccessEvent

    private val _editCatEvent = MutableLiveData<Event<String>>()
    val editCatEvent: LiveData<Event<String>>
        get() = _editCatEvent

    init {
        when(cat) {
            is Cat.Saved -> _menuState.value = MenuState.SHOW_SAVED
            is Cat.Unsaved -> {
                _menuState.value = MenuState.SHOW_UNSAVED
                _catData.value = (cat as? Cat.Unsaved)?.catData
            }
        }

        val disposable = catDataRepository.read().subscribe { cats ->
            val id = (cat as? Cat.Saved)?.catId

            if(id != null)
                cats.get(id)?.let { _catData.value = it.data }
        }
        addDisposable(disposable)
    }

    fun onSharePressed() {
        val pack = _catData.value?.let { Pack(it) }
        val single = pack?.let { sharingManager.makeTakeObservable(it) }
        if(single == null)
            return

        _menuState.value = MenuState.SHARING

        val disposable = single
            .doOnEvent{ _,_ -> _menuState.value = MenuState.SHOW_SAVED }
            .subscribe(
                { data -> _sharingSuccessEvent.value = Event(data) },
                { _sharingFailedStringIdEvent.value = Event(sharingErrorStringId) }
            )

        addDisposable(disposable)
    }

    fun onSavePressed() {
        _menuState.value = MenuState.SHOW_SAVED
        _dataSavedEvent.value = Event(Unit)
        val data = (cat as? Cat.Unsaved)?.catData
        if(data != null) {
            addDisposable(catDataRepository.add(data).subscribe { id ->
                cat = Cat.Saved(id)
            })
        }
    }

    fun onEditPressed() {
        (cat as? Cat.Saved)?.catId?.let { _editCatEvent.value = Event(it) }
    }

    val isVibrationEnabled: Boolean
        get() = preferences.isVibrationEnabled

    var isTutorialAchieved: Boolean
        get() = preferences.isPurringTutorialAchieved
        set(value) { preferences.isPurringTutorialAchieved = value }
}