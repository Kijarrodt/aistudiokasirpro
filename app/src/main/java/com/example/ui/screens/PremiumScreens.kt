package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreens(viewModel: KasirViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val isPremium = user?.subscriptionStatus == "premium"

    var selectedModule by remember { mutableStateOf("LAPORAN") } // LAPORAN, HUTANG, PELANGGAN, PROMO, CABANG, KASIR

    val context = LocalContext.current

    if (!isPremium) {
        // Redirection screen to Pricing if not premium
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate900)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Fitur Pro Terkunci",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Menu Laporan Keuangan, Kelola Hutang, Multi-Cabang, Manajemen Promo, dan Loyalty Pelanggan hanya tersedia untuk pelanggan Premium Pro.",
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(30.dp))
                Button(
                    onClick = { viewModel.activeScreen.value = "premium_pricing" },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Upgrade Premium (Mulai Rp 29rb)", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = { viewModel.activeScreen.value = "home" }) {
                    Text("Kembali ke Beranda", color = Color.Gray)
                }
            }
        }
    } else {
        // Premium user layout
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Area Premium Pro", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.activeScreen.value = "home" }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            bottomBar = {
                // Secondary horizontal controller for selecting current premium modules
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            "LAPORAN" to Icons.Default.BarChart,
                            "HUTANG" to Icons.Default.PendingActions,
                            "PELANGGAN" to Icons.Default.People,
                            "PROMO" to Icons.Default.LocalActivity,
                            "CABANG" to Icons.Default.AddBusiness,
                            "KASIR" to Icons.Default.Badge
                        ).forEach { (mod, icon) ->
                            val isSel = selectedModule == mod
                            Button(
                                onClick = { selectedModule = mod },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSel) OrangePrimary else MaterialTheme.colorScheme.surface
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(imageVector = icon, contentDescription = null, tint = if (isSel) Color.White else OrangePrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(mod, fontSize = 11.sp, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (selectedModule) {
                    "LAPORAN" -> PremiumLaporanTab(viewModel)
                    "HUTANG" -> PremiumHutangTab(viewModel)
                    "PELANGGAN" -> PremiumPelangganTab(viewModel)
                    "PROMO" -> PremiumPromoTab(viewModel)
                    "CABANG" -> PremiumCabangTab(viewModel)
                    "KASIR" -> PremiumKasirTab(viewModel)
                }
            }
        }
    }
}

