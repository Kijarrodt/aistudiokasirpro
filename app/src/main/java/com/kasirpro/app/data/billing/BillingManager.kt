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
import kotlinx.coroutines.tasks.await

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
                        queryAndValidateActivePurchases()
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

    fun queryAndValidateActivePurchases() {
        val client = billingClient ?: return
        if (!client.isReady) {
            Log.d("BillingManager", "Cannot query purchases, BillingClient is not ready")
            return
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        client.queryPurchasesAsync(params) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                coroutineScope.launch {
                    validatePurchases(purchasesList ?: emptyList())
                }
            } else {
                Log.e("BillingManager", "queryPurchasesAsync failed with code ${billingResult.responseCode}: ${billingResult.debugMessage}")
            }
        }
    }

    private suspend fun validatePurchases(purchasesList: List<Purchase>) {
        val uid = kasirRepository.auth.currentUser?.uid ?: return
        try {
            // Fetch the user document from Firestore directly to see if they activated via Play Billing
            val userDocRef = kasirRepository.firestore.collection("users").document(uid)
            val doc = userDocRef.get().await()
            if (doc != null && doc.exists()) {
                val purchaseToken = doc.getString("playBillingPurchaseToken") ?: ""
                val currentStatus = doc.getString("subscriptionStatus") ?: "free"

                if (purchaseToken.isNotBlank() && currentStatus != "free") {
                    // Check if it's a simulated token. We do not demote simulated/mock packages to prevent developer testing breaking.
                    if (purchaseToken.startsWith("simulated_")) {
                        Log.d("BillingManager", "Subscription is simulated ($purchaseToken), skipping verification.")
                        return
                    }

                    // For real Play Store Billing, the active purchase token must be in the list of currently active purchases
                    val isActive = purchasesList.any { purchase ->
                        purchase.purchaseToken == purchaseToken && purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                    }

                    if (!isActive) {
                        Log.w("BillingManager", "Subscription with token $purchaseToken is no longer active (cancelled/refunded). Downgrading to free.")
                        kasirRepository.demoteUserToFree(uid)
                    } else {
                        Log.d("BillingManager", "Subscription is verified and active.")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("BillingManager", "Error validating active purchases", e)
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
                .setProductId("paket_dasar_bulanan_50k")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("paket_dasar_tahunan")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("paket_profesional_100k")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("paket_profesional_1tahun")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("paket_bisnis_bulanan")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("paket_bisnis_tahunan")
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
