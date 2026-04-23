package com.example.fitfusion.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.video.AudioConfig
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.fitfusion.ui.theme.Primary
import com.example.fitfusion.ui.theme.Tertiary
import java.io.File

private enum class CaptureMode { PHOTO, VIDEO }

@Composable
fun PantallaCamera(
    onClose: () -> Unit,
    onMediaCaptured: (Uri, Boolean) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasCameraPermission = result[Manifest.permission.CAMERA] ?: hasCameraPermission
        hasAudioPermission  = result[Manifest.permission.RECORD_AUDIO] ?: hasAudioPermission
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission || !hasAudioPermission) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
            CameraContent(
                lifecycleOwner   = lifecycleOwner,
                canRecordAudio   = hasAudioPermission,
                onClose          = onClose,
                onMediaCaptured  = onMediaCaptured,
            )
        } else {
            PermissionPrompt(
                onRequest = {
                    permissionLauncher.launch(
                        arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                    )
                },
                onClose = onClose,
            )
        }
    }
}

@Composable
private fun CameraContent(
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    canRecordAudio: Boolean,
    onClose: () -> Unit,
    onMediaCaptured: (Uri, Boolean) -> Unit,
) {
    val context = LocalContext.current

    var mode by remember { mutableStateOf(CaptureMode.PHOTO) }
    var lensBack by remember { mutableStateOf(true) }
    var isRecording by remember { mutableStateOf(false) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }

    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE
            )
        }
    }

    DisposableEffect(lifecycleOwner) {
        controller.bindToLifecycle(lifecycleOwner)
        onDispose {
            activeRecording?.stop()
            controller.unbind()
        }
    }

    LaunchedEffect(lensBack) {
        controller.cameraSelector = if (lensBack) CameraSelector.DEFAULT_BACK_CAMERA
                                    else CameraSelector.DEFAULT_FRONT_CAMERA
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            PreviewView(ctx).apply {
                this.controller = controller
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        CircleButton(
            icon = Icons.Default.Close,
            contentDescription = "Cerrar cámara",
            onClick = onClose,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ModeToggle(
            mode = mode,
            enabled = !isRecording,
            onSelect = { mode = it },
        )

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(Modifier.width(56.dp))

            CaptureButton(
                mode = mode,
                isRecording = isRecording,
                onTap = {
                    when (mode) {
                        CaptureMode.PHOTO -> takePhoto(
                            context = context,
                            controller = controller,
                            onSaved = { onMediaCaptured(it, false) },
                        )
                        CaptureMode.VIDEO -> {
                            if (isRecording) {
                                activeRecording?.stop()
                            } else {
                                activeRecording = startRecording(
                                    context = context,
                                    controller = controller,
                                    withAudio = canRecordAudio,
                                    onFinalized = { uri ->
                                        isRecording = false
                                        activeRecording = null
                                        onMediaCaptured(uri, true)
                                    },
                                )
                                if (activeRecording != null) isRecording = true
                            }
                        }
                    }
                }
            )

            CircleButton(
                icon = Icons.Default.Cameraswitch,
                contentDescription = "Cambiar cámara",
                onClick = { if (!isRecording) lensBack = !lensBack },
                enabled = !isRecording,
            )
        }
    }
}

@Composable
private fun ModeToggle(
    mode: CaptureMode,
    enabled: Boolean,
    onSelect: (CaptureMode) -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ModeChip(
            label = "FOTO",
            selected = mode == CaptureMode.PHOTO,
            enabled = enabled,
            onClick = { if (enabled) onSelect(CaptureMode.PHOTO) }
        )
        ModeChip(
            label = "VIDEO",
            selected = mode == CaptureMode.VIDEO,
            enabled = enabled,
            onClick = { if (enabled) onSelect(CaptureMode.VIDEO) }
        )
    }
}

@Composable
private fun ModeChip(label: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.8.sp,
            color = if (selected) Color.Black else Color.White.copy(alpha = 0.85f),
        )
    }
}

@Composable
private fun CaptureButton(
    mode: CaptureMode,
    isRecording: Boolean,
    onTap: () -> Unit,
) {
    val outerScale by animateFloatAsState(
        targetValue = if (isRecording) 1.08f else 1f,
        label = "captureOuterScale"
    )
    Box(
        modifier = Modifier
            .size(86.dp)
            .scale(outerScale)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.25f))
            .clickable(onClick = onTap)
            .padding(6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    when {
                        isRecording -> Tertiary
                        mode == CaptureMode.VIDEO -> Color.White
                        else -> Color.White
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            when {
                isRecording -> Icon(
                    Icons.Default.Stop,
                    contentDescription = "Detener grabación",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp),
                )
                mode == CaptureMode.VIDEO -> Icon(
                    Icons.Default.Videocam,
                    contentDescription = "Grabar video",
                    tint = Tertiary,
                    modifier = Modifier.size(30.dp),
                )
                else -> Icon(
                    Icons.Default.PhotoCamera,
                    contentDescription = "Tomar foto",
                    tint = Primary,
                    modifier = Modifier.size(30.dp),
                )
            }
        }
    }
}

@Composable
private fun CircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = if (enabled) Color.White else Color.White.copy(alpha = 0.4f),
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun PermissionPrompt(onRequest: () -> Unit, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Primary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.PhotoCamera,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(34.dp),
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "Necesitamos la cámara",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Permite el acceso para capturar fotos de recetas y grabar vídeos de entrenamiento.",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.75f),
        )
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(Primary, Color(0xFF32CD32))))
                .clickable(onClick = onRequest),
            contentAlignment = Alignment.Center,
        ) {
            Text("Dar permisos", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Volver",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onClose)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

private fun takePhoto(
    context: android.content.Context,
    controller: LifecycleCameraController,
    onSaved: (Uri) -> Unit,
) {
    val dir = File(context.cacheDir, "fitfusion_media").apply { mkdirs() }
    val file = File(dir, "photo_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
    controller.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                onSaved(result.savedUri ?: Uri.fromFile(file))
            }
            override fun onError(exception: ImageCaptureException) { }
        }
    )
}

private fun startRecording(
    context: android.content.Context,
    controller: LifecycleCameraController,
    withAudio: Boolean,
    onFinalized: (Uri) -> Unit,
): Recording? {
    val dir = File(context.cacheDir, "fitfusion_media").apply { mkdirs() }
    val file = File(dir, "video_${System.currentTimeMillis()}.mp4")
    val outputOptions = FileOutputOptions.Builder(file).build()

    return try {
        controller.startRecording(
            outputOptions,
            if (withAudio) AudioConfig.create(true) else AudioConfig.AUDIO_DISABLED,
            ContextCompat.getMainExecutor(context),
        ) { event ->
            if (event is VideoRecordEvent.Finalize) {
                if (!event.hasError()) {
                    onFinalized(event.outputResults.outputUri.takeIf { it != Uri.EMPTY } ?: Uri.fromFile(file))
                }
            }
        }
    } catch (_: SecurityException) {
        null
    }
}
