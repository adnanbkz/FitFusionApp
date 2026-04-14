package com.example.fitfusion.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.fitfusion.R
import com.example.fitfusion.ui.components.*
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.TrackingViewModel

@Composable
fun PantallaTracking(
    navController: NavHostController,
    trackingViewModel: TrackingViewModel = viewModel()
) {
    val state by trackingViewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Surface).padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        // Top bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Primary),
                        contentAlignment = Alignment.Center
                    ) { Icon(painterResource(R.drawable.ic_dumbbell), null, Modifier.size(18.dp), tint = Color.White) }
                    Text("Kinetic", fontSize = 22.sp, fontWeight = FontWeight.Black, color = OnSurface)
                }
                IconButton(onClick = { }) { Icon(Icons.Default.Search, "Buscar", tint = OnSurface) }
            }
        }

        // Daily Goal ring
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("OBJETIVO DIARIO", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = Primary)
                    Box(contentAlignment = Alignment.Center) {
                        MomentumRing(progress = 0.65f, size = 140)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${state.kcalLeft}", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                            Text("KCAL RESTANTES", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = OnSurfaceVariant, letterSpacing = 1.sp)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatColumn("INGERIDAS", "${state.eaten}")
                        StatColumn("QUEMADAS", "${state.burned}")
                    }
                }
            }
        }

        // Macro Balance
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Balance de macros",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        MacroRow("PROTEÍNAS", state.protein, state.proteinGoal, Primary)
                        MacroRow("CARBOHIDRATOS", state.carbs, state.carbsGoal, Secondary)
                        MacroRow("GRASAS", state.fats, state.fatsGoal, Tertiary)
                    }
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("\uD83E\uDD16", fontSize = 20.sp)
                            Text(state.aiTip, fontSize = 13.sp, color = OnSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Log buttons
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { navController.navigate(Screens.AddFoodScreen.name) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, OnSurface.copy(alpha = 0.15f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Registrar comida",
                        color = OnSurface, fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
                OutlinedButton(
                    onClick = { navController.navigate(Screens.AddWorkoutScreen.name) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, OnSurface.copy(alpha = 0.15f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Registrar entrenamiento",
                        color = OnSurface, fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Recent Logs
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Registros recientes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                TextButton(onClick = { }) { Text("VER TODO", fontSize = 12.sp, color = Primary, fontWeight = FontWeight.Bold) }
            }
        }
        items(state.recentLogs) { log ->
            RecentLogItem(log.emoji, log.title, log.subtitle, log.calories, log.unit)
        }

        // Challenge card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(12.dp)).background(PrimaryContainer.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Person, null, Modifier.size(48.dp), tint = Primary.copy(alpha = 0.4f)) }
                    Text(
                        "NUEVO RETO",
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Tertiary,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                    Text(
                        "La semana de Sprint 10k",
                        fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OnSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        "Supera tus límites esta semana. Completa cinco sesiones de 10k para desbloquear la insignia 'Élite de resistencia'.",
                        fontSize = 14.sp, color = OnSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Button(
                        onClick = { },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OnSurface)
                    ) {
                        Text("Unirse al reto", color = SurfaceContainerLowest)
                    }
                }
            }
        }
    }
}