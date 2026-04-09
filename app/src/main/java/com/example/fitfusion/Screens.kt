package com.fitfusion.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.fitfusion.R
import kotlinx.coroutines.launch

// ════════════════════════════════════════════════════════════════
// Enum — all app routes
// ════════════════════════════════════════════════════════════════

enum class Screens {
    LoginScreen,
    SignUpScreen,
    HomeScreen,
    TrackingScreen,
    ProfileScreen,
    SettingsScreen,
    PostDetailScreen,
    WorkoutSummaryScreen
}

// ════════════════════════════════════════════════════════════════
// Design tokens (DESIGN.md — "The Kinetic Editorial")
// ════════════════════════════════════════════════════════════════

private val Primary = Color(0xFF006E0A)
private val PrimaryContainer = Color(0xFF32CD32)
private val Secondary = Color(0xFF476083)
private val Tertiary = Color(0xFFB02F00)
private val Surface = Color(0xFFFBF8FE)
private val SurfaceContainerLow = Color(0xFFF6F2F8)
private val SurfaceContainerLowest = Color(0xFFFFFFFF)
private val SurfaceContainerHigh = Color(0xFFEAE7ED)
private val OnSurface = Color(0xFF1B1B1F)
private val OnSurfaceVariant = Color(0xFF757575)
private val OutlineVariant = Color(0xFFBCCBB4)

private val GreenGradientBrush = Brush.linearGradient(
    colors = listOf(Primary, PrimaryContainer)
)

// ════════════════════════════════════════════════════════════════
// 1. LOGIN
// ════════════════════════════════════════════════════════════════

@Composable
fun PantallaLogin(
    onLoginSuccess: (username: String) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onSkip: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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

            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FitFusion",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = OnSurface
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_dumbbell),
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Avatar circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(60.dp),
                    tint = OnSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Welcome Back",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Precision tracking for your fitness journey.",
                fontSize = 15.sp,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Form card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    Text("Email Address", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = OnSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("name@example.com", color = OnSurfaceVariant) },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = OnSurfaceVariant) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = SurfaceContainerLowest,
                            focusedContainerColor = SurfaceContainerLowest,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Primary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

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
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("••••••••", color = OnSurfaceVariant) },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = OnSurfaceVariant) },
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = SurfaceContainerLowest,
                            focusedContainerColor = SurfaceContainerLowest,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Primary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    GreenGradientButton(
                        text = "Sign In",
                        onClick = {
                            if (email.isNotBlank() && password.isNotBlank()) {
                                onLoginSuccess(email.substringBefore("@"))
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please enter email and password")
                                }
                            }
                        }
                    )

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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

// ════════════════════════════════════════════════════════════════
// 2. SIGN UP
// ════════════════════════════════════════════════════════════════

@Composable
fun PantallaSignUp(
    onSignUpSuccess: (username: String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_dumbbell),
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(PrimaryContainer.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = Primary)
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Create Account", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Start your fitness journey today.", fontSize = 15.sp, color = OnSurfaceVariant)

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    AuthField("Full Name", displayName, { displayName = it }, "John Doe", Icons.Default.Person)
                    Spacer(modifier = Modifier.height(16.dp))
                    AuthField("Email Address", email, { email = it }, "name@example.com", Icons.Default.Email)
                    Spacer(modifier = Modifier.height(16.dp))
                    AuthField("Password", password, { password = it }, "••••••••", Icons.Default.Lock, isPassword = true)
                    Spacer(modifier = Modifier.height(16.dp))
                    AuthField("Confirm Password", confirmPassword, { confirmPassword = it }, "••••••••", Icons.Default.Lock, isPassword = true)

                    Spacer(modifier = Modifier.height(24.dp))

                    GreenGradientButton(
                        text = "Create Account",
                        onClick = {
                            when {
                                displayName.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() ->
                                    scope.launch { snackbarHostState.showSnackbar("Please fill in all fields") }
                                password != confirmPassword ->
                                    scope.launch { snackbarHostState.showSnackbar("Passwords do not match") }
                                password.length < 6 ->
                                    scope.launch { snackbarHostState.showSnackbar("Password must be at least 6 characters") }
                                else -> onSignUpSuccess(displayName)
                            }
                        }
                    )

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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

// ════════════════════════════════════════════════════════════════
// 3. HOME / FEED
// ════════════════════════════════════════════════════════════════

