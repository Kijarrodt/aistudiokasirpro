package com.kasirpro.app.util

import androidx.compose.runtime.mutableStateOf

object Translator {
    val currentLanguage = mutableStateOf("id")

    fun t(text: String): String {
        return translate(text, currentLanguage.value)
    }

    private val enMap = mapOf(
        // === SPLASH & ONBOARDING ===
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

        // === AUTH & LOGIN ===
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
        "Email atau Username Kasir" to "Email or Cashier Username",
        "budi_kasir atau owner@kasirpro.com" to "cashier_username or owner@kasirpro.com",
        "Masuk menggunakan Google" to "Sign in with Google",
        "Nama Pemilik" to "Owner Name",
        "Alamat Email" to "Email Address",
        "Daftar Sekarang" to "Register Now",
        "Kirim Link Reset" to "Send Reset Link",
        "Logo Toko" to "Store Logo",
        "Nama Toko/Bisnis" to "Store/Business Name",
        "contoh: Kopi Kita Jakarta" to "e.g., Kopi Kita Jakarta",
        "Alamat Kantor/Toko Utama" to "Main Office/Store Address",
        "Simpan & Masuk Beranda" to "Save & Enter Home",

        // === NAVIGATION & DRAWER ===
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

        // === DASHBOARD / HOME ===
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
        "Semua Cabang" to "All Branches",
        "Pendapatan Hari Ini" to "Today's Revenue",
        "Margin Profit (Est)" to "Profit Margin (Est)",
        "Transaksi Hari Ini" to "Today's Transactions",
        "Hutang Aktif" to "Active Debt",
        "Peringatan Stok Tipis" to "Low Stock Warning",
        "Hebat! Persediaan stok semua produk aman." to "Great! Stock levels of all products are safe.",
        "Resupply" to "Resupply",
        "Transaksi Terbaru" to "Recent Transactions",
        "Belum ada transaksi terekam saat ini." to "No transactions recorded at the moment.",
        "Detail Struk Transaksi" to "Transaction Receipt Details",
        "Cabang Utama" to "Main Branch",
        "Cetak Ulang Struk" to "Reprint Receipt",
        "Koreksi Transaksi" to "Correct Transaction",
        "Otoritas Pemilik Diperlukan" to "Owner Authority Required",
        "Masukkan Kode Unik Otoritas dari Pemilik Toko untuk mengizinkan koreksi/edit pada transaksi ini." to "Enter the unique Owner Authority Code to authorize corrections/edits to this transaction.",
        "Kode Otoritas Pemilik" to "Owner Authority Code",
        "Verifikasi" to "Verify",
        "Form Koreksi Transaksi" to "Transaction Correction Form",
        "Detail Produk Struk" to "Receipt Product Details",

        // === CASHIER & CHECKOUT ===
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
        "Koreksi / Perbaikan Transaksi" to "Transaction Correction / Repair",
        "Edit Produk Struk" to "Edit Receipt Products",
        "Tidak ada produk di struk" to "No products in receipt",
        "Tambah Produk ke Struk Baru" to "Add Product to New Receipt",
        "Total Belanja Baru (Rp)" to "New Total (Rp)",
        "Diskon Baru (Rp)" to "New Discount (Rp)",
        "Nominal Bayar Baru (Rp)" to "New Payment Amount (Rp)",
        "Status Pembayaran" to "Payment Status",
        "Pencatatan Pengeluaran Kas" to "Cash Expense Recording",
        "Catat uang keluar / beban operasional toko selama shift saat ini." to "Record cash out / store operational expenses during the current shift.",
        "Nominal Pengeluaran (Rp) *" to "Expense Amount (Rp) *",
        "Keterangan Pengeluaran *" to "Expense Description *",
        "Contoh: Listrik, Belanja Plastik" to "Example: Electricity, Plastic Bag Shopping",
        "Simpan Pengeluaran" to "Save Expense",
        "Simpan Koreksi" to "Save Correction",
        "Pilih Produk" to "Select Product",
        "Khusus transaksi hutang (DP/Piutang), Anda wajib memilih pelanggan yang sudah ada atau menambahkan pelanggan baru!" to "For debt transactions (DP/Receivable), you must select an existing customer or add a new customer!",
        "Peringatan: Transaksi hutang wajib memilih pelanggan!" to "Warning: Debt transaction requires selecting a customer!",
        "Transaksi gratis bulanan mencapai batas 50 kali. Silakan upgrade ke premium!" to "Free monthly transactions reached the limit of 50. Please upgrade to premium!",
        "Sukses: Sisa hutang pelanggan berhasil dicatat!" to "Success: Customer's remaining debt successfully recorded!",
        "Sukses: Pembayaran lunas diproses!" to "Success: Fully paid payment processed!",
        "Proses Hutang" to "Process Debt",
        "Proses Lunas" to "Process Paid",
        "Kembali" to "Back",
        "Belum Ada Data Pelanggan" to "No Customer Data Yet",
        "Anda belum menambahkan data pelanggan. Tambahkan pelanggan terlebih dahulu melalui menu Pelanggan." to "You haven't added any customer data. Please add customers first through the Customer menu.",
        "Fitur manajemen pelanggan tersedia di paket Premium. Upgrade sekarang untuk mengelola loyalitas pelanggan dan meningkatkan penjualan." to "Customer management features are available in the Premium package. Upgrade now to manage customer loyalty and boost sales.",
        "Upgrade Premium" to "Upgrade Premium",

        // === PRODUCTS & STOCK ===
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
        "PRODUK" to "PRODUCT",
        "STOK FISIK" to "PHYSICAL STOCK",
        "RIWAYAT" to "HISTORY",
        "Daftar Produk Kedaluwarsa" to "Expired Product List",
        "Produk Telah Kedaluwarsa:" to "Products Already Expired:",
        "Produk Mendekati Kedaluwarsa:" to "Products Nearing Expiration:",
        "Cari nama produk..." to "Search product name...",
        "Kategori:" to "Category:",
        "Tidak ada produk yang cocok dengan pencarian atau kategori Anda." to "No products match your search or category.",
        "Modal:" to "Cost:",
        "Jual:" to "Sell:",
        "Sisa Stok:" to "Remaining Stock:",
        "Sistem Stok:" to "Stock System:",
        "TAMBAH" to "ADD",
        "OPNAME" to "OPNAME",
        "Lihat Stok Saja" to "View Stock Only",
        "Tambah Produk Jualan Baru" to "Add New Sale Product",
        "Pilih / Ambil Foto Produk" to "Select / Take Product Photo",
        "Harga Modal" to "Cost Price",
        "Stok Minimum" to "Minimum Stock",
        "Barcode/UPC (Optional)" to "Barcode/UPC (Optional)",
        "Pengaturan Grosir" to "Wholesale Settings",
        "Harga Grosir" to "Wholesale Price",
        "Min Qty Grosir" to "Min Wholesale Qty",
        "Pengaturan Kedaluwarsa" to "Expiration Settings",
        "Ingatkan sebelum kedaluwarsa (Hari)" to "Remind before expiration (Days)",
        "Memproses foto produk..." to "Processing product photo...",
        "Daftarkan" to "Register",
        "Pilih Sumber Foto" to "Select Photo Source",
        "Ambil Foto dari Kamera" to "Take Photo from Camera",
        "Pilih dari Galeri" to "Select from Gallery",

        // === SETTINGS & BACKUP ===
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
        "Edit Profil Toko & Struk" to "Edit Store Profile & Receipt",
        "Detail toko di bawah ini akan ditampilkan secara otomatis pada Struk Belanja/Print cetak." to "The store details below will be automatically displayed on the receipt.",
        "Upload Logo" to "Upload Logo",
        "Nama Toko" to "Store Name",
        "Alamat Toko" to "Store Address",
        "No Telepon Toko" to "Store Phone Number",
        "Menyimpan logo toko..." to "Saving store logo...",
        "Pengaturan QRIS Pembayaran" to "QRIS Payment Settings",
        "Pilih Foto QRIS" to "Select QRIS Photo",
        "Klik untuk memilih gambar" to "Click to select image",
        "Menyimpan foto QRIS..." to "Saving QRIS photo...",
        "Kode Otoritas Pemilik" to "Owner Authority Code",
        "Kode verifikasi ini digunakan untuk menyetujui koreksi / edit transaksi yang dilakukan oleh Staff Kasir." to "This verification code is used to approve corrections/edits made by cashiers.",
        "Masukkan Kode Otoritas (Angka/Huruf)" to "Enter Authority Code (Alphanumeric)",
        "Fitur Premium Pro Terkunci" to "Premium Pro Features Locked",
        "Bagian ini hanya untuk pengguna Premium Pro. Silakan upgrade untuk membuka dan mengelola data secara real-time." to "This section is only for Premium Pro users. Please upgrade to unlock real-time data management.",
        "ID Kasir:" to "Cashier ID:",
        "Informasi Shift Aktif" to "Active Shift Info",
        "Mulai Shift" to "Start Shift",
        "Uang Modal Awal" to "Initial Capital",
        "Jumlah Transaksi" to "Transaction Count",
        "Total Tunai" to "Total Cash",
        "Total Non-Tunai" to "Total Non-Cash",
        "Uang Tunai di Laci" to "Cash in Drawer",
        "Akhiri Shift Kerja" to "End Work Shift",
        "Pengeluaran Kas Shift Ini" to "Cash Expense This Shift",
        "Catat" to "Record",
        "Belum ada pengeluaran kas selama shift ini." to "No cash expenses during this shift.",
        "Riwayat Transaksi Shift Ini" to "Transaction History This Shift",
        "Belum ada transaksi penjualan selama shift ini." to "No sales transactions during this shift.",
        "Pencatatan Pengeluaran" to "Record Expense",
        "Catat uang keluar / operasional laci kasir selama shift saat ini." to "Record cash out/operations from cash drawer during this shift.",
        "Detail Struk Penjualan" to "Sale Receipt Details",
        "Diskon Promo" to "Promo Discount",
        "DIBAYAR" to "PAID",
        "KEMBALI" to "CHANGE",
        "Tutup" to "Close",
        "Tidak Ada Shift Aktif" to "No Active Shift",
        "Silakan masuk ke halaman Kasir/POS untuk memulai shift baru Anda dan memasukkan modal awal." to "Please go to the Cashier/POS page to start your new shift and enter initial capital.",
        "Keluar dari Akun Kasir" to "Logout from Cashier Account",
        "Tutup Shift & Hitung Uang Laci" to "End Shift & Count Drawer Cash",
        "Uang Fisik di Laci Laci (Rp) *" to "Physical Cash in Drawer (Rp) *",
        "Contoh: 150000" to "Example: 150000",
        "Selisih Uang Laci:" to "Drawer Cash Difference:",
        "Akhiri Shift & Simpan" to "End Shift & Save",
        "Laporan Akhir Shift" to "End of Shift Report",
        "shift Anda telah berhasil diakhiri." to "your shift has been successfully ended.",
        "Kasir:" to "Cashier:",
        "Cabang:" to "Branch:",
        "Mulai Shift:" to "Start Shift:",
        "Selesai Shift:" to "End Shift:",
        "Jumlah Transaksi:" to "Transaction Count:",

        // === PREMIUM & MULTI-BRANCH ===
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
        "Laporan PDF & Excel" to "PDF & Excel Reports",
        "Kelola Karyawan" to "Manage Employees",
        "Daftarkan Pelanggan Baru" to "Register New Customer",
        "Nama Pelanggan" to "Customer Name",
        "No Handphone (WhatsApp)" to "Phone Number (WhatsApp)",
        "Tambah Hadiah Baru" to "Add New Reward",
        "Nama Hadiah (misal: Mug Cantik)" to "Reward Name (e.g., Nice Mug)",
        "Biaya Poin (misal: 100)" to "Points Cost (e.g., 100)",
        "Biaya Hadiah untuk Pembukuan (Rp)" to "Reward Cost for Bookkeeping (Rp)",
        "Nilai/harga modal hadiah" to "Cost value of reward",
        "Edit Hadiah" to "Edit Reward",
        "Nama Hadiah" to "Reward Name",
        "Biaya Poin" to "Points Cost",
        "Total Poin" to "Total Points",
        "Sesi Belanja" to "Shopping Session",
        "WhastApp" to "WhatsApp",
        "Penyesuaian" to "Adjustment",
        "Tukar Hadiah" to "Redeem Reward",
        "Koreksi / Bonus Poin Manual" to "Manual Point Correction / Bonus",
        "Jumlah poin, misal: 10 atau -5" to "Points amount, e.g., 10 or -5",
        "Sesuaikan" to "Adjust",
        "Kirim Broadcast Point CRM" to "Send Point CRM Broadcast",
        "Share Promo & Poin WA" to "Share Promo & Points via WA",
        "Pilih Hadiah Poin Pelanggan:" to "Select Customer Points Reward:",
        "Tidak ada hadiah tersedia di katalog." to "No rewards available in the catalog.",
        "Tukar" to "Redeem",
        "Kupon / Voucher Belanja" to "Coupon / Shopping Voucher",
        "Buat Voucher" to "Create Voucher",
        "Belum ada promo aktif." to "No active promos yet.",
        "Tambahkan Voucher Promo" to "Add Promo Voucher",
        "Nama Acara Promo" to "Promo Event Name",
        "Kode Promo (eg: HEMAT5)" to "Promo Code (e.g., HEMAT5)",
        "Tipe Potongan:" to "Discount Type:",
        "Persentase (%)" to "Percentage (%)",
        "Nominal (Rp)" to "Nominal (Rp)",
        "Nilai Diskon" to "Discount Value",
        "Min Transaksi Belanja" to "Min Purchase Transaction",
        "Aktifkan Promo" to "Activate Promo",
        "Daftar Cabang Usaha" to "List of Business Branches",
        "Tambah Cabang" to "Add Branch",
        "Cabang Utama (Default)" to "Main Branch (Default)",
        "Anda belum mendaftarkan cabang tambahan. Tambahkan cabang outlet baru untuk memantau data kasir terpisah secara realtime!" to "You have not registered any additional branches. Add new outlet branches to monitor separate cashier data in real-time!",
        "Belum ada kasir yang ditugaskan ke cabang ini." to "No cashiers assigned to this branch yet.",

        // === NOTIFICATIONS ===
        "Detail Notifikasi" to "Notification Details",
        "Unduh APK Baru" to "Download New APK",
        "Kembali ke Daftar Notifikasi" to "Back to Notification List",
        "Notifikasi Broadcast" to "Broadcast Notification",

        // === ADDITIONAL TRANSLATIONS ===
        "Batas Limit Akun Gratis" to "Free Account Limit",
        "Upgrade Sekarang" to "Upgrade Now",
        "Nanti Saja" to "Maybe Later",
        "Fitur Terkunci" to "Feature Locked",
        "Lihat Paket" to "View Packages",
        "Nanti" to "Later",
        "Pembaruan Tersedia" to "Update Available",
        "Versi terbaru aplikasi sudah tersedia. Update sekarang untuk mendapatkan fitur terbaru dan perbaikan bug." to "The latest version of the app is available. Update now to get the latest features and bug fixes.",
        "Tidak dapat membuka link download" to "Cannot open download link",
        "SUKSES" to "SUCCESS",
        "GAGAL" to "FAILED",
        "PERINGATAN" to "WARNING",
        "DISALIN" to "COPIED",
        "INFO" to "INFO",
        "Kembali ke Login" to "Back to Login",
        "Upload Massal" to "Mass Upload",
        "Batas min:" to "Min limit:",
        "Batas min" to "Min limit",
        "bungkus" to "pack",
        "porsi" to "portion",
        "buah" to "pieces",
        "botol" to "bottle",
        "lembar" to "sheet",
        "kotak" to "box",
        "grup" to "group",
        "pasang" to "pair",
        "lusin" to "dozen",
        "kodi" to "kodi",
        "Email pemulihan password berhasil dikirim ke" to "Password recovery email has been sent to",
        "Pelanggan" to "Customer",
        "berhasil disimpan!" to "successfully saved!",
        "ditambahkan ke keranjang (Banyaknya:" to "added to cart (Qty:",
        "sukses ditambahkan!" to "successfully added!",
        "Berhasil melampirkan" to "Successfully attached",
        "foto produk!" to "product photos!",
        "Gagal menyimpan pengeluaran:" to "Failed to save expense:",
        "Berhasil mencetak ke printer:" to "Successfully printed to:",
        "Dashboard Pengguna Aktif & Sesi" to "Active Users & Sessions Dashboard",
        "Total Pengguna" to "Total Users",
        "Aktif Hari Ini" to "Active Today",
        "Subs Premium" to "Premium Subs",
        "Free Tier" to "Free Tier",
        "Daftar Pengguna & Status Subscription" to "User List & Subscription Status",
        "Tidak ada data pengguna ditemukan." to "No user data found.",
        "Memantau Kinerja & Status Sistem (APM)" to "Monitor System Performance & Status (APM)",
        "SQLite Latency Ping" to "SQLite Latency Ping",
        "Optimal" to "Optimal",
        "Firestore Sync Engine" to "Firestore Sync Engine",
        "Terhubung & Sehat" to "Connected & Healthy",
        "Penggunaan Heap Memory JVM" to "JVM Heap Memory Usage",
        "Disk & Image Coil Cache size" to "Disk & Image Coil Cache Size",
        "System Console & Terminal Event Logs" to "System Console & Terminal Event Logs",
        "Aktif (Belum Dipakai)" to "Active (Unused)",
        "Terpakai (Sudah Aktif)" to "Used (Active)",
        "ID Pengguna / UID (Wajib)" to "User ID / UID (Required)",
        "Masukkan atau tempel UID pengguna..." to "Enter or paste user UID...",
        "Judul Notifikasi" to "Notification Title",
        "Isi Pesan Notifikasi" to "Notification Message Content",
        "Nomor Versi (Contoh: 1.1.0)" to "Version Number (e.g., 1.1.0)",
        "Link Download APK" to "APK Download Link",
        "Kirim ke Semua Pengguna" to "Send to All Users",
        "Belum ada broadcast yang dikirim" to "No broadcast sent yet",
        "Hubungkan Ulang" to "Reconnect",
        "Panel Admin KasirPro" to "KasirPro Admin Panel",
        "Pantauan (Dashboard)" to "Monitoring (Dashboard)",
        "Kode Aktivasi" to "Activation Code",
        "Broadcast Notifikasi" to "Broadcast Notification",
        "Foto terlalu besar. Pilih foto yang lebih kecil." to "Photo is too large. Choose a smaller photo.",
        "Gagal memproses foto" to "Failed to process photo",
        "Silakan isi semua data wajib!" to "Please fill in all required data!",
        "Menyimpan Produk" to "Saving Product",
        "Silakan lengkapi semua parameter wajib!" to "Please complete all required parameters!",
        "File kosong atau tidak dapat di-parse" to "Empty or unparseable file",
        "Gagal mengimpor spreadsheet:" to "Failed to import spreadsheet:",
        "Silakan pilih cabang tujuan!" to "Please select target branch!",
        "Barcode tidak terdaftar!" to "Barcode not registered!",
        "Barcode kosong atau tidak terbaca." to "Barcode is empty or unreadable.",
        "Scan dibatalkan:" to "Scan cancelled:",
        "GMS Code Scanner tidak tersedia. Pastikan Google Play Services terpasang." to "GMS Code Scanner is unavailable. Make sure Google Play Services is installed.",
        "Izin Bluetooth ditolak. Gagal menyambungkan ke printer." to "Bluetooth permission denied. Failed to connect to printer.",
        "Gagal mencetak. Harap sambungkan kembali printer Bluetooth Anda." to "Failed to print. Please reconnect your Bluetooth printer.",
        "Simulasi Cetak Struk ke printer RPP02N - Selesai!" to "Receipt Print Simulation to RPP02N - Finished!",
        "Kode Otoritas salah! Akses Ditolak." to "Incorrect Authority Code! Access Denied.",
        "Nominal bayar kurang dari total untuk status lunas!" to "Payment amount is less than total for fully paid status!",
        "Struk tidak boleh kosong!" to "Receipt cannot be empty!",
        "Berhasil simpan koreksi transaksi!" to "Successfully saved transaction correction!",
        "Nominal & keterangan wajib diisi!" to "Amount & description are required!",
        "Pengeluaran kas berhasil dicatatkan!" to "Cash expense successfully recorded!",
        "Gagal menyimpan pengeluaran:" to "Failed to save expense:"
    )

