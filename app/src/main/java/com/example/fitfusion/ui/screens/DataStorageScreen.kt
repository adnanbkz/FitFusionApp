package com.example.fitfusion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.DataStorageViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDataStorage(
    navController: NavHostController,
    dataStorageViewModel: DataStorageViewModel = viewModel()
) {
    val state by dataStorageViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.exportSuccess) {
        if (state.exportSuccess) {
            snackbarHostState.showSnackbar("Datos exportados correctamente")
            dataStorageViewModel.dismissExportSuccess()
        }
    }

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = dataStorageViewModel::dismissDeleteConfirm,
            title = { Text("¿Eliminar datos de entrenamiento?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Se eliminarán todos tus registros de entrenamiento. Esta acción no se puede deshacer.",
                    color = OnSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = dataStorageViewModel::deleteWorkoutData,
                    colors = ButtonDefaults.buttonColors(containerColor = Tertiary)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = dataStorageViewModel::dismissDeleteConfirm) {
                    Text("Cancelar", color = OnSurfaceVariant)
                }
            },
            containerColor = SurfaceContainerLowest,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Datos y almacenamiento",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Uso de almacenamiento
            DataSectionHeader("USO DE ALMACENAMIENTO")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Usado", fontSize = 14.sp, color = OnSurfaceVariant)
                        Text(
                            "${state.usedStorageMb.roundToInt()} MB / ${state.totalStorageMb.roundToInt()} MB",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { (state.usedStorageMb / state.totalStorageMb).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Primary,
                        trackColor = SurfaceContainerHigh
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    StorageRow("Entrenamientos", "${(state.usedStorageMb * 0.6f).roundToInt()} MB", Primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    StorageRow("Fotos y medios", "${(state.usedStorageMb * 0.25f).roundToInt()} MB", Secondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    StorageRow("Caché", "${state.cacheSizeMb.roundToInt()} MB", OnSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Caché
            DataSectionHeader("CACHÉ")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Refresh, null, Modifier.size(22.dp), tint = Primary)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Limpiar caché",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = OnSurface
                        )
                        Text(
                            if (state.cacheSizeMb > 0)
                                "${state.cacheSizeMb.roundToInt()} MB en caché"
                            else
                                "Caché vacío",
                            fontSize = 13.sp,
                            color = OnSurfaceVariant
                        )
                    }
                    if (state.isClearingCache) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        TextButton(
                            onClick = dataStorageViewModel::clearCache,
                            enabled = state.cacheSizeMb > 0
                        ) {
                            Text(
                                "Limpiar",
                                color = if (state.cacheSizeMb > 0) Primary else OnSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Gestión de datos
            DataSectionHeader("GESTIÓN DE DATOS")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Column {
                    DataActionRow(
                        icon = Icons.Default.Share,
                        iconTint = Secondary,
                        title = "Exportar mis datos",
                        subtitle = "Descarga un archivo con toda tu información",
                        actionLabel = if (state.isExporting) null else "Exportar",
                        isLoading = state.isExporting,
                        onClick = dataStorageViewModel::exportData
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = OutlineVariant.copy(alpha = 0.3f)
                    )
                    DataActionRow(
                        icon = Icons.Default.Delete,
                        iconTint = Tertiary,
                        title = "Borrar datos de entrenamiento",
                        subtitle = "Elimina todos tus registros de actividad",
                        actionLabel = "Borrar",
                        actionColor = Tertiary,
                        onClick = dataStorageViewModel::showDeleteConfirm
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DataSectionHeader(text: String) {
    Text(
        text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        color = Primary,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
private fun StorageRow(label: String, size: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, fontSize = 13.sp, color = OnSurface, modifier = Modifier.weight(1f))
        Text(size, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = OnSurfaceVariant)
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
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
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
                strokeWidth = 2.dp
            )
        } else if (actionLabel != null) {
            TextButton(onClick = onClick) {
                Text(actionLabel, color = actionColor, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}