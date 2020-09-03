package com.sergsave.purryourcat.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import com.sergsave.purryourcat.models.CatData

class CatDataViewModel(val data: LiveData<CatData>) : ViewModel() {}

class CatDataViewModelFactory(private val data: LiveData<CatData>): Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(CatDataViewModel::class.java)) {
            CatDataViewModel(data) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}