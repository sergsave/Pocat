package com.sergsave.purryourcat.screens.testing

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.sergsave.purryourcat.MyApplication
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.setToolbarAsActionBar
import kotlinx.android.synthetic.main.activity_testing.*

// TODO? Remove from main code, use manifest merger
// https://stackoverflow.com/questions/22024537/using-non-production-activity-for-testing-with-android-studio
class TestingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing)

        setToolbarAsActionBar(toolbar, showBackButton = true)

        val viewModel: TestingViewModel by viewModels {
            (application as MyApplication).appContainer.provideTestingViewModelFactory()
        }

        copy_all_button.setOnClickListener { viewModel.onCopyAllClicked() }
        viewModel.copyButtonStringId.observe(this, Observer {
            copy_all_button.isEnabled = it.isEnabled
            copy_all_button.text = getString(it.stringId)
        })
    }
}