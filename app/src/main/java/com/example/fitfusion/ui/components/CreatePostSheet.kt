package com.example.fitfusion.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.fitfusion.data.models.UserPostType
import com.example.fitfusion.ui.theme.GreenGradientBrush
import com.example.fitfusion.ui.theme.OnSurface
import com.example.fitfusion.ui.theme.OnSurfaceVariant
import com.example.fitfusion.ui.theme.OutlineVariant
import com.example.fitfusion.ui.theme.Primary
import com.example.fitfusion.ui.theme.SurfaceContainerHigh
import com.example.fitfusion.ui.theme.SurfaceContainerLow
import com.example.fitfusion.ui.theme.SurfaceContainerLowest
import com.example.fitfusion.viewmodel.ProfileUiState
import com.example.fitfusion.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostSheetHost(
    profileViewModel: ProfileViewModel,
    onNavigateToAddWorkout: () -> Unit,
) {
    val state by profileViewModel.uiState.collectAsState()
    if (!state.showCreatePostSheet) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = profileViewModel::dismissCreatePost,
        sheetState       = sheetState,
        containerColor   = SurfaceContainerLowest,
        dragHandle       = { BottomSheetDefaults.DragHandle(color = OutlineVariant) }
    ) {
        CreatePostSheetContent(
            state        = state,
            viewModel    = profileViewModel,
            onAddWorkout = {
                profileViewModel.dismissCreatePost()
                onNavigateToAddWorkout()
            }
        )
    }
}

