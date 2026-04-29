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
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.fitfusion.R
import com.example.fitfusion.data.models.Food
import com.example.fitfusion.data.models.MealSlot
import com.example.fitfusion.data.models.Recipe
import com.example.fitfusion.data.models.Serving
import com.example.fitfusion.data.repository.FoodRepository
import java.time.LocalDate
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.AddFoodViewModel
import com.example.fitfusion.viewmodel.FoodTab
import com.example.fitfusion.viewmodel.RecipeSubTab
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAddFood(
    navController: NavHostController,
    initialMealSlot: String? = null,
    addFoodViewModel: AddFoodViewModel = viewModel(),
) {
    val state by addFoodViewModel.uiState.collectAsState()
    val resolvedSlot = remember(initialMealSlot) {
        initialMealSlot?.let { id ->
            MealSlot.predefinedById(id)
                ?: FoodRepository.getDayLog(LocalDate.now()).meals.find { it.id == id }
        } ?: MealSlot.fromCurrentHour()
    }

    LaunchedEffect(resolvedSlot) {
        addFoodViewModel.setActiveMealSlot(resolvedSlot)
    }

    LaunchedEffect(state.activeTab, state.recipeSubTab) {
        if (state.activeTab == FoodTab.RECETAS) {
            when (state.recipeSubTab) {
                RecipeSubTab.MIS_RECETAS -> addFoodViewModel.loadMyRecipes()
                RecipeSubTab.USUARIOS    -> addFoodViewModel.loadCommunityRecipes()
            }
        }
    }

    val context = LocalContext.current
    LaunchedEffect(state.recipeFeedback) {
        state.recipeFeedback?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            addFoodViewModel.clearRecipeFeedback()
        }
    }

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Añadir a ${state.activeMealSlot.name}",
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
                    state         = state,
                    viewModel     = addFoodViewModel,
                    navController = navController,
                )
            }
        }
    }

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
                activeMealSlots = state.availableSlots,
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

    if (state.selectedRecipe != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = addFoodViewModel::dismissRecipeDetail,
            sheetState       = sheetState,
            containerColor   = SurfaceContainerLowest,
            dragHandle       = { BottomSheetDefaults.DragHandle(color = OutlineVariant) }
        ) {
            RecipeDetailSheet(recipe = state.selectedRecipe!!)
        }
    }
}


@Composable
private fun AlimentosContent(
    state: com.example.fitfusion.viewmodel.AddFoodUiState,
    viewModel: AddFoodViewModel,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
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

            item {
                SectionLabel(
                    "FRECUENTES EN ${state.activeMealSlot.name.uppercase()}",
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
            if (state.isLoadingSearch) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary, modifier = Modifier.size(32.dp))
                    }
                }
            } else if (state.searchResults.isEmpty()) {
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


@Composable
private fun RecetasContent(
    state: com.example.fitfusion.viewmodel.AddFoodUiState,
    viewModel: AddFoodViewModel,
    navController: NavHostController,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            RecipeSubTabRow(
                selected = state.recipeSubTab,
                onSelect = viewModel::setRecipeSubTab,
            )

            when (state.recipeSubTab) {
                RecipeSubTab.USUARIOS    -> CommunityRecipesList(state = state, viewModel = viewModel)
                RecipeSubTab.MIS_RECETAS -> MyRecipesList(state = state, viewModel = viewModel)
            }
        }

        if (state.recipeSubTab == RecipeSubTab.MIS_RECETAS) {
            FloatingActionButton(
                onClick        = { navController.navigate(Screens.CreateRecipeScreen.name) },
                containerColor = Primary,
                contentColor   = Color.White,
                modifier       = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
            ) {
                Icon(Icons.Default.Add, "Añadir receta")
            }
        }
    }
}

@Composable
private fun RecipeSubTabRow(
    selected: RecipeSubTab,
    onSelect: (RecipeSubTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceContainerLow)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        listOf(
            Triple(RecipeSubTab.USUARIOS,    "Recetas de usuarios", Icons.Default.Public),
            Triple(RecipeSubTab.MIS_RECETAS, "Mis recetas",         Icons.Default.Restaurant),
        ).forEach { (tab, label, icon) ->
            val isSelected = selected == tab
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) SurfaceContainerLowest else Color.Transparent)
                    .clickable { onSelect(tab) }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint               = if (isSelected) Primary else OnSurfaceVariant,
                    modifier           = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    label,
                    fontSize   = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color      = if (isSelected) OnSurface else OnSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MyRecipesList(
    state: com.example.fitfusion.viewmodel.AddFoodUiState,
    viewModel: AddFoodViewModel,
) {
    LazyColumn(
        modifier       = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        when {
            state.isLoadingMyRecipes && state.myRecipes.isEmpty() -> item { LoadingBox() }
            state.myRecipes.isEmpty() -> item {
                EmptyRecipesMessage(
                    title    = "Aún no tienes recetas",
                    subtitle = "Pulsa el botón + para crear tu primera receta",
                )
            }
            else -> {
                items(state.myRecipes, key = { it.id }) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onTap  = { viewModel.openRecipeDetail(recipe) },
                        trailing = null,
                    )
                }
                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }
}

