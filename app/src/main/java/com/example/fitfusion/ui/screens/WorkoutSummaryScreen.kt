package com.example.fitfusion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.fitfusion.ui.components.IntensityZoneRow
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.WorkoutSummaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaWorkoutSummary(
    navController: NavHostController,
    workoutSummaryViewModel: WorkoutSummaryViewModel = viewModel()
) {
    val state by workoutSummaryViewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Surface)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            TopAppBar(
                title = { Text("Resumen del entrenamiento", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás")
                    }
                },
                actions = { IconButton(onClick = { }) { Icon(Icons.Default.Share, "Compartir") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )

            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        state.sessionType,
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Primary,
                        modifier = Modifier
                            .background(PrimaryContainer.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    Text(state.title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OnSurface, modifier = Modifier.padding(top = 8.dp))
                    Text(state.loggedDate, fontSize = 14.sp, color = OnSurfaceVariant)
                }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("ENERGÍA TOTAL QUEMADA", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = OnSurfaceVariant)
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(state.totalKcal, fontSize = 56.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                            Text(" KCAL", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 10.dp))
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                        elevation = CardDefaults.cardElevation(0.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("DURACIÓN", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = OnSurfaceVariant)
                            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 4.dp)) {
                                Text(state.duration, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                                Text(" MINS", fontSize = 12.sp, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                            }
                        }
                    }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                        elevation = CardDefaults.cardElevation(0.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("RITMO MEDIO", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = OnSurfaceVariant)
                            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 4.dp)) {
                                Text(state.pace, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                                Text(" /MI", fontSize = 12.sp, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.verticalGradient(listOf(PrimaryContainer.copy(alpha = 0.1f), SurfaceContainerLow))),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(state.mapLocation, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text("\uD83D\uDCCD", fontSize = 12.sp)
                            Text(state.mapCity, fontSize = 13.sp, color = OnSurfaceVariant)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Zonas de intensidad", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                    TextButton(onClick = { }) {
                        Text("Ver detalles", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    state.zones.forEach { zone ->
                        IntensityZoneRow(
                            label = zone.label, duration = zone.duration,
                            bpmRange = zone.bpmRange, percentage = zone.percentage,
                            color = zone.color, progress = zone.progress
                        )
                    }
                }

                if (state.goalReached) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryContainer.copy(alpha = 0.1f)),
                        elevation = CardDefaults.cardElevation(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("¡Objetivo\ndiario alcanzado!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Primary, lineHeight = 26.sp)
                                Text(state.goalMessage, fontSize = 14.sp, color = OnSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
                            }
                            Box(
                                modifier = Modifier.size(64.dp).clip(CircleShape).background(Primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Primary),
                                    contentAlignment = Alignment.Center
                                ) { Text("\uD83C\uDFAF", fontSize = 24.sp) }
                            }
                        }
                    }
                }

                Box(modifier = Modifier.height(16.dp))
            }
        }

        FloatingActionButton(
            onClick = { },
            containerColor = Primary, contentColor = Color.White, shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 32.dp, bottom = 16.dp)
        ) { Icon(Icons.Default.Add, "Nuevo") }
    }
}