@Composable
private fun CreatePostSheetContent(
    state: ProfileUiState,
    viewModel: ProfileViewModel,
    onAddWorkout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "NUEVA PUBLICACIÓN",
                fontSize = 11.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp, color = Primary
            )
            Text(
                "Comparte lo que acabas de hacer",
                fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnSurface
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceContainerLow)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                Triple(UserPostType.WORKOUT, "Entreno", Icons.Default.FitnessCenter),
                Triple(UserPostType.NUTRITION, "Nutrición", Icons.Default.Restaurant),
            ).forEach { (type, label, icon) ->
                val isSelected = state.createPostType == type
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) SurfaceContainerLowest else Color.Transparent)
                        .clickable { viewModel.setCreatePostType(type) }
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isSelected) Primary else OnSurfaceVariant
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        label,
                        fontSize   = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color      = if (isSelected) OnSurface else OnSurfaceVariant
                    )
                }
            }
        }

        when (state.createPostType) {
            UserPostType.WORKOUT   -> WorkoutPostForm(state = state, viewModel = viewModel, onAddWorkout = onAddWorkout)
            UserPostType.NUTRITION -> NutritionPostForm(state = state, viewModel = viewModel)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .then(
                    if (state.canPublish) Modifier.background(GreenGradientBrush)
                    else Modifier.background(SurfaceContainerHigh)
                )
                .clickable(enabled = state.canPublish) { viewModel.publishPost() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Publicar",
                fontSize = 16.sp, fontWeight = FontWeight.Bold,
                color = if (state.canPublish) Color.White else OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun WorkoutPostForm(
    state: ProfileUiState,
    viewModel: ProfileViewModel,
    onAddWorkout: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.capturedVideoUri != null) {
            CapturedVideoPreview(onRemove = viewModel::clearCapturedVideo)
        }

        Text(
            "SELECCIONAR ENTRENO",
            fontSize = 11.sp, fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp, color = Primary
        )

        if (state.recentWorkouts.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    "Aún no has registrado entrenos",
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnSurface
                )
                Text(
                    "Registra tu primera sesión para publicarla",
                    fontSize = 12.sp, color = OnSurfaceVariant, textAlign = TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(GreenGradientBrush)
                        .clickable(onClick = onAddWorkout)
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Add, null, Modifier.size(16.dp), tint = Color.White)
                        Text("Registrar entreno", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        } else {
            state.recentWorkouts.forEach { workout ->
                val isSelected = state.selectedWorkout?.id == workout.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Primary.copy(alpha = 0.08f) else SurfaceContainerLow)
                        .clickable { viewModel.selectWorkout(workout) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(workout.emoji, fontSize = 22.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            workout.name,
                            fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Primary else OnSurface,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${workout.durationMinutes} min · ${workout.kcalBurned} kcal",
                            fontSize = 12.sp, color = OnSurfaceVariant
                        )
                    }
                    if (isSelected) {
                        Text("✓", fontSize = 18.sp, color = Primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        OutlinedTextField(
            value         = state.postCaption,
            onValueChange = viewModel::onPostCaptionChange,
            placeholder   = { Text("Añade una descripción (opcional)", color = OnSurfaceVariant, fontSize = 14.sp) },
            minLines      = 2,
            maxLines      = 4,
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = SurfaceContainerLow,
                focusedContainerColor   = SurfaceContainerLow,
                unfocusedBorderColor    = Color.Transparent,
                focusedBorderColor      = Primary,
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NutritionPostForm(
    state: ProfileUiState,
    viewModel: ProfileViewModel,
) {
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.onNutritionPhotoChange(it) } }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = SurfaceContainerLow,
        focusedContainerColor   = SurfaceContainerLow,
        unfocusedBorderColor    = Color.Transparent,
        focusedBorderColor      = Primary,
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceContainerLow)
                .clickable {
                    photoLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (state.nutritionPhotoUri != null) {
                AsyncImage(
                    model            = state.nutritionPhotoUri,
                    contentDescription = null,
                    contentScale     = ContentScale.Crop,
                    modifier         = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint     = OnSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                    Text("Añadir foto de la receta", fontSize = 13.sp, color = OnSurfaceVariant)
                }
            }
        }

        OutlinedTextField(
            value         = state.nutritionTitle,
            onValueChange = viewModel::onNutritionTitleChange,
            placeholder   = { Text("Nombre de la receta *", color = OnSurfaceVariant, fontSize = 14.sp) },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            colors        = fieldColors,
        )

        OutlinedTextField(
            value         = state.nutritionIngredients,
            onValueChange = viewModel::onNutritionIngredientsChange,
            placeholder   = { Text("Ingredientes (ej: 200g arroz, 1 pechuga...)", color = OnSurfaceVariant, fontSize = 14.sp) },
            label         = { Text("Ingredientes", fontSize = 12.sp) },
            minLines      = 3,
            maxLines      = 5,
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            colors        = fieldColors,
        )

        OutlinedTextField(
            value         = state.nutritionInstructions,
            onValueChange = viewModel::onNutritionInstructionsChange,
            placeholder   = { Text("Pasos de preparación...", color = OnSurfaceVariant, fontSize = 14.sp) },
            label         = { Text("Instrucciones", fontSize = 12.sp) },
            minLines      = 3,
            maxLines      = 6,
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            colors        = fieldColors,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value           = state.nutritionCookTime,
                onValueChange   = viewModel::onNutritionCookTimeChange,
                placeholder     = { Text("Tiempo (min)", color = OnSurfaceVariant, fontSize = 13.sp) },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier        = Modifier.weight(1f),
                shape           = RoundedCornerShape(12.dp),
                colors          = fieldColors,
            )
            OutlinedTextField(
                value           = state.nutritionKcal,
                onValueChange   = viewModel::onNutritionKcalChange,
                placeholder     = { Text("Kcal totales", color = OnSurfaceVariant, fontSize = 13.sp) },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier        = Modifier.weight(1f),
                shape           = RoundedCornerShape(12.dp),
                colors          = fieldColors,
            )
        }

        Text(
            "Mejor momento para comer",
            fontSize   = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color      = OnSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement   = Arrangement.spacedBy(8.dp),
        ) {
            listOf("Desayuno", "Pre-entreno", "Post-entreno", "Almuerzo", "Cena", "Snack").forEach { moment ->
                val selected = state.nutritionBestMoment == moment
                FilterChip(
                    selected = selected,
                    onClick  = { viewModel.onNutritionBestMomentChange(if (selected) "" else moment) },
                    label    = { Text(moment, fontSize = 12.sp) },
                    shape    = RoundedCornerShape(20.dp),
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary.copy(alpha = 0.15f),
                        selectedLabelColor     = Primary,
                    )
                )
            }
        }
    }
}

@Composable
private fun CapturedVideoPreview(onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(Color(0xFF1A1A1A), Color(0xFF2D2D2D))
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Primary.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(horizontal = 10.dp, vertical = 5.dp),
        ) {
            Text(
                "VIDEO",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
                color = Color.White,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Quitar video",
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
