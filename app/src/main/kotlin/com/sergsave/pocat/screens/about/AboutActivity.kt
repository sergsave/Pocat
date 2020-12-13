package com.sergsave.pocat.screens.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sergsave.pocat.BuildConfig
import com.sergsave.pocat.helpers.setToolbarAsActionBar
import com.sergsave.pocat.R
import com.sergsave.pocat.helpers.openRateAppLink
import de.psdev.licensesdialog.LicensesDialogFragment
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
        contact_item.setOnClickListener { sendEmail() }

        licenses_item.text.text = getString(R.string.licenses)
        licenses_item.setOnClickListener { openLicensesDialog() }

        credits_item.text.text = getString(R.string.credits)
        credits_item.setOnClickListener { openCreditsActivity() }
    }

    private fun sendEmail() {
        val address = getString(R.string.dev_email)
        val subject = getString(R.string.feedback_email_subject, getString(R.string.app_name))
        Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).also {
            it.putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
            it.putExtra(Intent.EXTRA_SUBJECT, subject)
            if (it.resolveActivity(packageManager) != null)
                startActivity(it)
        }
    }

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