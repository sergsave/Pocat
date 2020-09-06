package com.sergsave.purryourcat.ui.catslist

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider.Factory
import com.sergsave.purryourcat.models.extractContent
import com.sergsave.purryourcat.content.ContentRepository
import com.sergsave.purryourcat.data.CatDataRepository
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.sharing.SharingManager
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.kotlin.Singles

class CatsListViewModel(
    private var catDataRepository: CatDataRepository,
    private var contentRepository: ContentRepository,
    private var sharingManager: SharingManager
): ViewModel() {

    private var disposable: CompositeDisposable? = null
    private var onActionMode = false

    init {
        // Cleanup not in Application, because Application create is created after device reload
        sharingManager.cleanup()
        cleanUpUnusedContent()

        disposable.add(catDataRepository.read().subscribe{ _data.value = it })
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }

    private val _cats = MutableLiveData<Map<String, CatData>>()
    val cats: LiveData<Map<String, CatData>>
        get() = _cats

    private val _selection = MutableLiveData<List<String>>()
    val selection: LiveData<List<String>>
        get() = _selection

    private val _onItemClickedEvent = MutableLiveData<Event<String>>()
    val onItemClickedEvent: LiveData<Event<String>>
        get() = _onItemClickedEvent

    private val _startActionModeEvent = MutableLiveData<Event<Unit>>()
    val startActionModeEvent: LiveData<Event<Unit>>
        get() = _startActionModeEvent

    private val _finishActionModeEvent = MutableLiveData<Event<Unit>>()
    val finishActionModeEvent: LiveData<Event<Unit>>
        get() = _finishActionModeEvent

    private val _actionModeTitle = MutableLiveData<String>()
    val actionModeTitle: LiveData<String>
        get() = _actionModeTitle

    fun onItemClicked() {
        if(onActionMode)
            return

    }

    fun onActionModeStarted() {
        onActionMode = true
    }

    fun onActionModeFinished() {
        onActionMode = false
        selection.value = emptyList()
    }

    fun onRemovePressed() {

    }

    fun changeSelection(selection: List<String>) {
        val isSelected = selection.isEmpty().not()

        if(isSelected && onActionMode.not())
            _startActionModeEvent.value = Event(Unit)

        if(isSelected)
            _actionModeTitle.value = selection.size.toString()

        if(isSelected.not() && onActionMode)
            _finishActionModeEvent.value = Event(Unit)
    }

    private fun remove(catIds: List<String>) {
        // TODO: FP way
        catDataRepository.remove(id).subscribe{ _ -> cleanUpUnusedContent() }
    }

    private fun cleanUpUnusedContent() {
        Observables.zip(catDataRepository.read(), contentRepository.read())
            .take(1)
            .subscribe { (data, content) ->
                val usedContent = data.flatMap { (_, cat) -> cat.extractContent() }

                val unusedContent = content - usedContent
                unusedContent.forEach { contentRepository.remove(it).subscribe{ _ ->} }
            }
    }
}

class CatsListViewModelFactory(
    private var catDataRepository: CatDataRepository,
    private var contentRepository: ContentRepository,
    private val sharingManager: SharingManager
): Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(CatsListViewModel::class.java)) {
            CatsListViewModel(catDataRepository, contentRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}