package com.example.fitfusion.ui.screens

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
import com.example.fitfusion.ui.components.SectionTitle
import com.example.fitfusion.ui.components.SettingsRow
import com.example.fitfusion.ui.components.SettingsToggleRow
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.SettingsViewModel
import com.example.fitfusion.R

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
            title = { Text("Ajustes", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
            actions = { IconButton(onClick = { }) { Icon(Icons.Default.Search, "Buscar") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            elevation = CardDefaults.cardElevation(0.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(SurfaceContainerHigh), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, Modifier.size(32.dp), tint = OnSurfaceVariant)
                    Box(
                        modifier = Modifier.size(20.dp).align(Alignment.BottomEnd).offset(x = 2.dp, y = 2.dp).clip(CircleShape).background(Primary),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Edit, null, Modifier.size(12.dp), tint = Color.White) }
                }
                Column {
                    Text(userName ?: "Alex Rivera", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                    Text("alex.fit@kinetic.app", fontSize = 14.sp, color = OnSurfaceVariant)
                }
            }
        }

        SectionTitle("CONFIGURACIÓN DE CUENTA")
        SettingsRow(Icons.Default.Person, "Cuenta", "Gestiona tu perfil, correo y contraseña") {
            navController.navigate(Screens.AccountScreen.name)
        }
        SettingsRow(Icons.Default.Lock, "Privacidad", "Controla quién ve tu actividad y datos") {
            navController.navigate(Screens.PrivacyScreen.name)
        }

        SectionTitle("PREFERENCIAS")
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
            elevation = CardDefaults.cardElevation(0.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        ) {
            Column {
                SettingsToggleRow(icon = Icons.Default.Notifications, title = "Notificaciones push", subtitle = "Recordatorios diarios y alertas sociales", checked = state.pushNotifications, onCheckedChange = settingsViewModel::onPushNotificationsChange)
                SettingsToggleRow(iconPainter = R.drawable.ic_tracking, title = "Sincronización de datos de salud", subtitle = "Sincronización automática con Apple Health/Google Fit", checked = state.healthSync, onCheckedChange = settingsViewModel::onHealthSyncChange)
            }
        }

        SettingsRow(Icons.Default.Settings, "Datos y almacenamiento", "Gestión de caché y exportación de datos") {
            navController.navigate(Screens.DataStorageScreen.name)
        }
        SettingsRow(Icons.Default.Email, "Ayuda y soporte", "Preguntas frecuentes, contacto y avisos legales") {
            navController.navigate(Screens.HelpSupportScreen.name)
        }

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp).height(50.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, OnSurface.copy(alpha = 0.1f))
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null, Modifier.size(18.dp), tint = Tertiary)
                Text("Cerrar sesión en Kinetic", color = Tertiary, fontWeight = FontWeight.SemiBold)
            }
        }

        Text(
            "VERSION 4.2.0-ALPHA • KINETIC LABS",
            fontSize = 11.sp, letterSpacing = 1.sp, color = OnSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            textAlign = TextAlign.Center
        )
    }
}