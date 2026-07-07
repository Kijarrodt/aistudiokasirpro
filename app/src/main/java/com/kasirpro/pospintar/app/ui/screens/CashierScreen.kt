package com.kasirpro.pospintar.app.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kasirpro.pospintar.app.data.local.*
import com.kasirpro.pospintar.app.data.repository.ProductVariant
import com.kasirpro.pospintar.app.data.repository.TransactionItem
import com.kasirpro.pospintar.app.ui.viewmodel.KasirViewModel
import com.kasirpro.pospintar.app.ui.theme.*
import com.kasirpro.pospintar.app.util.BarcodeScannerHelper
import com.kasirpro.pospintar.app.util.BluetoothPrinterHelper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.kasirpro.pospintar.app.util.ProductImage
import com.kasirpro.pospintar.app.util.ShopLogoImage
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private fun playBarcodeBeep() {
    try {
        val toneGen = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 100)
        toneGen.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 150)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun triggerVibe(context: android.content.Context) {
    try {
        val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
        if (vibrator != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(50)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashierScreen(viewModel: KasirViewModel) {
    val business by viewModel.currentBusiness.collectAsState()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsState()
    val productsList by viewModel.products.collectAsState()
    val cart by viewModel.cartItems.collectAsState()
    val currentCustomer by viewModel.selectedCustomer.collectAsState()
    val activePromo by viewModel.appliedPromo.collectAsState()
    val isSplitEnabled by viewModel.splitPaymentEnabled.collectAsState()
    val splits by viewModel.splitPayments.collectAsState()
    val statusTrx by viewModel.transactionStatus.collectAsState()
    val cashPaid by viewModel.cashAmountPaid.collectAsState()
    val customersList by viewModel.customers.collectAsState()
    val promosList by viewModel.promos.collectAsState()

    val user by viewModel.currentUser.collectAsState()
    val activeShift by viewModel.activeShift.collectAsState()
    val isKasir = user?.role == "kasir" || user?.role == "kasir_invited"
    var modalInputVal by remember { mutableStateOf("") }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }
    
    // Bottom Sheet Checkout States
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showDiscountDialogItem by remember { mutableStateOf<TransactionItem?>(null) }
    var showCustomerPicker by remember { mutableStateOf(false) }
    var showPromoPicker by remember { mutableStateOf(false) }
    
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var newCustNama by remember { mutableStateOf("") }
    var newCustHp by remember { mutableStateOf("") }
    var newCustAlamat by remember { mutableStateOf("") }

    var redeemPointsInput by remember { mutableStateOf("") }

    var showRecentTransactionsDialog by remember { mutableStateOf(false) }
    var selectedTxForReceipt by remember { mutableStateOf<com.kasirpro.pospintar.app.data.local.TransactionEntity?>(null) }
    var showCorrectionAuthDialog by remember { mutableStateOf(false) }
    var showCorrectionEditDialog by remember { mutableStateOf(false) }
    var authCodeInput by remember { mutableStateOf("") }

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var editExpenseNominal by remember { mutableStateOf("") }
    var editExpenseKet by remember { mutableStateOf("") }
    
    // Auto collapse checkout dialog if cart is fully emptied
    LaunchedEffect(cart) {
        if (cart.isEmpty() && showCheckoutDialog) {
            showCheckoutDialog = false
        }
    }
    
    val context = LocalContext.current
    val categories = listOf("Semua") + productsList.map { it.kategori }.filter { !it.isNullOrBlank() }.distinct()

    val filteredProducts = productsList.filter {
        (selectedCategory == "Semua" || it.kategori == selectedCategory) &&
        (it.nama.contains(searchQuery, ignoreCase = true) || (it.barcode ?: "").contains(searchQuery))
    }

    val prefs = remember(context) { context.getSharedPreferences("kasir_pro_prefs", android.content.Context.MODE_PRIVATE) }
    val isLoyaltyEnabled = remember(prefs) { prefs.getBoolean("is_loyalty_enabled", true) }
    val pointRateValue = remember(prefs) { prefs.getFloat("point_rate", 100f) }

    val idrFormatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    idrFormatter.maximumFractionDigits = 0

    // Cart calculations
    val totalItemsCount = cart.sumOf { it.jumlah }
    val cartSubtotal = cart.sumOf { (it.harga - it.diskon) * it.jumlah }
    
    val promoDiscountAmount = activePromo?.let { p ->
        if (p.tipe == "diskon_persen") {
            cartSubtotal * (p.nilai / 100.0)
        } else {
            p.nilai
        }
    } ?: 0.0

    val pointsRedeemedAmountVal = if (isLoyaltyEnabled && currentCustomer != null) {
        redeemPointsInput.toIntOrNull() ?: 0
    } else 0
    val pointsDiscountAmount = pointsRedeemedAmountVal * pointRateValue.toDouble()

    val orderTotal = (cartSubtotal - promoDiscountAmount - pointsDiscountAmount).coerceAtLeast(0.0)

    // Shift modal lock popup
    if (isKasir && activeShift == null) {
        AlertDialog(
            onDismissRequest = { /* strictly non-dismissible */ },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LockClock, contentDescription = null, tint = OrangePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mulai Shift Kasir", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Masukkan jumlah saldo uang modal awal (cash) yang tersedia di laci uang kasir untuk memulai pencatatan shift hari ini.",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                    OutlinedTextField(
                        value = modalInputVal,
                        onValueChange = { modalInputVal = it.filter { char -> char.isDigit() } },
                        label = { Text("Modal Awal (Rp)") },
                        placeholder = { Text("Contoh: 50000") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = androidx.compose.ui.text.input.ImeAction.Done
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("modal_awal_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val modalDouble = modalInputVal.toDoubleOrNull() ?: 0.0
                        viewModel.startShift(modalDouble)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    enabled = modalInputVal.isNotEmpty(),
                    modifier = Modifier.testTag("start_shift_trigger_btn")
                ) {
                    Text("Mulai Shift POS")
                }
            }
        )
    }

    val scanInteractionSource = remember { MutableInteractionSource() }
    val isScanPressed by scanInteractionSource.collectIsPressedAsState()
    val scanScale by animateFloatAsState(
        targetValue = if (isScanPressed) 0.9f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "scan_barcode_scale"
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Kasir Pos Penjualan", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    if (!isKasir) {
                        IconButton(onClick = { viewModel.activeScreen.value = "home" }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu Kasir", tint = OrangePrimary)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Transaksi Terbaru & Koreksi", fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(Icons.Default.History, contentDescription = null, tint = OrangePrimary) },
                            onClick = {
                                showMenu = false
                                showRecentTransactionsDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Catat Pengeluaran", fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = OrangePrimary) },
                            onClick = {
                                showMenu = false
                                showAddExpenseDialog = true
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    BarcodeScannerHelper.startScan(context) { scannedValue ->
                        val matched = productsList.find { it.barcode == scannedValue }
                        if (matched != null) {
                            playBarcodeBeep()
                            triggerVibe(context)
                            if ((matched.varianRaw ?: "").isNotBlank()) {
                                viewModel.showVarianDialog.value = matched
                            } else {
                                viewModel.addToCart(matched)
                                val currentCount = cart.filter { it.id == matched.id }.sumOf { it.jumlah }
                                Toast.makeText(context, "${matched.nama} ditambahkan ke keranjang (Banyaknya: ${currentCount + 1})", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Barcode '$scannedValue' tidak ditemukan!", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                shape = CircleShape,
                containerColor = OrangePrimary,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 4.dp
                ),
                interactionSource = scanInteractionSource,
                modifier = Modifier
                    .graphicsLayer(scaleX = scanScale, scaleY = scanScale)
                    .testTag("scan_barcode_icon")
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Scan Barcode",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        bottomBar = {
            if (cart.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cart_preview_bar"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("$totalItemsCount Item Terpilih", fontSize = 12.sp)
                            Text(idrFormatter.format(orderTotal), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OrangePrimary)
                        }
                        Button(
                            onClick = { showCheckoutDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("pay_sheet_trigger")
                        ) {
                            Text("Bayar Sekarang", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search & Category Tags Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari produk atau barcode...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pos_product_search"),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Quick Category selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(modifier = Modifier.width(16.dp))
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        onClick = { selectedCategory = cat },
                        shape = RoundedCornerShape(24.dp),
                        color = if (isSelected) OrangePrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Icon(
                                imageVector = if (cat == "Semua") Icons.Default.GridView else Icons.Default.Folder,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Inventory2, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(72.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Produk Kosong", fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("Silakan tambahkan produk baru di menu Kelola Produk terlebih dahulu.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                     items(filteredProducts) { item ->
                        Card(
                            onClick = {
                                triggerVibe(context)
                                // Check if variant exist
                                val parts = (item.varianRaw ?: "").split(";").filter { it.isNotBlank() }
                                if (parts.isNotEmpty()) {
                                    viewModel.showVarianDialog.value = item
                                } else {
                                    viewModel.addToCart(item)
                                }
                            },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(110.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(OrangeLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        ProductImage(
                                            fotoBase64 = item.fotoBase64,
                                            contentDescription = item.nama,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = item.nama,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = idrFormatter.format(item.hargaJual),
                                        fontWeight = FontWeight.Bold,
                                        color = OrangePrimary,
                                        fontSize = 13.sp
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Stok: ${item.stok} ${item.satuan}", fontSize = 11.sp, color = if (item.stok <= item.stokMinimum) Color.Red else Color.Gray)
                                        if ((item.varianRaw ?: "").isNotBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(OrangeLight)
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text("VARIAN", fontSize = 9.sp, color = OrangeDark, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                val currentJumlahInCart = cart.filter { it.id == item.id }.sumOf { it.jumlah }
                                if (currentJumlahInCart > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .size(24.dp)
                                            .background(OrangePrimary, shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$currentJumlahInCart",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Custom Varian Options Dialog Selector
    val activeVarianProd by viewModel.showVarianDialog.collectAsState()
    if (activeVarianProd != null) {
        val prod = activeVarianProd!!
        val options = (prod.varianRaw ?: "").split(";").filter { it.isNotBlank() }.map { ProductVariant.fromString(it) }
        
        AlertDialog(
            onDismissRequest = { viewModel.showVarianDialog.value = null },
            title = { Text("Pilih Varian Produk") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(prod.nama, fontWeight = FontWeight.Bold)
                    options.forEach { opt ->
                        Card(
                            onClick = {
                                triggerVibe(context)
                                viewModel.addToCart(prod, opt)
                                viewModel.showVarianDialog.value = null
                            },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(opt.nama, fontWeight = FontWeight.SemiBold)
                                Text(idrFormatter.format(opt.harga), color = OrangePrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.showVarianDialog.value = null }) { Text("Batal") }
            }
        )
    }

    // Scanner Simulated Popup Panel Dialog
    val activeScanDialog by viewModel.showScanBarcodeDialog.collectAsState()
    if (activeScanDialog) {
        var simulatedScannedBarcode by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.showScanBarcodeDialog.value = false },
            icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(48.dp)) },
            title = { Text("Pindai Barcode Produk") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Masukkan manual kode barcode produk untuk memulai pencarian produk secara cepat.",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    OutlinedTextField(
                        value = simulatedScannedBarcode,
                        onValueChange = { simulatedScannedBarcode = it },
                        placeholder = { Text("contoh: 899123456001") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val matched = productsList.find { it.barcode == simulatedScannedBarcode }
                        if (matched != null) {
                            playBarcodeBeep()
                            triggerVibe(context)
                            if ((matched.varianRaw ?: "").isNotBlank()) {
                                viewModel.showVarianDialog.value = matched
                            } else {
                                viewModel.addToCart(matched)
                                Toast.makeText(context, "${matched.nama} sukses ditambahkan!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Barcode tidak terdaftar!", Toast.LENGTH_SHORT).show()
                        }
                        viewModel.showScanBarcodeDialog.value = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Simpan & Scan")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showScanBarcodeDialog.value = false }) { Text("Batal") }
            }
        )
    }

    // CHECKOUT DRAWER AND PAYMENT SPLIT CONTROLLERS
    if (showCheckoutDialog) {
        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            title = { Text("Konfirmasi Pembayaran Kasir", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                ) {
                    // Invoice Item List Summary
                    item {
                        Text("Ringkasan Pesanan", fontWeight = FontWeight.Bold, color = OrangePrimary)
                    }
                    items(cart) { ci ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ci.nama + (ci.varianSelected?.let { " ($it)" } ?: ""),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = idrFormatter.format(ci.harga),
                                        fontSize = 11.sp,
                                        color = OrangePrimary
                                    )
                                    
                                    // Control Buttons Row
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        // Reduce quantity button
                                        IconButton(
                                            onClick = { 
                                                triggerVibe(context)
                                                viewModel.updateCartQuantity(ci, ci.jumlah - 1) 
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "Kurang",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        
                                        // Current Quantity
                                        Text(
                                            text = "${ci.jumlah}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                        
                                        // Increase quantity button
                                        IconButton(
                                            onClick = { 
                                                triggerVibe(context)
                                                viewModel.updateCartQuantity(ci, ci.jumlah + 1) 
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Tambah",
                                                tint = OrangePrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        // Delete/Cancel Item completely
                                        IconButton(
                                            onClick = { 
                                                triggerVibe(context)
                                                viewModel.updateCartQuantity(ci, 0) 
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Batal",
                                                tint = Color.Red,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                                
                                Text(
                                    text = idrFormatter.format(ci.subtotal()),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = Color.Black,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }

                    // Divider line
                    item { HorizontalDivider() }

                    // Promo setup
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Promo & Loyalty", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                if (currentCustomer != null) {
                                    Text("Pelanggan: ${currentCustomer?.nama} (Stok: ${currentCustomer?.totalPoin} Poin, +${(orderTotal/10000).toInt()} Poin)", fontSize = 11.sp, color = OrangePrimary)
                                }
                                if (activePromo != null) {
                                    Text("Kupon: ${activePromo?.kode} (-${idrFormatter.format(promoDiscountAmount)})", fontSize = 11.sp, color = Color(0xFF15803D))
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(onClick = { showCustomerPicker = true }) {
                                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add Customer", tint = OrangePrimary)
                                }
                                IconButton(onClick = { showPromoPicker = true }) {
                                    Icon(imageVector = Icons.Default.ConfirmationNumber, contentDescription = "Promo", tint = OrangePrimary)
                                }
                            }
                        }
                    }

                    if (isLoyaltyEnabled && currentCustomer != null && currentCustomer!!.totalPoin > 0) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).background(OrangePrimary.copy(alpha = 0.05f)).padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Tukar Loyalty Poin (Maks ${currentCustomer!!.totalPoin} Poin)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OrangePrimary
                                    )
                                    Text(
                                        text = "Nilai: ${idrFormatter.format(pointRateValue)}/Poin",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = redeemPointsInput,
                                    onValueChange = { input ->
                                        val filteredInput = input.filter { it.isDigit() }
                                        val points = filteredInput.toIntOrNull() ?: 0
                                        if (points <= currentCustomer!!.totalPoin) {
                                            redeemPointsInput = filteredInput
                                        }
                                    },
                                    label = { Text("Jumlah poin yang ditukar", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                    placeholder = { Text("Masukkan poin, misal: 10") }
                                )
                                if (pointsDiscountAmount > 0.0) {
                                    Text(
                                        text = "Potongan Harga: -${idrFormatter.format(pointsDiscountAmount)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Payment partial choice / DP option
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Metode Status Piutang", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = statusTrx == "lunas", onClick = { viewModel.transactionStatus.value = "lunas" })
                                    Text("LUNAS", fontSize = 12.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = statusTrx == "dp", onClick = { viewModel.transactionStatus.value = "dp" })
                                    Text("DP / PIUTANG", fontSize = 12.sp)
                                }
                            }

                            if (statusTrx == "dp" && currentCustomer == null) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.08f)),
                                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Wajib memilih pelanggan jika status adalah Hutang (DP)",
                                                color = Color.Red,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Button(
                                            onClick = { showCustomerPicker = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 12.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Pilih / Tambah Pelanggan Sekarang", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Choice of Payment Method
                    item {
                        Column {
                            Text("Metode Pembayaran", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                listOf("Tunai", "QRIS").forEach { m ->
                                    Card(
                                        onClick = { viewModel.selectedPaymentMethod.value = m },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (selectedPaymentMethod == m) OrangePrimary else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(modifier = Modifier.padding(10.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                            Text(
                                                m.uppercase(),
                                                fontSize = 11.sp,
                                                color = if (selectedPaymentMethod == m) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (selectedPaymentMethod == "QRIS") {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("SCAN QRIS TOKO", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OrangePrimary)
                                
                                if (!business?.qrisBase64.isNullOrBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .size(220.dp)
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        com.kasirpro.pospintar.app.util.ShopQrisImage(
                                            qrisBase64 = business?.qrisBase64,
                                            contentDescription = "QRIS Live",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Text(
                                        "Silakan pelanggan scan QRIS di atas untuk membayar sejumlah:",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        idrFormatter.format(orderTotal),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = Color.Black,
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QrCodeScanner,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Foto QRIS Belum Tersedia",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Red
                                        )
                                        Text(
                                            "Pemilik toko harus meng-upload QRIS di menu Pengaturan terlebih dahulu.",
                                            fontSize = 10.sp,
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Money calculator
                        item {
                            Column {
                                Text("Nominal Pembayaran Tunai", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = if (cashPaid == 0.0) "" else cashPaid.toInt().toString(),
                                        onValueChange = { viewModel.cashAmountPaid.value = it.toDoubleOrNull() ?: 0.0 },
                                        placeholder = { Text("Bayar Pas: ${orderTotal.toInt()}") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f).testTag("cash_input_text")
                                    )
                                    Button(
                                        onClick = { viewModel.cashAmountPaid.value = orderTotal },
                                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                                    ) {
                                        Text("PAS", fontSize = 12.sp)
                                    }
                                }
                                // Quick select nominal bills helper
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(20000.0, 50000.0, 100000.0).forEach { bill ->
                                        Card(
                                            modifier = Modifier
                                                .clickable { viewModel.cashAmountPaid.value = bill }
                                                .weight(1f),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Text(
                                                idrFormatter.format(bill),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Auto Change Kembalian calculation
                        item {
                            val diff = cashPaid - orderTotal
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (diff >= 0) "Kembalian Anda:" else "Sisa Piutang (Kekurangan):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = idrFormatter.format(Math.abs(diff)),
                                    fontWeight = FontWeight.Bold,
                                    color = if (diff >= 0) Color(0xFF15803D) else Color.Red,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val user = viewModel.currentUser.value
                        val isPremium = user?.isPremium ?: false

                        // Debt/DP Customer mandatory check
                        if (statusTrx == "dp" && currentCustomer == null) {
                            Toast.makeText(context, "Khusus transaksi hutang (DP/Piutang), Anda wajib memilih pelanggan yang sudah ada atau menambahkan pelanggan baru!", Toast.LENGTH_LONG).show()
                            viewModel.showToast("Peringatan: Transaksi hutang wajib memilih pelanggan!")
                            return@Button
                        }

                        // Free limit transaction guard (50 transactions limit)
                        if (!isPremium && viewModel.transactions.value.size >= 50) {
                            showCheckoutDialog = false
                            viewModel.showLimitPopup.value = "Transaksi gratis bulanan mencapai batas 50 kali. Silakan upgrade ke premium!"
                            return@Button
                        }

                        // Execute checkout
                        viewModel.processCheckout(pointsRedeemedAmount = pointsRedeemedAmountVal, pointRateValue = pointRateValue.toDouble())
                        if (statusTrx == "dp") {
                            viewModel.showToast("Sukses: Sisa hutang pelanggan berhasil dicatat!")
                        } else {
                            viewModel.showToast("Sukses: Pembayaran lunas diproses!")
                        }
                        redeemPointsInput = ""
                        showCheckoutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (statusTrx == "dp" && currentCustomer == null) Color.Gray else OrangePrimary),
                    modifier = Modifier.testTag("finalize_payment_button")
                ) {
                    Text(if (statusTrx == "dp") "Proses Hutang" else "Proses Lunas")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutDialog = false }) { Text("Kembali") }
            }
        )
    }

    // Secondarypicker: Customer choose Loyalty DB Dialog
    if (showCustomerPicker) {
        val isPremium = user?.isPremium ?: false
        if (customersList.isEmpty()) {
            if (isPremium) {
                AlertDialog(
                    onDismissRequest = { showCustomerPicker = false },
                    icon = { Icon(imageVector = Icons.Default.People, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(48.dp)) },
                    title = { Text("Belum Ada Data Pelanggan", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
                    text = {
                        Text(
                            "Anda belum menambahkan data pelanggan. Tambahkan pelanggan terlebih dahulu melalui menu Pelanggan.",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showCustomerPicker = false
                                showAddCustomerDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                        ) {
                            Text("Tambah Pelanggan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCustomerPicker = false }) {
                            Text("Tutup")
                        }
                    }
                )
            } else {
                AlertDialog(
                    onDismissRequest = { showCustomerPicker = false },
                    icon = { Icon(imageVector = Icons.Default.People, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(48.dp)) },
                    title = { Text("Belum Ada Data Pelanggan", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
                    text = {
                        Text(
                            "Fitur manajemen pelanggan tersedia di paket Premium. Upgrade sekarang untuk mengelola loyalitas pelanggan dan meningkatkan penjualan.",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showCustomerPicker = false
                                viewModel.activeScreen.value = "premium_pricing"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                        ) {
                            Text("Upgrade Premium")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCustomerPicker = false }) {
                            Text("Tutup")
                        }
                    }
                )
            }
        } else {
            AlertDialog(
                onDismissRequest = { showCustomerPicker = false },
                title = { 
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Pilih Pelanggan Loyalty Hub", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (isPremium) {
                            OutlinedButton(
                                onClick = {
                                    showCustomerPicker = false
                                    showAddCustomerDialog = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary),
                                border = BorderStroke(1.dp, OrangePrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("+ Tambah Pelanggan Baru", fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp)) {
                            items(customersList) { c ->
                                Card(
                                    onClick = {
                                        viewModel.selectedCustomer.value = c
                                        showCustomerPicker = false
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text(c.nama, fontWeight = FontWeight.SemiBold)
                                            Text("Telp: ${c.nomorHp}", fontSize = 11.sp, color = Color.Gray)
                                            if (!c.alamat.isNullOrBlank()) {
                                                Text("Alamat: ${c.alamat}", fontSize = 10.sp, color = Color.Gray)
                                            }
                                        }
                                        Text("${c.totalPoin} PTS", fontWeight = FontWeight.Bold, color = OrangePrimary)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCustomerPicker = false }) { Text("Batal") }
                }
            )
        }
    }

    // CUSTOM DIALOG: TAMBAH PELANGGAN BARU KASIR
    if (showAddCustomerDialog) {
        var nameError by remember { mutableStateOf(false) }
        var hpError by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { showAddCustomerDialog = false },
            icon = { Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(40.dp)) },
            title = { Text("Tambah Pelanggan Baru", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Masukkan data pelanggan untuk dihubungkan ke program loyalty & bonus poin warung.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    
                    OutlinedTextField(
                        value = newCustNama,
                        onValueChange = { 
                            newCustNama = it
                            nameError = it.isBlank()
                        },
                        label = { Text("Nama Lengkap*") },
                        placeholder = { Text("Contoh: Ahmad Budiman") },
                        isError = nameError,
                        supportingText = { if (nameError) Text("Nama wajib diisi", color = MaterialTheme.colorScheme.error) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = newCustHp,
                        onValueChange = { 
                            newCustHp = it
                            hpError = it.isBlank()
                        },
                        label = { Text("No. WhatsApp/HP*") },
                        placeholder = { Text("Contoh: 08123456789") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = hpError,
                        supportingText = { if (hpError) Text("Nomor WA wajib diisi", color = MaterialTheme.colorScheme.error) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = newCustAlamat,
                        onValueChange = { newCustAlamat = it },
                        label = { Text("Alamat (Opsional)") },
                        placeholder = { Text("Contoh: Jl. Merdeka No. 10") },
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val n = newCustNama.trim()
                        val h = newCustHp.trim()
                        val a = newCustAlamat.trim().takeIf { it.isNotBlank() }
                        
                        if (n.isBlank() || h.isBlank()) {
                            nameError = n.isBlank()
                            hpError = h.isBlank()
                            return@Button
                        }
                        
                        viewModel.addCustomer(n, h, a)
                        Toast.makeText(context, "Pelanggan $n berhasil disimpan!", Toast.LENGTH_SHORT).show()
                        
                        // Reset forms
                        newCustNama = ""
                        newCustHp = ""
                        newCustAlamat = ""
                        showAddCustomerDialog = false
                        // Automatically re-open customer picker so they can select the newly added customer!
                        showCustomerPicker = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showAddCustomerDialog = false 
                    }
                ) {
                    Text("Batal")
                }
            }
        )
    }

    // Secondarypicker: Promos picker Dialog
    if (showPromoPicker) {
        AlertDialog(
            onDismissRequest = { showPromoPicker = false },
            title = { Text("Gunakan Voucher Kupon Promo") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp)) {
                    if (promosList.isEmpty()) {
                        item { Text("Database voucher promo kosong.", fontSize = 11.sp, color = Color.Gray) }
                    }
                    items(promosList.filter { it.isActive }) { p ->
                        Card(
                            onClick = {
                                viewModel.appliedPromo.value = p
                                showPromoPicker = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(p.nama, fontWeight = FontWeight.SemiBold)
                                    Text("Kode: ${p.kode}", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                }
                                val amountText = if (p.tipe == "diskon_persen") "${p.nilai.toInt()}%" else idrFormatter.format(p.nilai)
                                Text(amountText, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPromoPicker = false }) { Text("Batal") }
            }
        )
    }

    // Final digital structural thermal receipt popup modal
    val activeReceiptState by viewModel.activeReceipt.collectAsState()
    if (activeReceiptState != null) {
        val rx = activeReceiptState!!
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        var showBluetoothSimulation by remember { mutableStateOf(false) }
        var pairedPrinters by remember { mutableStateOf<List<android.bluetooth.BluetoothDevice>>(emptyList()) }
        var isPrinting by remember { mutableStateOf(false) }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            val allGranted = results.values.all { it }
            if (allGranted) {
                pairedPrinters = BluetoothPrinterHelper.getPairedPrinters(context)
                showBluetoothSimulation = true
            } else {
                Toast.makeText(context, "Izin Bluetooth ditolak. Gagal menyambungkan ke printer.", Toast.LENGTH_SHORT).show()
            }
        }

        // Thermal Struk Dialog
        AlertDialog(
            onDismissRequest = { viewModel.activeReceipt.value = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Transaksi Berhasil!", fontWeight = FontWeight.Bold, color = Color(0xFF22C55E), fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Dynamic Shop Logo
                    if (!business?.logoBase64.isNullOrBlank()) {
                        ShopLogoImage(
                            logoBase64 = business?.logoBase64,
                            contentDescription = business?.namaBisnis,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .padding(bottom = 6.dp)
                        )
                    }
                    Text(business?.namaBisnis ?: "KASIR PRO SHOP", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 15.sp, textAlign = TextAlign.Center)
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
                    Text("Terima kasih atas kunjungan Anda!", fontSize = 11.sp, textAlign = TextAlign.Center, color = Color.Black)
                }
            },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                // WhatsApp share implicit intent action
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "Terima kasih telah berbelanja di Kasir Pro! Total belanja Anda: ${idrFormatter.format(rx.total)} dengan status: ${rx.status.uppercase()}")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Bagikan struk penjualan"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bagikan WA", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                if (BluetoothPrinterHelper.hasBluetoothPermissions(context)) {
                                    pairedPrinters = BluetoothPrinterHelper.getPairedPrinters(context)
                                    showBluetoothSimulation = true
                                } else {
                                    permissionLauncher.launch(BluetoothPrinterHelper.bluetoothPermissions)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Print, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cetak Kasir", fontSize = 11.sp)
                        }
                    }
                    TextButton(
                        onClick = { viewModel.activeReceipt.value = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Selesai")
                    }
                }
            }
        )

        // Bluetooth Simulation/Real Printer Dialog Helper
        if (showBluetoothSimulation) {
            AlertDialog(
                onDismissRequest = { showBluetoothSimulation = false },
                icon = { Icon(Icons.Default.Bluetooth, contentDescription = null, tint = OrangePrimary) },
                title = { Text("Hubungkan Printer Thermal Bluetooth") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Pastikan printer Anda menyala dan telah dipasangkan (paired) di pengaturan Bluetooth HP.", fontSize = 11.sp)
                        
                        if (isPrinting) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = OrangePrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mencetak struk...", fontSize = 12.sp)
                            }
                        } else if (pairedPrinters.isEmpty()) {
                            Text("Tidak ada printer Bluetooth berpasangan ditemukan.", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Button(
                                onClick = {
                                    if (BluetoothPrinterHelper.hasBluetoothPermissions(context)) {
                                        pairedPrinters = BluetoothPrinterHelper.getPairedPrinters(context)
                                    } else {
                                        permissionLauncher.launch(BluetoothPrinterHelper.bluetoothPermissions)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                            ) {
                                Text("Muat Ulang / Cari Printer", fontSize = 11.sp)
                            }
                        } else {
                            Text("Pilih printer dari daftar di bawah ini:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.heightIn(max = 200.dp)
                            ) {
                                items(pairedPrinters) { device ->
                                    Card(
                                        onClick = {
                                            coroutineScope.launch {
                                                isPrinting = true
                                                val success = BluetoothPrinterHelper.printReceipt(
                                                    context = context,
                                                    device = device,
                                                    rx = rx,
                                                    businessName = viewModel.currentBusiness.value?.namaBisnis ?: "KASIR PRO",
                                                    address = viewModel.currentBusiness.value?.alamat ?: "",
                                                    phone = viewModel.currentBusiness.value?.noTelpon ?: ""
                                                )
                                                isPrinting = false
                                                if (success) {
                                                    Toast.makeText(context, "Berhasil mencetak ke printer: ${device.name ?: "Unknown"}", Toast.LENGTH_SHORT).show()
                                                    showBluetoothSimulation = false
                                                    viewModel.activeReceipt.value = null
                                                } else {
                                                    Toast.makeText(context, "Gagal mencetak. Harap sambungkan kembali printer Bluetooth Anda.", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        },
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(device.name ?: "Printer Tanpa Nama", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Text(device.address ?: "00:00:00:00:00:00", fontSize = 10.sp, color = Color.Gray)
                                            }
                                            Icon(Icons.Default.Bluetooth, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Simulation mode fallback button
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        Card(
                            onClick = {
                                Toast.makeText(context, "Simulasi Cetak Struk ke printer RPP02N - Selesai!", Toast.LENGTH_LONG).show()
                                showBluetoothSimulation = false
                                viewModel.activeReceipt.value = null
                            },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Simulasi Cetak (Bypass)", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Color.Black)
                                    Text("Gunakan jika tidak ada printer berpasangan", fontSize = 10.sp, color = Color.Gray)
                                }
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showBluetoothSimulation = false }) { Text("Kembali") }
                }
            )
        }
    }

        // ================= TRANSAKSI TERBARU & KOREKSI DI ROLE KASIR =================
        if (showRecentTransactionsDialog) {
            val transList by viewModel.transactions.collectAsState()
            val recentList = remember(transList) {
                transList.sortedByDescending { it.createdAt }.take(15)
            }
            AlertDialog(
                onDismissRequest = { showRecentTransactionsDialog = false },
                title = { Text("Transaksi Terbaru & Koreksi", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("Pilih transaksi untuk melihat detail struk atau melakukan perbaikan menggunakan authorization PIN pemilik toko.", fontSize = 11.sp, color = Color.Gray)
                        if (recentList.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                Text("Tidak ada transaksi selama shift ini.", color = Color.Gray, fontSize = 11.sp)
                            }
                        } else {
                            recentList.forEach { tx ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedTxForReceipt = tx
                                            showRecentTransactionsDialog = false
                                        },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(tx.id, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            Text(tx.metodeBayar, fontSize = 9.sp, color = Color.Gray)
                                            val dateStr = remember(tx.createdAt) {
                                                val sdf = java.text.SimpleDateFormat("dd MMM yyyy HH:mm", java.util.Locale("id", "ID"))
                                                sdf.format(java.util.Date(tx.createdAt))
                                            }
                                            Text(dateStr, fontSize = 9.sp, color = Color.Gray)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(idrFormatter.format(tx.total), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OrangePrimary)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (tx.status == "lunas") Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = tx.status.uppercase(),
                                                    fontSize = 8.sp,
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
                },
                confirmButton = {
                    TextButton(onClick = { showRecentTransactionsDialog = false }) { Text("Tutup") }
                }
            )
        }

        // Receipt Details Dialog with Edit Corection Button
        if (selectedTxForReceipt != null) {
            val rx = selectedTxForReceipt!!
            AlertDialog(
                onDismissRequest = { selectedTxForReceipt = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = OrangePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Detail Struk Transaksi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                        Text("Tanggal: ${java.text.SimpleDateFormat("dd-MM-yyyy HH:mm", java.util.Locale("id", "ID")).format(java.util.Date(rx.createdAt))}", fontSize = 10.sp, color = Color.Black)
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
                    }
                },
                confirmButton = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                viewModel.activeReceipt.value = rx
                                selectedTxForReceipt = null
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
                                showCorrectionAuthDialog = true
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

        // Owner verification code dialog
        if (showCorrectionAuthDialog) {
            AlertDialog(
                onDismissRequest = { showCorrectionAuthDialog = false },
                title = { Text("Otoritas Pemilik Diperlukan", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Masukkan kode otoritas/sandi pemilik warung untuk mengizinkan kasir mengedit data transaksi struk ini.", fontSize = 11.sp, color = Color.Gray)
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
                                authCodeInput = ""
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
                    TextButton(onClick = { showCorrectionAuthDialog = false }) { Text("Batal") }
                }
            )
        }

        // Form Koreksi Transaksi (Edit & Save)
        if (showCorrectionEditDialog && selectedTxForReceipt != null) {
            val rx = selectedTxForReceipt!!
            val allProducts by viewModel.products.collectAsState()

            // Parse items
            val parsedItems = remember(rx.id) {
                rx.itemsRaw.split(";").filter { it.isNotBlank() }.mapNotNull { line ->
                    val parts = line.split(":")
                    if (parts.size >= 4) {
                        val id = parts[0]
                        val name = parts[1]
                        val qty = parts[2].toIntOrNull() ?: 1
                        val price = parts[3].toDoubleOrNull() ?: 0.0
                        val vSelected = parts.getOrNull(4) ?: ""
                        val disc = parts.getOrNull(5)?.toDoubleOrNull() ?: 0.0
                        val sat = parts.getOrNull(6).orEmpty().takeIf { it.isNotBlank() } ?: "Pcs"
                        CorrectionItemCashier(id, name, qty, price, vSelected, disc, sat)
                    } else null
                }.toMutableStateList()
            }

            val calculatedSub = parsedItems.sumOf { it.harga * it.jumlah }
            val calculatedDisc = parsedItems.sumOf { it.diskon * it.jumlah }
            val calculatedTot = (calculatedSub - calculatedDisc).coerceAtLeast(0.0)

            var editTotalStr by remember { mutableStateOf(calculatedTot.toInt().toString()) }
            var editBayarStr by remember { mutableStateOf(rx.bayarNominal.toInt().toString()) }
            var editDiskonStr by remember { mutableStateOf(calculatedDisc.toInt().toString()) }
            var editMetodeBayar by remember { mutableStateOf(rx.metodeBayar) }
            var editStatus by remember { mutableStateOf(rx.status) }

            LaunchedEffect(parsedItems.toList()) {
                editTotalStr = calculatedTot.toInt().toString()
                editDiskonStr = calculatedDisc.toInt().toString()
            }

            var showAddProductMenuComp by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showCorrectionEditDialog = false },
                title = { Text("Koreksi / Perbaikan Transaksi", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    ) {
                        Text("No TRX: ${rx.id}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OrangePrimary)
                        Text("Edit Produk Struk", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            if (parsedItems.isEmpty()) {
                                Text("Tidak ada produk di struk", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(10.dp))
                            } else {
                                parsedItems.forEachIndexed { idx, item ->
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
                                            IconButton(
                                                onClick = {
                                                    if (item.jumlah > 1) {
                                                        parsedItems[idx] = item.copy(jumlah = item.jumlah - 1)
                                                    } else {
                                                        parsedItems.removeAt(idx)
                                                    }
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.RemoveCircle, contentDescription = "Kurang", tint = OrangePrimary)
                                            }
                                            Text("${item.jumlah}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            IconButton(
                                                onClick = {
                                                    parsedItems[idx] = item.copy(jumlah = item.jumlah + 1)
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Tambah", tint = OrangePrimary)
                                            }
                                            IconButton(
                                                onClick = { parsedItems.removeAt(idx) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { showAddProductMenuComp = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tambah Produk ke Struk Baru", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

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

                        Text("Metode Pembayaran", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf("Tunai", "QRIS", "Transfer", "Debit").forEach { m ->
                                Card(
                                    onClick = { editMetodeBayar = m },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (editMetodeBayar == m) OrangePrimary else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(modifier = Modifier.padding(6.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text(m, fontSize = 9.sp, color = if (editMetodeBayar == m) Color.White else Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Text("Status Pembayaran", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf("lunas", "dp").forEach { s ->
                                Card(
                                    onClick = { editStatus = s },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (editStatus == s) OrangePrimary else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(modifier = Modifier.padding(6.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text(s.uppercase(), fontSize = 9.sp, color = if (editStatus == s) Color.White else Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val totalVal = editTotalStr.toDoubleOrNull() ?: calculatedTot
                            val bayarVal = editBayarStr.toDoubleOrNull() ?: rx.bayarNominal
                            val diskonVal = editDiskonStr.toDoubleOrNull() ?: calculatedDisc

                            if (bayarVal < totalVal && editStatus == "lunas") {
                                Toast.makeText(context, "Nominal bayar kurang dari total untuk status lunas!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (parsedItems.isEmpty()) {
                                Toast.makeText(context, "Struk tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val updatedItemsString = parsedItems.joinToString(";") {
                                "${it.id}:${it.nama}:${it.jumlah}:${it.harga}:${it.varianSelected}:${it.diskon}:${it.satuan}"
                            }

                            val updatedTx = rx.copy(
                                itemsRaw = updatedItemsString,
                                subtotal = calculatedSub,
                                total = totalVal,
                                bayarNominal = bayarVal,
                                diskonTotal = diskonVal,
                                metodeBayar = editMetodeBayar,
                                status = editStatus,
                                kembalian = (bayarVal - totalVal).coerceAtLeast(0.0)
                            )

                            viewModel.correctTransaction(updatedTx) {
                                showCorrectionEditDialog = false
                                selectedTxForReceipt = null
                                Toast.makeText(context, "Berhasil simpan koreksi transaksi!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Text("Simpan Koreksi")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCorrectionEditDialog = false }) { Text("Batal") }
                }
            )

            if (showAddProductMenuComp) {
                var searchProdQueryComp by remember { mutableStateOf("") }
                val filteredStoreProdsComp = allProducts.filter { it.nama.contains(searchProdQueryComp, ignoreCase = true) }

                AlertDialog(
                    onDismissRequest = { showAddProductMenuComp = false },
                    title = { Text("Pilih Produk", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 280.dp)) {
                            OutlinedTextField(
                                value = searchProdQueryComp,
                                onValueChange = { searchProdQueryComp = it },
                                label = { Text("Cari Produk...") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            androidx.compose.foundation.lazy.LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(filteredStoreProdsComp) { prod ->
                                    Card(
                                        onClick = {
                                            val existingIndex = parsedItems.indexOfFirst { it.id == prod.id && it.varianSelected.isEmpty() }
                                            if (existingIndex >= 0) {
                                                parsedItems[existingIndex] = parsedItems[existingIndex].copy(jumlah = parsedItems[existingIndex].jumlah + 1)
                                            } else {
                                                parsedItems.add(CorrectionItemCashier(
                                                    id = prod.id,
                                                    nama = prod.nama,
                                                    jumlah = 1,
                                                    harga = prod.hargaJual,
                                                    varianSelected = "",
                                                    diskon = 0.0,
                                                    satuan = prod.satuan
                                                ))
                                            }
                                            showAddProductMenuComp = false
                                        },
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(prod.nama, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                                Text("Stok: ${prod.stok}", fontSize = 9.sp, color = Color.Gray)
                                            }
                                            Text(idrFormatter.format(prod.hargaJual), fontWeight = FontWeight.Bold, color = OrangePrimary, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showAddProductMenuComp = false }) { Text("Batal") }
                    }
                )
            }
        }

        // Catat Pengeluaran Dialogue
        if (showAddExpenseDialog) {
            val ownerId = remember(user) {
                if (user?.role == "kasir" || user?.role == "kasir_invited") user?.ownerId else user?.uid
            }
            AlertDialog(
                onDismissRequest = {
                    showAddExpenseDialog = false
                    editExpenseNominal = ""
                    editExpenseKet = ""
                },
                title = { Text("Pencatatan Pengeluaran Kas", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Payments, contentDescription = null, tint = OrangePrimary) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Catat uang keluar / beban operasional toko selama shift saat ini.", fontSize = 11.sp, color = Color.Gray)
                        OutlinedTextField(
                            value = editExpenseNominal,
                            onValueChange = { editExpenseNominal = it.filter { c -> c.isDigit() } },
                            label = { Text("Nominal Pengeluaran (Rp) *") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editExpenseKet,
                            onValueChange = { editExpenseKet = it },
                            label = { Text("Keterangan Pengeluaran *") },
                            placeholder = { Text("Contoh: Listrik, Belanja Plastik") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amt = editExpenseNominal.toDoubleOrNull() ?: 0.0
                            if (amt <= 0 || editExpenseKet.isBlank()) {
                                Toast.makeText(context, "Nominal & keterangan wajib diisi!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.recordExpense(amt, editExpenseKet) { success, error ->
                                if (success) {
                                    showAddExpenseDialog = false
                                    editExpenseNominal = ""
                                    editExpenseKet = ""
                                    Toast.makeText(context, "Pengeluaran kas berhasil dicatatkan!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Gagal menyimpan pengeluaran: $error", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Text("Simpan Pengeluaran")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAddExpenseDialog = false
                            editExpenseNominal = ""
                            editExpenseKet = ""
                        }
                    ) {
                        Text("Batal")
                    }
                }
            )
        }
}

data class CorrectionItemCashier(
    val id: String,
    val nama: String,
    val jumlah: Int,
    val harga: Double,
    val varianSelected: String,
    val diskon: Double,
    val satuan: String
)
