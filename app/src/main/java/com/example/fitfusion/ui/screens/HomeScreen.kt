package com.fitfusion.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.fitfusion.app.ui.components.*
import com.fitfusion.app.ui.theme.*
import com.fitfusion.app.viewmodel.HomeViewModel

@Composable
fun PantallaHome(
    navController: NavHostController,
    userName: String?,
    homeViewModel: HomeViewModel = viewModel()
) {
    val state by homeViewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Surface)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(SurfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Person, null, Modifier.size(20.dp), tint = OnSurfaceVariant) }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Kinetic", fontSize = 22.sp, fontWeight = FontWeight.Black, color = OnSurface)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Search, "Search", tint = OnSurface)
                    }
                }
            }

            // Daily Momentum
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("DAILY MOMENTUM", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = OnSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("${state.momentumPercent}", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                                Text("%", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                            }
                            Text("${state.kcalRemaining} kcal remaining", fontSize = 14.sp, color = OnSurfaceVariant)
                        }
                        MomentumRing(progress = state.momentumPercent / 100f, size = 80)
                    }
                }
            }

            // Feed posts
            items(state.posts) { post ->
                FeedPost(
                    author = post.author,
                    time = post.time,
                    tag = post.tag,
                    likes = post.likes,
                    comments = post.comments,
                    description = post.description,
                    navController = navController
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }

        FloatingActionButton(
            onClick = { },
            containerColor = Primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 32.dp, bottom = 16.dp)
        ) { Icon(Icons.Default.Add, "New post") }
    }
}