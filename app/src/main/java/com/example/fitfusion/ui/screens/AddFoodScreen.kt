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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.fitfusion.data.models.Food
import com.example.fitfusion.data.models.MealSlotType
import com.example.fitfusion.data.models.Recipe
import com.example.fitfusion.data.models.Serving
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.AddFoodViewModel
import com.example.fitfusion.viewmodel.FoodTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAddFood(
    navController: NavHostController,
    initialMealSlot: String? = null,
    addFoodViewModel: AddFoodViewModel = viewModel(),
) {
    val state by addFoodViewModel.uiState.collectAsState()
    val resolvedSlot = remember(initialMealSlot) {
        initialMealSlot?.let { runCatching { MealSlotType.valueOf(it) }.getOrNull() }
            ?: MealSlotType.fromCurrentHour()
    }

    LaunchedEffect(resolvedSlot) {
        addFoodViewModel.setActiveMealSlot(resolvedSlot)
    }

    LaunchedEffect(state.activeTab) {
        if (state.activeTab == FoodTab.RECETAS) addFoodViewModel.loadRecipes()
    }

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Añadir a ${state.activeMealSlot.label}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = OnSurface)
                    }
                },
                actions = {
                    if (state.activeTab == FoodTab.ALIMENTOS) {
                        IconButton(onClick = { }) {
                            Icon(
                                painterResource(R.drawable.ic_tracking),
                                contentDescription = "Escanear código",
                                tint = Primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
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
        ) {
            // ── Tab bar ───────────────────────────────────────────────────────
            TabRow(
                selectedTabIndex = state.activeTab.ordinal,
                containerColor   = Surface,
                contentColor     = Primary,
                indicator        = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[state.activeTab.ordinal]),
                        color    = Primary
                    )
                }
            ) {
                Tab(
                    selected = state.activeTab == FoodTab.ALIMENTOS,
                    onClick  = { addFoodViewModel.setActiveTab(FoodTab.ALIMENTOS) },
                    text     = {
                        Text(
                            "ALIMENTOS",
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = if (state.activeTab == FoodTab.ALIMENTOS) Primary else OnSurfaceVariant
                        )
                    }
                )
                Tab(
                    selected = state.activeTab == FoodTab.RECETAS,
                    onClick  = { addFoodViewModel.setActiveTab(FoodTab.RECETAS) },
                    text     = {
                        Text(
                            "RECETAS",
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = if (state.activeTab == FoodTab.RECETAS) Primary else OnSurfaceVariant
                        )
                    }
                )
            }

            when (state.activeTab) {
                FoodTab.ALIMENTOS -> AlimentosContent(
                    state      = state,
                    viewModel  = addFoodViewModel,
                )
                FoodTab.RECETAS -> RecetasContent(
                    state      = state,
                    viewModel  = addFoodViewModel,
                    navController = navController,
                )
            }
        }
    }

    // ── BottomSheet: alimento individual ─────────────────────────────────────
    if (state.selectedFood != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = addFoodViewModel::dismissSheet,
            sheetState       = sheetState,
            containerColor   = SurfaceContainerLowest,
            dragHandle       = { BottomSheetDefaults.DragHandle(color = OutlineVariant) }
        ) {
            FoodDetailSheet(
                food            = state.selectedFood!!,
                selectedServing = state.selectedServing ?: state.selectedFood!!.servingOptions.first(),
                quantity        = state.quantity,
                sheetMealSlot   = state.sheetMealSlot,
                activeMealSlots = state.mealConfig.activeSlots,
                onSelectServing = addFoodViewModel::selectServing,
                onIncrement     = addFoodViewModel::incrementQuantity,
                onDecrement     = addFoodViewModel::decrementQuantity,
                onSelectSlot    = addFoodViewModel::selectSheetMealSlot,
                onConfirm       = {
                    addFoodViewModel.confirmAdd()
                    navController.popBackStack()
                }
            )
        }
    }

    // ── BottomSheet: receta ───────────────────────────────────────────────────
    if (state.selectedRecipe != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = addFoodViewModel::dismissRecipeSheet,
            sheetState       = sheetState,
            containerColor   = SurfaceContainerLowest,
            dragHandle       = { BottomSheetDefaults.DragHandle(color = OutlineVariant) }
        ) {
            RecipeDetailSheet(
                recipe          = state.selectedRecipe!!,
                sheetMealSlot   = state.recipeSheetMealSlot,
                activeMealSlots = state.mealConfig.activeSlots,
                onSelectSlot    = addFoodViewModel::selectRecipeSheetMealSlot,
                onConfirm       = {
                    addFoodViewModel.confirmAddRecipe()
                    navController.popBackStack()
                }
            )
        }
    }
}

// ── Pestaña ALIMENTOS ─────────────────────────────────────────────────────────

