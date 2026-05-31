package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.*
import com.example.ui.viewmodel.KasirViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSettingsScreen(viewModel: KasirViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val isPremium = user?.subscriptionStatus == "premium"
    val isDarkState by viewModel.isDarkMode.collectAsState()
    val langState by viewModel.language.collectAsState()
    val backupDateState by viewModel.lastBackupDate.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showLanguagesPicker by remember { mutableStateOf(false) }

    if (viewModel.activeScreen.collectAsState().value == "premium_pricing") {
        PremiumPricingView(viewModel)
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Pengaturan & Profil", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.activeScreen.value = "home" }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // User Profile Header Info
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(OrangeLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(user?.nama ?: "Pengusaha Sukses", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(user?.email ?: "mimin@kasirpro.id", fontSize = 12.sp, color = Color.Gray)
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isPremium) Color(0xFFDCFCE7) else Color(0xFFF1F5F9))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isPremium) "TIER: PREMIUM PRO" else "TIER: GRATIS",
                                    fontSize = 10.sp,
                                    color = if (isPremium) Color(0xFF15803D) else Color.DarkGray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Subscription Controller Info Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isPremium) Color(0xFFDCFCE7) else OrangeLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Stars, contentDescription = null, tint = OrangePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPremium) "Premium Pro Aktif" else "Batas Fitur Gratis Terdeteksi",
                                fontWeight = FontWeight.Bold,
                                color = OrangeDark,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isPremium) "Selamat! Seluruh batasan produk ditiadakan. Akses multi-cabang, loyalitas pelanggan dan cloud backup aktif!"
                            else "Maksimal 10 produk, 1 kasir, dan laporan hari ini harian saja. Upgrade hari ini untuk performa maksimal!",
                            fontSize = 12.sp,
                            color = OrangeDark
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (isPremium) {
                                    scope.launch { viewModel.downgradeToFree() }
                                } else {
                                    viewModel.activeScreen.value = "premium_pricing"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isPremium) "Kembalikan ke Gratis (Demo)" else "Upgrade Premium Sekarang")
                        }
                    }
                }

                Text("Pengaturan Aplikasi", fontWeight = FontWeight.Bold, color = OrangePrimary, fontSize = 14.sp)

                // Layout settings preferences
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        // Dark Mode Toggle Switch
                        Row(
                            modifier = Modifier
                                .clickable { viewModel.setDarkMode(!isDarkState) }
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.DarkMode, contentDescription = null, tint = OrangePrimary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Mode Gelap (Dark Theme)")
                            }
                            Switch(
                                checked = isDarkState,
                                onCheckedChange = { viewModel.setDarkMode(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = OrangePrimary)
                            )
                        }
                        HorizontalDivider()

                        // Languages Picker selector modal trigger
                        Row(
                            modifier = Modifier
                                .clickable { showLanguagesPicker = true }
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = OrangePrimary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Bahasa (Language)")
                            }
                            Text(
                                text = if (langState == "id") "Bahasa Indonesia" else "English",
                                fontWeight = FontWeight.Bold,
                                color = OrangePrimary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Text("Backup & Recovery Data", fontWeight = FontWeight.Bold, color = OrangePrimary, fontSize = 14.sp)

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Terakhir Backup: $backupDateState", fontSize = 12.sp, color = Color.Gray)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    if (isPremium) {
                                        viewModel.runBackup()
                                        Toast.makeText(context, "Backup cloud Google Drive sukses!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.showLimitPopup.value = "Cloud Backup Google Drive otomatis/manual hanya didukung untuk tipe pelanggan premium!"
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                            ) {
                                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Backup Data", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    if (isPremium) {
                                        Toast.makeText(context, "Semua data lokal berhasil dipulihkan dari Server Cloud!", Toast.LENGTH_LONG).show()
                                    } else {
                                        viewModel.showLimitPopup.value = "Data RESTORE hanya didukung untuk akun premium!"
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                            ) {
                                Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restore Data", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Logout Actions
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch {
                                viewModel.logout()
                                viewModel.activeScreen.value = "login"
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Log out", tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Keluar dari Kasir Pro", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        }
    }

    if (showLanguagesPicker) {
        AlertDialog(
            onDismissRequest = { showLanguagesPicker = false },
            title = { Text("Pilih Bahasa") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("id" to "Bahasa Indonesia", "en" to "English").forEach { (code, name) ->
                        Card(
                            onClick = {
                                viewModel.setLanguage(code)
                                showLanguagesPicker = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(name, modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

// ==== SIMULATION PREMIUM PRICING CARD LAYOUT WITH MIDTRANS PROMPTS ====
@Composable
fun PremiumPricingView(viewModel: KasirViewModel) {
    val context = LocalContext.current
    var isVerifyingPayment by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = { viewModel.activeScreen.value = "settings" }) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        Icon(imageVector = Icons.Default.Stars, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(72.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text("Kasir Pro Premium", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = Color.White))
        Text("Tingkatkan produktivitas outlet Anda dengan fitur pro bisnis terlengkap!", color = Color.LightGray, textAlign = TextAlign.Center, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(modifier = Modifier.height(24.dp))

        // Compare checklist card panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate800)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Bandingkan Fitur:", fontWeight = FontWeight.Bold, color = OrangePrimary)
                
                listOf(
                    Triple("Maksimal Produk", "10 Produk", "Tanpa Batas!"),
                    Triple("Jumlah Transaksi", "50 / Bulan", "Murni Tanpa Batas!"),
                    Triple("Jumlah Toko Cabang", "1 Cabang", "Kelola Multi Cabang!"),
                    Triple("Laporan Keuangan", "Hanya Hari ini", "Detail Grafik Mingguan-Tahunan!"),
                    Triple("Database Hutang & Pelanggan", "Tidak Ada", " CRM & Tagihan Piutang!"),
                    Triple("Backup Google Drive & Sync", "Tidak Ada", "Awan Cloud Backup Realtime!")
                ).forEach { (metric, freeVal, premiumVal) ->
                    Column {
                        Text(metric, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Gratis: $freeVal", fontSize = 11.sp, color = Color.LightGray)
                            Text("Premium: $premiumVal", fontSize = 11.sp, color = Color.Green, fontWeight = FontWeight.Bold)
                        }
                    }
                    HorizontalDivider(color = Color.DarkGray)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Card Subscription pricing packs
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate700)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("PAKET PRO INDONESIA", fontWeight = FontWeight.Bold, color = Color.White)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("PRO BULANAN", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        Text("Hemat waktu operasional kencang", fontSize = 11.sp, color = Color.LightGray)
                    }
                    Text("Rp 29.000 / bln", fontWeight = FontWeight.ExtraBold, color = OrangePrimary, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("PRO TAHUNAN", fontWeight = FontWeight.Bold, color = Color.Green, fontSize = 16.sp)
                        Text("Hemat 42% paket langganan tahunan!", fontSize = 11.sp, color = Color.LightGray)
                    }
                    Text("Rp 199.000 / thn", fontWeight = FontWeight.ExtraBold, color = Color.Green, fontSize = 18.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                // Instantly trigger Midtrans API flow simulation
                isVerifyingPayment = true
                scope.launch {
                    kotlinx.coroutines.delay(2000) // simulated network delay
                    isVerifyingPayment = false
                    viewModel.upgradeToPremium()
                    Toast.makeText(context, "SINKRONISASI MIDTRANS: Transaksi Sukses! Akun Anda aktif sebagai Premium Pro.", Toast.LENGTH_LONG).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("midtrans_pay_trigger")
        ) {
            if (isVerifyingPayment) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("PILIH PAKET & BAYAR MIDTRANS", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
