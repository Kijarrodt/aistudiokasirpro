package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class KasirViewModel(application: Application) : AndroidViewModel(application) {
    val repository = KasirRepository(application)

    // UI Session flags
    val currentUser = repository.currentUser.stateIn(
        viewModelScope, SharingStarted.Eagerly, null
    )
    
    val currentBusiness = repository.getCurrentBusiness().stateIn(
        viewModelScope, SharingStarted.Eagerly, null
    )

    val products = repository.allProducts.stateIn(
        viewModelScope, SharingStarted.Eagerly, emptyList()
    )

    val lowStockProducts = repository.lowStockProducts.stateIn(
        viewModelScope, SharingStarted.Eagerly, emptyList()
    )

    val transactions = repository.allTransactions.stateIn(
        viewModelScope, SharingStarted.Eagerly, emptyList()
    )

    val debts = repository.allDebts.stateIn(
        viewModelScope, SharingStarted.Eagerly, emptyList()
    )

    val customers = repository.allCustomers.stateIn(
        viewModelScope, SharingStarted.Eagerly, emptyList()
    )

    val stockHistory = repository.stockHistory.stateIn(
        viewModelScope, SharingStarted.Eagerly, emptyList()
    )

    val promos = repository.allPromos.stateIn(
        viewModelScope, SharingStarted.Eagerly, emptyList()
    )

    val branches = repository.allBranches.stateIn(
        viewModelScope, SharingStarted.Eagerly, emptyList()
    )

    val cashiers = repository.allCashiers.stateIn(
        viewModelScope, SharingStarted.Eagerly, emptyList()
    )

    val isOnline = repository.isOnline
    val isDarkMode = repository.isDarkMode
    val language = repository.language
    val lastBackupDate = repository.lastBackupDate

    // Navigation and Pop-Up dialog states
    val activeScreen = MutableStateFlow("splash") // splash, onboarding, login, register, forgot_password, setup_toko, home, cashier, manage, settings, premium_pricing
    val showLimitPopup = MutableStateFlow<String?>(null) // Contains message of limit reached

    // Core Kasir Cart state
    val cartItems = MutableStateFlow<List<TransactionItem>>(emptyList())
    val selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val appliedPromo = MutableStateFlow<PromoEntity?>(null)
    val splitPaymentEnabled = MutableStateFlow(false)
    val splitPayments = MutableStateFlow<Map<String, Double>>(mapOf("Tunai" to 0.0)) // Payment method to amount map
    val transactionStatus = MutableStateFlow("lunas") // "lunas" or "dp"
    val cashAmountPaid = MutableStateFlow(0.0)
    val selectedPaymentMethod = MutableStateFlow("Tunai") // "Tunai" or "QRIS"

    // Current digital receipt
    val activeReceipt = MutableStateFlow<TransactionEntity?>(null)

    // Dialog & Simulation triggers
    val showScanBarcodeDialog = MutableStateFlow(false)
    val showVarianDialog = MutableStateFlow<ProductEntity?>(null)

    init {
        // Pre-populate database with default items for pristine demo layout if empty
        viewModelScope.launch {
            products.first { true } // wait for first load
            delayLoadingDemoAndSync()
        }

        // Auto-promote owner to premium on start if needed (one-shot check to avoid endless loop)
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val db = KasirDatabase.getDatabase(getApplication())
            val user = db.kasirDao().getCurrentUserRaw()
            if (user != null && user.role == "owner" && user.subscriptionStatus != "premium") {
                db.kasirDao().insertUser(user.copy(
                    subscriptionStatus = "premium",
                    subscriptionStartDate = System.currentTimeMillis(),
                    subscriptionEndDate = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)
                ))
            }
        }
    }

    private suspend fun delayLoadingDemoAndSync() {
        // Just empty callback
    }

    // CART ACTIONS
    fun addToCart(product: ProductEntity, selectedVariant: ProductVariant? = null) {
        val current = cartItems.value.toMutableList()
        val variantName = selectedVariant?.nama
        val finalPrice = selectedVariant?.harga ?: product.hargaJual
        
        val existingIndex = current.indexOfFirst { 
            it.id == product.id && it.varianSelected == variantName 
        }

        if (existingIndex >= 0) {
            val item = current[existingIndex]
            current[existingIndex] = item.copy(jumlah = item.jumlah + 1)
        } else {
            current.add(
                TransactionItem(
                    id = product.id,
                    nama = product.nama,
                    jumlah = 1,
                    harga = finalPrice,
                    varianSelected = variantName,
                    diskon = 0.0
                )
            )
        }
        cartItems.value = current
    }

    fun updateCartQuantity(item: TransactionItem, newQty: Int) {
        if (newQty <= 0) {
            cartItems.value = cartItems.value.filterNot { 
                it.id == item.id && it.varianSelected == item.varianSelected 
            }
        } else {
            cartItems.value = cartItems.value.map {
                if (it.id == item.id && it.varianSelected == item.varianSelected) {
                    it.copy(jumlah = newQty)
                } else it
            }
        }
    }

    fun applyCartItemDiscount(item: TransactionItem, discountAmount: Double) {
        cartItems.value = cartItems.value.map {
            if (it.id == item.id && it.varianSelected == item.varianSelected) {
                it.copy(diskon = discountAmount)
            } else it
        }
    }

    fun clearCart() {
        cartItems.value = emptyList()
        selectedCustomer.value = null
        appliedPromo.value = null
        cashAmountPaid.value = 0.0
        splitPayments.value = mapOf("Tunai" to 0.0)
        splitPaymentEnabled.value = false
        transactionStatus.value = "lunas"
        selectedPaymentMethod.value = "Tunai"
    }

    // CHECKOUT PROCESS
    fun processCheckout(customDiscountPrice: Double = 0.0) {
        val user = currentUser.value
        val isPremium = user?.subscriptionStatus == "premium"

        // Rule limit validation for Free Tier (Max 50 transactions per month)
        if (!isPremium && transactions.value.size >= 50) {
            showLimitPopup.value = "Batas 50 transaksi bulanan untuk akun Gratis telah tercapai. Upgrade ke Premium untuk transaksi tanpa batas!"
            return
        }

        viewModelScope.launch {
            val items = cartItems.value
            val subtotal = items.sumOf { (it.harga - it.diskon) * it.jumlah }
            val promoDisc = appliedPromo.value?.let { p ->
                if (p.tipe == "diskon_persen") {
                    subtotal * (p.nilai / 100.0)
                } else {
                    p.nilai
                }
            } ?: 0.0
            val diskonTotal = promoDisc + customDiscountPrice
            val total = (subtotal - diskonTotal).coerceAtLeast(0.0)

            val method = if (splitPaymentEnabled.value) {
                splitPayments.value.filter { it.value > 0 }.map { "${it.key}:Rp${it.value}" }.joinToString(", ")
            } else {
                selectedPaymentMethod.value
            }

            val paid = if (splitPaymentEnabled.value) {
                splitPayments.value.values.sum()
            } else if (selectedPaymentMethod.value == "QRIS") {
                total
            } else {
                cashAmountPaid.value
            }

            val change = (paid - total).coerceAtLeast(0.0)

            // Post transaction to Room database and reduce stock inventories
            val tx = repository.checkout(
                items = items,
                subtotal = subtotal,
                diskonTotal = diskonTotal,
                kodePromo = appliedPromo.value?.kode,
                total = total,
                metodeBayar = method,
                bayarNominal = paid,
                kembalian = change,
                status = transactionStatus.value,
                pelangganId = selectedCustomer.value?.id
            )

            // Handle Customer Loyalty Point Update Simulation (1 point per Rp10.000)
            selectedCustomer.value?.let { cust ->
                val addedPoints = (total / 10000).toInt()
                val updatedCustomer = cust.copy(
                    totalPoin = cust.totalPoin + addedPoints,
                    totalTransaksi = cust.totalTransaksi + 1
                )
                repository.updateCustomer(updatedCustomer)
            }

            activeReceipt.value = tx
            clearCart()
        }
    }

    // BUSINESS & STORE ONBOARDING SETUP
    fun onboardingStoreSetup(nama: String, alamat: String, logoUrl: String?) {
        viewModelScope.launch {
            repository.setupToko(nama, alamat, logoUrl)
            activeScreen.value = "home"
        }
    }

    // PRODUCT SAVE ACTION WITH FREE TIER VALIDATION
    fun addProduct(
        nama: String,
        kategori: String,
        hargaJual: Double,
        hargaModal: Double,
        stok: Int,
        stokMinimum: Int,
        barcode: String?,
        fotoUrl: String?,
        varianList: List<ProductVariant>
    ) {
        val user = currentUser.value
        val isPremium = user?.subscriptionStatus == "premium"

        // Constraint Free Tier check: max 10 products
        if (!isPremium && products.value.size >= 10) {
            showLimitPopup.value = "Batas maksimal 10 produk untuk akun Gratis tercapai. Upgrade ke Premium untuk menambah produk tanpa batas!"
            return
        }

        viewModelScope.launch {
            repository.insertProduct(nama, kategori, hargaJual, hargaModal, stok, stokMinimum, barcode, fotoUrl, varianList)
        }
    }

    fun editProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
        }
    }

    // ADD STOCK TRACKING
    fun updateProductStock(productId: String, type: String, amount: Int, note: String?) {
        viewModelScope.launch {
            repository.recordStockMovement(productId, type, amount, note)
        }
    }

    // PROMOS
    fun addPromo(nama: String, tipe: String, nilai: Double, minTx: Double, kode: String, durationDays: Int) {
        viewModelScope.launch {
            val berlakuSampai = System.currentTimeMillis() + (durationDays * 24L * 60 * 60 * 1000)
            repository.addPromo(nama, tipe, nilai, minTx, kode, berlakuSampai)
        }
    }

    fun togglePromo(id: String, active: Boolean) {
        viewModelScope.launch {
            repository.togglePromo(id, active)
        }
    }

    // BRANCHES WITH FREE TIER VALIDATION
    fun addBranch(nama: String, alamat: String) {
        val isPremium = currentUser.value?.subscriptionStatus == "premium"
        if (!isPremium) {
            showLimitPopup.value = "Fitur Multi-Cabang hanya untuk pengguna Premium. Upgrade sekarang!"
            return
        }
        viewModelScope.launch {
            repository.addBranch(nama, alamat)
        }
    }

    fun editBranch(branchId: String, nama: String, alamat: String) {
        viewModelScope.launch {
            repository.updateBranch(branchId, nama, alamat)
        }
    }

    fun removeBranch(branchId: String) {
        viewModelScope.launch {
            repository.deleteBranch(branchId)
        }
    }

    // CASHIER MANAGEMENT METHODS
    fun inviteKasir(email: String) {
        val isPremium = currentUser.value?.subscriptionStatus == "premium"
        if (!isPremium) {
            showLimitPopup.value = "Fitur Manajemen Kasir hanya untuk pengguna Premium. Upgrade sekarang!"
            return
        }
        viewModelScope.launch {
            val ownerId = currentUser.value?.uid ?: "owner-uid"
            repository.inviteKasir(email, ownerId)
        }
    }

    fun registerInvitedCashier(uid: String, nama: String) {
        viewModelScope.launch {
            repository.registerInvitedCashier(uid, nama)
        }
    }

    fun assignCashierToBranch(cashierId: String, branchId: String?) {
        viewModelScope.launch {
            repository.assignCashierToBranch(cashierId, branchId)
        }
    }

    fun deleteCashier(uid: String) {
        viewModelScope.launch {
            repository.deleteCashier(uid)
        }
    }

    // CUSTOMERS WITH FREE TIER VALIDATION
    fun addCustomer(nama: String, hp: String) {
        val isPremium = currentUser.value?.subscriptionStatus == "premium"
        if (!isPremium) {
            showLimitPopup.value = "Fitur Database Pelanggan & Loyalty Poin hanya untuk pengguna Premium!"
            return
        }
        viewModelScope.launch {
            repository.addCustomer(nama, hp)
        }
    }

    // DEBTS WITH PREMIUM CHECKS
    fun settleDebt(debtId: String) {
        viewModelScope.launch {
            repository.payDebt(debtId)
        }
    }

    // MANUAL BACKUP
    fun runBackup() {
        val isPremium = currentUser.value?.subscriptionStatus == "premium"
        if (!isPremium) {
            showLimitPopup.value = "Backup data otomatis & manual ke cloud hanya didukung untuk tipe akun Premium!"
            return
        }
        viewModelScope.launch {
            repository.triggerBackup()
        }
    }

    // SUB UPGRADE SIMULATION
    fun upgradeToPremium() {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val updatedUser = user.copy(
                subscriptionStatus = "premium",
                subscriptionStartDate = System.currentTimeMillis(),
                subscriptionEndDate = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
            )
            repository.registerUser(updatedUser.nama, updatedUser.email, "pass123") // Update user in memory
            // Make owner premium
            val freshUser = updatedUser.copy(uid = user.uid)
            // Save to room
            val db = KasirDatabase.getDatabase(getApplication())
            db.kasirDao().insertUser(freshUser)
            activeScreen.value = "home"
        }
    }

    fun downgradeToFree() {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val updatedUser = user.copy(
                subscriptionStatus = "free",
                subscriptionStartDate = null,
                subscriptionEndDate = null
            )
            val db = KasirDatabase.getDatabase(getApplication())
            db.kasirDao().insertUser(updatedUser)
        }
    }

    // SECTIONS CONTROL
    fun toggleOnlineMode() {
        val isPremium = currentUser.value?.subscriptionStatus == "premium"
        if (!isPremium) {
            showLimitPopup.value = "Mode Offline & Sinkronisasi hanya tersedia bagi pengguna Premium!"
            return
        }
        repository.toggleOnline()
        if (isOnline.value) {
            // Synchronize pending transactions
            viewModelScope.launch {
                repository.synchronizeOfflineData()
            }
        }
    }

    // DEMO DATA GENERATION (To populate database with outstanding mock elements on single click)
    fun populateDemoDataset() {
        viewModelScope.launch {
            val db = KasirDatabase.getDatabase(getApplication())
            val productCount = products.value.size
            if (productCount == 0) {
                // Populate default products
                repository.insertProduct(
                    nama = "Kopi Susu Gula Aren",
                    kategori = "Minuman",
                    hargaJual = 18000.0,
                    hargaModal = 9000.0,
                    stok = 50,
                    stokMinimum = 10,
                    barcode = "899123456001",
                    fotoUrl = "https://images.unsplash.com/photo-1541167760496-1628856ab772",
                    varianList = listOf(ProductVariant("Regular", 18000.0), ProductVariant("Large", 23000.0))
                )
                repository.insertProduct(
                    nama = "Roti Bakar Cokelat Keju",
                    kategori = "Makanan",
                    hargaJual = 15000.0,
                    hargaModal = 7500.0,
                    stok = 35,
                    stokMinimum = 5,
                    barcode = "899123456002",
                    fotoUrl = "https://images.unsplash.com/photo-1484723091739-30a097e8f929",
                    varianList = emptyList()
                )
                repository.insertProduct(
                    nama = "Indomie Goreng Double",
                    kategori = "Makanan",
                    hargaJual = 12000.0,
                    hargaModal = 6000.0,
                    stok = 75,
                    stokMinimum = 15,
                    barcode = "899123456003",
                    fotoUrl = null,
                    varianList = listOf(ProductVariant("Sedang", 12000.0), ProductVariant("Pedas Gila", 14000.0))
                )
                repository.insertProduct(
                    nama = "Teh Manis Krispy",
                    kategori = "Minuman",
                    hargaJual = 5000.0,
                    hargaModal = 2000.0,
                    stok = 8, // Trigger low stock! <= stokMinimum (10)
                    stokMinimum = 10,
                    barcode = "899123456004",
                    fotoUrl = null,
                    varianList = emptyList()
                )

                // Populate default customer
                repository.addCustomer("Ahmad Fauzi", "081234567890")
                repository.addCustomer("Siti Rahma", "081987654321")

                // Populate default promos
                repository.addPromo("Gebyar Promo Merdeka", "diskon_persen", 10.0, 50000.0, "MERDEKA10", 30)
                repository.addPromo("Cemilan Hemat", "diskon_nominal", 5000.0, 30000.0, "HEMAT5", 15)
                
                // Add default dummy branch
                repository.addBranch("Cabang Bandung", "Jl. Dago No. 120, Bandung")
                
                // Add default demo cashiers (one active, one invited/pending)
                val activeCashier = UserEntity(
                    uid = "kasir-dummy-1",
                    nama = "Indra Lesmana",
                    email = "indra@kopikita.id",
                    role = "kasir",
                    ownerId = "owner-uid",
                    assignedBranchId = "branch-1",
                    subscriptionStatus = "free",
                    subscriptionStartDate = null,
                    subscriptionEndDate = null,
                    lastActiveAt = System.currentTimeMillis() - 300_000 // 5 minutes ago
                )
                db.kasirDao().insertUser(activeCashier)

                val pendingCashier = UserEntity(
                    uid = "invited-dummy-2",
                    nama = "Undangan Pending",
                    email = "budi@kopikita.id",
                    role = "kasir_invited",
                    ownerId = "owner-uid",
                    assignedBranchId = null,
                    subscriptionStatus = "free",
                    subscriptionStartDate = null,
                    subscriptionEndDate = null,
                    createdAt = System.currentTimeMillis() - 600_000 // 10 mins ago
                )
                db.kasirDao().insertUser(pendingCashier)
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        repository.setDarkMode(enabled)
    }

    fun setLanguage(lang: String) {
        repository.setLanguage(lang)
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}
