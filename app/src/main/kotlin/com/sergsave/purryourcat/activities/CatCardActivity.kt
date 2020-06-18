package com.sergsave.purryourcat.activities

import android.content.Intent
import android.os.Bundle
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.fragments.CatFormFragment
import com.sergsave.purryourcat.fragments.PurringFragment
import com.sergsave.purryourcat.helpers.Constants
import com.sergsave.purryourcat.helpers.SimpleAlertDialog
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.viewmodels.*
import kotlinx.android.synthetic.main.activity_cat_card.*

class CatCardActivity : AppCompatActivity() {

    private enum class PageType(val fragmentTag: String) {
        ADD_NEW("AddNew"),
        EDIT("Edit"),
        PURRING("Purring")
    }

    private lateinit var currentPage : PageType
    private lateinit var viewModel : CatDataViewModel

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cat_card)

        // Use common toolbar for pages to avoid transition blinking
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val catId = getCatId(intent)
        initViewModel(catId)

        if(savedInstanceState == null) {
            if(getTransitionName(intent) != null)
                postponeEnterTransition()

            if(catId == null)
                switchToPage(PageType.ADD_NEW)
            else
                switchToPage(PageType.PURRING)

            return
        }

        val page = savedInstanceState.getString(BUNDLE_KEY_CURRENT_PAGE)
        if(page != null)
            switchToPage(PageType.valueOf(page))
    }

    private fun initViewModel(catId: String?) {
        viewModel = ViewModelProvider(this, CatDataViewModelFactory(catId))
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
                    ?: CatFormFragment.newInstance(mode, viewModel.data.value ?: CatData())

                _fragment.apply { initFragment(this) }
            }
            PageType.PURRING -> {
                val transition = getTransitionName(intent)

                val _fragment = existingFragment as PurringFragment?
                    ?: PurringFragment.newInstance(transition)

                _fragment.apply { initFragment(this) }
            }
        }

        setCurrentFragment(fragment, type.fragmentTag)
        currentPage = type
    }

    private fun initFragment(catForm: CatFormFragment) {
        val applyListener = object : CatFormFragment.OnApplyListener {
            override fun onApply() {
                val data = catForm.catDataChange.to

                if (data != null && data.isValid()) {
                    viewModel.change(data)
                    switchToPage(PageType.PURRING)
                }
                else
                    showApplyAlertDialog()
            }
        }

        catForm.setOnApplyListener(applyListener)
    }

    private fun initFragment(purring: PurringFragment) {
        purring.setOnEditRequestedListener(object : PurringFragment.OnEditRequestedListener {
            override fun onEditRequested() = switchToPage(PageType.EDIT)
        })
        purring.setOnImageLoadedListener(object : PurringFragment.OnImageLoadedListener {
            override fun onImageLoaded() = supportStartPostponedEnterTransition()
        })
    }

    override fun onBackPressed() {
        val finalize = {
            if (currentPage == PageType.EDIT)
                switchToPage(PageType.PURRING)
            else
                super.onBackPressed()
        }

        val formFragment = supportFragmentManager.findFragmentById(R.id.container) as?
                CatFormFragment

        val isFormActive = formFragment != null && formFragment.isVisible
        val change = formFragment?.catDataChange

        if(isFormActive && change?.to != change?.from)
            showBackAlertDialog({ finalize() })
        else
            finalize()
    }

    private fun CatData.isValid() = name != null && purrAudioUri != null && photoUri != null

    private fun showBackAlertDialog(finishCallback: () -> Unit) {
        val positiveText = resources.getString(R.string.discard)
        val negativeText = resources.getString(R.string._continue)
        val message = resources.getString(R.string.changes_not_saved)

        val buttons = mapOf(
            SimpleAlertDialog.Button.POSITIVE to positiveText,
            SimpleAlertDialog.Button.NEGATIVE to negativeText)

        val dialog = SimpleAlertDialog(this, message, buttons)

        dialog.listener = object: SimpleAlertDialog.Listener {
            override fun onDialogNegativeClick(dialog: DialogFragment?) { }
            override fun onDialogPositiveClick(dialog: DialogFragment?) {
                finishCallback()
            }
        }

        dialog.show(supportFragmentManager, DIALOG_ID_BACK)
    }

    private fun showApplyAlertDialog() {
        val dialog = SimpleAlertDialog(this, resources.getString(R.string.fill_the_form),
            mapOf(SimpleAlertDialog.Button.POSITIVE to resources.getString(R.string.ok)))

        dialog.show(supportFragmentManager, DIALOG_ID_APPLY)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(BUNDLE_KEY_CURRENT_PAGE, currentPage.toString())
    }

    companion object {
        private val BUNDLE_KEY_CURRENT_PAGE = "CurrentPage"

        private val DIALOG_ID_BACK = "DialogBack"
        private val DIALOG_ID_APPLY = "DialogApply"
    }
}
