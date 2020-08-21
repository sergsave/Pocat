package com.sergsave.purryourcat.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sergsave.purryourcat.MyApplication
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.fragments.CatsListFragment
import com.sergsave.purryourcat.helpers.ActionModeController
import com.sergsave.purryourcat.helpers.Constants
import com.sergsave.purryourcat.helpers.setToolbarAsActionBar
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.viewmodels.CatsListViewModel
import kotlinx.android.synthetic.main.activity_main.*

// TODO: Check sdk version of all function
// TODO: Names of constants (XX_BUNDLE_KEY or BUNDLE_KEY_XX)
// TODO: Code inspect and warnings
// TODO: Hangs on Xiaomi Redmi 6
// TODO: Change fragments interfaces to lambdas?

class MainActivity : AppCompatActivity() {

    private lateinit var catsListFragment: CatsListFragment
    private lateinit var viewModel: CatsListViewModel
    private var actionModeController = ActionModeController()

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViewModel()

        setToolbarAsActionBar(toolbar, showBackButton = false)

        catsListFragment = supportFragmentManager.findFragmentById(R.id.cats_list_fragment) as
            CatsListFragment // Static fragment
        initFragment(catsListFragment)

        val observer = Observer<Map<String, CatData>> { cats ->
            catsListFragment.cats = cats
        }
        viewModel.read().observe(this, observer)

        fab.setOnClickListener {
            actionModeController.finishActionMode()
            startActivity(Intent(this, CatCardActivity::class.java))
        }

        fab_clickable_layout.setOnClickListener { fab.performClick() }

        if(savedInstanceState == null) {
            // Cleanup not in Application, because Application create is created after device reload
            (application as MyApplication).appContainer.sharingManager.cleanup()
            checkInputSharingIntent()
        }
    }

    private fun initViewModel() {
        val factory = (application as MyApplication).appContainer.provideCatsListViewModelFactory()
        viewModel = ViewModelProvider(this, factory).get(CatsListViewModel::class.java)
    }

    private fun checkInputSharingIntent() {
        val isForwarded = intent?.getBooleanExtra(Constants.IS_FORWARDED_INTENT_KEY, false) ?: false
        if(isForwarded.not())
            return

        // Forward further
        val intent = Intent(this, CatCardActivity::class.java)
        intent.putExtra(Constants.SHARING_INPUT_INTENT_KEY, this.intent)

        startActivity(intent)
    }

    private fun initFragment(fragment: CatsListFragment) {
        fragment.onItemClickListener = object: CatsListFragment.OnItemClickListener {
            override fun onItemClick(catId: String, sharedElement: View, transitionName: String) {
                if(actionModeController.isInActionMode())
                    return

                goToPurringAnimated(catId, sharedElement, transitionName)
            }
        }

        fragment.onSelectionChangedListener = object: CatsListFragment.OnSelectionChangedListener {
            override fun onSelectionChanged(selected: List<String>)
                = handleSelectionChanged(selected)
        }
    }

    private fun goToPurringAnimated(catId: String, sharedElement: View, transitionName: String) {
        val intent = Intent(this, CatCardActivity::class.java)
        intent.putExtra(Constants.CAT_ID_INTENT_KEY, catId)
        intent.putExtra(Constants.SHARED_TRANSITION_NAME_INTENT_KEY, transitionName)

        val transitionOption = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this, sharedElement, transitionName)

        startActivity(intent, transitionOption.toBundle())
    }

    private fun handleSelectionChanged(selection: List<String>) {
        val isSelected = selection.isEmpty().not()
        val isInActionMode = actionModeController.isInActionMode()

        if(isSelected && isInActionMode.not())
            startActionMode()

        if(isSelected)
            actionModeController.title = selection.size.toString()

        if(isSelected.not() && isInActionMode)
            actionModeController.finishActionMode()
    }

    private fun startActionMode() {
        val onActionItemClickListener = object: ActionModeController.OnActionItemClickListener {
            override fun onItemClick(item: MenuItem) {
                if(item.itemId != R.id.action_remove)
                    return

                catsListFragment.selection.forEach { viewModel.remove(it) }
            }
        }

        val onFinishedListener = object: ActionModeController.OnFinishedListener {
            override fun onFinished() = catsListFragment.clearSelection()
        }

        actionModeController.apply {
            this.onActionItemClickListener = onActionItemClickListener
            this.onFinishedListener = onFinishedListener
            startActionMode(this@MainActivity, R.menu.menu_selection_context)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        if (menu is MenuBuilder)
            menu.setOptionalIconsVisible(true)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = when (item.itemId) {
            R.id.action_settings -> Intent(this, SettingsActivity::class.java)
            R.id.action_about -> Intent(this, AboutActivity::class.java)
            R.id.action_donate ->  Intent(this, DonateActivity::class.java)
            else -> null
        }

        if(intent == null)
            return super.onOptionsItemSelected(item)

        startActivity(intent)
        return true
    }
}