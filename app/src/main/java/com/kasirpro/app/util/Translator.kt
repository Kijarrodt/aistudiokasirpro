package com.kasirpro.app.util

import androidx.compose.runtime.mutableStateOf

object Translator {
    val currentLanguage = mutableStateOf("id")

    fun t(text: String): String {
        return translate(text, currentLanguage.value)
    }

    private val enMap = mapOf(
        // Splash & Onboarding
        "Selamat Datang di Kasir Pro" to "Welcome to Kasir Pro",
        "Aplikasi kasir pintar serbaguna untuk segala jenis usaha." to "A versatile smart cashier app for all types of businesses.",
        "Mulai Sekarang" to "Get Started",
        "Atur Profil Bisnis Anda" to "Set Up Your Business Profile",
        "Nama Bisnis" to "Business Name",
        "Alamat Bisnis" to "Business Address",
        "Nomor Telepon" to "Phone Number",
        "Lanjutkan" to "Continue",
        "Daftar Akun Baru" to "Register New Account",
        "Masuk ke Akun Anda" to "Log in to Your Account",

        // Auth
        "Masuk" to "Log In",
        "Daftar" to "Register",
        "Keluar" to "Logout",
        "Email" to "Email",
        "Kata Sandi" to "Password",
        "Nama Lengkap" to "Full Name",
        "Belum punya akun?" to "Don't have an account?",
        "Sudah punya akun?" to "Already have an account?",
        "Hubungkan Ke Server" to "Connect to Server",
        "Berhasil Masuk" to "Successfully Logged In",
        "Gagal" to "Failed",
        "Konfirmasi" to "Confirmation",
        "Batal" to "Cancel",
        "Simpan" to "Save",
        "Hapus" to "Delete",
        "Ubah" to "Edit",
        "Tambah" to "Add",
        "Cari" to "Search",
        "Semua" to "All",
        "Status" to "Status",
        "Detail" to "Detail",
        "Informasi" to "Information",
        "Peringatan" to "Warning",
        "Sukses" to "Success",
        "Muat Ulang" to "Reload",

        // Navigation & Drawer
        "Beranda" to "Home",
        "Transaksi" to "Transactions",
        "Kasir" to "Cashier",
        "Produk" to "Products",
        "Stok" to "Stock",
        "Laporan" to "Reports",
        "Pengaturan" to "Settings",
        "Pelanggan" to "Customers",
        "Diskon" to "Discount",
        "Promo" to "Promo",
        "Notifikasi" to "Notifications",
        "Cabang" to "Branches",
        "Premium" to "Premium",
        "Laporan Penjualan" to "Sales Report",
        "Riwayat Transaksi" to "Transaction History",

        // Dashboard
        "Dashboard Penjualan" to "Sales Dashboard",
        "Total Penjualan" to "Total Sales",
        "Total Pendapatan" to "Total Revenue",
        "Total Transaksi" to "Total Transactions",
        "Keuntungan Bersih" to "Net Profit",
        "Grafik Penjualan mingguan" to "Weekly Sales Chart",
        "Grafik Penjualan Mingguan" to "Weekly Sales Chart",
        "Produk Terlaris" to "Best Selling Products",
        "Metode Pembayaran Terpopuler" to "Most Popular Payment Methods",
        "Transaksi Terakhir" to "Recent Transactions",
        "Semua Transaksi" to "All Transactions",
        "Tidak ada data penjualan" to "No sales data",
        "Ringkasan Hari Ini" to "Today's Summary",
        "Ringkasan Penjualan" to "Sales Summary",
        "Metode Pembayaran" to "Payment Methods",
        "Tunai" to "Cash",
        "Non-Tunai" to "Non-Cash",
        "QRIS" to "QRIS",

        // Cashier Screen
        "Keranjang" to "Cart",
        "Kosongkan" to "Clear",
        "Keranjang Belanja" to "Shopping Cart",
        "Keranjang Kosong" to "Cart is Empty",
        "Pilih Pelanggan" to "Select Customer",
        "Umum" to "General",
        "Umum (Tanpa Nama)" to "General (No Name)",
        "Total" to "Total",
        "Subtotal" to "Subtotal",
        "Bayar" to "Pay",
        "Bayar (Cash)" to "Pay (Cash)",
        "Bayar (QRIS / Non-Cash)" to "Pay (QRIS / Non-Cash)",
        "Kembalian" to "Change",
        "Uang Pas" to "Exact Change",
        "Pilih Metode Pembayaran" to "Select Payment Method",
        "Masukkan Jumlah Pembayaran" to "Enter Payment Amount",
        "Uang Diterima" to "Cash Received",
        "Transaksi Berhasil" to "Transaction Successful",
        "Cetak Struk" to "Print Receipt",
        "Transaksi Baru" to "New Transaction",
        "Cari Produk..." to "Search Product...",
        "Diskon Transaksi" to "Transaction Discount",
        "Biaya Tambahan" to "Additional Fee",
        "Catatan" to "Notes",
        "Keranjang Belanja Kosong" to "Shopping Cart is Empty",

        // Products & Stock
        "Kelola Produk & Stok" to "Manage Products & Stock",
        "Daftar Produk" to "Product List",
        "Tambah Produk Baru" to "Add New Product",
        "Nama Produk" to "Product Name",
        "Harga Beli" to "Buy Price",
        "Harga Jual" to "Sell Price",
        "Stok Awal" to "Initial Stock",
        "Kategori" to "Category",
        "Pilih Kategori" to "Select Category",
        "Simpan Produk" to "Save Product",
        "Ubah Produk" to "Edit Product",
        "Hapus Produk" to "Delete Product",
        "Apakah Anda yakin?" to "Are you sure?",
        "Stok saat ini:" to "Current stock:",
        "Tambah Stok" to "Add Stock",
        "Kurangi Stok" to "Reduce Stock",
        "Riwayat Stok" to "Stock History",
        "Stok Minim" to "Low Stock",
        "Barcode" to "Barcode",
        "Scan Barcode" to "Scan Barcode",
        "Import Excel" to "Import Excel",
        "Export Excel" to "Export Excel",

        // Settings & Backup
        "Pengaturan & Sinkronisasi" to "Settings & Sync",
        "Pengaturan & Backup" to "Settings & Backup",
        "Informasi Akun" to "Account Info",
        "Hubungkan Printer Bluetooth" to "Connect Bluetooth Printer",
        "Mode Gelap" to "Dark Mode",
        "Mode Gelap (Dark Theme)" to "Dark Mode",
        "Bahasa" to "Language",
        "Bahasa (Language)" to "Language",
        "Pilih Bahasa" to "Select Language",
        "Keluar dari Akun" to "Log Out",
        "Data Cadangan (Backup)" to "Backup Data",
        "Backup & Recovery Data" to "Backup & Recovery Data",
        "Terakhir Backup" to "Last Backup",
        "Mulai Backup Sekarang" to "Start Backup Now",
        "Restore Data Cadangan" to "Restore Backup Data",
        "Pulihkan Data" to "Restore Data",
        "Cadangkan data Anda secara aman di cloud Firestore." to "Securely back up your data in the Firestore cloud.",
        "Kembalikan data dari backup terakhir Anda." to "Restore data from your last backup.",
        "Hubungkan Printer" to "Connect Printer",
        "Printer Aktif" to "Active Printer",
        "Printer Bluetooth" to "Bluetooth Printer",
        "Pilih Printer" to "Select Printer",
        "Keluar dari Kasir Pro" to "Exit Cashier Pro",
        "Apakah Anda yakin ingin keluar?" to "Are you sure you want to log out?",
        "Panel Admin (Kelola Kode)" to "Admin Panel (Manage Codes)",

        // Premium Screens
        "Fitur Premium Aktif!" to "Premium Features Active!",
        "Fitur Premium Aktif" to "Premium Features Active",
        "Upgrade ke Premium" to "Upgrade to Premium",
        "Fitur Khusus Pemilik Usaha" to "Special Features for Business Owners",
        "Nikmati semua fitur terbaik tanpa batas:" to "Enjoy all the best features without limits:",
        "Kelola Multi-Cabang & Karyawan" to "Manage Multi-Branch & Employees",
        "Backup Cloud Otomatis" to "Automatic Cloud Backup",
        "Laporan Ekspor Excel Lengkap" to "Full Excel Export Reports",
        "Fitur Hutang & Piutang Pelanggan" to "Customer Debt & Receivable Features",
        "Bebas Iklan & Dukungan Prioritas" to "Ad-Free & Priority Support",
        "Masukkan Kode Aktivasi" to "Enter Activation Code",
        "Gunakan Kode" to "Use Code",
        "Aktifkan" to "Activate",
        "Langganan Bulanan" to "Monthly Subscription",
        "Langganan Tahunan" to "Yearly Subscription",
        "Keuntungan Premium:" to "Premium Benefits:",
        "Laporan Multi-Cabang" to "Multi-Branch Report",
        "Backup Cloud Otomatis" to "Automatic Cloud Backup",
        "Laporan PDF & Excel" to "PDF & Excel Reports",
        "Kelola Karyawan" to "Manage Employees"
    )

