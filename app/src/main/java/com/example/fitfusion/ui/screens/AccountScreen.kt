package com.example.fitfusion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.AccountViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAccount(
    navController: NavHostController,
    accountViewModel: AccountViewModel = viewModel()
) {
    val state by accountViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            snackbarHostState.showSnackbar("Cambios guardados correctamente")
            accountViewModel.clearError()
        }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            accountViewModel.clearError()
}
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = { Text("Cuenta", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Avatar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainerLow)
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person, null,
                            Modifier.size(48.dp), tint = OnSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Edit, null,
                            Modifier.size(14.dp), tint = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Cambiar foto de perfil",
                    fontSize = 13.sp,
                    color = Primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Información personal
            SettingsSectionHeader("INFORMACIÓN PERSONAL")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    AccountField(
                        label = "Nombre completo",
                        value = state.displayName,
                        onValueChange = accountViewModel::onDisplayNameChange,
                        placeholder = "Tu nombre"
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = OutlineVariant.copy(alpha = 0.3f)
                    )
                    AccountField(
                        label = "Nombre de usuario",
                        value = state.username,
                        onValueChange = accountViewModel::onUsernameChange,
                        placeholder = "@usuario"
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = OutlineVariant.copy(alpha = 0.3f)
                    )
                    AccountField(
                        label = "Correo electrónico",
                        value = state.email,
                        onValueChange = accountViewModel::onEmailChange,
                        placeholder = "correo@ejemplo.com"
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = OutlineVariant.copy(alpha = 0.3f)
                    )
                    AccountField(
                        label = "Biografía",
                        value = state.bio,
                        onValueChange = accountViewModel::onBioChange,
                        placeholder = "Cuéntanos algo sobre ti",
                        singleLine = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = accountViewModel::saveProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = !state.isSaving
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar cambios", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Cambiar contraseña
            SettingsSectionHeader("CAMBIAR CONTRASEÑA")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    AccountPasswordField(
                        label = "Contraseña actual",
                        value = state.currentPassword,
                        onValueChange = accountViewModel::onCurrentPasswordChange
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = OutlineVariant.copy(alpha = 0.3f)
                    )
                    AccountPasswordField(
                        label = "Nueva contraseña",
                        value = state.newPassword,
                        onValueChange = accountViewModel::onNewPasswordChange
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = OutlineVariant.copy(alpha = 0.3f)
                    )
                    AccountPasswordField(
                        label = "Confirmar nueva contraseña",
                        value = state.confirmNewPassword,
                        onValueChange = accountViewModel::onConfirmNewPasswordChange
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = accountViewModel::changePassword,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
            ) {
                Icon(Icons.Default.Lock, null, Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Actualizar contraseña", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Zona peligrosa
            SettingsSectionHeader("ZONA PELIGROSA")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Tertiary.copy(alpha = 0.06f)
                ),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Eliminar cuenta",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Tertiary
                        )
                        Text(
                            "Esta acción es permanente e irreversible",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = { },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Tertiary),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, Tertiary.copy(alpha = 0.5f)
                        )
                    ) {
                        Text("Eliminar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(text: String) {
    Text(
        text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        color = Primary,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
private fun AccountField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true
) {
    Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = OnSurfaceVariant)
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = OnSurfaceVariant, fontSize = 14.sp) },
        singleLine = singleLine,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = SurfaceContainerLow,
            focusedContainerColor = SurfaceContainerLow,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Primary
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun AccountPasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = OnSurfaceVariant)
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("••••••••", color = OnSurfaceVariant) },
        visualTransformation = PasswordVisualTransformation(),
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = SurfaceContainerLow,
            focusedContainerColor = SurfaceContainerLow,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Primary
        ),
        modifier = Modifier.fillMaxWidth()
    )
}