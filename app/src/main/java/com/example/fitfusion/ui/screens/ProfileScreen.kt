package com.example.fitfusion.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import java.util.Locale
import com.example.fitfusion.data.models.UserPost
import com.example.fitfusion.data.models.UserPostType
import com.example.fitfusion.ui.components.*
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.FeedItem
import com.example.fitfusion.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaProfile(
    navController: NavHostController,
    userName: String?,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val state by profileViewModel.uiState.collectAsState()
    val tabs = listOf(
        Triple("Publicaciones", Icons.Default.GridView, 0),
        Triple("Guardado", Icons.Default.Bookmark, 1),
        Triple("Me gusta", Icons.Default.Favorite, 2),
    )

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { profileViewModel.updateProfilePhoto(it) } }

    LaunchedEffect(userName) { profileViewModel.updateFromUser(userName) }
    val primaryColor = Primary

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Surface),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // ── Top bar: @handle + icons ──────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    state.handle,
                    fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface
                )
                Row {
                    IconButton(onClick = profileViewModel::toggleSearchBar) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = if (state.showSearchBar) Primary else OnSurface
                        )
                    }
                    IconButton(onClick = { navController.navigate(Screens.HelpSupportScreen.name) }) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, "Dudas", tint = OnSurface)
                    }
                    IconButton(onClick = { navController.navigate(Screens.SettingsScreen.name) }) {
                        Icon(Icons.Default.Settings, "Ajustes", tint = OnSurface)
                    }
                }
            }
        }

        // ── Search bar (animated) ─────────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = state.showSearchBar,
                enter   = expandVertically(),
                exit    = shrinkVertically()
            ) {
                OutlinedTextField(
                    value         = state.searchQuery,
                    onValueChange = profileViewModel::onSearchQueryChange,
                    leadingIcon   = { Icon(Icons.Default.Search, null, tint = OnSurfaceVariant) },
                    placeholder   = {
                        Text(
                            when (state.selectedTab) {
                                0    -> "Buscar en tus posts"
                                else -> "Buscar"
                            },
                            color = OnSurfaceVariant, fontSize = 14.sp
                        )
                    },
                    singleLine    = true,
                    shape         = RoundedCornerShape(16.dp),
                    modifier      = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = SurfaceContainerLow,
                        focusedContainerColor   = SurfaceContainerLow,
                        unfocusedBorderColor    = Color.Transparent,
                        focusedBorderColor      = Primary,
                    )
                )
            }
        }

        // ── Instagram-style profile header ────────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainerLow)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                // Avatar (left) + Stats row (right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar with Primary ring + camera badge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.clickable {
                            photoLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Box(
                            modifier = Modifier.size(98.dp).drawBehind {
                                drawCircle(
                                    color  = primaryColor,
                                    radius = size.minDimension / 2,
                                    style  = Stroke(width = 5f)
                                )
                            }
                        )
                        if (state.profilePhotoUri != null) {
                            AsyncImage(
                                model              = state.profilePhotoUri,
                                contentDescription = "Foto de perfil",
                                contentScale       = ContentScale.Crop,
                                modifier           = Modifier.size(88.dp).clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceContainerHigh),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, null, Modifier.size(44.dp), tint = OnSurfaceVariant)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 2.dp, y = 2.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Cambiar foto",
                                tint               = Color.White,
                                modifier           = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Stats: Posts / Seguidores / Siguiendo
                    Row(
                        modifier            = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment   = Alignment.CenterVertically
                    ) {
                        StatBlock(value = state.postCount, label = "Publicaciones")
                        StatBlock(value = state.followers, label = "Seguidores")
                        StatBlock(value = state.following, label = "Siguiendo")
                    }
                }

                // Display name
                Text(
                    state.displayName,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = OnSurface,
                    modifier   = Modifier.padding(top = 12.dp)
                )

                // Bio
                if (state.bio.isNotBlank()) {
                    Text(
                        state.bio,
                        fontSize = 13.sp,
                        color    = OnSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Fitness summary chips
                ProfileFitnessSummary(
                    heightCm      = state.heightCm,
                    weightKg      = state.weightKg,
                    goalType      = state.goalType,
                    activityLevel = state.activityLevel,
                )

                // Edit profile button (Instagram style: full-width outlined)
                OutlinedButton(
                    onClick = { navController.navigate(Screens.AccountScreen.name) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    shape  = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = SurfaceContainerLowest,
                        contentColor   = OnSurface,
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OnSurface.copy(alpha = 0.3f))
                ) {
                    Text(
                        "Editar perfil",
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 14.sp
                    )
                }
            }
        }

        // ── Streak card ───────────────────────────────────────────────────
        item {
            StreakCard(
                streakDays = state.currentStreak,
                modifier   = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        // ── Tabs ──────────────────────────────────────────────────────────
        item {
            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor   = Color.Transparent,
                contentColor     = OnSurface,
                indicator        = { tabPositions ->
                    if (state.selectedTab < tabPositions.size) {
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedTab]),
                            color    = Primary
                        )
                    }
                }
            ) {
                tabs.forEach { (title, _, index) ->
                    val isSelected = state.selectedTab == index
                    val icon = when (index) {
                        0    -> Icons.Default.GridView
                        1    -> if (isSelected) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder
                        else -> if (isSelected) Icons.Default.Favorite else Icons.Default.FavoriteBorder
                    }
                    Tab(
                        selected = isSelected,
                        onClick  = { profileViewModel.onTabSelected(index) },
                        icon     = {
                            Icon(
                                icon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint     = if (isSelected) Primary else OnSurfaceVariant
                            )
                        },
                        text     = {
                            Text(
                                title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize   = 12.sp,
                                color      = if (isSelected) Primary else OnSurfaceVariant
                            )
                        }
                    )
                }
            }
        }

        // ── Tab content ───────────────────────────────────────────────────
        item {
            when (state.selectedTab) {
                0 -> PostsTab(posts = state.filteredPosts, isSearching = state.searchQuery.isNotBlank())
                1 -> SavedTab(items = state.savedFeedItems, onExplore = {
                    navController.navigate(Screens.HomeScreen.name) {
                        popUpTo(Screens.HomeScreen.name) { inclusive = true }
                    }
                })
                2 -> LikedTab(
                    items = state.likedFeedItems,
                    isLoading = state.isLoadingLikedPosts,
                    onExplore = {
                        navController.navigate(Screens.HomeScreen.name) {
                            popUpTo(Screens.HomeScreen.name) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}

// ─── StatBlock: number + label stacked (Instagram style) ─────────────────────
@Composable
private fun StatBlock(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = OnSurface)
        Text(label, fontSize = 12.sp, color = OnSurfaceVariant)
    }
}

// ─── ProfileFitnessSummary ────────────────────────────────────────────────────
@Composable
private fun ProfileFitnessSummary(
    heightCm: Int?,
    weightKg: Float?,
    goalType: String?,
    activityLevel: String?,
) {
    val items = buildList {
        heightCm?.let { add("${it} cm" to "ALTURA") }
        weightKg?.let { weight ->
            val formatted = if (weight % 1f == 0f) weight.toInt().toString() else "%.1f".format(Locale.US, weight)
            add("$formatted kg" to "PESO")
        }
        goalType?.takeIf { it.isNotBlank() }?.let { add(it to "OBJETIVO") }
        activityLevel?.takeIf { it.isNotBlank() }?.let { add(it to "ACTIVIDAD") }
    }.take(3)
    if (items.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { (value, label) ->
            val compactValue = if (value.length > 14) value.take(12) + "..." else value
            StatChip(value = compactValue, label = label, modifier = Modifier.weight(1f))
        }
    }
}

// ─── StreakCard ───────────────────────────────────────────────────────────────
@Composable
private fun StreakCard(streakDays: Int, modifier: Modifier = Modifier) {
    val (title, subtitle) = when {
        streakDays == 0  -> "Empieza tu racha" to "Registra un entreno hoy para arrancar"
        streakDays == 1  -> "1 día de racha" to "¡Buen comienzo! Mantén el ritmo mañana"
        streakDays < 7   -> "$streakDays días de racha" to "Vas encaminado, sigue así"
        streakDays < 30  -> "$streakDays días de racha" to "Impresionante constancia"
        else             -> "$streakDays días de racha" to "Eres una leyenda"
    }
    val accent = if (streakDays > 0) Primary else OnSurfaceVariant

    Card(
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint     = accent,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "RACHA ACTIVA",
                    fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp, color = accent
                )
                Text(
                    title,
                    fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    subtitle,
                    fontSize = 13.sp, color = OnSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

// ─── PostsTab: 3-column Instagram grid ───────────────────────────────────────
@Composable
private fun PostsTab(posts: List<UserPost>, isSearching: Boolean) {
    if (posts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isSearching) {
                    Text("Sin resultados", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnSurface, textAlign = TextAlign.Center)
                    Text("Prueba con otro término", fontSize = 13.sp, color = OnSurfaceVariant, textAlign = TextAlign.Center)
                } else {
                    Text("Sin publicaciones aún", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnSurface, textAlign = TextAlign.Center)
                    Text("Comparte tus entrenos desde el feed", fontSize = 13.sp, color = OnSurfaceVariant, textAlign = TextAlign.Center)
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            posts.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    row.forEach { post ->
                        PostGridCell(
                            post     = post,
                            modifier = Modifier.weight(1f).aspectRatio(1f)
                        )
                    }
                    // Fill remaining slots to keep grid alignment
                    repeat(3 - row.size) {
                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

// ─── PostGridCell ─────────────────────────────────────────────────────────────
@Composable
private fun PostGridCell(post: UserPost, modifier: Modifier = Modifier) {
    val bgBrush = when (post.type) {
        UserPostType.WORKOUT   -> GreenGradientBrush
        UserPostType.NUTRITION -> androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(Color(0xFFE65C00), Color(0xFFF9D423))
        )
    }
    Box(
        modifier = modifier.background(bgBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Icon(
                if (post.type == UserPostType.NUTRITION) Icons.Outlined.Restaurant else Icons.Outlined.FitnessCenter,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
            // Type badge
            val badgeText = when (post.type) {
                UserPostType.WORKOUT   -> "Entreno"
                UserPostType.NUTRITION -> "Receta"
            }
            Text(
                badgeText,
                fontSize   = 9.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White,
                modifier   = Modifier
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.25f))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                maxLines   = 1
            )
        }
    }
}

// ─── SavedTab: 3-column grid of saved FeedItems ──────────────────────────────
@Composable
private fun SavedTab(items: List<FeedItem>, onExplore: () -> Unit) {
    if (items.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Sin guardados", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = OnSurface)
                Text(
                    "Las publicaciones que guardes aparecerán aquí",
                    fontSize = 13.sp, color = OnSurfaceVariant, textAlign = TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(GreenGradientBrush)
                        .clickable(onClick = onExplore)
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Text("Explorar Feed", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    row.forEach { item ->
                        SavedGridCell(
                            item     = item,
                            modifier = Modifier.weight(1f).aspectRatio(1f)
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

// ─── SavedGridCell ────────────────────────────────────────────────────────────
@Composable
private fun SavedGridCell(item: FeedItem, modifier: Modifier = Modifier) {
    val (icon, bgBrush, badgeText) = when (item) {
        is FeedItem.Workout   -> Triple(
            Icons.Outlined.FitnessCenter,
            GreenGradientBrush,
            item.post.workoutName.take(10).ifBlank { "Entreno" }
        )
        is FeedItem.Nutrition -> Triple(
            Icons.Outlined.Restaurant,
            androidx.compose.ui.graphics.Brush.linearGradient(
                colors = listOf(Color(0xFFE65C00), Color(0xFFF9D423))
            ),
            item.post.title.take(10).ifBlank { "Receta" }
        )
    }
    Box(
        modifier = modifier.background(bgBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            Text(
                badgeText,
                fontSize   = 9.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White,
                modifier   = Modifier
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.25f))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
        }
    }
}

// ─── LikedTab: 3-column grid of liked FeedItems ──────────────────────────────
@Composable
private fun LikedTab(
    items: List<FeedItem>,
    isLoading: Boolean,
    onExplore: () -> Unit,
) {
    when {
        isLoading && items.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Primary)
            }
        }
        items.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Sin me gusta", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = OnSurface)
                    Text(
                        "Las publicaciones que marques con me gusta aparecerán aquí",
                        fontSize = 13.sp, color = OnSurfaceVariant, textAlign = TextAlign.Center
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(GreenGradientBrush)
                            .clickable(onClick = onExplore)
                            .padding(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        Text("Explorar Feed", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
        else -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        row.forEach { item ->
                            SavedGridCell(
                                item     = item,
                                modifier = Modifier.weight(1f).aspectRatio(1f)
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
}
