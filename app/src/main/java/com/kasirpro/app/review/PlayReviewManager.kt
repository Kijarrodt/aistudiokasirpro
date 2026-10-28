package com.kasirpro.app.review

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory

object PlayReviewManager {
    private const val TAG = "PlayReviewManager"
    private const val PREFS_NAME = "play_review_prefs"
    private const val KEY_REVIEW_DONE = "has_reviewed"
    private const val KEY_NEVER_SHOW = "never_show_review"
    private const val KEY_LAST_PROMPT = "last_review_prompt_time"
    private const val KEY_SESSION_COUNT = "review_session_count"

    fun shouldPromptReview(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_REVIEW_DONE, false)) return false
        if (prefs.getBoolean(KEY_NEVER_SHOW, false)) return false

        val lastPrompt = prefs.getLong(KEY_LAST_PROMPT, 0L)
        val now = System.currentTimeMillis()
        // Wait at least 3 days between prompts
        if (now - lastPrompt < 3 * 24 * 60 * 60 * 1000L) {
            return false
        }
        return true
    }

    fun recordPromptShown(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_PROMPT, System.currentTimeMillis()).apply()
    }

    fun markReviewCompleted(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_REVIEW_DONE, true).apply()
    }

    fun markNeverShowAgain(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_NEVER_SHOW, true).apply()
    }

    fun launchPlayReview(activity: Activity) {
        markReviewCompleted(activity)
        val reviewManager = ReviewManagerFactory.create(activity)
        val request = reviewManager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = reviewManager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener {
                    Log.d(TAG, "In-app review flow finished")
                }
            } else {
                Log.w(TAG, "In-app review request failed, opening Play Store URL directly")
                openPlayStoreDirectly(activity)
            }
        }
    }

    fun openPlayStoreDirectly(context: Context) {
        val packageName = "com.kasirpro.pospintar.app"
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }
}
