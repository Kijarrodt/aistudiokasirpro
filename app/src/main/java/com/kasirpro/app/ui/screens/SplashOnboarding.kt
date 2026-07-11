package com.kasirpro.app.ui.screens

import com.kasirpro.app.util.Translator
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kasirpro.app.ui.viewmodel.KasirViewModel
import com.kasirpro.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun SplashScreen(viewModel: KasirViewModel) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2000)
        
        // 1. Fetch user directly on IO dispatchers to avoid stateflow race timing
        var current = viewModel.repository.getCurrentUserRaw()
        val savedUid = viewModel.repository.auth.currentUser?.uid 
            ?: viewModel.repository.prefs.getString("logged_in_uid", null)
            
        if (current == null && savedUid != null) {
            // User is authenticated but local DB has not finished loading. Let's retry!
            var retries = 0
            while (current == null && retries < 15) {
                delay(150)
                current = viewModel.repository.getCurrentUserRaw()
                retries++
            }
        }

        // 2. If it is still null but savedUid is definitely not null (authenticated owner/kasir),
        // try to fetch/restore user profile from remote Firestore or fallback
        if (current == null && savedUid != null) {
            try {
                viewModel.repository.syncFromFirestore(savedUid)
                current = viewModel.repository.getCurrentUserRaw()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // 3. Fallback just in case user is offline and we failed to sync, recreate the local UserEntity
            // so we don't force the logged-in user to login again!
            if (current == null) {
                val isKasirSaved = viewModel.repository.prefs.getBoolean("is_kasir_saved", false)
                val savedEmail = viewModel.repository.prefs.getString("saved_user_email", "") ?: ""
                val savedName = viewModel.repository.prefs.getString("saved_user_name", "Pemilik Toko") ?: "Pemilik Toko"
                val savedOwnerId = viewModel.repository.prefs.getString("saved_owner_id", null)
                val isAtLeastProfesional = viewModel.repository.prefs.getBoolean("is_at_least_profesional", false)
                
                val fallbackUser = com.kasirpro.app.data.local.UserEntity(
                    uid = savedUid,
                    nama = savedName,
                    email = savedEmail,
                    role = if (isKasirSaved) Translator.t("kasir") else "owner",
                    ownerId = savedOwnerId,
                    assignedBranchId = null,
                    subscriptionStatus = if (isAtLeastProfesional) "profesional" else "free",
                    subscriptionType = null,
                    subscriptionStartDate = null,
                    subscriptionEndDate = null,
                    createdAt = System.currentTimeMillis(),
                    lastActiveAt = System.currentTimeMillis()
                )
                try {
                    viewModel.repository.dao.insertUser(fallbackUser)
                    current = fallbackUser
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        if (current != null) {
            var biz = viewModel.repository.getCurrentBusinessRaw()
            
            // Retry business load a short while if it started blank
            if (biz == null && current.role == "owner") {
                var bizRetries = 0
                while (biz == null && bizRetries < 5) {
                    delay(100)
                    biz = viewModel.repository.getCurrentBusinessRaw()
                    bizRetries++
                }
            }

            if (biz == null && current.role == "owner") {
                // Check if a business already exists in Firestore for this owner
                var onlineBizExists = false
                try {
                    val uid = current.uid
                    val firestoreBiz = viewModel.repository.getBusinessFromFirestore(uid)
                    if (firestoreBiz != null) {
                        onlineBizExists = true
                        viewModel.repository.insertBusinessLocal(firestoreBiz)
                        android.util.Log.d("SPLASH", "Restore existing business from Firestore on splash screen: ${firestoreBiz.namaBisnis}")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (onlineBizExists) {
                    viewModel.activeScreen.value = "home"
                } else {
                    viewModel.activeScreen.value = "setup_toko"
                }
            } else if (current.role == "kasir") {
                viewModel.activeScreen.value = "cashier"
            } else {
                viewModel.activeScreen.value = "home"
            }
        } else {
            viewModel.activeScreen.value = "onboarding"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(OrangePrimary, OrangeDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alphaAnim.value)
        ) {
            Icon(
                imageVector = Icons.Default.PointOfSale,
                contentDescription = Translator.t("Kasir Pro Logo"),
                tint = Color.White,
                modifier = Modifier
                    .size(100.dp)
                    .testTag("splash_logo")
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = Translator.t("Kasir Pro"),
                style = MaterialTheme.typography.displaySmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            )
            Text(
                text = Translator.t("Aplikasi Kasir Modern Indonesia"),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.8f)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(viewModel: KasirViewModel) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingPageData(
            title = Translator.t("Transaksi Instan & Barcode"),
            description = Translator.t("Catat penjualan kasir secepat kilat dengan pencarian pintar dan camera scan barcode otomatis."),
            icon = Icons.Default.FlashOn
        ),
        OnboardingPageData(
            title = Translator.t("Manajemen Cabang & Stok"),
            description = Translator.t("Pantau persediaan stok minimum dan kelola banyak cabang usaha secara realtime langsung dari genggaman Anda."),
            icon = Icons.Default.PointOfSale
        ),
        OnboardingPageData(
            title = "Mode Offline & Cloud Sync",
            description = Translator.t("Internet mati? Transaksi tetap berjalan normal secara offline dan otomatis sync saat online kembali."),
            icon = Icons.Default.CloudUpload
        )
    )

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicators
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { index ->
                        val selected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (selected) 24.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(if (selected) OrangePrimary else Color.LightGray)
                        )
                    }
                }

                // Call to action button
                Button(
                    onClick = {
                        if (pagerState.currentPage < 2) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            viewModel.activeScreen.value = "login"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("onboarding_next_button")
                ) {
                    Text(
                        text = if (pagerState.currentPage == 2) Translator.t("Mulai Sekarang") else "Lanjut",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (pagerState.currentPage == 2) Icons.Default.Done else Icons.Default.ArrowForward,
                        contentDescription = "Selanjutnya",
                        tint = Color.White
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            OnboardingView(data = pages[page])
        }
    }
}

data class OnboardingPageData(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun OnboardingView(data: OnboardingPageData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(OrangeLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = data.icon,
                contentDescription = null,
                tint = OrangePrimary,
                modifier = Modifier.size(80.dp)
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = data.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = data.description,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                lineHeight = 22.sp
            ),
            textAlign = TextAlign.Center
        )
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
