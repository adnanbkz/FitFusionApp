package com.example.fitfusion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.fitfusion.data.models.Recipe
import com.example.fitfusion.data.models.UserPost
import com.example.fitfusion.data.repository.UserProfile
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.SearchCategory
import com.example.fitfusion.viewmodel.UserSearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaUserSearch(
    navController: NavHostController,
    viewModel: UserSearchViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        placeholder = {
                            val hint = when (state.category) {
                                SearchCategory.PROFILES -> "Buscar usuarios…"
                                SearchCategory.WORKOUTS -> "Buscar entrenos…"
                                SearchCategory.RECIPES  -> "Buscar recetas…"
                            }
                            Text(hint, color = OnSurfaceVariant, fontSize = 15.sp)
                        },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = OnSurfaceVariant) },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = SurfaceContainerLow,
                            focusedContainerColor = SurfaceContainerLow,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Primary,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Category filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(SearchCategory.entries) { cat ->
                    val icon: ImageVector = when (cat) {
                        SearchCategory.PROFILES -> Icons.Default.Person
                        SearchCategory.WORKOUTS -> Icons.Default.FitnessCenter
                        SearchCategory.RECIPES  -> Icons.Default.MenuBook
                    }
                    FilterChip(
                        selected = state.category == cat,
                        onClick = { viewModel.onCategoryChange(cat) },
                        label = { Text(cat.label, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary.copy(alpha = 0.15f),
                            selectedLabelColor     = Primary,
                            selectedLeadingIconColor = Primary,
                        ),
                        shape = RoundedCornerShape(20.dp),
                    )
                }
            }

            HorizontalDivider(color = SurfaceContainerHigh, thickness = 0.5.dp)

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(
                            color = Primary,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }

                    state.query.isBlank() -> {
                        SearchEmptyHint(
                            category = state.category,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }

                    state.errorMessage != null -> {
                        SearchErrorState(
                            message = state.errorMessage!!,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }

                    state.category == SearchCategory.PROFILES -> {
                        if (state.hasSearched && state.profileResults.isEmpty()) {
                            NoResultsState(query = state.query, modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 8.dp),
                            ) {
                                items(state.profileResults, key = { it.uid }) { profile ->
                                    UserResultRow(
                                        profile = profile,
                                        onClick = {
                                            navController.navigate("${Screens.UserScreen.name}/${profile.uid}")
                                        },
                                    )
                                    HorizontalDivider(
                                        color = SurfaceContainerHigh,
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(start = 76.dp),
                                    )
                                }
                            }
                        }
                    }

                    state.category == SearchCategory.WORKOUTS -> {
                        if (state.hasSearched && state.workoutResults.isEmpty()) {
                            NoResultsState(query = state.query, modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 8.dp),
                            ) {
                                items(state.workoutResults, key = { it.id }) { post ->
                                    WorkoutResultRow(
                                        post = post,
                                        onClick = {
                                            navController.navigate("${Screens.PostDetailScreen.name}/${post.id}")
                                        },
                                    )
                                    HorizontalDivider(
                                        color = SurfaceContainerHigh,
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(start = 16.dp),
                                    )
                                }
                            }
                        }
                    }

                    state.category == SearchCategory.RECIPES -> {
                        if (state.hasSearched && state.recipeResults.isEmpty()) {
                            NoResultsState(query = state.query, modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 8.dp),
                            ) {
                                items(state.recipeResults, key = { it.id }) { recipe ->
                                    RecipeResultRow(recipe = recipe)
                                    HorizontalDivider(
                                        color = SurfaceContainerHigh,
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(start = 16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserResultRow(profile: UserProfile, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        UserAvatar(photoUrl = profile.photoUrl, displayName = profile.displayName, size = 48)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                profile.displayName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                profile.username,
                fontSize = 13.sp,
                color = OnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun WorkoutResultRow(post: UserPost, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                post.workoutName?.takeIf { it.isNotBlank() } ?: "Entreno",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (post.caption.isNotBlank()) {
                Text(
                    post.caption,
                    fontSize = 12.sp,
                    color = OnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (post.workoutDurationMinutes != null) {
                Text(
                    "${post.workoutDurationMinutes} min",
                    fontSize = 11.sp,
                    color = OnSurfaceVariant.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun RecipeResultRow(recipe: Recipe) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (!recipe.photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = recipe.photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp)),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                recipe.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val meta = listOfNotNull(
                recipe.authorName?.let { "por $it" },
                recipe.kcal?.let { "$it kcal" },
                recipe.cookTimeMin?.let { "${it} min" },
            ).joinToString(" · ")
            if (meta.isNotBlank()) {
                Text(meta, fontSize = 12.sp, color = OnSurfaceVariant, maxLines = 1)
            }
        }
    }
}

@Composable
internal fun UserAvatar(photoUrl: String?, displayName: String, size: Int) {
    val initials = displayName.trim().split(' ')
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "?" }

    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Primary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
            )
        } else {
            Text(
                initials,
                fontSize = (size * 0.35f).sp,
                fontWeight = FontWeight.Bold,
                color = Primary,
            )
        }
    }
}

@Composable
private fun SearchEmptyHint(category: SearchCategory, modifier: Modifier = Modifier) {
    val text = when (category) {
        SearchCategory.PROFILES -> "Busca usuarios por nombre\no por @usuario"
        SearchCategory.WORKOUTS -> "Busca entrenos por nombre\no descripción"
        SearchCategory.RECIPES  -> "Busca recetas de la comunidad\npor nombre"
    }
    Column(
        modifier = modifier.padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            tint = OnSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(48.dp),
        )
        Text(text, fontSize = 15.sp, color = OnSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SearchErrorState(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            tint = Color(0xFFE53935).copy(alpha = 0.7f),
            modifier = Modifier.size(48.dp),
        )
        Text(
            "Error al buscar",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = OnSurface,
            textAlign = TextAlign.Center,
        )
        Text(message, fontSize = 12.sp, color = OnSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
private fun NoResultsState(query: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            Icons.Default.Person,
            contentDescription = null,
            tint = OnSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(48.dp),
        )
        Text(
            "Sin resultados para \"$query\"",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = OnSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            "Prueba con otro término",
            fontSize = 13.sp,
            color = OnSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
