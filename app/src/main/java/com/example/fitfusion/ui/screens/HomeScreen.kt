package com.example.fitfusion.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.fitfusion.data.repository.UserProfileStore
import com.example.fitfusion.ui.components.CreatePostSheetHost
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
import com.example.fitfusion.viewmodel.FeedFilter
import com.example.fitfusion.viewmodel.FeedItem
import com.example.fitfusion.viewmodel.HomeViewModel
import com.example.fitfusion.viewmodel.NutritionPost
import com.example.fitfusion.viewmodel.ProfileViewModel
import com.example.fitfusion.viewmodel.WorkoutPost
import kotlinx.coroutines.launch
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHome(
    navController: NavHostController,
    userName: String?,
    homeViewModel: HomeViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(),
) {
    val state by homeViewModel.uiState.collectAsState()
    val photoUri by UserProfileStore.photoUri.collectAsState()

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true,
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })
    val scope = rememberCoroutineScope()

    BackHandler(
        enabled = sheetState.currentValue == SheetValue.Expanded || pagerState.currentPage != 1
    ) {
        scope.launch {
            when {
                sheetState.currentValue == SheetValue.Expanded -> sheetState.partialExpand()
                pagerState.currentPage != 1                    -> pagerState.animateScrollToPage(1)
            }
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 72.dp,
        sheetContainerColor = Surface,
        sheetContentColor = OnSurface,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetDragHandle = { TrackingSheetHandle() },
        sheetContent = {
            PantallaTracking(navController = navController)
        },
        containerColor = SurfaceContainerLow,
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) { page ->
            when (page) {
                0 -> PantallaCamera(
                    onClose = {
                        scope.launch { pagerState.animateScrollToPage(1) }
                    },
                    onMediaCaptured = { uri, isVideo ->
                        profileViewModel.showCreatePostWithMedia(uri, isVideo)
                        scope.launch { pagerState.animateScrollToPage(1) }
                    },
                )
                1 -> HomeFeedPage(
                    state = state,
                    userName = userName,
                    photoUri = photoUri?.toString(),
                    onAvatarClick = {
                        scope.launch { pagerState.animateScrollToPage(2) }
                    },
                    onFabClick = profileViewModel::showCreatePost,
                    onPostClick = { postId ->
                        navController.navigate("${Screens.PostDetailScreen.name}/$postId")
                    },
                    onLikeClick = homeViewModel::toggleLike,
                    onFilterSelect = homeViewModel::setFilter,
                )
                2 -> PantallaProfile(
                    navController = navController,
                    userName = userName,
                    profileViewModel = profileViewModel,
                )
            }
        }
    }

    CreatePostSheetHost(
        profileViewModel = profileViewModel,
        onNavigateToAddWorkout = {
            navController.navigate("${Screens.AddWorkoutScreen.name}?logMode=true")
        },
    )
}

