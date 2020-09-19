package com.sergsave.purryourcat.ui.catslist

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.sergsave.purryourcat.Constants
import com.sergsave.purryourcat.MyApplication
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.EventObserver
import com.sergsave.purryourcat.ui.catcard.CatCardActivity

// TODO: Check sdk version of all function
// TODO: Names of constants (XX_BUNDLE_KEY or BUNDLE_KEY_XX)
// TODO: Code inspect and warnings
// TODO: Hangs on Xiaomi Redmi 6
// TODO: Require context and requireActivity

class CatsListActivity : AppCompatActivity() {

    private val viewModel: CatsListActivityViewModel by viewModels {
        (application as MyApplication).appContainer.provideCatsListActivityViewModelFactory()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cats_list)

        if(savedInstanceState != null)
            return

        supportFragmentManager
            .beginTransaction()
            .add(R.id.container, CatsListFragment())
            .commit()

        viewModel.readyForHandleSharingDataEvent.observe(this, EventObserver {
            checkInputSharingIntent()
        })
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
}