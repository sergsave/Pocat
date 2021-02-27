package com.sergsave.pocat.screens.testing

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.sergsave.pocat.MyApplication
import com.sergsave.pocat.R
import com.sergsave.pocat.helpers.setToolbarAsActionBar
import kotlinx.android.synthetic.main.activity_testing.*

// TODO? Remove from main code, use manifest merger
// https://stackoverflow.com/questions/22024537/using-non-production-activity-for-testing-with-android-studio
class TestingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing)

        setToolbarAsActionBar(toolbar, showBackButton = true)
        val appContainer = (application as MyApplication).appContainer

        val viewModel: TestingViewModel by viewModels {
            appContainer.provideTestingViewModelFactory()
        }

        copy_all_button.setOnClickListener { viewModel.onCopyAllClicked() }
        viewModel.copyButtonStringId.observe(this, Observer {
            copy_all_button.isEnabled = it.isEnabled
            copy_all_button.text = getString(it.stringId)
        })

        reset_tutorial_button.setOnClickListener { viewModel.onResetTutorialClicked() }

        reset_apprate_button.setOnClickListener {
            appContainer.appRateManager.clearSavedRateInfo()
        }
    }
}