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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kasirpro.app.ui.theme.MyApplicationTheme
import com.kasirpro.app.ui.theme.OrangePrimary
import com.kasirpro.app.ui.viewmodel.KasirViewModel
import com.kasirpro.app.ui.screens.*

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by viewModel.isDarkMode.collectAsState()
            
            val context = androidx.compose.ui.platform.LocalContext.current
            var showUpdateDialog by remember { mutableStateOf(false) }
            var updateDownloadUrl by remember { mutableStateOf("") }
            
            LaunchedEffect(Unit) {
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
                                            icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                                            label = { Text("Beranda") },
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
                                        icon = { Icon(Icons.Default.PointOfSale, contentDescription = "Kasir") },
                                        label = { Text("Kasir") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = OrangePrimary,
                                            selectedTextColor = OrangePrimary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )

                                    NavigationBarItem(
                                        selected = activeScreenState == "manage",
                                        onClick = { viewModel.activeScreen.value = "manage" },
                                        icon = { Icon(Icons.Default.Inventory, contentDescription = "Produk") },
                                        label = { Text("Stok") },
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
                                            icon = { Icon(Icons.Default.Stars, contentDescription = "Premium") },
                                            label = { Text("Premium") },
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
                                        icon = { Icon(Icons.Default.Settings, contentDescription = "Pengaturan") },
                                        label = { Text("Pengaturan") },
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
                        Box(modifier = Modifier.padding(pad)) {
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
