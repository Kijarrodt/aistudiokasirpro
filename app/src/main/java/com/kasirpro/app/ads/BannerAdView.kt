package com.kasirpro.app.ads

import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun BannerAdView(
    adUnitId: String,
    isPremium: Boolean,
    modifier: Modifier = Modifier
) {
    if (isPremium) {
        // Do not render ads for premium users
        return
    }

    val testBannerId = "ca-app-pub-3940256099942544/6300978111"
    var activeAdUnitId by remember(adUnitId) { mutableStateOf(adUnitId) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 50.dp),
        contentAlignment = Alignment.Center
    ) {
        key(activeAdUnitId) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                factory = { context ->
                    AdView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        setAdSize(AdSize.BANNER)
                        this.adUnitId = activeAdUnitId
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                Log.d("BannerAdView", "Banner ad loaded successfully ($activeAdUnitId)")
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                Log.w(
                                    "BannerAdView",
                                    "Banner failed to load ($activeAdUnitId): ${error.message} (code ${error.code})"
                                )
                                // If the primary unit ID fails (e.g. no fill or unverified on test device),
                                // fall back to official Google test unit so the banner is guaranteed visible
                                if (activeAdUnitId != testBannerId) {
                                    activeAdUnitId = testBannerId
                                }
                            }
                        }

                        try {
                            loadAd(AdRequest.Builder().build())
                        } catch (e: Exception) {
                            Log.e("BannerAdView", "Failed loadAd: ${e.message}")
                            if (activeAdUnitId != testBannerId) {
                                activeAdUnitId = testBannerId
                            }
                        }
                    }
                }
            )
        }
    }
}
