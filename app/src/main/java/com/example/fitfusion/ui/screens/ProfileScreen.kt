package com.example.fitfusion.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import androidx.compose.ui.geometry.Offset
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import com.example.fitfusion.data.models.LoggedWorkout
import com.example.fitfusion.data.models.UserPost
import com.example.fitfusion.data.models.UserPostType
import com.example.fitfusion.data.models.WorkoutExercise
import com.example.fitfusion.data.models.WorkoutSet
import com.example.fitfusion.ui.components.*
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.ProfileViewModel

private data class EditableWorkoutState(
    val source: LoggedWorkout,
    val name: String = source.name,
    val durationMinutes: Int = source.durationMinutes.coerceAtLeast(1),
    val exercises: List<WorkoutExercise> = source.exercises,
) {
    fun toLoggedWorkout(): LoggedWorkout {
        val startedAt = source.startedAtMs
        val endedAt = startedAt?.let { it + durationMinutes * 60_000L } ?: source.endedAtMs
        return source.copy(
            name = name.ifBlank { "Entrenamiento" },
            durationMinutes = durationMinutes,
            kcalBurned = (durationMinutes * 6.5f).toInt(),
            endedAtMs = endedAt,
            exercises = exercises,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaProfile(
    navController: NavHostController,
    userName: String?,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val state by profileViewModel.uiState.collectAsState()
    var workoutEditState by remember { mutableStateOf<EditableWorkoutState?>(null) }
    var workoutEditError by remember { mutableStateOf<String?>(null) }
    var isSavingWorkoutEdit by remember { mutableStateOf(false) }
    val tabs = listOf(
        Triple("Posts", Icons.Default.GridView, 0),
        Triple("Entrenos", Icons.Default.FitnessCenter, 1),
        Triple("Likes", Icons.Default.Favorite, 2),
    )

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { profileViewModel.updateProfilePhoto(it) } }

    LaunchedEffect(userName) { profileViewModel.updateFromUser(userName) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Surface),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
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
                                    0 -> "Buscar en tus posts"
                                    1 -> "Buscar entrenamiento"
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

            item {
                Column(
                    modifier = Modifier.fillMaxWidth().background(SurfaceContainerLow).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.clickable {
                            photoLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Box(modifier = Modifier.size(110.dp).drawBehind {
                            drawCircle(
                                color  = Primary,
                                radius = size.minDimension / 2,
                                style  = Stroke(width = 6f)
                            )
                        })
                        if (state.profilePhotoUri != null) {
                            AsyncImage(
                                model = state.profilePhotoUri,
                                contentDescription = "Foto de perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(100.dp).clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(100.dp).clip(CircleShape).background(SurfaceContainerHigh),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, null, Modifier.size(50.dp), tint = OnSurfaceVariant)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 2.dp, y = 2.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Cambiar foto",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        state.displayName,
                        fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OnSurface,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Text(
                        state.bio,
                        fontSize = 14.sp, color = OnSurfaceVariant, textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                    ProfileFitnessSummary(
                        heightCm = state.heightCm,
                        weightKg = state.weightKg,
                        goalType = state.goalType,
                        activityLevel = state.activityLevel,
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(GreenGradientBrush)
                            .clickable { navController.navigate(Screens.AccountScreen.name) }
                            .padding(horizontal = 28.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Edit, null, Modifier.size(16.dp), tint = Color.White)
                            Text("Editar perfil", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatChip(state.postCount, "Posts", Modifier.weight(1f))
                    StatChip(state.followers, "Seguidores", Modifier.weight(1f))
                    StatChip(state.following, "Siguiendo", Modifier.weight(1f))
                }
            }

            item {
                StreakCard(
                    streakDays = state.currentStreak,
                    modifier   = Modifier.padding(horizontal = 24.dp)
                )
            }

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
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    tabs.forEach { (title, icon, index) ->
                        val isSelected = state.selectedTab == index
                        Tab(
                            selected = isSelected,
                            onClick  = { profileViewModel.onTabSelected(index) },
                            icon     = {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (isSelected) Primary else OnSurfaceVariant
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

            item {
                when (state.selectedTab) {
                    0 -> PostsTab(posts = state.filteredPosts, isSearching = state.searchQuery.isNotBlank())
                    1 -> WorkoutsTab(
                        currentWeekMinutes  = state.currentWeekMinutes,
                        previousWeekMinutes = state.previousWeekMinutes,
                        totalSessions       = state.totalSessionsThisWeek,
                        totalMinutes        = state.totalMinutesThisWeek,
                        totalKcal           = state.totalKcalThisWeek,
                        selectedWorkoutDay  = state.selectedWorkoutDay,
                        selectedDayWorkouts = state.filteredDayWorkouts,
                        isSearching         = state.searchQuery.isNotBlank(),
                        onDaySelected       = profileViewModel::selectWorkoutDay,
                        onAddWorkout        = { navController.navigate("${Screens.AddWorkoutScreen.name}?logMode=true") },
                        onRemoveWorkout     = profileViewModel::removeWorkoutFromDay,
                        onEditWorkout       = { workout ->
                            workoutEditState = EditableWorkoutState(workout)
                            workoutEditError = null
                        },
                    )
                    2 -> LikesTab(onExplore = {
                        navController.navigate(Screens.HomeScreen.name) {
                            popUpTo(Screens.HomeScreen.name) { inclusive = true }
                        }
                    })
                }
            }
        }

        workoutEditState?.let { editState ->
            EditWorkoutSheet(
                state = editState,
                isSaving = isSavingWorkoutEdit,
                errorMessage = workoutEditError,
                onStateChange = {
                    workoutEditState = it
                    workoutEditError = null
                },
                onDismiss = {
                    if (!isSavingWorkoutEdit) {
                        workoutEditState = null
                        workoutEditError = null
                    }
                },
                onSave = {
                    isSavingWorkoutEdit = true
                    profileViewModel.updateWorkout(
                        workout = editState.toLoggedWorkout(),
                        onSuccess = {
                            isSavingWorkoutEdit = false
                            workoutEditState = null
                            workoutEditError = null
                        },
                        onError = { message ->
                            isSavingWorkoutEdit = false
                            workoutEditError = message
                        },
                    )
                }
            )
        }

    }
}

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
            val formatted = if (weight % 1f == 0f) weight.toInt().toString() else "%.1f".format(java.util.Locale.US, weight)
            add("$formatted kg" to "PESO")
        }
        goalType?.takeIf { it.isNotBlank() }?.let { add(it to "OBJETIVO") }
        activityLevel?.takeIf { it.isNotBlank() }?.let { add(it to "ACTIVIDAD") }
    }.take(3)
    if (items.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { (value, label) ->
            val compactValue = if (value.length > 14) value.take(12) + "..." else value
            StatChip(value = compactValue, label = label, modifier = Modifier.weight(1f))
        }
    }
}


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
                    tint = accent,
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

@Composable
private fun PostsTab(posts: List<UserPost>, isSearching: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (posts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
            posts.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { post ->
                        PostGridCell(post = post, modifier = Modifier.weight(1f).height(160.dp))
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PostGridCell(post: UserPost, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceContainerHigh),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(12.dp)
        ) {
            Text(post.workoutEmoji ?: "", fontSize = 28.sp)
            Text(
                post.caption,
                fontSize   = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color      = OnSurface,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis,
                textAlign  = TextAlign.Center,
                lineHeight = 16.sp
            )
            if (post.type == UserPostType.WORKOUT && post.workoutDurationMinutes != null) {
                Text(
                    "${post.workoutDurationMinutes} min",
                    fontSize = 11.sp, color = OnSurfaceVariant
                )
            } else if (post.type == UserPostType.NUTRITION && post.nutritionKcal != null) {
                Text(
                    "${post.nutritionKcal} kcal",
                    fontSize = 11.sp, color = OnSurfaceVariant
                )
            }
        }
    }
}


@Composable
private fun WorkoutsTab(
    currentWeekMinutes: List<Int>,
    previousWeekMinutes: List<Int>,
    totalSessions: Int,
    totalMinutes: Int,
    totalKcal: Int,
    selectedWorkoutDay: LocalDate,
    selectedDayWorkouts: List<LoggedWorkout>,
    isSearching: Boolean,
    onDaySelected: (LocalDate) -> Unit,
    onAddWorkout: () -> Unit,
    onRemoveWorkout: (String, LocalDate) -> Unit,
    onEditWorkout: (LoggedWorkout) -> Unit,
) {
    val weekStart = remember { LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
    val selectedDayIdx = (selectedWorkoutDay.dayOfWeek.value - 1).coerceIn(0, 6)

    val curSum  = currentWeekMinutes.sum()
    val prevSum = previousWeekMinutes.sum()
    val weeklyChangePct: Int? = if (prevSum > 0) ((curSum - prevSum).toFloat() / prevSum * 100).toInt() else null
    val totalFormatted = if (curSum >= 60) "${curSum / 60}h ${curSum % 60}m" else "${curSum}m"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            elevation = CardDefaults.cardElevation(0.dp),
            modifier  = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "ACTIVIDAD SEMANAL",
                            fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp, color = Primary
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                totalFormatted,
                                fontSize = 32.sp, fontWeight = FontWeight.Bold, color = OnSurface
                            )
                            Text(
                                "esta semana",
                                fontSize = 13.sp, color = OnSurfaceVariant,
                                modifier = Modifier.padding(bottom = 5.dp)
                            )
                        }
                    }
                    if (weeklyChangePct != null) {
                        val isPositive = weeklyChangePct >= 0
                        val badgeColor = if (isPositive) Primary else Tertiary
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(badgeColor.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    if (isPositive) "+$weeklyChangePct%" else "$weeklyChangePct%",
                                    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = badgeColor
                                )
                            }
                            Text(
                                "vs sem. anterior",
                                fontSize = 10.sp, color = OnSurfaceVariant
                            )
                        }
                    }
                }
                WorkoutComparisonChart(
                    currentWeekMinutes  = currentWeekMinutes,
                    previousWeekMinutes = previousWeekMinutes,
                    selectedDayIdx      = selectedDayIdx,
                    onDaySelected       = { idx -> onDaySelected(weekStart.plusDays(idx.toLong())) }
                )
            }
        }

        Card(
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            elevation = CardDefaults.cardElevation(0.dp),
            modifier  = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeekStatItem("$totalSessions", "SESIONES", Primary)
                WeekStatDivider()
                WeekStatItem(
                    if (totalMinutes >= 60) "${totalMinutes / 60}h ${totalMinutes % 60}m" else "${totalMinutes}m",
                    "TIEMPO", Secondary
                )
                WeekStatDivider()
                WeekStatItem("$totalKcal", "KCAL", Tertiary)
            }
        }

        SelectedDayWorkoutsSection(
            date        = selectedWorkoutDay,
            workouts    = selectedDayWorkouts,
            isSearching = isSearching,
            onAdd       = onAddWorkout,
            onRemove    = { id -> onRemoveWorkout(id, selectedWorkoutDay) },
            onEdit      = onEditWorkout,
        )
    }
}

