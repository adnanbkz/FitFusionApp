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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
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
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

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

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.barcodeNotFound) {
        if (state.barcodeNotFound) {
            snackbarHostState.showSnackbar("Producto no encontrado en Open Food Facts")
            addFoodViewModel.clearBarcodeNotFound()
        }
    }

    Scaffold(
        containerColor = Surface,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        IconButton(onClick = addFoodViewModel::openPlateDialog) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "Estimar plato por descripción",
                                tint = Primary,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        if (state.barcodeLoading) {
                            Box(
                                modifier = Modifier.size(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color = Primary
                                )
                            }
                        } else {
                            IconButton(onClick = { addFoodViewModel.openScanner() }) {
                                Icon(
                                    painterResource(R.drawable.ic_tracking),
                                    contentDescription = "Escanear código",
                                    tint = Primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
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
                .imePadding()
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

    if (state.scannerOpen) {
        BarcodeScannerOverlay(
            onBarcodeDetected = addFoodViewModel::lookupBarcode,
            onClose           = addFoodViewModel::closeScanner,
        )
    }

    if (state.aiPlateDialogOpen) {
        AlertDialog(
            onDismissRequest = addFoodViewModel::dismissPlateDialog,
            title = { Text("Estimar plato con IA", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Describe el plato y la IA estimará sus macros.",
                        fontSize = 12.sp, color = OnSurfaceVariant,
                    )
                    OutlinedTextField(
                        value         = state.aiPlateDescription,
                        onValueChange = addFoodViewModel::onAiPlateDescriptionChange,
                        placeholder   = { Text("Ej: ensalada césar con pollo y picatostes", fontSize = 13.sp) },
                        minLines      = 3,
                        maxLines      = 5,
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(12.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = SurfaceContainerLow,
                            focusedContainerColor   = SurfaceContainerLow,
                            unfocusedBorderColor    = Color.Transparent,
                            focusedBorderColor      = Primary,
                        ),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = addFoodViewModel::estimatePlateWithAi,
                    enabled = state.aiPlateDescription.isNotBlank() && !state.isEstimatingPlate,
                ) {
                    if (state.isEstimatingPlate) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Primary)
                    } else {
                        Text("Estimar", color = Primary, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = addFoodViewModel::dismissPlateDialog) { Text("Cancelar") }
            },
        )
    }

    state.aiPlateError?.let { msg ->
        AlertDialog(
            onDismissRequest = addFoodViewModel::dismissPlateError,
            title = { Text("No se pudo consultar la IA") },
            text = { Text(msg, fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = addFoodViewModel::dismissPlateError) { Text("OK") }
            },
        )
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
            } else if (state.searchResults.isEmpty() && state.isLoadingExternalSearch) {
                item { SearchLoadingBox("Buscando más productos en Open Food Facts...") }
            } else if (state.searchResults.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (state.externalSearchFailed) "Búsqueda externa no disponible" else "Sin resultados locales",
                                fontWeight = FontWeight.Bold,
                                color = OnSurface,
                            )
                            Text(
                                if (state.externalSearchFailed) {
                                    "Prueba otra marca o escanea el código de barras"
                                } else {
                                    "Prueba otra marca o un nombre más corto"
                                },
                                fontSize = 13.sp, color = OnSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
                if (state.canLoadMoreSearch && !state.isLoadingExternalSearch) {
                    item {
                        LoadMoreSearchButton(
                            loading = state.isLoadingMoreSearch,
                            onClick = viewModel::loadMoreSearchResults,
                        )
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
                if (state.isLoadingExternalSearch) {
                    item { SearchLoadingBox("Buscando más productos en Open Food Facts...") }
                }
                if (state.externalSearchFailed) {
                    item {
                        SearchNotice("Mostrando resultados locales. Open Food Facts no responde ahora.")
                    }
                }
                if (state.canLoadMoreSearch && !state.isLoadingExternalSearch) {
                    item {
                        LoadMoreSearchButton(
                            loading = state.isLoadingMoreSearch,
                            onClick = viewModel::loadMoreSearchResults,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchLoadingBox(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(color = Primary, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        Text(message, color = OnSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
private fun SearchNotice(message: String) {
    Surface(
        color = SurfaceContainerLowest,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = message,
            color = OnSurfaceVariant,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun LoadMoreSearchButton(
    loading: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        OutlinedButton(
            onClick = onClick,
            enabled = !loading,
            shape = RoundedCornerShape(14.dp),
        ) {
            if (loading) {
                CircularProgressIndicator(color = Primary, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(if (loading) "Cargando..." else "Cargar más")
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
            Icon(Icons.Default.Restaurant, null, Modifier.size(24.dp), tint = Primary)
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
        ) { Icon(Icons.Default.Restaurant, null, Modifier.size(22.dp), tint = Primary) }

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
                    Icon(Icons.Default.Restaurant, null, Modifier.size(24.dp), tint = Primary)
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
                    if (isEmpty()) append(recipe.bestMoments.firstOrNull() ?: "Receta")
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
            ) { Icon(Icons.Default.Restaurant, null, Modifier.size(28.dp), tint = Primary) }
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
            ) { Icon(Icons.Default.Restaurant, null, Modifier.size(28.dp), tint = Primary) }
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

        if (recipe.ingredients.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("INGREDIENTES", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Primary)
                recipe.ingredients.forEach { ing ->
                    Text(
                        "${ing.name} · ${ing.quantityG}g · ${ing.totalKcal} kcal",
                        fontSize = 13.sp, color = OnSurface, lineHeight = 18.sp,
                    )
                }
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
        recipe.cookTimeMin?.let { add("$it min") }
        recipe.bestMoments.firstOrNull()?.let { add(it) }
        if (recipe.isPublic) add("Pública")
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

@Composable
private fun BarcodeScannerOverlay(
    onBarcodeDetected: (String) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasPermission) {
            val barcodeScanner = remember {
                BarcodeScanning.getClient(productBarcodeScannerOptions())
            }
            val controller = remember {
                LifecycleCameraController(context).apply {
                    setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
                }
            }

            DisposableEffect(Unit) {
                controller.bindToLifecycle(lifecycleOwner)
                controller.setImageAnalysisAnalyzer(
                    ContextCompat.getMainExecutor(context),
                    createBarcodeAnalyzer(barcodeScanner) { code ->
                        controller.clearImageAnalysisAnalyzer()
                        onBarcodeDetected(code)
                    }
                )
                onDispose {
                    controller.clearImageAnalysisAnalyzer()
                    controller.unbind()
                    barcodeScanner.close()
                }
            }

            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        this.controller = controller
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            ScanGuideFrame()

            Text(
                text = "Apunta al código de barras del producto",
                color = Color.White,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
                    .padding(horizontal = 32.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Permiso de cámara requerido", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    "Activa el permiso de cámara en Ajustes para usar el escáner",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cerrar escáner",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun ScanGuideFrame() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val frameWidth  = size.width * 0.72f
        val frameHeight = frameWidth * 0.48f
        val left   = (size.width  - frameWidth)  / 2f
        val top    = (size.height - frameHeight) / 2f
        val right  = left + frameWidth
        val bottom = top  + frameHeight
        val overlay = Color.Black.copy(alpha = 0.55f)

        drawRect(overlay, topLeft = Offset(0f,    0f),    size = Size(size.width, top))
        drawRect(overlay, topLeft = Offset(0f,    bottom), size = Size(size.width, size.height - bottom))
        drawRect(overlay, topLeft = Offset(0f,    top),   size = Size(left, frameHeight))
        drawRect(overlay, topLeft = Offset(right, top),   size = Size(size.width - right, frameHeight))

        val cornerLen = 28.dp.toPx()
        val sw = 3.dp.toPx()

        // Top-left
        drawLine(Color.White, Offset(left, top + cornerLen), Offset(left, top), sw, StrokeCap.Square)
        drawLine(Color.White, Offset(left, top), Offset(left + cornerLen, top), sw, StrokeCap.Square)
        // Top-right
        drawLine(Color.White, Offset(right - cornerLen, top), Offset(right, top), sw, StrokeCap.Square)
        drawLine(Color.White, Offset(right, top), Offset(right, top + cornerLen), sw, StrokeCap.Square)
        // Bottom-left
        drawLine(Color.White, Offset(left, bottom - cornerLen), Offset(left, bottom), sw, StrokeCap.Square)
        drawLine(Color.White, Offset(left, bottom), Offset(left + cornerLen, bottom), sw, StrokeCap.Square)
        // Bottom-right
        drawLine(Color.White, Offset(right - cornerLen, bottom), Offset(right, bottom), sw, StrokeCap.Square)
        drawLine(Color.White, Offset(right, bottom), Offset(right, bottom - cornerLen), sw, StrokeCap.Square)
    }
}

private fun createBarcodeAnalyzer(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    onDetected: (String) -> Unit,
): ImageAnalysis.Analyzer {
    val fired = AtomicBoolean(false)
    return ImageAnalysis.Analyzer { imageProxy ->
        if (fired.get()) { imageProxy.close(); return@Analyzer }
        val mediaImage = imageProxy.image
        if (mediaImage == null) { imageProxy.close(); return@Analyzer }
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                barcodes.firstNotNullOfOrNull { it.rawValue?.toProductBarcodeOrNull() }?.let { code ->
                    if (fired.compareAndSet(false, true)) onDetected(code)
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}

private fun productBarcodeScannerOptions(): BarcodeScannerOptions =
    BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_ITF,
        )
        .build()

private fun String.toProductBarcodeOrNull(): String? =
    filter(Char::isDigit).takeIf { it.length in 8..14 }
