package com.example.fitfusion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.fitfusion.data.repository.UserProfile
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.FollowListViewModel
import com.google.firebase.auth.FirebaseAuth

/**
 * Lista de seguidores o seguidos de un usuario, estilo Instagram. Cada fila es
 * navegable al perfil correspondiente. [mode] es "followers" o "following".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFollowList(
    navController: NavHostController,
    uid: String,
    mode: String,
    viewModel: FollowListViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(uid, mode) { viewModel.load(uid, mode) }

    val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (mode == "following") "Siguiendo" else "Seguidores",
                        fontWeight = FontWeight.Bold, fontSize = 17.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface),
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    color = Primary,
                    modifier = Modifier.align(Alignment.Center),
                )

                state.errorMessage != null && state.profiles.isEmpty() -> Text(
                    state.errorMessage!!,
                    fontSize = 13.sp, color = OnSurfaceVariant, textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = 40.dp),
                )

                state.profiles.isEmpty() -> Text(
                    if (mode == "following") "Todavía no sigue a nadie"
                    else "Todavía no tiene seguidores",
                    fontSize = 14.sp, color = OnSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center),
                )

                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.profiles, key = { it.uid }) { profile ->
                        FollowUserRow(
                            profile = profile,
                            onClick = {
                                if (profile.uid == currentUid) {
                                    navController.navigate(Screens.ProfileScreen.name)
                                } else {
                                    navController.navigate("${Screens.UserScreen.name}/${profile.uid}")
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FollowUserRow(profile: UserProfile, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(SurfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            if (!profile.photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = profile.photoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                )
            } else {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(24.dp), tint = OnSurfaceVariant)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                profile.displayName,
                fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = OnSurface,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Text(
                profile.username,
                fontSize = 13.sp, color = OnSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
