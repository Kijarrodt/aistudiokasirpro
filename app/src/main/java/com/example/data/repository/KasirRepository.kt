package com.example.data.repository

import android.content.Context
import androidx.room.*
import com.example.data.local.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID

// Product Varian definition
data class ProductVariant(
    val nama: String,
    val harga: Double
) {
    override fun toString(): String = "$nama:$harga"
    companion object {
        fun fromString(str: String): ProductVariant {
            val parts = str.split(":")
            if (parts.size >= 2) {
                return ProductVariant(parts[0], parts[1].toDoubleOrNull() ?: 0.0)
            }
            return ProductVariant(str, 0.0)
        }
    }
}

// Transaction Item definition
data class TransactionItem(
    val id: String,
    val nama: String,
    val jumlah: Int,
    val harga: Double,
    val varianSelected: String?,
    val diskon: Double
) {
    fun subtotal(): Double = (harga - diskon) * jumlah
}

class KasirRepository(private val context: Context) {
    private val database = KasirDatabase.getDatabase(context)
    private val dao = database.kasirDao()

    private val prefs = context.getSharedPreferences("kasir_prefs", Context.MODE_PRIVATE)
    private val _loggedInUid = MutableStateFlow<String?>(prefs.getString("logged_in_uid", null))

    fun setLoggedInDeviceUser(uid: String?) {
        prefs.edit().putString("logged_in_uid", uid).apply()
        _loggedInUid.value = uid
    }

