package com.example.fitfusion

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fitfusion.data.repository.UserProfileStore
import com.example.fitfusion.data.workout.ActiveWorkoutManager
import com.example.fitfusion.ui.components.ActiveWorkoutBanner
import com.example.fitfusion.ui.screens.PantallaLogin
import com.example.fitfusion.ui.screens.Screens
import com.example.fitfusion.viewmodel.AuthViewModel
import com.example.fitfusion.ui.screens.*
import com.google.firebase.FirebaseApp

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        UserProfileStore.ensureInitialized(applicationContext)
        ActiveWorkoutManager.init(applicationContext)
        setContent {
            val navController = rememberNavController()

            val authViewModel: AuthViewModel = viewModel()
            val initialLoggedUser = remember(authViewModel) { authViewModel.getSignedInDisplayName() }
            val startDestination = remember(authViewModel) {
                if (authViewModel.isUserSignedIn()) Screens.HomeScreen.name else Screens.LoginScreen.name
            }
            var loggedUser by remember { mutableStateOf(initialLoggedUser) }

            val notificationLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { /* result ignored — service still works without notification */ }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color(0xFFFBF8FE),
            ) { innerPadding ->
                Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    ActiveWorkoutBanner(navController = navController)
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        composable(Screens.LoginScreen.name) {
                            PantallaLogin(
                                onLoginSuccess = { username ->
                                    loggedUser = username
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

                        composable(Screens.SettingsScreen.name) {
                            PantallaSettings(
                                navController = navController,
                                userName = loggedUser,
                                onLogout = {
                                    authViewModel.signOut()
                                    loggedUser = null
                                    navController.navigate(Screens.LoginScreen.name) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Screens.PostDetailScreen.name) {
                            PantallaPostDetail(navController = navController)
                        }

                        composable(
                            route = "${Screens.PostDetailScreen.name}/{postId}",
                            arguments = listOf(navArgument("postId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            PantallaPostDetail(
                                navController = navController,
                                postId = backStackEntry.arguments?.getString("postId")
                            )
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

                        composable(Screens.ActiveWorkoutScreen.name) {
                            PantallaActiveWorkout(navController)
                        }

                        composable(Screens.WorkoutFinishScreen.name) {
                            PantallaWorkoutFinish(navController)
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
