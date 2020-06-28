package com.sergsave.purryourcat.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import com.sergsave.purryourcat.data.CatDataRepo
import com.sergsave.purryourcat.models.combineContent
import com.sergsave.purryourcat.content.ContentRepo
import com.sergsave.purryourcat.models.CatData

class CatCardViewModel(catData: CatData? = null, private var catRepoId: String? = null) : ViewModel() {
    private val _data = MutableLiveData<CatData>()
    private lateinit var repo: CatDataRepo

    init {
        CatDataRepo.instance?.let {
            repo = it
        } ?: run {
            assert(false) { "Must be init" }
        }
        if(catData != null && catRepoId != null) assert(false) { "Wrong init" }

        if(catData != null)
            _data.value = catData
        else
            _data.value = repo.read().value?.get(catRepoId)
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

class CatCardViewModelFactory(): Factory {

    private var catData: CatData? = null
    private var id: String? = null

    constructor(catData: CatData) : this() {
        this.catData = catData
    }

    constructor(catRepoId: String) : this() {
        id = catRepoId
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(CatCardViewModel::class.java)) {
            CatCardViewModel(catData, id) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}