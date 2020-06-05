package com.github.sergsave.purr_your_cat.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.github.sergsave.purr_your_cat.R
import com.github.sergsave.purr_your_cat.fragments.CatProfileFragment
import com.github.sergsave.purr_your_cat.fragments.PurringFragment
import com.github.sergsave.purr_your_cat.helpers.Constants
import com.github.sergsave.purr_your_cat.models.CatData
import com.github.sergsave.purr_your_cat.Singleton
import kotlinx.android.synthetic.main.activity_cat_card.*

class CatCardActivity : AppCompatActivity() {

    private enum class PageType(val fragmentTag: String) {
        ADD_NEW("AddNew"),
        EDIT("Edit"),
        PURRING("Purring")
    }

    private var currentPage : PageType? = null

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
            if(getSharedElementTransitionName() != null)
                postponeEnterTransition()

            if(getCatData() == null)
                switchToPage(PageType.ADD_NEW)
            else
                switchToPage(PageType.PURRING)
        }
        else {
            val page = savedInstanceState.getString(CURRENT_PAGE_BUNDLE_KEY)
            if(page != null)
                switchToPage(PageType.valueOf(page))
        }
    }

    // TODO? Save to bundle
    private fun getSharedElementTransitionName() : String? {
        return intent.getStringExtra(Constants.SHARED_TRANSITION_NAME_INTENT_KEY)
    }

    // TODO? ModelView or singleton with LiveData
    private fun getCatData() : CatData? {
        Singleton.catData = intent.getParcelableExtra(Constants.CAT_DATA_INTENT_KEY) as CatData?
        return Singleton.catData
    }

    private fun setCurrentFragment(fragment: Fragment, tag: String? = null)
    {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment, tag)
            .commit()
    }

    private fun switchToPage(type: PageType) {

        val existingFragment = supportFragmentManager.findFragmentByTag(type.fragmentTag)

        val fragment : Fragment = when(type) {
            // Use same fragment, but different tags
            PageType.ADD_NEW, PageType.EDIT -> {
                val fg = existingFragment as CatProfileFragment?
                    ?: CatProfileFragment.newInstance(Singleton.catData)
                fg.setOnApplyListener(object: CatProfileFragment.OnApplyListener {
                    override fun onApply() = switchToPage(PageType.PURRING)
                })
                fg
            }
            PageType.PURRING -> {
                val fg = existingFragment as PurringFragment?
                    ?: PurringFragment.newInstance(getSharedElementTransitionName())
                fg.setOnEditRequestedListener(object: PurringFragment.OnEditRequestedListener {
                    override fun onEditRequested() = switchToPage(PageType.EDIT)
                })
                fg
            }
        }

        // Just add listeners for existing
        if(existingFragment == null)
            setCurrentFragment(fragment, type.fragmentTag)

        currentPage = type
    }

    override fun onBackPressed() {
        val editPageFragment = supportFragmentManager.findFragmentByTag(PageType.EDIT.fragmentTag)

        if (editPageFragment != null && editPageFragment.isVisible()) {
            switchToPage(PageType.PURRING)
        }
        else
            super.onBackPressed()
    }

    override fun finishAfterTransition() {
        val intent = Intent()
        intent.putExtra(Constants.CAT_DATA_INTENT_KEY, Singleton.catData)
        setResult(Activity.RESULT_OK, intent)

        super.finishAfterTransition()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CURRENT_PAGE_BUNDLE_KEY, currentPage.toString())
    }

    companion object {
        private val CURRENT_PAGE_BUNDLE_KEY = "CurrentPage"
    }
}
