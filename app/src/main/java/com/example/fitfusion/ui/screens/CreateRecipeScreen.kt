package com.example.fitfusion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.fitfusion.data.models.Food
import com.example.fitfusion.data.models.RecipeIngredient
import com.example.fitfusion.data.models.Serving
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.CreateRecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCreateRecipe(
    navController: NavHostController,
    viewModel: CreateRecipeViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = {
                    Text("Nueva receta", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = OnSurface)
                    }
                },
                actions = {
                    TextButton(
                        onClick  = { viewModel.saveRecipe { navController.popBackStack() } },
                        enabled  = state.isValid && !state.isSaving
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Primary
                            )
                        } else {
                            Text(
                                "Guardar",
                                color      = if (state.isValid) Primary else OnSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 15.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Nombre + Emoji ────────────────────────────────────────────────
            item {
                Card(
                    shape     = RoundedCornerShape(20.dp),
                    colors    = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(0.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "INFORMACIÓN",
                            fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp, color = Primary
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value         = state.emoji,
                                onValueChange = viewModel::onEmojiChange,
                                modifier      = Modifier.width(72.dp),
                                textStyle     = LocalTextStyle.current.copy(
                                    fontSize  = 24.sp,
                                    textAlign = TextAlign.Center
                                ),
                                singleLine = true,
                                shape      = RoundedCornerShape(12.dp),
                                colors     = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = SurfaceContainerLow,
                                    focusedContainerColor   = SurfaceContainerLow,
                                    unfocusedBorderColor    = Color.Transparent,
                                    focusedBorderColor      = Primary,
                                )
                            )
                            OutlinedTextField(
                                value         = state.name,
                                onValueChange = viewModel::onNameChange,
                                placeholder   = {
                                    Text("Nombre de la receta", color = OnSurfaceVariant, fontSize = 15.sp)
                                },
                                singleLine = true,
                                modifier   = Modifier.weight(1f),
                                shape      = RoundedCornerShape(12.dp),
                                colors     = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = SurfaceContainerLow,
                                    focusedContainerColor   = SurfaceContainerLow,
                                    unfocusedBorderColor    = Color.Transparent,
                                    focusedBorderColor      = Primary,
                                )
                            )
                        }
                    }
                }
            }

            // ── Totales nutricionales (solo si hay ingredientes) ──────────────
            if (state.ingredients.isNotEmpty()) {
                item {
                    Card(
                        shape     = RoundedCornerShape(16.dp),
                        colors    = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                        elevation = CardDefaults.cardElevation(0.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            NutritionStat("${state.totalKcal}", "KCAL", OnSurface)
                            NutritionStatDivider()
                            NutritionStat("${state.totalProtein}g", "PROT", Primary)
                            NutritionStatDivider()
                            NutritionStat("${state.totalCarbs}g", "CARB", Secondary)
                            NutritionStatDivider()
                            NutritionStat("${state.totalFat}g", "GRASA", Tertiary)
                        }
                    }
                }
            }

            // ── Búsqueda de ingredientes ──────────────────────────────────────
            item {
                Text(
                    "INGREDIENTES",
                    fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp, color = Primary
                )
            }
            item {
                OutlinedTextField(
                    value         = state.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    placeholder   = {
                        Text("Busca ingredientes...", color = OnSurfaceVariant, fontSize = 14.sp)
                    },
                    leadingIcon  = { Icon(Icons.Default.Search, contentDescription = null, tint = OnSurfaceVariant) },
                    trailingIcon = {
                        if (state.searchQuery.isNotBlank()) {
                            IconButton(onClick = viewModel::clearSearch) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = OnSurfaceVariant)
                            }
                        }
                    },
                    singleLine = true,
                    shape      = RoundedCornerShape(14.dp),
                    colors     = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = SurfaceContainerLowest,
                        focusedContainerColor   = SurfaceContainerLowest,
                        unfocusedBorderColor    = Color.Transparent,
                        focusedBorderColor      = Primary,
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Resultados de búsqueda
            if (state.searchQuery.isNotBlank()) {
                when {
                    state.isLoadingResults -> {
                        item {
                            Box(
                                Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Primary, modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                    state.searchResults.isEmpty() -> {
                        item {
                            Box(
                                Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Sin resultados", color = OnSurfaceVariant, fontSize = 14.sp)
                            }
                        }
                    }
                    else -> {
                        items(state.searchResults) { food ->
                            IngredientSearchRow(
                                food  = food,
                                onTap = { viewModel.openIngredientSheet(food) }
                            )
                        }
                    }
                }
            }

            // ── Ingredientes añadidos ─────────────────────────────────────────
            if (state.ingredients.isNotEmpty()) {
                item {
                    Text(
                        "${state.ingredients.size} INGREDIENTE${if (state.ingredients.size != 1) "S" else ""} AÑADIDO${if (state.ingredients.size != 1) "S" else ""}",
                        fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp, color = OnSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                items(state.ingredients, key = { it.ingredientId + it.servingLabel }) { ingredient ->
                    AddedIngredientRow(
                        ingredient = ingredient,
                        onRemove   = { viewModel.removeIngredient(ingredient.ingredientId) }
                    )
                }
            }

            // Error
            if (state.saveError != null) {
                item {
                    Text(
                        state.saveError!!,
                        color    = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }

    // ── Sheet de añadir ingrediente ───────────────────────────────────────────
    if (state.selectedFood != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = viewModel::dismissIngredientSheet,
            sheetState       = sheetState,
            containerColor   = SurfaceContainerLowest,
            dragHandle       = { BottomSheetDefaults.DragHandle(color = OutlineVariant) }
        ) {
            IngredientAddSheet(
                food            = state.selectedFood!!,
                selectedServing = state.selectedServing ?: state.selectedFood!!.servingOptions.first(),
                quantity        = state.ingredientQuantity,
                onSelectServing = viewModel::selectServing,
                onIncrement     = viewModel::incrementQuantity,
                onDecrement     = viewModel::decrementQuantity,
                onConfirm       = viewModel::confirmAddIngredient,
            )
        }
    }
}

// ── Componentes privados ──────────────────────────────────────────────────────

@Composable
private fun IngredientSearchRow(food: Food, onTap: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) { Text(food.emoji, fontSize = 20.sp) }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                food.name,
                fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                color = OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            val serving = food.servingOptions.first()
            val kcal    = (food.kcalPer100g * serving.grams / 100f).toInt()
            Text("$kcal kcal · ${serving.label}", fontSize = 12.sp, color = OnSurfaceVariant)
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Primary),
            contentAlignment = Alignment.Center
        ) { Text("+", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun AddedIngredientRow(ingredient: RecipeIngredient, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) { Text(ingredient.emoji, fontSize = 18.sp) }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                ingredient.name,
                fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                color = OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(
                "${ingredient.servingLabel} × ${ingredient.quantity} · ${ingredient.kcal} kcal",
                fontSize = 12.sp, color = OnSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(SurfaceContainerHigh)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Text("×", fontSize = 16.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun IngredientAddSheet(
    food: Food,
    selectedServing: Serving,
    quantity: Int,
    onSelectServing: (Serving) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onConfirm: () -> Unit,
) {
    val kcal    = (food.kcalPer100g    * selectedServing.grams * quantity / 100f).toInt()
    val protein = (food.proteinPer100g * selectedServing.grams * quantity / 100f).toInt()
    val carbs   = (food.carbsPer100g   * selectedServing.grams * quantity / 100f).toInt()
    val fat     = (food.fatsPer100g    * selectedServing.grams * quantity / 100f).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Cabecera
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) { Text(food.emoji, fontSize = 28.sp) }
            Column {
                Text(food.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OnSurface)
                food.brand?.let { Text(it, fontSize = 13.sp, color = OnSurfaceVariant) }
            }
        }

        HorizontalDivider(color = OutlineVariant.copy(alpha = 0.3f))

        // Porción
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("PORCIÓN", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Primary)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(food.servingOptions) { serving ->
                    FilterChip(
                        selected = serving.label == selectedServing.label,
                        onClick  = { onSelectServing(serving) },
                        label    = { Text(serving.label, fontSize = 13.sp) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryContainer.copy(alpha = 0.15f),
                            selectedLabelColor     = Primary,
                            containerColor         = SurfaceContainerHigh
                        )
                    )
                }
            }
        }

        // Cantidad
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("CANTIDAD", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Primary)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHigh)
                        .clickable(onClick = onDecrement),
                    contentAlignment = Alignment.Center
                ) { Text("−", fontSize = 22.sp, color = OnSurface, fontWeight = FontWeight.Bold) }

                Text(
                    "$quantity",
                    fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OnSurface,
                    modifier = Modifier.widthIn(min = 32.dp)
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Primary)
                        .clickable(onClick = onIncrement),
                    contentAlignment = Alignment.Center
                ) { Text("+", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }

        // Preview nutricional
        Card(
            shape  = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NutritionStat("$kcal", "KCAL", OnSurface)
                NutritionStatDivider()
                NutritionStat("${protein}g", "PROT", Primary)
                NutritionStatDivider()
                NutritionStat("${carbs}g", "CARB", Secondary)
                NutritionStatDivider()
                NutritionStat("${fat}g", "GRASA", Tertiary)
            }
        }

        // Botón confirmar
        Button(
            onClick  = onConfirm,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("Añadir ingrediente", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
