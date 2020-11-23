package com.sergsave.purryourcat.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sergsave.purryourcat.BuildConfig
import com.sergsave.purryourcat.helpers.setToolbarAsActionBar
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.openRateAppLink
import de.psdev.licensesdialog.LicensesDialog
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.view_about_list_item.*
import kotlinx.android.synthetic.main.view_about_list_item.view.*

class AboutActivity : AppCompatActivity() {

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_about)
        setToolbarAsActionBar(toolbar, showBackButton = true)

        version_text.text = "${getString(R.string.version)} ${BuildConfig.VERSION_NAME}"

        rate_item.text.text = getString(R.string.rate_on_play_market)
        rate_item.setOnClickListener { openRateAppLink() }

        how_to_use_item.text.text = getString(R.string.how_to_use)
        how_to_use_item.setOnClickListener { openHowToUseDialog() }

        contact_item.text.text = getString(R.string.contact_us)
        contact_item.setOnClickListener { sendEmail(getString(R.string.dev_email)) }

        license_item.text.text = getString(R.string.licenses)
        license_item.setOnClickListener { openLicensesDialog() }
    }

    private fun sendEmail(address: String) {
        Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", address, null)).also {
            startActivity(it)
        }
    }

    private fun openLicensesDialog() {
        LicensesDialog.Builder(this)
            .setNotices(R.raw.notices)
            .setThemeResourceId(R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .build()
            .show()
    }

    private fun openHowToUseDialog() {
        HowToUseDialog().show(supportFragmentManager, null)
    }
}