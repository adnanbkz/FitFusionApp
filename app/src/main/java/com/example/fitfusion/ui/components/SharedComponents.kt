package com.example.fitfusion.ui.components
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.example.fitfusion.ui.screens.Screens
import com.example.fitfusion.ui.theme.*

@Composable
fun GreenGradientButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(GreenGradientBrush, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun DividerWithText(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f).height(1.dp).background(OutlineVariant.copy(alpha = 0.3f)))
        Text("  $text  ", fontSize = 13.sp, color = OnSurfaceVariant)
        Box(modifier = Modifier.weight(1f).height(1.dp).background(OutlineVariant.copy(alpha = 0.3f)))
    }
}

@Composable
fun SocialButtonsRow() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(
            onClick = { },
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f)),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceContainerLowest)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(R.drawable.ic_google), null, Modifier.size(18.dp), tint = Color.Unspecified)
                Text("Google", color = OnSurface, fontWeight = FontWeight.Medium)
            }
        }
        OutlinedButton(
            onClick = { },
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f)),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceContainerLowest)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(R.drawable.ic_facebook), null, Modifier.size(18.dp), tint = Color.Unspecified)
                Text("Facebook", color = OnSurface, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun AuthField(
    label: String, value: String, onValueChange: (String) -> Unit,
    placeholder: String, icon: ImageVector, isPassword: Boolean = false
) {
    Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = OnSurface)
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
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    )
}

@Composable
fun MomentumRing(progress: Float, size: Int) {
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
fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = OnSurfaceVariant)
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OnSurface)
    }
}

@Composable
fun MacroRow(label: String, current: Int, goal: Int, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = color, modifier = Modifier.width(60.dp))
        LinearProgressIndicator(
            progress = { (current.toFloat() / goal).coerceIn(0f, 1f) },
            modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color, trackColor = color.copy(alpha = 0.15f)
        )
        Text("$current", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurface, modifier = Modifier.padding(start = 12.dp))
        Text(" / ${goal}g", fontSize = 13.sp, color = OnSurfaceVariant)
    }
}

@Composable
fun RecentLogItem(emoji: String, title: String, subtitle: String, calories: String, unit: String) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(SurfaceContainerLow),
                    contentAlignment = Alignment.Center
                ) { Text(emoji, fontSize = 18.sp) }
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
fun FeedPost(
    author: String, time: String, tag: String,
    likes: Int, comments: Int, description: String,
    navController: NavHostController
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).clickable { navController.navigate(Screens.PostDetailScreen.name) }
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(SurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Person, null, Modifier.size(22.dp), tint = OnSurfaceVariant) }
                    Column {
                        Text(author, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurface)
                        Text("$time • $tag", fontSize = 12.sp, color = OnSurfaceVariant)
                    }
                }
                Icon(Icons.Default.MoreVert, null, tint = OnSurfaceVariant)
            }
            Box(
                modifier = Modifier.fillMaxWidth().height(220.dp).background(SurfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Person, null, Modifier.size(48.dp), tint = OnSurfaceVariant.copy(alpha = 0.2f)) }
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Favorite, null, Modifier.size(20.dp), tint = Primary)
                    Text("$likes", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Email, null, Modifier.size(18.dp), tint = OnSurfaceVariant)
                    Text("$comments", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                Icon(Icons.Default.Share, null, Modifier.size(18.dp), tint = OnSurfaceVariant)
            }
            Text(
                "$author $description",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                fontSize = 14.sp, color = OnSurface, lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun StatChip(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            Text(
                label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp, color = OnSurfaceVariant
            )
        }
    }
}

@Composable
fun WeeklyBarChart() {
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
                Text(
                    day,
                    fontSize = 9.sp,
                    fontWeight = if (index == 3) FontWeight.Bold else FontWeight.Normal,
                    color = if (index == 3) Primary else OnSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Primary,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(SurfaceContainerLow),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, Modifier.size(20.dp), tint = OnSurfaceVariant) }
            Column {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = OnSurface)
                Text(subtitle, fontSize = 13.sp, color = OnSurfaceVariant)
            }
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(18.dp), tint = OnSurfaceVariant)
    }
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector? = null,
    iconPainter: Int? = null,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (icon != null) {
                Icon(icon, null, Modifier.size(24.dp), tint = OnSurfaceVariant)
            } else if (iconPainter != null) {
                Icon(painterResource(iconPainter), null, Modifier.size(24.dp), tint = OnSurfaceVariant)
            }
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
fun OverlayStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color.White.copy(alpha = 0.7f))
    }
}

@Composable
fun StatCard(label: String, value: String, unit: String, emoji: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 20.sp)
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = OnSurfaceVariant, textAlign = TextAlign.Center)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                Text(" $unit", fontSize = 12.sp, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}

@Composable
fun CommentItem(author: String, text: String, time: String, likes: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(SurfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.Person, null, Modifier.size(18.dp), tint = OnSurfaceVariant) }
        Column {
            Text(author, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnSurface)
            Text(text, fontSize = 13.sp, color = OnSurface, lineHeight = 18.sp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(time, fontSize = 11.sp, color = OnSurfaceVariant)
                Text("Reply", fontSize = 11.sp, color = OnSurfaceVariant, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, null, Modifier.size(12.dp), tint = Tertiary)
                    Text("$likes", fontSize = 11.sp, color = OnSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun IntensityZoneRow(
    label: String, duration: String, bpmRange: String,
    percentage: String, color: Color, progress: Float
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = color)
            Text(duration, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurface)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color, trackColor = color.copy(alpha = 0.12f)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(bpmRange, fontSize = 11.sp, color = OnSurfaceVariant)
            Text(percentage, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = OnSurfaceVariant)
        }
    }
}