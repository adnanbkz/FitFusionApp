package com.example.fitfusion.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
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
import com.example.fitfusion.ui.components.*
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.PostDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPostDetail(
    navController: NavHostController,
    postDetailViewModel: PostDetailViewModel = viewModel()
) {
    val state by postDetailViewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Surface)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            TopAppBar(
                title = { Text("FitSocial", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Primary) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Default.Share, "Compartir") }
                    IconButton(onClick = { }) { Icon(Icons.Default.MoreVert, "Más opciones") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(SurfaceContainerHigh), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, Modifier.size(24.dp), tint = OnSurfaceVariant)
                    }
                    Column {
                        Text(state.authorName, fontWeight = FontWeight.Bold, color = OnSurface)
                        Text(state.authorSubtitle, fontSize = 13.sp, color = OnSurfaceVariant)
                    }
                }
                OutlinedButton(onClick = { }, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Primary)) {
                    Text("Seguir", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .height(320.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PrimaryContainer.copy(alpha = 0.15f))
            ) {
                Icon(Icons.Default.Person, null, Modifier.size(80.dp).align(Alignment.Center), tint = Primary.copy(alpha = 0.2f))
                Text(
                    "MAÑANA + ENTRENAMIENTO",
                    fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 2.sp,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 60.dp)
                )
                Row(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(OnSurface.copy(alpha = 0.6f)).padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OverlayStat(state.miles, "MILLAS")
                    OverlayStat(state.time, "TIEMPO")
                    OverlayStat(state.pace, "RITMO")
                }
            }

            Row(
                modifier = Modifier.padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Favorite, null, Modifier.size(20.dp), tint = Tertiary)
                    Text(state.likeCount, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Email, null, Modifier.size(18.dp), tint = OnSurfaceVariant)
                    Text(state.commentCount, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("⚡", fontSize = 16.sp)
                    Text(state.energyCount, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                Text(state.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                Text(state.description, fontSize = 15.sp, color = OnSurface, lineHeight = 22.sp, modifier = Modifier.padding(top = 8.dp))
                Text(state.hashtags, fontSize = 14.sp, color = Primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("FRECUENCIA CARDÍACA MEDIA", state.avgHeartRate, "PPM", "❤️", Modifier.weight(1f))
                StatCard("CALORÍAS QUEMADAS", state.caloriesBurned, "KCAL", "\uD83D\uDD25", Modifier.weight(1f))
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Comentarios", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                        Text("  (${state.commentCount})", fontSize = 14.sp, color = OnSurfaceVariant)
                    }
                    Text("Recientes ▾", fontSize = 13.sp, color = Primary, fontWeight = FontWeight.SemiBold)
                }

                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    state.comments.forEach { comment ->
                        if (comment.isAuthorReply) {
                            Row(
                                modifier = Modifier.padding(start = 40.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(PrimaryContainer.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, null, Modifier.size(16.dp), tint = Primary)
                                }
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(comment.author, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Primary)
                                        Text(
                                            "AUTOR", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Primary,
                                            modifier = Modifier.background(PrimaryContainer.copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(comment.text, fontSize = 13.sp, color = Primary.copy(alpha = 0.85f))
                                    Text("${comment.time} • Responder", fontSize = 11.sp, color = OnSurfaceVariant)
                                }
                            }
                        } else {
                            CommentItem(comment.author, comment.text, comment.time, comment.likes)
                        }
                    }
                }
            }

            Box(modifier = Modifier.height(80.dp))
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(SurfaceContainerLowest)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(SurfaceContainerHigh), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, null, Modifier.size(18.dp), tint = OnSurfaceVariant)
            }
            OutlinedTextField(
                value = state.commentText,
                onValueChange = postDetailViewModel::onCommentTextChange,
                placeholder = { Text("Añadir un comentario...", color = OnSurfaceVariant, fontSize = 14.sp) },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = SurfaceContainerLow, focusedContainerColor = SurfaceContainerLow,
                    unfocusedBorderColor = Color.Transparent, focusedBorderColor = Primary
                ),
                singleLine = true
            )
            IconButton(
                onClick = { postDetailViewModel.sendComment() },
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Primary)
            ) { Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
        }
    }
}