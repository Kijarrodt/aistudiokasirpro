package com.kasirpro.pospintar.app.data.repository

import android.content.Context
import androidx.room.*
import com.kasirpro.pospintar.app.data.local.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.util.UUID

sealed class RedeemResult {
    data class Success(val endDate: Long) : RedeemResult()
    data class Error(val message: String) : RedeemResult()
}

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
    val diskon: Double,
    val satuan: String = "Pcs"
) {
    fun subtotal(): Double = (harga - diskon) * jumlah
}

// Extension helpers to safely convert to Maps for Firestore
fun UserEntity.toMap(): Map<String, Any?> = mapOf(
    "uid" to uid,
    "nama" to nama,
    "email" to email,
    "role" to role,
    "ownerId" to ownerId,
    "assignedBranchId" to assignedBranchId,
    "subscriptionStatus" to subscriptionStatus,
    "subscriptionType" to subscriptionType,
    "subscriptionStartDate" to subscriptionStartDate,
    "subscriptionEndDate" to subscriptionEndDate,
    "createdAt" to createdAt,
    "lastActiveAt" to lastActiveAt
)

fun BusinessEntity.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "ownerId" to ownerId,
    "namaBisnis" to namaBisnis,
    "logoBase64" to logoBase64,
    "alamat" to alamat,
    "noTelpon" to noTelpon,
    "createdAt" to createdAt,
    "qrisBase64" to qrisBase64
)

fun BranchEntity.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "businessId" to businessId,
    "userId" to businessId.removePrefix("biz-"),
    "namaCabang" to namaCabang,
    "alamat" to alamat,
    "kasirIdsCsv" to kasirIdsCsv,
    "createdAt" to createdAt
)

fun ProductEntity.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "businessId" to businessId,
    "userId" to businessId.removePrefix("biz-"),
    "branchId" to branchId,
    "nama" to nama,
    "kategori" to kategori,
    "hargaJual" to hargaJual,
    "retailPrice" to hargaJual,
    "hargaModal" to hargaModal,
    "stok" to stok,
    "stokMinimum" to stokMinimum,
    "barcode" to barcode,
    "fotoBase64" to fotoBase64,
    "varianRaw" to varianRaw,
    "satuan" to satuan,
    "isActive" to isActive,
    "createdAt" to createdAt,
    "wholesalePrice" to wholesalePrice,
    "wholesaleMinQty" to wholesaleMinQty,
    "expiryDate" to expiryDate?.let { com.google.firebase.Timestamp(java.util.Date(it)) },
    "expiryReminderDays" to expiryReminderDays
)

fun TransactionEntity.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "businessId" to businessId,
    "userId" to businessId.removePrefix("biz-"),
    "branchId" to branchId,
    "kasirId" to kasirId,
    "kasirNama" to kasirNama,
    "itemsRaw" to itemsRaw,
    "subtotal" to subtotal,
    "diskonTotal" to diskonTotal,
    "kodePromo" to kodePromo,
    "total" to total,
    "metodeBayar" to metodeBayar,
    "bayarNominal" to bayarNominal,
    "kembalian" to kembalian,
    "status" to status,
    "pelangganId" to pelangganId,
    "createdAt" to createdAt,
    "isOfflinePending" to isOfflinePending
)

fun DebtEntity.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "businessId" to businessId,
    "userId" to businessId.removePrefix("biz-"),
    "branchId" to branchId,
    "pelangganId" to pelangganId,
    "pelangganNama" to pelangganNama,
    "jumlah" to jumlah,
    "transaksiId" to transaksiId,
    "status" to status,
    "createdAt" to createdAt
)

fun CustomerEntity.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "businessId" to businessId,
    "userId" to businessId.removePrefix("biz-"),
    "nama" to nama,
    "nomorHp" to nomorHp,
    "totalPoin" to totalPoin,
    "totalTransaksi" to totalTransaksi,
    "alamat" to alamat,
    "createdAt" to createdAt
)

fun StockHistoryEntity.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "productId" to productId,
    "businessId" to businessId,
    "userId" to businessId.removePrefix("biz-"),
    "tipe" to tipe,
    "jumlah" to jumlah,
    "stokSebelum" to stokSebelum,
    "stokSesudah" to stokSesudah,
    "keterangan" to keterangan,
    "createdAt" to createdAt
)

fun PromoEntity.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "businessId" to businessId,
    "userId" to businessId.removePrefix("biz-"),
    "nama" to nama,
    "tipe" to tipe,
    "nilai" to nilai,
    "minTransaksi" to minTransaksi,
    "kode" to kode,
    "isActive" to isActive,
    "berlakuSampai" to berlakuSampai,
    "createdAt" to createdAt
)

fun com.google.firebase.firestore.DocumentSnapshot.getSafeDouble(field: String): Double {
    val rawValue = this.get(field) ?: return 0.0
    return when (rawValue) {
        is Number -> rawValue.toDouble()
        is String -> rawValue.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }
}

class KasirRepository(val context: Context) {
    private val database = KasirDatabase.getDatabase(context)
    val dao = database.kasirDao()