@Composable
private fun AlimentosContent(
    state: com.example.fitfusion.viewmodel.AddFoodUiState,
    viewModel: AddFoodViewModel,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Buscador
        item {
            OutlinedTextField(
                value         = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder   = { Text("Busca alimentos, marcas...", color = OnSurfaceVariant, fontSize = 14.sp) },
                leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null, tint = OnSurfaceVariant) },
                trailingIcon  = {
                    if (state.searchQuery.isNotBlank()) {
                        IconButton(onClick = viewModel::clearSearch) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = OnSurfaceVariant)
                        }
                    }
                },
                shape      = RoundedCornerShape(14.dp),
                singleLine = true,
                colors     = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = SurfaceContainerLowest,
                    focusedContainerColor   = SurfaceContainerLowest,
                    unfocusedBorderColor    = Color.Transparent,
                    focusedBorderColor      = Primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (state.searchQuery.isBlank()) {
            // Favoritos
            item {
                SectionLabel("FAVORITOS", modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp))
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(state.favorites) { food ->
                        FavoriteChip(
                            food    = food,
                            onClick = { viewModel.openSheet(food, state.activeMealSlot) }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Frecuentes en esta comida
            item {
                SectionLabel(
                    "FRECUENTES EN ${state.activeMealSlot.label.uppercase()}",
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
            }
            items(state.recents.take(3)) { food ->
                FoodRow(
                    food       = food,
                    onTap      = { viewModel.openSheet(food, state.activeMealSlot) },
                    onQuickAdd = { viewModel.openSheet(food, state.activeMealSlot) }
                )
            }

            // Recientes
            item {
                Spacer(Modifier.height(8.dp))
                SectionLabel("RECIENTES", modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
            }
            items(state.recents) { food ->
                FoodRow(
                    food       = food,
                    onTap      = { viewModel.openSheet(food, state.activeMealSlot) },
                    onQuickAdd = { viewModel.openSheet(food, state.activeMealSlot) }
                )
            }
        } else {
            // Resultados de búsqueda
            if (state.searchResults.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Sin resultados", fontWeight = FontWeight.Bold, color = OnSurface)
                            Text(
                                "Prueba con otro nombre o marca",
                                fontSize = 13.sp, color = OnSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                item {
                    SectionLabel(
                        "${state.searchResults.size} RESULTADOS",
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                    )
                }
                items(state.searchResults) { food ->
                    FoodRow(
                        food       = food,
                        onTap      = { viewModel.openSheet(food, state.activeMealSlot) },
                        onQuickAdd = { viewModel.openSheet(food, state.activeMealSlot) }
                    )
                }
            }
        }
    }
}

// ── Pestaña RECETAS ───────────────────────────────────────────────────────────

@Composable
private fun RecetasContent(
    state: com.example.fitfusion.viewmodel.AddFoodUiState,
    viewModel: AddFoodViewModel,
    navController: NavHostController,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Botón "Nueva receta"
        item {
            OutlinedButton(
                onClick = { navController.navigate(Screens.CreateRecipeScreen.name) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape  = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Primary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Nueva receta", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }

        when {
            state.isLoadingRecipes -> {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary, modifier = Modifier.size(32.dp))
                    }
                }
            }
            state.recipes.isEmpty() -> {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🍽️", fontSize = 36.sp)
                        Text("Sin recetas", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = OnSurface)
                        Text(
                            "Crea tu primera receta con ingredientes\nde la base de datos",
                            fontSize = 13.sp, color = OnSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
            else -> {
                item {
                    SectionLabel(
                        "${state.recipes.size} RECETA${if (state.recipes.size > 1) "S" else ""}",
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                }
                items(state.recipes) { recipe ->
                    RecipeCard(
                        recipe  = recipe,
                        onTap   = { viewModel.openRecipeSheet(recipe, state.activeMealSlot) }
                    )
                }
            }
        }
    }
}

// ── Componentes ───────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        fontSize      = 11.sp,
        fontWeight    = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        color         = Primary,
        modifier      = modifier
    )
}

@Composable
private fun FavoriteChip(food: Food, onClick: () -> Unit) {
    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = Modifier.width(90.dp).clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(food.emoji, fontSize = 24.sp)
            Text(
                food.name,
                fontSize   = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color      = OnSurface,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun FoodRow(food: Food, onTap: () -> Unit, onQuickAdd: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) { Text(food.emoji, fontSize = 22.sp) }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                food.name,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 15.sp,
                color      = OnSurface,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                val serving = food.servingOptions.first()
                val kcal    = (food.kcalPer100g * serving.grams / 100f).toInt()
                Text("$kcal kcal", fontSize = 13.sp, color = OnSurface, fontWeight = FontWeight.Medium)
                Text("·", fontSize = 13.sp, color = OnSurfaceVariant)
                Text(serving.label, fontSize = 13.sp, color = OnSurfaceVariant)
                food.brand?.let {
                    Text("·", fontSize = 13.sp, color = OnSurfaceVariant)
                    Text(it, fontSize = 13.sp, color = OnSurfaceVariant)
                }
            }
        }

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Primary)
                .clickable(onClick = onQuickAdd),
            contentAlignment = Alignment.Center
        ) {
            Text("+", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RecipeCard(recipe: Recipe, onTap: () -> Unit) {
    Card(
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onTap)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) { Text(recipe.emoji, fontSize = 24.sp) }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    recipe.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp,
                    color      = OnSurface,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(
                    "${recipe.ingredients.size} ingrediente${if (recipe.ingredients.size != 1) "s" else ""} · ${recipe.totalKcal} kcal",
                    fontSize = 12.sp,
                    color    = OnSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("${recipe.totalProtein}g", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Primary)
                Text("PROT", fontSize = 9.sp, color = OnSurfaceVariant, letterSpacing = 0.5.sp)
            }
        }
    }
}

