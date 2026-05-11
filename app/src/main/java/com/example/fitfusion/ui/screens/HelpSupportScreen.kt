package com.example.fitfusion.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
        "¿Cómo funciona la sincronización con Health Connect?",
        "En Ajustes > Preferencias activa 'Sincronización de datos de salud'. Asegúrate de tener Health Connect instalado y de haber concedido los permisos necesarios."
    ),
    FaqItem(
        "¿Puedo usar la app sin conexión a internet?",
        "Sí. Puedes registrar entrenamientos y comidas sin conexión. Los datos se sincronizarán automáticamente cuando vuelvas a conectarte."
    ),
    FaqItem(
        "¿Cómo cambio mi objetivo calórico diario?",
        "Ve a tu Perfil > Ajustes > Cuenta y edita tus datos. El sistema calculará automáticamente las calorías recomendadas."
    ),
    FaqItem(
        "¿Cómo elimino una publicación del feed?",
        "Pulsa los tres puntos en la esquina de la publicación y selecciona 'Eliminar'. Esta acción es permanente."
    ),
    FaqItem(
        "¿Están seguros mis datos?",
        "Sí. Todos los datos se almacenan con cifrado en reposo y en tránsito. Puedes exportar o eliminar tu cuenta en cualquier momento desde Ajustes > Cuenta."
    )
)

private val tosText = """
TERMINOS DE SERVICIO DE FITFUSION

Ultima actualizacion: 1 de mayo de 2026

1. ACEPTACION DE LOS TERMINOS

Al descargar, instalar o utilizar la aplicacion FitFusion, aceptas estos Terminos de Servicio. Si no estas de acuerdo, no utilices la aplicacion.

2. DESCRIPCION DEL SERVICIO

FitFusion es una aplicacion de seguimiento de actividad fisica y nutricion que permite a los usuarios registrar entrenamientos, comidas, crear recetas, rutinas y compartir publicaciones en un feed social.

3. CUENTA DE USUARIO

Para acceder a ciertas funciones, debes crear una cuenta proporcionando un correo electronico valido. Eres responsable de mantener la confidencialidad de tus credenciales y de toda la actividad que ocurra bajo tu cuenta.

4. CONDUCTA DEL USUARIO

Te comprometes a no utilizar el servicio para publicar contenido ilegal, ofensivo, difamatorio, obsceno o que infrinja derechos de terceros. FitFusion se reserva el derecho de eliminar contenido y suspender cuentas que violen estos terminos.

5. PROPIEDAD INTELECTUAL

El codigo fuente de FitFusion se distribuye bajo la licencia MIT. Los nombres, logotipos y marcas asociados son propiedad de sus respectivos titulares. El contenido generado por los usuarios pertenece a sus respectivos autores.

6. EXENCION DE RESPONSABILIDAD

FitFusion se proporciona "tal cual", sin garantia de ningun tipo. No nos hacemos responsables de la exactitud de los datos de salud, recomendaciones nutricionales ni del uso que hagas de la informacion proporcionada. Consulta siempre a un profesional medico antes de comenzar cualquier programa de ejercicio o dieta.

7. LIMITACION DE RESPONSABILIDAD

En ningun caso FitFusion o sus desarrolladores seran responsables por danos directos, indirectos, incidentales o consecuentes derivados del uso o la imposibilidad de uso de la aplicacion.

8. MODIFICACIONES

Nos reservamos el derecho de modificar estos terminos en cualquier momento. Los cambios entraran en vigor inmediatamente despues de su publicacion en la aplicacion.

9. CONTACTO

Para cualquier duda sobre estos terminos, contacta a traves de la seccion de soporte de la aplicacion.

10. LEGISLACION APLICABLE

Estos terminos se rigen por las leyes aplicables en la jurisdiccion donde se distribuye la aplicacion, sin perjuicio de los derechos que te correspondan como consumidor.
""".trimIndent()

private val privacyText = """
POLITICA DE PRIVACIDAD DE FITFUSION

Ultima actualizacion: 1 de mayo de 2026

1. INFORMACION QUE RECOPILAMOS

FitFusion recopila la siguiente informacion:
- Datos de cuenta: correo electronico, nombre de usuario y foto de perfil.
- Datos de actividad: entrenamientos registrados, duracion, ejercicios realizados, calorias estimadas.
- Datos de nutricion: alimentos registrados, recetas creadas, ingestas diarias.
- Datos de salud: pasos y frecuencia cardiaca a traves de Health Connect (solo si el usuario otorga permisos explicitos).
- Datos sociales: publicaciones, comentarios e interacciones en el feed.

2. USO DE LA INFORMACION

Utilizamos tus datos para:
- Proporcionar y mantener el servicio de FitFusion.
- Personalizar tu experiencia y recomendaciones.
- Calcular estadisticas, rachas y resumenes de actividad.
- Mostrar tus publicaciones en el feed social segun tus preferencias de privacidad.
- Mejorar la aplicacion y desarrollar nuevas funciones.

3. COMPARTIR INFORMACION

No vendemos tus datos personales a terceros. Tus datos de entrenamiento y nutricion pueden ser visibles para otros usuarios de FitFusion unicamente si decides publicarlos en el feed social. Los ajustes de privacidad te permiten controlar quien ve tu actividad.

4. ALMACENAMIENTO Y SEGURIDAD

Tus datos se almacenan en servidores de Google Firebase con cifrado en reposo y en transito mediante TLS. Implementamos medidas de seguridad para proteger tu informacion, aunque ningun sistema es completamente seguro.

5. RETENCION DE DATOS

Conservamos tus datos mientras mantengas una cuenta activa. Puedes solicitar la eliminacion de tus datos en cualquier momento desde Ajustes > Cuenta > Eliminar cuenta.

6. TUS DERECHOS

Tienes derecho a acceder, rectificar, suprimir y exportar tus datos personales. Para ejercer estos derechos, contacta con nosotros a traves de la seccion de soporte.

7. HEALTH CONNECT

La integracion con Health Connect requiere permisos explicitos que puedes revocar en cualquier momento desde los ajustes de tu dispositivo. Los datos leidos de Health Connect se procesan localmente y solo se almacenan en tu cuenta de FitFusion si la sincronizacion esta activada.

8. CAMBIOS EN LA POLITICA

Te notificaremos cualquier cambio significativo en esta politica a traves de la aplicacion o por correo electronico.

9. CONTACTO

Para cuestiones de privacidad, escribe a traves de la seccion de soporte de la aplicacion.
""".trimIndent()

