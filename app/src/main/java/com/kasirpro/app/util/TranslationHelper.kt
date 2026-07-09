package com.kasirpro.app.util

import androidx.compose.runtime.Composable

@Composable
fun t(text: String): String {
    val lang = Translator.currentLanguage.value
    return Translator.translate(text, lang)
}