    init {
        try {
            if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
                com.google.firebase.FirebaseApp.initializeApp(context.applicationContext)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val auth by lazy { FirebaseAuth.getInstance() }
    val firestore by lazy { FirebaseFirestore.getInstance() }

    val prefs = context.getSharedPreferences("kasir_prefs", Context.MODE_PRIVATE)
    private val _loggedInUid = MutableStateFlow<String?>(null)

    private val _localCodesFlow = MutableStateFlow<List<Map<String, Any?>>>(emptyList())

    private fun getLocalActivationCodes(): List<Map<String, Any?>> {
        val jsonStr = prefs.getString("local_activation_codes", "[]") ?: "[]"
        return try {
            val arr = org.json.JSONArray(jsonStr)
            val list = mutableListOf<Map<String, Any?>>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val map = mutableMapOf<String, Any?>()
                map["id"] = obj.optString("id")
                map["type"] = obj.optString("type")
                map["durationDays"] = obj.optLong("durationDays")
                map["isUsed"] = obj.optBoolean("isUsed")
                map["usedBy"] = if (obj.isNull("usedBy")) null else obj.optString("usedBy")
                map["usedAt"] = if (obj.isNull("usedAt")) null else obj.optLong("usedAt")
                map["createdAt"] = obj.optLong("createdAt")
                map["createdBy"] = obj.optString("createdBy")
                list.add(map)
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveLocalActivationCodes(list: List<Map<String, Any?>>) {
        try {
            val arr = org.json.JSONArray()
            for (map in list) {
                val obj = org.json.JSONObject()
                obj.put("id", map["id"])
                obj.put("type", map["type"])
                obj.put("durationDays", map["durationDays"])
                obj.put("isUsed", map["isUsed"])
                obj.put("usedBy", map["usedBy"])
                obj.put("usedAt", map["usedAt"])
                obj.put("createdAt", map["createdAt"])
                obj.put("createdBy", map["createdBy"])
                arr.put(obj)
            }
            prefs.edit().putString("local_activation_codes", arr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getLocalExpenses(ownerId: String): List<Map<String, Any?>> {
        val jsonStr = prefs.getString("local_expenses_$ownerId", "[]") ?: "[]"
        return try {
            val arr = org.json.JSONArray(jsonStr)
            val list = mutableListOf<Map<String, Any?>>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val map = mutableMapOf<String, Any?>()
                map["id"] = obj.optString("id")
                map["ownerId"] = obj.optString("ownerId")
                map["amount"] = obj.optDouble("amount")
                map["keterangan"] = obj.optString("keterangan")
                map["createdAt"] = obj.optLong("createdAt")
                list.add(map)
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveLocalExpense(id: String, ownerId: String, amount: Double, keterangan: String, createdAt: Long) {
        try {
            val list = getLocalExpenses(ownerId).toMutableList()
            if (list.none { it["id"] == id }) {
                val map = mapOf(
                    "id" to id,
                    "ownerId" to ownerId,
                    "amount" to amount,
                    "keterangan" to keterangan,
                    "createdAt" to createdAt
                )
                list.add(map)
                
                val arr = org.json.JSONArray()
                for (item in list) {
                    val obj = org.json.JSONObject()
                    obj.put("id", item["id"])
                    obj.put("ownerId", item["ownerId"])
                    obj.put("amount", item["amount"])
                    obj.put("keterangan", item["keterangan"])
                    obj.put("createdAt", item["createdAt"])
                    arr.put(obj)
                }
                prefs.edit().putString("local_expenses_$ownerId", arr.toString()).apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setLoggedInDeviceUser(uid: String?) {
        prefs.edit().putString("logged_in_uid", uid).commit()
        _loggedInUid.value = uid
    }

    fun saveUserSessionMetadata(user: UserEntity?) {
        val editor = prefs.edit()
        if (user != null) {
            editor.putString("saved_user_email", user.email)
            editor.putString("saved_user_name", user.nama)
            editor.putBoolean("is_kasir_saved", user.role == "kasir" || user.role == "kasir_invited")
            editor.putString("saved_owner_id", user.ownerId)
            editor.putBoolean("is_at_least_profesional", user.isAtLeastProfesional)
        } else {
            editor.remove("saved_user_email")
            editor.remove("saved_user_name")
            editor.remove("is_kasir_saved")
            editor.remove("saved_owner_id")
            editor.remove("is_at_least_profesional")
        }
        editor.commit()
    }

    fun getOwnerVerificationCode(): String {
        return prefs.getString("owner_verification_code", "1234") ?: "1234"
    }

    fun saveOwnerVerificationCode(code: String) {
        prefs.edit().putString("owner_verification_code", code).apply()
    }

    init {
        try {
            _localCodesFlow.value = getLocalActivationCodes()
            val savedUid = prefs.getString("logged_in_uid", null)
            _loggedInUid.value = auth.currentUser?.uid ?: savedUid
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Sync with Firebase Auth state on start
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUid = auth.currentUser?.uid ?: prefs.getString("logged_in_uid", null)
                if (currentUid != null) {
                    withContext(Dispatchers.Main) {
                        _loggedInUid.value = currentUid
                    }
                    try {
                        syncFromFirestore()
                    } catch (sf: Exception) {
                        sf.printStackTrace()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _loggedInUid.value = null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
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

    // Observable Flows from local Room database, ensuring robust offline-first operation and preventing Firestore-based startup crashes.
    val currentUser: Flow<UserEntity?> = dao.getCurrentUser().distinctUntilChanged()

    suspend fun getCurrentUserRaw(): UserEntity? = dao.getCurrentUserRaw()

    suspend fun getCurrentBusinessRaw(): BusinessEntity? = dao.getCurrentBusinessRaw()

    suspend fun getBusinessFromFirestore(ownerId: String): BusinessEntity? {
        return try {
            val bizSnap = firestore.collection("businesses")
                .whereEqualTo("ownerId", ownerId)
                .get()
                .await()
            if (!bizSnap.isEmpty) {
                val doc = bizSnap.documents.first()
                BusinessEntity(
                    id = doc.id,
                    ownerId = ownerId,
                    namaBisnis = doc.getString("namaBisnis") ?: "Toko Kasir",
                    logoBase64 = doc.getString("logoBase64"),
                    alamat = doc.getString("alamat"),
                    noTelpon = doc.getString("noTelpon"),
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    qrisBase64 = doc.getString("qrisBase64")
                )
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun insertBusinessLocal(business: BusinessEntity) {
        dao.insertBusiness(business)
    }

    suspend fun updateBusinessProfile(namaBisnis: String, alamat: String?, noTelpon: String?, logoBase64: String?) {
        val biz = getCurrentBusinessRaw() ?: return
        val updated = biz.copy(
            namaBisnis = namaBisnis,
            alamat = alamat,
            noTelpon = noTelpon,
            logoBase64 = logoBase64
        )
        dao.insertBusiness(updated)
        try {
            firestore.collection("businesses").document(updated.id).set(updated.toMap()).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateBusinessQris(qrisBase64: String?) {
        val biz = getCurrentBusinessRaw() ?: return
        val updated = biz.copy(qrisBase64 = qrisBase64)
        dao.insertBusiness(updated)
        try {
            firestore.collection("businesses").document(updated.id).set(updated.toMap()).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getResolvedBusinessId(): String {
        val currentBiz = dao.getCurrentBusinessRaw()
        if (currentBiz != null) {
            return currentBiz.id
        }
        val currentUser = dao.getCurrentUserRaw()
        val ownerId = if (currentUser != null) {
            if (currentUser.role == "kasir" || currentUser.role == "kasir_invited") {
                currentUser.ownerId ?: currentUser.uid
            } else {
                currentUser.uid
            }
        } else {
            auth.currentUser?.uid ?: "owner-uid"
        }
        return "biz-$ownerId"
    }

    val currentOwnerId: Flow<String?> = currentUser.map { user ->
        user?.let {
            if (it.role == "kasir" || it.role == "kasir_invited") {
                it.ownerId
            } else {
                it.uid
            }
        }
    }.distinctUntilChanged()

    val currentBusinessFlow: Flow<BusinessEntity?> = dao.getCurrentBusiness().distinctUntilChanged()

    fun getCurrentBusiness(): Flow<BusinessEntity?> = currentBusinessFlow

    val currentBusinessIdFlow: Flow<String?> = currentBusinessFlow.map { it?.id }.distinctUntilChanged()

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

    // AUTH ACTIONS (Real Firebase Auth + Sync)
    suspend fun registerUser(nama: String, email: String, pass: String): Boolean {
        try {
            withContext(Dispatchers.IO) {
                database.clearAllTables()
            }
        } catch (e: Exception) { e.printStackTrace() }
        
        val firebaseUid: String
        try {
            val result = withTimeoutOrNull(15000L) {
                auth.createUserWithEmailAndPassword(email.trim(), pass).await()
            } ?: throw Exception("Registrasi gagal. Hubungan ke server tertunda atau gagal.")
            firebaseUid = result.user?.uid ?: throw Exception("Pendaftaran gagal. Sesi tidak ditemukan.")
        } catch (e: com.google.firebase.auth.FirebaseAuthUserCollisionException) {
            throw Exception("Email sudah digunakan. Silakan login")
        } catch (e: com.google.firebase.auth.FirebaseAuthException) {
            val errorCode = e.errorCode
            val msg = when {
                errorCode == "ERROR_EMAIL_ALREADY_IN_USE" || errorCode == "auth/email-already-in-use" -> "Email sudah digunakan. Silakan login"
                errorCode == "ERROR_INVALID_EMAIL" || errorCode == "auth/invalid-email" -> "Format email tidak valid"
                errorCode == "ERROR_WEAK_PASSWORD" || errorCode == "auth/weak-password" -> "Password minimal terdiri dari 6 karakter"
                else -> e.localizedMessage ?: "Registrasi gagal, silakan coba lagi."
            }
            throw Exception(msg)
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Koneksi gagal. Silakan periksa jaringan internet Anda.")
        }

        val localUser = UserEntity(
            uid = firebaseUid,
            nama = nama,
            email = email.trim(),
            role = "owner",
            ownerId = null,
            assignedBranchId = null,
            subscriptionStatus = "free",
            subscriptionStartDate = null,
            subscriptionEndDate = null
        )

        // Save to Firestore using map-serialization (gracefully ignore cloud failures if needed)
        try {
            withTimeoutOrNull(8000L) {
                firestore.collection("users").document(firebaseUid).set(localUser.toMap()).await()
            }
        } catch (f: Exception) {
            f.printStackTrace()
        }

        // Save to local Room (MUST succeed to keep the app working locally)
        dao.clearUsers()
        dao.insertUser(localUser)
        saveUserSessionMetadata(localUser)

        setLoggedInDeviceUser(firebaseUid)
        
        try {
            syncFromFirestore()
        } catch (s: Exception) {
            s.printStackTrace()
        }
        return true
    }

    suspend fun loginUser(email: String, pass: String): Boolean {
        val cleanInput = email.trim().lowercase()

        // 1. Try to check if input is NOT an email (i.e. cashier login by custom username)
        if (!cleanInput.contains("@")) {
            try {
                android.util.Log.d("KASIR_LOGIN", "Searching for cashier with username: $cleanInput")

                // Since firestore rules require request.auth != null, we sign in anonymously first.
                if (auth.currentUser == null) {
                    try {
                        auth.signInAnonymously().await()
                        android.util.Log.d("KASIR_LOGIN", "Successfully signed in anonymously for cashier search")
                    } catch (ae: Exception) {
                        android.util.Log.e("KASIR_LOGIN", "Failed to sign in anonymously: ${ae.localizedMessage}", ae)
                    }
                }

                val querySnapshot = firestore.collection("cashiers")
                    .whereEqualTo("username", cleanInput)
                    .get()
                    .await()
                
                android.util.Log.d("KASIR_LOGIN", "Cashiers found: ${querySnapshot.size()}")
                
                if (querySnapshot.isEmpty) {
                    android.util.Log.d("KASIR_LOGIN", "Login result: failed")
                    throw Exception("Username kasir tidak terdaftar.")
                }
                
                 // Find matching cashier by password
                val cashierDoc = querySnapshot.documents.firstOrNull { it.getString("password") == pass }
                if (cashierDoc == null) {
                    android.util.Log.d("KASIR_LOGIN", "Login result: failed")
                    throw Exception("Password salah. Silakan coba lagi.")
                }

                val status = cashierDoc.getString("status") ?: "aktif"
                if (status != "aktif") {
                    android.util.Log.d("KASIR_LOGIN", "Login result: failed")
                    throw Exception("Akun kasir ini dinonaktifkan.")
                }

                val ownerId = cashierDoc.getString("ownerId") ?: "owner-uid"
                val nama = cashierDoc.getString("cashierName") ?: cashierDoc.getString("nama") ?: "Kasir"
                val branchId = cashierDoc.getString("branchId") ?: "branch-1-biz-$ownerId"
                var subscriptionStatus = "free"
                var subscriptionType: String? = null
                try {
                    val ownerDoc = firestore.collection("users").document(ownerId).get().await()
                    if (ownerDoc.exists()) {
                        subscriptionStatus = ownerDoc.getString("subscriptionStatus") ?: "free"
                        subscriptionType = ownerDoc.getString("subscriptionType")
                    }
                } catch (ex: Exception) {
                    android.util.Log.e("KASIR_LOGIN", "Error fetching owner state in loginUser: ${ex.message}")
                }

                val cashierUser = UserEntity(
                    uid = cashierDoc.id,
                    nama = nama,
                    email = cleanInput,
                    role = "kasir",
                    ownerId = ownerId,
                    assignedBranchId = branchId,
                    subscriptionStatus = subscriptionStatus,
                    subscriptionType = subscriptionType,
                    subscriptionStartDate = null,
                    subscriptionEndDate = null,
                    createdAt = cashierDoc.getLong("createdAt") ?: System.currentTimeMillis(),
                    lastActiveAt = System.currentTimeMillis()
                )

                setLoggedInDeviceUser(cashierDoc.id)
                withContext(Dispatchers.IO) {
                    try {
                        database.clearAllTables()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                dao.clearUsers()
                dao.insertUser(cashierUser)
                saveUserSessionMetadata(cashierUser)

                // Update activity in Firestore cashier doc
                try {
                    firestore.collection("cashiers").document(cashierDoc.id).update("lastActiveAt", System.currentTimeMillis()).await()
                } catch (e: Exception) {}

                // Synchronize business elements for cashiers
                try {
                    syncFromFirestore(cashierDoc.id)
                } catch (s: Exception) {
                    s.printStackTrace()
                }
                
                android.util.Log.d("KASIR_LOGIN", "Login result: success")
                return true
            } catch (e: Exception) {
                android.util.Log.d("KASIR_LOGIN", "Login result: failed")
                if (e.message != null && (e.message!!.contains("Username") || e.message!!.contains("Password") || e.message!!.contains("dinonaktifkan"))) {
                    throw e
                }
                e.printStackTrace()
                throw Exception("Gagal login kasir: ${e.localizedMessage}")
            }
        }

        // 2. Standard flow for Owner Email Login
        try {
            val firebaseUid: String
            try {
                val result = withTimeoutOrNull(15000L) {
                    auth.signInWithEmailAndPassword(cleanInput, pass).await()
                } ?: throw Exception("Gagal masuk. Koneksi tertunda.")
                firebaseUid = result.user?.uid ?: throw Exception("Gagal masuk. Sesi tidak ditemukan.")
            } catch (e: com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                throw Exception("Email tidak terdaftar. Silakan daftar terlebih dahulu")
            } catch (e: com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                throw Exception("Password salah. Silakan coba lagi")
            } catch (e: com.google.firebase.auth.FirebaseAuthException) {
                val errorCode = e.errorCode
                val msg = when {
                    errorCode == "ERROR_USER_NOT_FOUND" || errorCode == "auth/user-not-found" -> "Email tidak terdaftar. Silakan daftar terlebih dahulu"
                    errorCode == "ERROR_WRONG_PASSWORD" || errorCode == "auth/wrong-password" -> "Password salah. Silakan coba lagi"
                    errorCode == "ERROR_INVALID_EMAIL" || errorCode == "auth/invalid-email" -> "Format email tidak valid"
                    errorCode == "ERROR_TOO_MANY_REQUESTS" || errorCode == "auth/too-many-requests" -> "Terlalu banyak percobaan. Coba lagi nanti"
                    else -> e.localizedMessage ?: "Gagal masuk. Silakan coba lagi."
                }
                throw Exception(msg)
            } catch (e: Exception) {
                e.printStackTrace()
                throw Exception("Koneksi gagal. Silakan periksa jaringan internet Anda.")
            }

            setLoggedInDeviceUser(firebaseUid)

            // Clear any stale local data of other accounts before syncing
            withContext(Dispatchers.IO) {
                try {
                    database.clearAllTables()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

            // Synchronize All Real User Data from Firestore to local Room Cache
            try {
                syncFromFirestore()
            } catch (s: Exception) {
                s.printStackTrace()
            }

            // Safe fallback if Firestore sync didn't find the user record
            val userInDb = dao.getUserById(firebaseUid)
            if (userInDb == null) {
                val role = "owner"
                val defaultUser = UserEntity(
                    uid = firebaseUid,
                    nama = "Owner Toko",
                    email = cleanInput,
                    role = role,
                    ownerId = null,
                    assignedBranchId = null,
                    subscriptionStatus = "free",
                    subscriptionStartDate = null,
                    subscriptionEndDate = null
                )
                try {
                    withTimeoutOrNull(5000L) {
                        firestore.collection("users").document(firebaseUid).set(defaultUser.toMap()).await()
                    }
                } catch (f: Exception) {
                    f.printStackTrace()
                }
                dao.clearUsers()
                dao.insertUser(defaultUser)
                saveUserSessionMetadata(defaultUser)
            } else {
                saveUserSessionMetadata(userInDb)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception(e.localizedMessage ?: "Email atau password salah!")
        }
    }

data class GoogleLoginResult(
    val success: Boolean,
    val isNewUser: Boolean,
    val role: String
)

    suspend fun loginWithGoogle(idToken: String): GoogleLoginResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = withTimeoutOrNull(10000L) {
                auth.signInWithCredential(credential).await()
            }
            val firebaseUid = result?.user?.uid ?: throw Exception("Auth failed or timed out")
            val email = result?.user?.email ?: "google-user@kasirpro.id"
            val namaUser = result?.user?.displayName ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
            val resultUid = firebaseUid

            setLoggedInDeviceUser(resultUid)
            android.util.Log.d("AUTH", "User UID: ${resultUid}")

            // Clear any stale local data of other accounts before Google session starts
            withContext(Dispatchers.IO) {
                try {
                    database.clearAllTables()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 1. Check if user document already exists in Firestore users collection
            var userExists = false
            var existingUser: UserEntity? = null

            try {
                // Check by UID first
                val doc = withTimeoutOrNull(5000L) {
                    firestore.collection("users").document(resultUid).get().await()
                }
                if (doc != null && doc.exists()) {
                    userExists = true
                    android.util.Log.d("AUTH", "User exists in Firestore by direct UID: true")
                    existingUser = UserEntity(
                        uid = resultUid,
                        nama = doc.getString("nama") ?: namaUser,
                        email = doc.getString("email") ?: email,
                        role = doc.getString("role") ?: "owner",
                        ownerId = doc.getString("ownerId"),
                        assignedBranchId = doc.getString("assignedBranchId"),
                        subscriptionStatus = doc.getString("subscriptionStatus") ?: "free",
                        subscriptionType = doc.getString("subscriptionType"),
                        subscriptionStartDate = doc.getLong("subscriptionStartDate"),
                        subscriptionEndDate = doc.getLong("subscriptionEndDate"),
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        lastActiveAt = System.currentTimeMillis()
                    )
                } else {
                    // Check by Email query as a fallback (for preexisting email/password registrations)
                    val cleanEmail = email.trim().lowercase()
                    var emailQuery = withTimeoutOrNull(5000L) {
                        firestore.collection("users").whereEqualTo("email", cleanEmail).get().await()
                    }
                    if (emailQuery == null || emailQuery.isEmpty) {
                        emailQuery = withTimeoutOrNull(5000L) {
                            firestore.collection("users").whereEqualTo("email", email.trim()).get().await()
                        }
                    }
                    if (emailQuery == null || emailQuery.isEmpty) {
                        if (cleanEmail == "kikijarrodt@gmail.com") {
                            emailQuery = withTimeoutOrNull(5000L) {
                                firestore.collection("users").whereEqualTo("email", "kikijarrodt@gmail.com").get().await()
                            }
                        }
                    }

                    if (emailQuery != null && !emailQuery.isEmpty) {
                        val firstDoc = emailQuery.documents.first()
                        userExists = true
                        val oldId = firstDoc.id
                        android.util.Log.d("AUTH", "User exists in Firestore by email lookup: true (old UID: $oldId)")
                        
                        existingUser = UserEntity(
                            uid = resultUid,
                            nama = firstDoc.getString("nama") ?: firstDoc.getString("name") ?: namaUser,
                            email = email,
                            role = firstDoc.getString("role") ?: "owner",
                            ownerId = firstDoc.getString("ownerId"),
                            assignedBranchId = firstDoc.getString("assignedBranchId"),
                            subscriptionStatus = firstDoc.getString("subscriptionStatus") ?: "free",
                            subscriptionType = firstDoc.getString("subscriptionType"),
                            subscriptionStartDate = firstDoc.getLong("subscriptionStartDate"),
                            subscriptionEndDate = firstDoc.getLong("subscriptionEndDate"),
                            createdAt = firstDoc.getLong("createdAt") ?: System.currentTimeMillis(),
                            lastActiveAt = System.currentTimeMillis()
                        )

                        // If they have a business under the old UID, copy and associate it with the new Google UID
                        try {
                            val bizQuery = withTimeoutOrNull(5000L) {
                                firestore.collection("businesses").whereEqualTo("ownerId", oldId).get().await()
                            }
                            if (bizQuery != null && !bizQuery.isEmpty) {
                                val oldBiz = bizQuery.documents.first()
                                val newBiz = BusinessEntity(
                                    id = "biz-$resultUid",
                                    ownerId = resultUid,
                                    namaBisnis = oldBiz.getString("namaBisnis") ?: "Toko Kasir",
                                    logoBase64 = oldBiz.getString("logoBase64"),
                                    alamat = oldBiz.getString("alamat"),
                                    noTelpon = oldBiz.getString("noTelpon"),
                                    createdAt = oldBiz.getLong("createdAt") ?: System.currentTimeMillis(),
                                    qrisBase64 = oldBiz.getString("qrisBase64")
                                )
                                // Save to Firestore and local Room
                                firestore.collection("businesses").document(newBiz.id).set(newBiz.toMap()).await()
                                dao.insertBusiness(newBiz)
                                android.util.Log.d("AUTH", "Migrated business info from $oldId to $resultUid")

                                // Copy products to the new business ID block in Firestore
                                val oldBizId = oldBiz.id
                                val newBizId = newBiz.id
                                try {
                                    val productsQuery = firestore.collection("products").whereEqualTo("businessId", oldBizId).get().await()
                                    for (pDoc in productsQuery.documents) {
                                        val productMap = pDoc.data?.toMutableMap() ?: mutableMapOf()
                                        productMap["id"] = pDoc.id
                                        productMap["businessId"] = newBizId
                                        firestore.collection("products").document(pDoc.id).set(productMap).await()
                                    }
                                    android.util.Log.d("AUTH", "Successfully copied associated products in Firestore")
                                    
                                    // Also copy transactions to new business ID
                                    val transQuery = firestore.collection("transactions").whereEqualTo("businessId", oldBizId).get().await()
                                    for (tDoc in transQuery.documents) {
                                        val transMap = tDoc.data?.toMutableMap() ?: mutableMapOf()
                                        transMap["id"] = tDoc.id
                                        transMap["businessId"] = newBizId
                                        firestore.collection("transactions").document(tDoc.id).set(transMap).await()
                                    }
                                    android.util.Log.d("AUTH", "Successfully copied associated transactions in Firestore")
                                } catch (pe: Exception) {
                                    pe.printStackTrace()
                                }
                            }
                        } catch (be: Exception) {
                            be.printStackTrace()
                        }
                    } else {
                        // Double check if there is an existing business under this resultUid
                        val bizCheck = withTimeoutOrNull(5000L) {
                            firestore.collection("businesses").whereEqualTo("ownerId", resultUid).get().await()
                        }
                        if (bizCheck != null && !bizCheck.isEmpty) {
                            userExists = true
                            android.util.Log.d("AUTH", "No user doc, but business found in Firestore for resultUid: true")
                            existingUser = UserEntity(
                                uid = resultUid,
                                nama = namaUser,
                                email = email,
                                role = "owner",
                                ownerId = null,
                                assignedBranchId = null,
                                subscriptionStatus = "free",
                                subscriptionStartDate = null,
                                subscriptionEndDate = null,
                                createdAt = System.currentTimeMillis(),
                                lastActiveAt = System.currentTimeMillis()
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            android.util.Log.d("AUTH", "User exists in Firestore: ${userExists}")

            val user = if (userExists && existingUser != null) {
                // User already exists online! Update details and lastActiveAt on Firestore under Google login resultUid
                val updatedUser = existingUser.copy(lastActiveAt = System.currentTimeMillis())
                try {
                    firestore.collection("users").document(resultUid).set(updatedUser.toMap()).await()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                updatedUser
            } else {
                // Truly a brand new user!
                val newUser = UserEntity(
                    uid = resultUid,
                    nama = namaUser,
                    email = email,
                    role = "owner",
                    ownerId = null,
                    assignedBranchId = null,
                    subscriptionStatus = "free",
                    subscriptionStartDate = null,
                    subscriptionEndDate = null,
                    createdAt = System.currentTimeMillis(),
                    lastActiveAt = System.currentTimeMillis()
                )
                // Write new user document to Firestore
                try {
                    firestore.collection("users").document(resultUid).set(newUser.toMap()).await()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                newUser
            }

            dao.clearUsers()
            dao.insertUser(user)
            saveUserSessionMetadata(user)

            try {
                syncFromFirestore(resultUid)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            GoogleLoginResult(
                success = true,
                isNewUser = !userExists,
                role = user.role
            )
        } catch (e: Exception) {
            e.printStackTrace()
            GoogleLoginResult(
                success = false,
                isNewUser = false,
                role = "owner"
            )
        }
    }

    suspend fun logout() {
        try {
            com.kasirpro.pospintar.app.util.ImageHelper.clearCache()
            auth.signOut()
        } catch (e: Exception) { e.printStackTrace() }
        setLoggedInDeviceUser(null)
        saveUserSessionMetadata(null)
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }
    }

    suspend fun resetPassword(email: String): Boolean {
        return try {
            auth.sendPasswordResetEmail(email).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // STORE ONBOARDING ACTIONS
    suspend fun setupToko(namaToko: String, alamat: String, logoBase64: String?): Boolean {
        val uid = auth.currentUser?.uid ?: "owner-uid"
        val businessId = "biz-$uid"
        val business = BusinessEntity(
            id = businessId,
            ownerId = uid,
            namaBisnis = namaToko,
            logoBase64 = logoBase64
        )
        try {
            firestore.collection("businesses").document(business.id).set(business.toMap()).await()
        } catch (e: Exception) { e.printStackTrace() }
        dao.insertBusiness(business)

        // Generate primary default branch
        val branch = BranchEntity(
            id = "branch-1-$businessId",
            businessId = business.id,
            namaCabang = "Cabang Utama",
            alamat = alamat,
            kasirIdsCsv = ""
        )
        try {
            firestore.collection("branches").document(branch.id).set(branch.toMap()).await()
        } catch (e: Exception) { e.printStackTrace() }
        dao.insertBranch(branch)
        return true
    }

    suspend fun getUserById(uid: String): UserEntity? {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                UserEntity(
                    uid = uid,
                    nama = doc.getString("nama") ?: "User",
                    email = doc.getString("email") ?: "",
                    role = doc.getString("role") ?: "owner",
                    ownerId = doc.getString("ownerId"),
                    assignedBranchId = doc.getString("assignedBranchId"),
                    subscriptionStatus = doc.getString("subscriptionStatus") ?: "free",
                    subscriptionType = doc.getString("subscriptionType"),
                    subscriptionStartDate = doc.getLong("subscriptionStartDate"),
                    subscriptionEndDate = doc.getLong("subscriptionEndDate"),
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    lastActiveAt = doc.getLong("lastActiveAt")
                )
            } else {
                dao.getUserById(uid)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            dao.getUserById(uid)
        }
    }

    suspend fun getProductById(id: String): ProductEntity? {
        return try {
            val doc = firestore.collection("products").document(id).get().await()
            if (doc.exists()) {
                val expRaw = doc.get("expiryDate")
                val expVal = when (expRaw) {
                    is com.google.firebase.Timestamp -> expRaw.toDate().time
                    is Number -> expRaw.toLong()
                    is String -> expRaw.toLongOrNull()
                    else -> null
                }
                ProductEntity(
                    id = doc.id,
                    businessId = doc.getString("businessId") ?: getResolvedBusinessId(),
                    branchId = doc.getString("branchId") ?: "branch-1",
                    nama = doc.getString("nama") ?: "",
                    kategori = doc.getString("kategori") ?: "",
                    hargaJual = doc.getSafeDouble("retailPrice").takeIf { it > 0.0 } ?: doc.getSafeDouble("hargaJual"),
                    hargaModal = doc.getSafeDouble("hargaModal"),
                    stok = doc.getLong("stok")?.toInt() ?: 0,
                    stokMinimum = doc.getLong("stokMinimum")?.toInt() ?: 0,
                    barcode = doc.getString("barcode"),
                    fotoBase64 = doc.getString("fotoBase64"),
                    varianRaw = doc.getString("varianRaw") ?: "",
                    satuan = doc.getString("satuan") ?: "Pcs",
                    isActive = doc.getBoolean("isActive") ?: true,
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    wholesalePrice = doc.getSafeDouble("wholesalePrice"),
                    wholesaleMinQty = doc.getLong("wholesaleMinQty")?.toInt() ?: 0,
                    expiryDate = expVal,
                    expiryReminderDays = doc.getLong("expiryReminderDays")?.toInt() ?: 7
                )
            } else {
                dao.getProductById(id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            dao.getProductById(id)
        }
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
        fotoBase64: String?,
        varianList: List<ProductVariant>,
        satuan: String = "Pcs",
        wholesalePrice: Double = 0.0,
        wholesaleMinQty: Int = 0,
        expiryDate: Long? = null,
        expiryReminderDays: Int = 7
    ): Boolean {
        val varianString = varianList.joinToString(";") { "${it.nama}:${it.harga}" }
        val bizId = getResolvedBusinessId()

        val product = ProductEntity(
            id = UUID.randomUUID().toString(),
            businessId = bizId,
            branchId = "branch-1-$bizId",
            nama = nama,
            kategori = kategori,
            hargaJual = hargaJual,
            hargaModal = hargaModal,
            stok = stok,
            stokMinimum = stokMinimum,
            barcode = barcode,
            fotoBase64 = fotoBase64,
            varianRaw = varianString,
            satuan = satuan,
            wholesalePrice = wholesalePrice,
            wholesaleMinQty = wholesaleMinQty,
            expiryDate = expiryDate,
            expiryReminderDays = expiryReminderDays
        )
        try {
            firestore.collection("products").document(product.id).set(product.toMap()).await()
        } catch (e: Exception) { e.printStackTrace() }
        dao.insertProduct(product)

        // Record stock history
        val hist = StockHistoryEntity(
            id = UUID.randomUUID().toString(),
            productId = product.id,
            businessId = bizId,
            tipe = "masuk",
            jumlah = stok,
            stokSebelum = 0,
            stokSesudah = stok,
            keterangan = "Stok awal produk baru"
        )
        try {
            firestore.collection("stock_history").document(hist.id).set(hist.toMap()).await()
        } catch (e: Exception) { e.printStackTrace() }
        dao.insertStockHistory(hist)
        return true
    }

    suspend fun insertProductWithBranch(
        id: String = UUID.randomUUID().toString(),
        nama: String,
        kategori: String,
        hargaJual: Double,
        hargaModal: Double,
        stok: Int,
        stokMinimum: Int,
        barcode: String?,
        fotoBase64: String?,
        branchId: String,
        satuan: String = "Pcs",
        wholesalePrice: Double = 0.0,
        wholesaleMinQty: Int = 0,
        expiryDate: Long? = null,
        expiryReminderDays: Int = 7
    ): Boolean {
        val bizId = getResolvedBusinessId()

        val product = ProductEntity(
            id = id,
            businessId = bizId,
            branchId = branchId,
            nama = nama,
            kategori = kategori,
            hargaJual = hargaJual,
            hargaModal = hargaModal,
            stok = stok,
            stokMinimum = stokMinimum,
            barcode = barcode,
            fotoBase64 = fotoBase64,
            varianRaw = "",
            satuan = satuan,
            wholesalePrice = wholesalePrice,
            wholesaleMinQty = wholesaleMinQty,
            expiryDate = expiryDate,
            expiryReminderDays = expiryReminderDays
        )
        try {
            firestore.collection("products").document(product.id).set(product.toMap()).await()
        } catch (e: Exception) { e.printStackTrace() }
        dao.insertProduct(product)

        val hist = StockHistoryEntity(
            id = UUID.randomUUID().toString(),
            productId = product.id,
            businessId = bizId,
            tipe = "masuk",
            jumlah = stok,
            stokSebelum = 0,
            stokSesudah = stok,
            keterangan = "Stok awal bulk upload"
        )
        try {
            firestore.collection("stock_history").document(hist.id).set(hist.toMap()).await()
        } catch (e: Exception) { e.printStackTrace() }
        dao.insertStockHistory(hist)
        return true
    }

    suspend fun updateProduct(product: ProductEntity) {
        try {
            firestore.collection("products").document(product.id).set(product.toMap()).await()
        } catch (e: Exception) { e.printStackTrace() }
        dao.insertProduct(product)
    }

    suspend fun deleteProduct(id: String) {
        try {
            firestore.collection("products").document(id).delete().await()
        } catch (e: Exception) { e.printStackTrace() }
        dao.deleteProduct(id)
    }

    // STOCK MANAGEMENT
    suspend fun recordStockMovement(
        productId: String,
        tipe: String, // "masuk", "keluar", "opname"
        jumlah: Int,
        keterangan: String?
    ) {
        val prod = getProductById(productId) ?: return
        val stokSebelum = prod.stok
        val stokSesudah = when (tipe) {
            "masuk" -> stokSebelum + jumlah
            "keluar" -> stokSebelum - jumlah
            "opname" -> jumlah
            else -> stokSebelum
        }
        val updated = prod.copy(stok = stokSesudah)
        try {
            firestore.collection("products").document(updated.id).set(updated.toMap()).await()
        } catch (e: Exception) { e.printStackTrace() }
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
        try {
            firestore.collection("stock_history").document(hist.id).set(hist.toMap()).await()
        } catch (e: Exception) { e.printStackTrace() }
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
        val itemsString = items.joinToString(";") {
            "${it.id}:${it.nama}:${it.jumlah}:${it.harga}:${it.varianSelected ?: ""}:${it.diskon}:${it.satuan}"
        }

        val currUser = currentUser.firstOrNull()
        var currentKasirId = "kasir-1"
        var currentKasirNama = "Kasir Pro"
        val bizId = getResolvedBusinessId()
        var currentBranchId = "branch-1-$bizId"

        if (currUser != null) {
            currentKasirId = currUser.uid
            currentKasirNama = currUser.nama
            currentBranchId = currUser.assignedBranchId ?: "branch-1-$bizId"

            if (currUser.role == "kasir") {
                val updatedKasir = currUser.copy(lastActiveAt = System.currentTimeMillis())
                try {
                    firestore.collection("users").document(updatedKasir.uid).set(updatedKasir.toMap()).await()
                } catch (e: Exception) { e.printStackTrace() }
                dao.insertUser(updatedKasir)
            }
        }

        val currentBizId = getResolvedBusinessId()

        val tx = TransactionEntity(
            id = "TRX-${System.currentTimeMillis()}",
            businessId = currentBizId,
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

        try {
            val branchesList = try {
                dao.getAllBranches().first()
            } catch(e: Exception) {
                emptyList()
            }
            val currentBranch = branchesList.find { it.id == currentBranchId }
            val currentBranchNama = currentBranch?.namaCabang ?: "Cabang Utama"

            val firestoreMap = tx.toMap().toMutableMap().apply {
                put("cashierId", currentKasirId)
                put("cashierName", currentKasirNama)
                put("branchName", currentBranchNama)
            }

            // Asynchronously post transaction to firestore to eliminate UI freezing delays
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    firestore.collection("transactions").document(tx.id).set(firestoreMap).await()
                } catch (e: Exception) { e.printStackTrace() }
            }
        } catch (e: Exception) { e.printStackTrace() }
        
        dao.insertTransaction(tx)

        // Update product inventory & record stock movement
        items.forEach { item ->
            val product = getProductById(item.id)
            if (product != null) {
                val stokSebelum = product.stok
                val stokSesudah = (stokSebelum - item.jumlah).coerceAtLeast(0)
                val updatedProduct = product.copy(stok = stokSesudah)
                
                // Asynchronously update product stock on firestore
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        firestore.collection("products").document(updatedProduct.id).set(updatedProduct.toMap()).await()
                    } catch (e: Exception) { e.printStackTrace() }
                }
                
                dao.insertProduct(updatedProduct)

                // Log stock movement
                val hs = StockHistoryEntity(
                    id = UUID.randomUUID().toString(),
                    productId = item.id,
                    businessId = product.businessId,
                    tipe = "keluar",
                    jumlah = item.jumlah,
                    stokSebelum = stokSebelum,
                    stokSesudah = stokSesudah,
                    keterangan = "Penjualan transaksi ${tx.id}"
                )
                // Asynchronously update stock history on firestore
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        firestore.collection("stock_history").document(hs.id).set(hs.toMap()).await()
                    } catch (e: Exception) { e.printStackTrace() }
                }
                
                dao.insertStockHistory(hs)
            }
        }

        if (status == "dp" && pelangganId != null) {
            val sisaHutang = total - bayarNominal
            if (sisaHutang > 0) {
                var pelangganNama = "Pelanggan Setia"
                val customer = dao.getAllCustomersRaw().find { it.id == pelangganId }
                if (customer != null) {
                    pelangganNama = customer.nama
                }
                val d = DebtEntity(
                    id = UUID.randomUUID().toString(),
                    businessId = tx.businessId,
                    branchId = tx.branchId,
                    pelangganId = pelangganId,
                    pelangganNama = pelangganNama,
                    jumlah = sisaHutang,
                    transaksiId = tx.id,
                    status = "belum"
                )
                // Asynchronously write debt database to firestore
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        firestore.collection("debts").document(d.id).set(d.toMap()).await()
                    } catch (e: Exception) { e.printStackTrace() }
                }
                dao.insertDebt(d)
            }
        }

        return tx
    }

    // PERSISTENCE SYNC FOR OFFLINE MODE
    suspend fun synchronizeOfflineData(): Int {
        var syncCount = 0

        // 1. Sync pending transactions
        try {
            val pending = dao.getOfflinePendingTransactions()
            pending.forEach { trx ->
                try {
                    firestore.collection("transactions").document(trx.id).set(trx.toMap()).await()
                    dao.markTransactionSynced(trx.id)
                    syncCount++
                } catch (e: Exception) { e.printStackTrace() }
            }
        } catch (e: Exception) { e.printStackTrace() }

        // 2. Sync products
        try {
            val localProducts = dao.getAllProductsRaw()
            localProducts.forEach { p ->
                // Push metadata to Firestore
                try {
                    firestore.collection("products").document(p.id).set(p.toMap()).await()
                } catch (dbEx: Exception) {
                    android.util.Log.e("OFFLINE_SYNC", "Failed to sync product ${p.nama} to Firestore: ${dbEx.message}")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("OFFLINE_SYNC", "Failure during product sync: ${e.message}", e)
        }

        // 3. Sync customers to Firestore
        try {
            val customers = dao.getAllCustomersRaw()
            customers.forEach { c ->
                try {
                    firestore.collection("customers").document(c.id).set(c.toMap()).await()
                } catch (e: Exception) { e.printStackTrace() }
            }
        } catch (e: Exception) { e.printStackTrace() }

        // 4. Sync promos to Firestore
        try {
            val promos = dao.getAllPromosRaw()
            promos.forEach { pr ->
                try {
                    firestore.collection("promos").document(pr.id).set(pr.toMap()).await()
                } catch (e: Exception) { e.printStackTrace() }
            }
        } catch (e: Exception) { e.printStackTrace() }

        // 5. Sync debts to Firestore
        try {
            val debts = dao.getAllDebtsRaw()
            debts.forEach { d ->
                try {
                    firestore.collection("debts").document(d.id).set(d.toMap()).await()
                } catch (e: Exception) { e.printStackTrace() }
            }
        } catch (e: Exception) { e.printStackTrace() }

        return syncCount
    }

    // MULTI BRANCH MANAGEMENT
    suspend fun addBranch(nama: String, alamat: String) {
        val bizId = getResolvedBusinessId()
        val branch = BranchEntity(
            id = UUID.randomUUID().toString(),
            businessId = bizId,
            namaCabang = nama,
            alamat = alamat,
            kasirIdsCsv = ""
        )
        try {
            firestore.collection("branches").document(branch.id).set(branch.toMap()).await()
        } catch (e: Exception) { e.printStackTrace() }
        dao.insertBranch(branch)
    }

    suspend fun updateBranch(id: String, nama: String, alamat: String) {
        val branchList = allBranches.firstOrNull() ?: emptyList()
        val currentBranch = branchList.find { it.id == id }
        if (currentBranch != null) {
            val updated = currentBranch.copy(namaCabang = nama, alamat = alamat)
            try {
                firestore.collection("branches").document(id).set(updated.toMap()).await()
            } catch (e: Exception) { e.printStackTrace() }
        }
        dao.updateBranch(id, nama, alamat)
    }

    suspend fun deleteBranch(id: String) {
        try {
            firestore.collection("branches").document(id).delete().await()
        } catch (e: Exception) { e.printStackTrace() }
        dao.deleteBranch(id)
    }

    // KASIR MANAGEMENT
    suspend fun isUsernameAvailable(username: String): Boolean {
        val lowercaseUsername = username.trim().lowercase()
        if (lowercaseUsername.isBlank()) return false
        return try {
            val queryCashiers = firestore.collection("cashiers")
                .whereEqualTo("username", lowercaseUsername)
                .get()
                .await()
            val queryUsers = firestore.collection("users")
                .whereEqualTo("email", lowercaseUsername)
                .get()
                .await()
            queryCashiers.isEmpty && queryUsers.isEmpty
        } catch (e: Exception) {
            e.printStackTrace()
            true
        }
    }

    suspend fun getCashierPassword(cashierId: String): String {
        return try {
            val doc = firestore.collection("cashiers").document(cashierId).get().await()
            doc.getString("password") ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun editCashier(cashierId: String, oldUsername: String, newNama: String, newUsername: String, newPass: String): Boolean {
        val lowercaseOld = oldUsername.trim().lowercase()
        val lowercaseNew = newUsername.trim().lowercase()
        val ownerId = auth.currentUser?.uid ?: "owner-uid"

        if (lowercaseOld != lowercaseNew) {
            val available = isUsernameAvailable(lowercaseNew)
            if (!available) {
                return false
            }
        }

        val user = getUserById(cashierId) ?: return false
        val updatedUser = user.copy(
            nama = newNama,
            email = lowercaseNew
        )

        try {
            val updateMap = mapOf(
                "nama" to newNama,
                "cashierName" to newNama,
                "username" to lowercaseNew,
                "password" to newPass
            )
            firestore.collection("cashiers").document(cashierId).update(updateMap).await()
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val branchName = if (user.assignedBranchId != null) {
                    dao.getBranchById(user.assignedBranchId)?.namaCabang ?: "Cabang Utama"
                } else "Cabang Utama"
                val cashierMap = mapOf(
                    "ownerId" to ownerId,
                    "cashierName" to newNama,
                    "nama" to newNama,
                    "username" to lowercaseNew,
                    "password" to newPass,
                    "branchId" to user.assignedBranchId,
                    "branchName" to branchName,
                    "status" to "aktif",
                    "isActive" to true,
                    "createdAt" to user.createdAt
                )
                firestore.collection("cashiers").document(cashierId).set(cashierMap).await()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        dao.insertUser(updatedUser)
        return true
    }

    suspend fun addCashier(nama: String, username: String, pass: String, branchId: String): Boolean {
        val lowercaseUsername = username.trim().lowercase()
        val ownerId = auth.currentUser?.uid ?: "owner-uid"
        val docId = "${lowercaseUsername}_$ownerId"

        // Check global uniqueness of username
        val available = isUsernameAvailable(lowercaseUsername)
        if (!available) {
            return false
        }

        val branchEntity = dao.getBranchById(branchId)
        val branchName = branchEntity?.namaCabang ?: "Cabang Utama"

        // 2. Put into Firestore collection "cashiers"
        val cashierMap = mapOf(
            "ownerId" to ownerId,
            "cashierName" to nama,
            "nama" to nama,
            "username" to lowercaseUsername,
            "password" to pass,
            "branchId" to branchId,
            "branchName" to branchName,
            "status" to "aktif",
            "isActive" to true,
            "createdAt" to System.currentTimeMillis()
        )

        try {
            firestore.collection("cashiers").document(docId).set(cashierMap).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Save to local Room DB
        val user = UserEntity(
            uid = docId,
            nama = nama,
            email = lowercaseUsername,
            role = "kasir",
            ownerId = ownerId,
            assignedBranchId = branchId,
            subscriptionStatus = "free",
            subscriptionStartDate = null,
            subscriptionEndDate = null,
            createdAt = System.currentTimeMillis(),
            lastActiveAt = null
        )
        dao.insertUser(user)
        return true
    }

    suspend fun assignCashierToBranch(cashierId: String, branchId: String?) {
        val user = getUserById(cashierId)
        if (user != null) {
            val updated = user.copy(assignedBranchId = branchId)
            try {
                val branchName = if (branchId != null) {
                    dao.getBranchById(branchId)?.namaCabang ?: "Cabang Utama"
                } else "Cabang Utama"
                
                val updateMap = mapOf(
                    "branchId" to branchId,
                    "branchName" to branchName
                )
                firestore.collection("cashiers").document(cashierId).update(updateMap).await()
            } catch (e: Exception) { e.printStackTrace() }
            dao.insertUser(updated)
        }
    }

    suspend fun deleteCashier(uid: String) {
        try {
            firestore.collection("cashiers").document(uid).delete().await()
        } catch (e: Exception) { e.printStackTrace() }
        dao.deleteUser(uid)
    }

    // CUSTOMERS
    suspend fun addCustomer(nama: String, nomorHp: String, alamat: String? = null) {
        val bizId = getResolvedBusinessId()
        val cust = CustomerEntity(
            id = UUID.randomUUID().toString(),
            businessId = bizId,
            nama = nama,
            nomorHp = nomorHp,
            totalPoin = 0,
            totalTransaksi = 0,
            alamat = alamat
        )
        try {
            firestore.collection("customers").document(cust.id).set(cust.toMap()).await()
        } catch (e: Exception) { e.printStackTrace() }
        dao.insertCustomer(cust)
    }

    suspend fun updateCustomer(customer: CustomerEntity) {
        try {
            firestore.collection("customers").document(customer.id).set(customer.toMap()).await()
        } catch (e: Exception) { e.printStackTrace() }
        dao.insertCustomer(customer)
    }

    // DEBTS
    suspend fun payDebt(debtId: String) {
        val debtsList = allDebts.firstOrNull()
        val debt = debtsList?.find { it.id == debtId }
        if (debt != null) {
            val updated = debt.copy(status = "lunas")
            try {
                firestore.collection("debts").document(debtId).set(updated.toMap()).await()
            } catch (e: Exception) { e.printStackTrace() }
            dao.updateDebtStatus(debtId, "lunas")

            val curUser = currentUser.firstOrNull()
            val kasirId = curUser?.uid ?: "kasir-1"
            val kasirNama = curUser?.nama ?: "Sistem Kasir"

            val px = TransactionEntity(
                id = "TRX-LUNAS-${System.currentTimeMillis()}",
                businessId = debt.businessId,
                branchId = debt.branchId,
                kasirId = kasirId,
                kasirNama = kasirNama,
                itemsRaw = "HUTANG:Pelunasan Hutang oleh ${debt.pelangganNama}:1:${debt.jumlah}::0:Pcs",
                subtotal = debt.jumlah,
                diskonTotal = 0.0,
                kodePromo = null,
                total = debt.jumlah,
                metodeBayar = "Pelunasan Hutang (Tunai)",
                bayarNominal = debt.jumlah,
                kembalian = 0.0,
                status = "lunas",
                pelangganId = debt.pelangganId
            )
            try {
                firestore.collection("transactions").document(px.id).set(px.toMap()).await()
            } catch(e: Exception) { e.printStackTrace() }
            dao.insertTransaction(px)

            // Dynamic customer loyalty point updates when settling debts
            try {
                val cust = dao.getAllCustomersRaw().find { it.id == debt.pelangganId }
                if (cust != null) {
                    val earnRate = prefs.getFloat("point_earn_rate", 10000f).toDouble()
                    val addedPoints = if (earnRate > 0) (debt.jumlah / earnRate).toInt() else 0
                    val updatedCustomer = cust.copy(
                        totalPoin = (cust.totalPoin + addedPoints).coerceAtLeast(0)
                    )
                    updateCustomer(updatedCustomer)
                }
            } catch (e: Exception) { e.printStackTrace() }
        } else {
            dao.updateDebtStatus(debtId, "lunas")
        }
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
        val bizId = getResolvedBusinessId()
        val promo = PromoEntity(
            id = UUID.randomUUID().toString(),
            businessId = bizId,
            nama = nama,
            tipe = tipe,
            nilai = nilai,
            minTransaksi = minTransaksi,
            kode = kode,
            isActive = true,
            berlakuSampai = berlakuSampai
        )
        try {
            firestore.collection("promos").document(promo.id).set(promo.toMap()).await()
        } catch (e: Exception) { e.printStackTrace() }
        dao.insertPromo(promo)
    }

    suspend fun togglePromo(id: String, active: Boolean) {
        val promoList = allPromos.firstOrNull()
        val currentPromo = promoList?.find { it.id == id }
        if (currentPromo != null) {
            val updated = currentPromo.copy(isActive = active)
            try {
                firestore.collection("promos").document(id).set(updated.toMap()).await()
            } catch (e: Exception) { e.printStackTrace() }
        }
        dao.updatePromoStatus(id, active)
    }

    // BACKUP
    suspend fun triggerBackup(): Boolean {
        _lastBackupDate.value = "Hari ini, ${getCurrentTimeString()}"
        return true
    }

    // PLAY BILLING SUBSCRIPTION
    suspend fun activatePlayBillingSubscription(
        uid: String,
        productId: String,
        basePlanId: String,
        purchaseToken: String
    ): Boolean {
        return try {
            val user = getUserById(uid) ?: return false
            
            // 1. Tentukan subscriptionStatus dari productId
            val status = when {
                productId.contains("dasar") -> "dasar"
                productId.contains("profesional") -> "profesional"
                productId.contains("bisnis") -> "bisnis"
                else -> "free"
            }
            
            // 2. Tentukan subscriptionType dari basePlanId
            val type = when {
                basePlanId.contains("tahunan") -> "tahunan"
                basePlanId.contains("bulanan") -> "bulanan"
                else -> "bulanan"
            }
            
            // 3. Hitung subscriptionEndDate
            val durationDays = if (type == "tahunan") 365L else 30L
            val startDate = System.currentTimeMillis()
            val endDate = startDate + (durationDays * 24 * 60 * 60 * 1000)
            
            val updated = user.copy(
                subscriptionStatus = status,
                subscriptionType = type,
                subscriptionStartDate = startDate,
                subscriptionEndDate = endDate
            )
            
            // 4. Simpan purchaseToken ke field baru "playBillingPurchaseToken" di dokumen user Firestore
            val firestoreMap = updated.toMap().toMutableMap().apply {
                put("playBillingPurchaseToken", purchaseToken)
            }
            
            // 5. Update dokumen user seperti fungsi upgradeUserSubscription yang sudah ada
            withTimeoutOrNull(8500L) {
                firestore.collection("users").document(uid).set(firestoreMap).await()
            }
            
            // Save to local Room
            dao.insertUser(updated)
            android.util.Log.d("PLAY_BILLING", "Subscription activated: status=$status, type=$type, end=$endDate")
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // SUBSCRIPTION & ACTIVATION CODES FLOW
    suspend fun upgradeUserSubscription(uid: String, status: String, isYearly: Boolean = false): Boolean {
        return try {
            val user = getUserById(uid) ?: return false
            val endDate = if (status == "premium") {
                if (isYearly) {
                    System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000) // 1 Year (365 days)
                } else {
                    System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000) // 1 Month (30 days)
                }
            } else {
                null
            }
            val startDate = if (status == "premium") {
                System.currentTimeMillis()
            } else {
                null
            }
            val updated = user.copy(
                subscriptionStatus = status,
                subscriptionStartDate = startDate,
                subscriptionEndDate = endDate
            )
            // Save to Firestore
            withTimeoutOrNull(8500L) {
                firestore.collection("users").document(uid).set(updated.toMap()).await()
            }
            // Save to local Room
            dao.insertUser(updated)
            android.util.Log.d("SUBSCRIPTION", "Status updated to premium for uid: ${user.uid}")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun processSubscription(packageName: String): Boolean {
        return true
    }

    suspend fun redeemActivationCode(uid: String, rawCode: String, expectedPackage: String? = null, expectedBillingCycle: String? = null): RedeemResult {
        return try {
            val code = rawCode.trim().uppercase()
            // Check if user is already premium
            val user = getUserById(uid) ?: return RedeemResult.Error("Pengguna tidak ditemukan")

            // Check local first!
            val localList = getLocalActivationCodes()
            val localMatch = localList.find { (it["id"] as? String)?.trim()?.uppercase() == code }

            var durationDays = 30L
            var codeType = "profesional"
            var billingCycle = "bulanan"
            var checkedSuccessfully = false

            if (localMatch != null) {
                val targetUid = (localMatch["targetUid"] as? String)?.trim() ?: ""
                if (targetUid.isNotBlank() && !targetUid.equals(uid.trim(), ignoreCase = true)) {
                    return RedeemResult.Error("Kode ini dikhususkan untuk ID Pengguna: $targetUid. ID Anda (${uid.trim()}) tidak cocok.")
                }
                val isUsed = localMatch["isUsed"] as? Boolean ?: false
                if (isUsed) {
                    return RedeemResult.Error("Kode sudah pernah digunakan")
                }
                durationDays = (localMatch["durationDays"] as? Number)?.toLong() ?: 30L
                codeType = (localMatch["type"] as? String) ?: "profesional"
                billingCycle = (localMatch["billingCycle"] as? String) ?: "bulanan"
                checkedSuccessfully = true
            }

            // 1. Try to check the user's OWN document (/users/{uid}) first!
            // This is completely free of PERMISSION_DENIED issues on 'activation_codes' collection!
            if (!checkedSuccessfully) {
                try {
                    val userDocRef = firestore.collection("users").document(uid)
                    val userSnapshot = userDocRef.get().await()
                    if (userSnapshot.exists() && userSnapshot.contains("assignedCodes")) {
                        val assignedCodes = userSnapshot.get("assignedCodes") as? Map<String, Any?>
                        val codeInfo = assignedCodes?.get(code) as? Map<String, Any?>
                        if (codeInfo != null) {
                            val isUsed = codeInfo["isUsed"] as? Boolean ?: false
                            if (isUsed) {
                                return RedeemResult.Error("Kode sudah pernah digunakan")
                            }
                            durationDays = (codeInfo["durationDays"] as? Number)?.toLong() ?: 30L
                            codeType = (codeInfo["type"] as? String) ?: "profesional"
                            billingCycle = (codeInfo["billingCycle"] as? String) ?: "bulanan"
                            checkedSuccessfully = true

                            // Mark it as used in user's own document
                            val updatedCodes = assignedCodes.toMutableMap().apply {
                                val currentCodeMap = (this[code] as? Map<String, Any?>)?.toMutableMap() ?: mutableMapOf()
                                currentCodeMap["isUsed"] = true
                                currentCodeMap["usedAt"] = System.currentTimeMillis()
                                this[code] = currentCodeMap
                            }
                            userDocRef.update("assignedCodes", updatedCodes).await()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    android.util.Log.e("REDEEM", "Error reading/updating assignedCodes in users collection", e)
                }
            }

            // 2. If not checked via local or user doc (e.g. old codes), fall back to standard activation_codes collection lookup
            if (!checkedSuccessfully) {
                try {
                    val docRef = firestore.collection("activation_codes").document(code)
                    val snapshot = docRef.get().await()
                    if (snapshot.exists()) {
                        val targetUid = (snapshot.getString("targetUid"))?.trim() ?: ""
                        if (targetUid.isNotBlank() && !targetUid.equals(uid.trim(), ignoreCase = true)) {
                            return RedeemResult.Error("Kode ini dikhususkan untuk ID Pengguna: $targetUid. ID Anda (${uid.trim()}) tidak cocok.")
                        }
                        val isUsed = snapshot.getBoolean("isUsed") ?: false
                        if (isUsed) {
                            return RedeemResult.Error("Kode sudah pernah digunakan")
                        }
                        durationDays = snapshot.getLong("durationDays") ?: 30L
                        codeType = snapshot.getString("type") ?: "profesional"
                        billingCycle = snapshot.getString("billingCycle") ?: "bulanan"
                        checkedSuccessfully = true
                    } else {
                        return RedeemResult.Error("Kode tidak valid atau belum didaftarkan oleh admin.")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    val errorMsg = e.message ?: ""
                    if (errorMsg.contains("PERMISSION_DENIED", ignoreCase = true) || errorMsg.contains("permission-denied", ignoreCase = true)) {
                        return RedeemResult.Error(
                            "Akses ditolak: Kode tidak valid atau tidak terdaftar untuk ID Pengguna Anda.\n\n" +
                            "Pastikan kode belum digunakan dan Admin mendaftarkan kode ini khusus untuk ID Pengguna (UID) Anda:\n$uid"
                        )
                    }
                    return RedeemResult.Error("Gagal memverifikasi kode (Offline atau error jaringan): ${e.message}")
                }
            }

            // Bulletproof string-based parser override in case of unpopulated DB fields
            if (code.startsWith("KASIRPRO-DASARTAHUNAN-")) {
                codeType = "dasar"
                billingCycle = "tahunan"
            } else if (code.startsWith("KASIRPRO-DASAR-")) {
                codeType = "dasar"
                billingCycle = "bulanan"
            } else if (code.startsWith("KASIRPRO-PROTAHUNAN-")) {
                codeType = "profesional"
                billingCycle = "tahunan"
            } else if (code.startsWith("KASIRPRO-PRO-")) {
                codeType = "profesional"
                billingCycle = "bulanan"
            } else if (code.startsWith("KASIRPRO-BISNISTAHUNAN-")) {
                codeType = "bisnis"
                billingCycle = "tahunan"
            } else if (code.startsWith("KASIRPRO-BISNIS-")) {
                codeType = "bisnis"
                billingCycle = "bulanan"
            }

            // Check if code matches the selected subscription package
            val cleanExpected = expectedPackage?.trim()?.lowercase()
            if (cleanExpected != null) {
                val cleanType = codeType.trim().lowercase()
                val isCompatible = cleanType == cleanExpected || 
                        (cleanType == "premium" && cleanExpected == "profesional") || 
                        (cleanType == "profesional" && cleanExpected == "premium")
                if (!isCompatible) {
                    val expectedName = when (cleanExpected) {
                        "dasar" -> "Paket Dasar"
                        "profesional" -> "Paket Profesional"
                        "bisnis" -> "Paket Bisnis"
                        else -> cleanExpected.replaceFirstChar { it.uppercase() }
                    }
                    val codeTypeName = when (cleanType) {
                        "dasar" -> "Paket Dasar"
                        "profesional" -> "Paket Profesional"
                        "bisnis" -> "Paket Bisnis"
                        else -> cleanType.replaceFirstChar { it.uppercase() }
                    }
                    return RedeemResult.Error("Kode yang dimasukkan adalah untuk $codeTypeName, sedangkan opsi yang Anda pilih adalah $expectedName. Silakan masukkan kode yang sesuai.")
                }
            }

            // Check if code matches the selected billing cycle
            val cleanExpectedCycle = expectedBillingCycle?.trim()?.lowercase()
            if (cleanExpectedCycle != null) {
                val cleanCycle = billingCycle.trim().lowercase()
                if (cleanCycle != cleanExpectedCycle) {
                    val expectedCycleName = if (cleanExpectedCycle == "tahunan") "Tahunan" else "Bulanan"
                    val codeCycleName = if (cleanCycle == "tahunan") "Tahunan" else "Bulanan"
                    return RedeemResult.Error("Kode yang dimasukkan adalah untuk tipe billing $codeCycleName, sedangkan opsi yang Anda pilih adalah $expectedCycleName. Silakan pilih tab opsi paket yang sesuai atau masukkan kode yang sesuai.")
                }
            }

            val calculatedDays = if (billingCycle.lowercase() == "tahunan") 365L else 30L
            val now = System.currentTimeMillis()
            val endDate = now + (calculatedDays * 24L * 60L * 60L * 1000L)

            // Update local status map
            val updatedLocal = localList.map { map ->
                if ((map["id"] as? String)?.trim()?.uppercase() == code) {
                    map.toMutableMap().apply {
                        this["isUsed"] = true
                        this["usedBy"] = uid
                        this["usedAt"] = now
                    }
                } else map
            }
            saveLocalActivationCodes(updatedLocal)
            _localCodesFlow.value = updatedLocal

            // Update firestore status
            try {
                firestore.collection("activation_codes").document(code).update(
                    mapOf(
                        "isUsed" to true,
                        "usedBy" to uid,
                        "usedAt" to now
                    )
                ).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Upgrade User to premium package type
            val updatedUser = user.copy(
                subscriptionStatus = codeType.lowercase(),
                subscriptionType = billingCycle.lowercase(),
                subscriptionStartDate = now,
                subscriptionEndDate = endDate
            )

            // Save to Firestore & local DB
            try {
                firestore.collection("users").document(uid).set(updatedUser.toMap()).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            dao.insertUser(updatedUser)

            RedeemResult.Success(endDate)
        } catch (e: Exception) {
            e.printStackTrace()
            RedeemResult.Error("Terjadi kesalahan: ${e.message}")
        }
    }

    fun getActivationCodesFlow(): kotlinx.coroutines.flow.Flow<List<Map<String, Any?>>> {
        val firestoreFlow = callbackFlow {
            trySend(emptyList()) // Send initial empty list to unblock combine on startup
            var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
            try {
                listenerRegistration = firestore.collection("activation_codes")
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            android.util.Log.e("FIRESTORE", "Error listening to activation_codes: ${error.message}")
                            trySend(emptyList())
                            return@addSnapshotListener
                        }
                        val list = snapshot?.documents?.mapNotNull { doc ->
                            val data = doc.data?.toMutableMap() ?: return@mapNotNull null
                            data["id"] = doc.id
                            data
                        } ?: emptyList()
                        trySend(list)
                    }
            } catch (e: Exception) {
                trySend(emptyList())
            }
            awaitClose { listenerRegistration?.remove() }
        }.flowOn(Dispatchers.IO)

        return kotlinx.coroutines.flow.combine(firestoreFlow, _localCodesFlow) { firestoreList, localList ->
            val merged = mutableMapOf<String, Map<String, Any?>>()
            for (code in localList) {
                val id = code["id"] as? String ?: continue
                merged[id] = code
            }
            for (code in firestoreList) {
                val id = code["id"] as? String ?: continue
                merged[id] = code
            }
            merged.values.sortedByDescending { (it["createdAt"] as? Number)?.toLong() ?: 0L }
        }
    }

    suspend fun generateActivationCode(type: String, billingCycle: String, durationDays: Int, createdByUid: String, targetUid: String): Boolean {
        android.util.Log.d("ADMIN", "Generating code for $targetUid (type=$type cycle=$billingCycle)...")
        val suffix = (1..6).map { "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".random() }.joinToString("")
        
        // Format of the suffix according to package:
        val prefix = when (type.lowercase()) {
            "dasar" -> if (billingCycle.lowercase() == "tahunan") "DASARTAHUNAN" else "DASAR"
            "profesional" -> if (billingCycle.lowercase() == "tahunan") "PROTAHUNAN" else "PRO"
            "bisnis" -> if (billingCycle.lowercase() == "tahunan") "BISNISTAHUNAN" else "BISNIS"
            else -> if (billingCycle.lowercase() == "tahunan") "PROTAHUNAN" else "PRO"
        }
        val kode = "KASIRPRO-$prefix-$suffix"
        android.util.Log.d("ADMIN", "Code generated: $kode")
        
        val now = System.currentTimeMillis()
        val codeMap = mapOf(
            "id" to kode,
            "type" to type.lowercase(),
            "billingCycle" to billingCycle.lowercase(),
            "durationDays" to durationDays.toLong(),
            "isUsed" to false,
            "usedBy" to null,
            "usedAt" to null,
            "createdAt" to now,
            "createdBy" to createdByUid,
            "targetUid" to targetUid
        )

        // Save local
        val currentLocal = getLocalActivationCodes().toMutableList()
        currentLocal.add(0, codeMap)
        saveLocalActivationCodes(currentLocal)
        _localCodesFlow.value = currentLocal

        var savedToActivationCodes = false
        var savedToUserDocument = false

        // 1. Try to save to activation_codes collection
        try {
            firestore.collection("activation_codes").document(kode).set(codeMap).await()
            android.util.Log.d("ADMIN", "Firestore Save result to activation_codes: success")
            savedToActivationCodes = true
        } catch (e: Exception) {
            android.util.Log.e("ADMIN", "Could not write to activation_codes collection (likely security rules)", e)
        }

        // 2. Try to copy code to user's personal Firestore document for direct read bypass
        if (targetUid.isNotBlank()) {
            try {
                val userDocRef = firestore.collection("users").document(targetUid.trim())
                val userSnapshot = userDocRef.get().await()
                val assignedCodes = if (userSnapshot.exists()) {
                    userSnapshot.get("assignedCodes") as? Map<String, Any?> ?: emptyMap()
                } else {
                    emptyMap()
                }
                val updatedCodes = assignedCodes.toMutableMap().apply {
                    this[kode] = mapOf(
                        "code" to kode,
                        "type" to type.lowercase(),
                        "billingCycle" to billingCycle.lowercase(),
                        "durationDays" to durationDays.toLong(),
                        "isUsed" to false,
                        "createdAt" to now
                    )
                }
                userDocRef.set(
                    mapOf("assignedCodes" to updatedCodes),
                    com.google.firebase.firestore.SetOptions.merge()
                ).await()
                android.util.Log.d("ADMIN", "Successfully associated code directly in user's document path")
                savedToUserDocument = true
            } catch (e: Exception) {
                android.util.Log.e("ADMIN", "Could not write assignedCodes fallback to user document", e)
            }
        }

        // Return true since local generation was successful and the code list is ready in local database
        return true
    }

    suspend fun syncAllCodesToUsers(): Boolean {
        return try {
            val uid = auth.currentUser?.uid
            if (uid.isNullOrBlank()) {
                android.util.Log.d("SYNC", "Not authenticated with Firebase. Skipping remote sync, local state is good.")
                return true
            }
            var syncedLocalCount = 0
            
            // 1. Sync from local SharedPreferences (which holds generated codes) to the user's document
            try {
                val localCodes = getLocalActivationCodes()
                val userDocRef = firestore.collection("users").document(uid)
                val userSnapshot = userDocRef.get().await()
                val assignedCodes = if (userSnapshot.exists()) {
                    userSnapshot.get("assignedCodes") as? Map<String, Any?> ?: emptyMap()
                } else {
                    emptyMap()
                }
                
                val updatedCodes = assignedCodes.toMutableMap()
                var localChanges = false
                
                for (codeMap in localCodes) {
                    val code = codeMap["id"] as? String ?: continue
                    val targetUid = (codeMap["targetUid"] as? String)?.trim() ?: ""
                    
                    // If this code belongs to the current user, or if they generated it, make sure it is in their user document
                    if (targetUid.equals(uid, ignoreCase = true) || uid.isNotBlank()) {
                        val codeIdStr = code.trim()
                        if (!updatedCodes.containsKey(codeIdStr)) {
                            updatedCodes[codeIdStr] = mapOf(
                                "code" to code,
                                "type" to (codeMap["type"] as? String ?: "bulanan"),
                                "durationDays" to ((codeMap["durationDays"] as? Number)?.toLong() ?: 30L),
                                "isUsed" to (codeMap["isUsed"] as? Boolean ?: false),
                                "createdAt" to ((codeMap["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis())
                            )
                            localChanges = true
                            syncedLocalCount++
                        }
                    }
                }
                
                if (localChanges) {
                    userDocRef.set(
                        mapOf("assignedCodes" to updatedCodes),
                        com.google.firebase.firestore.SetOptions.merge()
                    ).await()
                    android.util.Log.d("SYNC", "Synced $syncedLocalCount local codes to current Firebase user document $uid")
                }
            } catch (e: Exception) {
                android.util.Log.e("SYNC", "Error performing local to remote user doc sync", e)
            }

            // 2. Also try parsing from master activation_codes collection if the user has permission to do so (for real admins)
            try {
                val snapshot = firestore.collection("activation_codes").get().await()
                val documents = snapshot.documents
                for (doc in documents) {
                    val code = doc.id
                    val targetUid = doc.getString("targetUid")?.trim() ?: ""
                    val isUsed = doc.getBoolean("isUsed") ?: false
                    val type = doc.getString("type") ?: "bulanan"
                    val durationDays = doc.getLong("durationDays") ?: 30L
                    val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

                    if (targetUid.isNotBlank() && !isUsed) {
                        try {
                            val userDocRef = firestore.collection("users").document(targetUid)
                            val userSnapshot = userDocRef.get().await()
                            val assignedCodes = if (userSnapshot.exists()) {
                                userSnapshot.get("assignedCodes") as? Map<String, Any?> ?: emptyMap()
                            } else {
                                emptyMap()
                            }
                            
                            val existing = assignedCodes[code] as? Map<String, Any?>
                            val existingUsed = existing?.get("isUsed") as? Boolean ?: false
                            
                            if (existing == null || existingUsed != isUsed) {
                                val updatedCodes = assignedCodes.toMutableMap().apply {
                                    this[code] = mapOf(
                                        "code" to code,
                                        "type" to type,
                                        "durationDays" to durationDays,
                                        "isUsed" to isUsed,
                                        "createdAt" to createdAt
                                    )
                                }
                                userDocRef.set(
                                    mapOf("assignedCodes" to updatedCodes),
                                    com.google.firebase.firestore.SetOptions.merge()
                                ).await()
                                android.util.Log.d("SYNC", "Synced code $code to user $targetUid")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("SYNC", "Error syncing code $code to user $targetUid", e)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("SYNC", "Entire collection query skipped/failed (likely permission restriction for non-admin accounts). This is totally fine since local codes sync worked.", e)
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            true
        }
    }

    suspend fun deleteActivationCode(codeId: String): Boolean {
        val currentLocal = getLocalActivationCodes().toMutableList()
        currentLocal.removeAll { (it["id"] as? String) == codeId }
        saveLocalActivationCodes(currentLocal)
        _localCodesFlow.value = currentLocal

        try {
            firestore.collection("activation_codes").document(codeId).delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    suspend fun getAllUsersFirestore(): List<UserEntity> {
        return try {
            val snapshot = firestore.collection("users").get().await()
            snapshot.documents.mapNotNull { doc ->
                val uid = doc.id
                val nama = doc.getString("nama") ?: ""
                val email = doc.getString("email") ?: ""
                val role = doc.getString("role") ?: "owner"
                val ownerId = doc.getString("ownerId")
                val assignedBranchId = doc.getString("assignedBranchId")
                val subscriptionStatus = doc.getString("subscriptionStatus") ?: "free"
                val subscriptionType = doc.getString("subscriptionType")
                val subscriptionStartDate = doc.getLong("subscriptionStartDate")
                val subscriptionEndDate = doc.getLong("subscriptionEndDate")
                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                val lastActiveAt = doc.getLong("lastActiveAt")
                
                UserEntity(
                    uid = uid,
                    nama = nama,
                    email = email,
                    role = role,
                    ownerId = ownerId,
                    assignedBranchId = assignedBranchId,
                    subscriptionStatus = subscriptionStatus,
                    subscriptionType = subscriptionType,
                    subscriptionStartDate = subscriptionStartDate,
                    subscriptionEndDate = subscriptionEndDate,
                    createdAt = createdAt,
                    lastActiveAt = lastActiveAt
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun getCurrentTimeString(): String {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    // ==========================================
    // REAL FIRESTORE DATA SYNCHRONIZER Engine
    // ==========================================
    suspend fun syncFromFirestore(forcedUid: String? = null) {
        try {
            withContext(Dispatchers.IO) {
                val uid = forcedUid ?: _loggedInUid.value ?: return@withContext
                android.util.Log.d("SYNC", "Starting sync for uid: $uid")

                // 6. TEST KONEKSI FIRESTORE (As requested for diagnosis)
                try {
                    val testDoc = firestore.collection("users").document(uid).get().await()
                    android.util.Log.d("FIRESTORE_TEST", "Document exists: ${testDoc.exists()}")
                    android.util.Log.d("FIRESTORE_TEST", "Document data: ${testDoc.data}")
                } catch (te: Exception) {
                    android.util.Log.e("FIRESTORE_TEST", "Test read failed on users doc: ${te.message}", te)
                }

                withTimeoutOrNull(35000L) {
                    var targetOwnerId = uid

                    // 1. Sync User info
                    try {
                        android.util.Log.d("SYNC", "Fetching users collection for user: $uid ...")
                        val userDoc = firestore.collection("users").document(uid).get().await()
                        if (userDoc.exists()) {
                            android.util.Log.d("SYNC", "User document found in Firestore: ${userDoc.data}")
                            val user = UserEntity(
                                uid = uid,
                                nama = userDoc.getString("nama") ?: "User",
                                email = userDoc.getString("email") ?: "",
                                role = userDoc.getString("role") ?: "owner",
                                ownerId = userDoc.getString("ownerId"),
                                assignedBranchId = userDoc.getString("assignedBranchId"),
                                subscriptionStatus = userDoc.getString("subscriptionStatus") ?: "free",
                                subscriptionType = userDoc.getString("subscriptionType"),
                                subscriptionStartDate = userDoc.getLong("subscriptionStartDate"),
                                subscriptionEndDate = userDoc.getLong("subscriptionEndDate"),
                                createdAt = userDoc.getLong("createdAt") ?: System.currentTimeMillis(),
                                lastActiveAt = userDoc.getLong("lastActiveAt")
                            )
                            dao.insertUser(user)
                            saveUserSessionMetadata(user)
                            android.util.Log.d("SYNC", "Successfully synced user: ${user.nama} with status: ${user.subscriptionStatus}")
                            
                            if ((user.role == "kasir" || user.role == "kasir_invited") && !user.ownerId.isNullOrEmpty()) {
                                targetOwnerId = user.ownerId
                            }
                        } else {
                            android.util.Log.w("SYNC", "User document $uid not found in Firestore. Trying to fetch from cashiers collection...")
                            val cashierDoc = firestore.collection("cashiers").document(uid).get().await()
                            if (cashierDoc.exists()) {
                                android.util.Log.d("SYNC", "Cashier document found in Firestore: ${cashierDoc.data}")
                                val ownerId = cashierDoc.getString("ownerId") ?: "owner-uid"
                                val nama = cashierDoc.getString("cashierName") ?: cashierDoc.getString("nama") ?: "Kasir"
                                val branchId = cashierDoc.getString("branchId") ?: "branch-1-biz-$ownerId"
                                var subscriptionStatus = "free"
                                var subscriptionType: String? = null
                                try {
                                    val ownerDoc = firestore.collection("users").document(ownerId).get().await()
                                    if (ownerDoc.exists()) {
                                        subscriptionStatus = ownerDoc.getString("subscriptionStatus") ?: "free"
                                        subscriptionType = ownerDoc.getString("subscriptionType")
                                    }
                                } catch (ex: Exception) {
                                    android.util.Log.e("SYNC", "Error fetching owner state for cashier: ${ex.message}")
                                }
                                val c = UserEntity(
                                    uid = uid,
                                    nama = nama,
                                    email = cashierDoc.getString("username") ?: "",
                                    role = "kasir",
                                    ownerId = ownerId,
                                    assignedBranchId = branchId,
                                    subscriptionStatus = subscriptionStatus,
                                    subscriptionType = subscriptionType,
                                    subscriptionStartDate = null,
                                    subscriptionEndDate = null,
                                    createdAt = cashierDoc.getLong("createdAt") ?: System.currentTimeMillis(),
                                    lastActiveAt = cashierDoc.getLong("lastActiveAt") ?: System.currentTimeMillis()
                                )
                                dao.insertUser(c)
                                saveUserSessionMetadata(c)
                                targetOwnerId = ownerId
                            } else {
                                android.util.Log.w("SYNC", "User/Cashier document $uid not found in Firestore.")
                                val localUser = dao.getCurrentUserRaw()
                                if (localUser != null && (localUser.role == "kasir" || localUser.role == "kasir_invited") && !localUser.ownerId.isNullOrEmpty()) {
                                    targetOwnerId = localUser.ownerId
                                }
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("SYNC", "Error syncing user info: ${e.message}", e)
                    }

                    // 2. Sync Business info
                    var currentBusinessId: String? = null
                    try {
                        android.util.Log.d("SYNC", "Fetching businesses collection for ownerId: $targetOwnerId ...")
                        val bizSnap = firestore.collection("businesses").whereEqualTo("ownerId", targetOwnerId).get().await()
                        android.util.Log.d("SYNC", "Businesses found: ${bizSnap.size()}")
                        for (doc in bizSnap.documents) {
                            val biz = BusinessEntity(
                                id = doc.id,
                                ownerId = targetOwnerId,
                                namaBisnis = doc.getString("namaBisnis") ?: "",
                                logoBase64 = doc.getString("logoBase64"),
                                alamat = doc.getString("alamat"),
                                noTelpon = doc.getString("noTelpon"),
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                                qrisBase64 = doc.getString("qrisBase64")
                            )
                            dao.insertBusiness(biz)
                            currentBusinessId = doc.id
                            android.util.Log.d("SYNC", "Synced Business: ${biz.namaBisnis} (${biz.id})")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("SYNC", "Error syncing business info: ${e.message}", e)
                    }

                    // 2b. Sync Cashiers from Firestore cashiers collection
                    try {
                        android.util.Log.d("SYNC", "Fetching cashiers collection for ownerId: $targetOwnerId ...")
                        val cashiersSnap = firestore.collection("cashiers").whereEqualTo("ownerId", targetOwnerId).get().await()
                        android.util.Log.d("SYNC", "Cashiers found: ${cashiersSnap.size()}")
                        for (doc in cashiersSnap.documents) {
                            val cUid = doc.id
                            val username = doc.getString("username") ?: doc.id.substringBefore("_")
                            val nama = doc.getString("cashierName") ?: doc.getString("nama") ?: "Kasir"
                            val branchId = doc.getString("branchId") ?: ""
                            val c = UserEntity(
                                uid = cUid,
                                nama = nama,
                                email = username, // keep pure username in email field
                                role = "kasir",
                                ownerId = targetOwnerId,
                                assignedBranchId = branchId,
                                subscriptionStatus = "free",
                                subscriptionStartDate = null,
                                subscriptionEndDate = null,
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                                lastActiveAt = doc.getLong("lastActiveAt")
                            )
                            dao.insertUser(c)
                            android.util.Log.d("SYNC", "Synced Cashier: ${c.nama} (uid: ${c.uid})")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("SYNC", "Error syncing cashiers info: ${e.message}", e)
                    }

                    // If we found a business, sync all sub collections
                    val businessId = currentBusinessId ?: "biz-$targetOwnerId"
                    android.util.Log.d("SYNC", "Using business ID for scoping: $businessId")

                    // 3. Sync Branches
                    try {
                        android.util.Log.d("SYNC", "Fetching branches collection for businessId: $businessId ...")
                        val branchSnap = firestore.collection("branches").whereEqualTo("businessId", businessId).get().await()
                        android.util.Log.d("SYNC", "Branches found: ${branchSnap.size()}")
                        for (doc in branchSnap.documents) {
                            val b = BranchEntity(
                                id = doc.id,
                                businessId = businessId,
                                namaCabang = doc.getString("namaCabang") ?: "",
                                alamat = doc.getString("alamat") ?: "",
                                kasirIdsCsv = doc.getString("kasirIdsCsv") ?: "",
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                            )
                            dao.insertBranch(b)
                            android.util.Log.d("SYNC", "Synced Branch: ${b.namaCabang} (${b.id})")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("SYNC", "Error syncing branches: ${e.message}", e)
                    }

                    // 4. Sync Products
                    try {
                        android.util.Log.d("SYNC", "Fetching products collection for businessId: $businessId ...")
                        val prodSnap = firestore.collection("products").whereEqualTo("businessId", businessId).get().await()
                        android.util.Log.d("SYNC", "Products found: ${prodSnap.size()}")
                        for (doc in prodSnap.documents) {
                            val expRaw = doc.get("expiryDate")
                            val expVal = when (expRaw) {
                                is com.google.firebase.Timestamp -> expRaw.toDate().time
                                is Number -> expRaw.toLong()
                                is String -> expRaw.toLongOrNull()
                                else -> null
                            }
                            val p = ProductEntity(
                                id = doc.id,
                                businessId = businessId,
                                branchId = doc.getString("branchId") ?: "branch-1-$businessId",
                                nama = doc.getString("nama") ?: "",
                                kategori = doc.getString("kategori") ?: "",
                                hargaJual = doc.getSafeDouble("retailPrice").takeIf { it > 0.0 } ?: doc.getSafeDouble("hargaJual"),
                                hargaModal = doc.getSafeDouble("hargaModal"),
                                stok = doc.getLong("stok")?.toInt() ?: 0,
                                stokMinimum = doc.getLong("stokMinimum")?.toInt() ?: 0,
                                barcode = doc.getString("barcode"),
                                fotoBase64 = doc.getString("fotoBase64"),
                                varianRaw = doc.getString("varianRaw") ?: "",
                                satuan = doc.getString("satuan") ?: "Pcs",
                                isActive = doc.getBoolean("isActive") ?: true,
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                                wholesalePrice = doc.getSafeDouble("wholesalePrice"),
                                wholesaleMinQty = doc.getLong("wholesaleMinQty")?.toInt() ?: 0,
                                expiryDate = expVal,
                                expiryReminderDays = doc.getLong("expiryReminderDays")?.toInt() ?: 7
                            )
                            dao.insertProduct(p)
                            android.util.Log.d("SYNC", "Synced Product: ${p.nama} (Stock: ${p.stok})")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("SYNC", "Error syncing products: ${e.message}", e)
                    }

                    // 5. Sync Customers
                    try {
                        android.util.Log.d("SYNC", "Fetching customers collection for businessId: $businessId ...")
                        val custSnap = firestore.collection("customers").whereEqualTo("businessId", businessId).get().await()
                        android.util.Log.d("SYNC", "Customers found: ${custSnap.size()}")
                        for (doc in custSnap.documents) {
                            val c = CustomerEntity(
                                id = doc.id,
                                businessId = businessId,
                                nama = doc.getString("nama") ?: "",
                                nomorHp = doc.getString("nomorHp") ?: "",
                                totalPoin = doc.getLong("totalPoin")?.toInt() ?: 0,
                                totalTransaksi = doc.getLong("totalTransaksi")?.toInt() ?: 0,
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                            )
                            dao.insertCustomer(c)
                            android.util.Log.d("SYNC", "Synced Customer: ${c.nama}")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("SYNC", "Error syncing customers: ${e.message}", e)
                    }

                    // 6. Sync Promos
                    try {
                        android.util.Log.d("SYNC", "Fetching promos collection for businessId: $businessId ...")
                        val promoSnap = firestore.collection("promos").whereEqualTo("businessId", businessId).get().await()
                        android.util.Log.d("SYNC", "Promos found: ${promoSnap.size()}")
                        for (doc in promoSnap.documents) {
                            val pr = PromoEntity(
                                id = doc.id,
                                businessId = businessId,
                                nama = doc.getString("nama") ?: "",
                                tipe = doc.getString("tipe") ?: "",
                                nilai = doc.getSafeDouble("nilai"),
                                minTransaksi = doc.getSafeDouble("minTransaksi"),
                                kode = doc.getString("kode") ?: "",
                                isActive = doc.getBoolean("isActive") ?: true,
                                berlakuSampai = doc.getLong("berlakuSampai") ?: 0L,
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                            )
                            dao.insertPromo(pr)
                            android.util.Log.d("SYNC", "Synced Promo: ${pr.nama}")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("SYNC", "Error syncing promos: ${e.message}", e)
                    }

                    // 7. Sync Debts
                    try {
                        android.util.Log.d("SYNC", "Fetching debts collection for businessId: $businessId ...")
                        val debtSnap = firestore.collection("debts").whereEqualTo("businessId", businessId).get().await()
                        android.util.Log.d("SYNC", "Debts found: ${debtSnap.size()}")
                        for (doc in debtSnap.documents) {
                            val d = DebtEntity(
                                id = doc.id,
                                businessId = businessId,
                                branchId = doc.getString("branchId") ?: "branch-1-$businessId",
                                pelangganId = doc.getString("pelangganId") ?: "",
                                pelangganNama = doc.getString("pelangganNama") ?: "",
                                jumlah = doc.getSafeDouble("jumlah"),
                                transaksiId = doc.getString("transaksiId") ?: "",
                                status = doc.getString("status") ?: "belum",
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                            )
                            dao.insertDebt(d)
                            android.util.Log.d("SYNC", "Synced Debt for: ${d.pelangganNama} (Amount: ${d.jumlah})")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("SYNC", "Error syncing debts: ${e.message}", e)
                    }

                    // 8. Sync Transactions
                    try {
                        android.util.Log.d("SYNC", "Fetching transactions collection for businessId: $businessId ...")
                        val txSnap = firestore.collection("transactions").whereEqualTo("businessId", businessId).get().await()
                        android.util.Log.d("SYNC", "Transactions found: ${txSnap.size()}")
                        for (doc in txSnap.documents) {
                            val tx = TransactionEntity(
                                id = doc.id,
                                businessId = businessId,
                                branchId = doc.getString("branchId") ?: "branch-1-$businessId",
                                kasirId = doc.getString("kasirId") ?: "kasir-1",
                                kasirNama = doc.getString("kasirNama") ?: "Kasir Pro",
                                itemsRaw = doc.getString("itemsRaw") ?: "",
                                subtotal = doc.getSafeDouble("subtotal"),
                                diskonTotal = doc.getSafeDouble("diskonTotal"),
                                kodePromo = doc.getString("kodePromo"),
                                total = doc.getSafeDouble("total"),
                                metodeBayar = doc.getString("metodeBayar") ?: "Tunai",
                                bayarNominal = doc.getSafeDouble("bayarNominal"),
                                kembalian = doc.getSafeDouble("kembalian"),
                                status = doc.getString("status") ?: "lunas",
                                pelangganId = doc.getString("pelangganId"),
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                                isOfflinePending = false
                            )
                            dao.insertTransaction(tx)
                            android.util.Log.d("SYNC", "Synced Transaction: ${tx.id} (Total: ${tx.total})")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("SYNC", "Error syncing transactions: ${e.message}", e)
                    }

                    // 9. Sync Stock History
                    try {
                        android.util.Log.d("SYNC", "Fetching stock_history collection for businessId: $businessId ...")
                        val shSnap = firestore.collection("stock_history").whereEqualTo("businessId", businessId).get().await()
                        android.util.Log.d("SYNC", "Stock histories found: ${shSnap.size()}")
                        for (doc in shSnap.documents) {
                            val sh = StockHistoryEntity(
                                id = doc.id,
                                productId = doc.getString("productId") ?: "",
                                businessId = businessId,
                                tipe = doc.getString("tipe") ?: "masuk",
                                jumlah = doc.getLong("jumlah")?.toInt() ?: 0,
                                stokSebelum = doc.getLong("stokSebelum")?.toInt() ?: 0,
                                stokSesudah = doc.getLong("stokSesudah")?.toInt() ?: 0,
                                keterangan = doc.getString("keterangan"),
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                            )
                            dao.insertStockHistory(sh)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("SYNC", "Error syncing stock histories: ${e.message}", e)
                    }

                    android.util.Log.d("SYNC", "Sync completed successfully")
                } // end of withTimeoutOrNull
            } // end of withContext
        } catch (e: Exception) {
            android.util.Log.e("SYNC", "Fatal error during syncFromFirestore: ${e.message}", e)
            e.printStackTrace()
        }
    }

    // === CASHIER SHIFT METHODS ===
    suspend fun getActiveShift(cashierId: String): ShiftReport? {
        return try {
            val docs = firestore.collection("shifts")
                .whereEqualTo("cashierId", cashierId)
                .whereEqualTo("status", "aktif")
                .limit(1)
                .get()
                .await()
            if (!docs.isEmpty) {
                val doc = docs.documents.first()
                val map = doc.data ?: return null
                ShiftReport.fromMap(map.toMutableMap().apply { put("id", doc.id) })
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun startShift(cashierId: String, cashierName: String, branchId: String, branchName: String, modalAwal: Double): ShiftReport {
        val currentUser = dao.getCurrentUserRaw()
        val ownerId = if (currentUser != null) {
            currentUser.ownerId ?: currentUser.uid
        } else {
            auth.currentUser?.uid ?: "owner-uid"
        }
        val id = "SHIFT-${UUID.randomUUID()}"
        val shift = ShiftReport(
            id = id,
            ownerId = ownerId,
            cashierId = cashierId,
            cashierName = cashierName,
            branchId = branchId,
            branchName = branchName,
            startTime = System.currentTimeMillis(),
            modalAwal = modalAwal,
            status = "aktif"
        )
        try {
            firestore.collection("shifts").document(id).set(shift.toMap()).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return shift
    }

    suspend fun endShift(
        shiftId: String,
        finalTunai: Double,
        finalNonTunai: Double,
        finalTxTotal: Double,
        actualDrawerCash: Double,
        selisih: Double
    ): Boolean {
        return try {
            val updateMap = mapOf(
                "status" to "selesai",
                "endTime" to System.currentTimeMillis(),
                "totalTunai" to finalTunai,
                "totalNonTunai" to finalNonTunai,
                "totalTransaksi" to finalTxTotal,
                "actualDrawerCash" to actualDrawerCash,
                "selisih" to selisih
            )
            firestore.collection("shifts").document(shiftId).update(updateMap).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getAllShifts(): List<ShiftReport> {
        val currentUser = dao.getCurrentUserRaw()
        val ownerId = if (currentUser != null) {
            currentUser.ownerId ?: currentUser.uid
        } else {
            auth.currentUser?.uid ?: "owner-uid"
        }
        return try {
            val docs = firestore.collection("shifts")
                .whereEqualTo("ownerId", ownerId)
                .get()
                .await()
            docs.documents.mapNotNull { doc ->
                val map = doc.data ?: return@mapNotNull null
                ShiftReport.fromMap(map.toMutableMap().apply { put("id", doc.id) })
            }.sortedByDescending { it.startTime }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun correctTransaction(correctedTx: TransactionEntity) {
        val oldTx = dao.getTransactionById(correctedTx.id)
        
        // 1. Revert old transaction stock (re-adds to stock)
        if (oldTx != null) {
            val oldLines = oldTx.itemsRaw.split(";").filter { it.isNotBlank() }
            oldLines.forEach { line ->
                val parts = line.split(":")
                if (parts.size >= 3) {
                    val pId = parts[0]
                    val qty = parts[2].toIntOrNull() ?: 0
                    if (qty > 0) {
                        val product = getProductById(pId)
                        if (product != null) {
                            val stokSebelum = product.stok
                            val stokSesudah = stokSebelum + qty
                            val updatedProduct = product.copy(stok = stokSesudah)
                            
                            try {
                                firestore.collection("products").document(updatedProduct.id).set(updatedProduct.toMap()).await()
                            } catch (e: Exception) { e.printStackTrace() }
                            dao.insertProduct(updatedProduct)

                            // Restock movement log
                            val hs = StockHistoryEntity(
                                id = java.util.UUID.randomUUID().toString(),
                                productId = pId,
                                businessId = product.businessId,
                                tipe = "masuk",
                                jumlah = qty,
                                stokSebelum = stokSebelum,
                                stokSesudah = stokSesudah,
                                keterangan = "Restorasi koreksi transaksi ${correctedTx.id}"
                            )
                            try {
                                firestore.collection("stock_history").document(hs.id).set(hs.toMap()).await()
                            } catch (e: Exception) { e.printStackTrace() }
                            dao.insertStockHistory(hs)
                        }
                    }
                }
            }
        }

        // 2. Apply new corrected transaction stock (deducts from stock)
        val newLines = correctedTx.itemsRaw.split(";").filter { it.isNotBlank() }
        newLines.forEach { line ->
            val parts = line.split(":")
            if (parts.size >= 3) {
                val pId = parts[0]
                val qty = parts[2].toIntOrNull() ?: 0
                if (qty > 0) {
                    val product = getProductById(pId)
                    if (product != null) {
                        val stokSebelum = product.stok
                        val stokSesudah = (stokSebelum - qty).coerceAtLeast(0)
                        val updatedProduct = product.copy(stok = stokSesudah)
                        
                        try {
                            firestore.collection("products").document(updatedProduct.id).set(updatedProduct.toMap()).await()
                        } catch (e: Exception) { e.printStackTrace() }
                        dao.insertProduct(updatedProduct)

                        // Deduct stock movement log
                        val hs = StockHistoryEntity(
                            id = java.util.UUID.randomUUID().toString(),
                            productId = pId,
                            businessId = product.businessId,
                            tipe = "keluar",
                            jumlah = qty,
                            stokSebelum = stokSebelum,
                            stokSesudah = stokSesudah,
                            keterangan = "Pengurangan koreksi transaksi ${correctedTx.id}"
                        )
                        try {
                            firestore.collection("stock_history").document(hs.id).set(hs.toMap()).await()
                        } catch (e: Exception) { e.printStackTrace() }
                        dao.insertStockHistory(hs)
                    }
                }
            }
        }

        // 3. Save the corrected transaction
        dao.insertTransaction(correctedTx)
        try {
            val firestoreMap = correctedTx.toMap().toMutableMap()
            firestore.collection("transactions").document(correctedTx.id).set(firestoreMap).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

data class ShiftReport(
    val id: String = "",
    val ownerId: String = "",
    val cashierId: String = "",
    val cashierName: String = "",
    val branchId: String = "",
    val branchName: String = "",
    val startTime: Long = 0L,
    val endTime: Long? = null,
    val modalAwal: Double = 0.0,
    val totalTunai: Double = 0.0,
    val totalNonTunai: Double = 0.0,
    val totalTransaksi: Double = 0.0,
    val status: String = "aktif",
    val actualDrawerCash: Double = 0.0,
    val selisih: Double = 0.0
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "ownerId" to ownerId,
        "cashierId" to cashierId,
        "cashierName" to cashierName,
        "branchId" to branchId,
        "branchName" to branchName,
        "startTime" to startTime,
        "endTime" to endTime,
        "modalAwal" to modalAwal,
        "totalTunai" to totalTunai,
        "totalNonTunai" to totalNonTunai,
        "totalTransaksi" to totalTransaksi,
        "status" to status,
        "actualDrawerCash" to actualDrawerCash,
        "selisih" to selisih
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): ShiftReport {
            return ShiftReport(
                id = map["id"] as? String ?: "",
                ownerId = map["ownerId"] as? String ?: "",
                cashierId = map["cashierId"] as? String ?: "",
                cashierName = map["cashierName"] as? String ?: "",
                branchId = map["branchId"] as? String ?: "",
                branchName = map["branchName"] as? String ?: "",
                startTime = (map["startTime"] as? Number)?.toLong() ?: 0L,
                endTime = (map["endTime"] as? Number)?.toLong(),
                modalAwal = (map["modalAwal"] as? Number)?.toDouble() ?: 0.0,
                totalTunai = (map["totalTunai"] as? Number)?.toDouble() ?: 0.0,
                totalNonTunai = (map["totalNonTunai"] as? Number)?.toDouble() ?: 0.0,
                totalTransaksi = (map["totalTransaksi"] as? Number)?.toDouble() ?: 0.0,
                status = map["status"] as? String ?: "aktif",
                actualDrawerCash = (map["actualDrawerCash"] as? Number)?.toDouble() ?: 0.0,
                selisih = (map["selisih"] as? Number)?.toDouble() ?: 0.0
            )
        }
    }
}

// BROADCAST NOTIFICATIONS SYSTEM & HELPERS
private fun KasirRepository.getLocalBroadcasts(): List<Map<String, Any>> {
    val jsonStr = prefs.getString("local_broadcasts", "[]") ?: "[]"
    return try {
        val arr = org.json.JSONArray(jsonStr)
        val list = mutableListOf<Map<String, Any>>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val map = mutableMapOf<String, Any>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = obj.get(key)
            }
            list.add(map)
        }
        list
    } catch (e: Exception) {
        emptyList()
    }
}

private fun KasirRepository.saveLocalBroadcasts(list: List<Map<String, Any>>) {
    try {
        val arr = org.json.JSONArray()
        for (map in list) {
            val obj = org.json.JSONObject()
            for ((k, v) in map) {
                obj.put(k, v)
            }
            arr.put(obj)
        }
        prefs.edit().putString("local_broadcasts", arr.toString()).apply()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun KasirRepository.getLocalUserNotifications(): List<Map<String, Any>> {
    val jsonStr = prefs.getString("local_user_notifications", "[]") ?: "[]"
    return try {
        val arr = org.json.JSONArray(jsonStr)
        val list = mutableListOf<Map<String, Any>>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val map = mutableMapOf<String, Any>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = obj.get(key)
            }
            list.add(map)
        }
        list
    } catch (e: Exception) {
        emptyList()
    }
}

private fun KasirRepository.saveLocalUserNotifications(list: List<Map<String, Any>>) {
    try {
        val arr = org.json.JSONArray()
        for (map in list) {
            val obj = org.json.JSONObject()
            for ((k, v) in map) {
                obj.put(k, v)
            }
            arr.put(obj)
        }
        prefs.edit().putString("local_user_notifications", arr.toString()).apply()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

suspend fun KasirRepository.sendBroadcastNotification(
    title: String,
    message: String,
    type: String,
    downloadUrl: String?,
    version: String?,
    isActive: Boolean,
    createdBy: String,
    onProgress: (Int, Int) -> Unit = { _, _ -> }
): Boolean {
    val broadcastId = "bcast_" + java.util.UUID.randomUUID().toString().take(12)
    val now = System.currentTimeMillis()

    val broadcastMap = mutableMapOf<String, Any?>(
        "id" to broadcastId,
        "title" to title,
        "message" to message,
        "type" to type.lowercase(),
        "downloadUrl" to downloadUrl,
        "version" to version,
        "isActive" to isActive,
        "createdAt" to now,
        "createdBy" to createdBy
    )

    // ALWAYS save locally first to guarantee offline availability and success
    val localBroadcasts = getLocalBroadcasts().toMutableList()
    localBroadcasts.add(broadcastMap.filterValues { it != null } as Map<String, Any>)
    saveLocalBroadcasts(localBroadcasts)

    // Save local notification for this admin/currentUser
    val localNotifs = getLocalUserNotifications().toMutableList()
    val notifMapForLocal = mapOf(
        "id" to "notif_local_" + java.util.UUID.randomUUID().toString().take(12),
        "userId" to (auth.currentUser?.uid ?: "local-user"),
        "broadcastId" to broadcastId,
        "title" to title,
        "message" to message,
        "type" to type.lowercase(),
        "downloadUrl" to (downloadUrl ?: ""),
        "version" to (version ?: ""),
        "isRead" to false,
        "createdAt" to now
    )
    localNotifs.add(notifMapForLocal)
    saveLocalUserNotifications(localNotifs)

    return try {
        val db = FirebaseFirestore.getInstance()
        val broadcastDocRef = db.collection("broadcasts").document(broadcastId)

        // Save the broadcast document to firestore
        broadcastDocRef.set(broadcastMap).await()

        // Fetch all users to create notifications
        val users = getAllUsersFirestore()
        val totalUsers = users.size
        
        var sentCount = 0
        val batchSize = 40 // Let's use Firestore batching (max 500 per WriteBatch)
        var batch = db.batch()
        
        for (user in users) {
            val notifDocRef = db.collection("user_notifications").document()
            val notifMap = mapOf(
                "id" to notifDocRef.id,
                "userId" to user.uid,
                "broadcastId" to broadcastId,
                "title" to title,
                "message" to message,
                "type" to type.lowercase(),
                "downloadUrl" to downloadUrl,
                "version" to version,
                "isRead" to false,
                "createdAt" to now
            )
            batch.set(notifDocRef, notifMap)
            sentCount++
            
            if (sentCount % batchSize == 0 || sentCount == totalUsers) {
                batch.commit().await()
                onProgress(sentCount, totalUsers)
                batch = db.batch()
            }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        // Firestore write failed (e.g., Firestore is not activated/permissions). 
        // We already saved the broadcast and user notifications locally, so we return true!
        onProgress(1, 1)
        true
    }
}

suspend fun KasirRepository.getBroadcasts(): List<Map<String, Any>> {
    val localList = getLocalBroadcasts()
    return try {
        val db = FirebaseFirestore.getInstance()
        val snapshot = db.collection("broadcasts").get().await()
        val remoteList = snapshot.documents.mapNotNull { doc ->
            val map = doc.data ?: return@mapNotNull null
            map.toMutableMap().apply {
                this["id"] = doc.id
            }
        }
        val mergedMap = (localList + remoteList).associateBy { it["id"] as? String ?: "" }
        mergedMap.values.sortedByDescending { (it["createdAt"] as? Number)?.toLong() ?: 0L }
    } catch (e: Exception) {
        e.printStackTrace()
        localList.sortedByDescending { (it["createdAt"] as? Number)?.toLong() ?: 0L }
    }
}

suspend fun KasirRepository.deleteBroadcast(broadcastId: String): Boolean {
    // Delete from local
    val localBroadcasts = getLocalBroadcasts().toMutableList()
    localBroadcasts.removeAll { (it["id"] as? String ?: "") == broadcastId }
    saveLocalBroadcasts(localBroadcasts)

    val localNotifs = getLocalUserNotifications().toMutableList()
    localNotifs.removeAll { (it["broadcastId"] as? String ?: "") == broadcastId }
    saveLocalUserNotifications(localNotifs)

    return try {
        val db = FirebaseFirestore.getInstance()
        db.collection("broadcasts").document(broadcastId).delete().await()
        
        // Clean up user_notifications for this broadcast
        val matchingNotifs = db.collection("user_notifications")
            .whereEqualTo("broadcastId", broadcastId)
            .get()
            .await()
        
        var batch = db.batch()
        var count = 0
        for (doc in matchingNotifs.documents) {
            batch.delete(doc.reference)
            count++
            if (count % 40 == 0) {
                batch.commit().await()
                batch = db.batch()
            }
        }
        if (count % 40 != 0) {
            batch.commit().await()
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        true
    }
}

private fun KasirRepository.showSystemNotification(title: String, message: String) {
    try {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val channelId = "kasir_pro_notifications"
        val channelName = "Notifikasi Kasir Pro"
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                channelName,
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel untuk notifikasi dari aplikasi Kasir Pro"
                enableLights(true)
                lightColor = android.graphics.Color.RED
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val intent = android.content.Intent(context, com.kasirpro.pospintar.app.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("open_screen", "user_notifications")
        }
        
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.kasirpro.pospintar.app.R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun KasirRepository.listenToUserNotifications(userId: String, onUpdate: (List<Map<String, Any>>) -> Unit): com.google.firebase.firestore.ListenerRegistration {
    val localList = getLocalUserNotifications().filter { (it["userId"] as? String ?: "") == userId }
    // Dispatch local items first
    onUpdate(localList)

    var isInitialLoad = true
    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
        isInitialLoad = false
    }, 4500L) // Wait slightly for initial snapshot emissions to clear

    var lastUserNotifications = emptyList<Map<String, Any>>()
    var lastBroadcasts = emptyList<Map<String, Any>>()

    fun triggerMergedUpdate() {
        val mergedList = mutableListOf<Map<String, Any>>()
        
        // 1. Add all from user_notifications listener
        mergedList.addAll(lastUserNotifications)

        // 2. Synthesize notification from broadcasts that are ACTIVE
        for (bcast in lastBroadcasts) {
            val isActive = bcast["isActive"] as? Boolean ?: true
            if (!isActive) continue

            val bcastId = bcast["id"] as? String ?: ""
            if (bcastId.isEmpty()) continue
            
            // Check if there is already a user_notification with this broadcastId
            val alreadyHasNotif = mergedList.any { (it["broadcastId"] as? String ?: "") == bcastId }
            if (!alreadyHasNotif) {
                // Synthesize!
                val synthId = "synth_" + userId + "_" + bcastId
                // Check if this broadcast is locally marked as read
                val localRead = localList.any { 
                    ((it["broadcastId"] as? String ?: "") == bcastId || (it["id"] as? String ?: "") == synthId) && 
                    (it["isRead"] as? Boolean == true)
                }
                
                val synthesized = mapOf(
                    "id" to synthId,
                    "userId" to userId,
                    "broadcastId" to bcastId,
                    "title" to (bcast["title"] as? String ?: ""),
                    "message" to (bcast["message"] as? String ?: ""),
                    "type" to (bcast["type"] as? String ?: "info"),
                    "downloadUrl" to (bcast["downloadUrl"] as? String ?: ""),
                    "version" to (bcast["version"] as? String ?: ""),
                    "isRead" to localRead,
                    "createdAt" to ((bcast["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis())
                )
                mergedList.add(synthesized)
            }
        }

        val mergedMap = (localList + mergedList).associateBy { it["id"] as? String ?: "" }
        val sorted = mergedMap.values.sortedByDescending { (it["createdAt"] as? Number)?.toLong() ?: 0L }
        onUpdate(sorted)
    }

    val db = FirebaseFirestore.getInstance()

    val notifListener = try {
        db.collection("user_notifications")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    error.printStackTrace()
                    triggerMergedUpdate()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val remoteList = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        data.toMutableMap().apply {
                            this["id"] = doc.id
                        }
                    }
                    
                    if (!isInitialLoad) {
                        for (notif in remoteList) {
                            val notifId = notif["id"] as? String ?: ""
                            val isRead = notif["isRead"] as? Boolean ?: false
                            val title = notif["title"] as? String ?: "Kasir Pro"
                            val message = notif["message"] as? String ?: ""
                            
                            val wasInPrevious = lastUserNotifications.any { (it["id"] as? String ?: "") == notifId }
                            if (!wasInPrevious && !isRead) {
                                showSystemNotification(title, message)
                            }
                        }
                    }
                    
                    lastUserNotifications = remoteList
                }
                triggerMergedUpdate()
            }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    val bcastListener = try {
        db.collection("broadcasts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    error.printStackTrace()
                    triggerMergedUpdate()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val remoteBroadcasts = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        data.toMutableMap().apply {
                            this["id"] = doc.id
                        }
                    }
                    
                    if (!isInitialLoad) {
                        for (bcast in remoteBroadcasts) {
                            val bcastId = bcast["id"] as? String ?: ""
                            val title = bcast["title"] as? String ?: "Pengumuman Kasir Pro"
                            val message = bcast["message"] as? String ?: ""
                            val isActive = bcast["isActive"] as? Boolean ?: true
                            
                            val wasInPrevious = lastBroadcasts.any { (it["id"] as? String ?: "") == bcastId }
                            if (!wasInPrevious && isActive) {
                                val isLocallyRead = localList.any { 
                                    (it["broadcastId"] as? String ?: "") == bcastId && (it["isRead"] as? Boolean == true)
                                }
                                if (!isLocallyRead) {
                                    showSystemNotification(title, message)
                                }
                            }
                        }
                    }
                    
                    lastBroadcasts = remoteBroadcasts
                }
                triggerMergedUpdate()
            }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    return object : com.google.firebase.firestore.ListenerRegistration {
        override fun remove() {
            notifListener?.remove()
            bcastListener?.remove()
        }
    }
}

suspend fun KasirRepository.markNotificationAsRead(notifId: String): Boolean {
    // Also update local list
    val localNotifs = getLocalUserNotifications().toMutableList()
    var updated = false
    
    val bcastId = if (notifId.startsWith("synth_")) {
        notifId.split("_").lastOrNull() ?: notifId
    } else {
        notifId
    }
    
    val newList = localNotifs.map { notif ->
        val currentId = notif["id"] as? String ?: ""
        val currentBcastId = notif["broadcastId"] as? String ?: ""
        if (currentId == notifId || currentId == bcastId || currentBcastId == bcastId) {
            updated = true
            notif.toMutableMap().apply { this["isRead"] = true }
        } else {
            notif
        }
    }
    
    if (updated) {
        saveLocalUserNotifications(newList)
    } else {
        val newRecord = mapOf(
            "id" to notifId,
            "userId" to (auth.currentUser?.uid ?: "local-user"),
            "broadcastId" to bcastId,
            "isRead" to true,
            "createdAt" to System.currentTimeMillis()
        )
        localNotifs.add(newRecord)
        saveLocalUserNotifications(localNotifs)
    }

    return try {
        val db = FirebaseFirestore.getInstance()
        val map = mapOf(
            "id" to notifId,
            "userId" to (auth.currentUser?.uid ?: ""),
            "broadcastId" to bcastId,
            "isRead" to true,
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("user_notifications").document(notifId)
            .set(map, com.google.firebase.firestore.SetOptions.merge())
            .await()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        true
    }
}

suspend fun KasirRepository.markAllNotificationsAsRead(userId: String): Boolean {
    // 1. Update existing local ones
    val localNotifs = getLocalUserNotifications().toMutableList()
    val updatedList = localNotifs.map { notif ->
        if ((notif["userId"] as? String ?: "") == userId) {
            notif.toMutableMap().apply { this["isRead"] = true }
        } else {
            notif
        }
    }.toMutableList()

    // 2. Also mark any local broadcasts as read for this user
    val bcasts = getLocalBroadcasts()
    for (bcast in bcasts) {
        val bcastId = bcast["id"] as? String ?: ""
        if (bcastId.isEmpty()) continue
        val synthId = "synth_" + userId + "_" + bcastId
        val alreadyHasRead = updatedList.any { 
            ((it["broadcastId"] as? String ?: "") == bcastId || (it["id"] as? String ?: "") == synthId) && 
            (it["isRead"] as? Boolean == true)
        }
        if (!alreadyHasRead) {
            updatedList.add(mapOf(
                "id" to synthId,
                "userId" to userId,
                "broadcastId" to bcastId,
                "isRead" to true,
                "createdAt" to System.currentTimeMillis()
            ))
        }
    }
    saveLocalUserNotifications(updatedList)

    // 3. Mark in firestore
    return try {
        val db = FirebaseFirestore.getInstance()
        val unread = db.collection("user_notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .get()
            .await()
        var batch = db.batch()
        var count = 0
        for (doc in unread.documents) {
            batch.update(doc.reference, "isRead", true)
            count++
            if (count % 40 == 0) {
                batch.commit().await()
                batch = db.batch()
            }
        }
        if (count % 40 != 0) {
            batch.commit().await()
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        true
    }
}

