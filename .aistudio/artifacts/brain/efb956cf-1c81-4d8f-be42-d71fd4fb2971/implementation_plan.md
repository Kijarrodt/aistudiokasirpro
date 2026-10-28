# Rencana Implementasi: Integrasi AdMob, Rating Play Store & Optimasi Pengguna Gratis

Menambahkan integrasi Google AdMob lengkap, popup penilaian Google Play Store, serta alur pemicu langganan Pro pasca-iklan khusus untuk pengguna gratis tanpa mengganggu transaksi kasir yang butuh kecepatan.

---

## User Review Required

> [!IMPORTANT]
> **Ad Unit & App ID yang Digunakan:**
> - **AdMob App ID**: `ca-app-pub-9991336658531273~6760228236`
> - **Banner Ad 1**: `ca-app-pub-9991336658531273/2807208224` (Manajemen Produk & Stok)
> - **Banner Ad 2**: `ca-app-pub-9991336658531273/3911125078` (Riwayat Transaksi & Laporan)
> - **Interstitial Ad**: `ca-app-pub-9991336658531273/3005237510` (Setiap 3 transaksi selesai, saat kembali ke Dashboard)
> - **Rewarded Ad**: `ca-app-pub-9991336658531273/1883727536` (Reward: Ekspor 1x Laporan PDF Gratis untuk akun Free)

> [!NOTE]
> **Proteksi Pengguna Berlangganan (Premium/Pro):**
> Pengguna yang memiliki status langganan aktif (`isPremium == true`) **sama sekali TIDAK AKAN melihat atau memuat iklan apapun (Zero Ads)**.

---

## Proposed Changes

### 1. Konfigurasi Dependensi & Versi Aplikasi

#### `app/build.gradle.kts` & `gradle/libs.versions.toml`
- Naikkan versi aplikasi:
  - `versionCode` = `22`
  - `versionName` = `"1.0.2.1"`
- Tambahkan dependensi Google Mobile Ads SDK:
  - `com.google.android.gms:play-services-ads:23.6.0`
- Tambahkan dependensi Google Play In-App Review API:
  - `com.google.android.play:review-ktx:2.0.2`

#### `app/src/main/AndroidManifest.xml`
- Tambahkan meta-data AdMob Application ID:
  ```xml
  <meta-data
      android:name="com.google.android.gms.ads.APPLICATION_ID"
      android:value="ca-app-pub-9991336658531273~6760228236"/>
  ```

#### `app/proguard-rules.pro`
- Tambahkan keep rules untuk Google Mobile Ads SDK dan Google Play Review agar tidak terpotong oleh R8 minify:
  ```proguard
  # --- Google Mobile Ads (AdMob) ---
  -keep class com.google.android.gms.ads.** { *; }
  -dontwarn com.google.android.gms.ads.**
  # --- Google Play In-App Review ---
  -keep class com.google.android.play.core.review.** { *; }
  ```

---

### 2. Modul AdMob & Rating Play Store

#### `app/src/main/java/com/kasirpro/app/ads/AdMobManager.kt` (Baru)
- Mengelola inisialisasi `MobileAds.initialize(context)`.
- Preload & caching untuk **Interstitial Ad** dan **Rewarded Ad** agar siap tampil seketika tanpa jeda buffering.
- Fungsi `showInterstitialAd(activity, onDismissed)`:
  - Menampilkan iklan interstisial jika pengguna adalah akun Free dan ad sudah siap.
  - Saat ad ditutup (`onAdDismissedFullScreenContent`), panggil callback untuk memicu popup upgrade langganan.
- Fungsi `showRewardedAd(activity, onRewardEarned, onDismissed)`:
  - Menampilkan iklan rewarded jika free user ingin ekspor laporan PDF.
  - Memberikan reward callback untuk membuka unduhan PDF dan memicu penawaran langganan.

#### `app/src/main/java/com/kasirpro/app/ads/BannerAdView.kt` (Baru)
- Composable wrapper `AndroidView` untuk `AdView` M3:
  - Menghitung ukuran banner adaptif (`AdSize.BANNER` atau adaptive banner).
  - Mengisolasi lifecycle (pause, resume, destroy).
  - Otomatis menyembunyikan diri jika `isPremium == true`.

