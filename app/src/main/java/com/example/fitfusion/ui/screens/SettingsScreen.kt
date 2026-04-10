package com.fitfusion.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.fitfusion.app.R
import com.fitfusion.app.ui.components.*
import com.fitfusion.app.ui.theme.*
import com.fitfusion.app.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSettings(
    navController: NavHostController,
    userName: String?,
    onLogout: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val state by settingsViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(Surface).verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text("Settings", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
            actions = { IconButton(onClick = { }) { Icon(Icons.Default.Search, "Search") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
        )

        // User card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            elevation = CardDefaults.cardElevation(0.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(SurfaceContainerHigh), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, Modifier.size(32.dp), tint = OnSurfaceVariant)
                    Box(
                        modifier = Modifier.size(20.dp).align(Alignment.BottomEnd).offset(x = 2.dp, y = 2.dp).clip(CircleShape).background(Primary),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Edit, null, Modifier.size(12.dp), tint = Color.White) }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(userName ?: "Alex Rivera", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                    Text("alex.fit@kinetic.app", fontSize = 14.sp, color = OnSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        SectionTitle("ACCOUNT SETTINGS")
        SettingsRow(Icons.Default.Person, "Account", "Manage your profile, email, and password") { }
        SettingsRow(Icons.Default.Lock, "Privacy", "Control who sees your activity and data") { }

        Spacer(modifier = Modifier.height(24.dp))
        SectionTitle("PREFERENCES")
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
            elevation = CardDefaults.cardElevation(0.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        ) {
            Column {
                SettingsToggleRow(icon = Icons.Default.Notifications, title = "Push Notifications", subtitle = "Daily reminders and social alerts", checked = state.pushNotifications, onCheckedChange = settingsViewModel::onPushNotificationsChange)
                SettingsToggleRow(iconPainter = R.drawable.ic_tracking, title = "Health Data Sync", subtitle = "Auto-sync with Apple Health/Google Fit", checked = state.healthSync, onCheckedChange = settingsViewModel::onHealthSyncChange)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        SettingsRow(Icons.Default.Settings, "Data & Storage", "Cache management and data exports") { }
        SettingsRow(Icons.Default.Email, "Help & Support", "FAQs, contact us, and legal") { }

        Spacer(modifier = Modifier.height(32.dp))
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(50.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, OnSurface.copy(alpha = 0.1f))
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, null, Modifier.size(18.dp), tint = Tertiary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout from Kinetic", color = Tertiary, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("VERSION 4.2.0-ALPHA • KINETIC LABS", fontSize = 11.sp, letterSpacing = 1.sp, color = OnSurfaceVariant, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))
    }
}