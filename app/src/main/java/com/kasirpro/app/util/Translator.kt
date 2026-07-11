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
        "Gagal menyimpan pengeluaran:" to "Failed to save expense:",
        "Mulai Transaksi Pertama Anda" to "Start Your First Transaction",
        "Setup Toko Baru Anda" to "Set Up Your New Store",
        "Tambahkan info detail operasional toko utama Anda" to "Add operational details of your main store",
        "Logo berhasil diupload ke Storage!" to "Logo successfully uploaded to Storage!",
        "Silakan isi nama toko dan alamat!" to "Please fill in store name and address!",
        "Daftar Toko Kasir Pro" to "Register Kasir Pro Store",
        "Mulailah melayani transaksi bisnis dalam beberapa klik" to "Start serving business transactions in a few clicks",
        "Harap lengkapi semua data formulir!" to "Please complete all form data!",
        "Password minimal terdiri dari 6 karakter!" to "Password must be at least 6 characters!",
        "Belum memiliki akun? " to "Don't have an account? ",
        "Sudah memiliki akun? " to "Already have an account? ",
        "Masuk Kasir Pro" to "Log In to Kasir Pro",
        "Kelola finansial toko Anda dengan mudah" to "Manage your store's financials easily",
        "Google login gagal." to "Google login failed.",
        "Gagal mendapatkan token Google atau login dibatalkan." to "Failed to get Google token or login cancelled.",
        "Mohon lengkapi email dan password!" to "Please complete email and password!",
        "Email atau password salah!" to "Incorrect email or password!",
        "Inisialisasi Google Sign-In gagal." to "Google Sign-In initialization failed.",
        "Registrasi gagal, silakan coba lagi." to "Registration failed, please try again.",
        "Lupa Password" to "Forgot Password",
        "Masukkan email terdaftar Anda dan kami akan mengirimkan link reset password." to "Enter your registered email and we will send you a password reset link.",
        "Kembali ke Login" to "Back to Login",
        "Toko Kasir Pro" to "Kasir Pro Store",
        "Tekan & geser untuk detail interaktif" to "Press & drag for interactive details",
        "Tambah Produk ke Struk" to "Add Product to Receipt",
        "Tambah Akun Kasir" to "Add Cashier Account",
        "Tambah Hadiah" to "Add Reward",
        "Tambah Kasir" to "Add Cashier",
        "Tambah Outlet Cabang Baru" to "Add New Outlet Branch",
        "Tambah Pengeluaran" to "Add Expense",
        "Tambah akun kasir pertama Anda melalui tombol di atas untuk mendelegasikan transaksi kasir." to "Add your first cashier account using the button above to delegate cashier transactions.",
        "Teoritis Seharusnya (Sistem):" to "Theoretical (System):",
        "Uang Fisik Dilaporkan:" to "Physical Cash Reported:",
        "Uang kasir Tunai secara sistem yang harus ada di laci saat ini:" to "System cashier cash that should be in the drawer currently:",
        "Upgrade Premium (Mulai Rp 29rb)" to "Upgrade Premium (Starting at Rp 29k)",
        "Upgrade ke Paket Lebih Tinggi" to "Upgrade to a Higher Package",
        "Username / ID Login (Unik)" to "Username / Login ID (Unique)",
        "Username tersedia & siap digunakan!" to "Username is available & ready to use!",
        "Username tersedia!" to "Username is available!",
        "Username tidak tersedia atau sedang divalidasi. Silakan periksa kembali!" to "Username is unavailable or being validated. Please check again!",
        "Tugaskan Kasir ke Cabang" to "Assign Cashier to Branch",
        "Tugaskan ke Cabang:" to "Assign to Branch:",
        "TOTAL PENDAPATAN" to "TOTAL REVENUE",
        "TOTAL PIUTANG HAMPIR JATUH TEMPO" to "TOTAL RECEIVABLES NEARING DUE DATE",
        "TOTAL TRANSAKSI" to "TOTAL TRANSACTIONS",
        "Total Beban / Pengeluaran Toko" to "Total Store Expenses / Fees",
        "Total Harga Pokok Modal (HPP):" to "Total Cost of Goods Sold (COGS):",
        "Total Harga Pokok Penjualan (HPP)" to "Total Cost of Goods Sold (COGS)",
        "Total Omset Penjualan" to "Total Sales Turnover",
        "Total Pengeluaran (Beban):" to "Total Expense (Cost):",
        "Toko Utama" to "Main Store",
        "Tidak ada notifikasi untuk Anda" to "No notifications for you",
        "Scan Barcode Simulator" to "Scan Barcode Simulator",
        "Apply Code" to "Apply Code",
        "Restock baru dari supplier" to "New restock from supplier",
        "Tambah Manual Stok" to "Add Stock Manually",
        "Jumlah unit masuk" to "Quantity received",
        "Catatan riwayat" to "History notes",
        "Tambahkan" to "Add",
        "Opname stok akhir bulan" to "End of month stock audit",
        "Stok Opname Fisik" to "Physical Stock Audit",
        "Stok fisik nyata di rak" to "Actual physical stock on shelf",
        "Keterangan opname" to "Audit description",
        "Simpan & Sesuaikan" to "Save & Adjust",
        "Edit Detail Produk" to "Edit Product Details",
        "Ganti atau Tambah Foto Produk" to "Change or Add Product Photo",
        "Barcode (Optional)" to "Barcode (Optional)",
        "Menyimpan perubahan dan foto..." to "Saving changes and photo...",
        "Upload Produk Massal (.xlsx / .csv)" to "Bulk Upload Products (.xlsx / .csv)",
        "1. Pilih Cabang Toko Tujuan" to "1. Select Target Store Branch",
        "Pilih Cabang..." to "Select Branch...",
        "2. Unduh Template Resmi" to "2. Download Official Template",
        "Download Template" to "Download Template",
        "3. Pilih Spreadsheet (.xlsx / .csv)" to "3. Select Spreadsheet (.xlsx / .csv)",
        "Pilih File Excel / CSV" to "Select Excel / CSV File",
        "4. Foto Produk Langsung Di Dalam Spreadsheet" to "4. Product Photos Directly In Spreadsheet",
        "Foto produk wajib disatukan di dalam file Excel / CSV:" to "Product photos must be included in the Excel / CSV file:",
        "Mengunggah Produk Massal" to "Uploading Bulk Products",
        "Memulai bulk upload..." to "Starting bulk upload...",
        "Nama produk wajib diisi." to "Product name is required.",
        "Kategori wajib diisi." to "Category is required.",
        "Harga jual harus angka valid > 0." to "Selling price must be a valid number > 0.",
        "Stok awal harus angka." to "Initial stock must be a number.",
        "Stok awal tidak boleh negatif." to "Initial stock cannot be negative.",
        "Alat Tulis" to "Stationery",
        "Printer Tanpa Nama" to "Unnamed Printer",
        "Mulai Shift Kasir" to "Start Cashier Shift",
        "Masukkan jumlah saldo uang modal awal (cash) yang tersedia di laci uang kasir untuk memulai pencatatan shift hari ini." to "Enter the initial capital cash amount available in the cashier drawer to start recording today's shift.",
        "Modal Awal (Rp)" to "Initial Capital (Rp)",
        "Contoh: 50000" to "Example: 50000",
        "Mulai Shift POS" to "Start POS Shift",
        "Kasir Pos Penjualan" to "POS Cashier Sales",
        "Menu Kasir" to "Cashier Menu",
        "Catat Pengeluaran" to "Record Expense",
        "Bayar Sekarang" to "Pay Now",
        "Cari produk atau barcode..." to "Search product or barcode...",
        "Produk Kosong" to "No Products",
        "Silakan tambahkan produk baru di menu Kelola Produk terlebih dahulu." to "Please add a new product in the Manage Products menu first.",
        "Pilih Varian Produk" to "Select Product Variant",
        "Pindai Barcode Produk" to "Scan Product Barcode",
        "Masukkan manual kode barcode produk untuk memulai pencarian produk secara cepat." to "Manually enter product barcode to start quick product search.",
        "contoh: 899123456001" to "e.g., 899123456001",
        "Simpan & Scan" to "Save & Scan",
        "Konfirmasi Pembayaran Kasir" to "Confirm Cashier Payment",
        "Ringkasan Pesanan" to "Order Summary",
        "Promo & Loyalty" to "Promo & Loyalty",
        "Add Customer" to "Add Customer",
        "Jumlah poin yang ditukar" to "Points to redeem",
        "Masukkan poin, misal: 10" to "Enter points, e.g., 10",
        "Metode Status Piutang" to "Receivable Status Method",
        "DP / PIUTANG" to "DP / RECEIVABLE",
        "Wajib memilih pelanggan jika status adalah Hutang (DP)" to "Selecting a customer is required if status is Debt (DP)",
        "SCAN QRIS TOKO" to "SCAN STORE QRIS",
        "Silakan pelanggan scan QRIS di atas untuk membayar sejumlah:" to "Please have the customer scan the QRIS above to pay:",
        "Foto QRIS Belum Tersedia" to "QRIS Photo Not Available Yet",
        "Pemilik toko harus meng-upload QRIS di menu Pengaturan terlebih dahulu." to "The store owner must upload a QRIS in the Settings menu first.",
        "Nominal Pembayaran Tunai" to "Cash Payment Amount",
        "Kembalian Anda:" to "Your Change:",
        "Sisa Piutang (Kekurangan):" to "Remaining Receivable (Shortage):",
        "Masukkan data pelanggan untuk dihubungkan ke program loyalty & bonus poin warung." to "Enter customer details to connect to the store's loyalty & bonus points program.",
        "Nama Lengkap*" to "Full Name*",
        "Contoh: Ahmad Budiman" to "Example: Ahmad Budiman",
        "Nama wajib diisi" to "Name is required",
        "No. WhatsApp/HP*" to "WhatsApp/HP Number*",
        "Contoh: 08123456789" to "Example: 08123456789",
        "Nomor WA wajib diisi" to "WA number is required",
        "Alamat (Opsional)" to "Address (Optional)",
        "Contoh: Jl. Merdeka No. 10" to "Example: 10 Merdeka St.",
        "Gunakan Voucher Kupon Promo" to "Use Promo Voucher Coupon",
        "Database voucher promo kosong." to "Promo voucher database is empty.",
        "Transaksi Berhasil!" to "Transaction Successful!",
        "Terima kasih atas kunjungan Anda!" to "Thank you for your visit!",
        "Bagikan struk penjualan" to "Share sales receipt",
        "Bagikan WA" to "Share via WA",
        "Cetak Kasir" to "Print Cashier",
        "Hubungkan Printer Thermal Bluetooth" to "Connect Bluetooth Thermal Printer",
        "Pastikan printer Anda menyala dan telah dipasangkan (paired) di pengaturan Bluetooth HP." to "Make sure your printer is on and paired in your phone's Bluetooth settings.",
        "Mencetak struk..." to "Printing receipt...",
        "Tidak ada printer Bluetooth berpasangan ditemukan." to "No paired Bluetooth printer found.",
        "Muat Ulang / Cari Printer" to "Reload / Search Printer",
        "Pilih printer dari daftar di bawah ini:" to "Select a printer from the list below:",
        "Simulasi Cetak (Bypass)" to "Simulate Printing (Bypass)",
        "Gunakan jika tidak ada printer berpasangan" to "Use if there is no paired printer",
        "Pilih transaksi untuk melihat detail struk atau melakukan perbaikan menggunakan authorization PIN pemilik toko." to "Select a transaction to view receipt details or make corrections using the shop owner's authorization PIN.",
        "Masukkan kode otoritas/sandi pemilik warung untuk mengizinkan kasir mengedit data transaksi struk ini." to "Enter the store owner's authority code/password to allow the cashier to edit this transaction's receipt data.",
        "Uang Modal Awal:" to "Initial Capital:",
        "Pendapatan Tunai:" to "Cash Revenue:",
        "Pendapatan Non-Tunai:" to "Non-Cash Revenue:",
        "Total Omset:" to "Total Turnover:",
        "Tunai Seharusnya di Laci:" to "Cash Should Be in Drawer:",
        "Jumlah Uang Fisik Laci:" to "Physical Cash Amount in Drawer:",
        "Log Transaksi Shift ini:" to "Transaction Log for This Shift:",
        "Tidak ada transaksi selama shift ini." to "No transactions during this shift.",
        "Konfirmasi & Keluar" to "Confirm & Exit",
        "Layanan Aktif" to "Service Active",
        "Tingkatkan durasi langganan sebelum berakhir" to "Extend subscription duration before expiration",
        "Perpanjang" to "Extend",
        "Fitur Bisnis & Loyalitas (Premium)" to "Business & Loyalty Features (Premium)",
        "Loyalty Pelanggan & CRM" to "Customer Loyalty & CRM",
        "Manajemen Promo & Kupon" to "Promo & Coupon Management",
        "Outlet Multi-Cabang" to "Multi-Branch Outlets",
        "Staf & Akun Kasir" to "Staff & Cashier Accounts",
        "Pengaturan Aplikasi" to "App Settings",
        "Kode Otoritas Koreksi" to "Correction Authority Code",
        "Menghubungkan ke Google Play..." to "Connecting to Google Play...",
        "Laporan Akhir Shift" to "End of Shift Report",
        "Laporan PDF & Excel" to "PDF & Excel Reports",
        "Daftar Cabang Usaha" to "List of Business Branches",
        "Tambah Cabang" to "Add Branch",
        "Cabang Utama (Default)" to "Main Branch (Default)",
        "Template Excel berhasil didownload!" to "Excel template downloaded successfully!",
        "Tidak ada rekaman hutang." to "No debt records.",
        "Transaksi Terbaru & Koreksi" to "Recent Transactions & Corrections",
        "TAHUNAN (365 h)" to "YEARLY (365 d)",
        "BULANAN (30 h)" to "MONTHLY (30 d)",
        "Belum Ada Sesi" to "No Sessions Yet",
        "Jumlah Transaksi:" to "Transaction Count:",
        "GRATIS" to "FREE",
        "Profil" to "Profile",
        "Pengaturan & Profil" to "Settings & Profile",
        "Profil Kasir & Shift" to "Cashier Profile & Shift",
        "Fitur Pro Terkunci" to "Pro Feature Locked",
        "Menu Laporan Keuangan, dan Kelola Hutang hanya tersedia untuk pelanggan Premium Pro." to "Financial Reports and Debt Management menus are only available for Premium Pro Customers.",
        "Kembali ke Beranda" to "Back to Home",
        "Produk Belum Ada" to "No Products Yet",
        "Klik tombol '+' di pojok kanan atas untuk menambahkan produk jualan utama Anda!" to "Click the '+' button in the top right corner to add your main sales products!",
        "Search Products atau Barcode..." to "Search products or barcode...",
        "Buka batasan laporan, outlet cabang, backup data, dan kelola kasir tanpa batas dengan paket premium." to "Unlock unlimited reports, outlet branches, backup data, and cashier management with premium package.",

        // --- NEW DETAILED TRANSLATIONS ---
        "Pilih Paket Langganan" to "Select Subscription Plan",
        "Pilih Paket Langgganan" to "Select Subscription Plan",
        "Pilih Paket Langganan:" to "Select Subscription Plan:",
        "Langganan langsung via Google Play untuk membuka seluruh fitur premium." to "Subscribe directly via Google Play to unlock all premium features.",
        "Langganan langsung via Google Play untuk membuka seluruh fitur Premium." to "Subscribe directly via Google Play to unlock all Premium features.",
        "PAKET DASAR" to "BASIC PACKAGE",
        "PAKET PROFESIONAL" to "PROFESSIONAL PACKAGE",
        "PAKET BISNIS" to "BUSINESS PACKAGE",
        "Rp 50.000 / bln • Rp 500.000 / thn" to "IDR 50,000 / month • IDR 500,000 / year",
        "Rp 100.000 / bln • Rp 1.000.000 / thn" to "IDR 100,000 / month • IDR 1,000,000 / year",
        "Rp 150.000 / bln • Rp 1.500.000 / thn" to "IDR 150,000 / month • IDR 1,500,000 / year",
        "Maksimal 50 produk" to "Maximum of 50 products",
        "Transaksi tidak terbatas" to "Unlimited transactions",
        "1 Kasir" to "1 Cashier",
        "Laporan harian & mingguan" to "Daily & weekly reports",
        "Semua metode bayar" to "All payment methods",
        "Scan barcode aktif" to "Active barcode scanning",
        "Foto produk aktif" to "Active product photos",
        "Struk WhatsApp aktif" to "Active WhatsApp receipts",
        "Maksimal 3 cabang" to "Maximum of 3 branches",
        "Maksimal 5 kasir" to "Maximum of 5 cashiers",
        "Laporan bulanan & tahunan" to "Monthly & yearly reports",
        "Hutang & database pelanggan" to "Debt & customer database",
        "Semua fitur Dasar" to "All Basic features",
        "Produk & transaksi tidak terbatas" to "Unlimited products & transactions",
        "Maksimal 3 cabang & 5 kasir" to "Maximum 3 branches & 5 cashiers",
        "Hutang & database pelanggan aktif" to "Active debt & customer database",
        "Diskon & promo aktif" to "Active discount & promo",
        "Bulk upload Excel & Varian produk" to "Bulk Excel upload & product variants",
        "Laporan shift kasir aktif" to "Active cashier shift reports",
        "Backup otomatis" to "Automatic backup",
        "Laporan gabungan semua cabang" to "Combined reports of all branches",
        "Semua fitur Profesional" to "All Professional features",
        "Cabang & kasir tidak terbatas" to "Unlimited branches & cashiers",
        "Backup otomatis aktif" to "Active automatic backup",
        "Bulanan" to "Monthly",
        "Tahunan" to "Yearly",
        "PALING POPULER" to "MOST POPULAR",
        "Koneksi Online" to "Online Connection",
        "Mode Offline" to "Offline Mode",
        "Halo" to "Hello",
        "Selamat bekerja" to "Have a great workday",
        "Grafik Keuangan (7 Hari Terakhir)" to "Financial Chart (Last 7 Days)",
        "Tekan & geser untuk detail interaktif" to "Press & drag for interactive details",
        "Directory Pelanggan Loyal" to "Loyal Customer Directory",
        "Pelanggan Baru" to "New Customer",
        "Aturan Loyalty & Poin CRM" to "Loyalty Rules & CRM Points",
        "Belanja per Poin (Rp)" to "Purchase per Point (IDR)",
        "Nilai 1 Poin (Rp)" to "1 Point Value (IDR)",
        "Simpan Aturan" to "Save Rules",
        "Katalog Hadiah Loyalty (Tukar Hadiah)" to "Loyalty Reward Catalog (Redeem Reward)",
        "Katalog Hadiah untuk Ditukar Poin:" to "Reward Catalog to Redeem with Points:",
        "Belum ada pelanggan terdaftar." to "No registered customers yet.",
        "Daftarkan Pelanggan Baru" to "Register New Customer",
        "Nama Pelanggan" to "Customer Name",
        "No Handphone (WhatsApp)" to "Phone Number (WhatsApp)",
        "Daftar" to "Register",
        "Daftarkan" to "Register",
        "Batal" to "Cancel",
        "Tambah" to "Add",
        "Poin" to "Points",
        "Min" to "Sun",
        "Sen" to "Mon",
        "Sel" to "Tue",
        "Rab" to "Wed",
        "Kam" to "Thu",
        "Jum" to "Fri",
        "Sab" to "Sat",
        "Google Play Billing Belum Siap / Emulator" to "Google Play Billing Not Ready / Emulator",
        "Aplikasi berjalan di lingkungan non-Play Store. Anda dapat menggunakan mode simulasi di bawah untuk menguji integrasi." to "App is running in a non-Play Store environment. You can use the simulation mode below to test integration.",
        "Play Store Billing tidak aktif atau produk belum termuat." to "Play Store Billing is inactive or products have not loaded.",

        // --- PREMIUM PRO & SUBSCRIPTION EXTRA TRANSLATIONS ---
        "Harian" to "Daily",
        "Mingguan" to "Weekly",
        "Bulanan" to "Monthly",
        "HARIAN" to "DAILY",
        "MINGGUAN" to "WEEKLY",
        "BULANAN" to "MONTHLY",
        "LAPORAN" to "REPORTS",
        "HUTANG" to "DEBT",
        "CRM POIN" to "CRM POINTS",
        "INFORMASI PEMBARUAN APLIKASI" to "APPLICATION UPDATE INFORMATION",
        "Harap unduh pembaruan APK dari server penyedia resmi untuk performa terbaik dan perlindungan fitur baru." to "Please download the APK update from the official provider server for best performance and new feature protection.",
        "Gagal membuka link download!" to "Failed to open download link!",
        "Unduh APK Baru" to "Download New APK",
        "Kembali ke Daftar Notifikasi" to "Back to Notification List",
        "Notifikasi Broadcast" to "Broadcast Notifications",
        "Semua notifikasi ditandai dibaca" to "All notifications marked as read",
        "Tidak ada notifikasi untuk Anda" to "No notifications for you",
        "Laba Bersih Finansial:" to "Net Financial Profit:",
        "Laba Bersih Finansial" to "Net Financial Profit",
        "Rata-Rata" to "Average",
        "Rata-Rata Transaksi" to "Average Transactions",
        "Staf Kasir Terhubung" to "Connected Cashier Staff",
        "Belum ada kasir terdaftar" to "No cashiers registered yet",
        "Tambah akun kasir pertama Anda melalui tombol di atas untuk mendelegasikan transaksi kasir." to "Add your first cashier account using the button above to delegate cashier transactions.",
        "AKTIF" to "ACTIVE",
        "Dibuat:" to "Created:",
        "Belum dialokasikan" to "Not allocated yet",
        "Atur Cabang" to "Assign Branch",
        "Akun kasir berhasil dihapus" to "Cashier account deleted successfully",
        "Tambah Akun Kasir" to "Add Cashier Account",
        "Belum Lunas" to "Unpaid",
        "Sudah Lunas" to "Paid",
        "LUNASKAN" to "SETTLE",
        "Ref Transaksi ID:" to "Ref Transaction ID:",
        "Total Poin" to "Total Points",
        "Sesi Belanja" to "Shopping Sessions",
        "Penyesuaian" to "Adjustment",
        "Tukar Hadiah" to "Redeem Rewards",
        "Koreksi / Bonus Poin Manual" to "Manual Correction / Bonus Points",
        "Jumlah poin, misal: 10 atau -5" to "Number of points, e.g., 10 or -5",
        "Sesuaikan" to "Adjust",
        "Kirim Broadcast Point CRM" to "Send CRM Point Broadcast",
        "Share Promo & Poin WA" to "Share Promo & Points via WA",
        "Pilih Hadiah Poin Pelanggan:" to "Select Customer Point Reward:",
        "Tidak ada hadiah tersedia di katalog." to "No rewards available in the catalog.",
        "Tutup" to "Close",
        "Poin berhasil diperbarui!" to "Points updated successfully!",
        "Aturan CRM berhasil disimpan!" to "CRM Rules saved successfully!",
        "Memuat daftar hadiah..." to "Loading reward list...",
        "Hadiah berhasil dihapus!" to "Reward deleted successfully!",
        "Hadiah berhasil ditambahkan!" to "Reward added successfully!",
        "Kolom tidak valid!" to "Invalid fields!",
        "Edit Hadiah" to "Edit Reward",
        "Hadiah berhasil diperbarui!" to "Reward updated successfully!",
        "Daftar Cabang Usaha" to "Business Branches List",
        "Tambah Cabang" to "Add Branch",
        "Cabang Utama (Default)" to "Main Branch (Default)",
        "Anda belum mendaftarkan cabang tambahan. Tambahkan cabang outlet baru untuk memantau data kasir terpisah secara realtime!" to "You haven't registered additional branches. Add new outlet branches to monitor separate cashiers in realtime!",
        "Belum ada kasir yang ditugaskan ke cabang ini." to "No cashier assigned to this branch yet.",
        "Tambah Outlet Cabang Baru" to "Add New Outlet Branch",
        "Daftarkan cabang usaha baru Anda untuk manajemen produk dan alokasi kasir terpisah." to "Register your new business branch for separate product management and cashier allocation.",
        "Nama Cabang" to "Branch Name",
        "Alamat lengkap" to "Full Address",
        "contoh: Cabang Bandung" to "e.g., Bandung Branch",
        "contoh: Jl. Merdeka No. 45, Bandung" to "e.g., Jl. Merdeka No. 45, Bandung",
        "Daftar Cabang" to "Register Branch",
        "Edit Outlet Cabang" to "Edit Outlet Branch",
        "Simpan Perubahan" to "Save Changes",

        // --- NEW TRANS-KEYS FOR FULL COVERAGE ---
        "Area Premium Pro" to "Premium Pro Area",
        "Filter Laporan" to "Report Filters",
        "Berdasarkan Cabang:" to "Based on Branch:",
        "Berdasarkan Kasir / Staf:" to "Based on Cashier / Staff:",
        "Semua Cabang" to "All Branches",
        "Semua Kasir" to "All Cashiers",
        "RATA-RATA TRANSAKSI" to "AVERAGE TRANSACTION",
        "Breakdown Omset Akhir Bulan" to "End of Month Revenue Breakdown",
        "Berikut rangkuman performa omset per Cabang dan per Staf Kasir kontributor saat ini." to "Here is a summary of the current revenue performance per branch and contributor cashier staff.",
        "Berikut rangkuman performa omset per Branches dan per Staf Cashier kontributor saat ini." to "Here is a summary of the current revenue performance per branch and contributor cashier staff.",
        "Penjualan Per Cabang:" to "Sales Per Branch:",
        "Penjualan Per Branch:" to "Sales Per Branch:",
        "Belum ada omset penjualan cabang." to "No branch sales revenue yet.",
        "Belum ada omset penjualan Cabang." to "No branch sales revenue yet.",
        "Belum ada omset penjualan Branches." to "No branch sales revenue yet.",
        "Penjualan Per Staf Kasir:" to "Sales Per Cashier Staff:",
        "Penjualan Per Staf Cashier:" to "Sales Per Cashier Staff:",
        "Belum ada omset penjualan kasir." to "No cashier sales revenue yet.",
        "Belum ada omset penjualan Kasir." to "No cashier sales revenue yet.",
        "Riwayat Laporan Shift Kasir" to "Historical Cashier Shift Reports",
        "Pantau uang modal awal, omset tunai/non-tunai, dan pencocokan uang kas fisik di laci per sesi kasir langsung dari Firestore." to "Monitor initial capital, cash/non-cash revenue, and physical cash matching in drawer per cashier session directly from Firestore.",
        "Pantau Initial Capital, omset Cash/Non-Cash, dan pencocokan uang kas fisik di laci per sesi Cashier langsung dari Firestore." to "Monitor initial capital, cash/non-cash revenue, and physical cash matching in drawer per cashier session directly from Firestore.",
        "Belum ada pertanggungjawaban shift kasir tercatat sesuai filter saat ini." to "No recorded cashier shift accountability matches the current filters.",
        "Belum ada pertanggungjawaban shift Cashier tercatat sesuai filter saat ini." to "No recorded cashier shift accountability matches the current filters.",
        "Grafik Pendapatan Harian (7 Hari Terakhir)" to "Daily Revenue Chart (Last 7 Days)",
        "Grafik Pendapatan Daily (7 Hari Terakhir)" to "Daily Revenue Chart (Last 7 Days)",
        "Grafik Produk Terlaris (Qty)" to "Best Selling Products Chart (Qty)",
        "Grafik Best Selling Products (Qty)" to "Best Selling Products Chart (Qty)",
        "Belum ada data produk terlaris di interval ini." to "No best selling products data in this interval.",
        "Belum ada data Best Selling Products di interval ini." to "No best selling products data in this interval.",
        "Export PDF Laporan Kasir" to "Export Cashier PDF Report",
        "Export PDF Reports Cashier" to "Export Cashier PDF Report",
        "Omset Tunai (Cash):" to "Cash Revenue:",
        "Omset Non-Tunai:" to "Non-Cash Revenue:",
        "Selisih Kas Laci Fisik:" to "Physical Drawer Cash Difference:",
        "Riwayat Laporan Shift Kasir (Terkunci)" to "Historical Cashier Shift Reports (Locked)",
        "+ Tambah Pelanggan Baru" to "+ Add New Customer",
        "Aplikasi Kasir Modern Indonesia" to "Modern Indonesian Cashier Application",
        "Backup Data" to "Backup Data",
        "Belum Ada Mutasi" to "No Mutations/Transactions Yet",
        "Belum pernah aktif" to "Never active",
        "Buat Akun" to "Create Account",
        "Buat Kode Aktivasi Baru" to "Create New Activation Code",
        "Buat langsung kredensial masuk kasir tanpa undangan email." to "Create cashier login credentials directly without email invitation.",
        "Buka Kasir" to "Open Cashier",
        "Catat pengeluaran finansial / biaya beban operasional toko Anda di bawah ini." to "Record store financial expenditures / operational expenses below.",
        "Catat penjualan kasir secepat kilat dengan pencarian pintar dan camera scan barcode otomatis." to "Record cashier sales at lightning speed with smart search and automatic camera barcode scanning.",
        "Cloud Backup Google Drive otomatis/manual hanya didukung untuk tipe pelanggan premium!" to "Cloud Backup to Google Drive (automatic/manual) is only supported for premium accounts!",
        "Context activity tidak valid" to "Invalid activity context",
        "Contoh: Bayar Listrik, Gaji Karyawan, Sewa Tempat, dll." to "E.g., Electricity, Employee Salary, Rent, etc.",
        "Contoh: Belanja es batu, Listrik" to "E.g., Ice cubes shopping, Electricity",
        "Daftar Riwayat Notifikasi Broadcast" to "Broadcast Notification History List",
        "Daftar Toko Baru" to "Register New Store",
        "Data RESTORE hanya didukung untuk akun premium!" to "Data RESTORE is only supported for premium accounts!",
        "ESTIMASI LABA BERSIH" to "ESTIMATED NET PROFIT",
        "Edit Akun Kasir" to "Edit Cashier Account",
        "Edit Cabang" to "Edit Branch",
        "Edit Kasir" to "Edit Cashier",
        "Elektronik" to "Electronics",
        "Fitur Kode Unik Otoritas Koreksi hanya untuk pengguna Premium. Upgrade sekarang!" to "Authority Code feature for corrections is only for Premium users. Upgrade now!",
        "Fitur Koreksi Transaksi hanya tersedia untuk pengguna Premium Pro. Upgrade sekarang!" to "Transaction Correction feature is only available for Premium Pro users. Upgrade now!",
        "Fitur riwayat laporan shift kasir hanya tersedia untuk minimal Paket Profesional. Klik untuk upgrade sekarang!" to "Cashier shift report history feature requires at least the Professional Plan. Click to upgrade now!",
        "Form Broadcast Baru" to "New Broadcast Form",
        "Foto QRIS berhasil disimpan!" to "QRIS Photo successfully saved!",
        "Gagal membuat kode!" to "Failed to create code!",
        "Gagal memproses dokumen PDF!" to "Failed to process PDF document!",
        "Gagal menyinkronkan kode." to "Failed to sync code.",
        "Generate Kode Aktivasi" to "Generate Activation Code",
        "Hapus Broadcast" to "Delete Broadcast",
        "Hapus Cabang" to "Delete Branch",
        "Hapus Kasir" to "Delete Cashier",
        "I. RINGKASAN FINANSIAL OUTLET" to "I. OUTLET FINANCIAL SUMMARY",
        "ID Pengguna (UID) wajib diisi!" to "User ID (UID) is required!",
        "II. DAFTAR PERINGKAT PRODUK TERLARIS (QTY)" to "II. BEST-SELLING PRODUCTS RANKING LIST (QTY)",
        "Internet mati? Transaksi tetap berjalan normal secara offline dan otomatis sync saat online kembali." to "Offline? Transactions continue normally and automatically sync when online.",
        "Jumlah nominal pengeluaran harus valid!" to "Expense amount must be valid!",
        "KASIR PRO" to "KASIR PRO",
        "KASIR PRO - LAPORAN KEUANGAN" to "KASIR PRO - FINANCIAL REPORT",
        "KASIR PRO SHOP" to "KASIR PRO SHOP",
        "KUANTITAS TERJUAL" to "QUANTITY SOLD",
        "Kasir Aktif" to "Active Cashier",
        "Kasir Pro" to "Kasir Pro",
        "Kasir Pro Logo" to "Kasir Pro Logo",
        "Kasir Pro Primary Icon" to "Kasir Pro Primary Icon",
        "Keterangan Operasional *" to "Operational Details *",
        "Keterangan pengeluaran tidak boleh kosong!" to "Expense details cannot be empty!",
        "Kirim Broadcast Notifikasi ke Semua Pengguna" to "Send Broadcast Notification to All Users",
        "Kode Otoritas disimpan!" to "Authority Code saved!",
        "Kode dihapus!" to "Code deleted!",
        "Kode tidak boleh kosong!" to "Code cannot be empty!",
        "Koreksi transaksi berhasil diperbarui!" to "Transaction correction successfully updated!",
        "LUNAS" to "PAID",
        "Lainnya" to "Others",
        "Langganan Anda telah berakhir. Perpanjang sekarang" to "Your subscription has expired. Renew now",
        "Lihat Laporan" to "View Report",
        "Lihat Semua" to "View All",
        "Logo berhasil disimpan!" to "Logo successfully saved!",
        "Lupa Password?" to "Forgot Password?",
        "Maaf Username Tidak Tersedia!" to "Sorry, Username is Not Available!",
        "Makanan" to "Food",
        "Manajemen Cabang & Stok" to "Branch & Stock Management",
        "Memproses pengiriman broadcast..." to "Processing broadcast sending...",
        "Minimal 6 karakter" to "Minimum 6 characters",
        "Minuman" to "Beverage",
        "MissingPermission" to "Missing Permission",
        "Mulai Simpan" to "Start Saving",
        "Mutasi penyesuaian stok" to "Stock adjustment mutation",
        "NAMA BARANG / PRODUK" to "ITEM / PRODUCT NAME",
        "Nama Kasir" to "Cashier Name",
        "Nama toko tidak boleh kosong!" to "Store name cannot be empty!",
        "PAS" to "EXACT",
        "Pakaian" to "Clothing",
        "Pantau persediaan stok minimum dan kelola banyak cabang usaha secara realtime langsung dari genggaman Anda." to "Monitor minimum stock levels and manage multiple branches in real-time from your hand.",
        "Password" to "Password",
        "Password Baru / Ubah" to "New / Change Password",
        "Password Kasir" to "Cashier Password",
        "Password minimal 6 karakter!" to "Password must be at least 6 characters!",
        "Pengeluaran berhasil dicatat!" to "Expense successfully recorded!",
        "Perbarui nama, login ID (username), dan password untuk kasir Anda." to "Update name, login ID (username), and password for your cashier.",
        "Peringatan Kedaluwarsa Produk!" to "Product Expiration Warning!",
        "Pilih / Tambah Pelanggan Sekarang" to "Select / Add Customer Now",
        "Pilih Pelanggan Loyalty Hub" to "Select Loyalty Hub Customer",
        "Pilih Siklus Tagihan:" to "Select Billing Cycle:",
        "Pilih Tanggal Kedaluwarsa" to "Select Expiration Date",
        "Poin tidak cukup!" to "Not enough points!",
        "Restore Data" to "Restore Data",
        "Satuan" to "Unit",
        "Selesai" to "Finished",
        "Seluruh log penjualan kasir dan penyesuaian resupply tercatat lengkap di riwayat ini." to "All cashier sales logs and resupply adjustments are fully recorded in this history.",
        "Sembunyikan Sesi Shift Kasir" to "Hide Cashier Shift Session",
        "Semua data lokal berhasil dipulihkan dari Server Cloud!" to "All local data successfully restored from Cloud Server!",
        "Semua kolom formulir harus diisi!" to "All form fields must be filled!",
        "Semua kolom harus diisi!" to "All fields must be filled!",
        "Semua kolom wajib diisi!" to "All fields are required!",
        "Silakan pilih foto QRIS terlebih dahulu" to "Please select a QRIS photo first",
        "Simpan Akun" to "Save Account",
        "Simulasi gagal" to "Simulation failed",
        "Sinkronkan Kode ke Akun User" to "Sync Code to User Account",
        "Status Aktif" to "Active Status",
        "Stok Opname fisik otomatis dengan sinkronisasi penyesuaian (Selisih Hitung) hanya tersedia untuk premium!" to "Physical Stock Opname with adjustment synchronization (Count Difference) is only available for premium!",
        "Struk tidak boleh kosong! Hapus transaksi jika ingin membatalkan." to "Receipt cannot be empty! Delete transaction if you want to cancel.",
        "Tambah Pelanggan" to "Add Customer",
        "Tambah Pelanggan Baru" to "Add New Customer",
        "Tambah Produk" to "Add Product",
        "Tidak Terbatas" to "Unlimited",
        "Tipe Notifikasi:" to "Notification Type:",
        "Transaksi Instan & Barcode" to "Instant & Barcode Transactions",
        "Update" to "Update",
        "belum" to "not yet",
        "contoh: Budi Santoso" to "e.g., Budi Santoso",
        "contoh: budi_kasir" to "e.g., budi_cashier",
        "jumlah" to "amount",
        "lunas" to "paid"
    )

    private val sortedEnEntries = enMap.entries.sortedByDescending { it.key.length }
    private val enMapNormalized = enMap.mapKeys { it.key.replace(Regex("\\s+"), " ").trim().lowercase() }

    private val translationCache = java.util.concurrent.ConcurrentHashMap<String, String>()

    private val compiledPatterns: List<Pair<Regex, String>> by lazy {
        sortedEnEntries.filter { it.key.length > 3 }.map { (indo, eng) ->
            val prefixPattern = "(?<=^|[^a-zA-Z0-9_])"
            val suffixPattern = "(?=$|[^a-zA-Z0-9_])"
            Regex(prefixPattern + Regex.escape(indo) + suffixPattern, RegexOption.IGNORE_CASE) to eng
        }
    }

    fun translate(text: String, lang: String): String {
        if (lang != "en") return text
        val cacheKey = "$lang::$text"
        translationCache[cacheKey]?.let { return it }
        
        val trimmed = text.trim()
        val normalized = trimmed.replace(Regex("\\s+"), " ").lowercase()
        
        // 1. Exact match lookup using whitespace-normalized lowercase
        val exactMatch = enMapNormalized[normalized]
        if (exactMatch != null) {
            translationCache[cacheKey] = exactMatch
            return exactMatch
        }
        
        // 2. Handle pattern matches cleanly by translating substrings step by step
        var result = trimmed

        if (result.endsWith(" Transaksi", ignoreCase = true)) {
            val count = result.substringBeforeLast(" Transaksi")
            result = "$count Transactions"
        } else if (result.endsWith(" Akun", ignoreCase = true)) {
            val count = result.substringBeforeLast(" Akun")
            result = "$count Accounts"
        } else if (result.endsWith(" Poin", ignoreCase = true)) {
            val count = result.substringBeforeLast(" Poin")
            result = "$count Points"
        } else if (result.startsWith("Aktif hingga:", ignoreCase = true)) {
            result = "Active until: " + result.substringAfter("Aktif hingga:")
        } else if (result.contains("tidak ditemukan!", ignoreCase = true)) {
            result = result.replace("tidak ditemukan!", "not found!", ignoreCase = true)
        } else if (result.startsWith("Belum Lunas", ignoreCase = true)) {
            result = result.replace("Belum Lunas", "Unpaid", ignoreCase = true)
        } else if (result.startsWith("Daftar Kode Aktivasi", ignoreCase = true)) {
            result = result.replace("Daftar Kode Aktivasi", "Activation Codes List", ignoreCase = true)
        } else if (result.contains("Minus (", ignoreCase = true)) {
            result = result.replace("Minus (", "Negative (", ignoreCase = true)
        } else if (result.startsWith("Dibuat:", ignoreCase = true)) {
            result = "Created: " + result.substringAfter("Dibuat:")
        } else if (result.startsWith("Sisa Stok:", ignoreCase = true)) {
            result = "Remaining Stock: " + result.substringAfter("Sisa Stok:")
        } else if (result.startsWith("Stok:", ignoreCase = true)) {
            result = "Stock: " + result.substringAfter("Stok:")
        } else if (result.startsWith("Sistem Stok:", ignoreCase = true)) {
            result = "Stock System: " + result.substringAfter("Sistem Stok:")
        } else if (result.startsWith("Staf Kasir Terhubung", ignoreCase = true)) {
            result = result.replace("Staf Kasir Terhubung", "Connected Cashier Staff", ignoreCase = true)
        } else if (result.startsWith("Sudah Lunas", ignoreCase = true)) {
            result = result.replace("Sudah Lunas", "Fully Paid", ignoreCase = true)
        } else if (result.startsWith("Total Transaksi:", ignoreCase = true)) {
            result = "Total Transactions: " + result.substringAfter("Total Transaksi:").replace("kali", "times", ignoreCase = true)
        } else if (result.startsWith("Tukar Loyalty Poin", ignoreCase = true)) {
            result = result.replace("Tukar Loyalty Poin", "Redeem Loyalty Points", ignoreCase = true).replace("Poin", "Points", ignoreCase = true).replace("Maks", "Max", ignoreCase = true)
        } else if (result.startsWith("Versi Terbaru Tersedia:", ignoreCase = true)) {
            result = "New Version Available: " + result.substringAfter("Versi Terbaru Tersedia:")
        } else if (result.startsWith("Waktu Cetak Dokumen:", ignoreCase = true)) {
            result = "Document Print Time: " + result.substringAfter("Waktu Cetak Dokumen:")
        } else if (result.contains("AKTIF S/D", ignoreCase = true)) {
            result = result.replace("AKTIF S/D", "ACTIVE UNTIL", ignoreCase = true).replace("HARI LAGI", "DAYS LEFT", ignoreCase = true)
        } else if (result.contains("STATUS: AKTIF", ignoreCase = true)) {
            result = result.replace("STATUS: AKTIF", "STATUS: ACTIVE", ignoreCase = true).replace("Masa Aktif s/d", "Active period until", ignoreCase = true).replace("Hari Lagi", "Days Left", ignoreCase = true)
        } else if (result.contains("sukses ditambahkan!", ignoreCase = true)) {
            result = result.replace("sukses ditambahkan!", "successfully added!", ignoreCase = true)
        } else if (result.contains("ditambahkan ke keranjang", ignoreCase = true)) {
            result = result.replace("ditambahkan ke keranjang", "added to cart", ignoreCase = true).replace("Banyaknya", "Qty", ignoreCase = true)
        } else if (result.startsWith("Berhasil melampirkan", ignoreCase = true)) {
            result = "Successfully attached " + result.substringAfter("Berhasil melampirkan").replace("foto produk!", "product photos!", ignoreCase = true)
        } else if (result.startsWith("Gagal mencatat pengeluaran:", ignoreCase = true)) {
            result = "Failed to record expense: " + result.substringAfter("Gagal mencatat pengeluaran:")
        } else if (result.startsWith("Gagal mencatatkan pengeluaran:", ignoreCase = true)) {
            result = "Failed to record expense: " + result.substringAfter("Gagal mencatatkan pengeluaran:")
        } else if (result.startsWith("Gagal menyimpan pengeluaran:", ignoreCase = true)) {
            result = "Failed to save expense: " + result.substringAfter("Gagal menyimpan pengeluaran:")
        } else if (result.startsWith("Gagal memuat billing:", ignoreCase = true)) {
            result = "Failed to load billing: " + result.substringAfter("Gagal memuat billing:")
        } else if (result.startsWith("Simulsi: Berhasil mengaktifkan", ignoreCase = true) or result.startsWith("Simulasi: Berhasil mengaktifkan", ignoreCase = true)) {
            result = result.replace("Berhasil mengaktifkan", "Successfully activated", ignoreCase = true).replace("Bulanan", "Monthly", ignoreCase = true).replace("Tahunan", "Annual", ignoreCase = true)
        } else if (result.startsWith("Subscription", ignoreCase = true) and result.contains("berhasil diubah!", ignoreCase = true)) {
            result = result.replace("berhasil diubah!", "successfully changed!", ignoreCase = true)
        } else if (result.startsWith("Sukses mengupload", ignoreCase = true)) {
            result = result.replace("Sukses mengupload", "Successfully uploaded", ignoreCase = true).replace("produk ke Cabang", "products to Branch", ignoreCase = true)
        } else if (result.startsWith("Sukses: Kode", ignoreCase = true) and result.contains("berhasil dibuat!", ignoreCase = true)) {
            result = result.replace("berhasil dibuat!", "successfully created!", ignoreCase = true)
        } else if (result.startsWith("Sukses: Semua kode lama berhasil disinkronkan ke user!", ignoreCase = true)) {
            result = "Success: All legacy codes successfully synced to user!"
        } else if (result.startsWith("Tukar Hadiah:", ignoreCase = true)) {
            result = "Redeem Reward: " + result.substringAfter("Tukar Hadiah:").replace("Pelanggan", "Customer", ignoreCase = true)
        } else if (result.startsWith("Saran: Untuk mengoreksi transaksi ini", ignoreCase = true)) {
            result = "Tip: To correct this transaction, open Cashier POS Sales then select the menu icon (three dots) in the top right corner."
        } else if (result.startsWith("Semua data lokal berhasil dipulihkan", ignoreCase = true)) {
            result = "All local data successfully restored from Cloud Server!"
        } else if (result.contains("Ada ", ignoreCase = true) and result.contains(" produk yang telah kedaluwarsa", ignoreCase = true)) {
            result = "There are " + result.substringAfter("Ada ").replace("produk yang telah kedaluwarsa atau mendekati kedaluwarsa. Klik untuk melihat daftar.", "expired or near-expiration products. Click to view the list.", ignoreCase = true)
        }

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
        } else if (result.startsWith("No HP:", ignoreCase = true)) {
            result = "Phone No: " + result.substringAfter("No HP:")
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
        } else if (result.startsWith("Defisit / Minus", ignoreCase = true)) {
            result = "Deficit / Minus " + result.substringAfter("Defisit / Minus")
        } else if (result.startsWith("Barcode '", ignoreCase = true) && result.contains("' tidak ditemukan!", ignoreCase = true)) {
            result = result.replace("tidak ditemukan!", "not found!", ignoreCase = true)
        } else if (result.startsWith("Tukar Loyalty Poin (Maks ", ignoreCase = true)) {
            result = "Redeem Loyalty Points (Max " + result.substringAfter("Tukar Loyalty Poin (Maks ").replace("Poin)", "Points)")
        } else if (result.startsWith("Nilai:", ignoreCase = true) && result.endsWith("/Poin", ignoreCase = true)) {
            result = "Value:" + result.substringAfter("Nilai:").replace("/Poin", "/Point", ignoreCase = true)
        } else if (result.startsWith("Potongan Harga:", ignoreCase = true)) {
            result = "Discount Amount:" + result.substringAfter("Potongan Harga:")
        } else if (result.startsWith("Terima kasih telah berbelanja di Kasir Pro! Total belanja Anda:", ignoreCase = true)) {
            result = result.replace("Terima kasih telah berbelanja di Kasir Pro! Total belanja Anda:", "Thank you for shopping at Kasir Pro! Your total purchase:", ignoreCase = true)
                           .replace("dengan status:", "with status:", ignoreCase = true)
        } else if (result.startsWith("Sukses: Kode ", ignoreCase = true) && result.contains("berhasil dibuat!", ignoreCase = true)) {
            result = result.replace("Sukses: Kode", "Success: Code", ignoreCase = true)
                           .replace("berhasil dibuat!", "successfully created!", ignoreCase = true)
        } else if (result.startsWith("Versi Terbaru Tersedia:", ignoreCase = true)) {
            result = "Latest Version Available:" + result.substringAfter("Versi Terbaru Tersedia:")
        } else if (result.startsWith("Total Transaksi:", ignoreCase = true)) {
            result = "Total Transactions:" + result.substringAfter("Total Transaksi:").replace("kali", "times")
        } else if (result.startsWith("Toko / Mitra Warung:", ignoreCase = true)) {
            result = "Store / Shop Partner:" + result.substringAfter("Toko / Mitra Warung:")
        } else if (result.startsWith("Waktu Cetak Dokumen:", ignoreCase = true)) {
            result = "Document Print Time:" + result.substringAfter("Waktu Cetak Dokumen:")
        }

        // 3. Sentence-level replacement using word boundaries to prevent substring corruption
        compiledPatterns.forEach { (regex, eng) ->
            result = result.replace(regex, eng)
        }
        translationCache[cacheKey] = result
        return result
    }
}
