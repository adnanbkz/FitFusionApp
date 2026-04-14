package com.example.fitfusion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitfusion.R
import com.example.fitfusion.ui.components.*
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.AuthViewModel

@Composable
fun PantallaSignUp(
    onSignUpSuccess: (username: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val state by authViewModel.signUpState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            authViewModel.clearSignUpError()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Surface)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("FitFusion", fontSize = 24.sp, fontWeight = FontWeight.Black, color = OnSurface)
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(SurfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(R.drawable.ic_dumbbell), null, Modifier.size(20.dp), tint = OnSurfaceVariant)
                }
            }

            Box(
                modifier = Modifier
                    .padding(top = 32.dp)
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(PrimaryContainer.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, Modifier.size(60.dp), tint = Primary)
            }

            Text(
                "Crear cuenta",
                fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OnSurface,
                modifier = Modifier.padding(top = 20.dp)
            )
            Text(
                "Comienza tu camino fitness hoy.",
                fontSize = 15.sp, color = OnSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 28.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AuthField("Nombre completo", state.displayName, authViewModel::onSignUpNameChange, "Juan García", Icons.Default.Person)
                    AuthField("Correo electrónico", state.email, authViewModel::onSignUpEmailChange, "nombre@ejemplo.com", Icons.Default.Email)
                    AuthField("Contraseña", state.password, authViewModel::onSignUpPasswordChange, "••••••••", Icons.Default.Lock, isPassword = true)
                    AuthField("Confirmar contraseña", state.confirmPassword, authViewModel::onSignUpConfirmPasswordChange, "••••••••", Icons.Default.Lock, isPassword = true)
                    GreenGradientButton("Crear cuenta") { authViewModel.attemptSignUp(onSignUpSuccess) }
                    DividerWithText("O continúa con")
                    SocialButtonsRow()
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text("¿Ya tienes cuenta?", fontSize = 14.sp, color = OnSurfaceVariant)
                TextButton(onClick = onNavigateToLogin) {
                    Text("Iniciar sesión", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Primary)
                }
            }
        }

        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(16.dp))
    }
}