package com.fitfusion.app.ui.screens

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
import com.fitfusion.app.R
import com.fitfusion.app.ui.components.*
import com.fitfusion.app.ui.theme.*
import com.fitfusion.app.viewmodel.AuthViewModel

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

    Box(
        modifier = Modifier.fillMaxSize().background(Surface)
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
                modifier = Modifier.size(120.dp).clip(CircleShape).background(PrimaryContainer.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, Modifier.size(60.dp), tint = Primary)
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Create Account", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Start your fitness journey today.", fontSize = 15.sp, color = OnSurfaceVariant)

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    AuthField("Full Name", state.displayName, authViewModel::onSignUpNameChange, "John Doe", Icons.Default.Person)
                    Spacer(modifier = Modifier.height(16.dp))
                    AuthField("Email Address", state.email, authViewModel::onSignUpEmailChange, "name@example.com", Icons.Default.Email)
                    Spacer(modifier = Modifier.height(16.dp))
                    AuthField("Password", state.password, authViewModel::onSignUpPasswordChange, "••••••••", Icons.Default.Lock, isPassword = true)
                    Spacer(modifier = Modifier.height(16.dp))
                    AuthField("Confirm Password", state.confirmPassword, authViewModel::onSignUpConfirmPasswordChange, "••••••••", Icons.Default.Lock, isPassword = true)

                    Spacer(modifier = Modifier.height(24.dp))
                    GreenGradientButton("Create Account") { authViewModel.attemptSignUp(onSignUpSuccess) }
                    Spacer(modifier = Modifier.height(20.dp))
                    DividerWithText("Or continue with")
                    Spacer(modifier = Modifier.height(16.dp))
                    SocialButtonsRow()
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account?", fontSize = 14.sp, color = OnSurfaceVariant)
                TextButton(onClick = onNavigateToLogin) {
                    Text("Sign In", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Primary)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(16.dp))
    }
}