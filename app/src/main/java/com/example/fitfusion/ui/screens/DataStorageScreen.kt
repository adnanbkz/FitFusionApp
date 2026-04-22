package com.example.fitfusion.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.fitfusion.ui.theme.OnSurface
import com.example.fitfusion.ui.theme.OnSurfaceVariant
import com.example.fitfusion.ui.theme.OutlineVariant
import com.example.fitfusion.ui.theme.Primary
import com.example.fitfusion.ui.theme.Secondary
import com.example.fitfusion.ui.theme.Surface
import com.example.fitfusion.ui.theme.SurfaceContainerHigh
import com.example.fitfusion.ui.theme.SurfaceContainerLowest
import com.example.fitfusion.ui.theme.Tertiary
import com.example.fitfusion.viewmodel.DataStorageViewModel
import com.example.fitfusion.viewmodel.formatBytes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDataStorage(
    navController: NavHostController,
    dataStorageViewModel: DataStorageViewModel = viewModel(),
) {
    val state by dataStorageViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        dataStorageViewModel.shareEvent.collect { intent ->
            context.startActivity(intent)
        }
    }

    LaunchedEffect(state.exportSuccess) {
        if (state.exportSuccess) {
            snackbarHostState.showSnackbar("Datos exportados correctamente")
            dataStorageViewModel.dismissExportSuccess()
        }
    }

    LaunchedEffect(state.deleteError) {
        state.deleteError?.let {
            snackbarHostState.showSnackbar("Error: $it")
            dataStorageViewModel.dismissDeleteError()
        }
    }

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = dataStorageViewModel::dismissDeleteConfirm,
            title = {
                Text("¿Eliminar datos de salud?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Se eliminarán los ${state.healthDayCount} registros diarios de Health Connect " +
                        "almacenados en la nube. Esta acción no se puede deshacer.",
                    color = OnSurfaceVariant,
                )
            },
            confirmButton = {
                Button(
                    onClick = dataStorageViewModel::deleteHealthData,
                    colors = ButtonDefaults.buttonColors(containerColor = Tertiary),
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = dataStorageViewModel::dismissDeleteConfirm) {
                    Text("Cancelar", color = OnSurfaceVariant)
                }
            },
            containerColor = SurfaceContainerLowest,
            shape = RoundedCornerShape(20.dp),
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = {
                    Text("Datos y almacenamiento", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás")
                    }
                },
                actions = {
                    IconButton(
                        onClick = dataStorageViewModel::loadStorageInfo,
                        enabled = !state.isLoading,
                    ) {
                        Icon(Icons.Default.Refresh, "Actualizar", tint = OnSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 64.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Calculando almacenamiento...", color = OnSurfaceVariant, fontSize = 14.sp)
                    }
                }
            } else {

                StorageSectionHeader("ALMACENAMIENTO DEL DISPOSITIVO")
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Column {
                                Text(
                                    state.deviceUsedBytes.formatBytes(),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = OnSurface,
                                    letterSpacing = (-0.5).sp,
                                )
                                Text("usados", fontSize = 12.sp, color = OnSurfaceVariant)
                            }
                            Text(
                                "de ${state.deviceTotalBytes.formatBytes()}",
                                fontSize = 14.sp,
                                color = OnSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val usedFraction = if (state.deviceTotalBytes > 0) {
                            (state.deviceUsedBytes.toFloat() / state.deviceTotalBytes).coerceIn(0f, 1f)
                        } else 0f

                        LinearProgressIndicator(
                            progress = { usedFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = if (usedFraction > 0.85f) Tertiary else Primary,
                            trackColor = SurfaceContainerHigh,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            StorageLegendItem(
                                color = if (usedFraction > 0.85f) Tertiary else Primary,
                                label = "Ocupado",
                                value = state.deviceUsedBytes.formatBytes(),
                                modifier = Modifier.weight(1f),
                            )
                            StorageLegendItem(
                                color = OnSurfaceVariant,
                                label = "Libre",
                                value = state.deviceFreeBytes.formatBytes(),
                                modifier = Modifier.weight(1f),
                            )
                        }

                        if (usedFraction > 0.85f) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Almacenamiento casi lleno",
                                fontSize = 12.sp,
                                color = Tertiary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                StorageSectionHeader("USO DE LA APLICACIÓN")
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        AppStorageRow(
                            color = Secondary,
                            label = "Datos de la app",
                            value = state.appDataBytes.formatBytes(),
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        AppStorageRow(
                            color = Primary,
                            label = "Caché",
                            value = state.appCacheBytes.formatBytes(),
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = OutlineVariant.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                "Total app",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface,
                            )
                            Text(
                                state.appTotalBytes.formatBytes(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                StorageSectionHeader("CACHÉ")
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Refresh, null, Modifier.size(22.dp), tint = Primary)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Limpiar caché",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = OnSurface,
                            )
                            Text(
                                if (state.appCacheBytes > 0)
                                    "${state.appCacheBytes.formatBytes()} en caché"
                                else
                                    "Caché vacío",
                                fontSize = 13.sp,
                                color = OnSurfaceVariant,
                            )
                        }
                        if (state.isClearingCache) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Primary,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            TextButton(
                                onClick = dataStorageViewModel::clearCache,
                                enabled = state.appCacheBytes > 0,
                            ) {
                                Text(
                                    "Limpiar",
                                    color = if (state.appCacheBytes > 0) Primary else OnSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                StorageSectionHeader("GESTIÓN DE DATOS")
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                ) {
                    Column {
                        DataActionRow(
                            icon = Icons.Default.Share,
                            iconTint = Secondary,
                            title = "Exportar mis datos",
                            subtitle = "Descarga un JSON con tu perfil y datos de salud",
                            actionLabel = if (state.isExporting) null else "Exportar",
                            isLoading = state.isExporting,
                            onClick = dataStorageViewModel::exportData,
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = OutlineVariant.copy(alpha = 0.3f),
                        )
                        DataActionRow(
                            icon = Icons.Default.Delete,
                            iconTint = Tertiary,
                            title = "Borrar datos de salud",
                            subtitle = if (state.healthDayCount > 0)
                                "${state.healthDayCount} registros diarios en la nube"
                            else
                                "No hay registros almacenados",
                            actionLabel = if (state.isDeleting) null else "Borrar",
                            actionColor = Tertiary,
                            isLoading = state.isDeleting,
                            enabled = state.healthDayCount > 0,
                            onClick = dataStorageViewModel::showDeleteConfirm,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}


@Composable
private fun StorageSectionHeader(text: String) {
    Text(
        text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        color = Primary,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
    )
}

@Composable
private fun StorageLegendItem(
    color: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 11.sp, color = OnSurfaceVariant)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = OnSurface)
        }
    }
}

@Composable
private fun AppStorageRow(color: Color, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, fontSize = 13.sp, color = OnSurface, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = OnSurfaceVariant)
    }
}

@Composable
private fun DataActionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    actionLabel: String?,
    actionColor: Color = Primary,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, Modifier.size(22.dp), tint = iconTint)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = OnSurface)
            Text(subtitle, fontSize = 13.sp, color = OnSurfaceVariant)
        }
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Primary,
                strokeWidth = 2.dp,
            )
        } else if (actionLabel != null) {
            TextButton(onClick = onClick, enabled = enabled) {
                Text(
                    actionLabel,
                    color = if (enabled) actionColor else OnSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
