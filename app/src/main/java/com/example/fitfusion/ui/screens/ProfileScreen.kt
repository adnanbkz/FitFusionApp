package com.example.fitfusion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.fitfusion.data.models.LoggedWorkout
import com.example.fitfusion.data.models.UserPost
import com.example.fitfusion.data.models.UserPostType
import com.example.fitfusion.ui.components.*
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaProfile(
    navController: NavHostController,
    userName: String?,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val state by profileViewModel.uiState.collectAsState()
    val tabs = listOf("MIS PUBLICACIONES", "ENTRENAMIENTOS", "ME GUSTA")

    LaunchedEffect(userName) { profileViewModel.updateFromUser(userName) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Surface),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(
                bottom = if (state.selectedTab == 0) 88.dp else 24.dp
            )
        ) {
            // ── Cabecera ──────────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(SurfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, Modifier.size(20.dp), tint = OnSurfaceVariant)
                        }
                        Text("Kinetic", fontSize = 22.sp, fontWeight = FontWeight.Black, color = OnSurface)
                    }
                    Row {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Search, "Buscar", tint = OnSurface)
                        }
                        IconButton(onClick = { navController.navigate(Screens.SettingsScreen.name) }) {
                            Icon(Icons.Default.Settings, "Ajustes", tint = OnSurface)
                        }
                    }
                }
            }

            // ── Tarjeta de perfil ─────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().background(SurfaceContainerLow).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.size(110.dp).drawBehind {
                            drawCircle(
                                color  = Primary,
                                radius = size.minDimension / 2,
                                style  = Stroke(width = 6f)
                            )
                        })
                        Box(
                            modifier = Modifier.size(100.dp).clip(CircleShape).background(SurfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, Modifier.size(50.dp), tint = OnSurfaceVariant)
                        }
                    }
                    Text(
                        state.displayName,
                        fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OnSurface,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Text(state.handle, fontSize = 14.sp, color = OnSurfaceVariant)
                    Text(
                        state.bio,
                        fontSize = 14.sp, color = OnSurfaceVariant, textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                    Button(
                        onClick = { navController.navigate(Screens.AccountScreen.name) },
                        shape  = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = ButtonDefaults.ContentPadding,
                        modifier = Modifier.height(55.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(GreenGradientBrush, RoundedCornerShape(20.dp))
                                .padding(horizontal = 24.dp, vertical = 8.dp),
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

            // ── Stats ─────────────────────────────────────────────────────────
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

            // ── Card actividad semanal ────────────────────────────────────────
            item {
                Card(
                    shape     = RoundedCornerShape(20.dp),
                    colors    = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier  = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Seguimiento de impulso",
                                    fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface
                                )
                                Text(
                                    "TENDENCIA ACTIVIDAD SEMANAL",
                                    fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp, color = OnSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    state.weeklyChange,
                                    fontSize = 20.sp, fontWeight = FontWeight.Bold,
                                    color = if (state.weeklyChange.startsWith("+")) Primary else Tertiary
                                )
                                Text("VS SEMANA ANTERIOR", fontSize = 10.sp, color = OnSurfaceVariant)
                            }
                        }
                        WeeklyBarChart()
                    }
                }
            }

            // ── Tab bar ───────────────────────────────────────────────────────
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
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = state.selectedTab == index,
                            onClick  = { profileViewModel.onTabSelected(index) },
                            text     = {
                                Text(
                                    title,
                                    fontWeight    = if (state.selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize      = 12.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        )
                    }
                }
            }

            // ── Contenido de tab ──────────────────────────────────────────────
            item {
                when (state.selectedTab) {
                    0 -> PostsTab(posts = state.userPosts)
                    1 -> WorkoutsTab(
                        currentWeekMinutes  = state.currentWeekMinutes,
                        previousWeekMinutes = state.previousWeekMinutes,
                        totalSessions       = state.totalSessionsThisWeek,
                        totalMinutes        = state.totalMinutesThisWeek,
                        totalKcal           = state.totalKcalThisWeek,
                        recentWorkouts      = state.recentWorkouts,
                    )
                    2 -> LikesTab()
                }
            }
        }

        // ── FAB (solo en tab Publicaciones) ───────────────────────────────────
        if (state.selectedTab == 0) {
            FloatingActionButton(
                onClick          = profileViewModel::showCreatePost,
                modifier         = Modifier.align(Alignment.BottomEnd).padding(24.dp),
                containerColor   = Primary,
                contentColor     = Color.White,
                shape            = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva publicación")
            }
        }
    }

    // ── Sheet crear post ──────────────────────────────────────────────────────
    if (state.showCreatePostSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = profileViewModel::dismissCreatePost,
            sheetState       = sheetState,
            containerColor   = SurfaceContainerLowest,
            dragHandle       = { BottomSheetDefaults.DragHandle(color = OutlineVariant) }
        ) {
            CreatePostSheet(
                state      = state,
                viewModel  = profileViewModel,
            )
        }
    }
}

