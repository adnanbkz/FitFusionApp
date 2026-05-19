package com.example.fitfusion.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.fitfusion.data.models.UserPost
import com.example.fitfusion.data.models.UserPostType
import com.example.fitfusion.ui.components.StatChip
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.UserScreenViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaUserScreen(
    navController: NavHostController,
    uid: String,
    viewModel: UserScreenViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(uid) { viewModel.load(uid) }

    val primaryColor = Primary

    if (state.isLoading && state.profile == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Surface),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = Primary)
        }
        return
    }

    if (state.errorMessage != null && state.profile == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Surface),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 40.dp),
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = OnSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(56.dp),
                )
                Text(
                    "No se pudo cargar el perfil",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface,
                )
                Text(
                    state.errorMessage!!,
                    fontSize = 13.sp,
                    color = OnSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Volver", color = Primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        return
    }

    val profile = state.profile ?: return

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Surface),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        // ── Top bar: back arrow (left) + @handle (right) ──────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = OnSurface)
                }
                Text(
                    profile.username,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    modifier = Modifier.padding(end = 16.dp),
                )
            }
        }

        // ── Profile header ────────────────────────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainerLow)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                // Avatar (left) + Stats row (right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Avatar with Primary ring (no camera badge, not clickable)
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier.size(98.dp).drawBehind {
                                drawCircle(
                                    color = primaryColor,
                                    radius = size.minDimension / 2,
                                    style = Stroke(width = 5f),
                                )
                            },
                        )
                        if (!profile.photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = profile.photoUrl,
                                contentDescription = "Foto de perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(88.dp).clip(CircleShape),
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceContainerHigh),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Default.Person, null, Modifier.size(44.dp), tint = OnSurfaceVariant)
                            }
                        }
                    }

                    // Stats: Posts / Seguidores / Siguiendo
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        UserStatBlock(value = state.posts.size.toString(), label = "Publicaciones")
                        UserStatBlock(
                            value = compactCount(state.followersCount),
                            label = "Seguidores",
                            onClick = { navController.navigate("${Screens.FollowListScreen.name}/$uid/followers") },
                        )
                        UserStatBlock(
                            value = compactCount(state.followingCount),
                            label = "Siguiendo",
                            onClick = { navController.navigate("${Screens.FollowListScreen.name}/$uid/following") },
                        )
                    }
                }

                // Display name
                Text(
                    profile.displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    modifier = Modifier.padding(top = 12.dp),
                )

                // Bio
                if (profile.bio.isNotBlank()) {
                    Text(
                        profile.bio,
                        fontSize = 13.sp,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }

                // Fitness chips
                UserFitnessSummary(
                    heightCm = profile.heightCm,
                    weightKg = profile.weightKg,
                    goalType = profile.goalType,
                    activityLevel = profile.activityLevel,
                    showHeight = profile.showHeight,
                    showWeight = profile.showWeight,
                    showGoal = profile.showGoal,
                    showActivity = profile.showActivity,
                )

                // Follow / Unfollow button (full-width, Instagram style)
                if (state.isFollowing) {
                    OutlinedButton(
                        onClick = viewModel::toggleFollow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SurfaceContainerLowest,
                            contentColor = OnSurface,
                        ),
                        border = BorderStroke(1.dp, OnSurface.copy(alpha = 0.3f)),
                    ) {
                        Text("Siguiendo", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                } else {
                    Button(
                        onClick = viewModel::toggleFollow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    ) {
                        Text("Seguir", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White)
                    }
                }
            }
        }

        // ── Tab row ───────────────────────────────────────────────────────
        item {
            TabRow(
                selectedTabIndex = 0,
                containerColor = Color.Transparent,
                contentColor = OnSurface,
                indicator = { tabPositions ->
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[0]),
                        color = Primary,
                    )
                },
            ) {
                Tab(
                    selected = true,
                    onClick = {},
                    icon = {
                        Icon(
                            Icons.Default.GridView,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Primary,
                        )
                    },
                    text = {
                        Text(
                            "Publicaciones",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Primary,
                        )
                    },
                )
            }
        }

        // ── Posts grid ────────────────────────────────────────────────────
        item {
            UserPostsTab(
                posts = state.posts,
                onPostClick = { postId ->
                    navController.navigate("${Screens.PostDetailScreen.name}/$postId")
                },
            )
        }
    }
}

@Composable
private fun UserPostsTab(posts: List<UserPost>, onPostClick: (String) -> Unit) {
    if (posts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "Sin publicaciones aún",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface,
                )
                Text(
                    "Este usuario aún no ha publicado",
                    fontSize = 13.sp,
                    color = OnSurfaceVariant,
                )
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            posts.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    row.forEach { post ->
                        UserPostGridCell(
                            post = post,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            onClick = { onPostClick(post.id) },
                        )
                    }
                    repeat(3 - row.size) {
                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun UserStatBlock(value: String, label: String, onClick: (() -> Unit)? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (onClick != null) {
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        } else {
            Modifier
        },
    ) {
        Text(value, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = OnSurface)
        Text(label, fontSize = 12.sp, color = OnSurfaceVariant)
    }
}

@Composable
private fun UserFitnessSummary(
    heightCm: Int?,
    weightKg: Float?,
    goalType: String?,
    activityLevel: String?,
    showHeight: Boolean,
    showWeight: Boolean,
    showGoal: Boolean,
    showActivity: Boolean,
) {
    val items = buildList {
        if (showHeight) heightCm?.let { add("${it} cm" to "ALTURA") }
        if (showWeight) weightKg?.let { w ->
            val fmt = if (w % 1f == 0f) w.toInt().toString() else "%.1f".format(Locale.US, w)
            add("$fmt kg" to "PESO")
        }
        if (showGoal) goalType?.takeIf { it.isNotBlank() }?.let { add(it to "OBJETIVO") }
        if (showActivity) activityLevel?.takeIf { it.isNotBlank() }?.let { add(it to "ACTIVIDAD") }
    }.take(4)
    if (items.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { (value, label) ->
            val chipColor = when (label) {
                "ALTURA"   -> Color(0xFF4A90D9)
                "PESO"     -> Color(0xFFE0844A)
                "OBJETIVO" -> Primary
                else       -> Tertiary
            }
            StatChip(value = value, label = label, color = chipColor, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun UserPostGridCell(post: UserPost, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val thumbnailUrl = when (post.type) {
        UserPostType.WORKOUT   -> post.workoutMediaUrls.firstOrNull()
        UserPostType.NUTRITION -> post.nutritionPhotoUri
    }
    val bgBrush = when (post.type) {
        UserPostType.WORKOUT   -> GreenGradientBrush
        UserPostType.NUTRITION -> androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(Color(0xFFE65C00), Color(0xFFF9D423))
        )
    }
    val badgeText = when (post.type) {
        UserPostType.WORKOUT   -> "Entreno"
        UserPostType.NUTRITION -> "Receta"
    }
    Box(
        modifier = modifier.background(bgBrush).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (thumbnailUrl != null) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
            ) {
                Text(badgeText, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(4.dp),
            ) {
                Icon(
                    if (post.type == UserPostType.NUTRITION) Icons.Outlined.Restaurant else Icons.Outlined.FitnessCenter,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    badgeText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.25f))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    maxLines = 1,
                )
            }
        }
    }
}

private fun compactCount(value: Int): String = when {
    value >= 10_000 -> "${value / 1_000}k"
    value >= 1_000  -> "${value / 1_000}.${(value % 1_000) / 100}k"
    else            -> value.toString()
}
