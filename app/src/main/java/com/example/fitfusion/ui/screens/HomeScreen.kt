package com.example.fitfusion.ui.screens

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.fitfusion.R
import com.example.fitfusion.data.repository.UserProfileStore
import com.example.fitfusion.ui.components.CreatePostSheetHost
import com.example.fitfusion.ui.theme.OnSurface
import com.example.fitfusion.ui.theme.OnSurfaceVariant
import com.example.fitfusion.ui.theme.Primary
import com.example.fitfusion.ui.theme.PrimaryContainer
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
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    Scaffold(
        containerColor = Surface,
        bottomBar = {
            NavigationBar(containerColor = SurfaceContainerLowest) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(if (selectedTab == 0) Icons.Default.Home else Icons.Outlined.Home, "Inicio") },
                    label = { Text("Inicio") },
                    colors = navBarItemColors(),
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(if (selectedTab == 1) Icons.Default.Restaurant else Icons.Outlined.Restaurant, "Dieta") },
                    label = { Text("Dieta") },
                    colors = navBarItemColors(),
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Outlined.FitnessCenter, "Ejercicio") },
                    label = { Text("Ejercicio") },
                    colors = navBarItemColors(),
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Outlined.Person, "Perfil") },
                    label = { Text("Perfil") },
                    colors = navBarItemColors(),
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedTab) {
                0 -> HomeFeedPage(
                    state = state,
                    userName = userName,
                    photoUri = photoUri?.toString(),
                    onAvatarClick = { selectedTab = 3 },
                    onFabClick = profileViewModel::showCreatePost,
                    onPostClick = { postId ->
                        navController.navigate("${Screens.PostDetailScreen.name}/$postId")
                    },
                    onLikeClick = homeViewModel::toggleLike,
                    onFilterSelect = homeViewModel::setFilter,
                    onSaveClick = homeViewModel::toggleSave,
                )
                1 -> PantallaTracking(navController = navController)
                2 -> PantallaAddWorkout(navController = navController, isLogMode = true)
                3 -> PantallaProfile(
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
private fun navBarItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Primary,
    selectedTextColor = Primary,
    indicatorColor = Primary.copy(alpha = 0.12f),
    unselectedIconColor = OnSurfaceVariant,
    unselectedTextColor = OnSurfaceVariant,
)

@OptIn(ExperimentalFoundationApi::class)
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
    onSaveClick: (String) -> Unit,
) {
    val filtered = state.filteredItems
    var viewingStory by remember { mutableStateOf<String?>(null) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceContainerLowest)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            stickyHeader {
                FeedTopBar(
                    photoUri = photoUri,
                    userName = userName,
                    onAvatarClick = onAvatarClick,
                )
            }
            item {
                StoriesRow(userName = userName, photoUri = photoUri, onStoryClick = { viewingStory = it })
                HorizontalDivider(color = SurfaceContainerHigh, thickness = 0.5.dp)
            }
            item {
                FeedFilterRow(
                    current  = state.filter,
                    onSelect = onFilterSelect,
                    modifier = Modifier
                        .background(Surface)
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                )
                HorizontalDivider(color = SurfaceContainerHigh)
            }
            if (filtered.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 80.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        FeedEmptyState(filter = state.filter)
                    }
                }
            } else {
                items(
                    items = filtered,
                    key = { item ->
                        when (item) {
                            is FeedItem.Workout   -> item.post.id
                            is FeedItem.Nutrition -> item.post.id
                        }
                    },
                ) { item ->
                    when (item) {
                        is FeedItem.Workout -> WorkoutPostCard(
                            post        = item.post,
                            onLikeClick = { onLikeClick(item.post.id) },
                            onCardClick = { onPostClick(item.post.id) },
                            onSaveClick = { onSaveClick(item.post.id) },
                        )
                        is FeedItem.Nutrition -> NutritionPostCard(
                            post        = item.post,
                            onLikeClick = { onLikeClick(item.post.id) },
                            onCardClick = { onPostClick(item.post.id) },
                            onSaveClick = { onSaveClick(item.post.id) },
                        )
                    }
                    HorizontalDivider(color = SurfaceContainerHigh, thickness = 0.5.dp)
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
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
                        Brush.linearGradient(listOf(Primary, PrimaryContainer)),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, "Nueva publicación", modifier = Modifier.size(26.dp))
            }
        }
    }

    viewingStory?.let { storyOwner ->
        StoriesViewer(
            name = storyOwner,
            onDismiss = { viewingStory = null },
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
        FeedFilter.ALL to "Todo",
        FeedFilter.WORKOUTS to "Entrenos",
        FeedFilter.NUTRITION to "Recetas",
    )
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        items(items) { (filter, label) ->
            val selected = current == filter
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (selected) Primary else SurfaceContainerLow)
                    .border(
                        width = 1.dp,
                        color = if (selected) Primary else SurfaceContainerHigh,
                        shape = CircleShape,
                    )
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 18.dp, vertical = 8.dp),
            ) {
                Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) Color.White else OnSurfaceVariant,
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
            Text(message, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnSurface, textAlign = TextAlign.Center)
            Text(
                "Publica un entreno o una receta para empezar el feed",
                fontSize = 13.sp, color = OnSurfaceVariant, textAlign = TextAlign.Center
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
            .background(Surface.copy(alpha = 0.97f))
            .padding(horizontal = 8.dp)
    ) {
        // Notification bell on the left
        Box(
            modifier = Modifier.height(56.dp).align(Alignment.CenterStart),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(onClick = { }) {
                BadgedBox(badge = { Badge(containerColor = Tertiary) }) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notificaciones",
                        tint = OnSurface,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }

        // Brand logo centered
        AsyncImage(
            model = R.drawable.logowithoutbg,
            contentDescription = "FitFusion",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(56.dp)
                .width(110.dp)
                .align(Alignment.Center)
                .padding(vertical = 12.dp),
        )

        // Profile avatar on the far right
        Box(
            modifier = Modifier
                .height(56.dp)
                .align(Alignment.CenterEnd),
            contentAlignment = Alignment.Center,
        ) {
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
    onSaveClick: () -> Unit,
) {
    val context = LocalContext.current
    var showHeart by remember { mutableStateOf(false) }
    LaunchedEffect(showHeart) { if (showHeart) { delay(700); showHeart = false } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceContainerLowest)
            .clickable(onClick = onCardClick)
    ) {
        // Header: avatar + name + type | ⋮
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AuthorAvatar(initials = post.authorInitials, size = 36, color = Primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(post.author, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = OnSurface)
                Text(post.workoutType, fontSize = 12.sp, color = OnSurfaceVariant)
            }
            Icon(Icons.Default.MoreVert, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
        }

        // Media: dark green gradient with workout info overlaid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .background(
                    Brush.linearGradient(
                        colors = listOf(SurfaceContainerLowest, SurfaceContainerLowest, Primary.copy(alpha = 0.75f))
                    )
                )
                .pointerInput(post.isLiked) {
                    detectTapGestures(
                        onTap = { onCardClick() },
                        onDoubleTap = { if (!post.isLiked) onLikeClick(); showHeart = true },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                Text(
                    post.workoutName,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PostStatChip(
                        icon = { Icon(Icons.Outlined.Schedule, null, Modifier.size(14.dp), tint = Color.White.copy(0.8f)) },
                        label = "${post.durationMin} min",
                    )
                    if (post.totalWeightKg > 0) {
                        PostStatChip(
                            icon = { Icon(Icons.Outlined.FitnessCenter, null, Modifier.size(14.dp), tint = Color.White.copy(0.8f)) },
                            label = "${post.totalWeightKg.toInt()} kg",
                        )
                    }
                }
                if (post.exercises.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        post.exercises.take(3).forEach { ex ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF7ED321)),
                                )
                                Text(
                                    ex.name,
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    if (ex.reps > 0) "${ex.sets}×${ex.reps}" else "${ex.sets} series",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.6f),
                                )
                            }
                        }
                        if (post.exercises.size > 3) {
                            Text(
                                "+${post.exercises.size - 3} ejercicios más",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.55f),
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                }
            }
            HeartBurst(visible = showHeart)
        }

        // Action bar (no divider above)
        InteractionBar(
            likes = post.likes,
            comments = post.comments,
            isLiked = post.isLiked,
            isSaved = post.isSaved,
            onLikeClick = onLikeClick,
            onSaveClick = onSaveClick,
            onShareClick = {
                context.startActivity(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "${post.author} compartió un entrenamiento: ${post.workoutName}")
                    }.let { Intent.createChooser(it, "Compartir") }
                )
            },
        )

        // Likes count
        if (post.likes > 0) {
            Text(
                "${post.likes} me gusta",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = OnSurface,
                modifier = Modifier.padding(horizontal = 14.dp).padding(bottom = 3.dp),
            )
        }

        // Caption
        PostCaption(author = post.author, text = post.workoutName)

        // Comments link
        if (post.comments > 0) {
            Text(
                "Ver los ${post.comments} comentarios",
                fontSize = 13.sp,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
            )
        }

        // Timestamp
        Text(
            post.timeAgo.uppercase(),
            fontSize = 10.sp,
            color = OnSurfaceVariant,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
        )
    }
}