// ── FoodDetailSheet (internal para reutilizar en TrackingScreen) ──────────────

@Composable
internal fun FoodDetailSheet(
    food: Food,
    selectedServing: Serving,
    quantity: Int,
    sheetMealSlot: MealSlotType,
    activeMealSlots: List<MealSlotType>,
    onSelectServing: (Serving) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onSelectSlot: (MealSlotType) -> Unit,
    onConfirm: () -> Unit,
    confirmLabel: String = "Añadir al registro",
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

        // Selector de porción
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("PORCIÓN", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Primary)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(food.servingOptions) { serving ->
                    val selected = serving.label == selectedServing.label
                    FilterChip(
                        selected = selected,
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

        // Selector de cantidad
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
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color      = OnSurface,
                    modifier   = Modifier.widthIn(min = 32.dp),
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

        // Resumen nutricional
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

        // Selector de comida
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("AÑADIR A", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Primary)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(activeMealSlots) { slot ->
                    val selected = slot == sheetMealSlot
                    FilterChip(
                        selected = selected,
                        onClick  = { onSelectSlot(slot) },
                        label    = { Text("${slot.emoji} ${slot.label}", fontSize = 13.sp) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryContainer.copy(alpha = 0.15f),
                            selectedLabelColor     = Primary,
                            containerColor         = SurfaceContainerHigh
                        )
                    )
                }
            }
        }

        // Botón confirmar
        Button(
            onClick  = onConfirm,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text(confirmLabel, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun NutritionStat(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = OnSurfaceVariant)
    }
}

@Composable
internal fun NutritionStatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(OutlineVariant.copy(alpha = 0.3f))
    )
}

// ── RecipeDetailSheet ─────────────────────────────────────────────────────────

@Composable
private fun RecipeDetailSheet(
    recipe: Recipe,
    sheetMealSlot: MealSlotType,
    activeMealSlots: List<MealSlotType>,
    onSelectSlot: (MealSlotType) -> Unit,
    onConfirm: () -> Unit,
) {
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
            ) { Text(recipe.emoji, fontSize = 28.sp) }
            Column(modifier = Modifier.weight(1f)) {
                Text(recipe.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OnSurface)
                Text(
                    "${recipe.ingredients.size} ingrediente${if (recipe.ingredients.size != 1) "s" else ""}",
                    fontSize = 13.sp, color = OnSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${recipe.totalKcal}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                Text("KCAL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant, letterSpacing = 1.sp)
            }
        }

        HorizontalDivider(color = OutlineVariant.copy(alpha = 0.3f))

        // Lista de ingredientes
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("INGREDIENTES", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Primary)
            recipe.ingredients.forEach { ingredient ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) { Text(ingredient.emoji, fontSize = 16.sp) }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            ingredient.name,
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = OnSurface,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${ingredient.servingLabel} × ${ingredient.quantity}",
                            fontSize = 11.sp, color = OnSurfaceVariant
                        )
                    }
                    Text("${ingredient.kcal} kcal", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = OnSurface)
                }
            }
        }

        // Totales nutricionales
        Card(
            shape  = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NutritionStat("${recipe.totalKcal}", "KCAL", OnSurface)
                NutritionStatDivider()
                NutritionStat("${recipe.totalProtein}g", "PROT", Primary)
                NutritionStatDivider()
                NutritionStat("${recipe.totalCarbs}g", "CARB", Secondary)
                NutritionStatDivider()
                NutritionStat("${recipe.totalFat}g", "GRASA", Tertiary)
            }
        }

        // Selector de comida
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("AÑADIR A", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Primary)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(activeMealSlots) { slot ->
                    val selected = slot == sheetMealSlot
                    FilterChip(
                        selected = selected,
                        onClick  = { onSelectSlot(slot) },
                        label    = { Text("${slot.emoji} ${slot.label}", fontSize = 13.sp) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryContainer.copy(alpha = 0.15f),
                            selectedLabelColor     = Primary,
                            containerColor         = SurfaceContainerHigh
                        )
                    )
                }
            }
        }

        // Botón confirmar
        Button(
            onClick  = onConfirm,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("Añadir receta al registro", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