@Composable
fun PantallaHome(navController: NavHostController, userName: String?) {

    Box(modifier = Modifier.fillMaxSize().background(Surface)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, Modifier.size(20.dp), tint = OnSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Kinetic", fontSize = 22.sp, fontWeight = FontWeight.Black, color = OnSurface)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Search, "Search", tint = OnSurface)
                    }
                }
            }

            // Daily Momentum
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "DAILY MOMENTUM",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = OnSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("82", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                                Text(
                                    "%",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = OnSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            Text("1,450 kcal remaining", fontSize = 14.sp, color = OnSurfaceVariant)
                        }
                        MomentumRing(progress = 0.82f, size = 80)
                    }
                }
            }

            // Feed posts
            item {
                FeedPost(
                    author = "Alex Miller",
                    time = "2 hours ago",
                    tag = "Morning Run",
                    likes = 124,
                    comments = 18,
                    description = "Crushing the morning miles. The air in the valley was crisp today! \uD83C\uDF32\uD83C\uDFC3 #KineticRun #MorningVibes",
                    navController = navController
                )
            }
            item {
                FeedPost(
                    author = "Sarah Chen",
                    time = "4 hours ago",
                    tag = "Post-Workout Fuel",
                    likes = 89,
                    comments = 5,
                    description = "High protein, high micronutrients. This salmon bowl is exactly what the body needs after leg day. \uD83C\uDF63\uD83D\uDCAA #NutritionMatters",
                    navController = navController
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }

        // FAB
        FloatingActionButton(
            onClick = { },
            containerColor = Primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 32.dp, bottom = 16.dp)
        ) {
            Icon(Icons.Default.Add, "New post")
        }
    }
}

// ════════════════════════════════════════════════════════════════
// 4. TRACKING (Nutrition)
// ════════════════════════════════════════════════════════════════

