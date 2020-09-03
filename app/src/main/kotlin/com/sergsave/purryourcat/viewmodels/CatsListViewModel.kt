package com.sergsave.purryourcat.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider.Factory
import com.sergsave.purryourcat.models.extractContent
import com.sergsave.purryourcat.content.ContentRepository
import com.sergsave.purryourcat.data.CatDataRepository
import com.sergsave.purryourcat.models.CatData
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.kotlin.Singles

class CatsListViewModel(
    private var catDataRepository: CatDataRepository,
    private var contentRepository: ContentRepository
): ViewModel() {

    private val _data = MutableLiveData<Map<String, CatData>>()
    private var disposable: Disposable? = null

    init {
        cleanUpUnusedContent()
        disposable = catDataRepository.read().subscribe{ _data.value = it }
    }

    override fun onCleared() {
        disposable?.dispose()
        super.onCleared()
    }

    val data: LiveData<Map<String, CatData>>
        get() = _data

    fun remove(id: String) {
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
    private var contentRepository: ContentRepository
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