package com.sergsave.purryourcat.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import com.sergsave.purryourcat.data.CatDataRepo
import com.sergsave.purryourcat.models.CatData

class CatDataViewModel(private var catRepoId: String? = null) : ViewModel() {
    private val _data = MutableLiveData<CatData>()
    private lateinit var repo: CatDataRepo

    init {
        CatDataRepo.instance?.let {
            repo = it
        } ?: run {
            assert(false) { "Must be init" }
        }

        _data.value = repo.read().value?.get(catRepoId)
    }

    val data : LiveData<CatData>
        get() = _data

    fun change(cat: CatData) {
        if(catRepoId == null)
            catRepoId = repo.add(CatData())

        _data.value = cat
        catRepoId?.let { repo.update(it, cat) }
    }

    override fun onCleared() {
        super.onCleared()
    }
}

class CatDataViewModelFactory(private val catRepoId: String?): Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(CatDataViewModel::class.java)) {
            CatDataViewModel(catRepoId) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}