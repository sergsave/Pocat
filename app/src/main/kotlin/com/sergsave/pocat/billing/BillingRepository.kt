package com.sergsave.pocat.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.SkuType
import com.sergsave.pocat.BuildConfig
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import com.sergsave.pocat.helpers.Result
import com.sergsave.pocat.models.Product
import io.reactivex.Single
import java.io.IOException

typealias Sku = String
private typealias SkuMap = Map<Sku, SkuDetails>

class BillingRepository(private val context: Context) {

    private lateinit var billingClient: BillingClient

    private var cacheSku2SkuDetails: SkuMap = emptyMap()
    private val purchaseConfirmed = PublishSubject.create<Result<Sku>>()
    private val productsFetched = PublishSubject.create<List<Product>>()
    private val connected = PublishSubject.create<Result<Unit>>()

    fun observePurchaseConfirmed(): Observable<Result<Sku>> = purchaseConfirmed

    // Will reconnect automatically
    fun connectToBillingService(): Observable<Result<Unit>> {
        return connected.doOnSubscribe {
            // Always reinstanstiate, because a client is not valid after end of connection
            billingClient = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build()

            startConnectionToBilling()
        }
    }

    private fun startConnectionToBilling() {
        if (billingClient.isReady) {
            connected.onNext(Result.Success(Unit))
            return
        }

        billingClient.startConnection(object: BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingResponseCode.OK)
                    connected.onNext(Result.Success(Unit))
                else
                    connected.onNext(Result.Error(RuntimeException("No connection")))
            }

            override fun onBillingServiceDisconnected() {
                startConnectionToBilling()
            }
        })
    }

    // No errors expected
    fun fetchProductsForPurchase(): Observable<List<Product>> {
        // Hack. Use PublishSubject instead Observable.create to avoid memory leakage
        return productsFetched.doOnSubscribe {
            val params = SkuDetailsParams.newBuilder()
            params.setSkusList(ALL_SKUS).setType(SkuType.INAPP)
            billingClient.querySkuDetailsAsync(params.build()) { billingResult, detailsList ->
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    productsFetched.onNext(detailsList.orEmpty().map {
                        Product(it.sku, it.price, it.title, it.description)
                    })
                    cacheSku2SkuDetails = detailsList.orEmpty().map { it.sku to it }.toMap()
                } else
                    productsFetched.onNext(emptyList())
            }
        }
    }

    fun startPurchase(activity: Activity, sku: Sku) {
        val skuDetails = cacheSku2SkuDetails.get(sku)
        if (skuDetails == null)
            return

        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()

        billingClient.launchBillingFlow(activity, flowParams)
    }

    fun processPendingPurchases() {
        queryPurchases()
    }

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            when (billingResult.responseCode) {
                BillingResponseCode.OK -> purchases?.forEach { handlePurchase(it) }
                BillingResponseCode.ITEM_ALREADY_OWNED -> queryPurchases()
                BillingResponseCode.SERVICE_DISCONNECTED -> startConnectionToBilling()
                else -> { }
            }
        }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED ||
            !isSignatureValid(purchase)) {
            purchaseConfirmed.onNext(Result.Error(RuntimeException("Invalid purchase")))
            return
        }

        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.getPurchaseToken())
            .build()

        billingClient.consumeAsync(consumeParams, { billingResult, _ ->
            if (billingResult.responseCode == BillingResponseCode.OK)
                purchaseConfirmed.onNext(Result.Success(purchase.sku))
            else
                purchaseConfirmed.onNext(Result.Error(RuntimeException("Consuming error")))
        })
    }

    private fun isSignatureValid(purchase: Purchase): Boolean {
        return try {
            Security.verifyPurchase(
                GOOGLE_PLAY_PUBLIC_BASE_64_KEY, purchase.originalJson, purchase.signature
            )
        } catch(e: IOException) {
            false
        }
    }

    private fun queryPurchases() {
        billingClient.queryPurchases(BillingClient.SkuType.INAPP)?.purchasesList?.forEach {
            handlePurchase(it)
        }
    }

    fun disconnectFromBillingService() {
        billingClient.endConnection()
        cacheSku2SkuDetails = emptyMap()
    }

    enum class ProductType { DONATION }

    companion object {
        fun productTypeFrom(sku: Sku): ProductType? =
            when {
                DONATIONS_SKUS.contains(sku) -> ProductType.DONATION
                else -> null
            }

        private val DONATIONS_SKUS = listOf(
            "donation_1",
            "donation_2",
            "donation_3",
            "donation_4",
            "donation_5"
        )
        private val ALL_SKUS = DONATIONS_SKUS

        private const val GOOGLE_PLAY_PUBLIC_BASE_64_KEY =
            BuildConfig.GOOGLE_PLAY_PUBLIC_BASE_64_KEY
    }
}