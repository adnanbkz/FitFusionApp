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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitfusion.R
import com.example.fitfusion.ui.components.*
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.AuthViewModel

@Composable
fun PantallaLogin(
    onLoginSuccess: (username: String) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onSkip: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val state by authViewModel.loginState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            authViewModel.clearLoginError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

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

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier.size(120.dp).clip(CircleShape).background(SurfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, "Avatar", Modifier.size(60.dp), tint = OnSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Welcome Back", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Precision tracking for your fitness journey.", fontSize = 15.sp, color = OnSurfaceVariant, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Email Address", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = OnSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    AuthField("Email Address", state.email, authViewModel::onLoginEmailChange, "name@example.com", Icons.Default.Email)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Password", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = OnSurface)
                        TextButton(onClick = { }) {
                            Text("Forgot Password?", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Primary)
                        }
                    }
                    AuthField("Password", state.password, authViewModel::onLoginPasswordChange, "••••••••", Icons.Default.Lock, isPassword = true)

                    Spacer(modifier = Modifier.height(24.dp))
                    GreenGradientButton("Sign In") { authViewModel.attemptLogin(onLoginSuccess) }
                    Spacer(modifier = Modifier.height(20.dp))
                    DividerWithText("Or continue with")
                    Spacer(modifier = Modifier.height(16.dp))
                    SocialButtonsRow()
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account?", fontSize = 14.sp, color = OnSurfaceVariant)
                TextButton(onClick = onNavigateToSignUp) {
                    Text("Create New Account", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Primary)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(16.dp))
    }
}