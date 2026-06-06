package com.kasirpro.app.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kasirpro.app.data.local.*
import com.kasirpro.app.ui.viewmodel.KasirViewModel
import com.kasirpro.app.ui.theme.*
import com.kasirpro.app.util.ImageHelper
import com.kasirpro.app.util.ShopLogoImage
import coil.compose.AsyncImage
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

    val idrFormatter = remember {
        val fmt = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("id", "ID"))
        fmt.maximumFractionDigits = 0
        fmt
    }

    val business by viewModel.currentBusiness.collectAsState()
    var showEditShopProfile by remember { mutableStateOf(false) }
    var showQrisSettings by remember { mutableStateOf(false) }
    var showOwnerCodeDialog by remember { mutableStateOf(false) }
    var showLanguagesPicker by remember { mutableStateOf(false) }
    var activeSettingSubScreen by remember { mutableStateOf<String?>(null) }

    if (showEditShopProfile) {
        val biz = business
        var shopName by remember { mutableStateOf(biz?.namaBisnis ?: "") }
        var shopAddress by remember { mutableStateOf(biz?.alamat ?: "") }
        var shopPhone by remember { mutableStateOf(biz?.noTelpon ?: "") }
        var logoImgUri by remember { mutableStateOf<Uri?>(null) }
        var logoImgBytes by remember { mutableStateOf<ByteArray?>(null) }
        var isUploadingLogo by remember { mutableStateOf(false) }

        val logoPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                logoImgUri = uri
                val compressed = ImageHelper.compressImageUri(context, uri)
                if (compressed != null) {
                    logoImgBytes = compressed
                }
            }
        }

        AlertDialog(
            onDismissRequest = { if (!isUploadingLogo) showEditShopProfile = false },
            title = { Text("Edit Profil Toko & Struk", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Detail toko di bawah ini akan ditampilkan secara otomatis pada Struk Belanja/Print cetak.", fontSize = 12.sp, color = Color.Gray)
                    
                    // Logo Area
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .align(Alignment.CenterHorizontally)
                            .clickable { logoPickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (logoImgUri != null) {
                            AsyncImage(
                                model = logoImgUri,
                                contentDescription = "Logo Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (!biz?.logoUrl.isNullOrBlank()) {
                            ShopLogoImage(
                                logoUrl = biz?.logoUrl,
                                contentDescription = biz?.namaBisnis,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, tint = OrangePrimary)
                                Text("Upload Logo", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text("Nama Toko") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = shopAddress,
                        onValueChange = { shopAddress = it },
                        label = { Text("Alamat Toko") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = shopPhone,
                        onValueChange = { shopPhone = it },
                        label = { Text("No Telepon Toko") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    if (isUploadingLogo) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Text("Mengupload logo toko...", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (shopName.isBlank()) {
                            Toast.makeText(context, "Nama toko tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isUploadingLogo = true
                        scope.launch {
                            val ownerId = user?.uid ?: "owner-main"
                            var finalLogoUrl = biz?.logoUrl
                            val bytes = logoImgBytes
                            if (bytes != null) {
                                try {
                                    finalLogoUrl = ImageHelper.uploadShopLogo(context, ownerId, bytes)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            viewModel.updateBusinessProfile(shopName, shopAddress.takeIf { it.isNotBlank() }, shopPhone.takeIf { it.isNotBlank() }, finalLogoUrl) {
                                isUploadingLogo = false
                                showEditShopProfile = false
                                Toast.makeText(context, "Profil Toko diperbarui!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isUploadingLogo,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { if (!isUploadingLogo) showEditShopProfile = false }, enabled = !isUploadingLogo) {
                    Text("Batal")
                }
            }
        )
    }

    if (showQrisSettings) {
        val biz = business
        var qrisImgUri by remember { mutableStateOf<Uri?>(null) }
        var qrisImgBytes by remember { mutableStateOf<ByteArray?>(null) }
        var isUploadingQris by remember { mutableStateOf(false) }

        val qrisPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                qrisImgUri = uri
                val compressed = com.kasirpro.app.util.ImageHelper.compressImageUri(context, uri)
                if (compressed != null) {
                    qrisImgBytes = compressed
                }
            }
        }

        AlertDialog(
            onDismissRequest = { if (!isUploadingQris) showQrisSettings = false },
            title = { Text("Pengaturan QRIS Pembayaran", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Upload kode QR QRIS toko Anda. QRIS ini akan ditampilkan kepada kasir atau pelanggan saat melakukan transaksi dengan metode pembayaran QRIS.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { qrisPickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrisImgUri != null) {
                            com.kasirpro.app.util.ShopQrisImage(
                                qrisUrl = qrisImgUri.toString(),
                                contentDescription = "QRIS Preview",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (!biz?.qrisUrl.isNullOrBlank()) {
                            com.kasirpro.app.util.ShopQrisImage(
                                qrisUrl = biz?.qrisUrl,
                                contentDescription = "QRIS Live",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = OrangePrimary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Pilih Foto QRIS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Klik untuk memilih gambar", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }

                    if (qrisImgBytes != null) {
                        val kbSize = qrisImgBytes!!.size / 1024
                        Text("Ukuran gambar: ${kbSize} KB", fontSize = 11.sp, color = OrangePrimary)
                    }

                    if (isUploadingQris) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = OrangePrimary)
                            Text("Mengupload foto QRIS...", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ownerId = user?.uid ?: "owner-main"
                        val bytes = qrisImgBytes
                        if (bytes == null && biz?.qrisUrl == null) {
                            Toast.makeText(context, "Silakan pilih foto QRIS terlebih dahulu", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isUploadingQris = true
                        scope.launch {
                            var finalQrisUrl = biz?.qrisUrl
                            if (bytes != null) {
                                try {
                                    finalQrisUrl = com.kasirpro.app.util.ImageHelper.uploadQrisPhoto(context, ownerId, bytes)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            viewModel.updateBusinessQris(finalQrisUrl) {
                                isUploadingQris = false
                                showQrisSettings = false
                                Toast.makeText(context, "Foto QRIS berhasil disimpan!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isUploadingQris,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { if (!isUploadingQris) showQrisSettings = false }, enabled = !isUploadingQris) {
                    Text("Batal")
                }
            }
        )
    }

    if (showOwnerCodeDialog) {
        var code by remember { mutableStateOf(viewModel.getOwnerVerificationCode()) }
        AlertDialog(
            onDismissRequest = { showOwnerCodeDialog = false },
            title = { Text("Kode Otoritas Pemilik", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Kode verifikasi ini digunakan untuk menyetujui koreksi / edit transaksi yang dilakukan oleh Staff Kasir.", fontSize = 12.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = code,
                        onValueChange = { if (it.length <= 8) code = it },
                        label = { Text("Masukkan Kode Otoritas (Angka/Huruf)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (code.isBlank()) {
                            Toast.makeText(context, "Kode tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.saveOwnerVerificationCode(code)
                        showOwnerCodeDialog = false
                        Toast.makeText(context, "Kode Otoritas disimpan!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOwnerCodeDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (viewModel.activeScreen.collectAsState().value == "premium_pricing") {
        PremiumPricingView(viewModel)
    } else if (activeSettingSubScreen != null) {
        val defaultTitle = when (activeSettingSubScreen) {
            "PELANGGAN" -> "Loyalty Pelanggan & CRM"
            "PROMO" -> "Manajemen Promo & Kupon"
            "CABANG" -> "Outlet Multi-Cabang"
            "KASIR" -> "Staf & Akun Kasir"
            else -> "Fitur Premium"
        }
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    title = { Text(defaultTitle, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { activeSettingSubScreen = null }) {
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
            ) {
                if (!isPremium) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = OrangeLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = OrangePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Mode Peninjauan (Demo Premium)", fontWeight = FontWeight.Bold, color = OrangeDark, fontSize = 12.sp)
                                Text("Anda sedang melihat contoh fitur ini. Agar dapat menambah/menyimpan data secara realtime, silakan Upgrade ke Premium Pro.", fontSize = 11.sp, color = OrangeDark)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.activeScreen.value = "premium_pricing" },
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Upgrade", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                    when (activeSettingSubScreen) {
                        "PELANGGAN" -> PremiumPelangganTab(viewModel)
                        "PROMO" -> PremiumPromoTab(viewModel)
                        "CABANG" -> PremiumCabangTab(viewModel)
                        "KASIR" -> PremiumKasirTab(viewModel)
                    }
                }
            }
        }
    } else {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                val isKasir = user?.role == "kasir" || user?.role == "kasir_invited"
                TopAppBar(
                    title = { Text(if (isKasir) "Profil Kasir & Shift" else "Pengaturan & Profil", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        if (!isKasir) {
                            IconButton(onClick = { viewModel.activeScreen.value = "home" }) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            val isKasir = user?.role == "kasir" || user?.role == "kasir_invited"
            if (isKasir) {
                val activeShiftState by viewModel.activeShift.collectAsState()
                val allTxs by viewModel.transactions.collectAsState()

                val activeShiftTxs = remember(activeShiftState, allTxs) {
                    if (activeShiftState != null) {
                        allTxs.filter { tx ->
                            tx.kasirId == activeShiftState!!.cashierId && 
                            tx.branchId == activeShiftState!!.branchId && 
                            tx.createdAt in activeShiftState!!.startTime..System.currentTimeMillis()
                        }
                    } else {
                        emptyList()
                    }
                }

                val totalTunai = remember(activeShiftTxs) {
                    activeShiftTxs.filter { it.metodeBayar.contains("Tunai", ignoreCase = true) }.sumOf { it.total }
                }
                val totalNonTunai = remember(activeShiftTxs) {
                    activeShiftTxs.filter { !it.metodeBayar.contains("Tunai", ignoreCase = true) }.sumOf { it.total }
                }
                val totalTransaksi = remember(activeShiftTxs) {
                    activeShiftTxs.sumOf { it.total }
                }

                val showShiftReportState = remember { mutableStateOf<com.kasirpro.app.data.repository.ShiftReport?>(null) }
                val showShiftReportDialog = showShiftReportState.value
                var shiftReportTransactions by remember { mutableStateOf<List<TransactionEntity>>(emptyList()) }

                var showManualCashInputDialog by remember { mutableStateOf(false) }
                var manualCashValue by remember { mutableStateOf("") }

                val branchesList by viewModel.branches.collectAsState()
                val branchName = remember(branchesList, user) {
                    branchesList.find { it.id == user?.assignedBranchId }?.namaCabang ?: "Cabang Utama"
                }

                // --- Live Cashier Shift Expenses Data & Dialog States ---
                val ownerId = remember(user) {
                    if (user?.role == "kasir" || user?.role == "kasir_invited") user?.ownerId else user?.uid
                }

                val expensesState = remember { mutableStateOf<List<LocalExpenseItem>>(emptyList()) }
                var showAddExpenseDialogLocal by remember { mutableStateOf(false) }
                var localExpenseNominal by remember { mutableStateOf("") }
                var localExpenseKet by remember { mutableStateOf("") }

                // Dialog states for receipt and supervisor validation code
                var selectedTxForReceipt by remember { mutableStateOf<TransactionEntity?>(null) }
                var showCorrectionAuthDialog by remember { mutableStateOf(false) }
                var showCorrectionEditDialog by remember { mutableStateOf(false) }
                var authCodeInput by remember { mutableStateOf("") }

                DisposableEffect(ownerId) {
                    if (ownerId.isNullOrBlank()) return@DisposableEffect onDispose {}
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val expensesListener = db.collection("expenses")
                        .whereEqualTo("ownerId", ownerId)
                        .addSnapshotListener { snapshot, error ->
                            if (error == null && snapshot != null) {
                                val list = snapshot.documents.map { doc ->
                                    val amt = doc.getDouble("amount") 
                                        ?: doc.getDouble("jumlah") 
                                        ?: doc.getDouble("nominal") 
                                        ?: doc.getLong("amount")?.toDouble() 
                                        ?: doc.getLong("jumlah")?.toDouble() 
                                        ?: doc.getLong("nominal")?.toDouble() 
                                        ?: 0.0
                                    val date = doc.getLong("createdAt") 
                                        ?: doc.getLong("date") 
                                        ?: doc.getTimestamp("createdAt")?.seconds?.times(1000) 
                                        ?: doc.getTimestamp("date")?.seconds?.times(1000) 
                                        ?: System.currentTimeMillis()
                                    val ket = doc.getString("keterangan") ?: doc.getString("desc") ?: ""
                                    LocalExpenseItem(doc.id, amt, date, ket)
                                }
                                expensesState.value = list
                            }
                        }
                    onDispose {
                        expensesListener.remove()
                    }
                }

                val activeShiftExpenses = remember(activeShiftState, expensesState.value) {
                    if (activeShiftState != null) {
                        expensesState.value.filter { exp ->
                            exp.createdAt >= activeShiftState!!.startTime
                        }
                    } else {
                        emptyList()
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile Info
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
                                Text(user?.nama ?: "Kasir Aktif", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("ID Kasir: ${user?.uid ?: ""}", fontSize = 12.sp, color = Color.Gray)
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(OrangeLight)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "CABANG: $branchName",
                                        fontSize = 11.sp,
                                        color = OrangeDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Shift stats & active shift card
                    if (activeShiftState != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = OrangePrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Informasi Shift Aktif", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }

                                HorizontalDivider()

                                val startFmt = remember(activeShiftState) {
                                    val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale("id", "ID"))
                                    sdf.format(java.util.Date(activeShiftState!!.startTime))
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Mulai Shift", fontSize = 12.sp, color = Color.Gray)
                                    Text(startFmt, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Uang Modal Awal", fontSize = 12.sp, color = Color.Gray)
                                    Text(idrFormatter.format(activeShiftState!!.modalAwal), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Jumlah Transaksi", fontSize = 12.sp, color = Color.Gray)
                                    Text("${activeShiftTxs.size}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total Tunai", fontSize = 12.sp, color = Color.Gray)
                                    Text(idrFormatter.format(totalTunai), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total Non-Tunai", fontSize = 12.sp, color = Color.Gray)
                                    Text(idrFormatter.format(totalNonTunai), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                HorizontalDivider()

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Uang Tunai di Laci", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OrangePrimary)
                                    Text(idrFormatter.format(activeShiftState!!.modalAwal + totalTunai), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OrangePrimary)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                         showManualCashInputDialog = true
                                         // Managed in modal dialog confirmation instead

                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("end_shift_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Stop, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Akhiri Shift Kerja", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // FEATURE: CATATAN PENGELUARAN SHIFT INI
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Payments, contentDescription = null, tint = OrangePrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Pengeluaran Kas Shift Ini", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Button(
                                        onClick = { showAddExpenseDialogLocal = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary.copy(alpha = 0.15f), contentColor = OrangeDark),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Catat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                HorizontalDivider()

                                if (activeShiftExpenses.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Belum ada pengeluaran kas selama shift ini.", color = Color.Gray, fontSize = 11.sp)
                                    }
                                } else {
                                    activeShiftExpenses.forEach { exp ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(exp.keterangan, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                                val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale("id", "ID"))
                                                Text(sdf.format(java.util.Date(exp.createdAt)), fontSize = 10.sp, color = Color.Gray)
                                            }
                                            Text(idrFormatter.format(exp.amount), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Red)
                                        }
                                    }
                                }
                            }
                        }

                        // FEATURE: RIWAYAT TRANSAKSI SHIFT INI WITH CORRECTION INTERACTION
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.History, contentDescription = null, tint = OrangePrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Riwayat Transaksi Shift Ini", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }

                                HorizontalDivider()

                                if (activeShiftTxs.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Belum ada transaksi penjualan selama shift ini.", color = Color.Gray, fontSize = 11.sp)
                                    }
                                } else {
                                    val sortedTxs = remember(activeShiftTxs) {
                                        activeShiftTxs.sortedByDescending { it.createdAt }
                                    }
                                    sortedTxs.forEach { tx ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedTxForReceipt = tx }
                                                .padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(tx.id, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = OrangePrimary)
                                                val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale("id", "ID"))
                                                Text("${sdf.format(java.util.Date(tx.createdAt))} • ${tx.metodeBayar}", fontSize = 10.sp, color = Color.Gray)
                                            }
                                            Text(idrFormatter.format(tx.total), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (tx.status == "lunas") Color(0xFF16A34A) else Color(0xFFEAB308))
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    }
                                }
                            }
                        }

                        // Dialog Form untuk Catat Pengeluaran Local
                        if (showAddExpenseDialogLocal) {
                            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            AlertDialog(
                                onDismissRequest = {
                                    showAddExpenseDialogLocal = false
                                    localExpenseNominal = ""
                                    localExpenseKet = ""
                                },
                                title = { Text("Pencatatan Pengeluaran", fontWeight = FontWeight.Bold) },
                                icon = { Icon(imageVector = Icons.Default.Payments, contentDescription = null, tint = OrangePrimary) },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("Catat uang keluar / operasional laci kasir selama shift saat ini.", fontSize = 11.sp, color = Color.Gray)
                                        OutlinedTextField(
                                            value = localExpenseNominal,
                                            onValueChange = { localExpenseNominal = it.filter { c -> c.isDigit() } },
                                            label = { Text("Nominal Pengeluaran (Rp) *") },
                                            modifier = Modifier.fillMaxWidth(),
                                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = localExpenseKet,
                                            onValueChange = { localExpenseKet = it },
                                            label = { Text("Keterangan Pengeluaran *") },
                                            placeholder = { Text("Contoh: Belanja es batu, Listrik") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            val amt = localExpenseNominal.toDoubleOrNull() ?: 0.0
                                            if (amt <= 0 || localExpenseKet.isBlank()) {
                                                Toast.makeText(context, "Semua kolom wajib diisi!", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            viewModel.recordExpense(amt, localExpenseKet) { success, error ->
                                                if (success) {
                                                    showAddExpenseDialogLocal = false
                                                    localExpenseNominal = ""
                                                    localExpenseKet = ""
                                                    Toast.makeText(context, "Pengeluaran berhasil dicatat!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Gagal mencatat pengeluaran: $error", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                                    ) {
                                        Text("Simpan")
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = {
                                            showAddExpenseDialogLocal = false
                                            localExpenseNominal = ""
                                            localExpenseKet = ""
                                        }
                                    ) {
                                        Text("Batal")
                                    }
                                }
                            )
                        }

                        // Dialog Struk Detail untuk Cashier
                        if (selectedTxForReceipt != null) {
                            val rx = selectedTxForReceipt!!
                            AlertDialog(
                                onDismissRequest = { selectedTxForReceipt = null },
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = OrangePrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Detail Struk Penjualan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                },
                                text = {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White)
                                            .padding(10.dp)
                                            .verticalScroll(rememberScrollState()),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(viewModel.currentBusiness.value?.namaBisnis ?: "KASIR PRO", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp, textAlign = TextAlign.Center)
                                        Text("---------------------------------", color = Color.Black)
                                        Text("No TRX: ${rx.id}", fontSize = 10.sp, color = Color.Black)
                                        Text("Kasir: ${rx.kasirNama}", fontSize = 10.sp, color = Color.Black)
                                        Text("---------------------------------", color = Color.Black)

                                        val itemsSplit = rx.itemsRaw.split(";").filter { it.isNotBlank() }
                                        itemsSplit.forEach { line ->
                                            val parts = line.split(":")
                                            if (parts.size >= 4) {
                                                val name = parts.getOrNull(1).orEmpty()
                                                val qty = parts.getOrNull(2)?.toIntOrNull() ?: 1
                                                val price = parts.getOrNull(3)?.toDoubleOrNull() ?: 0.0
                                                val disc = parts.getOrNull(5)?.toDoubleOrNull() ?: 0.0
                                                val sat = parts.getOrNull(6).orEmpty().takeIf { it.isNotBlank() } ?: "Pcs"
                                                val itemSub = (price - disc) * qty

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("$name x$qty $sat", fontSize = 10.sp, color = Color.Black)
                                                    Text(idrFormatter.format(itemSub), fontSize = 10.sp, color = Color.Black)
                                                }
                                            }
                                        }

                                        Text("---------------------------------", color = Color.Black)
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Subtotal", fontSize = 10.sp, color = Color.Black)
                                            Text(idrFormatter.format(rx.subtotal), fontSize = 10.sp, color = Color.Black)
                                        }
                                        if (rx.diskonTotal > 0) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Diskon Promo", fontSize = 10.sp, color = Color.Black)
                                                Text("-${idrFormatter.format(rx.diskonTotal)}", fontSize = 10.sp, color = Color.Black)
                                            }
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("TOTAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            Text(idrFormatter.format(rx.total), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("DIBAYAR", fontSize = 10.sp, color = Color.Black)
                                            Text(idrFormatter.format(rx.bayarNominal), fontSize = 10.sp, color = Color.Black)
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("KEMBALI", fontSize = 10.sp, color = Color.Black)
                                            Text(idrFormatter.format(rx.kembalian), fontSize = 10.sp, color = Color.Black)
                                        }
                                        Text("---------------------------------", color = Color.Black)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(OrangeLight, shape = RoundedCornerShape(8.dp))
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                "Saran: Untuk mengoreksi transaksi ini, buka menu Kasir POS Penjualan lalu pilih ikon menu (titik tiga) di pojok kanan atas.",
                                                fontSize = 11.sp,
                                                color = OrangeDark,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { selectedTxForReceipt = null }) {
                                        Text("Tutup Struk")
                                    }
                                }
                            )
                        }
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                                Text("Tidak Ada Shift Aktif", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Silakan masuk ke halaman Kasir/POS untuk memulai shift baru Anda dan memasukkan modal awal.", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Minimal settings logout
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch {
                                    viewModel.logout()
                                    viewModel.activeScreen.value = "login"
                                }
                            }.testTag("kasir_logout_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.Logout, contentDescription = "Log out", tint = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Keluar dari Akun Kasir", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }

                if (showManualCashInputDialog && activeShiftState != null) {
                    val expectedCash = activeShiftState!!.modalAwal + totalTunai
                    val manualCashDouble = manualCashValue.toDoubleOrNull() ?: 0.0
                    val selisih = manualCashDouble - expectedCash

                    AlertDialog(
                        onDismissRequest = {
                            showManualCashInputDialog = false
                            manualCashValue = ""
                        },
                        title = { Text("Tutup Shift & Hitung Uang Laci", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    "Uang kasir Tunai secara sistem yang harus ada di laci saat ini:",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    idrFormatter.format(expectedCash),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF16A34A)
                                )

                                OutlinedTextField(
                                    value = manualCashValue,
                                    onValueChange = { manualCashValue = it.filter { char -> char.isDigit() } },
                                    label = { Text("Uang Fisik di Laci Laci (Rp) *") },
                                    placeholder = { Text("Contoh: 150000") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("manual_drawer_cash_input")
                                )

                                HorizontalDivider()

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Selisih Uang Laci:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    val selisihText = if (selisih == 0.0) {
                                        "Sesuai (Rp 0)"
                                    } else if (selisih > 0.0) {
                                        "Surplus (+${idrFormatter.format(selisih)})"
                                    } else {
                                        "Minus (${idrFormatter.format(selisih)})"
                                    }
                                    val selisihColor = if (selisih == 0.0) {
                                        Color.DarkGray
                                    } else if (selisih > 0.0) {
                                        Color(0xFF16A34A)
                                    } else {
                                        Color(0xFFDC2626)
                                    }
                                    Text(selisihText, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = selisihColor)
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    shiftReportTransactions = activeShiftTxs
                                    viewModel.endShift(
                                        actualDrawerCash = manualCashDouble,
                                        selisih = selisih
                                    ) { reported ->
                                        showShiftReportState.value = reported
                                        showManualCashInputDialog = false
                                        manualCashValue = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                modifier = Modifier.testTag("confirm_end_shift_btn")
                            ) {
                                Text("Akhiri Shift & Simpan")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showManualCashInputDialog = false
                                    manualCashValue = ""
                                }
                            ) {
                                Text("Batal")
                            }
                        }
                    )
                }

                // Show Shift Report Modal popup on Shift end
                if (showShiftReportDialog != null) {
                    AlertDialog(
                        onDismissRequest = { /* force action choice */ },
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Assessment, contentDescription = null, tint = OrangePrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Laporan Akhir Shift", fontWeight = FontWeight.Bold)
                            }
                        },
                        text = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Yth. Kasir ${showShiftReportDialog.cashierName}, shift Anda telah berhasil diakhiri.", fontSize = 12.sp, color = Color.DarkGray)
                                HorizontalDivider()
                                
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Kasir:", fontSize = 11.sp, color = Color.Gray)
                                        Text(showShiftReportDialog.cashierName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Cabang:", fontSize = 11.sp, color = Color.Gray)
                                        Text(showShiftReportDialog.branchName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Mulai Shift:", fontSize = 11.sp, color = Color.Gray)
                                        Text(remember {
                                            val sdf = java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale("id", "ID"))
                                            sdf.format(java.util.Date(showShiftReportDialog.startTime))
                                        }, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Selesai Shift:", fontSize = 11.sp, color = Color.Gray)
                                        Text(remember {
                                            val sdf = java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale("id", "ID"))
                                            sdf.format(java.util.Date(showShiftReportDialog.endTime ?: System.currentTimeMillis()))
                                        }, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                
                                HorizontalDivider()

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Jumlah Transaksi:", fontSize = 11.sp, color = Color.Gray)
                                        Text("${shiftReportTransactions.size}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Uang Modal Awal:", fontSize = 11.sp, color = Color.Gray)
                                        Text(idrFormatter.format(showShiftReportDialog.modalAwal), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Pendapatan Tunai:", fontSize = 11.sp, color = Color.Gray)
                                        Text(idrFormatter.format(showShiftReportDialog.totalTunai), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Pendapatan Non-Tunai:", fontSize = 11.sp, color = Color.Gray)
                                        Text(idrFormatter.format(showShiftReportDialog.totalNonTunai), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Total Omset:", fontSize = 11.sp, color = Color.Gray)
                                        Text(idrFormatter.format(showShiftReportDialog.totalTransaksi), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OrangePrimary)
                                    }
                                }

                                HorizontalDivider()

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Tunai Seharusnya di Laci:", fontSize = 11.sp, color = Color.Gray)
                                    Text(idrFormatter.format(showShiftReportDialog.modalAwal + showShiftReportDialog.totalTunai), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Jumlah Uang Fisik Laci:", fontSize = 11.sp, color = Color.Gray)
                                    Text(idrFormatter.format(showShiftReportDialog.actualDrawerCash), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                                }

                                val selisihVal = showShiftReportDialog.selisih
                                val selisihColor = if (selisihVal == 0.0) Color.DarkGray else if (selisihVal > 0.0) Color(0xFF16A34A) else Color(0xFFDC2626)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Selisih Uang Laci:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    val labelText = if (selisihVal == 0.0) {
                                        "Sesuai (Rp 0)"
                                    } else if (selisihVal > 0.0) {
                                        "Surplus (+${idrFormatter.format(selisihVal)})"
                                    } else {
                                        "Minus (${idrFormatter.format(selisihVal)})"
                                    }
                                    Text(labelText, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = selisihColor)
                                }

                                HorizontalDivider()

                                Text("Log Transaksi Shift ini:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                                if (shiftReportTransactions.isEmpty()) {
                                    Text("Tidak ada transaksi selama shift ini.", fontSize = 11.sp, color = Color.Gray)
                                } else {
                                    Box(modifier = Modifier.heightIn(max = 120.dp).fillMaxWidth().verticalScroll(rememberScrollState())) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            shiftReportTransactions.forEach { tx ->
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("#${tx.id.takeLast(6)} • ${tx.metodeBayar}", fontSize = 10.sp, color = Color.Gray)
                                                    Text(idrFormatter.format(tx.total), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    scope.launch {
                                        viewModel.logout()
                                        viewModel.activeScreen.value = "login"
                                        showShiftReportState.value = null
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                            ) {
                                Text("Konfirmasi & Keluar")
                            }
                        }
                    )
                }

            } else {
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

                Text("Fitur Bisnis & Loyalitas (Premium)", fontWeight = FontWeight.Bold, color = OrangePrimary, fontSize = 14.sp)

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        // Pelanggan Row
                        Row(
                            modifier = Modifier
                                .clickable { activeSettingSubScreen = "PELANGGAN" }
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.People, contentDescription = null, tint = OrangePrimary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Loyalty Pelanggan & CRM")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!isPremium) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = Color(0xFFEAB308), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                        HorizontalDivider()

                        // Promo Row
                        Row(
                            modifier = Modifier
                                .clickable { activeSettingSubScreen = "PROMO" }
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.LocalActivity, contentDescription = null, tint = OrangePrimary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Manajemen Promo & Kupon")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!isPremium) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = Color(0xFFEAB308), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                        HorizontalDivider()

                        // Cabang Row
                        Row(
                            modifier = Modifier
                                .clickable { activeSettingSubScreen = "CABANG" }
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.AddBusiness, contentDescription = null, tint = OrangePrimary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Outlet Multi-Cabang")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!isPremium) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = Color(0xFFEAB308), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                        HorizontalDivider()

                        // Kasir Row
                        Row(
                            modifier = Modifier
                                .clickable { activeSettingSubScreen = "KASIR" }
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = OrangePrimary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Staf & Akun Kasir")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!isPremium) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = Color(0xFFEAB308), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Pengaturan Aplikasi", fontWeight = FontWeight.Bold, color = OrangePrimary, fontSize = 14.sp)

                // Layout settings preferences
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        // Edit Profil Toko & Struk
                        Row(
                            modifier = Modifier
                                .clickable { showEditShopProfile = true }
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Storefront, contentDescription = null, tint = OrangePrimary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Edit Profil Toko & Struk")
                            }
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                        HorizontalDivider()

                        // Pengaturan QRIS Pembayaran
                        Row(
                            modifier = Modifier
                                .clickable { showQrisSettings = true }
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, tint = OrangePrimary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Pengaturan QRIS Pembayaran")
                            }
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                        HorizontalDivider()

                        // Generate Kode Unik Pemilik (Premium Only)
                        Row(
                            modifier = Modifier
                                .clickable { 
                                    if (isPremium) {
                                        showOwnerCodeDialog = true
                                    } else {
                                        viewModel.showLimitPopup.value = "Fitur Kode Unik Otoritas Koreksi hanya untuk pengguna Premium. Upgrade sekarang!"
                                    }
                                }
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.VpnKey, contentDescription = null, tint = OrangePrimary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Kode Otoritas Koreksi")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!isPremium) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = Color(0xFFEAB308), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                        HorizontalDivider()

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

        Spacer(modifier = Modifier.height(24.dp))

        var selectedIsYearly by remember { mutableStateOf(false) }

        // Card Subscription pricing packs
        Card(
            onClick = { selectedIsYearly = false },
            modifier = Modifier.fillMaxWidth(),
            border = if (!selectedIsYearly) androidx.compose.foundation.BorderStroke(2.dp, OrangePrimary) else null,
            colors = CardDefaults.cardColors(containerColor = if (!selectedIsYearly) Slate700 else Slate800)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("PRO BULANAN", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    Text("Hemat waktu operasional kencang", fontSize = 11.sp, color = Color.LightGray)
                }
                Text("Rp 29.000 / bln", fontWeight = FontWeight.ExtraBold, color = OrangePrimary, fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            onClick = { selectedIsYearly = true },
            modifier = Modifier.fillMaxWidth(),
            border = if (selectedIsYearly) androidx.compose.foundation.BorderStroke(2.dp, Color.Green) else null,
            colors = CardDefaults.cardColors(containerColor = if (selectedIsYearly) Slate700 else Slate800)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("PRO TAHUNAN", fontWeight = FontWeight.Bold, color = Color.Green, fontSize = 16.sp)
                    Text("Hemat 42% paket langganan tahunan!", fontSize = 11.sp, color = Color.LightGray)
                }
                Text("Rp 199.000 / thn", fontWeight = FontWeight.ExtraBold, color = Color.Green, fontSize = 18.sp)
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
                    viewModel.upgradeToPremium(selectedIsYearly)
                    val label = if (selectedIsYearly) "Tahunan" else "Bulanan"
                    Toast.makeText(context, "SINKRONISASI MIDTRANS: Transaksi Sukses! Akun Anda aktif sebagai Premium Pro ($label).", Toast.LENGTH_LONG).show()
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

data class LocalExpenseItem(
    val id: String,
    val amount: Double,
    val createdAt: Long,
    val keterangan: String
)
