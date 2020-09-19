package com.sergsave.purryourcat.ui.catslist

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sergsave.purryourcat.content.ContentRepository
import com.sergsave.purryourcat.data.CatDataRepository
import com.sergsave.purryourcat.helpers.DisposableViewModel
import com.sergsave.purryourcat.helpers.Event
import com.sergsave.purryourcat.helpers.Long2StringIdMapper
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.models.extractContent
import com.sergsave.purryourcat.sharing.SharingManager
import io.reactivex.Observable
import io.reactivex.rxkotlin.Flowables

class CatsListViewModel(
    private val catDataRepository: CatDataRepository,
    private val contentRepository: ContentRepository,
    sharingManager: SharingManager
): DisposableViewModel() {
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

    init {
        // Cleanup not in Application, because Application is created only after device reload
        addDisposable(sharingManager.cleanup().subscribe())
        cleanUpUnusedContent()

        addDisposable(catDataRepository.read().subscribe { catMap ->
            val listOfPairs = catMap.mapKeys{ (k, _) -> idMapper.longIdFrom(k) }.toList()
            val sortedByTime = listOfPairs.sortedByDescending { it.second.timeOfCreateMillis }
            _cats.value = sortedByTime.map { (id, timed) -> Pair(id, timed.data) }
        })
    }

    val invalidId = Long2StringIdMapper.INVALID_ID

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

    fun onActionModeStarted() {
        changeActionModeState(true)
    }

    fun onActionModeFinished() {
        changeActionModeState(false)

        _clearSelectionEvent.value = Event(Unit)
        selection = emptyList()
    }

    private fun changeActionModeState(value: Boolean) {
        if(_actionModeState.value != value)
            _actionModeState.value = value
    }

    fun onRemovePressed() {
        remove(selection.mapNotNull { idMapper.stringIdFrom(it) })
    }

    fun onSelectionChanged(selection: List<Long>) {
        this.selection = selection
        val isSelected = selection.isEmpty().not()

        if(isSelected) {
            changeActionModeState(true)
            _actionModeTitle.value = selection.size.toString()
        } else
            changeActionModeState(false)
    }

    private fun remove(catIds: List<String>) {
        addDisposable(
            Observable.fromIterable(catIds)
                .concatMapCompletable { catDataRepository.remove(it) }
                .doOnComplete{ cleanUpUnusedContent() }
                .subscribe{}
        )
    }

    private fun cleanUpUnusedContent() {
        val disposable = Flowables.zip(catDataRepository.read(), contentRepository.read())
            .take(1)
            .subscribe { (data, content) ->
                val usedContent = data.flatMap { (_, cat) -> cat.data.extractContent() }
                val unusedContent = content - usedContent

                addDisposable(
                    Observable.fromIterable(unusedContent)
                    .concatMapCompletable { contentRepository.remove(it) }
                    .subscribe{})
            }
        addDisposable(disposable)
    }
}