    fun translate(text: String, lang: String): String {
        if (lang != "en") return text
        
        // Try exact match
        enMap[text]?.let { return it }

        // Try trimmed match
        val trimmed = text.trim()
        enMap[trimmed]?.let { return it }

        // Handle pattern matches
        if (trimmed.startsWith("Terakhir Backup:")) {
            val suffix = trimmed.substringAfter("Terakhir Backup:")
            val fallbackSuffix = if (suffix.contains("Belum pernah")) "Never" else suffix
            return "Last Backup: $fallbackSuffix"
        }
        if (trimmed.startsWith("Tanggal:")) {
            return "Date: " + trimmed.substringAfter("Tanggal:")
        }
        if (trimmed.startsWith("Kembalian:")) {
            return "Change: " + trimmed.substringAfter("Kembalian:")
        }
        if (trimmed.startsWith("Stok:")) {
            return "Stock: " + trimmed.substringAfter("Stok:")
        }
        if (trimmed.startsWith("Diskon:")) {
            return "Discount: " + trimmed.substringAfter("Diskon:")
        }

        // Sentence-level word replacement or partial mapping
        var result = trimmed
        enMap.forEach { (indo, eng) ->
            if (indo.length > 3 && result.contains(indo, ignoreCase = true)) {
                result = result.replace(indo, eng, ignoreCase = true)
            }
        }
        return result
    }
}
