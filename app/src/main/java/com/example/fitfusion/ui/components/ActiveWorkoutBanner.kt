package com.example.fitfusion.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.fitfusion.data.workout.ActiveWorkoutManager
import com.example.fitfusion.ui.screens.Screens
import com.example.fitfusion.ui.theme.Primary
import com.example.fitfusion.viewmodel.formatElapsed

private val HIDDEN_ROUTES = setOf(
    Screens.LoginScreen.name,
    Screens.SignUpScreen.name,
    Screens.SettingsScreen.name,
    Screens.ActiveWorkoutScreen.name,
    Screens.WorkoutFinishScreen.name,
)

@Composable
fun ActiveWorkoutBanner(navController: NavHostController) {
    val session by ActiveWorkoutManager.session.collectAsState()
    val elapsed by ActiveWorkoutManager.elapsedSeconds.collectAsState()
    val backStack by navController.currentBackStackEntryAsState()
    val current = session ?: return
    val baseRoute = backStack?.destination?.route?.substringBefore('/')?.substringBefore('?')
    if (baseRoute in HIDDEN_ROUTES) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Primary)
            .clickable {
                navController.navigate(Screens.ActiveWorkoutScreen.name)
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                if (current.isPaused) Icons.Default.Pause else Icons.Outlined.Timer,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
            Text(
                if (current.isPaused) "ENTRENO EN PAUSA" else "ENTRENAMIENTO ACTIVO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = Color.White,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                formatElapsed(elapsed),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(Modifier.width(2.dp))
            Text(
                "Volver",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    }
}