private val licensesText = """
LICENCIAS DE CODIGO ABIERTO

FitFusion se distribuye bajo la licencia MIT y utiliza las siguientes bibliotecas de codigo abierto:

AndroidX Libraries (Apache 2.0)
  - androidx.navigation:navigation-compose
  - androidx.health.connect:connect-client
  - androidx.camera:camera-*
  - androidx.compose.material3:material3

Firebase SDK (Apache 2.0)
  - com.google.firebase:firebase-auth
  - com.google.firebase:firebase-firestore
  - com.google.firebase:firebase-storage
  - com.google.firebase:firebase-functions
  - com.google.firebase:firebase-crashlytics

Coil (Apache 2.0)
  - io.coil-kt.coil3:coil-compose
  - io.coil-kt.coil3:coil-network-okhttp

Google ML Kit (Apache 2.0)
  - com.google.mlkit:barcode-scanning

Coroutines (Apache 2.0)
  - org.jetbrains.kotlinx:kotlinx-coroutines-android

Licencia MIT de FitFusion:

Copyright (c) 2024-2026 FitFusion

Se concede permiso, de forma gratuita, a cualquier persona que obtenga una copia de este software y los archivos de documentacion asociados, para utilizar el software sin restricciones, incluyendo, sin limitacion, los derechos de usar, copiar, modificar, fusionar, publicar, distribuir, sublicenciar y/o vender copias del software, y permitir a las personas a quienes se les proporcione el software que lo hagan, sujeto a las siguientes condiciones:

El aviso de copyright anterior y este aviso de permiso se incluiran en todas las copias o partes sustanciales del software.

EL SOFTWARE SE PROPORCIONA "TAL CUAL", SIN GARANTIA DE NINGUN TIPO, EXPRESA O IMPLICITA, INCLUYENDO PERO NO LIMITADO A GARANTIAS DE COMERCIABILIDAD, IDONEIDAD PARA UN PROPOSITO PARTICULAR Y NO INFRACCION.
""".trimIndent()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHelpSupport(
    navController: NavHostController
) {
    val context = LocalContext.current
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
                        onClick = { openSupportEmail(context, "Consulta FitFusion") },
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
                        subtitle = "support@fitfusion.app",
                        onClick = { openSupportEmail(context, "Soporte FitFusion") }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = OutlineVariant.copy(alpha = 0.3f)
                    )
                    HelpLinkRow(
                        icon = Icons.Default.Star,
                        title = "Valorar la aplicación",
                        subtitle = "Comparte tu opinion en la tienda",
                        onClick = { }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = OutlineVariant.copy(alpha = 0.3f)
                    )
                    HelpLinkRow(
                        icon = Icons.Default.Notifications,
                        title = "Novedades y actualizaciones",
                        subtitle = "Consulta las últimas mejoras de FitFusion",
                        onClick = { }
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
                    ExpandableLegalRow(
                        icon = Icons.Default.Info,
                        title = "Términos de servicio",
                        subtitle = "Condiciones de uso de la plataforma",
                        content = tosText,
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = OutlineVariant.copy(alpha = 0.3f)
                    )
                    ExpandableLegalRow(
                        icon = Icons.Default.Lock,
                        title = "Política de privacidad",
                        subtitle = "Como tratamos tus datos personales",
                        content = privacyText,
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = OutlineVariant.copy(alpha = 0.3f)
                    )
                    ExpandableLegalRow(
                        icon = Icons.AutoMirrored.Filled.List,
                        title = "Licencias de código abierto",
                        subtitle = "Bibliotecas de terceros utilizadas",
                        content = licensesText,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            HelpSectionHeader("CONTACTAR")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "¿Tienes preguntas o sugerencias?",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Escribenos a adnaneeuu@gmail.com y te responderemos lo antes posible.",
                        fontSize = 13.sp,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { openSupportEmail(context, "Contacto FitFusion") },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Icon(Icons.Default.Email, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Enviar correo", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "VERSION 4.2.0-ALPHA | FITFUSION",
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

private fun openSupportEmail(context: android.content.Context, subject: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:adnaneeuu@gmail.com")
        putExtra(Intent.EXTRA_SUBJECT, subject)
    }
    runCatching { context.startActivity(intent) }
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
private fun HelpLinkRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
    }
}

@Composable
private fun ExpandableLegalRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    content: String,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
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
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                null,
                Modifier.size(20.dp),
                tint = OnSurfaceVariant
            )
        }
        AnimatedVisibility(visible = expanded) {
            Text(
                content,
                fontSize = 12.sp,
                color = OnSurfaceVariant,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    }
}
