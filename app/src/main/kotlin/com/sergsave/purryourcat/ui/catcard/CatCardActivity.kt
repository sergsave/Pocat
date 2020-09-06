package com.sergsave.purryourcat.ui.catcard

import android.content.Intent
import android.os.Bundle
import android.transition.Transition
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.google.android.material.transition.MaterialFadeThrough
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.models.CatData
import kotlinx.android.synthetic.main.activity_cat_card.*

class CatCardActivity : AppCompatActivity() {

    private var lateinit navigation: NavigationViewModel

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cat_card)

        initNavigation()

        setToolbarAsActionBar(toolbar, true)

        if(savedInstanceState == null && getTransitionName(intent) != null)
            postponeEnterTransition()
    }

    private fun initNavigation() {
        val factory = NavigationViewModelFactory(
            getCatId(intent) != null,
            getSharingIntent(intent) != null
        )
        navigation = ViewModelProvider(this, factory).get(NavigationViewModel::class.java)

        val openFormFragment = { mode: FormFragment.Mode ->
            val fragment = FormFragment.newInstance(getCatId(intent), mode)
            fragment.enterTransition = MaterialFadeThrough.create(this)
            showFragment(fragment, FORM_FRAGMENT_TAG)
        }

        navigation.editCatEvent.observe(this, Observer {
            openFormFragment(FormFragment.Mode.EDIT)
        })

        navigation.addNewCatEvent.observe(this, Observer {
            openFormFragment(FormFragment.Mode.EDIT)
        })

        navigation.openCatEvent.observe(this, Observer {
            val fragment = PurringFragment.newInstance(getCatId(intent), getTransitionName(intent))
            fragment.enterTransition = MaterialFadeThrough.create(this)
            showFragment(fragment, PURRING_FRAGMENT_TAG)
        })

        navigation.startLoadSharingDataEvent.observe(this, Observer {
            val fragment = SharingDataLoadFragment.newInstance(getSharingIntent(intent))
            showFragment(fragment, LOAD_FRAGMENT_TAG)
        })

        navigation.startSharedElementTransitionEvent.observe(this, Observer {
            supportStartPostponedEnterTransition()
        })

        navigation.finishEvent.observe(this, Observer {
            super.onBackPressed()
        })
    }

    private fun showFragment(fragment: Fragment, tag: String?)
    {
        if(supportFragmentManager.findFragmentByTag(tag) != null)
            return
        // TODO: content_container
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment, tag).commit()
    }

    override fun onBackPressed() {
        // Delegate routing
        navigation.onBackButtonPressed()
    }

//    private fun initFragment(fragment: ExternalSharingDataLoadFragment) {
//        fragment.onGiveSharingResultListener = object : ExternalSharingDataLoadFragment.OnGiveSharingResultListener {
//            override fun onSuccess(catData: CatData) {
//                viewModel.change(catData)
//                switchToPage(PageType.SHOW_UNSAVED)
//            }
//
//            override fun onError(error: String?) {
//                error?.let{ showSnackbar(it) }
//            }
//        }
//    }

//    private fun initFragment(fragment: UnsavedCatToolbarFragment) {
//        fragment.onSaveActionClickedListener = object: UnsavedCatToolbarFragment.OnSaveActionClickedListener {
//            override fun onSaveClicked() {
//                viewModel.syncDataWithRepo()
//                showSnackbar(getString(R.string.save_snackbar_message_text))
//                switchToPage(PageType.SHOW_SAVED)
//            }
//        }
//    }

//    private fun initFragment(fragment: SavedCatToolbarFragment) {
//        fragment.onEditActionClickedListener = object : SavedCatToolbarFragment.OnEditActionClickedListener {
//            override fun onEditClicked() = switchToPage(PageType.EDIT)
//        }
//
//        fragment.onTakeSharingResultListener = object: SavedCatToolbarFragment.OnTakeSharingResultListener {
//            override fun onSuccess(intent: Intent) = startActivity(intent)
//            override fun onError(error: String?) { error?.let{ showSnackbar(it) } }
//        }
//    }

//    private fun finalizeBackPress() {
//        if (currentPage == PageType.EDIT)
//            switchToPage(PageType.SHOW_SAVED)
//        else
//            super.onBackPressed()
//    }

//    private fun showSnackbar(message: String) {
//        Snackbar.make(content_container, message, Snackbar.LENGTH_LONG).show()
//    }
//
//    private fun showBackAlertDialog() {
//        val positiveText = resources.getString(R.string.discard)
//        val negativeText = resources.getString(R.string._continue)
//        val message = resources.getString(R.string.changes_not_saved)
//
//        val buttons = mapOf(
//            SimpleAlertDialog.Button.POSITIVE to positiveText,
//            SimpleAlertDialog.Button.NEGATIVE to negativeText)
//
//        val dialog = SimpleAlertDialog.newInstance(message, buttons)
//        initBackDialog(dialog)
//        dialog.show(supportFragmentManager, DIALOG_ID_BACK)
//    }

//    private fun initBackDialog(dialog: SimpleAlertDialog) {
//        dialog.listener = object: SimpleAlertDialog.Listener {
//            override fun onDialogNegativeClick(dialog: DialogFragment?) { }
//            override fun onDialogPositiveClick(dialog: DialogFragment?) {
//                finalizeBackPress()
//                viewModel.restoreFromBackup()
//            }
//        }
//    }

//    private fun showApplyAlertDialog() {
//        val dialog = SimpleAlertDialog.newInstance(resources.getString(R.string.fill_the_form),
//            mapOf(SimpleAlertDialog.Button.POSITIVE to resources.getString(R.string.ok)))
//
//        dialog.show(supportFragmentManager, DIALOG_ID_APPLY)
//    }

    companion object {
        private const val FORM_FRAGMENT_TAG = "FormFragment"
        private const val PURRING_FRAGMENT_TAG = "PurringFragment"
        private const val LOADING_FRAGMENT_TAG = "LoadingFragment"
    }
}