#### `app/src/main/java/com/kasirpro/app/ui/components/PlayReviewDialog.kt` (Baru) & `PlayReviewManager.kt`
- Mengelola pengecekan frekuensi rating (menggunakan DataStore / SharedPreferences agar tidak spamming).
- Tampil saat pengguna kembali ke Dashboard utama setelah menyelesaikan transaksi.
- Memberikan opsi:
  - "Beri Bintang 5": Membuka Google Play In-App Review Flow (atau fallback membuka URL Play Store `market://details?id=...`).
  - "Nanti Saja": Menunda 7 hari.
  - "Jangan Tampilkan Lagi".

---

### 3. Integrasi UI & Alur Transaksi Kasir

#### `app/src/main/java/com/kasirpro/app/ui/viewmodel/KasirViewModel.kt`
- Tambahkan state:
  - `completedTransactionsCount`: Menghitung transaksi selesai untuk memicu Interstitial tiap 3 transaksi.
  - `showSubscriptionUpsellDialog`: State untuk membuka popup penawaran langganan pasca-iklan.
  - `showPlayReviewPrompt`: State untuk memunculkan dialog review di Dashboard.

#### `app/src/main/java/com/kasirpro/app/ui/components/SubscriptionUpsellDialog.kt` (Baru / Diperbarui)
- Popup menarik bergaya Material 3 setelah iklan selesai:
  - Judul: *"Nikmati Kasir Cepat Tanpa Iklan & Tanpa Jeda"*
  - Keuntungan: Transaksi kasir kilat, Laporan lengkap, Bebas iklan seumur hidup/langganan.
  - Tombol: *"Langganan Kasir Pro Sekarang"* (mengarahkan langsung ke Google Play Billing) dan *"Lanjut Pakai Gratis"*.

#### Integrasi Halaman:
1. **Dashboard Utama**:
   - Menampilkan `PlayReviewDialog` saat kondisi terpenuhi (pengguna free/pro yang kembali ke dashboard setelah transaksi).
2. **Manajemen Produk / Stok**:
   - Menampilkan Banner Ad 1 (`ca-app-pub-9991336658531273/2807208224`) di bagian bawah layar untuk pengguna gratis.
3. **Riwayat Transaksi / Laporan**:
   - Menampilkan Banner Ad 2 (`ca-app-pub-9991336658531273/3911125078`) di bagian bawah layar untuk pengguna gratis.
4. **Halaman Transaksi Kasir**:
   - Transaksi kasir **TIDAK PERNAH** disisipkan banner atau pop-up yang memperlambat kasir saat input barang/bayar.
   - Setelah struk selesai dicetak / kasir menekan "Selesai" untuk kembali ke Dashboard:
     - Jika transaksi ke-3 kelipatan tercapai: Tampilkan Interstitial Ad, lalu popup penawaran langganan.
5. **Ekspor Laporan PDF**:
   - Untuk pengguna gratis, menampilkan dialog penawaran: *"Tonton 1 video singkat untuk ekspor PDF gratis, atau langganan Pro untuk ekspor bebas kapan saja"*.
   - Jika memilih tonton: jalankan Rewarded Ad, lalu lanjutkan proses unduh PDF.

---

## Verification Plan

### Automated Verification
- Jalankan `compile_applet` untuk memastikan seluruh dependensi Google Mobile Ads, Play Review, ProGuard rules, dan kode Kotlin terkompilasi tanpa error.

### Manual Verification (Testing Scenarios)
1. **Uji Akun Gratis (Free Tier)**:
   - Selesaikan transaksi kasir hingga 3 kali: Pastikan iklan interstisial terpicu dan setelah ditutup popup langganan langsung muncul.
   - Buka halaman Stok/Produk: Pastikan Banner 1 tampil di bawah layar.
   - Buka halaman Riwayat/Laporan: Pastikan Banner 2 tampil di bawah layar.
   - Coba ekspor laporan PDF: Pastikan dialog iklan reward muncul dan reward didapatkan setelah menonton.
   - Kembali ke Dashboard: Verifikasi popup rating Play Store muncul dengan opsi yang berfungsi.
2. **Uji Akun Pro / Berlangganan Aktif**:
   - Pastikan akun Pro tidak pernah menampilkan Banner 1, Banner 2, Interstisial, maupun Rewarded ad.
