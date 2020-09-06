package com.sergsave.purryourcat.ui.catcard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import com.sergsave.purryourcat.helpers.Event

class NavigationViewModel(isThereCatId: Boolean, isThereSharingInputData: Boolean)
    : ViewModel() {

    private enum class Page {
        ADD_NEW,
        EDIT,
        OPEN,
        EXTRACT
    }

    private var page: Page

    init {
        when {
            isThereCatId == true -> goToPage(Page.Open)
            isThereSharingInputData -> goToPage(Page.EXTRACT)
            else -> goToPage(Page.ADD_NEW)
        }
    }

    private val _editCatEvent = MutableLiveData<Event<Unit>>()
    val editCatEvent: LiveData<Event<Unit>>
        get() = _editCatEvent

    private val _addNewCatEvent = MutableLiveData<Event<Unit>>()
    val addNewCatEvent: LiveData<Event<Unit>>
        get() = _addNewCatEvent

    private val _openSavedCatEvent = MutableLiveData<Event<String>>()
    val openSaveCatEvent: LiveData<Event<String>>
        get() = _openSavedCatEvent

    private val _openUnsavedCatEvent = MutableLiveData<Event<CatData>>()
    val openUnsavedCatEvent: LiveData<Event<CatData>>
        get() = _openUnsavedCatEvent

    private val _startLoadSharingDataEvent = MutableLiveData<Event<Unit>>()
    val startLoadSharingDataEvent: LiveData<Event<Unit>>
        get() = _startLoadSharingData

    private val _startSharedElementTransitionEvent = MutableLiveData<Event<Unit>>()
    val startSharedElementTransitionEvent: LiveData<Event<Unit>>
        get() = _startSharedElementTransitionEvent

    private val _backPressedEvent = MutableLiveData<Event<Unit>>()
    val backPressedEvent: LiveData<Event<Unit>>
        get() = _backPressedEvent

    private val _finishEvent = MutableLiveData<Event<Unit>>()
    val finishEvent: LiveData<Event<Unit>>
        get() = _finishEvent

    private fun goToPage(page: Page) {
        this.page = page

        when(page) {
            Page.ADD_NEW -> _newCatEvent.value = Event(Unit)
            Page.EDIT -> _editCatEvent.value = Event(Unit)
            Page.OPEN -> _openCatEvent.value = Event(Unit)
            Page.LOADING -> _startLoadSharingData.value = Event(Unit)
        }
    }

    fun editCat() {
        goToPage(Page.EDIT)
    }

    fun openCat(id: String) {
        goToPage(Page.OPEN)
    }

    fun openCat(catData: CatData) {
        goToPage
    }

    fun addNewCat() {
        goToPage(Page.ADD_NEW)
    }

    fun onBackButtonPressed() {
        _backPressedEvent = Event(Unit)
    }

    fun goToBackScreen() {
        if(page == Page.EDIT)
            goToPage(Page.OPEN)
        else
            _finishEvent.value = Event(Unit)
    }

    fun startSharedElementTransition() {
        _startSharedElementTransitionEvent.value = Event(Unit)
    }
}

class NavigationViewModelFactory(isThereCatId: Boolean, isThereSharingInputData: Boolean): Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(NavigationViewModel::class.java)) {
            NavigationViewModel(catId, inputSharingData, transitionName) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}