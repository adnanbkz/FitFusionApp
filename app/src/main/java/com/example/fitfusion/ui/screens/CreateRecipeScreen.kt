package com.example.fitfusion.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.CreateRecipeViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PantallaCreateRecipe(
    navController: NavHostController,
    viewModel: CreateRecipeViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.onPhotoSelected(it) } }

    if (state.showCamera) {
        PantallaCamera(
            onClose          = viewModel::closeCamera,
            onMediaCaptured  = { uri, isVideo -> if (!isVideo) viewModel.onPhotoSelected(uri) }
        )
        return
    }

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = { Text("Nueva receta", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = OnSurface)
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveRecipe { navController.popBackStack() } },
                        enabled = state.isValid && !state.isSaving
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color       = Primary,
                            )
                        } else {
                            Text(
                                "Guardar",
                                color      = if (state.isValid) Primary else OnSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 15.sp,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface),
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                PhotoBox(
                    photoUri    = state.photoUri,
                    onTapAdd    = viewModel::openPhotoOptions,
                    onRemove    = viewModel::clearPhoto,
                )
            }

            item {
                RecipeInfoCard(state = state, viewModel = viewModel)
            }

            item {
                SectionLabel("DESCRIPCIÓN")
                Spacer(Modifier.height(8.dp))
                FieldTextArea(
                    value         = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    placeholder   = "Breve descripción de la receta (opcional)",
                    minLines      = 2,
                    maxLines      = 4,
                )
            }

            item {
                SectionLabel("INGREDIENTES")
                Spacer(Modifier.height(8.dp))
                FieldTextArea(
                    value         = state.ingredients,
                    onValueChange = viewModel::onIngredientsChange,
                    placeholder   = "Un ingrediente por línea\nEj:\n200g de arroz\n1 pechuga de pollo",
                    minLines      = 4,
                    maxLines      = 10,
                )
            }

            item {
                SectionLabel("PREPARACIÓN")
                Spacer(Modifier.height(8.dp))
                FieldTextArea(
                    value         = state.instructions,
                    onValueChange = viewModel::onInstructionsChange,
                    placeholder   = "Pasos a seguir...",
                    minLines      = 4,
                    maxLines      = 12,
                )
            }

            item {
                SectionLabel("DETALLES")
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField(
                        value         = state.cookTime,
                        onValueChange = viewModel::onCookTimeChange,
                        placeholder   = "Tiempo (min)",
                        modifier      = Modifier.weight(1f),
                    )
                    NumberField(
                        value         = state.kcal,
                        onValueChange = viewModel::onKcalChange,
                        placeholder   = "Kcal totales",
                        modifier      = Modifier.weight(1f),
                    )
                }
            }

            item {
                SectionLabel("MEJOR MOMENTO")
                Spacer(Modifier.height(8.dp))
                BestMomentChips(
                    selected    = state.bestMoment,
                    onSelect    = viewModel::onBestMomentChange,
                )
            }

            item {
                PublishRow(
                    isPublic   = state.isPublic,
                    onToggle   = viewModel::onPublicToggle,
                )
            }

            if (state.saveError != null) {
                item {
                    Text(
                        state.saveError!!,
                        color    = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }

    if (state.showPhotoOptions) {
        PhotoSourceSheet(
            onDismiss = viewModel::dismissPhotoOptions,
            onGallery = {
                viewModel.dismissPhotoOptions()
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onCamera = viewModel::openCamera,
        )
    }
}

@Composable
private fun PhotoBox(
    photoUri: android.net.Uri?,
    onTapAdd: () -> Unit,
    onRemove: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceContainerLowest)
            .clickable(enabled = photoUri == null, onClick = onTapAdd),
        contentAlignment = Alignment.Center,
    ) {
        if (photoUri != null) {
            AsyncImage(
                model             = photoUri,
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Close, "Quitar foto", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Default.CameraAlt, null, tint = OnSurfaceVariant, modifier = Modifier.size(36.dp))
                Text("Añadir foto de la receta", fontSize = 13.sp, color = OnSurfaceVariant)
            }
        }
    }
}

