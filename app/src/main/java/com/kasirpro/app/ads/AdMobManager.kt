package com.kasirpro.app.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
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

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
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

    fun loadInterstitial(context: Context) {
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(TAG, "Interstitial Ad Loaded successfully")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.e(TAG, "Interstitial Ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    fun showInterstitialAd(
        activity: Activity,
        isPremium: Boolean,
        onDismissed: () -> Unit
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
            // Not ready yet, reload and proceed immediately so checkout is not blocked
            loadInterstitial(activity)
            onDismissed()
        }
    }

    fun loadRewarded(context: Context) {
        if (rewardedAd != null || isRewardedLoading) return
        isRewardedLoading = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            REWARDED_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    Log.d(TAG, "Rewarded Ad Loaded successfully")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    Log.e(TAG, "Rewarded Ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    fun showRewardedAd(
        activity: Activity,
        isPremium: Boolean,
        onUserEarnedReward: () -> Unit,
        onDismissed: () -> Unit
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
