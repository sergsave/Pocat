package com.sergsave.purryourcat.activities

import android.content.Intent
import android.os.Bundle
import android.transition.Transition
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import com.sergsave.purryourcat.MyApplication
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.fragments.*
import com.sergsave.purryourcat.helpers.Constants
import com.sergsave.purryourcat.helpers.SimpleAlertDialog
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.viewmodels.CatCardViewModel
import kotlinx.android.synthetic.main.activity_cat_card.*

class CatCardActivity : AppCompatActivity() {

    private enum class PageType(val tag: String) {
        ADD_NEW("AddNew"),
        EDIT("Edit"),
        SHOW_SAVED("ShowSaved"),
        SHOW_UNSAVED("ShowUnsaved"),
        LOADING("Loading")
    }

    private var currentPage : PageType? = null
    private lateinit var viewModel : CatCardViewModel

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cat_card)

        val catId = getCatId(intent)
        initViewModel(catId)

        if(savedInstanceState == null) {
            if(getTransitionName(intent) != null)
                postponeEnterTransition()

            when {
                catId != null -> switchToPage(PageType.SHOW_SAVED)
                getSharingIntent(intent) != null -> switchToPage(PageType.LOADING)
                else -> switchToPage(PageType.ADD_NEW)
            }

            return
        }

        val page = savedInstanceState.getString(BUNDLE_KEY_CURRENT_PAGE)
        if(page != null)
            switchToPage(PageType.valueOf(page))

        restoreAlertDialogsState()
    }

    private fun initViewModel(catRepoId: String?) {
        val factory = (application as MyApplication).appContainer
            .provideCatCardViewModelFactory(catRepoId)
        viewModel = ViewModelProvider(this, factory).get(CatCardViewModel::class.java)
    }

    private fun getTransitionName(intent: Intent) =
        intent.getStringExtra(Constants.SHARED_TRANSITION_NAME_INTENT_KEY)

    private fun getCatId(intent: Intent) =
        intent.getStringExtra(Constants.CAT_ID_INTENT_KEY)

    private fun getSharingIntent(intent: Intent) =
        intent.getParcelableExtra<Intent>(Constants.SHARING_INPUT_INTENT_KEY)

    private fun switchToPage(type: PageType) {
        data class PageFactory(
            val actionBarFactory: () -> Fragment,
            val contentFactory: () -> Fragment
        )

        val data = viewModel.data.value ?: CatData()

        val pageFactory : PageFactory = when(type) {
            PageType.ADD_NEW, PageType.EDIT -> {
                val title =
                    if(type == PageType.ADD_NEW) getString(R.string.add_new_cat)
                    else getString(R.string.edit_cat)

                PageFactory({ ToolbarFragment.newInstance(title) },
                    { CatFormFragment.newInstance(data) })
            }
            PageType.SHOW_SAVED, PageType.SHOW_UNSAVED -> {
                val actionBarFactory: () -> Fragment =
                    if(type == PageType.SHOW_UNSAVED) { { UnsavedCatToolbarFragment() } }
                    else { { SavedCatToolbarFragment.newInstance(data) } }

                val transition = getTransitionName(intent)

                PageFactory(actionBarFactory, { PurringFragment.newInstance(transition, data) })
            }
            PageType.LOADING -> {
                PageFactory({ ToolbarFragment.newInstance(getString(R.string.wait)) },
                    { ExternalSharingDataLoadFragment.newInstance(getSharingIntent(intent)) })
            }
        }

        val show: (String?, Int, () -> Fragment, Transition?) -> Unit =
            { tag, viewId, factory, transition ->
                val fragment = supportFragmentManager.findFragmentByTag(tag) ?: factory()
                fragment.enterTransition = transition
                initFragment(fragment)
                showFragment(fragment, viewId, tag)
            }

        val transition = makeContentTransition(currentPage, type)
        show(type.tag + "ToolbarTag", R.id.toolbar_container, pageFactory.actionBarFactory, null)
        show(type.tag + "ContentTag", R.id.content_container, pageFactory.contentFactory, transition)

        currentPage = type
    }

    private fun makeContentTransition(from: PageType?, to: PageType?): Transition? {
        if(from == PageType.SHOW_UNSAVED && to == PageType.SHOW_SAVED)
            return null

        return MaterialFadeThrough.create(this)
    }

    private fun initFragment(fragment: Fragment) =
        when (fragment) {
            is SavedCatToolbarFragment -> initFragment(fragment)
            is UnsavedCatToolbarFragment -> initFragment(fragment)
            is PurringFragment -> initFragment(fragment)
            is CatFormFragment -> initFragment(fragment)
            is ExternalSharingDataLoadFragment -> initFragment(fragment)
            else -> {}
        }

    private fun initFragment(fragment: CatFormFragment) {
        fragment.onApplyListener = object : CatFormFragment.OnApplyListener {
            override fun onApply() {
                val data = fragment.catDataChange.to

                val isValid: (CatData) -> Boolean =
                    { it.name != null && it.purrAudioUri != null && it.photoUri != null }

                if (data == null || isValid(data).not()) {
                    showApplyAlertDialog()
                    return
                }

                viewModel.change(data)
                viewModel.syncDataWithRepo()
                switchToPage(PageType.SHOW_SAVED)
            }
        }
    }

    private fun initFragment(fragment: PurringFragment) {
        fragment.onImageLoadedListener = object : PurringFragment.OnImageLoadedListener {
            override fun onImageLoaded() = supportStartPostponedEnterTransition()
        }
    }

    private fun initFragment(fragment: ExternalSharingDataLoadFragment) {
        fragment.onGiveSharingResultListener = object : ExternalSharingDataLoadFragment.OnGiveSharingResultListener {
            override fun onSuccess(catData: CatData) {
                viewModel.change(catData)
                switchToPage(PageType.SHOW_UNSAVED)
            }

            override fun onError(error: String?) {
                error?.let{ showSnackbar(it) }
            }
        }
    }

    private fun initFragment(fragment: UnsavedCatToolbarFragment) {
        fragment.onSaveActionClickedListener = object: UnsavedCatToolbarFragment.OnSaveActionClickedListener {
            override fun onSaveClicked() {
                viewModel.syncDataWithRepo()
                showSnackbar(getString(R.string.save_snackbar_message_text))
                switchToPage(PageType.SHOW_SAVED)
            }
        }
    }

    private fun initFragment(fragment: SavedCatToolbarFragment) {
        fragment.onEditActionClickedListener = object : SavedCatToolbarFragment.OnEditActionClickedListener {
            override fun onEditClicked() = switchToPage(PageType.EDIT)
        }

        fragment.onTakeSharingResultListener = object: SavedCatToolbarFragment.OnTakeSharingResultListener {
            override fun onSuccess(intent: Intent) = startActivity(intent)
            override fun onError(error: String?) { error?.let{ showSnackbar(it) } }
        }
    }

    override fun onBackPressed() {
        val formFragment = supportFragmentManager.findFragmentById(R.id.container) as?
                CatFormFragment

        val isFormActive = formFragment != null && formFragment.isVisible
        val change = formFragment?.catDataChange

        if(isFormActive && change?.to != change?.from)
            showBackAlertDialog { finalizeBackPress() }
        else
            finalizeBackPress()
    }

    private fun finalizeBackPress() {
        if (currentPage == PageType.EDIT)
            switchToPage(PageType.SHOW_SAVED)
        else
            super.onBackPressed()
    }

    private fun showFragment(fragment: Fragment, viewId: Int, tag: String?)
    {
        if(supportFragmentManager.findFragmentByTag(tag) != null)
            return

        supportFragmentManager
            .beginTransaction()
            .replace(viewId, fragment, tag)
            .commit()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(content_container, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showBackAlertDialog(finishCallback: () -> Unit) {
        val positiveText = resources.getString(R.string.discard)
        val negativeText = resources.getString(R.string._continue)
        val message = resources.getString(R.string.changes_not_saved)

        val buttons = mapOf(
            SimpleAlertDialog.Button.POSITIVE to positiveText,
            SimpleAlertDialog.Button.NEGATIVE to negativeText)

        val dialog = SimpleAlertDialog.newInstance(message, buttons)
        setDialogPositiveListener(dialog, finishCallback)
        dialog.show(supportFragmentManager, DIALOG_ID_BACK)
    }

    private fun showApplyAlertDialog() {
        val dialog = SimpleAlertDialog.newInstance(resources.getString(R.string.fill_the_form),
            mapOf(SimpleAlertDialog.Button.POSITIVE to resources.getString(R.string.ok)))

        dialog.show(supportFragmentManager, DIALOG_ID_APPLY)
    }

    private fun restoreAlertDialogsState() {
        val backDialog = supportFragmentManager.findFragmentByTag(DIALOG_ID_BACK) as? SimpleAlertDialog
        backDialog?.let { setDialogPositiveListener(it) { finalizeBackPress() } }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(BUNDLE_KEY_CURRENT_PAGE, currentPage.toString())
    }

    companion object {
        private const val BUNDLE_KEY_CURRENT_PAGE = "CurrentPage"

        private const val DIALOG_ID_BACK = "DialogBack"
        private const val DIALOG_ID_APPLY = "DialogApply"
    }
}

private fun setDialogPositiveListener(dialog: SimpleAlertDialog, listener: () -> Unit) {
    dialog.listener = object: SimpleAlertDialog.Listener {
        override fun onDialogNegativeClick(dialog: DialogFragment?) { }
        override fun onDialogPositiveClick(dialog: DialogFragment?) {
            listener()
        }
    }
}
