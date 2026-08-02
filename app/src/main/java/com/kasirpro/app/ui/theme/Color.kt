package com.kasirpro.app.ui.theme

import androidx.compose.ui.graphics.Color

val OrangePrimary = Color(0xFFF97316)
val OrangeDark = Color(0xFFC2410C)
val OrangeLight = Color(0xFFFFEDD5)

val Slate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)
val Slate700 = Color(0xFF334155)
val Slate100 = Color(0xFFF1F5F9)
val Slate50 = Color(0xFFF8FAFC)

// === Palet status semantik ===
// Dipakai untuk arti (berhasil / gagal / peringatan / info), bukan sekadar "warna hijau".
// Pasangan *Container / On*Container selalu dipakai bersamaan supaya kontrasnya terjamin.

// Berhasil, lunas, surplus
val SuccessGreen = Color(0xFF16A34A)
val SuccessGreenBright = Color(0xFF22C55E)
val SuccessEmerald = Color(0xFF10B981)
val SuccessContainer = Color(0xFFDCFCE7)
val OnSuccessContainer = Color(0xFF15803D)

// Gagal, hutang, minus, stok habis
val DangerRed = Color(0xFFDC2626)
val DangerRedBright = Color(0xFFEF4444)
val DangerContainer = Color(0xFFFEE2E2)
val OnDangerContainer = Color(0xFFB91C1C)

// Peringatan, stok menipis, mendekati kedaluwarsa
val WarningAmber = Color(0xFFF59E0B)
val WarningYellow = Color(0xFFEAB308)
val WarningContainer = Color(0xFFFEF08A)
val OnWarningContainer = Color(0xFF854D0E)

// Informasi netral
val InfoBlue = Color(0xFF3B82F6)
val InfoBlueDark = Color(0xFF1D4ED8)
val InfoContainer = Color(0xFFEFF6FF)

// Latar lembut warna merek
val OrangeSurface = Color(0xFFFFF7ED)

// Badge paket Bisnis
val PlanPurpleContainer = Color(0xFFFAF5FF)
val OnPlanPurpleContainer = Color(0xFF7E22CE)

// Warna merek pihak ketiga (jangan dipakai untuk hal lain)
val WhatsAppGreen = Color(0xFF25D366)

// === Struk ===
// Struk sengaja selalu tinta gelap di atas kertas putih, di tema terang maupun gelap,
// supaya tampilan di layar sama dengan hasil cetak printer termal.
val ReceiptPaper = Color.White
val ReceiptInk = Color(0xFF111827)
val ReceiptInkMuted = Color(0xFF4B5563)

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
