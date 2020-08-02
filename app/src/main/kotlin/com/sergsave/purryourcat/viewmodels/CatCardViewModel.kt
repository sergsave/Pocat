package com.sergsave.purryourcat.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import com.sergsave.purryourcat.data.CatDataRepo
import com.sergsave.purryourcat.models.combineContent
import com.sergsave.purryourcat.content.ContentRepo
import com.sergsave.purryourcat.models.CatData

class CatCardViewModel(private var catRepoId: String? = null) : ViewModel() {
    private val _data = MutableLiveData<CatData>()
    private lateinit var repo: CatDataRepo

    init {
        CatDataRepo.instance?.let {
            repo = it
        } ?: run {
            assert(false) { "Must be init" }
        }

        catRepoId?.let { _data.value = repo.read().value?.get(it) }
    }

    val data : LiveData<CatData>
        get() = _data

    fun syncDataWithRepo() {
        _data.value?.let { catData ->
            val id = catRepoId
            if(id == null)
                catRepoId = repo.add(catData)
            else
                repo.update(id, catData)
        }
    }

    fun isDataSyncWithRepo(): Boolean = catRepoId != null

    fun change(data: CatData) {
        val prevData = _data.value ?: CatData()

        val dataWithUpdatedContent = data.combineContent(prevData, { new, old ->
            if(new != old) {
                val updated = ContentRepo.instance?.add(new)
                ContentRepo.instance?.remove(old)
                updated
            } else
                old
        })

        _data.value = dataWithUpdatedContent
    }

    override fun onCleared() {
        super.onCleared()
    }
}

class CatCardViewModelFactory(private val catRepoId: String?): Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(CatCardViewModel::class.java)) {
            CatCardViewModel(catRepoId) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}