@Composable
private fun CommunityRecipesList(
    state: com.example.fitfusion.viewmodel.AddFoodUiState,
    viewModel: AddFoodViewModel,
) {
    LazyColumn(
        modifier       = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        when {
            state.isLoadingCommunityRecipes && state.communityRecipes.isEmpty() -> item { LoadingBox() }
            state.communityRecipes.isEmpty() -> item {
                EmptyRecipesMessage(
                    title    = "Sin recetas de la comunidad",
                    subtitle = "Sé el primero en publicar una",
                )
            }
            else -> {
                items(state.communityRecipes, key = { it.id }) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onTap  = { viewModel.openRecipeDetail(recipe) },
                        trailing = {
                            val isSaving = state.savingCommunityRecipeId == recipe.id
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Primary)
                                    .clickable(enabled = !isSaving) {
                                        viewModel.saveCommunityRecipeToMine(recipe)
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color       = Color.White,
                                    )
                                } else {
                                    Icon(Icons.Default.Add, "Guardar", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingBox() {
    Box(
        Modifier.fillMaxWidth().padding(vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) { CircularProgressIndicator(color = Primary, modifier = Modifier.size(32.dp)) }
}

@Composable
private fun EmptyRecipesMessage(title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = OnSurface)
        Text(
            subtitle,
            fontSize   = 13.sp,
            color      = OnSurfaceVariant,
            textAlign  = TextAlign.Center,
            lineHeight = 18.sp,
        )
    }
}


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
private fun RecipeCard(
    recipe: Recipe,
    onTap: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    Card(
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onTap),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                if (recipe.photoUrl != null) {
                    AsyncImage(
                        model             = recipe.photoUrl,
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize(),
                    )
                } else {
                    Text(recipe.emoji, fontSize = 24.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    recipe.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp,
                    color      = OnSurface,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                )
                val subtitle = buildString {
                    recipe.authorName?.let { append("por $it") }
                    recipe.cookTimeMin?.let {
                        if (isNotEmpty()) append(" · ")
                        append("$it min")
                    }
                    recipe.kcal?.let {
                        if (isNotEmpty()) append(" · ")
                        append("$it kcal")
                    }
                    if (isEmpty()) append(recipe.bestMoment ?: "Receta")
                }
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color    = OnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            trailing?.invoke()
        }
    }
}


@Composable
internal fun FoodDetailSheet(
    food: Food,
    selectedServing: Serving,
    quantity: Int,
    sheetMealSlot: MealSlot,
    activeMealSlots: List<MealSlot>,
    onSelectServing: (Serving) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onSelectSlot: (MealSlot) -> Unit,
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

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("AÑADIR A", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Primary)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(activeMealSlots) { slot ->
                    val selected = slot == sheetMealSlot
                    FilterChip(
                        selected = selected,
                        onClick  = { onSelectSlot(slot) },
                        label    = { Text(slot.name, fontSize = 13.sp) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryContainer.copy(alpha = 0.15f),
                            selectedLabelColor     = Primary,
                            containerColor         = SurfaceContainerHigh
                        )
                    )
                }
            }
        }

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


@Composable
private fun RecipeDetailSheet(recipe: Recipe) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (recipe.photoUrl != null) {
            AsyncImage(
                model              = recipe.photoUrl,
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp)),
            )
        }

        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) { Text(recipe.emoji, fontSize = 28.sp) }
            Column(modifier = Modifier.weight(1f)) {
                Text(recipe.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OnSurface)
                recipe.authorName?.let {
                    Text("por $it", fontSize = 13.sp, color = OnSurfaceVariant)
                }
            }
            recipe.kcal?.let {
                Column(horizontalAlignment = Alignment.End) {
                    Text("$it", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                    Text("KCAL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant, letterSpacing = 1.sp)
                }
            }
        }

        if (recipe.description.isNotBlank()) {
            Text(recipe.description, fontSize = 14.sp, color = OnSurface, lineHeight = 20.sp)
        }

        RecipeDetailMeta(recipe = recipe)

        if (recipe.ingredients.isNotBlank()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("INGREDIENTES", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Primary)
                Text(recipe.ingredients, fontSize = 14.sp, color = OnSurface, lineHeight = 20.sp)
            }
        }

        if (recipe.instructions.isNotBlank()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("PREPARACIÓN", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Primary)
                Text(recipe.instructions, fontSize = 14.sp, color = OnSurface, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
private fun RecipeDetailMeta(recipe: Recipe) {
    val items = buildList {
        recipe.cookTimeMin?.let { add("⏱ $it min") }
        recipe.bestMoment?.let  { add("🍽 $it") }
        if (recipe.isPublic) add("🌍 Pública")
    }
    if (items.isEmpty()) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { label ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceContainerLow)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) { Text(label, fontSize = 12.sp, color = OnSurface) }
        }
    }
}
