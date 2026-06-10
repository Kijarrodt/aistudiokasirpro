package com.kasirpro.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import com.kasirpro.app.data.local.*
import com.kasirpro.app.ui.viewmodel.KasirViewModel
import com.kasirpro.app.data.repository.ShiftReport
import com.kasirpro.app.ui.theme.*
import java.text.NumberFormat
import java.util.Locale
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreens(viewModel: KasirViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val isPremium = user?.isPremium ?: false

    var selectedModule by remember { mutableStateOf("LAPORAN") } // LAPORAN, HUTANG, CRM POIN

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
                    text = "Menu Laporan Keuangan, dan Kelola Hutang hanya tersedia untuk pelanggan Premium Pro.",
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
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            "LAPORAN" to Icons.Default.BarChart,
                            "HUTANG" to Icons.Default.PendingActions,
                            "CRM POIN" to Icons.Default.CardGiftcard
                        ).forEach { (mod, icon) ->
                            val isSel = selectedModule == mod
                            Button(
                                onClick = { selectedModule = mod },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSel) OrangePrimary else MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.weight(1f),
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
                    "CRM POIN" -> PremiumPelangganTab(viewModel)
                }
            }
        }
    }
}

data class ExpenseItem(
    val id: String,
    val amount: Double,
    val createdAt: Long
)

