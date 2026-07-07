package com.kasirpro.pospintar.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.kasirpro.pospintar.app.ui.theme.OrangePrimary
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageHelper {

    // Simple cache for decoded Bitmaps to prevent redundant decoding overhead
    private val bitmapCache = java.util.concurrent.ConcurrentHashMap<String, Bitmap>()

    /**
     * Converts base64 string back to Bitmap using local in-memory caching.
     */
    fun base64ToBitmap(base64Str: String?): Bitmap? {
        if (base64Str.isNullOrBlank()) return null
        val cached = bitmapCache[base64Str]
        if (cached != null) return cached

        return try {
            val decodedBytes = android.util.Base64.decode(base64Str, android.util.Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            if (bitmap != null) {
                bitmapCache[base64Str] = bitmap
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Clear the local cache on Logout or App Close.
     */
    fun clearCache() {
        bitmapCache.clear()
    }

    /**
     * Resizes and compresses an image selected from URI, ensuring it stays under 200KB and maximum 400x400 px.
     * Returns a Base64 encoded string, or null if the photo is too large/invalid.
     */
    fun processAndConvertImageToBase64(context: Context, uri: Uri): String? {
        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream) ?: return null
            inputStream?.close()

            val base64 = processBitmapToBase64(bitmap)
            return base64
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            inputStream?.close()
        }
    }

    /**
     * Resizes and compresses a raw JPEG ByteArray, ensuring it stays under 200KB and maximum 400x400 px.
     * Returns a Base64 encoded string, or null if the photo is too large/invalid.
     */
    fun processAndConvertBytesToBase64(bytes: ByteArray): String? {
        try {
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
            val base64 = processBitmapToBase64(bitmap)
            return base64
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Common method to resize to max 400x400 and compress to under 200KB.
     */
    private fun processBitmapToBase64(bitmap: Bitmap): String? {
        try {
            val width = bitmap.width
            val height = bitmap.height
            val maxDimension = 400
            val resizedBitmap = if (width > maxDimension || height > maxDimension) {
                val ratio = width.toFloat() / height.toFloat()
                val (newWidth, newHeight) = if (ratio > 1) {
                    Pair(maxDimension, (maxDimension / ratio).toInt())
                } else {
                    Pair((maxDimension * ratio).toInt(), maxDimension)
                }
                Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            } else {
                bitmap
            }

            var quality = 90
            var compressedBytes: ByteArray
            do {
                val outputStream = ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                compressedBytes = outputStream.toByteArray()
                quality -= 10
            } while (compressedBytes.size > 200 * 1024 && quality > 10)

            if (compressedBytes.size > 200 * 1024) {
                if (resizedBitmap != bitmap) {
                    resizedBitmap.recycle()
                }
                bitmap.recycle()
                return null
            }

            if (resizedBitmap != bitmap) {
                resizedBitmap.recycle()
            }
            bitmap.recycle()

            return android.util.Base64.encodeToString(compressedBytes, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}

@Composable
fun ProductImage(
    fotoBase64: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    defaultIcon: ImageVector = Icons.Default.Fastfood
) {
    val bitmap = remember(fotoBase64) {
        if (!fotoBase64.isNullOrBlank()) {
            ImageHelper.base64ToBitmap(fotoBase64)
        } else {
            null
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = defaultIcon,
                contentDescription = null,
                tint = OrangePrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ShopLogoImage(
    logoBase64: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    defaultIcon: ImageVector = Icons.Default.Fastfood
) {
    val bitmap = remember(logoBase64) {
        if (!logoBase64.isNullOrBlank()) {
            ImageHelper.base64ToBitmap(logoBase64)
        } else {
            null
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = defaultIcon,
                contentDescription = null,
                tint = OrangePrimary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun ShopQrisImage(
    qrisBase64: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    defaultIcon: ImageVector = Icons.Default.QrCodeScanner
) {
    val bitmap = remember(qrisBase64) {
        if (!qrisBase64.isNullOrBlank()) {
            ImageHelper.base64ToBitmap(qrisBase64)
        } else {
            null
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = defaultIcon,
                contentDescription = null,
                tint = OrangePrimary,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
