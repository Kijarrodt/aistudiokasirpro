package com.kasirpro.app.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdMobManager {
    private const val TAG = "AdMobManager"

    const val APP_ID = "ca-app-pub-9991336658531273~6760228236"
    const val BANNER_1_ID = "ca-app-pub-9991336658531273/2807208224"
    const val BANNER_2_ID = "ca-app-pub-9991336658531273/3911125078"
    const val INTERSTITIAL_ID = "ca-app-pub-9991336658531273/3005237510"
    const val REWARDED_ID = "ca-app-pub-9991336658531273/1883727536"

    // Official Google Test Ad Unit IDs for development, emulator testing, and fallback
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            // Enable emulator test device support
            val configuration = RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                .build()
            MobileAds.setRequestConfiguration(configuration)

            MobileAds.initialize(context) { status ->
                Log.d(TAG, "AdMob Initialized: ${status.adapterStatusMap}")
                isInitialized = true
                preloadAds(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed initializing MobileAds: ${e.message}")
        }
    }

    fun preloadAds(context: Context) {
        loadInterstitial(context)
        loadRewarded(context)
    }

    fun loadInterstitial(context: Context, useTestAd: Boolean = false) {
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true

        val adUnit = if (useTestAd) TEST_INTERSTITIAL_ID else INTERSTITIAL_ID
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            adUnit,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(TAG, "Interstitial Ad Loaded successfully ($adUnit)")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.w(TAG, "Interstitial Ad failed to load ($adUnit): ${loadAdError.message}")
                    if (!useTestAd) {
                        // Fallback to test unit so interstitial works during testing/no fill
                        loadInterstitial(context, useTestAd = true)
                    }
                }
            }
        )
    }

    private var lastInterstitialTime = 0L
    private const val MIN_INTERSTITIAL_INTERVAL_MS = 20_000L // Min 20s between interstitial transitions
    private var hasShownEntryAd = false

    /**
     * Show interstitial ad.
     * @param onAdActuallyDismissed Callback invoked ONLY after the user has watched and closed the full-screen interstitial ad.
     * @param onDismissed General completion callback invoked whether the ad was watched or skipped/failed.
     */
    fun showInterstitialAd(
        activity: Activity,
        isPremium: Boolean,
        onAdActuallyDismissed: (() -> Unit)? = null,
        onDismissed: () -> Unit = {}
    ) {
        if (isPremium) {
            // Premium users never see ads
            onDismissed()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitial(activity)
                    // The interstitial ad was actually shown and dismissed by the user:
                    onAdActuallyDismissed?.invoke()
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    loadInterstitial(activity)
                    onDismissed()
                }
            }
            ad.show(activity)
        } else {
            // Not ready yet, reload and proceed immediately so UI navigation is not blocked
            loadInterstitial(activity)
            onDismissed()
        }
    }

    /**
     * Show interstitial ad on screen changes EXCEPT when navigating to or from cashier/transactions.
     */
    fun showScreenTransitionInterstitial(
        activity: Activity,
        fromScreen: String,
        toScreen: String,
        isPremium: Boolean,
        onAdActuallyDismissed: (() -> Unit)? = null,
        onDismissed: () -> Unit = {}
    ) {
        if (isPremium) {
            onDismissed()
            return
        }

        // Strictly exclude cashier and transaction areas
        if (fromScreen == "cashier" || toScreen == "cashier") {
            onDismissed()
            return
        }

        // Exclude startup / auth screens
        val excludedScreens = setOf(
            "splash", "onboarding", "login", "register",
            "forgot_password", "setup_toko"
        )
        if (fromScreen in excludedScreens || toScreen in excludedScreens) {
            onDismissed()
            return
        }

        if (fromScreen == toScreen) {
            onDismissed()
            return
        }

        val now = System.currentTimeMillis()
        if (now - lastInterstitialTime < MIN_INTERSTITIAL_INTERVAL_MS) {
            // Rate-limited to preserve pleasant user experience
            onDismissed()
            return
        }

        lastInterstitialTime = now
        showInterstitialAd(
            activity = activity,
            isPremium = false,
            onAdActuallyDismissed = onAdActuallyDismissed,
            onDismissed = onDismissed
        )
    }

    /**
     * Show interstitial ad when newly entering the app (dashboard after login/splash).
     * Note: Does NOT trigger the subscription upsell popup at app entry.
     */
    fun showAppEntryInterstitial(
        activity: Activity,
        isPremium: Boolean,
        onDismissed: () -> Unit = {}
    ) {
        if (isPremium || hasShownEntryAd) {
            onDismissed()
            return
        }
        hasShownEntryAd = true
        lastInterstitialTime = System.currentTimeMillis()
        // onAdActuallyDismissed is null because subscription offer must NOT appear at app startup
        showInterstitialAd(
            activity = activity,
            isPremium = false,
            onAdActuallyDismissed = null,
            onDismissed = onDismissed
        )
    }

    /**
     * Show interstitial ad when exiting the app.
     */
    fun showAppExitInterstitial(
        activity: Activity,
        isPremium: Boolean,
        onExit: () -> Unit = {}
    ) {
        if (isPremium) {
            onExit()
            return
        }
        showInterstitialAd(
            activity = activity,
            isPremium = false,
            onAdActuallyDismissed = null,
            onDismissed = onExit
        )
    }

    fun loadRewarded(context: Context, useTestAd: Boolean = false) {
        if (rewardedAd != null || isRewardedLoading) return
        isRewardedLoading = true

        val adUnit = if (useTestAd) TEST_REWARDED_ID else REWARDED_ID
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            adUnit,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    Log.d(TAG, "Rewarded Ad Loaded successfully ($adUnit)")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    Log.w(TAG, "Rewarded Ad failed to load ($adUnit): ${loadAdError.message}")
                    if (!useTestAd) {
                        loadRewarded(context, useTestAd = true)
                    }
                }
            }
        )
    }

    fun showRewardedAd(
        activity: Activity,
        isPremium: Boolean,
        onUserEarnedReward: () -> Unit,
        onDismissed: () -> Unit = {}
    ) {
        if (isPremium) {
            // Premium user gets reward directly
            onUserEarnedReward()
            onDismissed()
            return
        }

        val ad = rewardedAd
        if (ad != null) {
            var rewardEarned = false
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewarded(activity)
                    if (rewardEarned) {
                        onUserEarnedReward()
                    }
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    loadRewarded(activity)
                    onDismissed()
                }
            }
            ad.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                rewardEarned = true
            }
        } else {
            loadRewarded(activity)
            // If ad failed to load, allow user to continue gracefully
            onUserEarnedReward()
            onDismissed()
        }
    }
}
