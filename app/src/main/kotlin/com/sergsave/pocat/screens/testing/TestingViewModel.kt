package com.sergsave.pocat.screens.testing

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.sergsave.pocat.content.ContentRepository
import com.sergsave.pocat.persistent.CatDataRepository
import com.sergsave.pocat.helpers.DisposableViewModel
import com.sergsave.pocat.models.CatData
import com.sergsave.pocat.R
import com.sergsave.pocat.preference.PreferenceManager
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import java.lang.IllegalStateException

class TestingViewModel(
    private val catDataRepository: CatDataRepository,
    private val contentRepository: ContentRepository,
    private val preferences: PreferenceManager
): DisposableViewModel() {

    private fun createCatCopyObservable(cat: CatData): Single<CatData> {
        val error = IllegalStateException("Bad cat!")

        if (cat.photoUri == null || cat.purrAudioUri == null)
            return Single.error(error)

        return Singles.zip(
            contentRepository.addImage(cat.photoUri).onErrorReturnItem(Uri.EMPTY),
            contentRepository.addAudio(cat.purrAudioUri).onErrorReturnItem(Uri.EMPTY)
        )
            .map { (photo, audio) ->
                if (photo == Uri.EMPTY || audio == Uri.EMPTY)
                    throw error
                else
                    cat.copy(photoUri = photo, purrAudioUri = audio)
            }
    }
    fun onCopyAllClicked() {
        if(isCopyInProgress.value == true)
            return

        isCopyInProgress.value = true

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
        val stringId = if(it) R.string.testing_copy_in_progress else R.string.testing_copy_cats
        CopyButtonState(stringId, !it)
    })

    fun onResetTutorialClicked() {
        preferences.isPurringTutorialAchieved = false
    }
}

