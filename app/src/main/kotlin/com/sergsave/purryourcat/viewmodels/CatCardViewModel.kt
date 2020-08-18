package com.sergsave.purryourcat.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import com.sergsave.purryourcat.repositories.data.CatDataRepository
import com.sergsave.purryourcat.repositories.content.ContentRepository
import com.sergsave.purryourcat.models.combineContent
import com.sergsave.purryourcat.models.CatData

class CatCardViewModel(
    private var catDataRepository: CatDataRepository,
    private var contentRepository: ContentRepository,
    private var catId: String? = null
) : ViewModel() {
    private val _data = MutableLiveData<CatData>()

    init {
        catId?.let { _data.value = catDataRepository.read().value?.get(it) }
    }

    val data : LiveData<CatData>
        get() = _data

    fun syncDataWithRepo() {
        _data.value?.let { catData ->
            val id = catId
            if(id == null)
                catId = catDataRepository.add(catData)
            else
                catDataRepository.update(id, catData)
        }
    }

    fun isDataSyncWithRepo(): Boolean = catId != null

    fun change(data: CatData) {
        val prevData = _data.value ?: CatData()

        val dataWithUpdatedContent = data.combineContent(prevData) { new, old ->
            if(new != old) {
                val updated = contentRepository.add(new)
                contentRepository.remove(old)
                updated
            } else
                old
        }

        _data.value = dataWithUpdatedContent
    }
}

class CatCardViewModelFactory(
    private var catDataRepository: CatDataRepository,
    private var contentRepository: ContentRepository,
    private val catId: String?
): Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(CatCardViewModel::class.java)) {
            CatCardViewModel(catDataRepository, contentRepository, catId) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}