@Composable
fun PantallaTracking(navController: NavHostController) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top bar
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_dumbbell),
                            null,
                            Modifier.size(18.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kinetic", fontSize = 22.sp, fontWeight = FontWeight.Black, color = OnSurface)
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Search, "Search", tint = OnSurface)
                }
            }
        }

        // Daily Goal ring
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "DAILY GOAL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = Primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(contentAlignment = Alignment.Center) {
                        MomentumRing(progress = 0.65f, size = 140)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("1,420", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                            Text(
                                "KCAL LEFT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = OnSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatColumn("EATEN", "980")
                        StatColumn("BURNED", "340")
                    }
                }
            }
        }

        // Macro Balance
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Macro Balance", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                    Spacer(modifier = Modifier.height(16.dp))
                    MacroRow("PROTEIN", 112, 160, Primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    MacroRow("CARBS", 145, 210, Secondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    MacroRow("FATS", 42, 65, Tertiary)
                    Spacer(modifier = Modifier.height(16.dp))

                    // AI tip
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("\uD83E\uDD16", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "You're slightly under your protein goal. Consider a Greek yogurt snack.",
                                fontSize = 13.sp,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Log buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, OnSurface.copy(alpha = 0.15f))
                ) {
                    Text("Log Food", color = OnSurface, fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, OnSurface.copy(alpha = 0.15f))
                ) {
                    Text("Log Workout", color = OnSurface, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Recent Logs header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Logs", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                TextButton(onClick = { }) {
                    Text("VIEW ALL", fontSize = 12.sp, color = Primary, fontWeight = FontWeight.Bold)
                }
            }
        }
        item { RecentLogItem("\uD83C\uDF5C", "Quinoa Buddha Bowl", "Lunch • 12:45 PM", "450", "KCAL") }
        item { RecentLogItem("⚡", "High Intensity Inter...", "Workout • 08:30 AM", "-340", "KCAL") }
        item { RecentLogItem("☕", "Iced Oat Milk Latte", "Breakfast • 07:15 AM", "180", "KCAL") }

        // Challenge card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryContainer.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, Modifier.size(48.dp), tint = Primary.copy(alpha = 0.4f))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "NEW CHALLENGE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = Tertiary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("The 10k Sprint Week", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Push your limits this week. Complete five 10k sessions to unlock the 'Endurance Elite' badge.",
                        fontSize = 14.sp,
                        color = OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OnSurface)
                    ) {
                        Text("Join Challenge", color = SurfaceContainerLowest)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ════════════════════════════════════════════════════════════════
// 5. PROFILE
// ════════════════════════════════════════════════════════════════

@Composable
fun PantallaProfile(navController: NavHostController, userName: String?) {

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("MY POSTS", "STATS", "LIKED")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, Modifier.size(20.dp), tint = OnSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kinetic", fontSize = 22.sp, fontWeight = FontWeight.Black, color = OnSurface)
                }
                Row {
                    IconButton(onClick = { }) { Icon(Icons.Default.Search, "Search", tint = OnSurface) }
                    IconButton(onClick = { navController.navigate(Screens.SettingsScreen.name) }) {
                        Icon(Icons.Default.Settings, "Settings", tint = OnSurface)
                    }
                }
            }
        }

        // Profile header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainerLow)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar with green ring
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .drawBehind {
                                drawCircle(
                                    color = Primary,
                                    radius = size.minDimension / 2,
                                    style = Stroke(width = 6f)
                                )
                            }
                    )
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, Modifier.size(50.dp), tint = OnSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    userName ?: "Alex Rivera",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                Text(
                    "@${(userName ?: "alex").lowercase()}_kinetic",
                    fontSize = 14.sp,
                    color = OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Elite marathon runner & plant-based nutrition coach. Helping athletes unlock 110% through data-driven performance. \uD83C\uDF3F⚡",
                    fontSize = 14.sp,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = ButtonDefaults.ContentPadding,
                    modifier = Modifier.height(44.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(GreenGradientBrush, RoundedCornerShape(14.dp))
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, null, Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit Profile", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Stats chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip("POSTS", Modifier.weight(1f))
                StatChip("2.8k FOLLOWERS", Modifier.weight(1f))
                StatChip("492 FOLLOWING", Modifier.weight(1f))
            }
        }

        // Momentum Tracking
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Momentum Tracking", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                            Text(
                                "WEEKLY ACTIVITY TREND",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                color = OnSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("+12%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Primary)
                            Text("VS LAST WEEK", fontSize = 10.sp, color = OnSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    WeeklyBarChart()
                }
            }
        }

        // Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = OnSurface,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Primary
                        )
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    )
                }
            }
        }

        // Photo grid
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(5) {
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            null,
                            Modifier.size(32.dp),
                            tint = OnSurfaceVariant.copy(alpha = 0.3f)
                        )
                    }
                }
                items(1) {
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainerLow),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, null, Modifier.size(28.dp), tint = OnSurfaceVariant.copy(alpha = 0.4f))
                            Text(
                                "NEW ENTRY",
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                                color = OnSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// 6. SETTINGS
// ════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSettings(
    navController: NavHostController,
    userName: String?,
    onLogout: () -> Unit
) {
    var pushNotifications by remember { mutableStateOf(true) }
    var healthSync by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text("Settings", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            actions = {
                IconButton(onClick = { }) { Icon(Icons.Default.Search, "Search") }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
        )

        // User card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            elevation = CardDefaults.cardElevation(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, Modifier.size(32.dp), tint = OnSurfaceVariant)
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 2.dp, y = 2.dp)
                            .clip(CircleShape)
                            .background(Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Edit, null, Modifier.size(12.dp), tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(userName ?: "Alex Rivera", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                    Text("alex.fit@kinetic.app", fontSize = 14.sp, color = OnSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ACCOUNT SETTINGS
        SectionTitle("ACCOUNT SETTINGS")
        SettingsRow(Icons.Default.Person, "Account", "Manage your profile, email, and password") { }
        SettingsRow(Icons.Default.Lock, "Privacy", "Control who sees your activity and data") { }

        Spacer(modifier = Modifier.height(24.dp))

        // PREFERENCES
        SectionTitle("PREFERENCES")
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
            elevation = CardDefaults.cardElevation(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column {
                SettingsToggleRow(
                    icon = Icons.Default.Notifications,
                    title = "Push Notifications",
                    subtitle = "Daily reminders and social alerts",
                    checked = pushNotifications,
                    onCheckedChange = { pushNotifications = it }
                )
                SettingsToggleRow(
                    iconPainter = R.drawable.ic_tracking,
                    title = "Health Data Sync",
                    subtitle = "Auto-sync with Apple Health/Google Fit",
                    checked = healthSync,
                    onCheckedChange = { healthSync = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        SettingsRow(Icons.Default.Settings, "Data & Storage", "Cache management and data exports") { }
        SettingsRow(Icons.Default.Email, "Help & Support", "FAQs, contact us, and legal") { }

        Spacer(modifier = Modifier.height(32.dp))

        // Logout
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, OnSurface.copy(alpha = 0.1f))
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, null, Modifier.size(18.dp), tint = Tertiary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout from Kinetic", color = Tertiary, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "VERSION 4.2.0-ALPHA • KINETIC LABS",
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            color = OnSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ════════════════════════════════════════════════════════════════
// 7. POST DETAIL
// ════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPostDetail(navController: NavHostController) {

    var commentText by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(Surface)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            TopAppBar(
                title = { Text("FitSocial", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Primary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Default.Share, "Share") }
                    IconButton(onClick = { }) { Icon(Icons.Default.MoreVert, "More") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )

            // Author + Follow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, Modifier.size(24.dp), tint = OnSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Elena Rodriguez", fontWeight = FontWeight.Bold, color = OnSurface)
                        Text("Pro Athlete • 2h ago", fontSize = 13.sp, color = OnSurfaceVariant)
                    }
                }
                OutlinedButton(
                    onClick = { },
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Primary)
                ) {
                    Text("Follow", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Workout image with stats overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(320.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PrimaryContainer.copy(alpha = 0.15f))
            ) {
                Icon(
                    Icons.Default.Person,
                    null,
                    Modifier
                        .size(80.dp)
                        .align(Alignment.Center),
                    tint = Primary.copy(alpha = 0.2f)
                )
                // "MORNING + WORK" label
                Text(
                    "MORNING + WORK",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 2.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 60.dp)
                )
                // Stats overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(OnSurface.copy(alpha = 0.6f))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OverlayStat("8.42", "MILES")
                    OverlayStat("52:14", "TIME")
                    OverlayStat("6:12", "PACE")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Engagement
            Row(
                modifier = Modifier.padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, null, Modifier.size(20.dp), tint = Tertiary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("1.2k", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, null, Modifier.size(18.dp), tint = OnSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("84", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚡", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("42", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title + description + hashtags
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("Morning Coastal Intervals", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Nothing beats the salt air at 6 AM. Pushed the tempo on the final 3 miles. Feeling incredibly strong as marathon prep officially kicks into high gear. The new shoes are definitely making a difference in energy return! \uD83C\uDFC3\uD83D\uDCA8",
                    fontSize = 15.sp,
                    color = OnSurface,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "#MarathonTraining  #MorningRun\n#CoastalRun  #FitLife",
                    fontSize = 14.sp,
                    color = Primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Heart Rate + Calories cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("AVERAGE HEART RATE", "158", "BPM", "❤\uFE0F", Modifier.weight(1f))
                StatCard("CALORIES BURNED", "842", "KCAL", "\uD83D\uDD25", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Comments
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Comments", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                        Text("  (84)", fontSize = 14.sp, color = OnSurfaceVariant)
                    }
                    Text("Recent ▾", fontSize = 13.sp, color = Primary, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(16.dp))

                CommentItem("Marcus Chen", "Insane pace for intervals! What shoes are those? I'm looking for a new pair for my long runs.", "12m ago", 12)
                Spacer(modifier = Modifier.height(16.dp))
                CommentItem("Sarah Miller", "The coastal path is gorgeous this time of year. Great work on that pace!", "45m ago", 3)
                Spacer(modifier = Modifier.height(8.dp))

                // Author reply (indented)
                Row(modifier = Modifier.padding(start = 40.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainer.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, Modifier.size(16.dp), tint = Primary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Elena Rodriguez", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "AUTHOR",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                modifier = Modifier
                                    .background(PrimaryContainer.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text("Thanks Sarah! It was truly magic out there today.", fontSize = 13.sp, color = Primary.copy(alpha = 0.85f))
                        Text("30m ago • Reply", fontSize = 11.sp, color = OnSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // Bottom comment input
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(SurfaceContainerLowest)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, Modifier.size(18.dp), tint = OnSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = { Text("Add a comment...", color = OnSurfaceVariant, fontSize = 14.sp) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = SurfaceContainerLow,
                    focusedContainerColor = SurfaceContainerLow,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Primary
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Primary)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// 8. WORKOUT SUMMARY
// ════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaWorkoutSummary(navController: NavHostController) {

    Box(modifier = Modifier.fillMaxSize().background(Surface)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            TopAppBar(
                title = { Text("Workout Summary", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Default.Share, "Share") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {

                // Session tag
                Text(
                    "CARDIO SESSION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = Primary,
                    modifier = Modifier
                        .background(PrimaryContainer.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Morning Run", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                Text("Logged on June 12, 2024 at 7:15 AM", fontSize = 14.sp, color = OnSurfaceVariant)

                Spacer(modifier = Modifier.height(24.dp))

                // Total energy
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            "TOTAL ENERGY BURNED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = OnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("482", fontSize = 56.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                            Text(
                                " KCAL",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = OnSurfaceVariant,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Duration + Pace
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                        elevation = CardDefaults.cardElevation(0.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("DURATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = OnSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("45:00", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                                Text(" MINS", fontSize = 12.sp, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                            }
                        }
                    }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                        elevation = CardDefaults.cardElevation(0.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("AVERAGE PACE", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = OnSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("8'12\"", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                                Text(" /MI", fontSize = 12.sp, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Map placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(PrimaryContainer.copy(alpha = 0.1f), SurfaceContainerLow)
                            )
                        ),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Central Park Loop", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("\uD83D\uDCCD", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New York, NY", fontSize = 13.sp, color = OnSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Intensity Zones header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Intensity Zones", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                    TextButton(onClick = { }) {
                        Text("View Details", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                IntensityZoneRow("Peak", "08:24", "165 - 180 BPM", "18.6% OF WORKOUT", Tertiary, 0.186f)
                Spacer(modifier = Modifier.height(16.dp))
                IntensityZoneRow("Cardio", "26:15", "140 - 164 BPM", "58.3% OF WORKOUT", Primary, 0.583f)
                Spacer(modifier = Modifier.height(16.dp))
                IntensityZoneRow("Fat Burn", "10:21", "115 - 139 BPM", "23.1% OF WORKOUT", Secondary, 0.231f)

                Spacer(modifier = Modifier.height(24.dp))

                // Daily Goal Reached
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryContainer.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Daily Goal\nReached!",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                lineHeight = 26.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "This run completed your cardio goal for the week. You're 12% ahead of your schedule.",
                                fontSize = 14.sp,
                                color = OnSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("\uD83C\uDFAF", fontSize = 24.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { },
            containerColor = Primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 32.dp, bottom = 16.dp)
        ) {
            Icon(Icons.Default.Add, "New")
        }
    }
}

// ════════════════════════════════════════════════════════════════
// SHARED COMPONENTS
// ════════════════════════════════════════════════════════════════

@Composable
private fun GreenGradientButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GreenGradientBrush, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun DividerWithText(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f).height(1.dp).background(OutlineVariant.copy(alpha = 0.3f)))
        Text("  $text  ", fontSize = 13.sp, color = OnSurfaceVariant)
        Box(modifier = Modifier.weight(1f).height(1.dp).background(OutlineVariant.copy(alpha = 0.3f)))
    }
}

@Composable
private fun SocialButtonsRow() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(
            onClick = { },
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f)),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceContainerLowest)
        ) {
            Icon(painter = painterResource(R.drawable.ic_google), null, Modifier.size(18.dp), tint = Color.Unspecified)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Google", color = OnSurface, fontWeight = FontWeight.Medium)
        }
        OutlinedButton(
            onClick = { },
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f)),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceContainerLowest)
        ) {
            Icon(painter = painterResource(R.drawable.ic_facebook), null, Modifier.size(18.dp), tint = Color.Unspecified)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Facebook", color = OnSurface, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun AuthField(
    label: String, value: String, onValueChange: (String) -> Unit,
    placeholder: String, icon: ImageVector, isPassword: Boolean = false
) {
    Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = OnSurface)
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = OnSurfaceVariant) },
        leadingIcon = { Icon(icon, null, tint = OnSurfaceVariant) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = SurfaceContainerLowest,
            focusedContainerColor = SurfaceContainerLowest,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Primary
        ),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun MomentumRing(progress: Float, size: Int) {
    val strokeWidth = if (size > 100) 14f else 10f
    Box(
        modifier = Modifier
            .size(size.dp)
            .drawBehind {
                drawArc(
                    color = PrimaryContainer.copy(alpha = 0.2f),
                    startAngle = -90f, sweepAngle = 360f, useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(this.size.width - strokeWidth, this.size.height - strokeWidth)
                )
                drawArc(
                    color = Primary,
                    startAngle = -90f, sweepAngle = 360f * progress, useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(this.size.width - strokeWidth, this.size.height - strokeWidth)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (size <= 100) {
            Text("⚡", fontSize = 24.sp)
        }
    }
}

@Composable
private fun FeedPost(
    author: String, time: String, tag: String,
    likes: Int, comments: Int, description: String,
    navController: NavHostController
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clickable { navController.navigate(Screens.PostDetailScreen.name) }
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(SurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Person, null, Modifier.size(22.dp), tint = OnSurfaceVariant) }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(author, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurface)
                        Text("$time • $tag", fontSize = 12.sp, color = OnSurfaceVariant)
                    }
                }
                Icon(Icons.Default.MoreVert, null, tint = OnSurfaceVariant)
            }

            // Image placeholder
            Box(
                modifier = Modifier.fillMaxWidth().height(220.dp).background(SurfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Person, null, Modifier.size(48.dp), tint = OnSurfaceVariant.copy(alpha = 0.2f)) }

            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, null, Modifier.size(20.dp), tint = Primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$likes", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, null, Modifier.size(18.dp), tint = OnSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$comments", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                Icon(Icons.Default.Share, null, Modifier.size(18.dp), tint = OnSurfaceVariant)
            }

            Text(
                "$author $description",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 14.sp, color = OnSurface, lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = OnSurfaceVariant)
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OnSurface)
    }
}

@Composable
private fun MacroRow(label: String, current: Int, goal: Int, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = color, modifier = Modifier.width(60.dp))
        LinearProgressIndicator(
            progress = { (current.toFloat() / goal).coerceIn(0f, 1f) },
            modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color, trackColor = color.copy(alpha = 0.15f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text("$current", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurface)
        Text(" / ${goal}g", fontSize = 13.sp, color = OnSurfaceVariant)
    }
}

@Composable
private fun RecentLogItem(emoji: String, title: String, subtitle: String, calories: String, unit: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(SurfaceContainerLow),
                    contentAlignment = Alignment.Center
                ) { Text(emoji, fontSize = 18.sp) }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(subtitle, fontSize = 12.sp, color = OnSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(calories, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnSurface)
                Text(unit, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = OnSurfaceVariant)
            }
        }
    }
}

@Composable
private fun StatChip(text: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Text(
            text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = OnSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
        )
    }
}

