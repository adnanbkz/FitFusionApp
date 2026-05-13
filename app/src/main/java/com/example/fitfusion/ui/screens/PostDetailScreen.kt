package com.example.fitfusion.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.fitfusion.ui.components.*
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.ExerciseItem
import com.example.fitfusion.viewmodel.PostDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPostDetail(
    navController: NavHostController,
    postId: String? = null,
    postDetailViewModel: PostDetailViewModel = viewModel()
) {
    val state by postDetailViewModel.uiState.collectAsState()
    var showComments by remember { mutableStateOf(false) }

    LaunchedEffect(postId) {
        postDetailViewModel.loadPost(postId)
    }

    if (showComments && !postId.isNullOrBlank()) {
        CommentsBottomSheet(postId = postId, onDismiss = { showComments = false })
    }

    Box(modifier = Modifier.fillMaxSize().background(Surface)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(SurfaceContainerHigh),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Person, null, Modifier.size(18.dp), tint = OnSurfaceVariant)
                        }
                        Column {
                            Text(state.authorName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = OnSurface)
                            if (state.authorSubtitle.isNotBlank()) {
                                Text(state.authorSubtitle, fontSize = 11.sp, color = OnSurfaceVariant)
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = OnSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Default.Share, "Compartir", tint = OnSurface) }
                    IconButton(onClick = { }) { Icon(Icons.Default.MoreVert, "Más opciones", tint = OnSurface) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .background(PrimaryContainer.copy(alpha = 0.15f))
            ) {
                if (state.mediaUri != null) {
                    AsyncImage(
                        model = state.mediaUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize(),
                    )
                } else {
                    Icon(
                        Icons.Default.FitnessCenter,
                        null,
                        Modifier.size(72.dp).align(Alignment.Center),
                        tint = Primary.copy(alpha = 0.25f),
                    )
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    OverlayStat(state.statOneValue, state.statOneLabel)
                    OverlayStat(state.statTwoValue, state.statTwoLabel)
                    OverlayStat(state.statThreeValue, state.statThreeLabel)
                }
            }

            val likeColor by animateColorAsState(
                targetValue = if (state.isLiked) Tertiary else OnSurfaceVariant,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "likeColor",
            )
            val likeScale by animateFloatAsState(
                targetValue = if (state.isLiked) 1.25f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "likeScale",
            )
            val saveColor by animateColorAsState(
                targetValue = if (state.isSaved) Primary else OnSurfaceVariant,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "saveColor",
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    IconButton(onClick = { postDetailViewModel.toggleLike() }) {
                        Icon(
                            if (state.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            "Me gusta",
                            Modifier.size(24.dp).scale(likeScale),
                            tint = likeColor,
                        )
                    }
                    IconButton(onClick = { showComments = true }) {
                        Icon(Icons.Outlined.ChatBubbleOutline, "Comentarios", Modifier.size(22.dp), tint = OnSurfaceVariant)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Share, "Compartir", Modifier.size(22.dp), tint = OnSurfaceVariant)
                    }
                }
                IconButton(onClick = { postDetailViewModel.toggleSave() }) {
                    Icon(
                        if (state.isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        if (state.isSaved) "Guardado" else "Guardar",
                        Modifier.size(24.dp),
                        tint = saveColor,
                    )
                }
            }

            if (state.likeCount.isNotBlank() && state.likeCount != "0") {
                Text(
                    "${state.likeCount} me gusta",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = OnSurface,
                    modifier = Modifier.padding(horizontal = 14.dp).padding(bottom = 4.dp),
                )
            }

            if (state.title.isNotBlank()) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(state.authorName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = OnSurface)
                    Text(state.title, fontSize = 14.sp, color = OnSurface, modifier = Modifier.weight(1f, fill = false))
                }
            }

            if (state.isWorkout && state.exercises.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.exercises.forEach { exercise ->
                        DetailExerciseRow(exercise)
                    }
                }
            }

            if (!state.isWorkout) {
                if (state.description.isNotBlank()) {
                    Text(
                        state.description,
                        fontSize = 14.sp,
                        color = OnSurface,
                        lineHeight = 21.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    )
                }
                if (state.kcal > 0 || state.proteinG > 0 || state.carbsG > 0) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (state.kcal > 0) DetailMacroChip("${state.kcal}", "kcal", Primary)
                        if (state.proteinG > 0) DetailMacroChip("${state.proteinG}g", "prot", Tertiary)
                        if (state.carbsG > 0) DetailMacroChip("${state.carbsG}g", "carbs", Secondary)
                    }
                }
            }

            if (state.commentCount != "0") {
                Text(
                    "Ver los ${state.commentCount} comentarios",
                    fontSize = 14.sp,
                    color = OnSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                        .clickable { showComments = true },
                )
            }

            Box(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DetailExerciseRow(exercise: ExerciseItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Primary.copy(alpha = 0.6f)))
        Text(exercise.name, fontSize = 14.sp, color = OnSurface, modifier = Modifier.weight(1f))
        Text(
            buildString {
                append("${exercise.sets}×${exercise.reps}")
                if (exercise.weightKg > 0.0) append(" · ${exercise.weightKg.toInt()}kg")
            },
            fontSize = 13.sp,
            color = OnSurfaceVariant,
        )
    }
}

@Composable
private fun DetailMacroChip(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
        Text(label, fontSize = 11.sp, color = color.copy(alpha = 0.7f), fontWeight = FontWeight.SemiBold)
    }
}
