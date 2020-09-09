package com.sergsave.purryourcat.ui.catslist

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sergsave.purryourcat.models.extractContent
import com.sergsave.purryourcat.content.ContentRepository
import com.sergsave.purryourcat.data.CatDataRepository
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.sharing.SharingManager
import com.sergsave.purryourcat.helpers.Event
import com.sergsave.purryourcat.helpers.DisposableViewModel
import com.sergsave.purryourcat.helpers.Long2StringIdMapper
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.kotlin.Singles

class CatsListViewModel(
    private val catDataRepository: CatDataRepository,
    private val contentRepository: ContentRepository,
    private val sharingManager: SharingManager
): DisposableViewModel() {
    private var selection = listOf<Long>()
    private val idMapper = Long2StringIdMapper()

    private val _cats = MutableLiveData<List<Pair<Long, CatData>>>()
    val cats: LiveData<List<Pair<Long, CatData>>>
        get() = _cats

    private val _actionModeState = MutableLiveData<Boolean>()
    val actionModeState: LiveData<Boolean>
        get() = _actionModeState

    private val _finishActionModeEvent = MutableLiveData<Event<Unit>>()
    val finishActionModeEvent: LiveData<Event<Unit>>
        get() = _finishActionModeEvent

    private val _actionModeTitle = MutableLiveData<String>()
    val actionModeTitle: LiveData<String>
        get() = _actionModeTitle

    private val _clearSelectionEvent = MutableLiveData<Event<Unit>>()
    val clearSelectionEvent: LiveData<Event<Unit>>
        get() = _clearSelectionEvent

    init {
        // Cleanup not in Application, because Application is created only after device reload
        sharingManager.cleanup()
        cleanUpUnusedContent()

        addDisposable(catDataRepository.read().subscribe{
            _cats.value = it.mapKeys{ (k, _) -> idMapper.longIdFrom(k) }.toList()
        })
    }

    val invalidId = Long2StringIdMapper.INVALID_ID

    fun stringCatIdFrom(longId: Long) = idMapper.stringIdFrom(longId)

    fun handleOnItemClick(): Boolean = actionModeState.value != true

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
        // TODO: FP way
        catIds.forEach {
            addDisposable(catDataRepository.remove(it).subscribe{ _ -> cleanUpUnusedContent() })
        }
    }

    private fun cleanUpUnusedContent() {
        val disposable = Observables.zip(catDataRepository.read(), contentRepository.read())
            .take(1)
            .subscribe { (data, content) ->
                val usedContent = data.flatMap { (_, cat) -> cat.extractContent() }

                val unusedContent = content - usedContent
                unusedContent.forEach {
                    addDisposable(contentRepository.remove(it).subscribe{ _ ->})
                }
            }
        addDisposable(disposable)
    }
}

