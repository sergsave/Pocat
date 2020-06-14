package com.sergsave.purryourcat.activities

import android.content.Intent
import android.os.Bundle
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.data.CatDataRepo
import com.sergsave.purryourcat.fragments.CatFormFragment
import com.sergsave.purryourcat.fragments.PurringFragment
import com.sergsave.purryourcat.helpers.Constants
import com.sergsave.purryourcat.models.CatData
import kotlinx.android.synthetic.main.activity_cat_card.*

class CatDataViewModel(catRepoId: String? = null) : ViewModel() {
    private val _data = MutableLiveData<CatData>()
    private lateinit var repo: CatDataRepo
    private val catRepoId: String

    init {
        CatDataRepo.instance?.let {
            repo = it
        } ?: run {
            assert(false) { "Must be init" }
        }

        this.catRepoId = catRepoId ?: repo.add(CatData())
        _data.value = repo.read().value?.get(this.catRepoId)
    }

    val data : LiveData<CatData>
        get() = _data

    fun change(cat: CatData) {
        _data.value = cat
        repo.update(catRepoId, cat)
    }

    override fun onCleared() {
        super.onCleared()
    }
}

class CatCardActivity : AppCompatActivity() {

    private enum class PageType(val fragmentTag: String) {
        ADD_NEW("AddNew"),
        EDIT("Edit"),
        PURRING("Purring")
    }

    private lateinit var currentPage : PageType

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cat_card)

        // Use common toolbar for pages to avoid transition blinking
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        if(savedInstanceState == null) {
            if(getTransitionName(intent) != null)
                postponeEnterTransition()

            val catId = getCatId(intent)
            initViewModel(catId)

            if(catId == null)
                switchToPage(PageType.ADD_NEW)
            else
                switchToPage(PageType.PURRING)

            return
        }

        val page = savedInstanceState.getString(CURRENT_PAGE_BUNDLE_KEY)
        if(page != null)
            switchToPage(PageType.valueOf(page))
    }

    private fun initViewModel(catId: String?) {
        // Get instance for creation
        ViewModelProvider(this, CatDataViewModelFactory(catId))
            .get(CatDataViewModel::class.java)
    }

    private fun getTransitionName(intent: Intent) : String? {
        return intent.getStringExtra(Constants.SHARED_TRANSITION_NAME_INTENT_KEY)
    }

    private fun getCatId(intent: Intent) : String? {
        return intent.getStringExtra(Constants.CAT_ID_INTENT_KEY)
    }

    private fun setCurrentFragment(fragment: Fragment, tag: String? = null)
    {
        if(supportFragmentManager.findFragmentByTag(tag) != null)
            return

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment, tag)
            .commit()
    }

    private fun switchToPage(type: PageType) {
        val existingFragment = supportFragmentManager.findFragmentByTag(type.fragmentTag)

        val fragment : Fragment = when(type) {
            PageType.ADD_NEW, PageType.EDIT -> {
                val mode =
                    if(type == PageType.ADD_NEW) CatFormFragment.Mode.CREATE
                    else CatFormFragment.Mode.EDIT

                val _fragment = existingFragment as CatFormFragment?
                    ?: CatFormFragment.newInstance(mode)

                _fragment.apply {
                    setOnApplyListener(object : CatFormFragment.OnApplyListener {
                        override fun onApply() = switchToPage(PageType.PURRING)
                    })
                }
            }
            PageType.PURRING -> {
                val transition = getTransitionName(intent)

                val _fragment = existingFragment as PurringFragment?
                    ?: PurringFragment.newInstance(transition)

                _fragment.apply {
                    setOnEditRequestedListener(object : PurringFragment.OnEditRequestedListener {
                        override fun onEditRequested() = switchToPage(PageType.EDIT)
                    })
                    setOnImageLoadedListener(object : PurringFragment.OnImageLoadedListener {
                        override fun onImageLoaded() = supportStartPostponedEnterTransition()
                    })
                }
            }
        }

        setCurrentFragment(fragment, type.fragmentTag)
        currentPage = type
    }

    override fun onBackPressed() {
        if (currentPage == PageType.EDIT) {
            switchToPage(PageType.PURRING)
        }
        else
            super.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CURRENT_PAGE_BUNDLE_KEY, currentPage.toString())
    }

    class CatDataViewModelFactory(private val catRepoId: String?): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(CatDataViewModel::class.java)) {
                CatDataViewModel(catRepoId) as T
            } else {
                throw IllegalArgumentException("ViewModel Not Found")
            }
        }
    }

    companion object {
        private val CURRENT_PAGE_BUNDLE_KEY = "CurrentPage"
    }
}
