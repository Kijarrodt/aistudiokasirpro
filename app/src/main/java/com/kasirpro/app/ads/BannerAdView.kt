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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 50.dp),
        contentAlignment = Alignment.Center
    ) {
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
                    this.adUnitId = adUnitId
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            Log.d("BannerAdView", "Banner ad loaded successfully ($adUnitId)")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.w(
                                "BannerAdView",
                                "Banner failed to load ($adUnitId): ${error.message} (code ${error.code})"
                            )
                        }
                    }

                    try {
                        loadAd(AdRequest.Builder().build())
                    } catch (e: Exception) {
                        Log.e("BannerAdView", "Failed loadAd: ${e.message}")
                    }
                }
            }
        )
    }
}
