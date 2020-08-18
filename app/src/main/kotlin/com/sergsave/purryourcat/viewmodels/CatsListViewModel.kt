package com.sergsave.purryourcat.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import com.sergsave.purryourcat.models.extractContent
import com.sergsave.purryourcat.repositories.content.ContentRepository
import com.sergsave.purryourcat.repositories.data.CatDataRepository

class CatsListViewModel(
    private var catDataRepository: CatDataRepository,
    private var contentRepository: ContentRepository
): ViewModel() {

    init {
        cleanUpUnusedContent()
    }

    fun read() = catDataRepository.read()

    fun remove(id: String) {
        catDataRepository.remove(id)
        cleanUpUnusedContent()
    }

    private fun cleanUpUnusedContent() {
        val usedContent = ArrayList<Uri>()
        catDataRepository.read().value?.forEach{(_, cat) ->
            usedContent.addAll(cat.extractContent())
        }
        val allContent = contentRepository.read().value
        allContent?.let { all -> (all - usedContent).forEach{
            contentRepository.remove(it)
        }}
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