// ── Tab: Publicaciones ────────────────────────────────────────────────────────

@Composable
private fun PostsTab(posts: List<UserPost>) {
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
                    Text("📸", fontSize = 36.sp)
                    Text("Sin publicaciones aún", fontWeight = FontWeight.SemiBold, color = OnSurface)
                    Text("Pulsa + para compartir tu primer entreno", fontSize = 13.sp, color = OnSurfaceVariant)
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
            Text(post.workoutEmoji ?: if (post.type == UserPostType.WORKOUT) "🏋️" else "🥗", fontSize = 28.sp)
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

// ── Tab: Entrenamientos ───────────────────────────────────────────────────────

@Composable
private fun WorkoutsTab(
    currentWeekMinutes: List<Int>,
    previousWeekMinutes: List<Int>,
    totalSessions: Int,
    totalMinutes: Int,
    totalKcal: Int,
    recentWorkouts: List<LoggedWorkout>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Gráfica comparativa
        Card(
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            elevation = CardDefaults.cardElevation(0.dp),
            modifier  = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "MINUTOS DE ENTRENAMIENTO",
                    fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp, color = Primary
                )
                WorkoutComparisonChart(
                    currentWeekMinutes  = currentWeekMinutes,
                    previousWeekMinutes = previousWeekMinutes,
                )
            }
        }

        // Stats resumen de la semana
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

        // Lista de entrenamientos recientes
        if (recentWorkouts.isNotEmpty()) {
            Text(
                "ÚLTIMOS ENTRENAMIENTOS",
                fontSize = 11.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp, color = Primary
            )
            recentWorkouts.forEach { workout ->
                WorkoutProfileCard(workout = workout)
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🏃", fontSize = 36.sp)
                    Text("Sin entrenamientos esta semana", fontSize = 14.sp, color = OnSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun WorkoutComparisonChart(
    currentWeekMinutes: List<Int>,
    previousWeekMinutes: List<Int>,
) {
    val allValues  = currentWeekMinutes + previousWeekMinutes
    val maxVal     = allValues.maxOrNull()?.coerceAtLeast(1) ?: 1
    val maxBarH    = 72.dp
    val dayLabels  = listOf("L", "M", "X", "J", "V", "S", "D")

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            dayLabels.forEachIndexed { idx, label ->
                val cur  = currentWeekMinutes.getOrElse(idx) { 0 }
                val prev = previousWeekMinutes.getOrElse(idx) { 0 }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Etiqueta minutos semana actual
                    Box(modifier = Modifier.height(18.dp), contentAlignment = Alignment.BottomCenter) {
                        if (cur > 0) {
                            Text(
                                "${cur}m",
                                fontSize   = 8.sp,
                                color      = Primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    // Barras
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment     = Alignment.Bottom,
                        modifier              = Modifier.height(maxBarH)
                    ) {
                        // Semana anterior — gris
                        Box(
                            modifier = Modifier
                                .width(10.dp)
                                .height(maxBarH * (prev.toFloat() / maxVal).coerceAtLeast(0.04f))
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                .background(SurfaceContainerHigh)
                        )
                        // Semana actual — verde
                        Box(
                            modifier = Modifier
                                .width(10.dp)
                                .height(maxBarH * (cur.toFloat() / maxVal).coerceAtLeast(0.04f))
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                .background(Primary)
                        )
                    }
                    // Etiqueta día
                    Text(
                        label,
                        fontSize   = 11.sp,
                        color      = OnSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        modifier   = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        // Leyenda
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
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
                    "${workout.durationMinutes} min · ${workout.kcalBurned} kcal · ${workout.exercises.size} ejercicios",
                    fontSize = 12.sp, color = OnSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    workout.date.dayOfMonth.toString(),
                    fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface
                )
                Text(
                    workout.date.month.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale("es")).uppercase(),
                    fontSize = 10.sp, color = OnSurfaceVariant, letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// ── Tab: Me gusta ─────────────────────────────────────────────────────────────

@Composable
private fun LikesTab() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("❤️", fontSize = 36.sp)
            Text("Sin likes aún", fontWeight = FontWeight.SemiBold, color = OnSurface)
            Text("Las publicaciones que te gusten aparecerán aquí", fontSize = 13.sp, color = OnSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

// ── Sheet: Crear publicación ──────────────────────────────────────────────────

@Composable
private fun CreatePostSheet(
    state: com.example.fitfusion.viewmodel.ProfileUiState,
    viewModel: ProfileViewModel,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "NUEVA PUBLICACIÓN",
            fontSize = 11.sp, fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp, color = Primary
        )

        // Toggle tipo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainerLow)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(UserPostType.WORKOUT to "🏋️ Entreno", UserPostType.NUTRITION to "🥗 Nutrición")
                .forEach { (type, label) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (state.createPostType == type) SurfaceContainerLowest else Color.Transparent)
                            .clickable { viewModel.setCreatePostType(type) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            fontSize   = 14.sp,
                            fontWeight = if (state.createPostType == type) FontWeight.Bold else FontWeight.Normal,
                            color      = if (state.createPostType == type) OnSurface else OnSurfaceVariant
                        )
                    }
                }
        }

        HorizontalDivider(color = OutlineVariant.copy(alpha = 0.3f))

        when (state.createPostType) {
            UserPostType.WORKOUT -> WorkoutPostForm(state = state, viewModel = viewModel)
            UserPostType.NUTRITION -> NutritionPostForm(state = state, viewModel = viewModel)
        }

        // Botón publicar
        Button(
            onClick  = { viewModel.publishPost() },
            enabled  = state.canPublish,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("Publicar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun WorkoutPostForm(
    state: com.example.fitfusion.viewmodel.ProfileUiState,
    viewModel: ProfileViewModel,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "SELECCIONAR ENTRENO",
            fontSize = 11.sp, fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp, color = Primary
        )

        if (state.recentWorkouts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay entrenamientos recientes", fontSize = 13.sp, color = OnSurfaceVariant)
            }
        } else {
            state.recentWorkouts.forEach { workout ->
                val isSelected = state.selectedWorkout?.id == workout.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Primary.copy(alpha = 0.08f) else SurfaceContainerLow)
                        .clickable { viewModel.selectWorkout(workout) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(workout.emoji, fontSize = 22.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            workout.name,
                            fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Primary else OnSurface,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${workout.durationMinutes} min · ${workout.kcalBurned} kcal",
                            fontSize = 12.sp, color = OnSurfaceVariant
                        )
                    }
                    if (isSelected) {
                        Text("✓", fontSize = 18.sp, color = Primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Descripción opcional
        OutlinedTextField(
            value         = state.postCaption,
            onValueChange = viewModel::onPostCaptionChange,
            placeholder   = { Text("Añade una descripción (opcional)", color = OnSurfaceVariant, fontSize = 14.sp) },
            minLines      = 2,
            maxLines      = 4,
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = SurfaceContainerLow,
                focusedContainerColor   = SurfaceContainerLow,
                unfocusedBorderColor    = Color.Transparent,
                focusedBorderColor      = Primary,
            )
        )
    }
}

@Composable
private fun NutritionPostForm(
    state: com.example.fitfusion.viewmodel.ProfileUiState,
    viewModel: ProfileViewModel,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value         = state.nutritionTitle,
            onValueChange = viewModel::onNutritionTitleChange,
            placeholder   = { Text("Título de la publicación *", color = OnSurfaceVariant, fontSize = 14.sp) },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = SurfaceContainerLow,
                focusedContainerColor   = SurfaceContainerLow,
                unfocusedBorderColor    = Color.Transparent,
                focusedBorderColor      = Primary,
            )
        )
        OutlinedTextField(
            value         = state.nutritionDesc,
            onValueChange = viewModel::onNutritionDescChange,
            placeholder   = { Text("Descripción (opcional)", color = OnSurfaceVariant, fontSize = 14.sp) },
            minLines      = 2,
            maxLines      = 4,
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = SurfaceContainerLow,
                focusedContainerColor   = SurfaceContainerLow,
                unfocusedBorderColor    = Color.Transparent,
                focusedBorderColor      = Primary,
            )
        )
        OutlinedTextField(
            value         = state.nutritionKcal,
            onValueChange = viewModel::onNutritionKcalChange,
            placeholder   = { Text("Kcal totales (opcional)", color = OnSurfaceVariant, fontSize = 14.sp) },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            ),
            colors        = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = SurfaceContainerLow,
                focusedContainerColor   = SurfaceContainerLow,
                unfocusedBorderColor    = Color.Transparent,
                focusedBorderColor      = Primary,
            )
        )
    }
}
