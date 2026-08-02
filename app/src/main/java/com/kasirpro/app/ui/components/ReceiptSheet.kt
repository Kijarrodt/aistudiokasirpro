package com.kasirpro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kasirpro.app.data.local.BusinessEntity
import com.kasirpro.app.data.local.TransactionEntity
import com.kasirpro.app.ui.theme.ReceiptInk
import com.kasirpro.app.ui.theme.ReceiptInkMuted
import com.kasirpro.app.ui.theme.ReceiptPaper
import com.kasirpro.app.util.ShopLogoImage
import com.kasirpro.app.util.TransactionItemCodec
import com.kasirpro.app.util.Translator
import java.text.NumberFormat

/**
 * Struk digital tunggal untuk seluruh aplikasi.
 *
 * Sebelumnya tata letak ini disalin di empat tempat (dialog struk kasir, detail struk kasir,
 * detail struk dashboard, dan detail struk pengaturan) dan sudah mulai berbeda-beda ukuran
 * fontnya. Semua pemanggil kini memakai komponen ini agar tampilannya persis sama.
 *
 * Komponen ini sengaja memakai warna tetap (tinta gelap di atas kertas putih) di tema terang
 * maupun gelap supaya sama dengan hasil cetak printer termal.
 *
 * @param footer slot opsional di bawah struk, mis. kotak catatan tambahan.
 */
@Composable
fun ReceiptSheet(
    rx: TransactionEntity,
    business: BusinessEntity?,
    idrFormatter: NumberFormat,
    modifier: Modifier = Modifier,
    footer: @Composable ColumnScope.() -> Unit = {}
) {
    val dateFormatter = java.text.SimpleDateFormat("dd-MM-yyyy HH:mm", java.util.Locale("id", "ID"))
    val items = TransactionItemCodec.decode(rx.itemsRaw)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ReceiptPaper)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (!business?.logoBase64.isNullOrBlank()) {
            ShopLogoImage(
                logoBase64 = business?.logoBase64,
                contentDescription = business?.namaBisnis,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .padding(bottom = 6.dp)
            )
        }

        ReceiptText(
            text = business?.namaBisnis ?: Translator.t("KASIR PRO SHOP"),
            size = 15,
            bold = true,
            align = TextAlign.Center
        )
        ReceiptText(
            text = business?.alamat?.takeIf { it.isNotBlank() } ?: Translator.t("Cabang Utama"),
            muted = true,
            align = TextAlign.Center
        )
        if (!business?.noTelpon.isNullOrBlank()) {
            ReceiptText(text = "Tel: ${business?.noTelpon}", muted = true, align = TextAlign.Center)
        }

        ReceiptDivider()
        ReceiptText(text = "No TRX: ${rx.id}", align = TextAlign.Start, fill = true)
        ReceiptText(
            text = "${Translator.t("Tanggal")}: ${dateFormatter.format(java.util.Date(rx.createdAt))}",
            align = TextAlign.Start,
            fill = true
        )
        ReceiptText(
            text = "${Translator.t("Kasir")}: ${rx.kasirNama}",
            align = TextAlign.Start,
            fill = true
        )
        ReceiptDivider()

        items.forEach { item ->
            val label = buildString {
                append(item.nama)
                item.varianSelected?.takeIf { it.isNotBlank() }?.let { append(" ($it)") }
                append(" x${item.jumlah} ${item.satuan}")
            }
            ReceiptRow(label = label, value = idrFormatter.format(item.subtotal()))
        }

        ReceiptDivider()
        ReceiptRow(label = Translator.t("Subtotal"), value = idrFormatter.format(rx.subtotal))
        if (rx.diskonTotal > 0) {
            ReceiptRow(
                label = Translator.t("Diskon Promo"),
                value = "-${idrFormatter.format(rx.diskonTotal)}"
            )
        }
        ReceiptRow(
            label = Translator.t("TOTAL"),
            value = idrFormatter.format(rx.total),
            bold = true,
            size = 13
        )
        ReceiptRow(label = Translator.t("DIBAYAR"), value = idrFormatter.format(rx.bayarNominal))
        ReceiptRow(label = Translator.t("KEMBALI"), value = idrFormatter.format(rx.kembalian))
        ReceiptDivider()

        ReceiptText(
            text = Translator.t("Terima kasih atas kunjungan Anda!"),
            align = TextAlign.Center
        )

        footer()
    }
}

@Composable
private fun ReceiptDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 4.dp),
        color = ReceiptInkMuted
    )
}

/**
 * Baris label di kiri dan nominal di kanan. Nama produk memakai weight supaya nama panjang
 * terpotong dengan elipsis, bukan mendorong nominal keluar layar seperti versi lama.
 */
@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    bold: Boolean = false,
    size: Int = 12
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = size.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = ReceiptInk,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        Text(
            text = value,
            fontSize = size.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = ReceiptInk,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun ReceiptText(
    text: String,
    size: Int = 12,
    bold: Boolean = false,
    muted: Boolean = false,
    align: TextAlign = TextAlign.Center,
    fill: Boolean = false
) {
    Text(
        text = text,
        fontSize = size.sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        color = if (muted) ReceiptInkMuted else ReceiptInk,
        textAlign = align,
        modifier = if (fill) Modifier.fillMaxWidth() else Modifier
    )
}
