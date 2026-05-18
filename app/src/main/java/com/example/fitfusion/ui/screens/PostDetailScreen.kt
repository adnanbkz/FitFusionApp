package com.example.fitfusion.ui.screens

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.fitfusion.data.repository.FeedComment
import com.example.fitfusion.data.repository.PostInteractionRepository
import com.example.fitfusion.ui.components.CommentsBottomSheet
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.ExerciseItem
import com.example.fitfusion.viewmodel.PostDetailViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PantallaPostDetail(
    navController: NavHostController,
    postId: String? = null,
    postDetailViewModel: PostDetailViewModel = viewModel(),
) {
    val state by postDetailViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showAllComments by remember { mutableStateOf(false) }
    var commentInput by remember { mutableStateOf("") }
    var isSendingComment by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(postId) { postDetailViewModel.loadPost(postId) }

    if (showAllComments && !postId.isNullOrBlank()) {
        CommentsBottomSheet(postId = postId, onDismiss = { showAllComments = false })
    }

    Scaffold(
        containerColor = Surface,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            if (!postId.isNullOrBlank()) {
                Column(modifier = Modifier.imePadding()) {
                    HorizontalDivider(color = SurfaceContainerHigh)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Surface)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = commentInput,
                            onValueChange = { commentInput = it },
                            placeholder = { Text("Añade un comentario…", color = OnSurfaceVariant, fontSize = 14.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = SurfaceContainerLow,
                                focusedContainerColor = SurfaceContainerLow,
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Primary,
                            ),
                            singleLine = true,
                        )
                        IconButton(
                            onClick = {
                                if (commentInput.isBlank() || isSendingComment) return@IconButton
                                val text = commentInput
                                scope.launch {
                                    isSendingComment = true
                                    if (runCatching { PostInteractionRepository.addComment(postId, text) }.isSuccess) {
                                        commentInput = ""
                                    }
                                    isSendingComment = false
                                }
                            },
                            enabled = commentInput.isNotBlank() && !isSendingComment,
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Enviar",
                                tint = if (commentInput.isNotBlank() && !isSendingComment)
                                    Primary else OnSurfaceVariant.copy(alpha = 0.4f),
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Header ────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = OnSurface)
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHigh)
                        .clickable(enabled = state.authorId.isNotBlank()) {
                            navController.navigate("${Screens.UserScreen.name}/${state.authorId}")
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (!state.authorPhotoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = state.authorPhotoUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                        )
                    } else {
                        Icon(Icons.Default.Person, null, Modifier.size(22.dp), tint = OnSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(state.authorName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurface)
                    if (state.authorSubtitle.isNotBlank()) {
                        Text(state.authorSubtitle, fontSize = 12.sp, color = OnSurfaceVariant)
                    }
                }

                IconButton(onClick = { }) {
                    Icon(Icons.Default.MoreVert, "Más opciones", tint = OnSurface)
                }
            }

            // ── Stats card (with or without images) ───────────────────────
            PostStatsCard(
                isWorkout = state.isWorkout,
                mediaUrls = state.mediaUrls,
                statOneValue = state.statOneValue,
                statOneLabel = state.statOneLabel,
                statTwoValue = state.statTwoValue,
                statTwoLabel = state.statTwoLabel,
                statThreeValue = state.statThreeValue,
                statThreeLabel = state.statThreeLabel,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )

            // ── Title ─────────────────────────────────────────────────────
            if (state.title.isNotBlank()) {
                Text(
                    state.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }

            // ── Exercise list (workout) ───────────────────────────────────
            if (state.isWorkout && state.exercises.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Primary))
                    Text(
                        "EJERCICIOS · ${state.exercises.size}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = Primary,
                    )
                }
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    state.exercises.forEachIndexed { idx, exercise ->
                        WorkoutExerciseCard(index = idx + 1, exercise = exercise)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── Nutrition description + macros (image now shown in card) ──
            if (!state.isWorkout) {
                if (state.description.isNotBlank()) {
                    Text(
                        state.description,
                        fontSize = 14.sp,
                        color = OnSurface,
                        lineHeight = 21.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                if (state.kcal > 0 || state.proteinG > 0 || state.carbsG > 0) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (state.kcal > 0) DetailMacroChip("${state.kcal}", "kcal", Primary)
                        if (state.proteinG > 0) DetailMacroChip("${state.proteinG}g", "prot", Tertiary)
                        if (state.carbsG > 0) DetailMacroChip("${state.carbsG}g", "carbs", Secondary)
                    }
                }
            }

            // ── Divider + Action row ──────────────────────────────────────
            HorizontalDivider(color = SurfaceContainerHigh, modifier = Modifier.padding(top = 8.dp))

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
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    IconButton(onClick = { postDetailViewModel.toggleLike() }) {
                        Icon(
                            if (state.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            "Me gusta",
                            Modifier.size(26.dp).scale(likeScale),
                            tint = likeColor,
                        )
                    }
                    IconButton(onClick = { showAllComments = true }) {
                        Icon(Icons.Outlined.ChatBubbleOutline, "Comentarios", Modifier.size(24.dp), tint = OnSurfaceVariant)
                    }
                    IconButton(onClick = {
                        val shareText = buildString {
                            append(state.title)
                            if (state.authorName.isNotBlank()) append("\n— ${state.authorName}")
                            if (state.description.isNotBlank()) append("\n${state.description}")
                            if (!postId.isNullOrBlank()) {
                                append("\n\nÁbrelo en FitFusion: fitfusion://post/$postId")
                            }
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, null))
                    }) {
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

            // ── Like count ────────────────────────────────────────────────
            val likeCountInt = state.likeCount.toIntOrNull() ?: 0
            if (likeCountInt > 0) {
                Text(
                    "$likeCountInt me gusta",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = OnSurface,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 4.dp),
                )
            }

            // ── Comment count link ────────────────────────────────────────
            val commentCountInt = state.commentCount.toIntOrNull() ?: 0
            if (commentCountInt > 0) {
                Text(
                    "Ver los $commentCountInt comentarios",
                    fontSize = 14.sp,
                    color = OnSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { showAllComments = true },
                )
            }

            // ── Inline comment preview ────────────────────────────────────
            if (state.previewComments.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    state.previewComments.forEach { comment ->
                        InlineCommentRow(comment = comment)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── Stats card ────────────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PostStatsCard(
    isWorkout: Boolean,
    mediaUrls: List<String>,
    statOneValue: String, statOneLabel: String,
    statTwoValue: String, statTwoLabel: String,
    statThreeValue: String, statThreeLabel: String,
    modifier: Modifier = Modifier,
) {
    val cardColor = if (isWorkout) Primary else Color(0xFFE65C00)
    val hasImages = mediaUrls.isNotEmpty()
    val pagerState = rememberPagerState(pageCount = { mediaUrls.size.coerceAtLeast(1) })

    if (hasImages) {
        // ── Carousel card with stats overlay ─────────────────────────────
        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(24.dp)),
        ) {
            // Images
            when {
                mediaUrls.size > 1 -> {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                    ) { page ->
                        AsyncImage(
                            model = mediaUrls[page],
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                else -> {
                    AsyncImage(
                        model = mediaUrls[0],
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            // Gradient scrim for text readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f)),
                        )
                    ),
            )

            // Stats + dot indicators at the bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    CardStat(statOneValue, statOneLabel)
                    CardStat(statTwoValue, statTwoLabel)
                    CardStat(statThreeValue, statThreeLabel)
                }
                if (mediaUrls.size > 1) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(mediaUrls.size) { index ->
                            val isSelected = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 7.dp else 5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color.White
                                        else Color.White.copy(alpha = 0.5f),
                                    ),
                            )
                        }
                    }
                }
            }

            // Page counter badge (top-right) for multiple images
            if (mediaUrls.size > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.45f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        "${pagerState.currentPage + 1}/${mediaUrls.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            }
        }
    } else {
        // ── No images: solid color card with icon + stats ─────────────────
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(cardColor)
                .padding(horizontal = 24.dp, vertical = 28.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Icon(
                    if (isWorkout) Icons.Outlined.FitnessCenter else Icons.Outlined.Restaurant,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color.White.copy(alpha = 0.9f),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    CardStat(statOneValue, statOneLabel)
                    CardStat(statTwoValue, statTwoLabel)
                    CardStat(statThreeValue, statThreeLabel)
                }
            }
        }
    }
}

