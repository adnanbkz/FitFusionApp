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
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.fitfusion.viewmodel.PostDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPostDetail(
    navController: NavHostController,
    postId: String? = null,
    postDetailViewModel: PostDetailViewModel = viewModel()
) {
    val state by postDetailViewModel.uiState.collectAsState()

    LaunchedEffect(postId) {
        postDetailViewModel.loadPost(postId)
    }

    Box(modifier = Modifier.fillMaxSize().background(Surface)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // TopAppBar: username as title (Instagram style)
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

            // Media: full-width, no side padding, aspectRatio
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
                if (state.mediaLabel.isNotBlank()) {
                    Text(
                        state.mediaLabel,
                        fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 72.dp),
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

            // Action bar — Instagram style: ♥ 💬 ✈ left | 🔖 right
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
                    IconButton(onClick = { }) {
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

            // Likes count
            if (state.likeCount.isNotBlank() && state.likeCount != "0") {
                Text(
                    "${state.likeCount} me gusta",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = OnSurface,
                    modifier = Modifier.padding(horizontal = 14.dp).padding(bottom = 4.dp),
                )
            }

            // Caption: author bold + title
            if (state.title.isNotBlank()) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(state.authorName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = OnSurface)
                    Text(state.title, fontSize = 14.sp, color = OnSurface, modifier = Modifier.weight(1f, fill = false))
                }
            }

            // Description
            if (state.description.isNotBlank()) {
                Text(
                    state.description,
                    fontSize = 14.sp,
                    color = OnSurface,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
                )
            }

            // Hashtags
            if (state.hashtags.isNotBlank()) {
                Text(
                    state.hashtags,
                    fontSize = 14.sp,
                    color = Primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
                )
            }

            // Stat cards
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatCard(state.metricOneLabel, state.metricOneValue, state.metricOneUnit, Modifier.weight(1f))
                StatCard(state.metricTwoLabel, state.metricTwoValue, state.metricTwoUnit, Modifier.weight(1f))
            }

            // Comments link
            if (state.commentCount != "0") {
                Text(
                    "Ver los ${state.commentCount} comentarios",
                    fontSize = 14.sp,
                    color = OnSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                        .clickable { },
                )
            }

            // Comments
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                state.comments.forEach { comment ->
                    if (comment.isAuthorReply) {
                        Row(
                            modifier = Modifier.padding(start = 40.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier.size(32.dp).clip(CircleShape).background(PrimaryContainer.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Default.Person, null, Modifier.size(16.dp), tint = Primary)
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(comment.author, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Primary)
                                    Text(
                                        "AUTOR", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Primary,
                                        modifier = Modifier
                                            .background(PrimaryContainer.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                                Text(comment.text, fontSize = 13.sp, color = Primary.copy(alpha = 0.85f))
                                Text("${comment.time} · Responder", fontSize = 11.sp, color = OnSurfaceVariant)
                            }
                        }
                    } else {
                        CommentItem(comment.author, comment.text, comment.time, comment.likes)
                    }
                }
            }

            Box(modifier = Modifier.height(80.dp))
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(SurfaceContainerLowest)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            state.commentErrorMessage?.let { message ->
                Text(
                    message,
                    color = Tertiary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 44.dp, bottom = 6.dp),
                )
            }

            Row(
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
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = SurfaceContainerLow, focusedContainerColor = SurfaceContainerLow,
                        unfocusedBorderColor = Color.Transparent, focusedBorderColor = Primary
                    ),
                    singleLine = true,
                    enabled = !state.isSendingComment,
                )
                val canSend = state.commentText.isNotBlank() && !state.isSendingComment
                IconButton(
                    onClick = { postDetailViewModel.sendComment() },
                    enabled = canSend,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (canSend) Primary else SurfaceContainerHigh)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        null,
                        tint = if (canSend) Color.White else OnSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