// ==== 1. LAPORAN KEUNGAN ====
@Composable
fun PremiumLaporanTab(viewModel: KasirViewModel) {
    val txs by viewModel.transactions.collectAsState()
    val branchesList by viewModel.branches.collectAsState()
    val productsList by viewModel.products.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    
    val idrFormatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    idrFormatter.maximumFractionDigits = 0

    val isAtLeastProfesional = user?.isAtLeastProfesional ?: false
    val isAtLeastBisnis = user?.isAtLeastBisnis ?: false

    var selectedBranchId by remember { mutableStateOf("all") }
    var selectedCashierId by remember { mutableStateOf("all") }
    var reportInterval by remember { mutableStateOf(if (user?.isAtLeastProfesional == true) "BULANAN" else "HARIAN") } // HARIAN, MINGGUAN, BULANAN
    var isShiftsExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(isAtLeastProfesional, user) {
        if (!isAtLeastProfesional && reportInterval == "BULANAN") {
            reportInterval = "HARIAN"
        }
    }

    LaunchedEffect(isAtLeastBisnis, branchesList, user) {
        if (!isAtLeastBisnis && selectedBranchId == "all" && branchesList.isNotEmpty()) {
            val defaultBranch = user?.assignedBranchId ?: branchesList.firstOrNull()?.id ?: "all"
            selectedBranchId = defaultBranch
        }
    }

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var editExpenseNominal by remember { mutableStateOf("") }
    var editExpenseKet by remember { mutableStateOf("") }

    // Fetch user owner ID
    val ownerId = remember(user) {
        if (user?.role == "kasir" || user?.role == "kasir_invited") user?.ownerId else user?.uid
    }

    // Dynamic, Real-time Expenses and Shifts from Firebase Firestore
    val expensesState = remember { mutableStateOf<List<ExpenseItem>>(emptyList()) }
    val shiftsState = remember { mutableStateOf<List<ShiftReport>>(emptyList()) }

    // Collect local offline expenses
    val localExpensesList by viewModel.localExpensesList.collectAsState()

    // Merge remote and local expenses dynamically
    val mergedExpenses = remember(expensesState.value, localExpensesList) {
        val mergedList = mutableListOf<ExpenseItem>()
        // Add firestore expenses
        mergedList.addAll(expensesState.value)
        // Add local expenses if not already in the list
        localExpensesList.forEach { exp ->
            val id = exp["id"] as? String ?: ""
            if (mergedList.none { it.id == id }) {
                val amt = exp["amount"] as? Double ?: 0.0
                val date = exp["createdAt"] as? Long ?: System.currentTimeMillis()
                mergedList.add(ExpenseItem(id, amt, date))
            }
        }
        mergedList.sortedByDescending { it.createdAt }
    }

    DisposableEffect(ownerId) {
        if (ownerId.isNullOrBlank()) return@DisposableEffect onDispose {}
        
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        
        // 1. Real-time Expenses Snapshot Listener
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
                        ExpenseItem(doc.id, amt, date)
                    }
                    expensesState.value = list
                }
            }
            
        // 2. Real-time Shifts Snapshot Listener
        val shiftsListener = db.collection("shifts")
            .whereEqualTo("ownerId", ownerId)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val map = doc.data ?: return@mapNotNull null
                        ShiftReport.fromMap(map.toMutableMap().apply { put("id", doc.id) })
                    }.sortedByDescending { it.startTime }
                    shiftsState.value = list
                }
            }
            
        onDispose {
            expensesListener.remove()
            shiftsListener.remove()
        }
    }

    // Map Product ID to Cost Price (hargaModal) for perfect Profit calculation
    val productCostMap = remember(productsList) {
        productsList.associate { it.id to it.hargaModal }
    }

    // Time ranges definitions
    val now = System.currentTimeMillis()
    val calendar = java.util.Calendar.getInstance()

    // Today start (Harian)
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
    calendar.set(java.util.Calendar.MINUTE, 0)
    calendar.set(java.util.Calendar.SECOND, 0)
    calendar.set(java.util.Calendar.MILLISECOND, 0)
    val todayStart = calendar.timeInMillis

    // 7 Days ago (Mingguan)
    val sevenDaysAgoStart = now - (7L * 24 * 60 * 60 * 1000)

    // Month start (Bulan ini)
    val monthCalendar = java.util.Calendar.getInstance()
    monthCalendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
    monthCalendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
    monthCalendar.set(java.util.Calendar.MINUTE, 0)
    monthCalendar.set(java.util.Calendar.SECOND, 0)
    monthCalendar.set(java.util.Calendar.MILLISECOND, 0)
    val monthStart = monthCalendar.timeInMillis

    // Filter transaction lists based on intervals and selectors
    val filtered = txs.filter { tx ->
        val matchBranch = (selectedBranchId == "all" || tx.branchId == selectedBranchId)
        val matchCashier = (selectedCashierId == "all" || tx.kasirId == selectedCashierId)
        
        val matchInterval = when (reportInterval) {
            "HARIAN" -> tx.createdAt >= todayStart
            "MINGGUAN" -> tx.createdAt >= sevenDaysAgoStart
            "BULANAN" -> tx.createdAt >= monthStart
            else -> true
        }
        matchBranch && matchCashier && matchInterval
    }

    // Calculate Pendapatan, HPP, and Keuntungan from products in transactions
    val finalIncome = filtered.sumOf { it.actualIncome }
    
    var calculatedCapital = 0.0
    filtered.forEach { tx ->
        var txHpp = 0.0
        val itemsSplit = tx.itemsRaw.split(";").filter { it.isNotBlank() }
        itemsSplit.forEach { line ->
            val parts = line.split(":")
            if (parts.size >= 3) {
                val pId = parts[0]
                val qty = parts[2].toIntOrNull() ?: 1
                val sellPrice = parts.getOrNull(3)?.toDoubleOrNull() ?: 0.0
                val diskon = parts.getOrNull(5)?.toDoubleOrNull() ?: 0.0
                val finalItemPrice = sellPrice - diskon
                
                val itemCost = productCostMap[pId] ?: (finalItemPrice * 0.55)
                txHpp += itemCost * qty
            }
        }
        if (txHpp == 0.0 && tx.total > 0.0) {
            txHpp = tx.total * 0.55
        }
        calculatedCapital += txHpp
    }

    val calculatedNetProfit = finalIncome - calculatedCapital

    // Filter historical expenses based on interval
    val filteredExpenses = mergedExpenses.filter { exp ->
        when (reportInterval) {
            "HARIAN" -> exp.createdAt >= todayStart
            "MINGGUAN" -> exp.createdAt >= sevenDaysAgoStart
            "BULANAN" -> exp.createdAt >= monthStart
            else -> true
        }
    }
    val totalExpensesValue = filteredExpenses.sumOf { it.amount }
    val finalFinancialNet = calculatedNetProfit - totalExpensesValue

    val countTx = filtered.size
    val averageBasket = if (countTx > 0) finalIncome / countTx else 0.0

    // Top Selling Products parsed dynamically from itemsRaw
    val topProducts = remember(filtered) {
        val map = mutableMapOf<String, Int>()
        filtered.forEach { tx ->
            val itemsSplit = tx.itemsRaw.split(";").filter { it.isNotBlank() }
            itemsSplit.forEach { line ->
                val parts = line.split(":")
                if (parts.size >= 2) {
                    val name = parts[1]
                    val qty = parts.getOrNull(2)?.toIntOrNull() ?: 1
                    map[name] = (map[name] ?: 0) + qty
                }
            }
        }
        map.toList().sortedByDescending { it.second }.take(4)
    }

    // Daily Sales Graph (7 Days history) from real Firestore/Room transactions
    val dailyIncomeList = remember(filtered) {
        val list = mutableListOf<Pair<String, Double>>()
        val sdf = java.text.SimpleDateFormat("dd MMM", java.util.Locale("id", "ID"))
        for (i in 6 downTo 0) {
            val dateCal = java.util.Calendar.getInstance()
            dateCal.add(java.util.Calendar.DAY_OF_YEAR, -i)
            
            dateCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            dateCal.set(java.util.Calendar.MINUTE, 0)
            dateCal.set(java.util.Calendar.SECOND, 0)
            dateCal.set(java.util.Calendar.MILLISECOND, 0)
            val dayStart = dateCal.timeInMillis
            
            dateCal.set(java.util.Calendar.HOUR_OF_DAY, 23)
            dateCal.set(java.util.Calendar.MINUTE, 59)
            dateCal.set(java.util.Calendar.SECOND, 59)
            val dayEnd = dateCal.timeInMillis
            
            val totalForDay = filtered.filter { it.createdAt in dayStart..dayEnd }.sumOf { it.actualIncome }
            list.add(sdf.format(dateCal.time) to totalForDay)
        }
        list
    }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Filters selector card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Filter Laporan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    
                    // Branch Selector Row
                    Column {
                        Text("Berdasarkan Cabang:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // "Semua" option
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedBranchId == "all") OrangePrimary else MaterialTheme.colorScheme.surface)
                                    .clickable {
                                        if (isAtLeastBisnis) {
                                            selectedBranchId = "all"
                                        } else {
                                            viewModel.triggerUpgradePopup("Paket Bisnis")
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Semua Cabang", fontSize = 11.sp, color = if (selectedBranchId == "all") Color.White else MaterialTheme.colorScheme.onSurface)
                            }

                            branchesList.forEach { br ->
                                val isSelected = selectedBranchId == br.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) OrangePrimary else MaterialTheme.colorScheme.surface)
                                        .clickable { selectedBranchId = br.id }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(br.namaCabang, fontSize = 11.sp, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }

                    // Cashier Selector Row
                    Column {
                        Text("Berdasarkan Kasir / Staf:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // "Semua" option
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedCashierId == "all") OrangePrimary else MaterialTheme.colorScheme.surface)
                                    .clickable { selectedCashierId = "all" }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Semua Kasir", fontSize = 11.sp, color = if (selectedCashierId == "all") Color.White else MaterialTheme.colorScheme.onSurface)
                            }

                            val distinctCashiers = txs.map { it.kasirId to it.kasirNama }.distinct()
                            distinctCashiers.forEach { (cId, cName) ->
                                if (cId.isNotEmpty()) {
                                    val isSelected = selectedCashierId == cId
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) OrangePrimary else MaterialTheme.colorScheme.surface)
                                            .clickable { selectedCashierId = cId }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(cName, fontSize = 11.sp, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Interval toggles
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("HARIAN", "MINGGUAN", "BULANAN").forEach { mode ->
                    val sel = reportInterval == mode
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (mode == "BULANAN" && !isAtLeastProfesional) {
                                    viewModel.triggerUpgradePopup("Paket Profesional")
                                } else {
                                    reportInterval = mode
                                }
                            },
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
                            Text("Total Pengeluaran (Beban):", fontSize = 11.sp, color = Color.Gray)
                            Text(idrFormatter.format(totalExpensesValue), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFB91C1C))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Laba Bersih Finansial:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(idrFormatter.format(finalFinancialNet), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (finalFinancialNet >= 0) Color(0xFF15803D) else Color(0xFFB91C1C))
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { showAddExpenseDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary.copy(alpha = 0.1f)),
                            border = BorderStroke(1.dp, OrangePrimary),
                            modifier = Modifier.fillMaxWidth().testTag("add_expense_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tambah Pengeluaran", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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

        // Laporan breakdown per cabang dan per kasir
        item {
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
                        Text("Breakdown Omset Akhir Bulan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Icon(imageVector = Icons.Default.Summarize, contentDescription = null, tint = OrangePrimary)
                    }
                    Text("Berikut rangkuman performa omset per Cabang dan per Staf Kasir kontributor saat ini.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))
                    
                    // Group by Branch
                    Text("Penjualan Per Cabang:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OrangePrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    val branchGroups = filtered.groupBy { it.branchId }
                    if (branchGroups.isEmpty()) {
                        Text("Belum ada omset penjualan cabang.", fontSize = 11.sp, color = Color.Gray)
                    } else {
                        branchGroups.forEach { (bId, bTxs) ->
                            val bName = branchesList.find { it.id == bId }?.namaCabang ?: "Cabang Utama"
                            val bSum = bTxs.sumOf { it.actualIncome }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Home, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(bName, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                                Text(idrFormatter.format(bSum), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Group by Cashier
                    Text("Penjualan Per Staf Kasir:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OrangePrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    val cashierGroups = filtered.groupBy { it.kasirId to it.kasirNama }
                    if (cashierGroups.isEmpty()) {
                        Text("Belum ada omset penjualan kasir.", fontSize = 11.sp, color = Color.Gray)
                    } else {
                        cashierGroups.forEach { (key, cTxs) ->
                            val (cId, cName) = key
                            val cSum = cTxs.sumOf { it.actualIncome }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (cName.isNotBlank()) cName else "ID: $cId", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                                Text(idrFormatter.format(cSum), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // === REALTIME CASHER SHIFT HISTORICAL TRACKING ===
        item {
            if (isAtLeastProfesional) {
                val filteredShifts = remember(shiftsState.value, selectedBranchId, selectedCashierId) {
                    shiftsState.value.filter { shift ->
                        val matchBranch = (selectedBranchId == "all" || shift.branchId == selectedBranchId)
                        val matchCashier = (selectedCashierId == "all" || shift.cashierId == selectedCashierId)
                        matchBranch && matchCashier
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("historic_shifts_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Riwayat Laporan Shift Kasir", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Icon(imageVector = Icons.Default.SupervisorAccount, contentDescription = null, tint = OrangePrimary)
                        }
                        Text("Pantau uang modal awal, omset tunai/non-tunai, dan pencocokan uang kas fisik di laci per sesi kasir langsung dari Firestore.", fontSize = 11.sp, color = Color.Gray)

                        HorizontalDivider()

                        if (filteredShifts.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                Text("Belum ada pertanggungjawaban shift kasir tercatat sesuai filter saat ini.", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                            }
                        } else {
                            val displayShifts = if (isShiftsExpanded) filteredShifts else filteredShifts.take(3)
                            displayShifts.forEach { shift ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text(shift.cashierName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (shift.status == "aktif") Color(0xFFFEF08A) else Color(0xFFDCFCE7))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    shift.status.uppercase(),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (shift.status == "aktif") Color(0xFF854D0E) else Color(0xFF15803D)
                                                )
                                            }
                                        }
                                        
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Cabang:", fontSize = 11.sp, color = Color.Gray)
                                            Text(shift.branchName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Mulai Shift:", fontSize = 11.sp, color = Color.Gray)
                                            val startStr = java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale("id", "ID")).format(java.util.Date(shift.startTime))
                                            Text(startStr, fontSize = 11.sp)
                                        }

                                        if (shift.endTime != null) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Selesai Shift:", fontSize = 11.sp, color = Color.Gray)
                                                val endStr = java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale("id", "ID")).format(java.util.Date(shift.endTime))
                                                Text(endStr, fontSize = 11.sp)
                                            }
                                        }

                                        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Uang Modal Awal:", fontSize = 11.sp, color = Color.Gray)
                                            Text(idrFormatter.format(shift.modalAwal), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        }

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Omset Tunai (Cash):", fontSize = 11.sp, color = Color.Gray)
                                            Text(idrFormatter.format(shift.totalTunai), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        }

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Omset Non-Tunai:", fontSize = 11.sp, color = Color.Gray)
                                            Text(idrFormatter.format(shift.totalNonTunai), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        }

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Teoritis Seharusnya (Sistem):", fontSize = 11.sp, color = Color.Gray)
                                            Text(idrFormatter.format(shift.modalAwal + shift.totalTunai), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }

                                        if (shift.status != "aktif") {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Uang Fisik Dilaporkan:", fontSize = 11.sp, color = Color.Gray)
                                                Text(idrFormatter.format(shift.actualDrawerCash), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }

                                            val selisihVal = shift.selisih
                                            val selisihColor = if (selisihVal == 0.0) Color.DarkGray else if (selisihVal > 0.0) Color(0xFF16A34A) else Color(0xFFDC2626)
                                            val labelText = if (selisihVal == 0.0) {
                                                "Sesuai (Rp 0)"
                                            } else if (selisihVal > 0.0) {
                                                "Surplus (+${idrFormatter.format(selisihVal)})"
                                            } else {
                                                "Defisit / Minus (${idrFormatter.format(selisihVal)})"
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Selisih Kas Laci Fisik:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Text(labelText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = selisihColor)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (filteredShifts.size > 3) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            TextButton(
                                onClick = { isShiftsExpanded = !isShiftsExpanded },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = if (isShiftsExpanded) "Sembunyikan Sesi Shift Kasir" else "Lihat Semua Sesi Shift Kasir (${filteredShifts.size})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OrangePrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (isShiftsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = OrangePrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.triggerUpgradePopup("Paket Profesional") }
                        .testTag("shifts_locked_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Riwayat Laporan Shift Kasir (Terkunci)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Fitur riwayat laporan shift kasir hanya tersedia untuk minimal Paket Profesional. Klik untuk upgrade sekarang!",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Grafik real pendapatan (7 Hari terakhir)
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Grafik Pendapatan Harian (7 Hari Terakhir)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = OrangePrimary)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val maxIncome = dailyIncomeList.maxOfOrNull { it.second } ?: 1.0
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        dailyIncomeList.forEach { (dateStr, income) ->
                            val barHeightPercent = if (maxIncome > 0) (income / maxIncome).toFloat().coerceIn(0.05f, 1f) else 0.05f
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (income > 0) {
                                        if (income >= 1_000_000) String.format("%.1fM", income / 1_000_000)
                                        else if (income >= 1_000) String.format("%.0fK", income / 1_000)
                                        else String.format("%.0f", income)
                                    } else "",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OrangePrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .fillMaxHeight(barHeightPercent)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(OrangePrimary)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = dateStr, fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Grafik Penjualan Terlaris Real
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Grafik Produk Terlaris (Qty)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Icon(imageVector = Icons.Default.Leaderboard, contentDescription = null, tint = OrangePrimary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (topProducts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text("Belum ada data produk terlaris di interval ini.", fontSize = 11.sp, color = Color.Gray)
                        }
                    } else {
                        val maxQty = topProducts.maxOfOrNull { it.second } ?: 1
                        topProducts.forEach { (pName, qty) ->
                            val percent = if (maxQty > 0) qty.toFloat() / maxQty else 0f
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(pName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Text("$qty Unit", fontSize = 11.sp, color = OrangePrimary, fontWeight = FontWeight.Bold)
                                }
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
                                            .fillMaxWidth(percent.coerceIn(0.01f, 1f))
                                            .fillMaxHeight()
                                            .background(OrangePrimary)
                                    )
                                }
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
                    val pdfFile = generateLaporanKeuanganPdf(
                        context = context,
                        interval = reportInterval,
                        storeName = user?.nama ?: "Toko Utama",
                        omset = finalIncome,
                        hpp = calculatedCapital,
                        pengeluaran = totalExpensesValue,
                        labaBersih = finalFinancialNet,
                        transactionsCount = countTx,
                        topProducts = topProducts,
                        idrFormatter = idrFormatter
                    )
                    
                    if (pdfFile != null && pdfFile.exists()) {
                        try {
                            val uri = FileProvider.getUriForFile(
                                context,
                                "com.kasirpro.app.fileprovider",
                                pdfFile
                            )
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Export PDF Laporan Keuangan"))
                            Toast.makeText(context, "Dokumen PDF Laporan berhasil dibuat!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(context, "Error sharing PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, "Gagal memproses dokumen PDF!", Toast.LENGTH_SHORT).show()
                    }
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

    if (showAddExpenseDialog) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        AlertDialog(
            onDismissRequest = { showAddExpenseDialog = false },
            title = { Text("Catat Pengeluaran Baru", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Catat pengeluaran finansial / biaya beban operasional toko Anda di bawah ini.", fontSize = 11.sp, color = Color.Gray)
                    
                    OutlinedTextField(
                        value = editExpenseNominal,
                        onValueChange = { editExpenseNominal = it },
                        label = { Text("Nominal Pengeluaran (Rp) *") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("expense_nominal_input")
                    )

                    OutlinedTextField(
                        value = editExpenseKet,
                        onValueChange = { editExpenseKet = it },
                        label = { Text("Keterangan Operasional *") },
                        placeholder = { Text("Contoh: Bayar Listrik, Gaji Karyawan, Sewa Tempat, dll.") },
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth().testTag("expense_description_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = editExpenseNominal.toDoubleOrNull() ?: 0.0
                        if (amt <= 0.0) {
                            Toast.makeText(context, "Jumlah nominal pengeluaran harus valid!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (editExpenseKet.isBlank()) {
                            Toast.makeText(context, "Keterangan pengeluaran tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        viewModel.recordExpense(amt, editExpenseKet) { success, error ->
                            if (success) {
                                showAddExpenseDialog = false
                                editExpenseNominal = ""
                                editExpenseKet = ""
                                Toast.makeText(context, "Pengeluaran berhasil dicatat!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Gagal mencatatkan pengeluaran: $error", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    modifier = Modifier.testTag("submit_expense_btn")
                ) {
                    Text("Simpan Pengeluaran")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExpenseDialog = false }) {
                    Text("Batal")
                }
            }
        )
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
    var selectedCustForDetail by remember { mutableStateOf<CustomerEntity?>(null) }
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    var showConfigLoyalty by remember { mutableStateOf(false) }
    var earnRateInput by remember { mutableStateOf(viewModel.getPointEarnRate().toInt().toString()) }
    var redeemRateInput by remember { mutableStateOf(viewModel.getPointRedeemRate().toInt().toString()) }

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

        // Aturan poin loyalty configuration panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showConfigLoyalty = !showConfigLoyalty },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Aturan Loyalty & Poin CRM", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Icon(
                            imageVector = if (showConfigLoyalty) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (showConfigLoyalty) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = earnRateInput,
                                onValueChange = { earnRateInput = it },
                                label = { Text("Belanja per Poin (Rp)", fontSize = 10.sp) },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                            )
                            OutlinedTextField(
                                value = redeemRateInput,
                                onValueChange = { redeemRateInput = it },
                                label = { Text("Nilai 1 Poin (Rp)", fontSize = 10.sp) },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                val eRate = earnRateInput.toDoubleOrNull() ?: 10000.0
                                val rRate = redeemRateInput.toDoubleOrNull() ?: 100.0
                                viewModel.setPointEarnRate(eRate)
                                viewModel.setPointRedeemRate(rRate)
                                Toast.makeText(context, "Aturan CRM berhasil disimpan!", Toast.LENGTH_SHORT).show()
                                showConfigLoyalty = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Simpan Aturan", fontSize = 12.sp)
                        }
                    }
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
                    modifier = Modifier.fillMaxWidth().clickable { selectedCustForDetail = c },
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(c.nama, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("No HP: ${c.nomorHp}", fontSize = 11.sp, color = Color.Gray)
                            Text("Total Transaksi: ${c.totalTransaksi} kali", fontSize = 10.sp, color = Color.Gray)
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

    // CUSTOMER DETAIL & CRM SERVICES DIALOG
    if (selectedCustForDetail != null) {
        val cust = selectedCustForDetail!!
        var adjustPoinStr by remember { mutableStateOf("") }
        var activeSubTab by remember { mutableStateOf(0) } // 0: Info, 1: Redeem Rewards

        AlertDialog(
            onDismissRequest = { selectedCustForDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CardMembership, contentDescription = null, tint = OrangePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(cust.nama, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Poin", fontSize = 10.sp, color = Color.Gray)
                            Text("${cust.totalPoin}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = OrangeDark)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Sesi Belanja", fontSize = 10.sp, color = Color.Gray)
                            Text("${cust.totalTransaksi}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("WhastApp", fontSize = 10.sp, color = Color.Gray)
                            Text(if (cust.nomorHp.length > 5) cust.nomorHp.take(5) + "..." else cust.nomorHp, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                        }
                    }

                    TabRow(
                        selectedTabIndex = activeSubTab,
                        containerColor = Color.Transparent,
                        contentColor = OrangePrimary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[activeSubTab]),
                                color = OrangePrimary
                            )
                        }
                    ) {
                        Tab(selected = activeSubTab == 0, onClick = { activeSubTab = 0 }) {
                            Text("Penyesuaian", modifier = Modifier.padding(8.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Tab(selected = activeSubTab == 1, onClick = { activeSubTab = 1 }) {
                            Text("Tukar Hadiah", modifier = Modifier.padding(8.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (activeSubTab == 0) {
                        // Point adjustment & Share CRM
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Koreksi / Bonus Poin Manual", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                            OutlinedTextField(
                                value = adjustPoinStr,
                                onValueChange = { adjustPoinStr = it },
                                placeholder = { Text("Jumlah poin, misal: 10 atau -5") },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = {
                                        val pts = adjustPoinStr.toIntOrNull() ?: 0
                                        if (pts != 0) {
                                            val updated = cust.copy(totalPoin = (cust.totalPoin + pts).coerceAtLeast(0))
                                            viewModel.updateCustomer(updated)
                                            selectedCustForDetail = updated
                                            adjustPoinStr = ""
                                            Toast.makeText(context, "Poin berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Sesuaikan", fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(4.dp))

                            Text("Kirim Broadcast Point CRM", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                            Button(
                                onClick = {
                                    val earnR = viewModel.getPointEarnRate().toInt()
                                    val msg = "Halo ${cust.nama}, terima kasih telah berbelanja pelanggan setia di Toko kami. Poin Loyalty Anda saat ini *${cust.totalPoin} Poin* (Setiap Rp$earnR = 1 Poin). Tukarkan poin Anda dengan hadiah menarik di kasir kami!"
                                    try {
                                        val uri = Uri.parse("https://api.whatsapp.com/send?phone=${cust.nomorHp}&text=" + java.net.URLEncoder.encode(msg, "UTF-8"))
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(msg))
                                        Toast.makeText(context, "Link WA gagal dibuka. Teks pesan telah disalin ke Clipboard!", Toast.LENGTH_LONG).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // WhatsApp Green
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share Promo & Poin WA", fontSize = 12.sp)
                            }
                        }
                    } else {
                        // Gift Redemption Catalog
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Pilih Hadiah Poin Pelanggan:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                            
                            val rewards = listOf(
                                Pair("Voucher Diskon Rp5.000", 50),
                                Pair("Gantungan Kunci Cantik", 100),
                                Pair("Buku Saku KasirPro", 150),
                                Pair("Kaos Polo Eksklusif", 350)
                            )

                            rewards.forEach { (gift, cost) ->
                                val listColor = if (cust.totalPoin >= cost) OrangePrimary else Color.Gray

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, listColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(gift, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Butuh $cost Poin", fontSize = 11.sp, color = listColor)
                                    }
                                    Button(
                                        onClick = {
                                            if (cust.totalPoin >= cost) {
                                                val updated = cust.copy(totalPoin = cust.totalPoin - cost)
                                                viewModel.updateCustomer(updated)
                                                selectedCustForDetail = updated
                                                Toast.makeText(context, "Hadiah '$gift' berhasil ditukarkan! $cost Poin dikurangi.", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "Poin tidak cukup!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        enabled = (cust.totalPoin >= cost),
                                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Tukar", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedCustForDetail = null }) {
                    Text("Tutup", color = OrangePrimary)
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

        if (branchesList.size <= 1 && branchesList.none { !it.id.startsWith("branch-1") }) { // includes default main branch only
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
                                        imageVector = if (br.id.startsWith("branch-1")) Icons.Default.Storefront else Icons.Default.AddBusiness,
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
                                if (!br.id.startsWith("branch-1")) {
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
    
    var showAddCashierDialog by remember { mutableStateOf(false) }
    var cashierName by remember { mutableStateOf("") }
    var cashierUsername by remember { mutableStateOf("") }
    var cashierPassword by remember { mutableStateOf("") }
    var selectedBranchId by remember { mutableStateOf("") }
    
    // Live Availability Check for Add Cashier
    var isAddUsernameAvailable by remember { mutableStateOf<Boolean?>(null) }
    var isCheckingAddUsername by remember { mutableStateOf(false) }

    LaunchedEffect(cashierUsername) {
        val clean = cashierUsername.trim().lowercase()
        if (clean.isEmpty()) {
            isAddUsernameAvailable = null
            return@LaunchedEffect
        }
        isCheckingAddUsername = true
        kotlinx.coroutines.delay(500)
        viewModel.checkUsernameAvailability(clean) { available ->
            isAddUsernameAvailable = available
            isCheckingAddUsername = false
        }
    }

    // Edit Cashier states
    var editCashierTarget by remember { mutableStateOf<UserEntity?>(null) }
    var editCashierName by remember { mutableStateOf("") }
    var editCashierUsername by remember { mutableStateOf("") }
    var editCashierOldUsername by remember { mutableStateOf("") }
    var editCashierPassword by remember { mutableStateOf("") }
    var isEditUsernameAvailable by remember { mutableStateOf<Boolean?>(null) }
    var isCheckingEditUsername by remember { mutableStateOf(false) }

    LaunchedEffect(editCashierUsername) {
        val clean = editCashierUsername.trim().lowercase()
        if (clean.isEmpty() || clean == editCashierOldUsername.trim().lowercase()) {
            isEditUsernameAvailable = true
            return@LaunchedEffect
        }
        isCheckingEditUsername = true
        kotlinx.coroutines.delay(500)
        viewModel.checkUsernameAvailability(clean, editCashierOldUsername) { available ->
            isEditUsernameAvailable = available
            isCheckingEditUsername = false
        }
    }
    
    var assignBranchTarget by remember { mutableStateOf<UserEntity?>(null) }
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
                    onClick = { showAddCashierDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambah Kasir", fontSize = 12.sp)
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
                            Text("Tambah akun kasir pertama Anda melalui tombol di atas untuk mendelegasikan transaksi kasir.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
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
                                        .background(OrangeLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Badge,
                                        contentDescription = null,
                                        tint = OrangePrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(cs.nama, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Username: ${cs.email}", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFDCFCE7))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("AKTIF", fontSize = 9.sp, color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Dibuat: " + formatRelativeTime(cs.createdAt),
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
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { assignBranchTarget = cs },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary.copy(alpha = 0.15f)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Atur Cabang", fontSize = 10.sp, color = OrangePrimary, fontWeight = FontWeight.Bold)
                                }

                                IconButton(
                                    onClick = {
                                        editCashierTarget = cs
                                        editCashierName = cs.nama
                                        editCashierUsername = cs.email
                                        editCashierOldUsername = cs.email
                                        editCashierPassword = ""
                                        viewModel.getCashierPassword(cs.uid) { pass ->
                                            editCashierPassword = pass
                                        }
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Kasir", tint = OrangePrimary, modifier = Modifier.size(18.dp))
                                }
                                
                                IconButton(
                                    onClick = { 
                                        viewModel.deleteCashier(cs.uid)
                                        Toast.makeText(context, "Akun kasir berhasil dihapus", Toast.LENGTH_SHORT).show()
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

    if (showAddCashierDialog) {
        if (selectedBranchId.isEmpty() && branchesList.isNotEmpty()) {
            selectedBranchId = branchesList.first().id
        }

        AlertDialog(
            onDismissRequest = { showAddCashierDialog = false },
            title = { Text("Tambah Akun Kasir", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Buat langsung kredensial masuk kasir tanpa undangan email.", fontSize = 12.sp, color = Color.Gray)
                    
                    OutlinedTextField(
                        value = cashierName,
                        onValueChange = { cashierName = it },
                        label = { Text("Nama Kasir") },
                        placeholder = { Text("contoh: Budi Santoso") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("add_cashier_name")
                    )

                    OutlinedTextField(
                        value = cashierUsername,
                        onValueChange = { cashierUsername = it },
                        label = { Text("Username / ID Login (Unik)") },
                        placeholder = { Text("contoh: budi_kasir") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("add_cashier_username")
                    )

                    // Helper label for Add Cashier Username live check
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isCheckingAddUsername) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.dp, color = OrangePrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Memeriksa ketersediaan...", fontSize = 11.sp, color = Color.Gray)
                        } else if (isAddUsernameAvailable != null) {
                            if (isAddUsernameAvailable == true) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Username tersedia & siap digunakan!", fontSize = 11.sp, color = Color(0xFF15803D))
                            } else {
                                Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = Color.Red, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Username sudah digunakan pengguna lain!", fontSize = 11.sp, color = Color.Red)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = cashierPassword,
                        onValueChange = { cashierPassword = it },
                        label = { Text("Password Kasir") },
                        placeholder = { Text("Minimal 6 karakter") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("add_cashier_password")
                    )

                    Text("Tugaskan ke Cabang:", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        branchesList.forEach { br ->
                            val isSelected = selectedBranchId == br.id
                            Card(
                                onClick = { selectedBranchId = br.id },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) OrangeLight else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(br.namaCabang, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                        if (isSelected) {
                                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(16.dp))
                                        }
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
                        if (cashierName.isBlank() || cashierUsername.isBlank() || cashierPassword.isBlank() || selectedBranchId.isBlank()) {
                            Toast.makeText(context, "Semua kolom formulir harus diisi!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (cashierPassword.length < 6) {
                            Toast.makeText(context, "Password minimal 6 karakter!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (isAddUsernameAvailable == false) {
                            Toast.makeText(context, "Username tidak tersedia / sudah digunakan!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.addCashier(
                            nama = cashierName,
                            username = cashierUsername,
                            pass = cashierPassword,
                            branchId = selectedBranchId
                        ) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (success) {
                                cashierName = ""
                                cashierUsername = ""
                                cashierPassword = ""
                                selectedBranchId = ""
                                showAddCashierDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_add_cashier_button")
                ) {
                    Text("Buat Akun")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCashierDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (editCashierTarget != null) {
        AlertDialog(
            onDismissRequest = { editCashierTarget = null },
            title = { Text("Edit Akun Kasir", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Perbarui nama, login ID (username), dan password untuk kasir Anda.", fontSize = 12.sp, color = Color.Gray)
                    
                    OutlinedTextField(
                        value = editCashierName,
                        onValueChange = { editCashierName = it },
                        label = { Text("Nama Kasir") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editCashierUsername,
                        onValueChange = { editCashierUsername = it },
                        label = { Text("Username / ID Login (Unik)") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isCheckingEditUsername) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.dp, color = OrangePrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Memeriksa ketersediaan...", fontSize = 11.sp, color = Color.Gray)
                        } else if (isEditUsernameAvailable != null) {
                            if (isEditUsernameAvailable == true) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Username tersedia!", fontSize = 11.sp, color = Color(0xFF15803D))
                            } else {
                                Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = Color.Red, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Username sudah digunakan pengguna lain!", fontSize = 11.sp, color = Color.Red)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editCashierPassword,
                        onValueChange = { editCashierPassword = it },
                        label = { Text("Password Baru / Ubah") },
                        placeholder = { Text("Minimal 6 karakter") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editCashierName.isBlank() || editCashierUsername.isBlank() || editCashierPassword.isBlank()) {
                            Toast.makeText(context, "Semua kolom harus diisi!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (editCashierPassword.length < 6) {
                            Toast.makeText(context, "Password minimal 6 karakter!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (isEditUsernameAvailable == false) {
                            Toast.makeText(context, "Username tidak dapat digunakan karena sudah dimiliki akun lain!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        viewModel.editCashier(
                            cashierId = editCashierTarget!!.uid,
                            oldUsername = editCashierOldUsername,
                            newNama = editCashierName,
                            newUsername = editCashierUsername,
                            newPass = editCashierPassword
                        ) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (success) {
                                editCashierTarget = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Simpan Akun")
                }
            },
            dismissButton = {
                TextButton(onClick = { editCashierTarget = null }) {
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

fun generateLaporanKeuanganPdf(
    context: android.content.Context,
    interval: String,
    storeName: String,
    omset: Double,
    hpp: Double,
    pengeluaran: Double,
    labaBersih: Double,
    transactionsCount: Int,
    topProducts: List<Pair<String, Int>>,
    idrFormatter: java.text.NumberFormat
): File? {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        
        val paint = Paint()
        var yPosition = 40f
        
        // Header Banner Background Accent
        val headerPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#F97316")
        }
        canvas.drawRect(0f, 0f, 595f, 110f, headerPaint)
        
        // Header Text
        val titlePaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 20f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }
        canvas.drawText("KASIR PRO - LAPORAN KEUANGAN", 30f, 45f, titlePaint)
        
        val subtitlePaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 11f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        canvas.drawText("Laporan Finansial Profesional Selang Waktu: $interval", 30f, 70f, subtitlePaint)
        canvas.drawText("Toko / Mitra Warung: $storeName", 30f, 88f, subtitlePaint)
        
        yPosition = 150f
        
        // Section: Ringkasan Finansial
        val sectionTitlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1E293B")
            textSize = 13f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }
        
        canvas.drawText("I. RINGKASAN FINANSIAL OUTLET", 30f, yPosition, sectionTitlePaint)
        
        val linePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#CBD5E1")
            strokeWidth = 1f
        }
        yPosition += 8f
        canvas.drawLine(30f, yPosition, 565f, yPosition, linePaint)
        
        yPosition += 25f
        
        val gridLabelPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#475569")
            textSize = 11f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        
        val gridValuePaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 11f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }
        
        val financials = listOf(
            "Total Omset Penjualan" to idrFormatter.format(omset),
            "Total Harga Pokok Penjualan (HPP)" to idrFormatter.format(hpp),
            "Total Beban / Pengeluaran Toko" to idrFormatter.format(pengeluaran),
            "Jumlah Transaksi Sukses" to "$transactionsCount Transaksi"
        )
        
        financials.forEach { (label, value) ->
            canvas.drawText(label, 40f, yPosition, gridLabelPaint)
            val textWidth = gridValuePaint.measureText(value)
            canvas.drawText(value, 555f - textWidth, yPosition, gridValuePaint)
            yPosition += 22f
        }
        
        yPosition += 10f
        
        // Highlight Card for net profit
        val profitCardPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#DCFCE7")
        }
        canvas.drawRoundRect(30f, yPosition, 565f, yPosition + 45f, 6f, 6f, profitCardPaint)
        
        val netProfitLabelPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#166534")
            textSize = 12f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }
        canvas.drawText("ESTIMASI LABA BERSIH", 45f, yPosition + 27f, netProfitLabelPaint)
        
        val netProfitVal = idrFormatter.format(labaBersih)
        val netProfitValuePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#166534")
            textSize = 14f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }
        val pWidth = netProfitValuePaint.measureText(netProfitVal)
        canvas.drawText(netProfitVal, 550f - pWidth, yPosition + 28f, netProfitValuePaint)
        
        yPosition += 80f
        
        // Section: Produk Terlaris
        canvas.drawText("II. DAFTAR PERINGKAT PRODUK TERLARIS (QTY)", 30f, yPosition, sectionTitlePaint)
        yPosition += 8f
        canvas.drawLine(30f, yPosition, 565f, yPosition, linePaint)
        yPosition += 25f
        
        if (topProducts.isEmpty()) {
            canvas.drawText("Belum ada data barang / produk terjual di selang waktu laporan keuangan ini.", 40f, yPosition, gridLabelPaint)
            yPosition += 20f
        } else {
            val thPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#64748B")
                textSize = 9f
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            }
            canvas.drawText("NAMA BARANG / PRODUK", 40f, yPosition, thPaint)
            canvas.drawText("KUANTITAS TERJUAL", 440f, yPosition, thPaint)
            yPosition += 12f
            canvas.drawLine(30f, yPosition, 565f, yPosition, linePaint)
            yPosition += 20f
            
            topProducts.forEach { (pName, qty) ->
                if (yPosition < 770f) {
                    canvas.drawText(pName, 40f, yPosition, gridLabelPaint)
                    val qVal = "$qty Unit"
                    val qWidth = gridValuePaint.measureText(qVal)
                    canvas.drawText(qVal, 555f - qWidth, yPosition, gridValuePaint)
                    yPosition += 20f
                }
            }
        }
        
        // Footer Notes & Printing info
        val footerLabelPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#94A3B8")
            textSize = 8f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        canvas.drawText("Dokumen ini digenerasi murni oleh Kasir Pro Premium Laporan Keuangan.", 30f, 795f, footerLabelPaint)
        
        val dateFormatted = java.text.SimpleDateFormat("dd MMMM yyyy HH:mm:ss", java.util.Locale("id", "ID")).format(java.util.Date())
        canvas.drawText("Waktu Cetak Dokumen: $dateFormatted WITA/WIB", 30f, 810f, footerLabelPaint)
        
        pdfDocument.finishPage(page)
        
        val cacheFile = File(context.cacheDir, "Laporan_Keuangan_KasirPro.pdf")
        if (cacheFile.exists()) {
            cacheFile.delete()
        }
        val fileOutputStream = FileOutputStream(cacheFile)
        pdfDocument.writeTo(fileOutputStream)
        pdfDocument.close()
        fileOutputStream.close()
        
        return cacheFile
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}
