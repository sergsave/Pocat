package com.sergsave.purryourcat.ui.testing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.sergsave.purryourcat.content.ContentRepository
import com.sergsave.purryourcat.persistent.CatDataRepository
import com.sergsave.purryourcat.helpers.DisposableViewModel
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.R
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles

class TestingViewModel(
    private val catDataRepository: CatDataRepository,
    private val contentRepository: ContentRepository
): DisposableViewModel() {

    fun onCopyAllClicked() {
        if(isCopyInProgress.value == true)
            return

        isCopyInProgress.value = true

        val createCatCopyObservable = { cat: CatData ->
            Singles.zip(
                contentRepository.addImage(cat.photoUri),
                contentRepository.addAudio(cat.purrAudioUri)
            )
                .map { cat.copy(photoUri = it.first, purrAudioUri = it.second) }
        }

        val disposable = catDataRepository.read()
            .take(1)
            .flatMapIterable { cats -> cats.map { it.value.data } }
            .concatMapSingle { cat ->
                createCatCopyObservable(cat)
                    .flatMap { catCopy -> catDataRepository.add(catCopy) }
                    .onErrorResumeNext(Single.just(""))
            }
            .subscribe(
                { },
                { isCopyInProgress.value = false },
                { isCopyInProgress.value = false }
            )

        addDisposable(disposable)
    }

    private val isCopyInProgress = MutableLiveData<Boolean>(false)

    data class CopyButtonState(val stringId: Int, val isEnabled: Boolean)
    val copyButtonStringId: LiveData<CopyButtonState> = Transformations.map(isCopyInProgress, {
        val stringId = if(it) R.string.copy_in_progress else R.string.copy_cats
        CopyButtonState(stringId, it.not())
    })
}

