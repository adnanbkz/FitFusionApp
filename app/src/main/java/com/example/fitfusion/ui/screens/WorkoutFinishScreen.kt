package com.example.fitfusion.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.WorkoutFinishViewModel
import com.example.fitfusion.viewmodel.formatElapsed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaWorkoutFinish(
    navController: NavHostController,
    workoutFinishViewModel: WorkoutFinishViewModel = viewModel(),
) {
    val state by workoutFinishViewModel.uiState.collectAsState()

    LaunchedEffect(state.session) {
        if (state.session == null && state.savedWorkoutId == null) {
            navController.popBackStack()
        }
    }

    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5),
    ) { uris -> if (uris.isNotEmpty()) workoutFinishViewModel.onMediaPicked(uris) }

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = { Text("Finalizar entrenamiento", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            state.session?.let { session ->
                Card(
                    shape  = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                "RESUMEN", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp, color = Primary,
                            )
                            Text(
                                formatElapsed(state.elapsedSeconds),
                                fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OnSurface,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${session.exerciseCount}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                            Text("EJERCICIOS", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = OnSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text("${session.totalSets} series", fontSize = 12.sp, color = OnSurfaceVariant)
                        }
                    }
                }
            }

            OutlinedTextField(
                value         = state.title,
                onValueChange = workoutFinishViewModel::onTitleChange,
                label         = { Text("Título") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = SurfaceContainerLowest,
                    focusedContainerColor   = SurfaceContainerLowest,
                    unfocusedBorderColor    = Color.Transparent,
                    focusedBorderColor      = Primary,
                ),
            )

            OutlinedTextField(
                value         = state.description,
                onValueChange = workoutFinishViewModel::onDescriptionChange,
                label         = { Text("Descripción (opcional)") },
                modifier      = Modifier.fillMaxWidth().heightIn(min = 110.dp),
                shape         = RoundedCornerShape(12.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = SurfaceContainerLowest,
                    focusedContainerColor   = SurfaceContainerLowest,
                    unfocusedBorderColor    = Color.Transparent,
                    focusedBorderColor      = Primary,
                ),
                maxLines      = 5,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "ADJUNTAR FOTOS O VÍDEOS",
                    fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp, color = Primary,
                )
                Text(
                    "${state.mediaUris.size}/5",
                    fontSize = 12.sp, color = OnSurfaceVariant,
                )
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (state.mediaUris.size < 5) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SurfaceContainerLow)
                                .clickable {
                                    mediaPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                    )
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Primary)
                                Spacer(Modifier.height(4.dp))
                                Text("Añadir", fontSize = 11.sp, color = Primary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                items(state.mediaUris) { uri ->
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceContainerLow),
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                        IconButton(
                            onClick = { workoutFinishViewModel.removeMedia(uri) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f)),
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Quitar",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }
            }

            state.errorMessage?.let { msg ->
                Text(msg, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    workoutFinishViewModel.save {
                        navController.popBackStack(Screens.HomeScreen.name, inclusive = false)
                    }
                },
                enabled  = !state.isSaving,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Primary),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White, strokeWidth = 2.dp,
                    )
                } else {
                    Text("Guardar entrenamiento", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