    private val sortedEnEntries = enMap.entries.sortedByDescending { it.key.length }
    private val enMapCaseInsensitive = enMap.mapKeys { it.key.trim().lowercase() }

    fun translate(text: String, lang: String): String {
        if (lang != "en") return text
        
        val trimmed = text.trim()
        
        // 1. Exact match lookup
        val exactMatch = enMapCaseInsensitive[trimmed.lowercase()]
        if (exactMatch != null) {
            return exactMatch
        }
        
        // 2. Handle pattern matches cleanly by translating substrings step by step
        var result = trimmed
        if (result.startsWith("Terakhir Backup:", ignoreCase = true)) {
            val suffix = result.substringAfter("Terakhir Backup:")
            val fallbackSuffix = if (suffix.contains("Belum pernah", ignoreCase = true)) "Never" else suffix
            result = "Last Backup: $fallbackSuffix"
        } else if (result.startsWith("Tanggal:", ignoreCase = true)) {
            result = "Date: " + result.substringAfter("Tanggal:")
        } else if (result.startsWith("Kembalian:", ignoreCase = true)) {
            result = "Change: " + result.substringAfter("Kembalian:")
        } else if (result.startsWith("Stok:", ignoreCase = true)) {
            result = "Stock: " + result.substringAfter("Stok:")
        } else if (result.startsWith("Diskon:", ignoreCase = true)) {
            result = "Discount: " + result.substringAfter("Diskon:")
        } else if (result.startsWith("Persediaan system terhitung:", ignoreCase = true)) {
            result = "System stock counted: " + result.substringAfter("Persediaan system terhitung:")
        } else if (result.startsWith("Preview & Validasi Data", ignoreCase = true)) {
            result = "Preview & Data Validation " + result.substringAfter("Preview & Validasi Data").replace("Baris", "Rows", ignoreCase = true)
        } else if (result.startsWith("Valid:", ignoreCase = true)) {
            result = "Valid:" + result.substringAfter("Valid:")
        } else if (result.startsWith("Error:", ignoreCase = true)) {
            result = "Error:" + result.substringAfter("Error:")
        } else if (result.startsWith("Nama:", ignoreCase = true) && result.contains("| Kategori:", ignoreCase = true)) {
            val parts = result.split("|")
            val namaPart = "Name:" + parts[0].substringAfter("Nama:")
            val katPart = " Category:" + parts[1].substringAfter("Kategori:")
            result = "$namaPart|$katPart"
        } else if (result.startsWith("Jual:", ignoreCase = true) && result.contains("| Stok:", ignoreCase = true)) {
            val parts = result.split("|")
            val jualPart = "Sell:" + parts[0].substringAfter("Jual:")
            val stokPart = " Stock:" + parts[1].substringAfter("Stok:")
            result = "$jualPart|$stokPart"
        } else if (result.startsWith("Detail Error:", ignoreCase = true)) {
            result = "Error Details: " + result.substringAfter("Detail Error:")
        } else if (result.startsWith("Jumlah total melebihi batas", ignoreCase = true)) {
            result = result.replace("Jumlah total melebihi batas", "Total amount exceeds the limit of", ignoreCase = true)
                          .replace("produk untuk paket Anda. Memerlukan minimal", "products for your plan. Requires at least", ignoreCase = true)
        } else if (result.startsWith("Sukses mengupload", ignoreCase = true)) {
            result = result.replace("Sukses mengupload", "Successfully uploaded", ignoreCase = true)
                          .replace("produk ke Cabang", "products to Branch", ignoreCase = true)
        } else if (result.endsWith("Item Terpilih", ignoreCase = true)) {
            val count = result.substringBefore("Item Terpilih")
            result = "$count Items Selected"
        } else if (result.startsWith("Pelanggan:", ignoreCase = true)) {
            result = result.replace("Pelanggan:", "Customer:", ignoreCase = true)
                          .replace("Stok:", "Stock:", ignoreCase = true)
                          .replace("Poin", "Points", ignoreCase = true)
        } else if (result.startsWith("Kupon:", ignoreCase = true)) {
            result = "Coupon:" + result.substringAfter("Kupon:")
        } else if (result.startsWith("Bayar Pas:", ignoreCase = true)) {
            result = "Exact Change:" + result.substringAfter("Bayar Pas:")
        } else if (result.startsWith("Telp:", ignoreCase = true)) {
            result = "Phone:" + result.substringAfter("Telp:")
        } else if (result.startsWith("Alamat:", ignoreCase = true)) {
            result = "Address:" + result.substringAfter("Alamat:")
        } else if (result.startsWith("Kode:", ignoreCase = true)) {
            result = result.replace("Kode:", "Code:", ignoreCase = true)
        } else if (result.startsWith("Tel:", ignoreCase = true)) {
            result = "Phone:" + result.substringAfter("Tel:")
        } else if (result.startsWith("No TRX:", ignoreCase = true)) {
            result = "TX No:" + result.substringAfter("No TRX:")
        } else if (result.startsWith("Kasir:", ignoreCase = true)) {
            result = "Cashier:" + result.substringAfter("Kasir:")
        } else if (result.startsWith("ID Kasir:", ignoreCase = true)) {
            result = "Cashier ID:" + result.substringAfter("ID Kasir:")
        } else if (result.startsWith("Uang Fisik di Laci", ignoreCase = true)) {
            result = "Physical Cash in Drawer" + result.substringAfter("Uang Fisik di Laci")
        } else if (result.startsWith("Selisih Uang Laci:", ignoreCase = true)) {
            result = "Drawer Cash Difference:" + result.substringAfter("Selisih Uang Laci:")
        } else if (result.startsWith("Yth. Kasir", ignoreCase = true)) {
            result = result.replace("Yth. Kasir", "Dear Cashier", ignoreCase = true)
                          .replace("shift Anda telah berhasil diakhiri.", "your shift has been successfully ended.", ignoreCase = true)
        } else if (result.startsWith("Mulai Shift:", ignoreCase = true)) {
            result = "Start Shift:" + result.substringAfter("Mulai Shift:")
        } else if (result.startsWith("Selesai Shift:", ignoreCase = true)) {
            result = "End Shift:" + result.substringAfter("Selesai Shift:")
        } else if (result.startsWith("Jumlah Transaksi:", ignoreCase = true)) {
            result = "Transaction Count:" + result.substringAfter("Jumlah Transaksi:")
        } else if (result.startsWith("Expired:", ignoreCase = true)) {
            result = "Expired:" + result.substringAfter("Expired:")
        } else if (result.startsWith("Butuh ", ignoreCase = true) && result.contains(" Poin", ignoreCase = true)) {
            result = result.replace("Butuh", "Requires", ignoreCase = true)
                          .replace("Poin", "Points", ignoreCase = true)
                          .replace("Biaya Toko", "Store Cost", ignoreCase = true)
        } else if (result.startsWith("ID Transaksi:", ignoreCase = true)) {
            result = "Transaction ID: " + result.substringAfter("ID Transaksi:")
        } else if (result.startsWith("Email pemulihan password berhasil dikirim ke", ignoreCase = true)) {
            result = "Password recovery email has been sent to " + result.substringAfter("Email pemulihan password berhasil dikirim ke")
        } else if (result.startsWith("Pelanggan", ignoreCase = true) && result.contains("berhasil disimpan!", ignoreCase = true)) {
            result = "Customer " + result.substringAfter("Pelanggan").replace("berhasil disimpan!", "successfully saved!", ignoreCase = true)
        } else if (result.contains("ditambahkan ke keranjang (Banyaknya:", ignoreCase = true)) {
            result = result.replace("ditambahkan ke keranjang (Banyaknya:", "added to cart (Qty:", ignoreCase = true)
        } else if (result.contains("sukses ditambahkan!", ignoreCase = true)) {
            result = result.replace("sukses ditambahkan!", "successfully added!", ignoreCase = true)
        } else if (result.startsWith("Berhasil melampirkan", ignoreCase = true)) {
            result = result.replace("Berhasil melampirkan", "Successfully attached", ignoreCase = true)
                           .replace("foto produk!", "product photos!", ignoreCase = true)
        } else if (result.startsWith("Gagal menyimpan pengeluaran:", ignoreCase = true)) {
            result = "Failed to save expense: " + result.substringAfter("Gagal menyimpan pengeluaran:")
        } else if (result.startsWith("Berhasil mencetak ke printer:", ignoreCase = true)) {
            result = "Successfully printed to: " + result.substringAfter("Berhasil mencetak ke printer:")
        }

        // 3. Sentence-level replacement using word boundaries to prevent substring corruption
        sortedEnEntries.forEach { (indo, eng) ->
            if (indo.length > 3) {
                val prefixPattern = "(?<=^|[^a-zA-Z0-9_])"
                val suffixPattern = "(?=$|[^a-zA-Z0-9_])"
                val regex = Regex(prefixPattern + Regex.escape(indo) + suffixPattern, RegexOption.IGNORE_CASE)
                result = result.replace(regex, eng)
            }
        }
        return result
    }
}