@Composable
private fun NutritionPostCard(
    post: NutritionPost,
    onLikeClick: () -> Unit,
    onCardClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    val context = LocalContext.current
    var showHeart by remember { mutableStateOf(false) }
    LaunchedEffect(showHeart) { if (showHeart) { delay(700); showHeart = false } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceContainerLowest)
            .clickable(onClick = onCardClick)
    ) {
        // Header: avatar + name + meta | ⋮
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AuthorAvatar(initials = post.authorInitials, size = 36, color = Secondary)
            Column(modifier = Modifier.weight(1f)) {
                Text(post.author, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = OnSurface)
                Text("Receta · ${post.timeAgo}", fontSize = 12.sp, color = OnSurfaceVariant)
            }
            Icon(Icons.Default.MoreVert, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
        }

        // Media: image or gradient banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .pointerInput(post.isLiked) {
                    detectTapGestures(
                        onTap = { onCardClick() },
                        onDoubleTap = { if (!post.isLiked) onLikeClick(); showHeart = true },
                    )
                },
            contentAlignment = Alignment.BottomStart,
        ) {
            if (post.imageUrl != null) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SurfaceContainerLowest, Primary.copy(alpha = 0.4f), PrimaryContainer.copy(alpha = 0.6f))
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            Icons.Default.Restaurant,
                            null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(48.dp),
                        )
                        Text(
                            post.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                    }
                }
            }
            // NUTRICIÓN badge
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    "NUTRICIÓN",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.5.sp,
                )
            }
            HeartBurst(visible = showHeart)
        }

        // Action bar
        InteractionBar(
            likes = post.likes,
            comments = post.comments,
            isLiked = post.isLiked,
            isSaved = post.isSaved,
            onLikeClick = onLikeClick,
            onSaveClick = onSaveClick,
            onShareClick = {
                context.startActivity(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "${post.author} compartió una receta: ${post.title}")
                    }.let { Intent.createChooser(it, "Compartir") }
                )
            },
        )

        // Likes count
        if (post.likes > 0) {
            Text(
                "${post.likes} me gusta",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = OnSurface,
                modifier = Modifier.padding(horizontal = 14.dp).padding(bottom = 3.dp),
            )
        }

        // Caption + description
        PostCaption(author = post.author, text = post.title)
        if (post.description.isNotBlank()) {
            Text(
                post.description,
                fontSize = 13.sp,
                color = OnSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
            )
        }

        // Macro chips
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            MacroChip("${post.kcal} kcal", Primary)
            MacroChip("${post.proteinG}g prot", Secondary)
            MacroChip("${post.carbsG}g carbs", Tertiary)
        }

        // Comments link
        if (post.comments > 0) {
            Text(
                "Ver los ${post.comments} comentarios",
                fontSize = 13.sp,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
            )
        }

        // Timestamp
        Text(
            post.timeAgo.uppercase(),
            fontSize = 10.sp,
            color = OnSurfaceVariant,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
        )
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
    isSaved: Boolean,
    onLikeClick: () -> Unit,
    onSaveClick: () -> Unit,
    onShareClick: () -> Unit = {},
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
    val saveColor by animateColorAsState(
        targetValue = if (isSaved) Primary else OnSurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "saveColor",
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left group: like + comment
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
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comentarios",
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Text("$comments", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            }
        }

        // Right group: share + bookmark
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onShareClick,
                modifier = Modifier.size(28.dp),
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Compartir",
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
            IconButton(
                onClick = onSaveClick,
                modifier = Modifier.size(28.dp),
            ) {
                Icon(
                    if (isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (isSaved) "Guardado" else "Guardar",
                    tint = saveColor,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun PostStatChip(icon: @Composable () -> Unit, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        icon()
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun PostCaption(author: String, text: String) {
    Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(author, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = OnSurface)
        Text(
            text,
            fontSize = 13.sp,
            color = OnSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
    }
}

@Composable
private fun MacroChip(text: String, color: Color) {
    Text(
        text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.10f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
private fun StoriesRow(userName: String?, photoUri: String?, onStoryClick: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth().background(Surface),
    ) {
        item {
            StoryBubble(
                name = userName ?: "Tú",
                photoUri = photoUri,
                isOwn = true,
                onClick = { onStoryClick(userName ?: "Tú") },
            )
        }
    }
}

@Composable
private fun StoryBubble(name: String, photoUri: String?, isOwn: Boolean, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Box(modifier = Modifier.size(66.dp)) {
            // Gradient ring
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(Primary, PrimaryContainer, Secondary))
                    ),
            )
            // Avatar
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(Surface),
                contentAlignment = Alignment.Center,
            ) {
                if (photoUri != null) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                    )
                } else {
                    Text(
                        name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                    )
                }
            }
            // "+" badge for own story
            if (isOwn) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(Primary)
                        .border(2.dp, Surface, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(12.dp), tint = Color.White)
                }
            }
        }
        Text(
            if (isOwn) "Tu historia" else name,
            fontSize = 11.sp,
            color = OnSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(66.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun HeartBurst(visible: Boolean) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(if (visible) 150 else 400),
        label = "heartBurst",
    )
    if (alpha > 0f) {
        Icon(
            Icons.Default.Favorite,
            contentDescription = null,
            tint = Color.White.copy(alpha = alpha),
            modifier = Modifier.size(80.dp),
        )
    }
}

@Composable
private fun StoriesViewer(name: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(SurfaceContainerLowest, Primary.copy(alpha = 0.3f)))
                )
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Primary, PrimaryContainer, Secondary))
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerLow),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                        )
                    }
                }
                Text(name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                Text(
                    "No hay historias recientes",
                    fontSize = 14.sp,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
            ) {
                Icon(Icons.Default.Close, "Cerrar", tint = OnSurface, modifier = Modifier.size(24.dp))
            }
        }
    }
}
