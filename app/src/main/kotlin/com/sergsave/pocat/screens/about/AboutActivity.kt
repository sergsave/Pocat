package com.sergsave.pocat.screens.about

import android.content.Intent
import android.content.res.AssetManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sergsave.pocat.BuildConfig
import com.sergsave.pocat.R
import com.sergsave.pocat.helpers.openRateAppLink
import com.sergsave.pocat.helpers.sendEmail
import com.sergsave.pocat.helpers.sendShareAppLink
import com.sergsave.pocat.helpers.setToolbarAsActionBar
import de.psdev.licensesdialog.LicensesDialogFragment
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.view_about_list_item.view.*
import timber.log.Timber

class AboutActivity : AppCompatActivity() {

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_about)
        setToolbarAsActionBar(toolbar, showBackButton = true)

        version_text.text = getString(R.string.about_version, BuildConfig.VERSION_NAME)

        rate_item.text.text = getString(R.string.about_rate_on_play_market)
        rate_item.setOnClickListener { openRateAppLink() }

        share_item.text.text = getString(R.string.about_share_app)
        share_item.setOnClickListener { sendShareAppLink() }

        how_to_use_item.text.text = getString(R.string.about_how_to_use)
        how_to_use_item.setOnClickListener { openHowToUseDialog() }

        contact_item.text.text = getString(R.string.about_contact_us)
        contact_item.setOnClickListener { sendEmail() }

        licenses_item.text.text = getString(R.string.about_licenses_open_source)
        licenses_item.setOnClickListener { openLicensesDialog() }

        credits_item.text.text = getString(R.string.about_credits)
        credits_item.setOnClickListener { openCreditsActivity() }

        // Secret way to check error logging
        credits_item.setOnLongClickListener {
            Timber.e(RuntimeException("Test of error logging"))
            false
        }
    }

    private fun sendShareAppLink() {
        val message = getString(R.string.about_share_app_text)
        val chooserTitle = getString(R.string.about_share_app_chooser_title)
        sendShareAppLink(message, chooserTitle)
    }

    private fun sendEmail() {
        val address = getString(R.string.feedback_email)
        val subject = getString(R.string.feedback_message_subject, getString(R.string.app_name))
        sendEmail(address, subject)
    }

    // Workaround for webview bug inside LicenseDialog
    // https://stackoverflow.com/questions/41025200/android-view-inflateexception-error-inflating-class-android-webkit-webview
    override fun getAssets(): AssetManager = resources.assets

    private fun openLicensesDialog() {
        LicensesDialogFragment.Builder(this)
            .setNotices(R.raw.notices)
            .setThemeResourceId(R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .build()
            .show(supportFragmentManager, null)
    }

    private fun openHowToUseDialog() {
        HowToUseDialog().show(supportFragmentManager, null)
    }

    private fun openCreditsActivity() {
        startActivity(Intent(this, CreditsActivity::class.java))
    }
}