package com.example.fitfusion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.AddWorkoutViewModel
import com.example.fitfusion.viewmodel.FeaturedWorkout
import com.example.fitfusion.viewmodel.RecentWorkout
import com.example.fitfusion.viewmodel.WorkoutCategory

@Composable
fun PantallaAddWorkout(
    navController: NavHostController,
    addWorkoutViewModel: AddWorkoutViewModel = viewModel()
) {
    val state by addWorkoutViewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Surface)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Top Bar
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = OnSurface
                        )
                    }
                    Text(
                        "Añadir entrenamiento",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar", tint = OnSurface)
                    }
                }
            }

            // Featured Workout of the Day
            item {
                Spacer(modifier = Modifier.height(8.dp))
                FeaturedWorkoutCard(
                    workout = state.featured,
                    onPlay = addWorkoutViewModel::startFeaturedWorkout
                )
            }

            // Quick Start Header
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(


                        "Inicio rápido",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    TextButton(onClick = { }) {
                        Text(
                            "VER TODO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = Primary
                        )
                    }
                }
            }

            // Quick Start Grid (2 rows of 2)
            item {
                Spacer(modifier = Modifier.height(4.dp))
                val categories = WorkoutCategory.entries
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        categories.take(2).forEach { cat ->
                            QuickStartCard(
                                category = cat,
                                onClick = { addWorkoutViewModel.startCategory(cat) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        categories.drop(2).forEach { cat ->
                            QuickStartCard(
                                category = cat,
                                onClick = { addWorkoutViewModel.startCategory(cat) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Recent Workouts Header
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Entrenamientos recientes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Recent Workout Items
            items(state.recentWorkouts) { workout ->
                RecentWorkoutRow(
                    workout = workout,
                    isLogged = workout.id in state.loggedWorkoutIds,
                    onLog = { addWorkoutViewModel.logRecentWorkout(workout.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item { Spacer(modifier = Modifier.height(88.dp)) }
        }

        // FAB
        FloatingActionButton(
            onClick = { },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp),
            containerColor = Primary,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nuevo entrenamiento")
        }
    }
}

@Composable
private fun FeaturedWorkoutCard(
    workout: FeaturedWorkout,
    onPlay: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(160.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Dark gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1A2E1A),
                                Color(0xFF0D1F0D)
                            )
                        )
                    )
            )
            // Subtle green glow in top-right
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Primary.copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            radius = 400f
                        )
                    )
            )
            // Emoji figure centered
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(workout.emoji, fontSize = 64.sp)
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                // "WORKOUT OF THE DAY" badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Primary)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        "ENTRENAMIENTO DEL DÍA",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    workout.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WorkoutStatPill("${workout.durationMin} min")
                    WorkoutStatPill(workout.intensity)
                }
            }

            // Play button
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Primary)
                    .clickable(onClick = onPlay),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Iniciar",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun WorkoutStatPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

@Composable
private fun QuickStartCard(
    category: WorkoutCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = modifier
            .height(84.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(category.emoji, fontSize = 20.sp)
            }
            Column {
                Text(
                    category.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = OnSurface
                )
                Text(
                    category.label,
                    fontSize = 11.sp,
                    color = OnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun RecentWorkoutRow(
    workout: RecentWorkout,
    isLogged: Boolean,
    onLog: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(workout.emoji, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    workout.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${workout.durationMin} min",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant
                    )
                    Text("•", fontSize = 12.sp, color = OnSurfaceVariant)
                    Text(
                        workout.intensity,
                        fontSize = 12.sp,
                        color = OnSurfaceVariant
                    )
                    Text("•", fontSize = 12.sp, color = OnSurfaceVariant)
                    Text(
                        workout.metric,
                        fontSize = 12.sp,
                        color = OnSurfaceVariant
                    )
                }
            }
            // Add button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isLogged) Primary.copy(alpha = 0.2f) else Primary)
                    .clickable(enabled = !isLogged, onClick = onLog),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Registrar",
                    tint = if (isLogged) Primary else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}