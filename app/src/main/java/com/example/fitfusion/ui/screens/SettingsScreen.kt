package com.example.fitfusion.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.health.connect.client.PermissionController
import com.example.fitfusion.R
import com.example.fitfusion.ui.components.SectionTitle
import com.example.fitfusion.ui.components.SettingsRow
import com.example.fitfusion.ui.components.SettingsToggleRow
import com.example.fitfusion.ui.theme.OnSurface
import com.example.fitfusion.ui.theme.OnSurfaceVariant
import com.example.fitfusion.ui.theme.OutlineVariant
import com.example.fitfusion.ui.theme.Primary
import com.example.fitfusion.ui.theme.Secondary
import com.example.fitfusion.ui.theme.Surface
import com.example.fitfusion.ui.theme.SurfaceContainerHigh
import com.example.fitfusion.ui.theme.SurfaceContainerLow
import com.example.fitfusion.ui.theme.SurfaceContainerLowest
import com.example.fitfusion.ui.theme.Tertiary
import com.example.fitfusion.viewmodel.HealthConnectStatus
import com.example.fitfusion.viewmodel.SettingsViewModel

private const val HC_PROVIDER_PACKAGE = "com.google.android.apps.healthdata"
private const val HC_PLAY_STORE_URL =
    "https://play.google.com/store/apps/details?id=$HC_PROVIDER_PACKAGE"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSettings(
    navController: NavHostController,
    userName: String?,
    onLogout: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel(),
) {
    val state by settingsViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        settingsViewModel.onHealthPermissionsResult(grantedPermissions)
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) {
        settingsViewModel.refreshHealthStatus()
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                settingsViewModel.refreshHealthStatus()
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text("Ajustes", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface),
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            elevation = CardDefaults.cardElevation(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHigh),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Person, null, Modifier.size(32.dp), tint = OnSurfaceVariant)
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Edit, null, Modifier.size(12.dp), tint = Color.White)
                    }
                }
                Column {
                    Text(
                        userName ?: "Usuario",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface,
                    )
                    Text("Perfil activo", fontSize = 14.sp, color = OnSurfaceVariant)
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
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            elevation = CardDefaults.cardElevation(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        ) {
            SettingsToggleRow(
                icon = Icons.Default.Notifications,
                title = "Notificaciones push",
                subtitle = "Recordatorios diarios y alertas sociales",
                checked = state.pushNotifications,
                onCheckedChange = settingsViewModel::onPushNotificationsChange,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SectionTitle("DATOS DE SALUD")

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            elevation = CardDefaults.cardElevation(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        ) {
            Column {
                SettingsToggleRow(
                    iconPainter = R.drawable.ic_tracking,
                    title = "Sincronización Health Connect",
                    subtitle = when (state.hcStatus) {
                        HealthConnectStatus.AVAILABLE -> "Lee pasos y frecuencia cardíaca"
                        HealthConnectStatus.NEEDS_UPDATE -> "Actualización de Health Connect requerida"
                        HealthConnectStatus.UNAVAILABLE -> "Health Connect no está instalado"
                    },
                    checked = state.healthSyncEnabled,
                    onCheckedChange = settingsViewModel::onHealthSyncToggle,
                )

                AnimatedVisibility(
                    visible = state.healthSyncEnabled,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Column {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = OutlineVariant.copy(alpha = 0.2f),
                        )
                        HealthConnectStatusPanel(
                            state = state,
                            onRequestPermissions = {
                                permissionLauncher.launch(settingsViewModel.healthManager.permissions)
                            },
                            onManageAccess = {
                                settingsLauncher.launch(settingsViewModel.healthManager.settingsIntent())
                            },
                            onSyncNow = settingsViewModel::syncNow,
                            onInstall = {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(HC_PLAY_STORE_URL))
                                )
                            },
                            onDismissError = settingsViewModel::dismissSyncError,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsRow(Icons.Default.Settings, "Datos y almacenamiento", "Gestión de caché y exportación de datos") {
            navController.navigate(Screens.DataStorageScreen.name)
        }
        SettingsRow(Icons.Default.Email, "Ayuda y soporte", "Preguntas frecuentes, contacto y avisos legales") {
            navController.navigate(Screens.HelpSupportScreen.name)
        }

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, OnSurface.copy(alpha = 0.1f)),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null, Modifier.size(18.dp), tint = Tertiary)
                Text("Cerrar sesión", color = Tertiary, fontWeight = FontWeight.SemiBold)
            }
        }

        Text(
            "FitFusion v1.0.0-alpha",
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            color = OnSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            textAlign = TextAlign.Center,
        )
    }
}


