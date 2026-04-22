package com.example.fitfusion.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.fitfusion.data.models.ExerciseCatalogItem
import com.example.fitfusion.data.repository.ExerciseRepository
import com.example.fitfusion.util.EquipmentTranslations
import com.example.fitfusion.ui.theme.OnSurface
import com.example.fitfusion.ui.theme.OnSurfaceVariant
import com.example.fitfusion.ui.theme.Primary
import com.example.fitfusion.ui.theme.PrimaryContainer
import com.example.fitfusion.ui.theme.Surface
import com.example.fitfusion.ui.theme.SurfaceContainerHigh
import com.example.fitfusion.ui.theme.SurfaceContainerLow
import com.example.fitfusion.ui.theme.SurfaceContainerLowest
import com.example.fitfusion.ui.theme.Tertiary

private sealed interface DetailState {
    data object Loading : DetailState
    data class Success(val exercise: ExerciseCatalogItem) : DetailState
    data class Error(val message: String) : DetailState
    data object NotFound : DetailState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaExerciseDetail(
    navController: NavHostController,
    documentId: String,
) {
    val repository = remember { ExerciseRepository() }
    var state: DetailState by remember { mutableStateOf(DetailState.Loading) }

    LaunchedEffect(documentId) {
        repository.fetchExerciseById(
            documentId = documentId,
            onSuccess = { exercise ->
                state = if (exercise == null) DetailState.NotFound else DetailState.Success(exercise)
            },
            onError = { e ->
                state = DetailState.Error(e.localizedMessage ?: "Error desconocido")
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item {
            TopAppBar(
                title = {
                    if (state is DetailState.Success) {
                        Text(
                            (state as DetailState.Success).exercise.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            maxLines = 1,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }

        when (val s = state) {
            DetailState.Loading -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Cargando ejercicio...", color = OnSurfaceVariant)
                    }
                }
            }

            is DetailState.Error -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(s.message, color = Tertiary, fontSize = 14.sp)
                }
            }

            DetailState.NotFound -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Ejercicio no encontrado.", color = OnSurfaceVariant)
                }
            }

            is DetailState.Success -> {
                item { ExerciseDetailContent(exercise = s.exercise) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExerciseDetailContent(exercise: ExerciseCatalogItem) {
    val context = LocalContext.current

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = SurfaceContainerLowest),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    exercise.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    exercise.slug,
                    fontSize = 13.sp,
                    color = OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    exercise.difficultyLevel?.let { DifficultyBadge(it) }
                    exercise.bodyRegion?.let { DetailChip(it) }
                    exercise.mechanics?.let { DetailChip(it) }
                    exercise.laterality?.let { DetailChip(it) }
                    if (exercise.isCombinationExercise == true) DetailChip("Ejercicio combinado")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        DetailSection(title = "Músculos") {
            if (exercise.primeMoverMuscle != null) {
                MuscleRow(label = "Motor principal", value = exercise.primeMoverMuscle, dotColor = Primary)
            }
            if (exercise.secondaryMuscle != null) {
                MuscleRow(label = "Secundario", value = exercise.secondaryMuscle, dotColor = Primary.copy(alpha = 0.55f))
            }
            if (exercise.tertiaryMuscle != null) {
                MuscleRow(label = "Terciario", value = exercise.tertiaryMuscle, dotColor = Primary.copy(alpha = 0.30f))
            }
            if (exercise.muscleGroup != null) {
                InfoRow(label = "Grupo muscular", value = exercise.muscleGroup)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        DetailSection(title = "Equipamiento") {
            exercise.primaryEquipment?.let { InfoRow(label = "Principal", value = EquipmentTranslations.translate(it)) }
            exercise.secondaryEquipment?.let { InfoRow(label = "Secundario", value = EquipmentTranslations.translate(it)) }
            exercise.loadPosition?.let { InfoRow(label = "Posición de carga", value = it) }
        }

        Spacer(modifier = Modifier.height(12.dp))

        DetailSection(title = "Técnica") {
            exercise.posture?.let { InfoRow(label = "Postura", value = it) }
            exercise.footElevation?.let { InfoRow(label = "Elevación de pie", value = it) }
            exercise.grip?.let { InfoRow(label = "Agarre", value = it) }
            exercise.armMode?.let { InfoRow(label = "Modo de brazo", value = it) }
            exercise.armPattern?.let { InfoRow(label = "Patrón de brazo", value = it) }
            exercise.legPattern?.let { InfoRow(label = "Patrón de pierna", value = it) }
            exercise.forceType?.let { InfoRow(label = "Tipo de fuerza", value = it) }
        }

        Spacer(modifier = Modifier.height(12.dp))

        DetailSection(title = "Clasificación") {
            exercise.primaryExerciseClassification?.let { InfoRow(label = "Clasificación", value = it) }
            exercise.difficultyLevel?.let { InfoRow(label = "Dificultad", value = it) }
            InfoRow(label = "ID de ejercicio", value = exercise.exerciseId)
        }

        val hasVideo = exercise.shortYoutubeDemoUrl != null || exercise.inDepthYoutubeTechniqueUrl != null
        if (hasVideo) {
            Spacer(modifier = Modifier.height(12.dp))
            DetailSection(title = "Videos") {
                exercise.shortYoutubeDemoUrl?.let { url ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Icon(Icons.Outlined.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Demo corta (YouTube)")
                    }
                }
                exercise.inDepthYoutubeTechniqueUrl?.let { url ->
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                    ) {
                        Icon(Icons.Outlined.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Técnica en profundidad (YouTube)")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = SurfaceContainerLowest),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Primary,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = SurfaceContainerHigh)
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            fontSize = 14.sp,
            color = OnSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = OnSurface,
            modifier = Modifier.weight(1.2f)
        )
    }
}

@Composable
private fun MuscleRow(label: String, value: String, dotColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, color = OnSurfaceVariant)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = OnSurface)
        }
    }
}

@Composable
private fun DifficultyBadge(level: String) {
    val (bg, fg) = when (level.lowercase()) {
        "novice" -> Pair(Primary.copy(alpha = 0.10f), Primary)
        "beginner" -> Pair(Color(0xFF1565C0).copy(alpha = 0.10f), Color(0xFF1565C0))
        "intermediate" -> Pair(Color(0xFFE65100).copy(alpha = 0.10f), Color(0xFFE65100))
        "advanced" -> Pair(Tertiary.copy(alpha = 0.10f), Tertiary)
        else -> Pair(SurfaceContainerLow, OnSurfaceVariant)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(level, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

@Composable
private fun DetailChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(SurfaceContainerLow)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 12.sp, color = OnSurfaceVariant)
    }
}