    init {
        // Fallback for existing sessions: if prefs has no uid but database has a user, save it as logged-in
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val extantUid = prefs.getString("logged_in_uid", null)
            if (extantUid == null) {
                val dbUser = dao.getCurrentUserRaw()
                if (dbUser != null) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        setLoggedInDeviceUser(dbUser.uid)
                    }
                }
            }
        }
    }

    // Active session states
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _language = MutableStateFlow("id") // "id" or "en"
    val language: StateFlow<String> = _language.asStateFlow()

    private val _lastBackupDate = MutableStateFlow<String?>("Belum pernah")
    val lastBackupDate: StateFlow<String?> = _lastBackupDate.asStateFlow()

    // Observable Flows from Room Db
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentUser: Flow<UserEntity?> = _loggedInUid.flatMapLatest { uid ->
        if (uid != null) {
            dao.getUserByIdFlow(uid)
        } else {
            flowOf(null)
        }
    }
    val allProducts: Flow<List<ProductEntity>> = dao.getAllProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = dao.getLowStockProducts()
    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
    val allDebts: Flow<List<DebtEntity>> = dao.getAllDebts()
    val allCustomers: Flow<List<CustomerEntity>> = dao.getAllCustomers()
    val stockHistory: Flow<List<StockHistoryEntity>> = dao.getStockHistory()
    val allPromos: Flow<List<PromoEntity>> = dao.getAllPromos()
    val allBranches: Flow<List<BranchEntity>> = dao.getAllBranches()
    val allCashiers: Flow<List<UserEntity>> = dao.getAllCashiers()

    fun toggleOnline() {
        _isOnline.value = !_isOnline.value
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    fun setLanguage(lang: String) {
        _language.value = lang
    }

    // AUTH ACTIONS (Simulated Firebase Auth + Sync)
    suspend fun registerUser(nama: String, email: String, pass: String): Boolean {
        val user = UserEntity(
            uid = UUID.randomUUID().toString(),
            nama = nama,
            email = email,
            role = "owner",
            ownerId = null,
            assignedBranchId = null,
            subscriptionStatus = "premium",
            subscriptionStartDate = System.currentTimeMillis(),
            subscriptionEndDate = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)
        )
        dao.clearUsers()
        dao.insertUser(user)
        setLoggedInDeviceUser(user.uid)
        return true
    }

    suspend fun loginUser(email: String, pass: String): Boolean {
        // Authenticate standard accounts or create a simulated one
        val role = if (email.contains("kasir")) "kasir" else "owner"
        val user = UserEntity(
            uid = if (role == "kasir") "kasir-uid" else "owner-uid",
            nama = if (role == "kasir") "Kasir Pratama" else "Owner Toko",
            email = email,
            role = role,
            ownerId = if (role == "kasir") "owner-uid" else null,
            assignedBranchId = if (role == "kasir") "branch-1" else null,
            subscriptionStatus = "premium",
            subscriptionStartDate = System.currentTimeMillis(),
            subscriptionEndDate = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)
        )
        dao.clearUsers()
        dao.insertUser(user)
        setLoggedInDeviceUser(user.uid)
        return true
    }

    suspend fun loginWithGoogle(): Boolean {
        val user = UserEntity(
            uid = "google-uid-123",
            nama = "Kiki Jarrodt",
            email = "kikijarrodt@gmail.com",
            role = "owner",
            ownerId = null,
            assignedBranchId = null,
            subscriptionStatus = "premium", // Premium privileges for our Google Account!
            subscriptionStartDate = System.currentTimeMillis(),
            subscriptionEndDate = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
        )
        dao.clearUsers()
        dao.insertUser(user)
        setLoggedInDeviceUser(user.uid)
        return true
    }

    suspend fun logout() {
        setLoggedInDeviceUser(null)
        dao.clearUsers()
    }

    suspend fun resetPassword(email: String): Boolean {
        // Simulated reset password
        return true
    }

    // STORE ONBOARDING ACTIONS
    suspend fun setupToko(namaToko: String, alamat: String, logoUrl: String?): Boolean {
        val business = BusinessEntity(
            id = UUID.randomUUID().toString(),
            ownerId = "owner-uid",
            namaBisnis = namaToko,
            logoUrl = logoUrl
        )
        dao.insertBusiness(business)

        // Generate primary default branch
        val branch = BranchEntity(
            id = "branch-1",
            businessId = business.id,
            namaCabang = "Cabang Utama",
            alamat = alamat,
            kasirIdsCsv = "kasir-uid"
        )
        dao.insertBranch(branch)
        return true
    }

    fun getCurrentBusiness(): Flow<BusinessEntity?> {
        return dao.getCurrentBusiness()
    }

    // PRODUCT ACTIONS
    suspend fun insertProduct(
        nama: String,
        kategori: String,
        hargaJual: Double,
        hargaModal: Double,
        stok: Int,
        stokMinimum: Int,
        barcode: String?,
        fotoUrl: String?,
        varianList: List<ProductVariant>
    ): Boolean {
        val varianString = varianList.joinToString(";") { "${it.nama}:${it.harga}" }
        val product = ProductEntity(
            id = UUID.randomUUID().toString(),
            businessId = "biz-default",
            branchId = "branch-1",
            nama = nama,
            kategori = kategori,
            hargaJual = hargaJual,
            hargaModal = hargaModal,
            stok = stok,
            stokMinimum = stokMinimum,
            barcode = barcode,
            fotoUrl = fotoUrl,
            varianRaw = varianString
        )
        dao.insertProduct(product)
        // Record stock history
        val hist = StockHistoryEntity(
            id = UUID.randomUUID().toString(),
            productId = product.id,
            businessId = "biz-default",
            tipe = "masuk",
            jumlah = stok,
            stokSebelum = 0,
            stokSesudah = stok,
            keterangan = "Stok awal produk baru"
        )
        dao.insertStockHistory(hist)
        return true
    }

    suspend fun updateProduct(product: ProductEntity) {
        dao.insertProduct(product)
    }

    suspend fun deleteProduct(id: String) {
        dao.deleteProduct(id)
    }

    // STOCK MANAGEMENT
    suspend fun recordStockMovement(
        productId: String,
        tipe: String, // "masuk", "keluar", "opname"
        jumlah: Int,
        keterangan: String?
    ) {
        val prod = dao.getProductById(productId) ?: return
        val stokSebelum = prod.stok
        val stokSesudah = when (tipe) {
            "masuk" -> stokSebelum + jumlah
            "keluar" -> stokSebelum - jumlah
            "opname" -> jumlah
            else -> stokSebelum
        }
        val updated = prod.copy(stok = stokSesudah)
        dao.insertProduct(updated)

        val hist = StockHistoryEntity(
            id = UUID.randomUUID().toString(),
            productId = productId,
            businessId = prod.businessId,
            tipe = tipe,
            jumlah = if (tipe == "opname") (stokSesudah - stokSebelum) else jumlah,
            stokSebelum = stokSebelum,
            stokSesudah = stokSesudah,
            keterangan = keterangan
        )
        dao.insertStockHistory(hist)
    }

    // TRANSACTION ACTIONS (Checkout)
    suspend fun checkout(
        items: List<TransactionItem>,
        subtotal: Double,
        diskonTotal: Double,
        kodePromo: String?,
        total: Double,
        metodeBayar: String,
        bayarNominal: Double,
        kembalian: Double,
        status: String, // "lunas" or "dp"
        pelangganId: String?
    ): TransactionEntity {
        // Build items serialized string
        val itemsString = items.joinToString(";") { 
            "${it.id}:${it.nama}:${it.jumlah}:${it.harga}:${it.varianSelected ?: ""}:${it.diskon}" 
        }

        // Fetch current active user to set cashier details and update last active timestamp
        val currentUser = dao.getCurrentUser().firstOrNull()
        var currentKasirId = "kasir-1"
        var currentKasirNama = "Kasir Pro"
        var currentBranchId = "branch-1"

        if (currentUser != null) {
            currentKasirId = currentUser.uid
            currentKasirNama = currentUser.nama
            currentBranchId = currentUser.assignedBranchId ?: "branch-1"
            
            // Log cashier last active timestamp
            if (currentUser.role == "kasir") {
                dao.insertUser(currentUser.copy(lastActiveAt = System.currentTimeMillis()))
            }
        }

        val tx = TransactionEntity(
            id = "TRX-${System.currentTimeMillis()}",
            businessId = "biz-default",
            branchId = currentBranchId,
            kasirId = currentKasirId,
            kasirNama = currentKasirNama,
            itemsRaw = itemsString,
            subtotal = subtotal,
            diskonTotal = diskonTotal,
            kodePromo = kodePromo,
            total = total,
            metodeBayar = metodeBayar,
            bayarNominal = bayarNominal,
            kembalian = kembalian,
            status = status,
            pelangganId = pelangganId,
            isOfflinePending = !_isOnline.value
        )

        dao.insertTransaction(tx)

        // Update product inventory & record stock movement
        items.forEach { item ->
            val product = dao.getProductById(item.id)
            if (product != null) {
                val stokSebelum = product.stok
                val stokSesudah = (stokSebelum - item.jumlah).coerceAtLeast(0)
                dao.insertProduct(product.copy(stok = stokSesudah))

                // Log stock movement
                dao.insertStockHistory(
                    StockHistoryEntity(
                        id = UUID.randomUUID().toString(),
                        productId = item.id,
                        businessId = product.businessId,
                        tipe = "keluar",
                        jumlah = item.jumlah,
                        stokSebelum = stokSebelum,
                        stokSesudah = stokSesudah,
                        keterangan = "Penjualan transaksi ${tx.id}"
                    )
                )
            }
        }

        // Handle points and totals for Loyalty Customer
        if (pelangganId != null) {
            val customersList = mutableListOf<CustomerEntity>()
            // Query & Update customer locally
            // For simple simulation, increment points directly
            val addedPoints = (total / 10000).toInt()
            // We update customer fields inside ViewModel or direct db search
        }

        // Create debt record if partial payment (DP)
        if (status == "dp" && pelangganId != null) {
            val sisaHutang = total - bayarNominal
            if (sisaHutang > 0) {
                dao.insertDebt(
                    DebtEntity(
                        id = UUID.randomUUID().toString(),
                        businessId = tx.businessId,
                        branchId = tx.branchId,
                        pelangganId = pelangganId,
                        pelangganNama = "Pelanggan Setia",
                        jumlah = sisaHutang,
                        transaksiId = tx.id,
                        status = "belum"
                    )
                )
            }
        }

        return tx
    }

    // PERSISTENCE SYNC FOR OFFLINE MODE
    suspend fun synchronizeOfflineData(): Int {
        val pending = dao.getOfflinePendingTransactions()
        pending.forEach { trx ->
            // Simulate API HTTP upload
            dao.markTransactionSynced(trx.id)
        }
        return pending.size
    }

    // MULTI BRANCH MANAGEMENT
    suspend fun addBranch(nama: String, alamat: String) {
        val branch = BranchEntity(
            id = UUID.randomUUID().toString(),
            businessId = "biz-default",
            namaCabang = nama,
            alamat = alamat,
            kasirIdsCsv = ""
        )
        dao.insertBranch(branch)
    }

    suspend fun updateBranch(id: String, nama: String, alamat: String) {
        dao.updateBranch(id, nama, alamat)
    }

    suspend fun deleteBranch(id: String) {
        dao.deleteBranch(id)
    }

    // KASIR MANAGEMENT
    suspend fun inviteKasir(email: String, ownerId: String) {
        val user = UserEntity(
            uid = "invited-" + UUID.randomUUID().toString(),
            nama = "Undangan Pending",
            email = email,
            role = "kasir_invited",
            ownerId = ownerId,
            assignedBranchId = null,
            subscriptionStatus = "free",
            subscriptionStartDate = null,
            subscriptionEndDate = null
        )
        dao.insertUser(user)
    }

    suspend fun registerInvitedCashier(uid: String, nama: String): Boolean {
        val user = dao.getUserById(uid) ?: return false
        val activeUser = user.copy(
            nama = nama,
            role = "kasir",
            lastActiveAt = System.currentTimeMillis()
        )
        dao.insertUser(activeUser)
        return true
    }

    suspend fun assignCashierToBranch(cashierId: String, branchId: String?) {
        val user = dao.getUserById(cashierId)
        if (user != null) {
            dao.insertUser(user.copy(assignedBranchId = branchId))
        }
    }

    suspend fun deleteCashier(uid: String) {
        dao.deleteUser(uid)
    }

    // CUSTOMERS
    suspend fun addCustomer(nama: String, nomorHp: String) {
        val cust = CustomerEntity(
            id = UUID.randomUUID().toString(),
            businessId = "biz-default",
            nama = nama,
            nomorHp = nomorHp,
            totalPoin = 0,
            totalTransaksi = 0
        )
        dao.insertCustomer(cust)
    }

    suspend fun updateCustomer(customer: CustomerEntity) {
        dao.insertCustomer(customer)
    }

    // DEBTS
    suspend fun payDebt(debtId: String) {
        dao.updateDebtStatus(debtId, "lunas")
    }

    // PROMOS
    suspend fun addPromo(
        nama: String,
        tipe: String, // "diskon_persen", "diskon_nominal"
        nilai: Double,
        minTransaksi: Double,
        kode: String,
        berlakuSampai: Long
    ) {
        val promo = PromoEntity(
            id = UUID.randomUUID().toString(),
            businessId = "biz-default",
            nama = nama,
            tipe = tipe,
            nilai = nilai,
            minTransaksi = minTransaksi,
            kode = kode,
            isActive = true,
            berlakuSampai = berlakuSampai
        )
        dao.insertPromo(promo)
    }

    suspend fun togglePromo(id: String, active: Boolean) {
        dao.updatePromoStatus(id, active)
    }

    // BACKUP
    suspend fun triggerBackup(): Boolean {
        _lastBackupDate.value = "Heri ini, ${getCurrentTimeString()}"
        return true
    }

    // SUBSCRIPTION & MIDTRANS SIMULATOR
    suspend fun processSubscription(packageName: String): Boolean {
        // Connect to simulated API or local db updates
        val u = dao.getCurrentUser()
        return true
    }

    private fun getCurrentTimeString(): String {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}
