package com.example.fitfusion.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.fitfusion.ui.theme.OnSurface
import com.example.fitfusion.ui.theme.OnSurfaceVariant
import com.example.fitfusion.ui.theme.Primary
import com.example.fitfusion.ui.theme.Surface
import com.example.fitfusion.ui.theme.Tertiary
import com.example.fitfusion.ui.theme.SurfaceContainerLow
import com.example.fitfusion.viewmodel.OnboardingViewModel

private val ACTIVITY_LEVELS = listOf("Sedentario", "Ligero", "Medio", "Alto", "Atleta")
private val GOAL_TYPES = listOf("Perder grasa", "Mantener peso", "Ganar músculo", "Mejorar resistencia")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaOnboarding(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.finished) {
        if (state.finished) onComplete()
    }

    BackHandler {
        if (state.step > 0) viewModel.previousStep()
    }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.onPhotoPicked(it) } }

    Scaffold(
        containerColor = Surface,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { viewModel.previousStep() },
                    enabled = state.step > 0,
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                }
                LinearProgressIndicator(
                    progress = { (state.step + 1) / 7f },
                    color = Primary,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                )
                Text("${state.step + 1}/7", fontSize = 13.sp, color = OnSurfaceVariant)
                Spacer(Modifier.width(12.dp))
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            when (state.step) {
                0 -> StepUsername(state.username, viewModel::onUsernameChange)
                1 -> StepPhoto(
                    photoUri = state.photoUri,
                    onPickPhoto = {
                        photoLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                )
                2 -> StepHeight(state.heightCm, viewModel::onHeightChange)
                3 -> StepWeight(state.weightKg, viewModel::onWeightChange)
                4 -> StepChips(
                    title = "¿Cuál es tu nivel de actividad?",
                    subtitle = "Esto nos ayuda a calibrar tus calorías diarias.",
                    options = ACTIVITY_LEVELS,
                    selected = state.activityLevel,
                    onSelect = viewModel::onActivityLevelChange,
                )
                5 -> StepBirthDate(state.birthDate, viewModel::onBirthDateChange)
                6 -> StepChips(
                    title = "¿Cuál es tu objetivo?",
                    subtitle = "Lo guardamos como referencia. No se mostrará en tu perfil público.",
                    options = GOAL_TYPES,
                    selected = state.goalType,
                    onSelect = viewModel::onGoalTypeChange,
                )
            }

            if (state.errorMessage != null) {
                Text(state.errorMessage!!, color = Tertiary, fontSize = 13.sp)
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = viewModel::nextStep,
                enabled = state.canAdvance && !state.isSaving,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        if (state.step >= 6) "Empezar" else "Continuar",
                        fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StepUsername(value: String, onChange: (String) -> Unit) {
    StepHeader(
        title = "Elige tu nombre de usuario",
        subtitle = "Será tu identidad en FitFusion. Solo letras, números, puntos y guiones bajos.",
    )
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text("Nombre de usuario") },
        placeholder = { Text("tu_usuario") },
        prefix = { Text("@", color = OnSurfaceVariant) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.None,
        ),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = SurfaceContainerLow,
            focusedContainerColor = SurfaceContainerLow,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Primary,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
    Text(
        "${value.length}/20",
        fontSize = 11.sp,
        color = OnSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.End,
    )
}

@Composable
private fun StepPhoto(photoUri: Uri?, onPickPhoto: () -> Unit) {
    StepHeader(
        title = "Añade una foto de perfil",
        subtitle = "Opcional. Puedes cambiarla cuando quieras desde tu perfil.",
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(SurfaceContainerLow)
                .clickable { onPickPhoto() },
            contentAlignment = Alignment.Center,
        ) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(32.dp),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Añadir foto", fontSize = 12.sp, color = OnSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun StepHeight(value: String, onChange: (String) -> Unit) {
    StepHeader(
        title = "¿Cuánto mides?",
        subtitle = "Tu altura nos ayuda a calcular tu metabolismo basal.",
    )
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text("Altura (cm)") },
        placeholder = { Text("175") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = SurfaceContainerLow,
            focusedContainerColor = SurfaceContainerLow,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Primary,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun StepWeight(value: String, onChange: (String) -> Unit) {
    StepHeader(
        title = "¿Cuánto pesas?",
        subtitle = "Lo usamos para tus estadísticas. Puedes actualizarlo cuando quieras.",
    )
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text("Peso (kg)") },
        placeholder = { Text("72.5") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = SurfaceContainerLow,
            focusedContainerColor = SurfaceContainerLow,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Primary,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun StepBirthDate(value: String, onChange: (String) -> Unit) {
    StepHeader(
        title = "¿Cuándo naciste?",
        subtitle = "Tu edad afecta tus recomendaciones diarias.",
    )
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text("Fecha de nacimiento") },
        placeholder = { Text("DD/MM/AAAA") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = SurfaceContainerLow,
            focusedContainerColor = SurfaceContainerLow,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Primary,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun StepChips(
    title: String,
    subtitle: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    StepHeader(title = title, subtitle = subtitle)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        options.forEach { option ->
            val isSelected = option == selected
            OutlinedButton(
                onClick = { onSelect(option) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) Primary.copy(alpha = 0.12f) else SurfaceContainerLow,
                    contentColor = if (isSelected) Primary else OnSurface,
                ),
            ) {
                Text(option, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun StepHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
        Text(title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = OnSurface, lineHeight = 32.sp)
        Text(subtitle, fontSize = 14.sp, color = OnSurfaceVariant, lineHeight = 20.sp)
    }
}
