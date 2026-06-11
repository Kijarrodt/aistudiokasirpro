package com.kasirpro.app.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.widget.Toast
import com.kasirpro.app.data.local.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object BluetoothPrinterHelper {

    // Standard Bluetooth Serial Port (SPP) UUID
    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    val bluetoothPermissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        arrayOf(
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.BLUETOOTH_SCAN
        )
    } else {
        arrayOf(
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    fun hasBluetoothPermissions(context: Context): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("MissingPermission")
    fun getPairedPrinters(context: Context): List<BluetoothDevice> {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter()
        if (adapter == null || !adapter.isEnabled) {
            return emptyList()
        }
        return try {
            adapter.bondedDevices.filter { device ->
                val name = device.name ?: ""
                val deviceClass = device.bluetoothClass?.majorDeviceClass ?: 0
                // Major class 1536 is imaging (includes printers) or generic printed patterns
                name.contains("print", ignoreCase = true) ||
                name.contains("thermal", ignoreCase = true) ||
                name.contains("rpp", ignoreCase = true) ||
                name.contains("mpt", ignoreCase = true) ||
                deviceClass == 1536 ||
                deviceClass == 1664 // toy or other printer class
            }
        } catch (e: SecurityException) {
            emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun printReceipt(
        context: Context,
        device: BluetoothDevice,
        rx: TransactionEntity,
        businessName: String,
        address: String = "",
        phone: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        var socket: BluetoothSocket? = null
        var outputStream: OutputStream? = null
        try {
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            socket.connect()
            outputStream = socket.outputStream

            val rawBytes = formatEscPosBytes(rx, businessName, address, phone)
            outputStream.write(rawBytes)
            outputStream.flush()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            try {
                outputStream?.close()
                socket?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun formatEscPosBytes(
        rx: TransactionEntity,
        businessName: String,
        address: String,
        phone: String
    ): ByteArray {
        val bytes = mutableListOf<Byte>()

        // Commands
        val init = byteArrayOf(0x1B, 0x40)
        val center = byteArrayOf(0x1B, 0x61, 0x01)
        val left = byteArrayOf(0x1B, 0x61, 0x00)
        val right = byteArrayOf(0x1B, 0x61, 0x02)
        val boldOn = byteArrayOf(0x1B, 0x45, 0x01)
        val boldOff = byteArrayOf(0x1B, 0x45, 0x00)
        val sizeLarge = byteArrayOf(0x1D, 0x21, 0x11)
        val sizeNormal = byteArrayOf(0x1D, 0x21, 0x00)
        val newLine = "\n".toByteArray(charset("GBK"))

        fun add(arr: ByteArray) {
            bytes.addAll(arr.toList())
        }

        fun addLine(text: String, alignment: ByteArray = left, isBold: Boolean = false, isLarge: Boolean = false) {
            add(alignment)
            if (isLarge) add(sizeLarge) else add(sizeNormal)
            if (isBold) add(boldOn) else add(boldOff)
            add(text.toByteArray(charset("GBK")))
            add(newLine)
        }

        // Initialize Printer
        add(init)

        // Header - Shop name large & bold
        addLine(businessName, center, isBold = true, isLarge = true)
        if (address.isNotBlank()) {
            addLine(address, center, isBold = false)
        }
        if (phone.isNotBlank()) {
            addLine("Telp: $phone", center, isBold = false)
        }

        // Line separator
        addLine("--------------------------------", center)

        // Transaction Metadata
        val formatTanggal = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale("id", "ID"))
        val dateText = formatTanggal.format(Date(rx.createdAt))
        
        addLine("ID TRX : ${rx.id}", left)
        addLine("Tanggal: $dateText", left)
        addLine("Kasir  : ${rx.kasirNama}", left)
        addLine("--------------------------------", center)

        // Cart items List
        val itemsSplit = rx.itemsRaw.split(";").filter { it.isNotBlank() }
        for (line in itemsSplit) {
            val parts = line.split(":")
            if (parts.size >= 4) {
                val name = parts.getOrNull(1).orEmpty()
                val qty = parts.getOrNull(2)?.toIntOrNull() ?: 1
                val price = parts.getOrNull(3)?.toDoubleOrNull() ?: 0.0
                val disc = parts.getOrNull(5)?.toDoubleOrNull() ?: 0.0
                val itemSub = (price - disc) * qty

                val itemName = if (name.length > 30) name.substring(0, 27) + "..." else name
                addLine(itemName, left, isBold = true)
                
                // Format qty & subtotal right alignment
                val qtyPriceStr = "  ${qty} x ${formatIdrValue(price)}"
                val subtotalStr = formatIdrValue(itemSub)
                
                val spacesCount = 31 - qtyPriceStr.length - subtotalStr.length
                val spaces = if (spacesCount > 0) " ".repeat(spacesCount) else " "
                addLine("$qtyPriceStr$spaces$subtotalStr", left)
            }
        }

        addLine("--------------------------------", center)

        // Totals
        fun addTotalLine(label: String, value: Double, isBold: Boolean = false) {
            val valStr = formatIdrValue(value)
            val spacesCount = 31 - label.length - valStr.length
            val spaces = if (spacesCount > 0) " ".repeat(spacesCount) else " "
            addLine("$label$spaces$valStr", left, isBold = isBold)
        }

        addTotalLine("Subtotal", rx.subtotal)
        if (rx.diskonTotal > 0) {
            addTotalLine("Diskon", -rx.diskonTotal)
        }
        addTotalLine("Total", rx.total, isBold = true)
        addTotalLine("Bayar", rx.bayarNominal)
        addTotalLine("Kembali", rx.kembalian)

        addLine("--------------------------------", center)
        addLine("Status: ${rx.status.uppercase()}", center, isBold = true)
        addLine("", center)
        addLine("Terima Kasih Telah Berbelanja", center)
        addLine("Kasir Pro - Aplikasi Kasir Andal", center)
        
        // Feed lines and paper cut
        add(newLine)
        add(newLine)
        add(newLine)
        add(newLine)

        return bytes.toByteArray()
    }

    private fun formatIdrValue(value: Double): String {
        return "Rp " + String.format(Locale("id", "ID"), "%,.0f", value)
    }
}
