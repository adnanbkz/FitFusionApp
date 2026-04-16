package com.example.fitfusion.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.fitfusion.ui.theme.OnSurface
import com.example.fitfusion.ui.theme.OnSurfaceVariant
import com.example.fitfusion.ui.theme.OutlineVariant
import com.example.fitfusion.ui.theme.Primary
import com.example.fitfusion.ui.theme.PrimaryContainer
import com.example.fitfusion.ui.theme.Surface
import com.example.fitfusion.ui.theme.SurfaceContainerHigh
import com.example.fitfusion.ui.theme.SurfaceContainerLow
import com.example.fitfusion.ui.theme.SurfaceContainerLowest
import com.example.fitfusion.util.EquipmentTranslations
import com.example.fitfusion.util.MuscleTranslations
import com.example.fitfusion.viewmodel.CreateExerciseUiState
import com.example.fitfusion.viewmodel.CreateExerciseViewModel
import com.example.fitfusion.viewmodel.EQUIPMENT_OPTIONS
import com.example.fitfusion.viewmodel.MUSCLE_OPTIONS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCreateExercise(
    navController: NavHostController,
    viewModel: CreateExerciseViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> viewModel.onPhotoSelected(uri) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item {
            TopAppBar(
                title = {
                    Text(
                        "Nuevo ejercicio",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface),
            )
        }

        // ── Foto ──────────────────────────────────────────────────────────
        item {
            PhotoPickerSection(
                uri = state.photoUri,
                onPickPhoto = { photoPicker.launch("image/*") },
                onRemovePhoto = { viewModel.onPhotoSelected(null) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ── Nombre ────────────────────────────────────────────────────────
        item {
            CreateSection(title = "Información básica") {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nombre del ejercicio") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = SurfaceContainerLow,
                        focusedContainerColor = SurfaceContainerLow,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Primary,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ── Músculos ──────────────────────────────────────────────────────
        item {
            CreateSection(title = "Músculos") {
                MuscleDropdownRow(
                    label = "Motor principal",
                    selected = state.primeMoverMuscle,
                    dotColor = Primary,
                    onSelected = viewModel::onPrimeMoverSelected,
                )

                if (state.showSecondaryMuscle) {
                    Spacer(modifier = Modifier.height(12.dp))
                    MuscleDropdownRow(
                        label = "Secundario",
                        selected = state.secondaryMuscle,
                        dotColor = Primary.copy(alpha = 0.55f),
                        onSelected = viewModel::onSecondaryMuscleSelected,
                    )
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = viewModel::onShowSecondaryMuscle,
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text(
                            "+ Añadir músculo secundario",
                            color = Primary,
                            fontSize = 13.sp,
                        )
                    }
                }

                if (state.showSecondaryMuscle) {
                    if (state.showTertiaryMuscle) {
                        Spacer(modifier = Modifier.height(12.dp))
                        MuscleDropdownRow(
                            label = "Terciario",
                            selected = state.tertiaryMuscle,
                            dotColor = Primary.copy(alpha = 0.30f),
                            onSelected = viewModel::onTertiaryMuscleSelected,
                        )
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = viewModel::onShowTertiaryMuscle,
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            Text(
                                "+ Añadir músculo terciario",
                                color = Primary,
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ── Equipamiento ──────────────────────────────────────────────────
        item {
            EquipmentSection(
                selected = state.selectedEquipment,
                onToggle = viewModel::onEquipmentToggle,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // ── Botón guardar ─────────────────────────────────────────────────
        item {
            SaveButton(
                isValid = state.isValid,
                onSave = {
                    viewModel.onSave { navController.popBackStack() }
                },
            )
        }
    }
}

// ── Secciones ──────────────────────────────────────────────────────────────────

@Composable
private fun PhotoPickerSection(
    uri: Uri?,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        if (uri != null) {
            Box {
                AsyncImage(
                    model = uri,
                    contentDescription = "Foto del ejercicio",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onPickPhoto() },
                )
                IconButton(
                    onClick = onRemovePhoto,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Eliminar foto",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLow)
                    .border(
                        width = 1.5.dp,
                        color = OutlineVariant,
                        shape = RoundedCornerShape(16.dp),
                    )
                    .clickable { onPickPhoto() },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(36.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Añadir foto",
                        fontSize = 14.sp,
                        color = OnSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = SurfaceContainerLowest),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Primary,
                letterSpacing = 0.8.sp,
            )
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = SurfaceContainerHigh)
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MuscleDropdownRow(
    label: String,
    selected: String?,
    dotColor: Color,
    onSelected: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, color = OnSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                Row(
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceContainerLow)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (selected != null) MuscleTranslations.translate(selected) else "Seleccionar músculo",
                        fontSize = 14.sp,
                        fontWeight = if (selected != null) FontWeight.Medium else FontWeight.Normal,
                        color = if (selected != null) OnSurface else OnSurfaceVariant,
                    )
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = SurfaceContainerLowest,
                ) {
                    MUSCLE_OPTIONS.forEach { muscle ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    MuscleTranslations.translate(muscle),
                                    fontSize = 14.sp,
                                    color = if (muscle == selected) Primary else OnSurface,
                                    fontWeight = if (muscle == selected) FontWeight.SemiBold else FontWeight.Normal,
                                )
                            },
                            onClick = {
                                onSelected(muscle)
                                expanded = false
                            },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EquipmentSection(
    selected: Set<String>,
    onToggle: (String) -> Unit,
) {
    CreateSection(title = "Equipamiento") {
        Text(
            "Selecciona el equipamiento necesario",
            fontSize = 13.sp,
            color = OnSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            EQUIPMENT_OPTIONS.forEach { equipment ->
                FilterChip(
                    selected = equipment in selected,
                    onClick = { onToggle(equipment) },
                    label = { Text(EquipmentTranslations.translate(equipment), fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryContainer,
                        selectedLabelColor = Primary,
                        containerColor = SurfaceContainerLow,
                    ),
                )
            }
        }
    }
}

@Composable
private fun SaveButton(
    isValid: Boolean,
    onSave: () -> Unit,
) {
    Button(
        onClick = onSave,
        enabled = isValid,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary,
            disabledContainerColor = SurfaceContainerHigh,
            disabledContentColor = OnSurfaceVariant,
        ),
    ) {
        Text(
            "Guardar ejercicio",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
