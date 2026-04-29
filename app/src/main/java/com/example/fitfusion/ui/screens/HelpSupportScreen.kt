package com.example.fitfusion.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.fitfusion.ui.theme.*

private data class FaqItem(val question: String, val answer: String)

private val faqItems = listOf(
    FaqItem(
        "¿Cómo registro un entrenamiento?",
        "Ve a la pestaña Seguimiento y pulsa 'Registrar entrenamiento'. Elige el tipo de actividad, introduce la duración y los detalles, luego guarda."
    ),
    FaqItem(
        "¿Cómo funciona la sincronización con Google Fit?",
        "En Ajustes › Preferencias activa 'Sincronización de datos de salud'. Asegúrate de tener Google Fit instalado y de haber concedido los permisos necesarios."
    ),
    FaqItem(
        "¿Puedo usar la app sin conexión a internet?",
        "Sí. Puedes registrar entrenamientos y comidas sin conexión. Los datos se sincronizarán automáticamente cuando vuelvas a conectarte."
    ),
    FaqItem(
        "¿Cómo cambio mi objetivo calórico diario?",
        "Ve a tu Perfil › Ajustes › Cuenta y edita los datos de tu objetivo. El sistema calculará automáticamente las calorías recomendadas."
    ),
    FaqItem(
        "¿Cómo elimino una publicación del feed?",
        "Pulsa los tres puntos (⋮) en la esquina de la publicación y selecciona 'Eliminar'. Esta acción es permanente."
    ),
    FaqItem(
        "¿Están seguros mis datos?",
        "Sí. Todos los datos se almacenan con cifrado en reposo y en tránsito. Puedes exportar o eliminar tu cuenta en cualquier momento desde Ajustes › Cuenta."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHelpSupport(
    navController: NavHostController
) {
    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = {
                    Text("Ayuda y soporte", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                },
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
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Primary.copy(alpha = 0.08f)
                ),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "¿Necesitas ayuda?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = OnSurface
                        )
                        Text(
                            "Nuestro equipo responde en menos de 24 h",
                            fontSize = 13.sp,
                            color = OnSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Contactar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            HelpSectionHeader("PREGUNTAS FRECUENTES")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    faqItems.forEachIndexed { index, item ->
                        FaqRow(item)
                        if (index < faqItems.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                color = OutlineVariant.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            HelpSectionHeader("SOPORTE")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Column {
                    HelpLinkRow(
                        icon = Icons.Default.Email,
                        title = "Enviar un correo de soporte",
                        subtitle = "support@fitfusion.app"
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = OutlineVariant.copy(alpha = 0.3f)
                    )
                    HelpLinkRow(
                        icon = Icons.Default.Star,
                        title = "Valorar la aplicación",
                        subtitle = "Comparte tu opinión en la tienda"
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = OutlineVariant.copy(alpha = 0.3f)
                    )
                    HelpLinkRow(
                        icon = Icons.Default.Notifications,
                        title = "Novedades y actualizaciones",
                        subtitle = "Consulta las últimas mejoras de FitFusion"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            HelpSectionHeader("LEGAL")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Column {
                    HelpLinkRow(
                        icon = Icons.Default.Info,
                        title = "Términos de servicio",
                        subtitle = "Condiciones de uso de la plataforma"
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = OutlineVariant.copy(alpha = 0.3f)
                    )
                    HelpLinkRow(
                        icon = Icons.Default.Lock,
                        title = "Política de privacidad",
                        subtitle = "Cómo tratamos tus datos personales"
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = OutlineVariant.copy(alpha = 0.3f)
                    )
                    HelpLinkRow(
                        icon = Icons.Default.List,
                        title = "Licencias de código abierto",
                        subtitle = "Bibliotecas de terceros utilizadas"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "VERSION 4.2.0-ALPHA • FITFUSION",
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = OnSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HelpSectionHeader(text: String) {
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
private fun FaqRow(item: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                item.question,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = OnSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        AnimatedVisibility(visible = expanded) {
            Text(
                item.answer,
                fontSize = 13.sp,
                color = OnSurfaceVariant,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun HelpLinkRow(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
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
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            null,
            Modifier.size(16.dp),
            tint = OnSurfaceVariant
        )
    }
}