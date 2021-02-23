package com.sergsave.pocat.screens.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sergsave.pocat.helpers.Event
import com.sergsave.pocat.models.Card

class NavigationViewModel : ViewModel() {

    private var pagePosition: Int? = null

    data class OpenCatData(val card: Card, val transition: SharedElementTransitionData)

    private val _openCatEvent = MutableLiveData<Event<OpenCatData>>()
    val openCatEvent: LiveData<Event<OpenCatData>>
        get() = _openCatEvent

    private val _addNewCatEvent = MutableLiveData<Event<Unit>>()
    val addNewCatEvent: LiveData<Event<Unit>>
        get() = _addNewCatEvent

    private val _pageChangedEvent = MutableLiveData<Event<Unit>>()
    val pageChangedEvent: LiveData<Event<Unit>>
        get() = _pageChangedEvent

    fun openCat(card: Card, transition: SharedElementTransitionData) {
        _openCatEvent.value = Event(OpenCatData(card, transition))
    }

    fun onPageChanged(position: Int) {
        if(position != pagePosition)
            _pageChangedEvent.value = Event(Unit)

        pagePosition = position
    }

    fun addNewCat() {
        _addNewCatEvent.value = Event(Unit)
    }
}