package com.example.fitfusion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.fitfusion.R
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.AddFoodViewModel
import com.example.fitfusion.viewmodel.FoodItem
import com.example.fitfusion.viewmodel.MealType
import com.example.fitfusion.viewmodel.SuggestedFoodItem

@Composable
fun PantallaAddFood(
    navController: NavHostController,
    addFoodViewModel: AddFoodViewModel = viewModel()
) {
    val state by addFoodViewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Top Bar
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = OnSurface
                    )
                }
                Text(
                    "Añadir comida",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                IconButton(onClick = { }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_tracking),
                        contentDescription = "Escanear",
                        tint = Primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Search Bar
        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = addFoodViewModel::onSearchQueryChange,
                placeholder = {
                    Text(
                        "Busca alimentos, marcas, recetas...",
                        color = OnSurfaceVariant,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, null, tint = OnSurfaceVariant)
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = SurfaceContainerLowest,
                    focusedContainerColor = SurfaceContainerLowest,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Primary
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }

        // Quick Add
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Acceso rápido",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MealType.entries.forEach { meal ->
                    QuickAddChip(
                        meal = meal,
                        isSelected = state.selectedMeal == meal,
                        onClick = { addFoodViewModel.onMealSelected(meal) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Recently Logged Header
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Registrado recientemente",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                TextButton(onClick = { }) {
                    Text(
                        "Ver todo",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary
                    )
                }
            }
        }

        // Recently Logged Items
        items(state.recentlyLogged) { food ->
            RecentFoodRow(
                food = food,
                isAdded = food.id in state.addedItemIds,
                onAdd = { addFoodViewModel.addFood(food.id) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Suggested For You Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Sugerido para ti",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Featured suggestion
        item {
            val featured = state.suggested.firstOrNull { it.isFeatured }
            if (featured != null) {
                FeaturedSuggestionCard(
                    item = featured,
                    isAdded = (featured.id + 1000) in state.addedItemIds,
                    onAdd = { addFoodViewModel.addSuggested(featured.id) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Other suggestions
        item {
            val others = state.suggested.filter { !it.isFeatured }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(others) { item ->
                    SmallSuggestionCard(
                        item = item,
                        isAdded = (item.id + 1000) in state.addedItemIds,
                        onAdd = { addFoodViewModel.addSuggested(item.id) }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun QuickAddChip(
    meal: MealType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (emoji, label) = when (meal) {
        MealType.BREAKFAST -> "☀️" to "DESAYUNO"
        MealType.LUNCH -> "🍴" to "ALMUERZO"
        MealType.DINNER -> "🌙" to "CENA"
    }
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Primary else SurfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = modifier
            .clickable(onClick = onClick)
            .then(
                if (!isSelected) Modifier.border(1.dp, OutlineVariant.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                else Modifier
            )
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 20.sp)
            Text(
                label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = if (isSelected) Color.White else OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecentFoodRow(
    food: FoodItem,
    isAdded: Boolean,
    onAdd: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food image placeholder
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Text(food.emoji, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            // Food info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    food.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "${food.calories}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = OnSurface
                    )
                    Text("kcal", fontSize = 12.sp, color = OnSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MacroChip("P", food.protein, Primary)
                    MacroChip("F", food.fats, Tertiary)
                    MacroChip("C", food.carbs, Secondary)
                }
            }
            // Add button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isAdded) Primary.copy(alpha = 0.2f) else Primary)
                    .clickable(enabled = !isAdded, onClick = onAdd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Añadir",
                    tint = if (isAdded) Primary else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun MacroChip(label: String, value: Int, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            "$label ${value}g",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun FeaturedSuggestionCard(
    item: SuggestedFoodItem,
    isAdded: Boolean,
    onAdd: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(PrimaryContainer.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(item.emoji, fontSize = 56.sp)
            }
            // Tag chip
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(8.dp))
                    .background(OnSurface.copy(alpha = 0.7f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "PARA DESAYUNO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            }
        }
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = OnSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${item.calories} kcal",
                        fontSize = 13.sp,
                        color = OnSurfaceVariant
                    )
                    Text(
                        "  |  ",
                        fontSize = 13.sp,
                        color = OutlineVariant
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Primary.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            item.tag,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isAdded) Primary.copy(alpha = 0.2f) else Primary)
                    .clickable(enabled = !isAdded, onClick = onAdd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Añadir",
                    tint = if (isAdded) Primary else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SmallSuggestionCard(
    item: SuggestedFoodItem,
    isAdded: Boolean,
    onAdd: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.width(140.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(SurfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Text(item.emoji, fontSize = 36.sp)
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    item.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "${item.calories} kcal",
                    fontSize = 12.sp,
                    color = OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (isAdded) Primary.copy(alpha = 0.2f) else Primary)
                        .clickable(enabled = !isAdded, onClick = onAdd)
                        .align(Alignment.End),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Añadir",
                        tint = if (isAdded) Primary else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}