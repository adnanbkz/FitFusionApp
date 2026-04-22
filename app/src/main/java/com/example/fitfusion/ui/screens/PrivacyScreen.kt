package com.example.fitfusion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.PrivacyViewModel
import com.example.fitfusion.viewmodel.VisibilityOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrivacy(
    navController: NavHostController,
    privacyViewModel: PrivacyViewModel = viewModel()
) {
    val state by privacyViewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = { Text("Privacidad", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            PrivacySectionHeader("VISIBILIDAD")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    VisibilitySelector(
                        label = "Perfil",
                        subtitle = "Quién puede ver tu perfil",
                        icon = Icons.Default.Person,
                        selected = state.profileVisibility,
                        onSelect = privacyViewModel::onProfileVisibilityChange
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = OutlineVariant.copy(alpha = 0.3f)
                    )
                    VisibilitySelector(
                        label = "Actividad",
                        subtitle = "Quién puede ver tus registros y entrenamientos",
                        icon = Icons.Default.Star,
                        selected = state.activityVisibility,
                        onSelect = privacyViewModel::onActivityVisibilityChange
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = OutlineVariant.copy(alpha = 0.3f)
                    )
                    VisibilitySelector(
                        label = "Mensajes",
                        subtitle = "Quién puede enviarte mensajes",
                        icon = Icons.Default.Email,
                        selected = state.whoCanMessage,
                        onSelect = privacyViewModel::onWhoCanMessageChange
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrivacySectionHeader("INTERACCIONES")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Column {
                    PrivacyToggleRow(
                        icon = Icons.Default.Search,
                        title = "Aparecer en búsquedas",
                        subtitle = "Otros usuarios pueden encontrarte por nombre",
                        checked = state.showInSearch,
                        onCheckedChange = privacyViewModel::onShowInSearchChange
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = OutlineVariant.copy(alpha = 0.3f)
                    )
                    PrivacyToggleRow(
                        icon = Icons.Default.AddCircle,
                        title = "Permitir etiquetado",
                        subtitle = "Otros pueden etiquetarte en publicaciones",
                        checked = state.allowTagging,
                        onCheckedChange = privacyViewModel::onAllowTaggingChange
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = OutlineVariant.copy(alpha = 0.3f)
                    )
                    PrivacyToggleRow(
                        icon = Icons.Default.LocationOn,
                        title = "Compartir ubicación",
                        subtitle = "Incluir ubicación en tus entrenamientos",
                        checked = state.shareLocation,
                        onCheckedChange = privacyViewModel::onShareLocationChange
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrivacySectionHeader("USUARIOS BLOQUEADOS")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                if (state.blockedUsers.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No tienes usuarios bloqueados",
                            fontSize = 14.sp,
                            color = OnSurfaceVariant
                        )
                    }
                } else {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        state.blockedUsers.forEachIndexed { index, username ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceContainerHigh),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person, null,
                                        Modifier.size(18.dp), tint = OnSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    username,
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = OnSurface
                                )
                                TextButton(
                                    onClick = { privacyViewModel.unblockUser(username) }
                                ) {
                                    Text(
                                        "Desbloquear",
                                        fontSize = 13.sp,
                                        color = Primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            if (index < state.blockedUsers.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                    color = OutlineVariant.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PrivacySectionHeader(text: String) {
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
private fun VisibilitySelector(
    label: String,
    subtitle: String,
    icon: ImageVector,
    selected: VisibilityOption,
    onSelect: (VisibilityOption) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(18.dp), tint = Primary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = OnSurface)
                Text(subtitle, fontSize = 12.sp, color = OnSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VisibilityOption.entries.forEach { option ->
                val isSelected = option == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Primary else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) Primary else OutlineVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelect(option) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        option.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) Color.White else OnSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceContainerLow),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, Modifier.size(18.dp), tint = OnSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = OnSurface)
            Text(subtitle, fontSize = 12.sp, color = OnSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = Primary)
        )
    }
}