@Composable
private fun RecipeInfoCard(
    state: com.example.fitfusion.viewmodel.CreateRecipeUiState,
    viewModel: CreateRecipeViewModel,
) {
    Card(
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionLabel("INFORMACIÓN")
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value         = state.emoji,
                    onValueChange = viewModel::onEmojiChange,
                    modifier      = Modifier.width(72.dp),
                    textStyle     = LocalTextStyle.current.copy(fontSize = 24.sp, textAlign = TextAlign.Center),
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp),
                    colors        = filledFieldColors(),
                )
                OutlinedTextField(
                    value         = state.name,
                    onValueChange = viewModel::onNameChange,
                    placeholder   = { Text("Nombre de la receta *", color = OnSurfaceVariant, fontSize = 15.sp) },
                    singleLine    = true,
                    modifier      = Modifier.weight(1f),
                    shape         = RoundedCornerShape(12.dp),
                    colors        = filledFieldColors(),
                )
            }
        }
    }
}

@Composable
private fun FieldTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int,
    maxLines: Int,
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        placeholder   = { Text(placeholder, color = OnSurfaceVariant, fontSize = 14.sp) },
        minLines      = minLines,
        maxLines      = maxLines,
        modifier      = Modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(14.dp),
        colors        = filledFieldColors(),
    )
}

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value           = value,
        onValueChange   = onValueChange,
        placeholder     = { Text(placeholder, color = OnSurfaceVariant, fontSize = 13.sp) },
        singleLine      = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier        = modifier,
        shape           = RoundedCornerShape(12.dp),
        colors          = filledFieldColors(),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BestMomentChips(selected: String?, onSelect: (String?) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement   = Arrangement.spacedBy(8.dp),
    ) {
        listOf("Desayuno", "Almuerzo", "Cena", "Snack", "Pre-entreno", "Post-entreno").forEach { moment ->
            val isSelected = selected == moment
            FilterChip(
                selected = isSelected,
                onClick  = { onSelect(if (isSelected) null else moment) },
                label    = { Text(moment, fontSize = 12.sp) },
                shape    = RoundedCornerShape(20.dp),
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary.copy(alpha = 0.15f),
                    selectedLabelColor     = Primary,
                ),
            )
        }
    }
}

@Composable
private fun PublishRow(isPublic: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Publicar en la comunidad", fontWeight = FontWeight.SemiBold, color = OnSurface, fontSize = 14.sp)
                Text(
                    "Otros usuarios podrán verla y guardarla",
                    fontSize = 12.sp, color = OnSurfaceVariant,
                )
            }
            Switch(
                checked         = isPublic,
                onCheckedChange = onToggle,
                colors          = SwitchDefaults.colors(
                    checkedThumbColor  = Color.White,
                    checkedTrackColor  = Primary,
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoSourceSheet(
    onDismiss: () -> Unit,
    onGallery: () -> Unit,
    onCamera: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = SurfaceContainerLowest,
        dragHandle       = { BottomSheetDefaults.DragHandle(color = OutlineVariant) },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Fuente de la foto", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OnSurface)
            PhotoSourceOption(
                icon    = Icons.Default.PhotoCamera,
                title   = "Cámara",
                subtitle= "Hacer una foto nueva",
                onClick = onCamera,
            )
            PhotoSourceOption(
                icon    = Icons.Default.PhotoLibrary,
                title   = "Galería",
                subtitle= "Elegir una foto existente",
                onClick = onGallery,
            )
        }
    }
}

@Composable
private fun PhotoSourceOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(Primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = Primary, modifier = Modifier.size(22.dp)) }
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = OnSurface)
            Text(subtitle, fontSize = 12.sp, color = OnSurfaceVariant)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        fontSize      = 11.sp,
        fontWeight    = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        color         = Primary,
    )
}

@Composable
private fun filledFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = SurfaceContainerLow,
    focusedContainerColor   = SurfaceContainerLow,
    unfocusedBorderColor    = Color.Transparent,
    focusedBorderColor      = Primary,
)
