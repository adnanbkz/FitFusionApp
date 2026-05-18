package com.example.fitfusion.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.fitfusion.data.models.Food
import com.example.fitfusion.data.models.RecipeIngredient
import com.example.fitfusion.ui.theme.*
import com.example.fitfusion.viewmodel.CreateRecipeViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PantallaCreateRecipe(
    navController: NavHostController,
    viewModel: CreateRecipeViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.onPhotoSelected(it) } }

    if (state.showCamera) {
        PantallaCamera(
            onClose          = viewModel::closeCamera,
            onMediaCaptured  = { uri, isVideo -> if (!isVideo) viewModel.onPhotoSelected(uri) }
        )
        return
    }

    BackHandler {
        viewModel.requestExitWithDirtyCheck { navController.popBackStack() }
    }

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = { Text("Nueva receta", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.requestExitWithDirtyCheck { navController.popBackStack() }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = OnSurface)
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveRecipe { navController.popBackStack() } },
                        enabled = state.isValid && !state.isSaving
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color       = Primary,
                            )
                        } else {
                            Text(
                                "Guardar",
                                color      = if (state.isValid) Primary else OnSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 15.sp,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface),
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(innerPadding).imePadding(),
            contentPadding = PaddingValues(16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                PhotoBox(
                    photoUri    = state.photoUri,
                    onTapAdd    = viewModel::openPhotoOptions,
                    onRemove    = viewModel::clearPhoto,
                )
            }

            item {
                RecipeInfoCard(state = state, viewModel = viewModel)
            }

            item {
                SectionLabel("DESCRIPCIÓN")
                Spacer(Modifier.height(8.dp))
                FieldTextArea(
                    value         = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    placeholder   = "Breve descripción de la receta (opcional)",
                    minLines      = 2,
                    maxLines      = 4,
                )
            }

            item {
                SectionLabel("INGREDIENTES")
                Spacer(Modifier.height(8.dp))
                IngredientsSection(
                    ingredients   = state.ingredients,
                    onAdd         = viewModel::openIngredientPicker,
                    onRemove      = viewModel::removeIngredient,
                    onUpdateQty   = viewModel::updateIngredientQuantity,
                )
            }

            item {
                SectionLabel("PREPARACIÓN")
                Spacer(Modifier.height(8.dp))
                FieldTextArea(
                    value         = state.instructions,
                    onValueChange = viewModel::onInstructionsChange,
                    placeholder   = "Pasos a seguir...",
                    minLines      = 4,
                    maxLines      = 12,
                )
            }

            item {
                SectionLabel("DETALLES")
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField(
                        value         = state.cookTime,
                        onValueChange = viewModel::onCookTimeChange,
                        placeholder   = "Tiempo (min)",
                        modifier      = Modifier.weight(1f),
                    )
                    KcalReadonlyField(
                        kcal        = state.totalKcal,
                        overridden  = state.kcalOverride != null,
                        canRefine   = state.ingredients.isNotEmpty(),
                        isRefining  = state.isRefiningKcal,
                        onRefine    = viewModel::refineKcalWithAi,
                        modifier    = Modifier.weight(1f),
                    )
                }
            }

            item {
                SectionLabel("MEJOR MOMENTO")
                Spacer(Modifier.height(8.dp))
                BestMomentChips(
                    selected = state.bestMoments,
                    onToggle = viewModel::toggleBestMoment,
                )
            }

            item {
                PublishRow(
                    isPublic   = state.isPublic,
                    onToggle   = viewModel::onPublicToggle,
                )
            }

            if (state.saveError != null) {
                item {
                    Text(
                        state.saveError!!,
                        color    = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }

    if (state.showPhotoOptions) {
        PhotoSourceSheet(
            onDismiss = viewModel::dismissPhotoOptions,
            onGallery = {
                viewModel.dismissPhotoOptions()
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onCamera = viewModel::openCamera,
        )
    }

    if (state.showIngredientPicker) {
        IngredientPickerSheet(
            query              = state.ingredientQuery,
            onQueryChange      = viewModel::onIngredientQueryChange,
            results            = state.ingredientResults,
            isSearching        = state.isSearchingIngredients,
            onAdd              = viewModel::addIngredient,
            onDismiss          = viewModel::dismissIngredientPicker,
        )
    }

    state.aiKcalResult?.let { result ->
        AlertDialog(
            onDismissRequest = viewModel::dismissAiKcal,
            title = { Text("Estimación IA", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Calculado por ingredientes: ${state.computedKcal} kcal", fontSize = 13.sp)
                    Text(
                        "Sugerencia IA: ${result.totalKcal} kcal · ${result.totalProteinG}P · ${result.totalCarbsG}C · ${result.totalFatG}G",
                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Primary,
                    )
                    result.notes?.takeIf { it.isNotBlank() }?.let {
                        Text(it, fontSize = 12.sp, color = OnSurfaceVariant)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::applyAiKcal) {
                    Text("Aplicar", color = Primary, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissAiKcal) { Text("Cancelar") }
            },
        )
    }

    state.aiKcalError?.let { msg ->
        AlertDialog(
            onDismissRequest = viewModel::dismissAiKcalError,
            title = { Text("No se pudo consultar la IA") },
            text = { Text(msg, fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissAiKcalError) { Text("OK") }
            },
        )
    }

    if (state.showDraftDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDraftDialog,
            title   = { Text("¿Guardar como borrador?") },
            text    = { Text("Tienes cambios sin guardar. Puedes guardar la receta como borrador o descartarla.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveAsDraft { navController.popBackStack() }
                }) { Text("Guardar borrador", color = Primary, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        viewModel.dismissDraftDialog()
                        navController.popBackStack()
                    }) { Text("Descartar", color = MaterialTheme.colorScheme.error) }
                    TextButton(onClick = viewModel::dismissDraftDialog) { Text("Seguir") }
                }
            },
        )
    }
}

@Composable
private fun IngredientsSection(
    ingredients: List<RecipeIngredient>,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit,
    onUpdateQty: (Int, Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (ingredients.isEmpty()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                elevation = CardDefaults.cardElevation(0.dp),
            ) {
                Text(
                    "Aún no has añadido ingredientes",
                    fontSize = 13.sp, color = OnSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }
        } else {
            ingredients.forEachIndexed { index, ing ->
                IngredientRow(
                    ingredient = ing,
                    onUpdateQty = { onUpdateQty(index, it) },
                    onRemove = { onRemove(index) },
                )
            }
        }
        OutlinedButton(
            onClick = onAdd,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Añadir ingrediente", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun IngredientRow(
    ingredient: RecipeIngredient,
    onUpdateQty: (Int) -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(ingredient.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = OnSurface, maxLines = 1)
                Text(
                    "${ingredient.totalKcal} kcal · ${ingredient.totalProtein}g P · ${ingredient.totalCarbs}g C · ${ingredient.totalFat}g G",
                    fontSize = 11.sp, color = OnSurfaceVariant,
                )
            }
            QuantityStepper(
                value = ingredient.quantityG,
                onChange = onUpdateQty,
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, "Quitar", tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun QuantityStepper(value: Int, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(SurfaceContainerLowest)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { onChange(value - 10) }, enabled = value > 10, modifier = Modifier.size(28.dp)) {
            Text("-", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (value > 10) Primary else OnSurfaceVariant)
        }
        Text("${value}g", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.widthIn(min = 40.dp), textAlign = TextAlign.Center)
        IconButton(onClick = { onChange(value + 10) }, modifier = Modifier.size(28.dp)) {
            Text("+", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Primary)
        }
    }
}

@Composable
private fun KcalReadonlyField(
    kcal: Int,
    overridden: Boolean,
    canRefine: Boolean,
    isRefining: Boolean,
    onRefine: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (overridden) "Kcal (IA)" else "Kcal totales",
                    fontSize = 11.sp, color = if (overridden) Primary else OnSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                if (canRefine) {
                    if (isRefining) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color       = Primary,
                        )
                    } else {
                        TextButton(onClick = onRefine, contentPadding = PaddingValues(horizontal = 6.dp)) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("IA", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Text("$kcal kcal", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientPickerSheet(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<Food>,
    isSearching: Boolean,
    onAdd: (Food, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedFood by remember { mutableStateOf<Food?>(null) }
    var quantity by remember { mutableStateOf("100") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = SurfaceContainerLowest,
        dragHandle       = { BottomSheetDefaults.DragHandle(color = OutlineVariant) },
        modifier         = Modifier.imePadding(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp).heightIn(max = 540.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Buscar ingrediente", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OnSurface)
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Ej: pollo, arroz, manzana...", color = OnSurfaceVariant) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = OnSurfaceVariant) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = SurfaceContainerLow,
                    focusedContainerColor = SurfaceContainerLow,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Primary,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            if (selectedFood != null) {
                val food = selectedFood!!
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                    elevation = CardDefaults.cardElevation(0.dp),
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(food.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurface)
                        food.brand?.let { Text(it, fontSize = 12.sp, color = OnSurfaceVariant) }
                        Text("${food.kcalPer100g.toInt()} kcal / 100g", fontSize = 12.sp, color = Primary, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = quantity,
                                onValueChange = { v -> if (v.all(Char::isDigit) && v.length <= 4) quantity = v },
                                label = { Text("Cantidad (g)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = SurfaceContainerLowest,
                                    focusedContainerColor = SurfaceContainerLowest,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Primary,
                                ),
                            )
                            Button(
                                onClick = {
                                    val q = quantity.toIntOrNull() ?: 100
                                    onAdd(food, q)
                                    selectedFood = null
                                    quantity = "100"
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            ) { Text("Añadir", fontWeight = FontWeight.SemiBold) }
                        }
                    }
                }
            } else if (isSearching) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary, modifier = Modifier.size(28.dp))
                }
            } else if (query.isNotBlank() && results.isEmpty()) {
                Text("Sin resultados para \"$query\"", fontSize = 13.sp, color = OnSurfaceVariant)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f, fill = false)) {
                    items(results, key = { it.id }) { food ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceContainerLow)
                                .clickable {
                                    selectedFood = food
                                    quantity = "100"
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(food.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = OnSurface, maxLines = 1)
                                if (food.brand != null) {
                                    Text(food.brand, fontSize = 11.sp, color = OnSurfaceVariant, maxLines = 1)
                                }
                            }
                            Text("${food.kcalPer100g.toInt()} kcal/100g", fontSize = 12.sp, color = Primary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PhotoBox(
    photoUri: android.net.Uri?,
    onTapAdd: () -> Unit,
    onRemove: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceContainerLowest)
            .clickable(enabled = photoUri == null, onClick = onTapAdd),
        contentAlignment = Alignment.Center,
    ) {
        if (photoUri != null) {
            AsyncImage(
                model             = photoUri,
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Close, "Quitar foto", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Default.CameraAlt, null, tint = OnSurfaceVariant, modifier = Modifier.size(36.dp))
                Text("Añadir foto de la receta", fontSize = 13.sp, color = OnSurfaceVariant)
            }
        }
    }
}

@Composable
private fun RecipeInfoCard(
    state: com.example.fitfusion.viewmodel.CreateRecipeUiState,
    viewModel: CreateRecipeViewModel,
) {
    Card(
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionLabel("INFORMACIÓN")
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value         = state.name,
                    onValueChange = viewModel::onNameChange,
                    placeholder   = { Text("Nombre de la receta *", color = OnSurfaceVariant, fontSize = 15.sp) },
                    singleLine    = true,
                    modifier      = Modifier.weight(1f),
                    shape         = RoundedCornerShape(12.dp),
                    colors        = filledFieldColors(),
                )
            }
        }
    }
}

