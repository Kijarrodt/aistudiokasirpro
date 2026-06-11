# KasirPro - Aplikasi Point of Sale (POS) Modern & Pintar

KasirPro adalah solusi Point of Sale (POS) kasir digital serbaguna yang dirancang menggunakan platform Android native cerdas, kokoh, dan berkinerja tinggi. Tampilan antarmuka yang modern menggunakan **Jetpack Compose (Material Design 3)**, dipadukan dengan arsitektur **MVVM (Model-View-ViewModel)** dan database lokal super cepat **Room DB** serta sinkronisasi awan berbasis **Google Firebase Firestore**.

Aplikasi ini dapat beroperasi secara penuh secara luring (offline) dan secara otomatis menyinkronkan data bisnis saat kembali terhubung dengan internet, sangat aman untuk pengoperasian harian tanpa khawatir kehilangan data.

---

## 1. Fitur Utama Aplikasi

### 📊 Dashboard Bisnis & Analitik Real-Time
* **Grafik Penjualan**: Visualisasi omset pemasukan dan laba bersih secara visual dan dinamis guna memantau kesehatan keuangan bisnis.
* **Statistik Produk Terlaris**: Mengetahui produk yang paling dicari oleh pelanggan dengan cepat untuk optimasi stok.
* **Notifikasi Stok Menipis & Kadaluwarsa**: Sistem peringatan dini dari aplikasi agar pemilik toko menyadari produk dengan stok di bawah batas minimal dan produk yang mendekati tanggal kedaluwarsa.

### 🛒 Mesin Kasir & Transaksi Cepat (POS)
* **Pencarian Kode & Scan Barcode**: Memanfaatkan kamera ponsel cerdas Anda untuk membaca barcode dengan cepat atau mencarinya dalam katalog visual.
* **Manajemen Varian & Satuan**: Menangani satu produk dengan beragam varian (misal: warna, ukuran) beserta harga grosir/eceran yang dinamis.
* **Diskon & Promo Pintar**: Mengaplikasikan diskon persen atau nominal langsung di item belanjaan maupun tingkat keranjang transaksi.
* **Struk WhatsApp & Cetak Termal**: Kirim struk belanja digital yang cantik secara langsung ke nomor WhatsApp pelanggan atau cetak lewat printer Bluetooth.

### 📦 Manajemen Produk & Riwayat Stok
* **Kartu Stok Terintegrasi**: Mencatat setiap aktivitas mutasi stok baru ("Stock-In"), barang rusak/keluar ("Stock-Out"), dan penyesuaian stok ("Opname").
* **Grosir Terintegrasi**: Mengatur diskon bertingkat (misal: beli minimal 10 buah mendapatkan harga khusus).
* **Manajemen Kedaluwarsa (Expiry Date)**: Menampung tanggal kedaluwarsa dengan jangka waktu pengingat yang dapat disesuaikan.

### 👥 Kelola Database Pelanggan & CRM Poin
* **Profil Pelanggan**: Menyimpan nomor kontak, riwayat kunjungan, dan jumlah transaksi pelanggan setia.
* **Loyalty Reward (Poin)**: Konfigurasi poin belanja per nominal tertentu yang nantinya dikumpulkan oleh pelanggan untuk ditukar kupon atau diskon belanja.

### 💸 Buku Catatan Hutang & Piutang (Debts)
* **Pencatatan Piutang Terintegrasi**: Menjual produk dengan metode pembayaran DP (Uang Muka) atau tempo dengan pencatatan hutang otomatis.
* **Sistem Pembayaran Cicilan**: Pembayaran bertahap untuk piutang pelanggan hingga lunas secara transparan.

### 👥 Multi-Cabang & Manajemen Kasir (Shift)
* **Manajemen Cabang (Multi-Outlet)**: Mengendalikan beberapa cabang toko dari satu akun pemilik.
* **Akses Multi-Kasir Terproteksi**: Mendaftarkan akun kasir khusus secara aman dengan pin dan kata sandi yang disandikan.
* **Shift Laci Kas (Cash Drawer Lock)**: Mencatat setoran modal awal kasir dan uang akhir kasir saat bertukar shift kerja untuk menghindari kecurangan atau selisih nominal laci kasir.

