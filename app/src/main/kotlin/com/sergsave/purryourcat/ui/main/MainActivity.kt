package com.sergsave.purryourcat.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.sergsave.purryourcat.BuildConfig
import com.sergsave.purryourcat.Constants
import com.sergsave.purryourcat.MyApplication
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.EventObserver
import com.sergsave.purryourcat.helpers.FirstTimeLaunchBugWorkaround
import com.sergsave.purryourcat.helpers.setToolbarAsActionBar
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.abs

// TODO: Check sdk version of all function
// TODO: Names of constants (XX_BUNDLE_KEY or BUNDLE_KEY_XX)
// TODO: Code inspect and warnings
// TODO: Require context and requireActivity
// TODO: Check all unhadled exception (in not rxjava code). Specify type of exception. Don't ignore.

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
            // Cleanup not in Application, because Application is created only after device reload
            viewModel.cleanUnusedFiles()
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
        if(isForwarded.not())
            return

        viewModel.onForwardIntent()

        // Forward further
        launchCatCard(this.intent)
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menu?.findItem(R.id.action_testing)?.isVisible = BuildConfig.DEBUG

        if (menu is MenuBuilder)
            menu.setOptionalIconsVisible(true)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
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