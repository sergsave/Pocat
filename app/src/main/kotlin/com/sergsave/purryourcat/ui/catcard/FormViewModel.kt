package com.sergsave.purryourcat.ui.catcard

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.persistent.CatDataRepository
import com.sergsave.purryourcat.content.ContentRepository
import com.sergsave.purryourcat.helpers.Event
import com.sergsave.purryourcat.helpers.DisposableViewModel
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.models.Card

class FormViewModel(
    private val catDataRepository: CatDataRepository,
    private val contentRepository: ContentRepository,
    private val card: Card? = null
) : DisposableViewModel() {

    private var backup: CatData? = null

    private val _name = MutableLiveData<String>()
    val name: LiveData<String>
        get() = _name

    private val _photoUri = MutableLiveData<Uri>()
    val photoUri: LiveData<Uri>
        get() = _photoUri

    private val _audioUri = MutableLiveData<Uri>()
    val audioUri: LiveData<Uri>
        get() = _audioUri

    enum class SoundButtonType { ADD_SOUND, SOUND_IS_ADDED }

    val soundButtonType: LiveData<SoundButtonType> = Transformations.map(_audioUri) { uri ->
        if(uri == null)
            SoundButtonType.ADD_SOUND
        else
            SoundButtonType.SOUND_IS_ADDED
    }

    private val _unsavedChangesMessageEvent = MutableLiveData<Event<Unit>>()
    val unsavedChangesMessageEvent: LiveData<Event<Unit>>
        get() = _unsavedChangesMessageEvent

    private val _notValidDataMessageEvent = MutableLiveData<Event<Unit>>()
    val notValidDataMessageEvent: LiveData<Event<Unit>>
        get() = _notValidDataMessageEvent

    private val _audioChangedMessageEvent = MutableLiveData<Event<Unit>>()
    val audioChangedMessageEvent: LiveData<Event<Unit>>
        get() = _audioChangedMessageEvent

    private val _openCardEvent = MutableLiveData<Event<Card>>()
    val openCardEvent: LiveData<Event<Card>>
        get() = _openCardEvent

    init {
        if(card != null) assert (card.persistentId != null)

        val data = card?.data ?: CatData()
        updateData(data)
        backup = data
    }

    private fun currentData() = CatData(_name.value, _photoUri.value, _audioUri.value)

    val toolbarTitleStringId: Int
        get() {
            return if(card == null) R.string.add_new_cat else R.string.edit_cat
        }

    fun changeName(name: String) {
        // Empty text is null for correct comparing data with backup
        _name.value = if(name.isNotEmpty()) name else null
    }

    fun changePhoto(uri: Uri) {
        if(uri != _photoUri.value) {
            addDisposable(contentRepository.addImage(uri).subscribe(
                { newUri -> _photoUri.value = newUri },
                { Log.e(TAG, "Adding failed", it) }
            ))
        }
    }

    fun changeAudio(uri: Uri) {
        if(_audioUri.value != null)
            _audioChangedMessageEvent.value = Event(Unit)

        if(uri != _audioUri.value) {
            addDisposable(contentRepository.addAudio(uri).subscribe(
                { newUri -> _audioUri.value = newUri },
                { Log.e(TAG, "Adding failed", it) }
            ))
        }
    }

    fun onApplyPressed() {
        if (isCurrentDataValid().not()) {
            _notValidDataMessageEvent.value = Event(Unit)
            return
        }

        syncDataWithRepo()
    }

    fun handleBackPressed(): Boolean {
        if(wereChangesAfterBackup().not())
            return true

        _unsavedChangesMessageEvent.value = Event(Unit)
        return false
    }

    fun onDiscardChanges() {
        restoreFromBackup()
    }

    private fun syncDataWithRepo() {
        val data = currentData()
        val currentCard = card?.copy(data = data)
        val id = currentCard?.persistentId

        val message = "Sync failed"

        if(currentCard != null && id != null) {
            addDisposable(catDataRepository.update(id, currentCard.data).subscribe(
                { _openCardEvent.value = Event(currentCard) },
                { Log.e(TAG, message, it) }
            ))
            return
        }

        addDisposable(catDataRepository.add(data).subscribe(
            { newId -> _openCardEvent.value = Event(Card(newId, data, true, true)) },
            { Log.e(TAG, message, it) }
        ))
    }

    private fun updateData(data: CatData?) {
        _name.value = data?.name
        _photoUri.value = data?.photoUri
        _audioUri.value = data?.purrAudioUri
    }

    private fun isCurrentDataValid(): Boolean {
        return _name.value != null && _photoUri.value != null && _audioUri.value != null
    }

    private fun restoreFromBackup() {
        backup?.let{ updateData(it) }
    }

    private fun wereChangesAfterBackup(): Boolean {
        return currentData() != backup
    }

    companion object {
        private const val TAG = "FormViewModel"
    }
}
