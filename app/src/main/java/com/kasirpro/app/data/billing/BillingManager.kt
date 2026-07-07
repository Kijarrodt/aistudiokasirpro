package com.kasirpro.app.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.kasirpro.app.data.repository.KasirRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BillingManager(
    private val context: Context,
    private val kasirRepository: KasirRepository
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var billingClient: BillingClient? = null

    // To store available ProductDetails in memory after query
    var availableProducts: List<ProductDetails> = emptyList()
        private set

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d("BillingManager", "Purchase canceled by user")
        } else {
            Log.e("BillingManager", "Purchase failed with response code: ${billingResult.responseCode}, message: ${billingResult.debugMessage}")
        }
    }

    init {
        initializeBillingClient()
    }

    private fun initializeBillingClient() {
        val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
            .enableOneTimeProducts()
            .build()
            billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(pendingPurchasesParams)
            .build()
        
        connectToPlayBilling()
    }

    private fun connectToPlayBilling() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("BillingManager", "BillingClient setup finished successfully")
                    // Pre-query products on connection success
                    coroutineScope.launch {
                        queryAvailableSubscriptions()
                    }
                } else {
                    Log.e("BillingManager", "BillingClient setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w("BillingManager", "BillingClient service disconnected. Reconnecting...")
                // In production, we should implement custom retry logic with exponential backoff.
            }
        })
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                
                billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d("BillingManager", "Purchase acknowledged successfully")
                        processPurchaseInRepository(purchase)
                    } else {
                        Log.e("BillingManager", "Acknowledge failed: ${billingResult.debugMessage}")
                    }
                }
            } else {
                Log.d("BillingManager", "Purchase already acknowledged")
                processPurchaseInRepository(purchase)
            }
        }
    }

    private fun processPurchaseInRepository(purchase: Purchase) {
        val productId = purchase.products.firstOrNull() ?: return
        
        // Retrieve the stored base plan ID corresponding to this purchase from SharedPreferences
        val pendingProductId = context.getSharedPreferences("billing_prefs", Context.MODE_PRIVATE)
            .getString("pending_product_id", null)
        val pendingBasePlanId = context.getSharedPreferences("billing_prefs", Context.MODE_PRIVATE)
            .getString("pending_base_plan_id", null)
        
        val basePlanId = if (pendingProductId == productId && pendingBasePlanId != null) {
            pendingBasePlanId
        } else {
            "bulanan" // Default fallback
        }
        
        val uid = kasirRepository.auth.currentUser?.uid ?: return
        
        coroutineScope.launch {
            val success = kasirRepository.activatePlayBillingSubscription(
                uid = uid,
                productId = productId,
                basePlanId = basePlanId,
                purchaseToken = purchase.purchaseToken
            )
            if (success) {
                Log.d("BillingManager", "Successfully upgraded subscription in repository for uid: $uid")
            } else {
                Log.e("BillingManager", "Failed to upgrade subscription in repository for uid: $uid")
            }
        }
    }

    suspend fun queryAvailableSubscriptions(): List<ProductDetails> = withContext(Dispatchers.IO) {
        val client = billingClient ?: return@withContext emptyList()
        
        // Wait until client is ready if needed
        var retries = 0
        while (!client.isReady && retries < 5) {
            try {
                kotlinx.coroutines.delay(500)
            } catch (e: Exception) {}
            retries++
        }
        
        if (!client.isReady) {
            Log.e("BillingManager", "BillingClient is not ready after retries")
            return@withContext emptyList()
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("kasirpro_dasar")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("kasirpro_profesional")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("kasirpro_bisnis")
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        try {
            // Using suspended queryProductDetails from play-billing-ktx
            val productDetailsResult = client.queryProductDetails(params)
            val detailsList = productDetailsResult.productDetailsList ?: emptyList()
            availableProducts = detailsList
            Log.d("BillingManager", "Queried ${detailsList.size} products successfully")
            detailsList
        } catch (e: Exception) {
            Log.e("BillingManager", "Error querying product details", e)
            emptyList()
        }
    }

    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails, offerToken: String, basePlanId: String) {
        val client = billingClient ?: return
        if (!client.isReady) {
            Log.e("BillingManager", "Cannot launch purchase flow, BillingClient is not ready")
            return
        }

        // Save selection to SharedPreferences to match it in onPurchasesUpdated
        context.getSharedPreferences("billing_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("pending_product_id", productDetails.productId)
            .putString("pending_base_plan_id", basePlanId)
            .apply()

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val billingResult = client.launchBillingFlow(activity, billingFlowParams)
        Log.d("BillingManager", "Billing flow launch response code: ${billingResult.responseCode}")
    }
}
