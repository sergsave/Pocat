package com.sergsave.purryourcat.screens.catcard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sergsave.purryourcat.helpers.Event
import com.sergsave.purryourcat.models.Card

class NavigationViewModel(card: Card?, isThereSharingInputData: Boolean)
    : ViewModel() {

    sealed class Page {
        object AddNew : Page()
        class Edit(val card: Card): Page()
        class Open(val card: Card): Page()
        object Extract : Page()
    }

    private lateinit var page: Page

    private val _editCatEvent = MutableLiveData<Event<Card>>()
    val editCatEvent: LiveData<Event<Card>>
        get() = _editCatEvent

    private val _addNewCatEvent = MutableLiveData<Event<Unit>>()
    val addNewCatEvent: LiveData<Event<Unit>>
        get() = _addNewCatEvent

    private val _openCatEvent = MutableLiveData<Event<Card>>()
    val openCatEvent: LiveData<Event<Card>>
        get() = _openCatEvent

    private val _startExtractSharingDataEvent = MutableLiveData<Event<Unit>>()
    val startExtractSharingDataEvent: LiveData<Event<Unit>>
        get() = _startExtractSharingDataEvent

    private val _backPressedEvent = MutableLiveData<Event<Unit>>()
    val backPressedEvent: LiveData<Event<Unit>>
        get() = _backPressedEvent

    private val _finishEvent = MutableLiveData<Event<Unit>>()
    val finishEvent: LiveData<Event<Unit>>
        get() = _finishEvent

    private val _showTutorialEvent = MutableLiveData<Event<Unit>>()
    val showTutorialEvent: LiveData<Event<Unit>>
        get() = _showTutorialEvent

    private val _tutorialFinishedEvent = MutableLiveData<Event<Unit>>()
    val tutorialFinishedEvent: LiveData<Event<Unit>>
        get() = _tutorialFinishedEvent

    init {
        when {
            card != null -> goToPage(Page.Open(card))
            isThereSharingInputData -> goToPage(Page.Extract)
            else -> goToPage(Page.AddNew)
        }
    }

    private fun goToPage(page: Page) {
        this.page = page

        when(page) {
            is Page.AddNew -> _addNewCatEvent.value = Event(Unit)
            is Page.Edit -> _editCatEvent.value = Event(page.card)
            is Page.Open -> _openCatEvent.value = Event(page.card)
            is Page.Extract -> _startExtractSharingDataEvent.value = Event(Unit)
        }
    }

    // Available only for cats saved in persistent repo
    fun editCat(card: Card) {
        goToPage(Page.Edit(card))
    }

    fun openCat(card: Card) {
        goToPage(Page.Open(card))
    }

    fun onBackButtonPressed() {
        _backPressedEvent.value = Event(Unit)
    }

    fun showTutorial() {
        _showTutorialEvent.value = Event(Unit)
    }

    fun onTutorialFinished() {
        _tutorialFinishedEvent.value = Event(Unit)
    }

    fun goToBackScreen() {
        val page = this.page
        if(page is Page.Edit)
            goToPage(Page.Open(page.card))
        else
            _finishEvent.value = Event(Unit)
    }
}