// ==== 1. LAPORAN KEUNGAN ====
@Composable
fun PremiumLaporanTab(viewModel: KasirViewModel) {
    val txs by viewModel.transactions.collectAsState()
    val branchesList by viewModel.branches.collectAsState()
    val idrFormatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    idrFormatter.maximumFractionDigits = 0

    var selectedBranch by remember { mutableStateOf("Semua Cabang") }
    var reportInterval by remember { mutableStateOf("BULANAN") } // HARIAN, BULANAN, TAHUNAN

    val filtered = txs.filter {
        selectedBranch == "Semua Cabang" // Aggregate filter simulator
    }

    val finalIncome = filtered.sumOf { it.total }
    val calculatedCapital = filtered.sumOf { it.subtotal * 0.55 } // Modal cost estimated mapping
    val calculatedNetProfit = finalIncome - calculatedCapital
    val countTx = filtered.size
    val averageBasket = if (countTx > 0) finalIncome / countTx else 0.0

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Interval toggles
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("HARIAN", "BULANAN", "TAHUNAN").forEach { mode ->
                    val sel = reportInterval == mode
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { reportInterval = mode },
                        colors = CardDefaults.cardColors(containerColor = if (sel) OrangePrimary else MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = mode,
                            modifier = Modifier.padding(10.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (sel) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Summary Statistics Box cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("TOTAL PENDAPATAN", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(idrFormatter.format(finalIncome), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OrangePrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Harga Pokok Modal (HPP):", fontSize = 11.sp, color = Color.Gray)
                            Text(idrFormatter.format(calculatedCapital), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("KEUNTUNGAN BERSIH (NET):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(idrFormatter.format(calculatedNetProfit), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("RATA-RATA TRANSAKSI", fontSize = 10.sp, color = Color.Gray)
                            Text(idrFormatter.format(averageBasket), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("TOTAL TRANSAKSI", fontSize = 10.sp, color = Color.Gray)
                            Text("$countTx Transaksi", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Grafik Bar Terlaris Simulated Canvas Panel
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Grafik Penjualan Terlaris", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Icon(imageVector = Icons.Default.Leaderboard, contentDescription = null, tint = OrangePrimary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Simple interactive bar graph rows
                    listOf(
                        "Kopi Gula Aren" to 0.9f,
                        "Roti Bakar Cokelat" to 0.65f,
                        "Indomie Double Pedas" to 0.4f
                    ).forEach { (pName, percent) ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(pName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(Color.LightGray.copy(alpha = 0.3f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(percent)
                                        .fillMaxHeight()
                                        .background(OrangePrimary)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Export PDF Actions triggers
        item {
            Button(
                onClick = {
                    // Simulates PDF compilation & sharing intent logs
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "KASIR PRO - LAPORAN KEUANGAN BULANAN\nToko: Toko Kasir Pro Utama\n\nTotal Omset: ${idrFormatter.format(finalIncome)}\nLaba Bersih: ${idrFormatter.format(calculatedNetProfit)}")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Export PDF Laporan Keuangan"))
                    Toast.makeText(context, "Dokumen PDF export sukses dibuat!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export PDF Laporan Kasir", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// ==== 2. KELOLA HUTANG / PIUTANG ====
@Composable
fun PremiumHutangTab(viewModel: KasirViewModel) {
    val debtsList by viewModel.debts.collectAsState()
    val idrFormatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    idrFormatter.maximumFractionDigits = 0

    val unpaid = debtsList.filter { it.status == "belum" }
    val paid = debtsList.filter { it.status == "lunas" }

    var showUnpaidByMe by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TOTAL PIUTANG HAMPIR JATUH TEMPO", fontSize = 11.sp, color = Color(0xFFB91C1C), fontWeight = FontWeight.Bold)
                    Text(idrFormatter.format(unpaid.sumOf { it.jumlah }), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFB91C1C))
                }
            }
        }

        // Toggles list harian lunas/belum
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { showUnpaidByMe = true },
                    colors = ButtonDefaults.buttonColors(containerColor = if (showUnpaidByMe) OrangePrimary else MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Belum Lunas (${unpaid.size})", color = if (showUnpaidByMe) Color.White else MaterialTheme.colorScheme.onSurface)
                }
                Button(
                    onClick = { showUnpaidByMe = false },
                    colors = ButtonDefaults.buttonColors(containerColor = if (!showUnpaidByMe) OrangePrimary else MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Sudah Lunas (${paid.size})", color = if (!showUnpaidByMe) Color.White else MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        val activeList = if (showUnpaidByMe) unpaid else paid
        if (activeList.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Tidak ada rekaman hutang.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            items(activeList) { debt ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(debt.pelangganNama, fontWeight = FontWeight.Bold)
                            Text("Ref Transaksi ID: ${debt.transaksiId}", fontSize = 11.sp, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(idrFormatter.format(debt.jumlah), fontWeight = FontWeight.Bold, color = if (debt.status == "belum") Color.Red else Color(0xFF15803D))
                            if (debt.status == "belum") {
                                Button(
                                    onClick = { viewModel.settleDebt(debt.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("LUNASKAN", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==== 3. PELANGGAN CRM ====
@Composable
fun PremiumPelangganTab(viewModel: KasirViewModel) {
    val customersList by viewModel.customers.collectAsState()
    var showAddCustDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Directory Pelanggan Loyal", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Button(onClick = { showAddCustDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pelanggan Baru", fontSize = 12.sp)
                }
            }
        }

        if (customersList.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Belum ada pelanggan terdaftar.", color = Color.Gray)
                }
            }
        } else {
            items(customersList) { c ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(c.nama, fontWeight = FontWeight.Bold)
                            Text("No HP: ${c.nomorHp}", fontSize = 11.sp, color = Color.Gray)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(OrangeLight)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("${c.totalPoin} Poin", fontWeight = FontWeight.Bold, color = OrangeDark, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    if (showAddCustDialog) {
        var cNama by remember { mutableStateOf("") }
        var cHp by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCustDialog = false },
            title = { Text("Daftarkan Pelanggan Baru") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = cNama, onValueChange = { cNama = it }, label = { Text("Nama Pelanggan") })
                    OutlinedTextField(value = cHp, onValueChange = { cHp = it }, label = { Text("No Handphone (WhatsApp)") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cNama.isNotBlank() && cHp.isNotBlank()) {
                            viewModel.addCustomer(cNama, cHp)
                        }
                        showAddCustDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Daftar")
                }
            }
        )
    }
}

// ==== 4. PROMO & VOUCHER DISKON ====
@Composable
fun PremiumPromoTab(viewModel: KasirViewModel) {
    val promosList by viewModel.promos.collectAsState()
    var showAddPromoDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Kupon / Voucher Belanja", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Button(onClick = { showAddPromoDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)) {
                    Text("Buat Voucher", fontSize = 12.sp)
                }
            }
        }

        if (promosList.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Belum ada promo aktif.", color = Color.Gray)
                }
            }
        } else {
            items(promosList) { p ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(p.nama, fontWeight = FontWeight.Bold)
                            Text("Kode: ${p.kode} • Min Tx: Rp${p.minTransaksi.toInt()}", fontSize = 11.sp, color = Color.Gray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = p.isActive,
                                onCheckedChange = { viewModel.togglePromo(p.id, it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = OrangePrimary, checkedTrackColor = OrangeLight)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddPromoDialog) {
        var pNama by remember { mutableStateOf("") }
        var pKode by remember { mutableStateOf("") }
        var pTipe by remember { mutableStateOf("diskon_persen") }
        var pNilai by remember { mutableStateOf("") }
        var pMinTx by remember { mutableStateOf("") }
        var pDays by remember { mutableStateOf("30") }

        AlertDialog(
            onDismissRequest = { showAddPromoDialog = false },
            title = { Text("Tambahkan Voucher Promo") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
                ) {
                    OutlinedTextField(value = pNama, onValueChange = { pNama = it }, label = { Text("Nama Acara Promo") })
                    OutlinedTextField(value = pKode, onValueChange = { pKode = it }, label = { Text("Kode Promo (eg: HEMAT5)") })
                    
                    Text("Tipe Potongan:")
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = pTipe == "diskon_persen", onClick = { pTipe = "diskon_persen" })
                            Text("Persentase (%)", fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = pTipe == "diskon_nominal", onClick = { pTipe = "diskon_nominal" })
                            Text("Nominal (Rp)", fontSize = 12.sp)
                        }
                    }

                    OutlinedTextField(value = pNilai, onValueChange = { pNilai = it }, label = { Text("Nilai Diskon") })
                    OutlinedTextField(value = pMinTx, onValueChange = { pMinTx = it }, label = { Text("Min Transaksi Belanja") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pNama.isNotBlank() && pKode.isNotBlank() && pNilai.isNotBlank()) {
                            viewModel.addPromo(
                                nama = pNama,
                                tipe = pTipe,
                                nilai = pNilai.toDoubleOrNull() ?: 0.0,
                                minTx = pMinTx.toDoubleOrNull() ?: 0.0,
                                kode = pKode.uppercase(),
                                durationDays = pDays.toIntOrNull() ?: 30
                            )
                        }
                        showAddPromoDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Aktifkan Promo")
                }
            }
        )
    }
}

// ==== 5. MULTI CABANG MANAGEMENT ====
@Composable
fun PremiumCabangTab(viewModel: KasirViewModel) {
    val branchesList by viewModel.branches.collectAsState()
    val cashiersList by viewModel.cashiers.collectAsState()
    var showAddBranchDialog by remember { mutableStateOf(false) }
    var editBranchTarget by remember { mutableStateOf<BranchEntity?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Daftar Cabang Usaha", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Button(
                    onClick = { showAddBranchDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambah Cabang", fontSize = 12.sp)
                }
            }
        }

        if (branchesList.size <= 1 && branchesList.none { it.id != "branch-1" }) { // includes default main branch only
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Cabang Utama (Default)", fontWeight = FontWeight.Bold, color = OrangePrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Anda belum mendaftarkan cabang tambahan. Tambahkan cabang outlet baru untuk memantau data kasir terpisah secara realtime!", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        } else {
            items(branchesList) { br ->
                val assignedCashiers = cashiersList.filter { it.assignedBranchId == br.id }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (br.id == "branch-1") Icons.Default.Storefront else Icons.Default.AddBusiness,
                                        contentDescription = null,
                                        tint = OrangePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(br.namaCabang, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(br.alamat, fontSize = 12.sp, color = Color.Gray)
                            }
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { editBranchTarget = br }) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Cabang", tint = OrangePrimary)
                                }
                                if (br.id != "branch-1") {
                                    IconButton(onClick = { viewModel.removeBranch(br.id) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus Cabang", tint = Color.Red)
                                    }
                                }
                            }
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        
                        Text(
                            text = "Staf Kasir Terhubung (${assignedCashiers.size}):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangeDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (assignedCashiers.isEmpty()) {
                            Text("Belum ada kasir yang ditugaskan ke cabang ini.", fontSize = 11.sp, color = Color.Gray)
                        } else {
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                assignedCashiers.forEach { cs ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(OrangeLight)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(cs.nama, fontSize = 11.sp, color = OrangeDark, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddBranchDialog) {
        var bNama by remember { mutableStateOf("") }
        var bAlamat by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddBranchDialog = false },
            title = { Text("Tambah Outlet Cabang Baru", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Daftarkan cabang usaha baru Anda untuk manajemen produk dan alokasi kasir terpisah.", fontSize = 12.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = bNama,
                        onValueChange = { bNama = it },
                        label = { Text("Nama Cabang") },
                        singleLine = true,
                        placeholder = { Text("contoh: Cabang Bandung") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bAlamat,
                        onValueChange = { bAlamat = it },
                        label = { Text("Alamat lengkap") },
                        placeholder = { Text("contoh: Jl. Merdeka No. 45, Bandung") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (bNama.isNotBlank()) {
                            viewModel.addBranch(bNama, bAlamat)
                        }
                        showAddBranchDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Daftar Cabang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBranchDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    editBranchTarget?.let { target ->
        var bNama by remember { mutableStateOf(target.namaCabang) }
        var bAlamat by remember { mutableStateOf(target.alamat) }

        AlertDialog(
            onDismissRequest = { editBranchTarget = null },
            title = { Text("Edit Outlet Cabang", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = bNama,
                        onValueChange = { bNama = it },
                        label = { Text("Nama Cabang") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bAlamat,
                        onValueChange = { bAlamat = it },
                        label = { Text("Alamat lengkap") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (bNama.isNotBlank()) {
                            viewModel.editBranch(target.id, bNama, bAlamat)
                        }
                        editBranchTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Simpan Perubahan")
                }
            },
            dismissButton = {
                TextButton(onClick = { editBranchTarget = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

// ==== 6. MANAJEMEN AKUN KASIR ====
@Composable
fun PremiumKasirTab(viewModel: KasirViewModel) {
    val cashiersList by viewModel.cashiers.collectAsState()
    val branchesList by viewModel.branches.collectAsState()
    
    var showInviteDialog by remember { mutableStateOf(false) }
    var inviteEmail by remember { mutableStateOf("") }
    
    var assignBranchTarget by remember { mutableStateOf<UserEntity?>(null) }
    var simulateRegisterTarget by remember { mutableStateOf<UserEntity?>(null) }
    
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Staf & Akun Kasir", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Button(
                    onClick = { showInviteDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Undang Kasir", fontSize = 12.sp)
                }
            }
        }

        if (cashiersList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = OrangePrimary.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Belum ada kasir terdaftar", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Undang kasir pertama Anda melalui tombol di atas untuk mendelegasikan transaksi kasir.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        } else {
            items(cashiersList) { cs ->
                val assignedBranch = branchesList.find { it.id == cs.assignedBranchId }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (cs.role == "kasir_invited") OrangeLight.copy(alpha = 0.6f) else OrangeLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (cs.role == "kasir_invited") Icons.Default.Email else Icons.Default.Badge,
                                        contentDescription = null,
                                        tint = OrangePrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(cs.nama, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(cs.email, fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                if (cs.role == "kasir_invited") {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFFEF3C7))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("PENDING", fontSize = 9.sp, color = Color(0xFFD97706), fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFDCFCE7))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("AKTIF", fontSize = 9.sp, color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (cs.role == "kasir_invited") "Dibuat: " + formatRelativeTime(cs.createdAt) else "Aktivitas terakhir: " + formatLastActiveString(cs.lastActiveAt),
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Display outlet info and button to assign branch
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(imageVector = Icons.Default.Home, contentDescription = null, modifier = Modifier.size(14.dp), tint = OrangePrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Outlet: ${assignedBranch?.namaCabang ?: "Belum dialokasikan"}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (assignedBranch != null) MaterialTheme.colorScheme.onSurface else Color.Gray
                                )
                            }
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (cs.role == "kasir_invited") {
                                    Button(
                                        onClick = { simulateRegisterTarget = cs },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Simulasi Daftar", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Button(
                                        onClick = { assignBranchTarget = cs },
                                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary.copy(alpha = 0.15f)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Atur Cabang", fontSize = 10.sp, color = OrangePrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                                
                                IconButton(
                                    onClick = { 
                                        viewModel.deleteCashier(cs.uid)
                                        Toast.makeText(context, "Akun kasir dan data terkait berhasil dihapus", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus Kasir", tint = Color.Red, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showInviteDialog) {
        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            title = { Text("Undang Staf Kasir Baru", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Sistem akan menyimpan data undangan kasir. Kasir tersebut akan otomatis terhubung ke Toko Anda setelah melakukan pendaftaran.", fontSize = 12.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = inviteEmail,
                        onValueChange = { inviteEmail = it },
                        label = { Text("Email Kasir") },
                        singleLine = true,
                        placeholder = { Text("contoh: budi@kopikita.id") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inviteEmail.isNotBlank()) {
                            viewModel.inviteKasir(inviteEmail)
                            Toast.makeText(context, "Sistem: Undangan email berhasil disimulasikan!", Toast.LENGTH_SHORT).show()
                            inviteEmail = ""
                        }
                        showInviteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Kirim Undangan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInviteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    assignBranchTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { assignBranchTarget = null },
            title = { Text("Tugaskan Kasir ke Cabang", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pilih outlet cabang tempat kasir '${target.nama}' akan ditempatkan:", fontSize = 12.sp, color = Color.Gray)
                    
                    branchesList.forEach { br ->
                        val isCurrent = target.assignedBranchId == br.id
                        Card(
                            onClick = {
                                viewModel.assignCashierToBranch(target.uid, br.id)
                                assignBranchTarget = null
                                Toast.makeText(context, "${target.nama} berhasil ditugaskan ke ${br.namaCabang}", Toast.LENGTH_SHORT).show()
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) OrangeLight else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(br.namaCabang, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    if (isCurrent) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { assignBranchTarget = null }) {
                    Text("Batal")
                }
            }
        )
    }

    simulateRegisterTarget?.let { target ->
        var registerNama by remember { mutableStateOf("") }
        var registerPassword by remember { mutableStateOf("pass123") }

        AlertDialog(
            onDismissRequest = { simulateRegisterTarget = null },
            title = { Text("Pendaftaran Form Kasir (Simulasi)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Simulasi Halaman Registrasi yang diakses staf kasir melalui link undangan email ke: ${target.email}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    OutlinedTextField(
                        value = target.email,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Email (Terkunci)") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = registerNama,
                        onValueChange = { registerNama = it },
                        label = { Text("Nama Lengkap Kasir") },
                        placeholder = { Text("budi") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = registerPassword,
                        onValueChange = { registerPassword = it },
                        label = { Text("Buat Password Akun") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (registerNama.isNotBlank()) {
                            viewModel.registerInvitedCashier(target.uid, registerNama)
                            simulateRegisterTarget = null
                            Toast.makeText(context, "Akun kasir berhasil didaftarkan dan terhubung otomatis!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Mohon lengkapi nama kasir!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Daftar Sekarang")
                }
            },
            dismissButton = {
                TextButton(onClick = { simulateRegisterTarget = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    if (diff < 60_000) return "Baru saja"
    if (diff < 3600_000) return "${diff / 60_000} menit lalu"
    if (diff < 86400_000) return "${diff / 3600_000} jam lalu"
    val sdf = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

fun formatLastActiveString(timestamp: Long?): String {
    if (timestamp == null) return "Belum pernah aktif"
    return formatRelativeTime(timestamp)
}