@Composable
private fun HomeFeedPage(
    state: com.example.fitfusion.viewmodel.FeedUiState,
    userName: String?,
    photoUri: String?,
    onAvatarClick: () -> Unit,
    onFabClick: () -> Unit,
    onPostClick: (String) -> Unit,
    onLikeClick: (String) -> Unit,
    onFilterSelect: (FeedFilter) -> Unit,
) {
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
            item {
                FeedTopBar(
                    photoUri = photoUri,
                    userName = userName,
                    onAvatarClick = onAvatarClick,
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .background(Surface)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Text(
                        greetingForNow(userName),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Primary,
                        letterSpacing = (-0.5).sp,
                    )
                    Text(
                        "Lo último de la comunidad",
                        fontSize = 14.sp,
                        color = OnSurfaceVariant,
                    )
                }
            }

            item {
                FeedFilterRow(
                    current  = state.filter,
                    onSelect = onFilterSelect,
                    modifier = Modifier
                        .background(Surface)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp)
                )
            }

            val filtered = state.filteredItems
            if (filtered.isEmpty()) {
                item { FeedEmptyState(filter = state.filter) }
            } else {
                items(filtered, key = { item ->
                    when (item) {
                        is FeedItem.Workout   -> item.post.id
                        is FeedItem.Nutrition -> item.post.id
                    }
                }) { item ->
                    when (item) {
                        is FeedItem.Workout -> WorkoutPostCard(
                            post        = item.post,
                            onLikeClick = { onLikeClick(item.post.id) },
                            onCardClick = { onPostClick(item.post.id) },
                        )
                        is FeedItem.Nutrition -> NutritionPostCard(
                            post        = item.post,
                            onLikeClick = { onLikeClick(item.post.id) },
                            onCardClick = { onPostClick(item.post.id) },
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        FloatingActionButton(
            onClick = onFabClick,
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

@Composable
private fun TrackingSheetHandle() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(OnSurfaceVariant.copy(alpha = 0.35f))
        )
        Text(
            "TRACKING",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            color = Primary,
        )
    }
}


private fun greetingForNow(userName: String?): String {
    val hour = LocalTime.now().hour
    val greeting = when (hour) {
        in 6..11  -> "Buenos días"
        in 12..19 -> "Buenas tardes"
        else      -> "Buenas noches"
    }
    val firstName = userName?.trim()?.split(' ')?.firstOrNull()?.takeIf { it.isNotBlank() }
    return if (firstName != null) "$greeting, $firstName" else greeting
}

@Composable
private fun FeedFilterRow(
    current: FeedFilter,
    onSelect: (FeedFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = listOf(
        Triple(FeedFilter.ALL, "Todo", null),
        Triple(FeedFilter.WORKOUTS, "Entrenos", Icons.Outlined.FitnessCenter),
        Triple(FeedFilter.NUTRITION, "Recetas", Icons.Default.Restaurant),
    )
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { (filter, label, icon) ->
            val selected = current == filter
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (selected) Primary else SurfaceContainerLow)
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (icon != null) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (selected) Color.White else OnSurfaceVariant
                    )
                }
                Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) Color.White else OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FeedEmptyState(filter: FeedFilter) {
    val message = when (filter) {
        FeedFilter.ALL       -> "Aún no hay publicaciones"
        FeedFilter.WORKOUTS  -> "Aún no hay entrenos publicados"
        FeedFilter.NUTRITION -> "Aún no hay recetas publicadas"
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(message, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnSurface)
            Text(
                "Publica un entreno o una receta para empezar el feed",
                fontSize = 13.sp, color = OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun FeedTopBar(
    photoUri: String?,
    userName: String?,
    onAvatarClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface.copy(alpha = 0.95f))
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(modifier = Modifier.size(40.dp))

            Text(
                "FitFusion",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Primary,
                letterSpacing = (-0.5).sp,
            )

            ProfileAvatarButton(
                photoUri = photoUri,
                userName = userName,
                onClick = onAvatarClick,
            )
        }
    }
}

@Composable
private fun ProfileAvatarButton(
    photoUri: String?,
    userName: String?,
    onClick: () -> Unit,
) {
    val initials = remember(userName) {
        userName?.trim()?.split(' ')
            ?.filter { it.isNotBlank() }
            ?.take(2)
            ?.map { it.first().uppercaseChar() }
            ?.joinToString("")
            ?.ifBlank { null }
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Primary.copy(alpha = 0.12f))
            .border(1.5.dp, Primary, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        when {
            photoUri != null -> AsyncImage(
                model = photoUri,
                contentDescription = "Mi perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
            )
            initials != null -> Text(
                initials,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Primary,
            )
            else -> Icon(
                Icons.Outlined.Person,
                contentDescription = "Mi perfil",
                tint = Primary,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}


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

            PostAuthorRow(
                initials = post.authorInitials,
                author = post.author,
                meta = "${post.timeAgo} · ${post.workoutType}",
                initialsColor = Primary,
            )

            Spacer(modifier = Modifier.height(14.dp))

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
                        label = "${post.totalWeightKg.toInt()}kg",
                        labelColor = Secondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLow)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (post.exercises.isEmpty()) {
                    Text(
                        "Sin ejercicios detallados",
                        fontSize = 13.sp,
                        color = OnSurfaceVariant,
                    )
                } else {
                    post.exercises.forEach { exercise ->
                        ExerciseRow(exercise)
                    }
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
                if (post.imageUrl != null) {
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = null,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                }

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
