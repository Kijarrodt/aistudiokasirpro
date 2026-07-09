package com.kasirpro.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kasirpro.app.ui.theme.*
import com.kasirpro.app.ui.viewmodel.KasirViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserNotificationsScreen(viewModel: KasirViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val notifications by viewModel.userNotifications.collectAsState()

    // Screen state logic: null means showing the list. Otherwise, showing the detail of that notification.
    var selectedNotification by remember { mutableStateOf<Map<String, Any>?>(null) }

    if (selectedNotification != null) {
        val notif = selectedNotification!!
        val id = notif["id"] as? String ?: ""
        val title = notif["title"] as? String ?: ""
        val message = notif["message"] as? String ?: ""
        val type = notif["type"] as? String ?: "info"
        val createdAt = (notif["createdAt"] as? Number)?.toLong() ?: 0L
        val isRead = notif["isRead"] as? Boolean ?: false

        // Extract broadcast updates data if present
        val version = notif["version"] as? String
        val downloadUrl = notif["downloadUrl"] as? String

        // Mark as read immediately when viewed
        LaunchedEffect(id) {
            if (!isRead) {
                viewModel.markNotifRead(id)
            }
        }

        val typeColor = when (type.lowercase()) {
            "update" -> Color.Green
            "promo" -> Color.Yellow
            "maintenance" -> Color.Red
            else -> Color.Cyan
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detail Notifikasi", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { selectedNotification = null }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Slate900,
                        titleContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Slate900)
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info capsule and date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(typeColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = type.uppercase(),
                            color = typeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = android.text.format.DateFormat.format("dd MMMM yyyy HH:mm", createdAt).toString(),
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }

                // Title
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                HorizontalDivider(color = Color.DarkGray)

                // Message content
                Text(
                    text = message,
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.weight(1f)
                )

                // For Application Updates, render conditional fields and action button
                if (type.lowercase() == "update" && !version.isNullOrBlank() && !downloadUrl.isNullOrBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "INFORMASI PEMBARUAN APLIKASI",
                                color = Color.Green,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Versi Terbaru Tersedia: $version",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Harap unduh pembaruan APK dari server penyedia resmi untuk performa terbaik dan perlindungan fitur baru.",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Gagal membuka link download!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Slate900)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Unduh APK Baru", color = Slate900, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Return button
                OutlinedButton(
                    onClick = { selectedNotification = null },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Kembali ke Daftar Notifikasi")
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Notifikasi Broadcast", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                viewModel.markAllNotifsRead()
                                Toast.makeText(context, "Semua notifikasi ditandai dibaca", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("mark_all_read_btn")
                        ) {
                            Icon(imageVector = Icons.Default.DoneAll, contentDescription = "Mark all read", tint = OrangePrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Slate900,
                        titleContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Slate900)
                    .padding(paddingValues)
            ) {
                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Tidak ada notifikasi untuk Anda",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(notifications.size) { index ->
                            val notif = notifications[index]
                            val id = notif["id"] as? String ?: ""
                            val title = notif["title"] as? String ?: ""
                            val message = notif["message"] as? String ?: ""
                            val type = notif["type"] as? String ?: "info"
                            val createdAt = (notif["createdAt"] as? Number)?.toLong() ?: 0L
                            val isRead = notif["isRead"] as? Boolean ?: false

                            val typeColor = when (type.lowercase()) {
                                "update" -> Color.Green
                                "promo" -> Color.Yellow
                                "maintenance" -> Color.Red
                                else -> Color.Cyan
                            }

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isRead) Slate800.copy(alpha = 0.5f) else Slate800
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.markNotifRead(id)
                                        selectedNotification = notif.toMutableMap().apply { this["isRead"] = true }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(14.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Visual indicator bullet for unread messages
                                    if (!isRead) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(Color.Cyan, CircleShape)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(Color.Transparent, CircleShape)
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Broadcast category
                                            Box(
                                                modifier = Modifier
                                                    .background(typeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = type.uppercase(),
                                                    color = typeColor,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Text(
                                                text = android.text.format.DateFormat.format("dd MMM HH:mm", createdAt).toString(),
                                                color = Color.Gray,
                                                fontSize = 9.sp
                                            )
                                        }

                                        Text(
                                            text = title,
                                            color = if (isRead) Color.LightGray else Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = if (isRead) FontWeight.Bold else FontWeight.ExtraBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Text(
                                            text = message,
                                            color = Color.Gray,
                                            fontSize = 11.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
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
