package com.sergsave.purryourcat.ui.catcard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sergsave.purryourcat.helpers.Event
import com.sergsave.purryourcat.models.CatData

class NavigationViewModel(catId: String?, isThereSharingInputData: Boolean)
    : ViewModel() {

    sealed class Page {
        object AddNew : Page()
        class Edit(val id: String): Page()
        class OpenById(val id: String): Page()
        class OpenByData(val data: CatData): Page()
        object Extract : Page()
    }

    private lateinit var page: Page

    private val _editCatEvent = MutableLiveData<Event<String>>()
    val editCatEvent: LiveData<Event<String>>
        get() = _editCatEvent

    private val _addNewCatEvent = MutableLiveData<Event<Unit>>()
    val addNewCatEvent: LiveData<Event<Unit>>
        get() = _addNewCatEvent

    private val _openSavedCatEvent = MutableLiveData<Event<String>>()
    val openSavedCatEvent: LiveData<Event<String>>
        get() = _openSavedCatEvent

    private val _openUnsavedCatEvent = MutableLiveData<Event<CatData>>()
    val openUnsavedCatEvent: LiveData<Event<CatData>>
        get() = _openUnsavedCatEvent

    private val _startExtractSharingDataEvent = MutableLiveData<Event<Unit>>()
    val startExtractSharingDataEvent: LiveData<Event<Unit>>
        get() = _startExtractSharingDataEvent

    private val _backPressedEvent = MutableLiveData<Event<Unit>>()
    val backPressedEvent: LiveData<Event<Unit>>
        get() = _backPressedEvent

    private val _finishEvent = MutableLiveData<Event<Unit>>()
    val finishEvent: LiveData<Event<Unit>>
        get() = _finishEvent

    init {
        when {
            catId != null -> goToPage(Page.OpenById(catId))
            isThereSharingInputData -> goToPage(Page.Extract)
            else -> goToPage(Page.AddNew)
        }
    }

    private fun goToPage(page: Page) {
        this.page = page

        when(page) {
            is Page.AddNew -> _addNewCatEvent.value = Event(Unit)
            is Page.Edit -> _editCatEvent.value = Event(page.id)
            is Page.OpenById -> _openSavedCatEvent.value = Event(page.id)
            is Page.OpenByData -> _openUnsavedCatEvent.value = Event(page.data)
            is Page.Extract -> _startExtractSharingDataEvent.value = Event(Unit)
        }
    }

    fun editCat(id: String) {
        goToPage(Page.Edit(id))
    }

    fun openCat(id: String) {
        goToPage(Page.OpenById(id))
    }

    fun openCat(catData: CatData) {
        goToPage(Page.OpenByData(catData))
    }

    fun onBackButtonPressed() {
        _backPressedEvent.value = Event(Unit)
    }

    fun goToBackScreen() {
        val page = this.page
        if(page is Page.Edit)
            goToPage(Page.OpenById(page.id))
        else
            _finishEvent.value = Event(Unit)
    }
}