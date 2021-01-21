package com.sergsave.pocat.screens.donate

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sergsave.pocat.billing.BillingRepository
import com.sergsave.pocat.helpers.DisposableViewModel
import com.sergsave.pocat.helpers.Event
import com.sergsave.pocat.helpers.Result
import com.sergsave.pocat.models.Product

class DonateViewModel(private val billingRepo: BillingRepository): DisposableViewModel() {

    private val _donations = MutableLiveData<List<Product>>()
    val donations: LiveData<List<Product>>
        get() = _donations

    enum class LoadingState { IN_PROGRESS, ERROR, SUCCESS }
    private val _loadingState = MutableLiveData<LoadingState>()
    val loadingState: LiveData<LoadingState>
        get() = _loadingState

    private val _showThankYouEvent = MutableLiveData<Event<Unit>>()
    val showThankYouEvent: LiveData<Event<Unit>>
        get() = _showThankYouEvent

    private var recentlyPurchasedSku: String? = null

    init {
        startConnection()

        addDisposable(billingRepo.observePurchaseConfirmed().subscribe {
            if (it is Result.Success && it.value == recentlyPurchasedSku)
                _showThankYouEvent.value = Event(Unit)

            recentlyPurchasedSku = null
        })
    }

    private fun startConnection() {
        _loadingState.value = LoadingState.IN_PROGRESS

        addDisposable(billingRepo.connectToBillingService()
            .filter { it is Result.Success }
            .map {
                processPendingPurchases()
                fetchProductsForPurchase()
            }.subscribe()
        )
    }

    private fun processPendingPurchases() = billingRepo.processPendingPurchases()

    private fun fetchProductsForPurchase() {
        val disposable = billingRepo.fetchProductsForPurchase().subscribe { products ->
            val donationProducts = products.filter {
                BillingRepository.productTypeFrom(it.sku) == BillingRepository.ProductType.DONATION
            }
            _loadingState.value = if (donationProducts.isEmpty())
                LoadingState.ERROR
            else
                LoadingState.SUCCESS

            _donations.value = donationProducts
        }

        addDisposable(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        // Not disconnect from billing service. No interruption of purchase needed.
    }

    fun onRetryClicked() = startConnection()

    // No activity context saved in viewModel, so usage is safe here
    fun makePurchase(activity: Activity, donation: Product) {
        recentlyPurchasedSku = donation.sku
        billingRepo.startPurchase(activity, donation.sku)
    }
}

