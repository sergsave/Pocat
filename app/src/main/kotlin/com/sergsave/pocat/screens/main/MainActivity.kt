package com.sergsave.pocat.screens.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.sergsave.pocat.BuildConfig
import com.sergsave.pocat.Constants
import com.sergsave.pocat.MyApplication
import com.sergsave.pocat.R
import com.sergsave.pocat.helpers.EventObserver
import com.sergsave.pocat.helpers.FirstTimeLaunchBugWorkaround
import com.sergsave.pocat.helpers.setToolbarAsActionBar
import kotlinx.android.synthetic.main.activity_main.*

// TODO: Names of constants (XX_BUNDLE_KEY or BUNDLE_KEY_XX)

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels {
        (application as MyApplication).appContainer.provideMainViewModelFactory()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirstTimeLaunchBugWorkaround.needFinishOnCreate(this)){
            finish()
            return
        }

        setContentView(R.layout.activity_main)
        
        if (savedInstanceState == null) {
            // Do this not in Application, because Application is created only after device reload
            viewModel.onActivityStarted()
            checkInputSharingIntent()
        }

        viewModel.requestPageChangeEvent.observe(this, EventObserver {
            pager.setCurrentItem(it, false)
        })

        setupPager()

        setToolbarAsActionBar(toolbar, showBackButton = false)
        supportActionBar?.elevation = 0f
    }

    private fun setupPager() {
        pager.adapter = PagerAdapter(this)
        pager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.onPageChanged(position)
            }
        })

        TabLayoutMediator(tab_layout, pager) { tab, position ->
            val tabInfo = viewModel.tabInfoForPosition(position)
            tabInfo?.let { tab.text = getString(it.titleStringId) }
        }.attach()
    }

    private fun checkInputSharingIntent() {
        val isForwarded = intent?.getBooleanExtra(Constants.IS_FORWARDED_INTENT_KEY, false) ?: false
        if (!isForwarded)
            return

        viewModel.onForwardIntent()

        // Forward further
        launchCatCard(this.intent)
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menu?.findItem(R.id.action_testing)?.isVisible = !BuildConfig.HIDE_TEST_ACTION

        if (menu is MenuBuilder)
            menu.setOptionalIconsVisible(true)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        viewModel.onOptionsItemSelected(item.itemId)
        when (item.itemId) {
            R.id.action_settings -> launchSettings()
            R.id.action_about -> launchAbout()
            R.id.action_donate -> launchDonate()
            R.id.action_testing -> launchTesting()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}