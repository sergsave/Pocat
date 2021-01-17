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
import java.io.IOException

typealias Sku = String
private typealias SkuMap = Map<Sku, SkuDetails>

class BillingRepository(private val context: Context) {

    private lateinit var billingClient: BillingClient

    private var cacheSku2SkuDetails: SkuMap = emptyMap()
    private val sku2SkuDetails = PublishSubject.create<SkuMap>()
    private val purchaseConfirmed = PublishSubject.create<Result<Sku>>()

    fun observeProductsForPurchase(): Observable<List<Product>> =
        sku2SkuDetails
            .doOnNext { cacheSku2SkuDetails = it }
            .map { it.values.map { details ->
                details.run { Product(sku, price, title, description) }
            }
        }

    fun observePurchaseConfirmed(): Observable<Result<Sku>> = purchaseConfirmed

    fun startConnectionToBillingService() {
        // Always reinstanstiate, because a client is not valid after end of connection
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        tryConnectToBilling()
    }

    private fun tryConnectToBilling() {
        if (billingClient.isReady)
            return

        billingClient.startConnection(object: BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    querySkuDetails()
                    queryPurchases()
                } else
                    sku2SkuDetails.onNext(emptyMap())
            }

            override fun onBillingServiceDisconnected() {
                tryConnectToBilling()
            }
        })
    }

    private fun querySkuDetails() {
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(ALL_SKUS).setType(SkuType.INAPP)
        billingClient.querySkuDetailsAsync(params.build()) { billingResult, detailsList ->
            if (billingResult.responseCode == BillingResponseCode.OK)
                sku2SkuDetails.onNext(detailsList.orEmpty().map { it.sku to it }.toMap())
            else
                sku2SkuDetails.onNext(emptyMap())
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

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            when (billingResult.responseCode) {
                BillingResponseCode.OK -> purchases?.forEach { handlePurchase(it) }
                BillingResponseCode.ITEM_ALREADY_OWNED -> queryPurchases()
                BillingResponseCode.SERVICE_DISCONNECTED -> tryConnectToBilling()
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

    fun endConnectionFromBillingService() {
        billingClient.endConnection()
        sku2SkuDetails.onNext(emptyMap())
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