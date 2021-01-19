package com.sergsave.pocat.screens.main

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sergsave.pocat.helpers.Event
import com.sergsave.pocat.helpers.Long2StringIdMapper
import com.sergsave.pocat.models.CatData

class CatsListViewModel: ViewModel() {
    private var selection = listOf<Long>()
    private val idMapper = Long2StringIdMapper()
    private var wasClickedRecently = false

    private val _cats = MutableLiveData<List<Pair<Long, CatData>>>()
    val cats: LiveData<List<Pair<Long, CatData>>>
        get() = _cats

    private val _actionModeState = MutableLiveData<Boolean>()
    val actionModeState: LiveData<Boolean>
        get() = _actionModeState

    private val _actionModeTitle = MutableLiveData<String>()
    val actionModeTitle: LiveData<String>
        get() = _actionModeTitle

    private val _clearSelectionEvent = MutableLiveData<Event<Unit>>()
    val clearSelectionEvent: LiveData<Event<Unit>>
        get() = _clearSelectionEvent

    private val _removeRequestedEvent = MutableLiveData<Event<List<String>>>()
    val removeRequestedEvent: LiveData<Event<List<String>>>
        get() = _removeRequestedEvent

    val invalidId = Long2StringIdMapper.INVALID_ID

    var catsWithStringId: List<Pair<String, CatData>>
        get() {
            val mapped = _cats.value?.mapNotNull {
                idMapper.stringIdFrom(it.first)?.let { id -> Pair(id, it.second) }
            }
            return mapped ?: emptyList()
        }
        set(value) {
            _cats.value = value.map { Pair(idMapper.longIdFrom(it.first), it.second) }
        }

    val allSelectionIds: List<Long>
        get() = cats.value?.map { it.first} ?: emptyList()

    fun stringCatIdFrom(longId: Long) = idMapper.stringIdFrom(longId)

    fun handleOnItemClick(): Boolean {
        if(actionModeState.value == true)
            return false

        if(wasClickedRecently)
            return false

        wasClickedRecently = true

        val debounceTimeout = 500L
        Handler(Looper.getMainLooper()).postDelayed({ wasClickedRecently = false }, debounceTimeout)

        return true
    }

    fun clearSelection() {
        _clearSelectionEvent.value = Event(Unit)
        selection = emptyList()
    }

    fun onActionModeStarted() {
        changeActionModeState(true)
    }

    fun onActionModeFinished() {
        changeActionModeState(false)
        clearSelection()
    }

    private fun changeActionModeState(value: Boolean) {
        if(_actionModeState.value != value)
            _actionModeState.value = value
    }

    fun onSelectionChanged(selection: List<Long>) {
        this.selection = selection
        val isSelected = !selection.isEmpty()

        if(isSelected) {
            changeActionModeState(true)
            _actionModeTitle.value = selection.size.toString()
        } else
            changeActionModeState(false)
    }

    fun onRemoveConfirmed() {
        _removeRequestedEvent.value = Event(selection.mapNotNull { idMapper.stringIdFrom(it) })
        changeActionModeState(false)
    }
}