### ☁️ Sinkronisasi & Backup Otomatis
* **Mode Offline Handal**: Transaksi tetap lancar tanpa internet, data terenkripsi lokal di dalam Room DB.
* **Firebase Real-time Sync**: Mengunggah data penjualan, produk, dan laporan secara aman ke Firestore seketika saat jaringan internet tesedia.

---

## 2. Kelebihan Aplikasi KasirPro

1. **Aman Terhadap Kebocoran Data (Data Privacy Focus)**: Konfigurasi sensitif (seperti Google Web Client ID, API Keys, Google Services) dikelola dengan aman menggunakan `.env` (Secrets panel di AI Studio) dan tidak terekspos di repositori Git publik.
2. **Fleksibilitas Konektivitas**: Mendukung bypass mode luring penuh saat melakukan pengembangan atau penggunaan di area minim sinyal.
3. **Ekosistem Ringan & Responsif**: Menggunakan Native Kotlin Jetpack Compose terbaru yang meminimalkan beban CPU dan memori ponsel dibandingkan platform hibrid (React Native/Flutter).
4. **Desain Visual Modern (Aesthetic Comfort)**: Menggunakan tema warna gelap yang elegan (*Slate Dark & Premium Orange*) dengan Material Design 3 untuk perlindungan mata pekerja kasir dalam jam kerja malam yang panjang.

---

## 3. Tingkatan Keanggotaan & Perbedaan Paket

Pelanggan dapat memilih tiga paket keanggotaan berdasarkan skala pertumbuhan bisnis masing-masing:

| Fitur Utama | 🟢 Paket Dasar | 🔵 Paket Profesional (Rekomendasi) | 🟣 Paket Bisnis |
| :--- | :---: | :---: | :---: |
| **Harga Bulanan** | Rp 50.000 / bulan | Rp 100.000 / bulan | Rp 150.000 / bulan |
| **Harga Tahunan** | Rp 500.000 / tahun *(Hemat 2 Bulan)* | Rp 1.000.000 / tahun *(Hemat 2 Bulan)* | Rp 1.500.000 / tahun *(Hemat 2 Bulan)* |
| **Kapasitas Produk** | Maksimal 50 Produk | **Tanpa Batas** | **Tanpa Batas** |
| **Sistem Transaksi** | Tanpa Batas | Tanpa Batas | Tanpa Batas |
| **Batas Cabang & Kasir** | 1 Kasir (Maks. 3 Cabang) | **Maks. 3 Cabang & 5 Kasir** | **Tanpa Batas Cabang & Kasir** |
| **Periode Laporan** | Harian & Mingguan Saja | **Harian, Mingguan, Bulanan & Tahunan** | **Seluruh Periode & Laporan Gabungan** |
| **Ekspor Laporan** | ❌ Tidak Tersedia | **Ekspor Laporan ke PDF & Excel** | **Ekspor Laporan lengkap seluruh cabang** |
| **Kelola Hutang & CRM** | ❌ Tidak Tersedia | **Aktif (Pencatatan Hutang & Poin)** | **Aktif (Pencatatan Hutang & Poin)** |
| **Manajemen Promo & Varian**| ❌ Tidak Tersedia | **Aktif (Varian produk detail & Promo)** | **Aktif (Varian produk detail & Promo)** |
| **Shift Kerja Kasir** | ❌ Tidak Tersedia | **Aktif** | **Aktif (Dapat dilacak semua cabang)** |
| **Bulk Upload Excel** | ❌ Tidak Tersedia | **Aktif** | **Aktif** |
| **Sistem Backup Data** | Lokal Manual | Manual | **Otomatis Cloud Backup** |
| **Laporan Antar-Cabang** | ❌ Tidak Tersedia | ❌ Tidak Tersedia | **Laporan Konsolidasi Gabungan Cabang** |

---

## 🚀 Cara Mulai Menggunakan

Untuk pengembang lokal atau proses kontribusi proyek:
1. Pastikan Anda mengimpor berkas konfigurasi `google-services.json` ke dalam direktori `app/` lokal Anda (berkas ini diabaikan oleh `.gitignore` publik demi keamanan).
2. Tambahkan variabel lingkungan Anda di dalam berkas `.env` lokal, mengambil contoh dari berkas `.env.example`.
3. Jalankan unit test lokal menggunakan Gradle:
   ```bash
   gradle :app:testDebugUnitTest
   ```
4. Lakukan kompilasi debug dan pasang aplikasi langsung menggunakan Android Studio.
