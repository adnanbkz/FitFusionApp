package com.example.fitfusion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.fitfusion.ui.components.*
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.ProfileViewModel

@Composable
fun PantallaProfile(
    navController: NavHostController,
    userName: String?,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val state by profileViewModel.uiState.collectAsState()
    val tabs = listOf("MIS PUBLICACIONES", "ESTADÍSTICAS", "ME GUSTA")

    LaunchedEffect(userName) { profileViewModel.updateFromUser(userName) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Surface),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(SurfaceContainerHigh), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, Modifier.size(20.dp), tint = OnSurfaceVariant)
                    }
                    Text("Kinetic", fontSize = 22.sp, fontWeight = FontWeight.Black, color = OnSurface)
                }
                Row {
                    IconButton(onClick = { }) { Icon(Icons.Default.Search, "Buscar", tint = OnSurface) }
                    IconButton(onClick = { navController.navigate(Screens.SettingsScreen.name) }) {
                        Icon(Icons.Default.Settings, "Ajustes", tint = OnSurface)
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth().background(SurfaceContainerLow).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(110.dp).drawBehind {
                        drawCircle(color = Primary, radius = size.minDimension / 2, style = Stroke(width = 6f))
                    })
                    Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(SurfaceContainerHigh), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, Modifier.size(50.dp), tint = OnSurfaceVariant)
                    }
                }
                Text(
                    state.displayName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OnSurface,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(state.handle, fontSize = 14.sp, color = OnSurfaceVariant)
                Text(
                    state.bio, fontSize = 14.sp, color = OnSurfaceVariant, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                Button(
                    onClick = { navController.navigate(Screens.AccountScreen.name) },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = ButtonDefaults.ContentPadding,
                    modifier = Modifier.height(55.dp)
                ) {
                    Box(
                        modifier = Modifier.background(GreenGradientBrush, RoundedCornerShape(20.dp)).padding(horizontal = 24.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Edit, null, Modifier.size(16.dp), tint = Color.White)
                            Text("Editar perfil", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip(state.postCount, Modifier.weight(1f))
                StatChip(state.followers, Modifier.weight(1f))
                StatChip(state.following, Modifier.weight(1f))
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Seguimiento de impulso", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                            Text("TENDENCIA ACTIVIDAD SEMANAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = OnSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(state.weeklyChange, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Primary)
                            Text("VS SEMANA ANTERIOR", fontSize = 10.sp, color = OnSurfaceVariant)
                        }
                    }
                    WeeklyBarChart()
                }
            }
        }

        item {
            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = Color.Transparent,
                contentColor = OnSurface,
                indicator = { tabPositions ->
                    if (state.selectedTab < tabPositions.size) {
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedTab]),
                            color = Primary
                        )
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { profileViewModel.onTabSelected(index) },
                        text = {
                            Text(title, fontWeight = if (state.selectedTab == index) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp, letterSpacing = 1.sp)
                        }
                    )
                }
            }
        }

        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().height(400.dp).padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.photoCount) {
                    Box(
                        modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(SurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Person, null, Modifier.size(32.dp), tint = OnSurfaceVariant.copy(alpha = 0.3f)) }
                }
                items(1) {
                    Box(
                        modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(SurfaceContainerLow),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, null, Modifier.size(28.dp), tint = OnSurfaceVariant.copy(alpha = 0.4f))
                            Text("NUEVA ENTRADA", fontSize = 10.sp, letterSpacing = 1.sp, color = OnSurfaceVariant.copy(alpha = 0.4f))
                        }
                    }
                }
            }
        }
    }
}