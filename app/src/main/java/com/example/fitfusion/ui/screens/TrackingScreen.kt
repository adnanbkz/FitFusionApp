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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top bar
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Primary),
                        contentAlignment = Alignment.Center
                    ) { Icon(painterResource(R.drawable.ic_dumbbell), null, Modifier.size(18.dp), tint = Color.White) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kinetic", fontSize = 22.sp, fontWeight = FontWeight.Black, color = OnSurface)
                }
                IconButton(onClick = { }) { Icon(Icons.Default.Search, "Search", tint = OnSurface) }
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
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("DAILY GOAL", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = Primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(contentAlignment = Alignment.Center) {
                        MomentumRing(progress = 0.65f, size = 140)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${state.kcalLeft}", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                            Text("KCAL LEFT", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = OnSurfaceVariant, letterSpacing = 1.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatColumn("EATEN", "${state.eaten}")
                        StatColumn("BURNED", "${state.burned}")
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
                    Text("Macro Balance", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                    Spacer(modifier = Modifier.height(16.dp))
                    MacroRow("PROTEIN", state.protein, state.proteinGoal, Primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    MacroRow("CARBS", state.carbs, state.carbsGoal, Secondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    MacroRow("FATS", state.fats, state.fatsGoal, Tertiary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("\uD83E\uDD16", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
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
                    onClick = { }, modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, OnSurface.copy(alpha = 0.15f))
                ) { Text("Log Food", color = OnSurface, fontWeight = FontWeight.SemiBold) }
                OutlinedButton(
                    onClick = { }, modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, OnSurface.copy(alpha = 0.15f))
                ) { Text("Log Workout", color = OnSurface, fontWeight = FontWeight.SemiBold) }
            }
        }

        // Recent Logs
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Recent Logs", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                TextButton(onClick = { }) { Text("VIEW ALL", fontSize = 12.sp, color = Primary, fontWeight = FontWeight.Bold) }
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
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("NEW CHALLENGE", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Tertiary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("The 10k Sprint Week", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Push your limits this week. Complete five 10k sessions to unlock the 'Endurance Elite' badge.", fontSize = 14.sp, color = OnSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = OnSurface)) {
                        Text("Join Challenge", color = SurfaceContainerLowest)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
