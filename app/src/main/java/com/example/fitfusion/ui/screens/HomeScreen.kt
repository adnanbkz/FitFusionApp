package com.example.fitfusion.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.fitfusion.ui.theme.OnSurface
import com.example.fitfusion.ui.theme.OnSurfaceVariant
import com.example.fitfusion.ui.theme.Primary
import com.example.fitfusion.ui.theme.Secondary
import com.example.fitfusion.ui.theme.Surface
import com.example.fitfusion.ui.theme.SurfaceContainerHigh
import com.example.fitfusion.ui.theme.SurfaceContainerLow
import com.example.fitfusion.ui.theme.SurfaceContainerLowest
import com.example.fitfusion.ui.theme.Tertiary
import com.example.fitfusion.viewmodel.ExerciseItem
import com.example.fitfusion.viewmodel.FeedItem
import com.example.fitfusion.viewmodel.HomeViewModel
import com.example.fitfusion.viewmodel.NutritionPost
import com.example.fitfusion.viewmodel.WorkoutPost

@Composable
fun PantallaHome(
    navController: NavHostController,
    userName: String?,
    homeViewModel: HomeViewModel = viewModel(),
) {
    val state by homeViewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceContainerLow)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── TopAppBar ────────────────────────────────────────────
            item {
                FeedTopBar()
            }

            // ── "Today's Pulse" editorial header ────────────────────
            item {
                Column(
                    modifier = Modifier
                        .background(Surface)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Text(
                        "Today's Pulse",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Primary,
                        letterSpacing = (-0.5).sp,
                    )
                    Text(
                        "Stay consistent, stay kinetic.",
                        fontSize = 14.sp,
                        color = OnSurfaceVariant,
                    )
                }
            }

            // ── Feed items ───────────────────────────────────────────
            items(state.items, key = { item ->
                when (item) {
                    is FeedItem.Workout -> item.post.id
                    is FeedItem.Nutrition -> item.post.id
                }
            }) { item ->
                when (item) {
                    is FeedItem.Workout -> WorkoutPostCard(
                        post = item.post,
                        onLikeClick = { homeViewModel.toggleLike(item.post.id) },
                        onCardClick = { navController.navigate(Screens.PostDetailScreen.name) },
                    )
                    is FeedItem.Nutrition -> NutritionPostCard(
                        post = item.post,
                        onLikeClick = { homeViewModel.toggleLike(item.post.id) },
                        onCardClick = { navController.navigate(Screens.PostDetailScreen.name) },
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // ── FAB ──────────────────────────────────────────────────────
        FloatingActionButton(
            onClick = { },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp),
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.Transparent,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 2.dp,
            )
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Brush.linearGradient(listOf(Primary, Color(0xFF32CD32))),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, "Nueva publicación", modifier = Modifier.size(26.dp))
            }
        }
    }
}

// ── TopBar ───────────────────────────────────────────────────────────────────

@Composable
private fun FeedTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface.copy(alpha = 0.95f))
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(modifier = Modifier.size(48.dp))

            Text(
                "FitSocial",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Primary,
                letterSpacing = (-0.5).sp,
            )

            IconButton(onClick = { }) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notificaciones",
                    tint = OnSurfaceVariant,
                )
            }
        }
    }
}

// ── Workout post card ────────────────────────────────────────────────────────