@Composable
private fun FieldTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int,
    maxLines: Int,
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        placeholder   = { Text(placeholder, color = OnSurfaceVariant, fontSize = 14.sp) },
        minLines      = minLines,
        maxLines      = maxLines,
        modifier      = Modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(14.dp),
        colors        = filledFieldColors(),
    )
}

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value           = value,
        onValueChange   = onValueChange,
        placeholder     = { Text(placeholder, color = OnSurfaceVariant, fontSize = 13.sp) },
        singleLine      = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier        = modifier,
        shape           = RoundedCornerShape(12.dp),
        colors          = filledFieldColors(),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BestMomentChips(selected: Set<String>, onToggle: (String) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement   = Arrangement.spacedBy(8.dp),
    ) {
        listOf("Desayuno", "Almuerzo", "Cena", "Snack", "Pre-entreno", "Post-entreno").forEach { moment ->
            val isSelected = moment in selected
            FilterChip(
                selected = isSelected,
                onClick  = { onToggle(moment) },
                label    = { Text(moment, fontSize = 12.sp) },
                shape    = RoundedCornerShape(20.dp),
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary.copy(alpha = 0.15f),
                    selectedLabelColor     = Primary,
                ),
            )
        }
    }
}

@Composable
private fun PublishRow(isPublic: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Publicar en la comunidad", fontWeight = FontWeight.SemiBold, color = OnSurface, fontSize = 14.sp)
                Text(
                    "Otros usuarios podrán verla y guardarla",
                    fontSize = 12.sp, color = OnSurfaceVariant,
                )
            }
            Switch(
                checked         = isPublic,
                onCheckedChange = onToggle,
                colors          = SwitchDefaults.colors(
                    checkedThumbColor  = Color.White,
                    checkedTrackColor  = Primary,
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoSourceSheet(
    onDismiss: () -> Unit,
    onGallery: () -> Unit,
    onCamera: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = SurfaceContainerLowest,
        dragHandle       = { BottomSheetDefaults.DragHandle(color = OutlineVariant) },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Fuente de la foto", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OnSurface)
            PhotoSourceOption(
                icon    = Icons.Default.PhotoCamera,
                title   = "Cámara",
                subtitle= "Hacer una foto nueva",
                onClick = onCamera,
            )
            PhotoSourceOption(
                icon    = Icons.Default.PhotoLibrary,
                title   = "Galería",
                subtitle= "Elegir una foto existente",
                onClick = onGallery,
            )
        }
    }
}

@Composable
private fun PhotoSourceOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(Primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = Primary, modifier = Modifier.size(22.dp)) }
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = OnSurface)
            Text(subtitle, fontSize = 12.sp, color = OnSurfaceVariant)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        fontSize      = 11.sp,
        fontWeight    = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        color         = Primary,
    )
}

@Composable
private fun filledFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = SurfaceContainerLow,
    focusedContainerColor   = SurfaceContainerLow,
    unfocusedBorderColor    = Color.Transparent,
    focusedBorderColor      = Primary,
)
