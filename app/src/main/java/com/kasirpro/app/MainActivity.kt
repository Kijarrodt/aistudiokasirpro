package com.kasirpro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kasirpro.app.ui.theme.MyApplicationTheme
import com.kasirpro.app.ui.theme.OrangePrimary
import com.kasirpro.app.ui.viewmodel.KasirViewModel
import com.kasirpro.app.ui.screens.*
import com.kasirpro.app.util.t

class MainActivity : ComponentActivity() {
    private val viewModel: KasirViewModel by viewModels()

    private fun hasNewVersion(current: String, latest: String): Boolean {
        val curParts = current.split(".").mapNotNull { it.toIntOrNull() }
        val latParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val maxLen = maxOf(curParts.size, latParts.size)
        for (i in 0 until maxLen) {
            val curVal = curParts.getOrElse(i) { 0 }
            val latVal = latParts.getOrElse(i) { 0 }
            if (latVal > curVal) return true
            if (curVal > latVal) return false
        }
        return false
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        val screenToOpen = intent.getStringExtra("open_screen")
        if (screenToOpen != null) {
            viewModel.activeScreen.value = screenToOpen
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request launcher for camera and notification permissions when starting (Modern Android popup)
        val permissionsToRequest = mutableListOf(android.Manifest.permission.CAMERA)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        try {
            requestPermissions(permissionsToRequest.toTypedArray(), 101)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val screenToOpen = intent?.getStringExtra("open_screen")
        if (screenToOpen != null) {
            viewModel.activeScreen.value = screenToOpen
        }

        enableEdgeToEdge()
        setContent {
            val isDarkTheme by viewModel.isDarkMode.collectAsState()
            
            val context = androidx.compose.ui.platform.LocalContext.current
            var showUpdateDialog by remember { mutableStateOf(false) }
            var updateDownloadUrl by remember { mutableStateOf("") }
            
            LaunchedEffect(Unit) {
                try {
                    viewModel.billingManager.queryAndValidateActivePurchases()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                try {
                    val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    firestore.collection("app_config")
                        .document("version")
                        .get()
                        .addOnSuccessListener { doc ->
                            if (doc != null && doc.exists()) {
                                val latestVersion = doc.getString("latestVersion") ?: ""
                                val downloadUrl = doc.getString("downloadUrl") ?: ""
                                
                                val currentVersion = try {
                                    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
                                } catch (e: Exception) {
                                    "1.0"
                                }
                                
                                if (latestVersion.isNotBlank() && hasNewVersion(currentVersion, latestVersion)) {
                                    updateDownloadUrl = downloadUrl
                                    showUpdateDialog = true
                                }
                            }
                        }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val activeScreenState by viewModel.activeScreen.collectAsState()
                    val currUser by viewModel.currentUser.collectAsState()
                    val isKasir = currUser?.role == "kasir"

                    // Intercept back-button clicks to handle in-app screen stacks
                    val enabledBack = activeScreenState !in listOf("splash", "login", "onboarding") &&
                            !(isKasir && activeScreenState == "cashier") &&
                            !(!isKasir && activeScreenState == "home")

                    androidx.activity.compose.BackHandler(enabled = enabledBack) {
                        when (activeScreenState) {
                            "register", "forgot_password", "setup_toko" -> {
                                viewModel.activeScreen.value = "login"
                            }
                            "premium_pricing" -> {
                                viewModel.activeScreen.value = "settings"
                            }
                            else -> {
                                if (isKasir) {
                                    viewModel.activeScreen.value = "cashier"
                                } else {
                                    viewModel.activeScreen.value = "home"
                                }
                            }
                        }
                    }

                    val customNotificationText by viewModel.customNotification.collectAsState()

                    Scaffold(
                        contentWindowInsets = WindowInsets(0.dp),
                        bottomBar = {
                            val currUser by viewModel.currentUser.collectAsState()
                            val isKasir = currUser?.role == "kasir"
                            val allowedScreens = if (isKasir) {
                                listOf("cashier", "manage", "settings")
                            } else {
                                listOf("home", "cashier", "manage", "premium", "settings")
                            }

                            if (activeScreenState in allowedScreens) {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ) {
                                    if (!isKasir) {
                                        NavigationBarItem(
                                            selected = activeScreenState == "home",
                                            onClick = { viewModel.activeScreen.value = "home" },
                                            icon = { Icon(Icons.Default.Home, contentDescription = t("Beranda")) },
                                            label = { Text(t("Beranda")) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = OrangePrimary,
                                                selectedTextColor = OrangePrimary,
                                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                            )
                                        )
                                    }
                                    
                                    NavigationBarItem(
                                        selected = activeScreenState == "cashier",
                                        onClick = { viewModel.activeScreen.value = "cashier" },
                                        icon = { Icon(Icons.Default.PointOfSale, contentDescription = t("Kasir")) },
                                        label = { Text(t("Kasir")) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = OrangePrimary,
                                            selectedTextColor = OrangePrimary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )
                                    
                                    NavigationBarItem(
                                        selected = activeScreenState == "manage",
                                        onClick = { viewModel.activeScreen.value = "manage" },
                                        icon = {
                                            val expiryCount by viewModel.totalExpiryWarningsCount.collectAsState()
                                            BadgedBox(
                                                badge = {
                                                    if (expiryCount > 0) {
                                                        Badge(
                                                            containerColor = Color(0xFFD32F2F),
                                                            contentColor = Color.White
                                                        ) {
                                                            Text(expiryCount.toString(), modifier = Modifier.testTag("expiry_badge_count"))
                                                        }
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.Inventory, contentDescription = t("Stok"))
                                            }
                                        },
                                        label = { Text(t("Stok")) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = OrangePrimary,
                                            selectedTextColor = OrangePrimary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )
                                    
                                    if (!isKasir) {
                                        NavigationBarItem(
                                            selected = activeScreenState == "premium",
                                            onClick = { viewModel.activeScreen.value = "premium" },
                                            icon = { Icon(Icons.Default.Stars, contentDescription = t("Premium")) },
                                            label = { Text(t("Premium")) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = OrangePrimary,
                                                selectedTextColor = OrangePrimary,
                                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                            )
                                        )
                                    }
                                    
                                    NavigationBarItem(
                                        selected = activeScreenState == "settings",
                                        onClick = { viewModel.activeScreen.value = "settings" },
                                        icon = { Icon(Icons.Default.Settings, contentDescription = t("Pengaturan")) },
                                        label = { Text(t("Pengaturan")) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = OrangePrimary,
                                            selectedTextColor = OrangePrimary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    ) { pad ->
                        Box(modifier = Modifier.fillMaxSize().padding(pad)) {
                            // Current screen logic
                            when (activeScreenState) {
                                "splash" -> SplashScreen(viewModel)
                                "onboarding" -> OnboardingScreen(viewModel)
                                "login" -> LoginScreen(viewModel)
                                "register" -> RegisterScreen(viewModel)
                                "forgot_password" -> ForgotPasswordScreen(viewModel)
                                "setup_toko" -> SetupTokoScreen(viewModel)
                                "home" -> DashboardScreen(viewModel)
                                "cashier" -> CashierScreen(viewModel)
                                "manage" -> ProductsStokScreen(viewModel)
                                "premium" -> PremiumScreens(viewModel)
                                "settings" -> BackupSettingsScreen(viewModel)
                                "premium_pricing" -> BackupSettingsScreen(viewModel) // embedded view inside BackupSettingsScreen switcher
                                "user_notifications" -> com.kasirpro.app.ui.screens.UserNotificationsScreen(viewModel, onBack = { viewModel.activeScreen.value = "home" })
                            }

                            // Slid-in / overlapping Custom Photo Toast/Notification
                            customNotificationText?.let { msg ->
                                val lowerMsg = msg.lowercase()
                                val isSuccess = lowerMsg.contains("sukses") || lowerMsg.contains("berhasil")
                                val isFailed = lowerMsg.contains("gagal") || lowerMsg.contains("error") || lowerMsg.contains("salah") || lowerMsg.contains("ditolak")
                                val isWarning = lowerMsg.contains("peringatan") || lowerMsg.contains("perhatian") || lowerMsg.contains("wajib") || lowerMsg.contains("kurang") || lowerMsg.contains("tidak cukup")
                                val isCopy = lowerMsg.contains("salin") || lowerMsg.contains("copy") || lowerMsg.contains("clipboard")

                                val accentColor = when {
                                    isSuccess -> Color(0xFF10B981) // Emerald Green for success
                                    isFailed -> Color(0xFFEF4444) // Crimson Red for failure
                                    isWarning -> Color(0xFFF59E0B) // Amber for warnings
                                    isCopy -> Color(0xFF3B82F6) // Dynamic Blue for copying properties
                                    else -> OrangePrimary // Default brand orange
                                }

                                val statusIcon = when {
                                    isSuccess -> Icons.Default.CheckCircle
                                    isFailed -> Icons.Default.Error
                                    isWarning -> Icons.Default.Warning
                                    isCopy -> Icons.Default.ContentCopy
                                    else -> Icons.Default.Info
                                }

                                val cleanMsg = msg
                                    .replace("Sukses:", "")
                                    .replace("Gagal:", "")
                                    .replace("Peringatan:", "")
                                    .replace("Salin:", "")
                                    .trim()

                                Card(
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // Deep Slate Dark 900
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, accentColor),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.TopCenter)
                                        .padding(horizontal = 16.dp, vertical = 24.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(14.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // App brand cash register icon
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(
                                                    color = Color.White.copy(alpha = 0.05f),
                                                    shape = androidx.compose.foundation.shape.CircleShape
                                                )
                                                .padding(2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            androidx.compose.foundation.Image(
                                                painter = androidx.compose.ui.res.painterResource(
                                                    id = com.kasirpro.app.R.drawable.ic_kasir_logo_1780920830633
                                                ),
                                                contentDescription = "Kasir Pro Brand Logo",
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // Notification message text
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            val titleText = when {
                                                isSuccess -> "SUKSES"
                                                isFailed -> "GAGAL"
                                                isWarning -> "PERINGATAN"
                                                isCopy -> "DISALIN"
                                                else -> "INFO"
                                            }
                                            Text(
                                                text = titleText,
                                                color = accentColor,
                                                fontSize = 11.sp,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            )
                                            Spacer(modifier = Modifier.height(1.dp))
                                            Text(
                                                text = cleanMsg,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                                lineHeight = 18.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        // Dynamic Status Icon Indicating Success, Warning, Error, etc.
                                        Icon(
                                            imageVector = statusIcon,
                                            contentDescription = null,
                                            tint = accentColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Global validation popover modal to notify about Free Limitations and trigger Upgrades
                    val limitMsg by viewModel.showLimitPopup.collectAsState()
                    if (limitMsg != null) {
                        AlertDialog(
                            onDismissRequest = { viewModel.showLimitPopup.value = null },
                            icon = { Icon(Icons.Default.NewReleases, contentDescription = null, tint = OrangePrimary) },
                            title = { Text("Batas Limit Akun Gratis") },
                            text = { Text(limitMsg!!) },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        viewModel.showLimitPopup.value = null
                                        viewModel.activeScreen.value = "premium_pricing"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                                ) {
                                    Text("Upgrade Sekarang")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { viewModel.showLimitPopup.value = null }) {
                                    Text("Nanti Saja")
                                }
                            }
                        )
                    }

                    val showUpgrade by viewModel.showUpgradePopup.collectAsState()
                    val upgradeMsg by viewModel.upgradeMessage.collectAsState()
                    if (showUpgrade) {
                        AlertDialog(
                            onDismissRequest = { viewModel.showUpgradePopup.value = false },
                            icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = OrangePrimary) },
                            title = { Text("Fitur Terkunci", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                            text = { Text(upgradeMsg) },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        viewModel.showUpgradePopup.value = false
                                        viewModel.activeScreen.value = "premium_pricing"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                                ) {
                                    Text("Lihat Paket")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { viewModel.showUpgradePopup.value = false }) {
                                    Text("Nanti")
                                }
                            }
                        )
                    }

                    if (showUpdateDialog && updateDownloadUrl.isNotBlank()) {
                        AlertDialog(
                            onDismissRequest = { showUpdateDialog = false },
                            title = { Text("Pembaruan Tersedia", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                            text = { Text("Versi terbaru aplikasi sudah tersedia. Update sekarang untuk mendapatkan fitur terbaru dan perbaikan bug.") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        try {
                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(updateDownloadUrl))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(context, "Tidak dapat membuka link download", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                        showUpdateDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                                ) {
                                    Text("Update Sekarang")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showUpdateDialog = false }) {
                                    Text("Nanti")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
