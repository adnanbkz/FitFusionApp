package com.example.fitfusion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fitfusion.ui.screens.PantallaLogin
import com.example.fitfusion.ui.screens.PantallaProfile
import com.example.fitfusion.ui.screens.Screens
import com.example.fitfusion.viewmodel.AuthViewModel
import com.example.fitfusion.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var selectedItem by remember { mutableIntStateOf(0) }
            val items = listOf("HOME", "TRACK", "PROFILE")
            val icons: List<Any> = listOf(
                Icons.Outlined.Home,
                painterResource(R.drawable.ic_tracking),
                Icons.Outlined.Person
            )
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // Shared AuthViewModel for Login + SignUp
            val authViewModel: AuthViewModel = viewModel()
            val initialLoggedUser = remember(authViewModel) { authViewModel.getSignedInDisplayName() }
            val startDestination = remember(authViewModel) {
                if (authViewModel.isUserSignedIn()) Screens.HomeScreen.name else Screens.LoginScreen.name
            }
            var loggedUser by remember { mutableStateOf(initialLoggedUser) }

            val isMainScreen = currentRoute == Screens.HomeScreen.name ||
                    currentRoute == Screens.TrackingScreen.name ||
                    currentRoute == Screens.ProfileScreen.name

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color(0xFFFBF8FE),
                bottomBar = {
                    if (isMainScreen) {
                        NavigationBar(
                            containerColor = Color(0xFFFBF8FE).copy(alpha = 0.85f),
                            tonalElevation = 0.dp
                        ) {
                            items.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    selected = selectedItem == index,
                                    onClick = {
                                        selectedItem = index
                                        val route = when (index) {
                                            0 -> Screens.HomeScreen.name
                                            1 -> Screens.TrackingScreen.name
                                            2 -> Screens.ProfileScreen.name
                                            else -> Screens.HomeScreen.name
                                        }
                                        navController.navigate(route) {
                                            popUpTo(Screens.HomeScreen.name) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        when (val ic = icons[index]) {
                                            is ImageVector -> Icon(ic, null, Modifier.size(24.dp))
                                            is Painter -> Icon(ic, null, Modifier.size(24.dp))
                                        }
                                    },
                                    label = {
                                        Text(
                                            item,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 1.sp
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF006E0A),
                                        selectedTextColor = Color(0xFF006E0A),
                                        unselectedIconColor = Color(0xFF757575),
                                        unselectedTextColor = Color(0xFF757575),
                                        indicatorColor = Color(0xFFBDD6FF).copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable(Screens.LoginScreen.name) {
                            PantallaLogin(
                                onLoginSuccess = { username ->
                                    loggedUser = username
                                    selectedItem = 0
                                    navController.navigate(Screens.HomeScreen.name) {
                                        popUpTo(Screens.LoginScreen.name) { inclusive = true }
                                    }
                                },
                                onNavigateToSignUp = {
                                    navController.navigate(Screens.SignUpScreen.name)
                                },
                                onSkip = {
                                    loggedUser = null
                                    navController.navigate(Screens.HomeScreen.name) {
                                        popUpTo(Screens.LoginScreen.name) { inclusive = true }
                                    }
                                },
                                authViewModel = authViewModel
                            )
                        }

                        composable(Screens.SignUpScreen.name) {
                            PantallaSignUp(
                                onSignUpSuccess = { username ->
                                    loggedUser = username
                                    selectedItem = 0
                                    navController.navigate(Screens.HomeScreen.name) {
                                        popUpTo(Screens.SignUpScreen.name) { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = {
                                    navController.popBackStack()
                                },
                                authViewModel = authViewModel
                            )
                        }

                        composable(Screens.HomeScreen.name) {
                            PantallaHome(
                                navController = navController,
                                userName = loggedUser
                            )
                        }

                        composable(Screens.TrackingScreen.name) {
                            PantallaTracking(navController = navController)
                        }

                        composable(Screens.ProfileScreen.name) {
                            PantallaProfile(
                                navController = navController,
                                userName = loggedUser
                            )
                        }

                        composable(Screens.SettingsScreen.name) {
                            PantallaSettings(
                                navController = navController,
                                userName = loggedUser,
                                onLogout = {
                                    authViewModel.signOut()
                                    loggedUser = null
                                    selectedItem = 0
                                    navController.navigate(Screens.LoginScreen.name) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Screens.PostDetailScreen.name) {
                            PantallaPostDetail(navController)
                        }

                        composable(Screens.WorkoutSummaryScreen.name) {
                            PantallaWorkoutSummary(navController)
                        }

                        composable(
                            route = "${Screens.AddFoodScreen.name}/{mealSlot}",
                            arguments = listOf(navArgument("mealSlot") { type = NavType.StringType })
                        ) { backStackEntry ->
                            PantallaAddFood(
                                navController = navController,
                                initialMealSlot = backStackEntry.arguments?.getString("mealSlot")
                            )
                        }

                        composable(Screens.WeeklyLogScreen.name) {
                            PantallaWeeklyLog(navController)
                        }

                        composable(
                            route = "${Screens.AddWorkoutScreen.name}?logMode={logMode}",
                            arguments = listOf(navArgument("logMode") { type = NavType.BoolType; defaultValue = false })
                        ) { backStackEntry ->
                            PantallaAddWorkout(
                                navController = navController,
                                isLogMode = backStackEntry.arguments?.getBoolean("logMode") ?: false
                            )
                        }

                        composable(Screens.AccountScreen.name) {
                            PantallaAccount(navController)
                        }

                        composable(Screens.PrivacyScreen.name) {
                            PantallaPrivacy(navController)
                        }

                        composable(Screens.DataStorageScreen.name) {
                            PantallaDataStorage(navController)
                        }

                        composable(Screens.HelpSupportScreen.name) {
                            PantallaHelpSupport(navController)
                        }

                        composable(Screens.CreateExerciseScreen.name) {
                            PantallaCreateExercise(navController)
                        }

                        composable(Screens.CreateRecipeScreen.name) {
                            PantallaCreateRecipe(navController)
                        }

                        composable(
                            route = "${Screens.ExerciseDetailScreen.name}/{documentId}",
                            arguments = listOf(navArgument("documentId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            PantallaExerciseDetail(
                                navController,
                                backStackEntry.arguments?.getString("documentId") ?: ""
                            )
                        }
                    }
                }
            }
        }
    }
}