@Composable
private fun WorkoutComparisonChart(
    currentWeekMinutes: List<Int>,
    previousWeekMinutes: List<Int>,
    selectedDayIdx: Int,
    onDaySelected: (Int) -> Unit,
) {
    val allValues  = currentWeekMinutes + previousWeekMinutes
    val maxVal     = allValues.maxOrNull()?.coerceAtLeast(1) ?: 1
    val maxBarH    = 120.dp
    val barW       = 14.dp
    val dayLabels  = listOf("L", "M", "X", "J", "V", "S", "D")
    val todayIdx   = (LocalDate.now().dayOfWeek.value - 1).coerceIn(0, 6)
    val guideColor = OutlineVariant.copy(alpha = 0.25f)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(maxBarH)) {
            Canvas(modifier = Modifier.fillMaxWidth().height(maxBarH)) {
                listOf(0.25f, 0.5f, 0.75f, 1f).forEach { frac ->
                    val y = size.height * (1f - frac)
                    drawLine(
                        color       = guideColor,
                        start       = Offset(0f, y),
                        end         = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
            Row(
                modifier              = Modifier.fillMaxWidth().height(maxBarH),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.Bottom
            ) {
                dayLabels.indices.forEach { idx ->
                    val cur        = currentWeekMinutes.getOrElse(idx) { 0 }
                    val prev       = previousWeekMinutes.getOrElse(idx) { 0 }
                    val isSelected = idx == selectedDayIdx
                    val isToday    = idx == todayIdx

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment     = Alignment.Bottom,
                        modifier              = Modifier
                            .height(maxBarH)
                            .clickable { onDaySelected(idx) }
                    ) {
                        Box(
                            modifier = Modifier
                                .width(barW)
                                .height(maxBarH * (prev.toFloat() / maxVal).coerceAtLeast(0.03f))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(if (isSelected) SurfaceContainerHigh else SurfaceContainerHigh.copy(alpha = 0.6f))
                        )
                        Box(
                            modifier = Modifier
                                .width(barW)
                                .height(maxBarH * (cur.toFloat() / maxVal).coerceAtLeast(0.03f))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    when {
                                        isSelected -> Primary
                                        isToday    -> Primary.copy(alpha = 0.65f)
                                        else       -> Primary.copy(alpha = 0.35f)
                                    }
                                )
                        )
                    }
                }
            }
        }

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dayLabels.forEachIndexed { idx, label ->
                val cur        = currentWeekMinutes.getOrElse(idx) { 0 }
                val isSelected = idx == selectedDayIdx
                val isToday    = idx == todayIdx
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier            = Modifier.clickable { onDaySelected(idx) }
                ) {
                    Text(
                        label,
                        fontSize   = 11.sp,
                        color      = if (isSelected || isToday) Primary else OnSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else if (isToday) FontWeight.SemiBold else FontWeight.Medium
                    )
                    if (cur > 0) {
                        Text(
                            if (cur >= 60) "${cur / 60}h${if (cur % 60 > 0) "${cur % 60}m" else ""}" else "${cur}m",
                            fontSize   = 9.sp,
                            color      = if (isSelected || isToday) Primary else OnSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Spacer(Modifier.height(12.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Primary else Color.Transparent)
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier              = Modifier.padding(top = 4.dp)
        ) {
            LegendItem(color = Primary, label = "Esta semana")
            LegendItem(color = SurfaceContainerHigh, label = "Semana anterior")
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(label, fontSize = 11.sp, color = OnSurfaceVariant)
    }
}

@Composable
private fun WeekStatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = OnSurfaceVariant)
    }
}