@Composable
private fun WeeklyBarChart() {
    val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    val values = listOf(0.3f, 0.4f, 0.5f, 1f, 0.6f, 0.35f, 0.45f)
    Row(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEachIndexed { index, day ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height((80 * values[index]).dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(if (index == 3) Primary else SurfaceContainerHigh)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    day, fontSize = 9.sp,
                    fontWeight = if (index == 3) FontWeight.Bold else FontWeight.Normal,
                    color = if (index == 3) Primary else OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Primary,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(SurfaceContainerLow),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, Modifier.size(20.dp), tint = OnSurfaceVariant) }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = OnSurface)
                Text(subtitle, fontSize = 13.sp, color = OnSurfaceVariant)
            }
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(18.dp), tint = OnSurfaceVariant)
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector? = null,
    iconPainter: Int? = null,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            if (icon != null) {
                Icon(icon, null, Modifier.size(24.dp), tint = OnSurfaceVariant)
            } else if (iconPainter != null) {
                Icon(painterResource(iconPainter), null, Modifier.size(24.dp), tint = OnSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = OnSurface)
                Text(subtitle, fontSize = 13.sp, color = OnSurfaceVariant)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = Primary)
        )
    }
}

@Composable
private fun OverlayStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color.White.copy(alpha = 0.7f))
    }
}

@Composable
private fun StatCard(label: String, value: String, unit: String, emoji: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = OnSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                Text(" $unit", fontSize = 12.sp, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}

@Composable
private fun CommentItem(author: String, text: String, time: String, likes: Int) {
    Row {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(SurfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.Person, null, Modifier.size(18.dp), tint = OnSurfaceVariant) }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(author, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnSurface)
            Text(text, fontSize = 13.sp, color = OnSurface, lineHeight = 18.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(time, fontSize = 11.sp, color = OnSurfaceVariant)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Reply", fontSize = 11.sp, color = OnSurfaceVariant, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.Favorite, null, Modifier.size(12.dp), tint = Tertiary)
                Spacer(modifier = Modifier.width(2.dp))
                Text("$likes", fontSize = 11.sp, color = OnSurfaceVariant)
            }
        }
    }
}

@Composable
private fun IntensityZoneRow(
    label: String, duration: String, bpmRange: String,
    percentage: String, color: Color, progress: Float
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = color)
            Text(duration, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurface)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color, trackColor = color.copy(alpha = 0.12f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(bpmRange, fontSize = 11.sp, color = OnSurfaceVariant)
            Text(percentage, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = OnSurfaceVariant)
        }
    }
}