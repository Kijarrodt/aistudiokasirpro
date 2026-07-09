package com.kasirpro.app.util

object Toast {
    const val LENGTH_SHORT = android.widget.Toast.LENGTH_SHORT
    const val LENGTH_LONG = android.widget.Toast.LENGTH_LONG
    fun makeText(context: android.content.Context, text: CharSequence, duration: Int): android.widget.Toast {
        return android.widget.Toast.makeText(context, tNon(text.toString()), duration)
    }
}