@Composable
private fun WeekStatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(OutlineVariant.copy(alpha = 0.3f))
    )
}

@Composable
private fun WorkoutProfileCard(workout: LoggedWorkout) {
    Card(
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) { Text(workout.emoji, fontSize = 22.sp) }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    workout.name,
                    fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                    color = OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    workoutSummaryLine(workout),
                    fontSize = 12.sp, color = OnSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    workout.date.dayOfMonth.toString(),
                    fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface
                )
                Text(
                    workout.date.month.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es")).uppercase(),
                    fontSize = 10.sp, color = OnSurfaceVariant, letterSpacing = 0.5.sp
                )
            }
        }
    }
}


@Composable
private fun SelectedDayWorkoutsSection(
    date: LocalDate,
    workouts: List<LoggedWorkout>,
    isSearching: Boolean,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit,
    onEdit: (LoggedWorkout) -> Unit,
) {
    val today = remember { LocalDate.now() }
    val dayName = when (date) {
        today            -> "Hoy"
        today.minusDays(1) -> "Ayer"
        else -> date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es"))
            .replaceFirstChar { it.uppercase() }
    }
    val dateLabel = "${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es")).replaceFirstChar { it.uppercase() }}"

    Card(
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "ENTRENAMIENTOS",
                        fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp, color = Primary
                    )
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier              = Modifier.padding(top = 2.dp)
                    ) {
                        Text(dayName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                        Text(
                            dateLabel,
                            fontSize = 13.sp, color = OnSurfaceVariant,
                            modifier = Modifier.padding(bottom = 1.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GreenGradientBrush)
                        .clickable(onClick = onAdd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Añadir entrenamiento",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            if (workouts.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isSearching) {
                        Text(
                            "Sin resultados",
                            fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnSurface
                        )
                        Text(
                            "Prueba con otro término",
                            fontSize = 13.sp, color = OnSurfaceVariant
                        )
                    } else {
                        Text(
                            "Sin entrenamientos",
                            fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnSurface
                        )
                        Text(
                            "Pulsa + para registrar tu sesión",
                            fontSize = 13.sp, color = OnSurfaceVariant
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    workouts.forEach { workout ->
                        WorkoutDayCard(
                            workout = workout,
                            onEdit = { onEdit(workout) },
                            onRemove = { onRemove(workout.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutDayCard(
    workout: LoggedWorkout,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
) {
    var showExercises by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceContainerLow)
                .clickable { showExercises = !showExercises }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(workout.emoji, fontSize = 20.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    workout.name,
                    fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                    color = OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    workoutSummaryLine(workout),
                    fontSize = 12.sp, color = OnSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerHigh)
                    .clickable(onClick = onEdit),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar entrenamiento",
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerHigh)
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Eliminar entrenamiento",
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = showExercises && workout.exercises.isNotEmpty(),
            enter   = expandVertically(),
            exit    = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                workout.exercises.forEach { WorkoutExerciseItem(it) }
            }
        }
    }
}

@Composable
private fun WorkoutExerciseItem(exercise: WorkoutExercise) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 5.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                exercise.name,
                fontSize = 13.sp, color = OnSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(
                exercise.summary,
                fontSize = 12.sp, color = OnSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            exercise.setBreakdown,
            fontSize = 11.sp,
            color = OnSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun workoutSummaryLine(workout: LoggedWorkout): String {
    val volume = workout.totalVolumeKg.toInt()
    val base = "${workout.durationMinutes} min · ${workout.kcalBurned} kcal · ${workout.totalSets} series"
    return if (volume > 0) "$base · $volume kg" else base
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditWorkoutSheet(
    state: EditableWorkoutState,
    isSaving: Boolean,
    errorMessage: String?,
    onStateChange: (EditableWorkoutState) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceContainerLowest,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Editar entrenamiento", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                    Text(
                        state.source.date.dayOfMonth.toString() + " " +
                            state.source.date.month.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es")),
                        fontSize = 12.sp,
                        color = OnSurfaceVariant
                    )
                }
                Text(
                    "${state.exercises.sumOf { it.sets.size }} series",
                    fontSize = 12.sp,
                    color = OnSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedTextField(
                value = state.name,
                onValueChange = { onStateChange(state.copy(name = it)) },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = SurfaceContainerLow,
                    focusedContainerColor = SurfaceContainerLow,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Primary,
                )
            )

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Duración", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnSurface)
                        Text("Actualiza tiempo y kcal estimadas", fontSize = 12.sp, color = OnSurfaceVariant)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProfileStepperButton(
                            label = "−",
                            onClick = {
                                onStateChange(
                                    state.copy(durationMinutes = (state.durationMinutes - 5).coerceAtLeast(1))
                                )
                            }
                        )
                        Text(
                            "${state.durationMinutes} min",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        ProfileStepperButton(
                            label = "+",
                            onClick = {
                                onStateChange(
                                    state.copy(durationMinutes = (state.durationMinutes + 5).coerceAtMost(240))
                                )
                            }
                        )
                    }
                }
            }

            Text(
                "EJERCICIOS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = Primary
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.exercises.forEachIndexed { exerciseIndex, exercise ->
                    EditableWorkoutExerciseCard(
                        exercise = exercise,
                        onAddSet = {
                            onStateChange(
                                state.copy(
                                    exercises = state.exercises.updateAt(exerciseIndex) { current ->
                                        current.copy(
                                            sets = current.sets + (current.sets.lastOrNull()
                                                ?: WorkoutSet(reps = 10, weightKg = 0f))
                                        )
                                    }
                                )
                            )
                        },
                        onRemoveSet = { setIndex ->
                            onStateChange(
                                state.copy(
                                    exercises = state.exercises.updateAt(exerciseIndex) { current ->
                                        if (current.sets.size <= 1 || setIndex !in current.sets.indices) current
                                        else current.copy(
                                            sets = current.sets.filterIndexed { index, _ -> index != setIndex }
                                        )
                                    }
                                )
                            )
                        },
                        onSetRepsChange = { setIndex, reps ->
                            onStateChange(
                                state.copy(
                                    exercises = state.exercises.updateAt(exerciseIndex) { current ->
                                        current.copy(
                                            sets = current.sets.updateAt(setIndex) { set ->
                                                set.copy(reps = reps.coerceIn(1, 50))
                                            }
                                        )
                                    }
                                )
                            )
                        },
                        onSetWeightChange = { setIndex, weight ->
                            onStateChange(
                                state.copy(
                                    exercises = state.exercises.updateAt(exerciseIndex) { current ->
                                        current.copy(
                                            sets = current.sets.updateAt(setIndex) { set ->
                                                set.copy(weightKg = weight.coerceIn(0, 300).toFloat())
                                            }
                                        )
                                    }
                                )
                            )
                        }
                    )
                }
            }

            errorMessage?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Button(
                onClick = onSave,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar cambios", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun EditableWorkoutExerciseCard(
    exercise: WorkoutExercise,
    onAddSet: () -> Unit,
    onRemoveSet: (Int) -> Unit,
    onSetRepsChange: (Int, Int) -> Unit,
    onSetWeightChange: (Int, Int) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        exercise.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = OnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(exercise.muscleGroup, fontSize = 12.sp, color = OnSurfaceVariant)
                }
                AssistChip(
                    onClick = onAddSet,
                    label = { Text("Añadir serie") },
                    colors = AssistChipDefaults.assistChipColors(containerColor = SurfaceContainerHigh)
                )
            }
            HorizontalDivider(color = OutlineVariant.copy(alpha = 0.4f))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                exercise.sets.forEachIndexed { setIndex, set ->
                    EditableWorkoutSetRow(
                        setIndex = setIndex,
                        set = set,
                        canRemove = exercise.sets.size > 1,
                        onRemove = { onRemoveSet(setIndex) },
                        onRepsChange = { reps -> onSetRepsChange(setIndex, reps) },
                        onWeightChange = { weight -> onSetWeightChange(setIndex, weight) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EditableWorkoutSetRow(
    setIndex: Int,
    set: WorkoutSet,
    canRemove: Boolean,
    onRemove: () -> Unit,
    onRepsChange: (Int) -> Unit,
    onWeightChange: (Int) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Serie ${setIndex + 1}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface
                )
                if (canRemove) {
                    TextButton(onClick = onRemove) {
                        Text("Eliminar", color = Primary)
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                EditableWorkoutStepper(
                    label = "Reps",
                    value = set.reps,
                    onDecrement = { onRepsChange((set.reps - 1).coerceAtLeast(1)) },
                    onIncrement = { onRepsChange((set.reps + 1).coerceAtMost(50)) },
                )
                EditableWorkoutStepper(
                    label = "Peso (kg)",
                    value = set.weightKg.toInt(),
                    onDecrement = { onWeightChange((set.weightKg.toInt() - 5).coerceAtLeast(0)) },
                    onIncrement = { onWeightChange((set.weightKg.toInt() + 5).coerceAtMost(300)) },
                )
            }
        }
    }
}

@Composable
private fun EditableWorkoutStepper(
    label: String,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(label, fontSize = 11.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Medium)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProfileStepperButton(label = "−", onClick = onDecrement)
            Text(
                "$value",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface,
                modifier = Modifier.widthIn(min = 28.dp)
            )
            ProfileStepperButton(label = "+", onClick = onIncrement)
        }
    }
}

@Composable
private fun ProfileStepperButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(SurfaceContainerHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 18.sp, color = OnSurface, fontWeight = FontWeight.Bold)
    }
}

private fun <T> List<T>.updateAt(index: Int, transform: (T) -> T): List<T> {
    if (index !in indices) return this
    return mapIndexed { currentIndex, item ->
        if (currentIndex == index) transform(item) else item
    }
}


@Composable
private fun LikesTab(onExplore: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Sin likes aún", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = OnSurface)
            Text(
                "Las publicaciones que te gusten aparecerán aquí",
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
