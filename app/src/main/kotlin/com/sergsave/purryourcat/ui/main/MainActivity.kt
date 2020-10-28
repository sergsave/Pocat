package com.sergsave.purryourcat.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.sergsave.purryourcat.BuildConfig
import com.sergsave.purryourcat.Constants
import com.sergsave.purryourcat.MyApplication
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.FirstTimeLaunchBugWorkaround
import com.sergsave.purryourcat.helpers.setToolbarAsActionBar
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.abs

// TODO: Check sdk version of all function
// TODO: Names of constants (XX_BUNDLE_KEY or BUNDLE_KEY_XX)
// TODO: Code inspect and warnings
// TODO: Hangs on Xiaomi Redmi 6
// TODO: Require context and requireActivity

class MainActivity : AppCompatActivity() {

    private enum class TabInfo(val pageNumber: Int, val titleStringId: Int) {
        SAMPLES(0, R.string.samples_tab),
        USER_CATS(1, R.string.user_cats_tab)
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

        // Don't use "by viewModels()" here, beacause it's a lazy
        val factory = (application as MyApplication).appContainer.provideMainViewModelFactory()
        val viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        setupPager(viewModel)

        setToolbarAsActionBar(toolbar, showBackButton = false)
        supportActionBar?.elevation = 0f

        checkInputSharingIntent()
    }

    private fun setupPager(viewModel: MainViewModel) {
        pager.adapter = PagerAdapter(this)
        pager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.onPageChanged(position)
            }
        })

        TabLayoutMediator(tab_layout, pager) { tab, position ->
            val tabInfo = listOf(TabInfo.SAMPLES, TabInfo.USER_CATS).find {
                it.pageNumber == position
            }
            tabInfo?.let { tab.text = getString(it.titleStringId) }
        }.attach()
    }

    private fun checkInputSharingIntent() {
        val isForwarded = intent?.getBooleanExtra(Constants.IS_FORWARDED_INTENT_KEY, false) ?: false
        if(isForwarded.not())
            return

        pager.setCurrentItem(TabInfo.USER_CATS.pageNumber, false)

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