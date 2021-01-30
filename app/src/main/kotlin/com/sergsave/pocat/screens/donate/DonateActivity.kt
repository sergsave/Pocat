package com.sergsave.pocat.screens.donate

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.sergsave.pocat.R
import com.sergsave.pocat.MyApplication
import com.sergsave.pocat.helpers.EventObserver
import com.sergsave.pocat.helpers.setToolbarAsActionBar
import com.sergsave.pocat.screens.donate.DonateViewModel.LoadingState
import com.sergsave.pocat.models.Product
import kotlinx.android.synthetic.main.activity_donate.*

class DonateActivity : AppCompatActivity() {

    private val viewModel: DonateViewModel by viewModels {
        (application as MyApplication).appContainer.provideDonateViewModelFactory()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)

        setToolbarAsActionBar(toolbar, showBackButton = true)

        val lifecycleOwner = this

        viewModel.apply {
            loadingState.observe(lifecycleOwner, Observer {
                mapOf(
                    LoadingState.IN_PROGRESS to products_progress_bar,
                    LoadingState.ERROR to products_empty_stub,
                    LoadingState.SUCCESS to products_layout
                ).apply {
                    values.forEach { view -> view.visibility = View.GONE }
                    get(it)!!.visibility = View.VISIBLE
                }
            })

            donations.observe(lifecycleOwner, Observer { setProductChips(it) })

            showThankYouEvent.observe(lifecycleOwner, EventObserver {
                Snackbar.make(main_layout, R.string.donation_thank_you, Snackbar.LENGTH_LONG).show()
            })
        }

        retry_button.setOnClickListener { viewModel.onRetryClicked() }
    }

    private fun setProductChips(products: List<Product>) {
        products_chips.removeAllViews()
        val makeChip = { product: Product ->
            Chip(this).apply {
                text = product.price
                setOnClickListener { viewModel.makePurchase(this@DonateActivity, product) }
            }
        }
        products.forEach { products_chips.addView(makeChip(it)) }
    }
}