@Composable
private fun HealthConnectStatusPanel(
    state: com.example.fitfusion.viewmodel.SettingsUiState,
    onRequestPermissions: () -> Unit,
    onManageAccess: () -> Unit,
    onSyncNow: () -> Unit,
    onInstall: () -> Unit,
    onDismissError: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (state.hcStatus) {
            HealthConnectStatus.UNAVAILABLE -> {
                HcInfoCard(
                    icon = { Icon(Icons.Default.Warning, null, Modifier.size(20.dp), tint = Tertiary) },
                    title = "Health Connect no está instalado",
                    subtitle = "Instala la app para leer tus datos de actividad y salud.",
                    tint = Tertiary,
                ) {
                    Button(
                        onClick = onInstall,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Tertiary),
                    ) {
                        Text("Instalar Health Connect")
                    }
                }
            }

            HealthConnectStatus.NEEDS_UPDATE -> {
                HcInfoCard(
                    icon = { Icon(Icons.Default.Warning, null, Modifier.size(20.dp), tint = Secondary) },
                    title = "Actualización requerida",
                    subtitle = "Actualiza Health Connect en Google Play para continuar.",
                    tint = Secondary,
                ) {
                    Button(
                        onClick = onInstall,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                    ) {
                        Text("Actualizar")
                    }
                }
            }

            HealthConnectStatus.AVAILABLE -> {
                if (!state.hasPermissions) {
                    HcInfoCard(
                        icon = { Icon(Icons.Default.Lock, null, Modifier.size(20.dp), tint = Secondary) },
                        title = "Permisos pendientes",
                        subtitle = "Concede acceso a Pasos y Frecuencia cardíaca para empezar.",
                        tint = Secondary,
                    ) {
                        Button(
                            onClick = onRequestPermissions,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        ) {
                            Icon(Icons.Default.Lock, null, Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Conceder permisos")
                        }
                        OutlinedButton(
                            onClick = onManageAccess,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Secondary),
                            border = BorderStroke(1.dp, Secondary.copy(alpha = 0.4f)),
                        ) {
                            Text("Gestionar acceso")
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                Modifier.size(16.dp),
                                tint = Primary,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Conectado a Health Connect",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Primary,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            state.lastSyncTime?.let {
                                Text(
                                    "Hoy, $it",
                                    fontSize = 11.sp,
                                    color = OnSurfaceVariant,
                                )
                            }
                        }

                        if (state.syncData != null || state.isSyncing) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                HcStatPill(
                                    value = if (state.isSyncing) "—" else "${state.syncData?.steps ?: 0}",
                                    label = "pasos",
                                    modifier = Modifier.weight(1f),
                                )
                                HcStatPill(
                                    value = if (state.isSyncing) "—" else "${state.syncData?.stepCaloriesEstimated ?: 0}",
                                    label = "kcal",
                                    modifier = Modifier.weight(1f),
                                )
                                HcStatPill(
                                    value = if (state.isSyncing) "—"
                                    else state.syncData?.averageHeartRate?.let { "$it bpm" } ?: "—",
                                    label = "FC media",
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }

                        if (state.syncError != null) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Tertiary.copy(alpha = 0.08f),
                                ),
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        null,
                                        Modifier.size(16.dp),
                                        tint = Tertiary,
                                    )
                                    Text(
                                        state.syncError,
                                        fontSize = 12.sp,
                                        color = Tertiary,
                                        modifier = Modifier.weight(1f),
                                    )
                                    IconButton(
                                        onClick = onDismissError,
                                        modifier = Modifier.size(20.dp),
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            "Reintentar",
                                            Modifier.size(14.dp),
                                            tint = Tertiary,
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = onSyncNow,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                            border = BorderStroke(1.dp, Primary.copy(alpha = 0.4f)),
                            enabled = !state.isSyncing,
                        ) {
                            if (state.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Primary,
                                    strokeWidth = 2.dp,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sincronizando...")
                            } else {
                                Icon(
                                    Icons.Default.Refresh,
                                    null,
                                    Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sincronizar ahora")
                            }
                        }
                        OutlinedButton(
                            onClick = onManageAccess,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurfaceVariant),
                            border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f)),
                        ) {
                            Icon(Icons.Default.Lock, null, Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gestionar acceso")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HcInfoCard(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    tint: Color,
    action: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = tint.copy(alpha = 0.06f)),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                icon()
                Column {
                    Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                    Text(subtitle, fontSize = 12.sp, color = OnSurfaceVariant)
                }
            }
            action()
        }
    }
}

@Composable
private fun HcStatPill(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerLow)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnSurface)
        Text(label, fontSize = 9.sp, color = OnSurfaceVariant, letterSpacing = 0.5.sp)
    }
}
