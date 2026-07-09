package com.kasirpro.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kasirpro.app.ui.viewmodel.KasirViewModel
import com.kasirpro.app.data.local.*
import com.kasirpro.app.ui.theme.*
import java.text.NumberFormat
import java.util.Locale
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import com.kasirpro.app.data.local.TransactionEntity
import com.kasirpro.app.util.ShopLogoImage
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: KasirViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val business by viewModel.currentBusiness.collectAsState()
    val transactionsList by viewModel.transactions.collectAsState()
    val lowStockList by viewModel.lowStockProducts.collectAsState()
    val debtsList by viewModel.debts.collectAsState()
    val branchesList by viewModel.branches.collectAsState()
    val isOnlineState by viewModel.isOnline.collectAsState()

    var showBranchDropdown by remember { mutableStateOf(false) }
    var selectedBranchName by remember { mutableStateOf("Semua Cabang") }

    var selectedTxForReceipt by remember { mutableStateOf<TransactionEntity?>(null) }
    var showCorrectionAuthDialog by remember { mutableStateOf(false) }
    var showCorrectionEditDialog by remember { mutableStateOf(false) }
    var authCodeInput by remember { mutableStateOf("") }

    // Analytics calculations (Today's metrics)
    val today = System.currentTimeMillis()
    val startOfDay = today - (today % (24 * 60 * 60 * 1000))
    val todayTransactions = transactionsList.filter { it.createdAt >= startOfDay }

    val totalIncome = todayTransactions.sumOf { it.actualIncome }
    val totalProfit = todayTransactions.sumOf { tx ->
        // Profit calculation helper
        tx.actualIncome * 0.45 // Estimated margin simulator based on user parameters
    }
    val trxCount = todayTransactions.size
    val activeDebts = debtsList.filter { it.status == "belum" }.sumOf { it.jumlah }

    // Last 7 days real revenue aggregation for the interactive financial graph
    val last7DaysData = remember(transactionsList) {
        val daysList = (0..6).map { i ->
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -i)
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            cal
        }.reversed()

        daysList.map { cal ->
            val dayStart = cal.timeInMillis
            val dayEnd = dayStart + (24 * 60 * 60 * 1000)
            val dayRevenue = transactionsList.filter { t -> t.createdAt in dayStart until dayEnd }.sumOf { it.actualIncome }
            
            val sdf = java.text.SimpleDateFormat("EEE", java.util.Locale("id", "ID"))
            val label = sdf.format(cal.time)
            Pair(label, dayRevenue)
        }
    }

    // Number format helper for IDR currency formatting
    val idrFormatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    idrFormatter.maximumFractionDigits = 0

    val posInteractionSource = remember { MutableInteractionSource() }
    val isPosPressed by posInteractionSource.collectIsPressedAsState()
    val posScale by animateFloatAsState(
        targetValue = if (isPosPressed) 0.9f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "pos_action_scale"
    )

    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = business?.namaBisnis ?: "Toko Kasir Pro",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnlineState) Color.Green else Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOnlineState) "Koneksi Online" else "Mode Offline",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                actions = {
                    // Notification Bell Icon with Badge
                    IconButton(
                        onClick = { viewModel.activeScreen.value = "user_notifications" },
                        modifier = Modifier.testTag("notification_bell_button")
                    ) {
                        Box {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = OrangePrimary
                            )
                            if (unreadCount > 0) {
                                val countText = if (unreadCount > 99) "99+" else "$unreadCount"
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 6.dp, y = (-6).dp)
                                        .background(Color.Red, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 4.dp)
                                        .sizeIn(minWidth = 16.dp, minHeight = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = countText,
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        style = androidx.compose.ui.text.TextStyle(
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            lineHeight = 1.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Simulated active online sync backup trigger
                    IconButton(onClick = { viewModel.toggleOnlineMode() }) {
                        Icon(
                            imageVector = if (isOnlineState) Icons.Default.CloudQueue else Icons.Default.CloudOff,
                            contentDescription = "Sync",
                            tint = if (isOnlineState) OrangePrimary else Color.Gray
                        )
                    }

                    // Active Firestore Sync trigger button
                    IconButton(
                        onClick = { viewModel.forceSyncFromFirestore() },
                        modifier = Modifier.testTag("force_sync_button")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Sinkronkan dari Firebase", tint = OrangePrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.activeScreen.value = "cashier" },
                shape = CircleShape,
                containerColor = OrangePrimary,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 4.dp
                ),
                interactionSource = posInteractionSource,
                modifier = Modifier
                    .graphicsLayer(scaleX = posScale, scaleY = posScale)
                    .testTag("floating_pos_btn")
            ) {
                Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Buka Kasir")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sapaan Owner / Kasir info & Branch selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Halo, ${user?.nama ?: "Pengusaha Kasir"}!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Selamat bekerja (${user?.role ?: "owner"})",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }

                    // Branch filter selector for owners
                    if (user?.role == "owner") {
                        Box {
                            Button(
                                onClick = { showBranchDropdown = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(selectedBranchName, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(
                                expanded = showBranchDropdown,
                                onDismissRequest = { showBranchDropdown = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Semua Cabang") },
                                    onClick = {
                                        selectedBranchName = "Semua Cabang"
                                        showBranchDropdown = false
                                    }
                                )
                                branchesList.forEach { branch ->
                                    DropdownMenuItem(
                                        text = { Text(branch.namaCabang) },
                                        onClick = {
                                            selectedBranchName = branch.namaCabang
                                            showBranchDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Grid cards for sales analytics
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Card Pendapatan Hari Ini
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = OrangeLight)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = OrangePrimary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Pendapatan Hari Ini", fontSize = 11.sp, color = OrangeDark, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = idrFormatter.format(totalIncome),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OrangeDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Card Keuntungan Hari Ini
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFF15803D))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Margin Profit (Est)", fontSize = 11.sp, color = Color(0xFF15803D), fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = idrFormatter.format(totalProfit),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Card Jumlah Transaksi
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = OrangePrimary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Transaksi Hari Ini", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.SemiBold)
                                Text("$trxCount Transaksi", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Card Hutang Aktif (Premium)
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Icon(Icons.Default.PendingActions, contentDescription = null, tint = Color(0xFFE11D48))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Hutang Aktif", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = idrFormatter.format(activeDebts),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE11D48),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            item {
                InteractiveFinancialChart(data = last7DaysData, currencyFormatter = idrFormatter)
            }

            // Low Stock list tracking
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Peringatan Stok Tipis", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = "Lihat Semua",
                        color = OrangePrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            viewModel.activeScreen.value = "manage"
                        }
                    )
                }
            }

            if (lowStockList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF22C55E))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Hebat! Persediaan stok semua produk aman.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
                        }
                    }
                }
            } else {
                items(lowStockList.take(3)) { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(product.nama, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Stok: ${product.stok} (Batas min: ${product.stokMinimum})", fontSize = 11.sp, color = Color.Red)
                            }
                            Button(
                                onClick = { viewModel.activeScreen.value = "manage" },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                            ) {
                                Text("Resupply", fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Recent transactions tracking list
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Transaksi Terbaru", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = "Lihat Laporan",
                        color = OrangePrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            viewModel.activeScreen.value = "manage"
                        }
                    )
                }
            }

            if (transactionsList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada transaksi terekam saat ini.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                items(transactionsList.take(5)) { tx ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTxForReceipt = tx
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(OrangeLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(tx.id, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(tx.metodeBayar, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(idrFormatter.format(tx.total), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OrangePrimary)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (tx.status == "lunas") Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = tx.status.uppercase(),
                                        fontSize = 9.sp,
                                        color = if (tx.status == "lunas") Color(0xFF15803D) else Color(0xFFB91C1C),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Viewing Receipt & Correction Trigger Dialogue
    if (selectedTxForReceipt != null) {
        val rx = selectedTxForReceipt!!
        val isPremium = user?.isPremium ?: false
        val scope = rememberCoroutineScope()
        val context = LocalContext.current

        AlertDialog(
            onDismissRequest = { selectedTxForReceipt = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = OrangePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Detail Struk Transaksi", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Dynamic Logo
                    if (!business?.logoBase64.isNullOrBlank()) {
                        ShopLogoImage(
                            logoBase64 = business?.logoBase64,
                            contentDescription = business?.namaBisnis,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .padding(bottom = 6.dp)
                        )
                    }
                    Text(business?.namaBisnis ?: "KASIR PRO SHOP", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp, textAlign = TextAlign.Center)
                    if (!business?.alamat.isNullOrBlank()) {
                        Text(business!!.alamat!!, fontSize = 11.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
                    } else {
                        Text("Cabang Utama", fontSize = 11.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
                    }
                    if (!business?.noTelpon.isNullOrBlank()) {
                        Text("Tel: ${business!!.noTelpon!!}", fontSize = 11.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
                    }
                    Text("---------------------------------", color = Color.Black)
                    Text("No TRX: ${rx.id}", fontSize = 11.sp, color = Color.Black)
                    Text("Tanggal: ${java.text.SimpleDateFormat("dd-MM-yyyy HH:mm", java.util.Locale("id", "ID")).format(java.util.Date(rx.createdAt))}", fontSize = 11.sp, color = Color.Black)
                    Text("Kasir: ${rx.kasirNama}", fontSize = 11.sp, color = Color.Black)
                    Text("---------------------------------", color = Color.Black)

                    // Serialized items
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
                                Text("$name x$qty $sat", fontSize = 11.sp, color = Color.Black)
                                Text(idrFormatter.format(itemSub), fontSize = 11.sp, color = Color.Black)
                            }
                        }
                    }

                    Text("---------------------------------", color = Color.Black)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", fontSize = 11.sp, color = Color.Black)
                        Text(idrFormatter.format(rx.subtotal), fontSize = 11.sp, color = Color.Black)
                    }
                    if (rx.diskonTotal > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Diskon Promo", fontSize = 11.sp, color = Color.Black)
                            Text("-${idrFormatter.format(rx.diskonTotal)}", fontSize = 11.sp, color = Color.Black)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TOTAL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(idrFormatter.format(rx.total), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("DIBAYAR", fontSize = 11.sp, color = Color.Black)
                        Text(idrFormatter.format(rx.bayarNominal), fontSize = 11.sp, color = Color.Black)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("KEMBALI", fontSize = 11.sp, color = Color.Black)
                        Text(idrFormatter.format(rx.kembalian), fontSize = 11.sp, color = Color.Black)
                    }
                    Text("---------------------------------", color = Color.Black)
                }
            },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Menghubungkan ke printer RPP02N...", Toast.LENGTH_SHORT).show()
                            Toast.makeText(context, "Mencetak ulang struk No TRX: ${rx.id} - Selesai!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cetak Ulang Struk")
                    }

                    Button(
                        onClick = {
                            if (isPremium) {
                                showCorrectionAuthDialog = true
                            } else {
                                viewModel.showLimitPopup.value = "Fitur Koreksi Transaksi hanya tersedia untuk pengguna Premium Pro. Upgrade sekarang!"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Koreksi Transaksi")
                    }

                    OutlinedButton(
                        onClick = { selectedTxForReceipt = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tutup")
                    }
                }
            }
        )
    }

    if (showCorrectionAuthDialog) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { showCorrectionAuthDialog = false },
            title = { Text("Otoritas Pemilik Diperlukan", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Masukkan Kode Unik Otoritas dari Pemilik Toko untuk mengizinkan koreksi/edit pada transaksi ini.", fontSize = 12.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = authCodeInput,
                        onValueChange = { authCodeInput = it },
                        label = { Text("Kode Otoritas Pemilik") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val realCode = viewModel.getOwnerVerificationCode()
                        if (authCodeInput == realCode) {
                            showCorrectionAuthDialog = false
                            showCorrectionEditDialog = true
                            authCodeInput = "" // clear
                        } else {
                            Toast.makeText(context, "Kode Otoritas salah! Akses Ditolak.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Verifikasi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCorrectionAuthDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showCorrectionEditDialog && selectedTxForReceipt != null) {
        val rx = selectedTxForReceipt!!
        val context = LocalContext.current
        val allProducts by viewModel.products.collectAsState()

        // Parse items
        val parsedItems = remember(rx.id) {
            rx.itemsRaw.split(";").filter { it.isNotBlank() }.mapNotNull { line ->
                val parts = line.split(":")
                if (parts.size >= 4) {
                    val id = parts[0]
                    val nama = parts[1]
                    val jumlah = parts[2].toIntOrNull() ?: 1
                    val harga = parts[3].toDoubleOrNull() ?: 0.0
                    val varianSelected = parts.getOrNull(4) ?: ""
                    val diskon = parts.getOrNull(5)?.toDoubleOrNull() ?: 0.0
                    val satuan = parts.getOrNull(6).orEmpty().takeIf { it.isNotBlank() } ?: "Pcs"
                    CorrectionItem(id, nama, jumlah, harga, varianSelected, diskon, satuan)
                } else null
            }.toMutableStateList()
        }

        // Auto calculated totals from items
        val calculatedSubtotal = parsedItems.sumOf { it.harga * it.jumlah }
        val calculatedDiskon = parsedItems.sumOf { it.diskon * it.jumlah }
        val calculatedTotal = (calculatedSubtotal - calculatedDiskon).coerceAtLeast(0.0)

        var editTotalStr by remember { mutableStateOf(calculatedTotal.toInt().toString()) }
        var editBayarStr by remember { mutableStateOf(rx.bayarNominal.toInt().toString()) }
        var editDiskonStr by remember { mutableStateOf(calculatedDiskon.toInt().toString()) }
        var editMetodeBayar by remember { mutableStateOf(rx.metodeBayar) }
        var editStatus by remember { mutableStateOf(rx.status) }

        // Sync when products change
        LaunchedEffect(parsedItems.toList()) {
            editTotalStr = calculatedTotal.toInt().toString()
            editDiskonStr = calculatedDiskon.toInt().toString()
        }

        var showAddProductMenu by remember { mutableStateOf(false) }
        val idrFormatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        idrFormatter.maximumFractionDigits = 0

        AlertDialog(
            onDismissRequest = { showCorrectionEditDialog = false },
            title = { Text("Form Koreksi Transaksi", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                ) {
                    Text("ID Transaksi: ${rx.id}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OrangePrimary)
                    
                    Text("Detail Produk Struk", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OrangePrimary)
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        if (parsedItems.isEmpty()) {
                            Text("Tidak ada produk di struk", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(8.dp))
                        } else {
                            parsedItems.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.nama, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("${idrFormatter.format(item.harga)} x ${item.jumlah}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // Reduce Qty Button
                                        IconButton(
                                            onClick = {
                                                if (item.jumlah > 1) {
                                                    parsedItems[index] = item.copy(jumlah = item.jumlah - 1)
                                                } else {
                                                    parsedItems.removeAt(index)
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.RemoveCircle, contentDescription = "Kurang", tint = OrangePrimary)
                                        }

                                        Text("${item.jumlah}", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))

                                        // Increase Qty Button
                                        IconButton(
                                            onClick = {
                                                parsedItems[index] = item.copy(jumlah = item.jumlah + 1)
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Tambah", tint = OrangePrimary)
                                        }

                                        // Delete Button
                                        IconButton(
                                            onClick = { parsedItems.removeAt(index) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red)
                                        }
                                    }
                                }
                                if (index < parsedItems.lastIndex) {
                                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }

                    // Button to add existing product to the receipt
                    Button(
                        onClick = { showAddProductMenu = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tambah Produk ke Struk", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = editTotalStr,
                        onValueChange = { editTotalStr = it },
                        label = { Text("Total Belanja Baru (Rp)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = editDiskonStr,
                        onValueChange = { editDiskonStr = it },
                        label = { Text("Diskon Baru (Rp)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = editBayarStr,
                        onValueChange = { editBayarStr = it },
                        label = { Text("Nominal Bayar Baru (Rp)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    // Dropdown for Metode Bayar
                    Column {
                        Text("Metode Pembayaran", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                            listOf("Tunai", "QRIS", "Transfer", "Debit").forEach { m ->
                                Card(
                                    onClick = { editMetodeBayar = m },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (editMetodeBayar == m) OrangePrimary else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(modifier = Modifier.padding(8.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text(m, fontSize = 10.sp, color = if (editMetodeBayar == m) Color.White else Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Dropdown for Status
                    Column {
                        Text("Status Pembayaran", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                            listOf("lunas", "dp").forEach { s ->
                                Card(
                                    onClick = { editStatus = s },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (editStatus == s) OrangePrimary else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(modifier = Modifier.padding(8.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text(s.uppercase(), fontSize = 10.sp, color = if (editStatus == s) Color.White else Color.Black, fontWeight = FontWeight.Bold)
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
                        val totalVal = editTotalStr.toDoubleOrNull() ?: calculatedTotal
                        val bayarVal = editBayarStr.toDoubleOrNull() ?: rx.bayarNominal
                        val diskonVal = editDiskonStr.toDoubleOrNull() ?: calculatedDiskon

                        if (bayarVal < totalVal && editStatus == "lunas") {
                            Toast.makeText(context, "Nominal bayar kurang dari total untuk status lunas!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (parsedItems.isEmpty()) {
                            Toast.makeText(context, "Struk tidak boleh kosong! Hapus transaksi jika ingin membatalkan.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val calculatedKembalian = (bayarVal - totalVal).coerceAtLeast(0.0)

                        // Serialize itemsRaw
                        val updatedItemsString = parsedItems.joinToString(";") {
                            "${it.id}:${it.nama}:${it.jumlah}:${it.harga}:${it.varianSelected}:${it.diskon}:${it.satuan}"
                        }

                        val updatedTx = rx.copy(
                            itemsRaw = updatedItemsString,
                            subtotal = calculatedSubtotal,
                            total = totalVal,
                            bayarNominal = bayarVal,
                            diskonTotal = diskonVal,
                            metodeBayar = editMetodeBayar,
                            status = editStatus,
                            kembalian = calculatedKembalian
                        )

                        viewModel.correctTransaction(updatedTx) {
                            showCorrectionEditDialog = false
                            selectedTxForReceipt = null
                            Toast.makeText(context, "Koreksi transaksi berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Simpan Koreksi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCorrectionEditDialog = false }) {
                    Text("Batal")
                }
            }
        )

        // Nested dialog to search and pick a product to add
        if (showAddProductMenu) {
            var searchProdQuery by remember { mutableStateOf("") }
            val filteredStoreProds = allProducts.filter { it.nama.contains(searchProdQuery, ignoreCase = true) }

            AlertDialog(
                onDismissRequest = { showAddProductMenu = false },
                title = { Text("Pilih Produk", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 300.dp)) {
                        OutlinedTextField(
                            value = searchProdQuery,
                            onValueChange = { searchProdQuery = it },
                            placeholder = { Text("Cari Produk...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                            items(filteredStoreProds) { prod ->
                                Card(
                                    onClick = {
                                        val existingIndex = parsedItems.indexOfFirst { it.id == prod.id && it.varianSelected.isEmpty() }
                                        if (existingIndex >= 0) {
                                            parsedItems[existingIndex] = parsedItems[existingIndex].copy(jumlah = parsedItems[existingIndex].jumlah + 1)
                                        } else {
                                            parsedItems.add(CorrectionItem(
                                                id = prod.id,
                                                nama = prod.nama,
                                                jumlah = 1,
                                                harga = prod.hargaJual,
                                                varianSelected = "",
                                                diskon = 0.0,
                                                satuan = prod.satuan
                                            ))
                                        }
                                        showAddProductMenu = false
                                    },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(prod.nama, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                            Text("Stok: ${prod.stok}", fontSize = 10.sp, color = Color.Gray)
                                        }
                                        Text(idrFormatter.format(prod.hargaJual), fontWeight = FontWeight.Bold, color = OrangePrimary, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAddProductMenu = false }) { Text("Batal") }
                }
            )
        }
    }
}

data class CorrectionItem(
    val id: String,
    val nama: String,
    val jumlah: Int,
    val harga: Double,
    val varianSelected: String,
    val diskon: Double,
    val satuan: String
)

@Composable
fun InteractiveFinancialChart(
    data: List<Pair<String, Double>>,
    currencyFormatter: NumberFormat
) {
    var selectedX by remember { mutableStateOf<Float?>(null) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val primaryColor = OrangePrimary
    val accentColor = OrangeDark

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Grafik Keuangan (7 Hari Terakhir)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Tekan & geser untuk detail interaktif",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                
                val labelShow = if (selectedIndex != null && selectedIndex!! in data.indices) {
                    val p = data[selectedIndex!!]
                    "${p.first}: ${currencyFormatter.format(p.second)}"
                } else {
                    val total7Days = data.sumOf { it.second }
                    "Total: ${currencyFormatter.format(total7Days)}"
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(OrangeLight)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = labelShow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangeDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val maxVal = maxOf(data.maxOfOrNull { it.second } ?: 1.0, 1000.0)

            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .pointerInput(data) {
                        detectTapGestures(
                            onPress = { offset ->
                                val width = size.width.toFloat()
                                val stepX = width / (data.size - 1).coerceAtLeast(1).toFloat()
                                val index = (offset.x / stepX).roundToInt().coerceIn(0, data.size - 1)
                                selectedX = index * stepX
                                selectedIndex = index
                                tryAwaitRelease()
                                selectedX = null
                                selectedIndex = null
                            }
                        )
                    }
                    .pointerInput(data) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val width = size.width.toFloat()
                                val stepX = width / (data.size - 1).coerceAtLeast(1).toFloat()
                                val index = (offset.x / stepX).roundToInt().coerceIn(0, data.size - 1)
                                selectedX = index * stepX
                                selectedIndex = index
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val offset = change.position
                                val width = size.width.toFloat()
                                val stepX = width / (data.size - 1).coerceAtLeast(1).toFloat()
                                val index = (offset.x / stepX).roundToInt().coerceIn(0, data.size - 1)
                                selectedX = index * stepX
                                selectedIndex = index
                            },
                            onDragEnd = {
                                selectedX = null
                                selectedIndex = null
                            },
                            onDragCancel = {
                                selectedX = null
                                selectedIndex = null
                            }
                        )
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val graphHeight = canvasHeight - 30.dp.toPx()

                val points = data.mapIndexed { idx, pair ->
                    val x = if (data.size > 1) idx * (canvasWidth / (data.size - 1)) else canvasWidth / 2
                    val ratio = (pair.second / maxVal).coerceIn(0.0, 1.0)
                    val y = graphHeight - (ratio * (graphHeight - 20.dp.toPx())).toFloat()
                    androidx.compose.ui.geometry.Offset(x, y)
                }

                // Draw background grid lines
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = 20.dp.toPx() + i * (graphHeight - 20.dp.toPx()) / gridLines
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.4f),
                        start = androidx.compose.ui.geometry.Offset(0f, y),
                        end = androidx.compose.ui.geometry.Offset(canvasWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw curve path
                val curvePath = androidx.compose.ui.graphics.Path().apply {
                    if (points.isNotEmpty()) {
                        moveTo(points[0].x, points[0].y)
                        for (i in 1 until points.size) {
                            val prev = points[i - 1]
                            val curr = points[i]
                            cubicTo(
                                x1 = prev.x + (curr.x - prev.x) / 2f,
                                y1 = prev.y,
                                x2 = prev.x + (curr.x - prev.x) / 2f,
                                y2 = curr.y,
                                x3 = curr.x,
                                y3 = curr.y
                            )
                        }
                    }
                }

                // Gradient fill
                val areaPath = androidx.compose.ui.graphics.Path().apply {
                    addPath(curvePath)
                    if (points.isNotEmpty()) {
                        lineTo(points.last().x, graphHeight)
                        lineTo(points.first().x, graphHeight)
                        close()
                    }
                }

                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.25f), Color.Transparent),
                        startY = 10.dp.toPx(),
                        endY = graphHeight
                    )
                )

                drawPath(
                    path = curvePath,
                    color = primaryColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 3.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                )

                // Draw labels
                data.forEachIndexed { idx, pair ->
                    val pt = points[idx]
                    drawContext.canvas.nativeCanvas.drawText(
                        pair.first,
                        pt.x,
                        canvasHeight - 6.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 10.dp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                    )
                }

                // Draw circles inside points
                points.forEach { pt ->
                    drawCircle(
                        color = primaryColor,
                        radius = 4.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 1.5.dp.toPx(),
                        center = pt
                    )
                }

                // Vertical dash interaction line display
                selectedX?.let { xVal ->
                    drawLine(
                        color = accentColor,
                        start = androidx.compose.ui.geometry.Offset(xVal, 10.dp.toPx()),
                        end = androidx.compose.ui.geometry.Offset(xVal, graphHeight),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(10f, 10f), 0f
                        )
                    )

                    selectedIndex?.let { sIdx ->
                        if (sIdx in points.indices) {
                            val activePt = points[sIdx]
                            drawCircle(
                                color = accentColor,
                                radius = 7.dp.toPx(),
                                center = activePt
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 4.dp.toPx(),
                                center = activePt
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Text(
    text: String,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
    fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    fontStyle: androidx.compose.ui.text.font.FontStyle? = null,
    fontWeight: androidx.compose.ui.text.font.FontWeight? = null,
    fontFamily: androidx.compose.ui.text.font.FontFamily? = null,
    letterSpacing: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    textDecoration: androidx.compose.ui.text.style.TextDecoration? = null,
    textAlign: androidx.compose.ui.text.style.TextAlign? = null,
    lineHeight: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    overflow: androidx.compose.ui.text.style.TextOverflow = androidx.compose.ui.text.style.TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((androidx.compose.ui.text.TextLayoutResult) -> Unit)? = null,
    style: androidx.compose.ui.text.TextStyle = androidx.compose.material3.LocalTextStyle.current
) {
    androidx.compose.material3.Text(
        text = com.kasirpro.app.util.t(text),
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style
    )
}