@Composable
private fun CardStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Color.White.copy(alpha = 0.75f),
        )
    }
}

// ── Exercise card (workout) ─────────────────────────────────────────────────────
@Composable
private fun WorkoutExerciseCard(index: Int, exercise: ExerciseItem) {
    val sets = exercise.setBreakdown
        .split(" · ")
        .map { it.trim() }
        .filter { it.isNotBlank() }
    val headline = exercise.summary.ifBlank { "${exercise.sets} series" }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceContainerLow)
            .border(1.dp, SurfaceContainerHigh, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header: index badge + name + summary pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("$index", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Primary)
            }
            Text(
                exercise.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Primary.copy(alpha = 0.10f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text(headline, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Primary)
            }
        }

        // Per-set breakdown (only when stored — posts antiguos no lo tienen)
        if (sets.isNotEmpty()) {
            Column {
                sets.forEachIndexed { setIdx, setText ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 3.dp, height = 14.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Primary.copy(alpha = 0.45f)),
                        )
                        Text(
                            "Serie ${setIdx + 1}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            setText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface,
                        )
                    }
                    if (setIdx < sets.size - 1) {
                        HorizontalDivider(color = SurfaceContainerHigh.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

// ── Macro chip (nutrition) ────────────────────────────────────────────────────
@Composable
private fun DetailMacroChip(value: String, label: String, color: Color) {
    Column(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
        Text(label, fontSize = 11.sp, color = color.copy(alpha = 0.7f), fontWeight = FontWeight.SemiBold)
    }
}

// ── Inline comment preview row ────────────────────────────────────────────────
@Composable
private fun InlineCommentRow(comment: FeedComment) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SurfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Person, null, Modifier.size(18.dp), tint = OnSurfaceVariant)
        }
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = OnSurface)) {
                    append(comment.authorName)
                }
                append("  ")
                withStyle(SpanStyle(color = OnSurface)) {
                    append(comment.text)
                }
            },
            fontSize = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}