@Composable
private fun WorkoutPostCard(
    post: WorkoutPost,
    onLikeClick: () -> Unit,
    onCardClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onCardClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp, pressedElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Author row
            PostAuthorRow(
                initials = post.authorInitials,
                author = post.author,
                meta = "${post.timeAgo} · ${post.workoutType}",
                initialsColor = Primary,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Workout name + stats
            Text(
                post.workoutName,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Primary,
                letterSpacing = (-0.5).sp,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatBadge(
                    icon = { Icon(Icons.Outlined.Schedule, null, Modifier.size(14.dp), tint = Secondary) },
                    label = "${post.durationMin}min",
                    labelColor = OnSurface,
                )
                if (post.totalWeightKg > 0) {
                    StatBadge(
                        icon = { Icon(Icons.Outlined.FitnessCenter, null, Modifier.size(14.dp), tint = Secondary) },
                        label = "${"%.3f".format(post.totalWeightKg)}kg",
                        labelColor = Secondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Exercise list sub-card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLow)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                post.exercises.forEach { exercise ->
                    ExerciseRow(exercise)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            InteractionBar(
                likes = post.likes,
                comments = post.comments,
                isLiked = post.isLiked,
                onLikeClick = onLikeClick,
            )
        }
    }
}

// ── Nutrition post card ──────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NutritionPostCard(
    post: NutritionPost,
    onLikeClick: () -> Unit,
    onCardClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onCardClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp, pressedElevation = 0.dp),
    ) {
        Column {
            // Food image / placeholder with NUTRITION badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF2D5016),
                                Color(0xFF4A7C28),
                                Color(0xFFA8C97B),
                            )
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("🥗", fontSize = 56.sp)

                // NUTRITION pill
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.92f))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                ) {
                    Text(
                        "NUTRITION",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Primary,
                        letterSpacing = 1.5.sp,
                    )
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {

                // Compact author row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AuthorAvatar(initials = post.authorInitials, size = 32, color = Secondary)
                    Text(
                        post.author,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        post.timeAgo,
                        fontSize = 11.sp,
                        color = OnSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    post.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = OnSurface,
                    letterSpacing = (-0.5).sp,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    post.description,
                    fontSize = 14.sp,
                    color = OnSurfaceVariant,
                    lineHeight = 21.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Macro highlights
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MacroPill(value = "${post.kcal}", label = "Kcal", color = Primary, modifier = Modifier.weight(1f))
                    MacroPill(value = "${post.proteinG}g", label = "Prot", color = Secondary, modifier = Modifier.weight(1f))
                    MacroPill(value = "${post.carbsG}g", label = "Carbs", color = Tertiary, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                InteractionBar(
                    likes = post.likes,
                    comments = post.comments,
                    isLiked = post.isLiked,
                    onLikeClick = onLikeClick,
                )
            }
        }
    }
}

// ── Sub-components ───────────────────────────────────────────────────────────

@Composable
private fun PostAuthorRow(
    initials: String,
    author: String,
    meta: String,
    initialsColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AuthorAvatar(initials = initials, size = 44, color = initialsColor)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(author, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurface)
            Text(meta, fontSize = 12.sp, color = OnSurfaceVariant)
        }
        Icon(Icons.Default.MoreVert, null, tint = OnSurfaceVariant)
    }
}

@Composable
private fun AuthorAvatar(initials: String, size: Int, color: Color) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            initials,
            fontSize = (size * 0.35f).sp,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

@Composable
private fun StatBadge(icon: @Composable () -> Unit, label: String, labelColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        icon()
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = labelColor)
    }
}

@Composable
private fun ExerciseRow(exercise: ExerciseItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Text("💪", fontSize = 16.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(exercise.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnSurface)
            val detail = if (exercise.reps > 0) {
                "${exercise.sets} series · ${exercise.reps} reps"
            } else {
                "${exercise.sets} series"
            }
            Text(detail, fontSize = 12.sp, color = OnSurfaceVariant)
        }
    }
}

@Composable
private fun MacroPill(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerLow)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = color)
        Text(
            label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = OnSurfaceVariant,
        )
    }
}

@Composable
private fun InteractionBar(
    likes: Int,
    comments: Int,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
) {
    val likeColor by animateColorAsState(
        targetValue = if (isLiked) Tertiary else OnSurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "likeColor",
    )
    val likeScale by animateFloatAsState(
        targetValue = if (isLiked) 1.25f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "likeScale",
    )

    HorizontalDivider(
        color = SurfaceContainerHigh,
        modifier = Modifier.padding(bottom = 12.dp),
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.weight(1f),
        ) {
            // Like
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                IconButton(
                    onClick = onLikeClick,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Me gusta",
                        tint = likeColor,
                        modifier = Modifier
                            .size(22.dp)
                            .scale(likeScale),
                    )
                }
                Text("$likes", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            }
            // Comment
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Icon(
                    Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Comentarios",
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Text("$comments", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            }
        }
        // Share
        IconButton(
            onClick = { },
            modifier = Modifier.size(28.dp),
        ) {
            Icon(
                Icons.Default.Share,
                contentDescription = "Compartir",
                tint = OnSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
