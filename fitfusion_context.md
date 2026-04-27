# FitFusion Context For Next Agent

Fecha de handoff: 2026-04-27
Workspace: `/home/adnan/Project`
Usuario: Adnan. Suele pedir las cosas en español. Prefiere ejecución directa cuando el camino está claro.

## Estado Actual Del Repo

- Rama actual: `main`.
- Build verificado: `./gradlew :app:assembleDebug` pasa limpio.
- `git diff --check` limpio.
- Ramas auxiliares locales a no borrar: `backup-main-before-sync-20260423`, `sync-main-from-master-20260423`.

## Roadmap Del Proyecto

Fuente de verdad: `/home/adnan/Videos/codex_documento.md`.

Orden de prioridad del core:
1. Auth real ✅
2. Catálogo de ejercicios ✅
3. Workouts ✅
4. Health Connect ✅
5. FatSecret ⚠️ (Cloud Functions escritas, pendiente deploy — ver abajo)
6. Daily summaries ✅ (capa de datos completa, UI pendiente)
7. Social/feed ✅ (básico funcional)

## Trabajo Hecho En Esta Sesión (2026-04-27)

### CLAUDE.md
Se creó `/home/adnan/Project/CLAUDE.md` con arquitectura, comandos y gotchas del proyecto.

### FatSecret via Cloud Functions
Estado: **listo para deploy, bloqueado por plan Blaze**.

- `/home/adnan/Project/firebase.json` — config Firebase.
- `/home/adnan/Project/.firebaserc` — proyecto `fitfusiondiet`.
- `/home/adnan/Project/functions/index.js` — dos HTTPS callables:
  - `searchFoods(query, maxResults)` — OAuth 1.0a → FatSecret `foods.search` → devuelve lista normalizada.
  - `getFoodDetail(foodId)` — OAuth 1.0a → FatSecret `food.get.v4` → nutrición detallada (para barcode futuro).
- `/home/adnan/Project/functions/package.json` — Node 20, `firebase-functions ^4.9.0`, `firebase-admin ^12.0.0`.
- `/home/adnan/Project/functions/.env` — credenciales FatSecret reales (en `.gitignore`).
  - `FATSECRET_KEY=c513df59e1da4973bfb3b407e6d9c194`
  - `FATSECRET_SECRET=990f1e810a9a4160923c24bce984f226`
- Las Cloud Functions leen de `process.env.FATSECRET_KEY` y `process.env.FATSECRET_SECRET`.

**Para deployar cuando tenga Blaze:**
```bash
firebase login   # si no está autenticado
firebase deploy --only functions
```

### Android — Modelo Food
`app/src/main/java/com/example/fitfusion/data/models/FoodModels.kt`:
- `emoji: String` → `emoji: String = "🍽️"` (default para alimentos FatSecret).
- Añadido `val fatSecretId: String? = null`.

### FatSecretRepository
Nuevo: `app/src/main/java/com/example/fitfusion/data/repository/FatSecretRepository.kt`
- `object` singleton.
- `searchFoods(query, maxResults)` — llama Cloud Function `searchFoods` via `FirebaseFunctions`.
- `getFoodDetail(fatSecretId)` — llama Cloud Function `getFoodDetail`.
- Mapea respuesta a `List<Food>` con `fatSecretId` y emoji default.

### FoodRepository — Firestore Real
`app/src/main/java/com/example/fitfusion/data/repository/FoodRepository.kt` **completamente reescrito**:
- Era mock puro (12 alimentos hardcodeados, in-memory). Ahora es Firestore real.
- Patrón `object` singleton con `AuthStateListener` (igual que `WorkoutRepository`).
- Colección Firestore: `users/{uid}/foodLogs/{logId}` — cada `LoggedFood` es un documento.
- Listener en tiempo real reconstruye `_dayLogs: StateFlow<Map<LocalDate, DayLog>>`.
- `_recents: StateFlow<List<Food>>` — últimos 4 alimentos únicos registrados.
- `favorites` devuelve lista vacía (feature futura).
- Custom meals (addMealToDay, removeMealFromDay, renameMealInDay) siguen in-memory (se pierden al reiniciar — aceptable MVP).
- `addFood`, `removeFood`, `updateFood` son ahora `suspend` y llaman `pushFoodSummary()` tras cada write.

Estructura documento Firestore `foodLogs/{logId}`:
```
id, date (yyyy-MM-dd), mealSlotId, mealSlotName, mealSlotIsCustom,
quantity, servingLabel, servingGrams,
foodId, fatSecretId?, foodName, foodBrand?, emoji,
kcalPer100g, proteinPer100g, carbsPer100g, fatsPer100g, createdAtMs
```

### AddFoodViewModel — Búsqueda Paralela
`app/src/main/java/com/example/fitfusion/viewmodel/AddFoodViewModel.kt`:
- Búsqueda en paralelo: `IngredientRepository` (Firestore) + `FatSecretRepository` (Cloud Function) con `async/await`.
- Merge: resultados Firestore primero, luego FatSecret sin duplicados por nombre.
- `isLoadingSearch: Boolean` añadido a `AddFoodUiState`.
- `confirmAdd` y `confirmAddRecipe` ahora llaman `FoodRepository.addFood()` con `viewModelScope.launch`.
- `recents` se recibe de `FoodRepository.recents` StateFlow.

### AddFoodScreen — Spinner de búsqueda
Spinner `CircularProgressIndicator` mientras `isLoadingSearch == true`.

### TrackingViewModel
`removeFood` y `confirmEdit` envueltos en `viewModelScope.launch` (ahora que son suspend).

### Daily Summaries — Capa de Datos

**DailySummaryRepository** nuevo: `app/src/main/java/com/example/fitfusion/data/repository/DailySummaryRepository.kt`
- `mergeFoodSummary(date, kcalConsumed, proteinG, carbsG, fatG)` — merge en Firestore.
- `mergeWorkoutSummary(date, workoutCount, kcalBurned, totalVolumeKg)` — merge en Firestore.
- Usa `SetOptions.merge()` para no pisar campos de otras fuentes.

Documento `users/{uid}/dailySummaries/{yyyy-MM-dd}` tiene ahora campos de tres fuentes:
| Fuente | Campos |
|---|---|
| HealthRepository (existía antes) | `steps`, `stepCaloriesEstimated`, `averageHeartRate`, `healthSource`, `healthSyncedAt` |
| FoodRepository (nuevo) | `kcalConsumed`, `proteinG`, `carbsG`, `fatG` |
| WorkoutRepository (nuevo) | `workoutCount`, `kcalBurned`, `totalVolumeKg` |

**WorkoutRepository**: `saveWorkout` y `removeWorkout` llaman `pushWorkoutSummary(date)` al terminar.
**FoodRepository**: `addFood`, `removeFood`, `updateFood` llaman `pushFoodSummary(date)` al terminar.

**Pendiente de esta feature**: Exponer `dailySummaries` en la UI (TrackingScreen o pantalla nueva).

## Próximo Paso Probable

1. **Deploy functions** (cuando tenga plan Blaze):
   ```bash
   firebase deploy --only functions
   ```
2. **UI de Daily Summaries** — añadir listener en `TrackingViewModel` que lea `users/{uid}/dailySummaries/{fecha}` y muestre el resumen consolidado (kcal consumidas vs quemadas, macros, pasos, workouts) en `TrackingScreen`.
3. **Probar flujo completo** en dispositivo: buscar alimento (FatSecret), registrarlo, verificar que aparece en Firestore `foodLogs` y que `dailySummaries` se actualiza.

## Firestore Security Rules Actuales

Las reglas publicadas por el usuario (2026-04-27) cubren:
- `users/{uid}/**` — ownership.
- `exercises/{exerciseId}` — read only si autenticado.
- `posts/{postId}` + `likes` + `comments` — validación completa.
- `users/{uid}/foodLogs` está cubierto por `match /users/{userId}/{document=**}`.
- `users/{uid}/dailySummaries` ídem.

## Issues Y Notas Importantes

- `functions/.env` tiene las credenciales reales → nunca commitear (está en `.gitignore`).
- `HealthConnectMannager.kt` — typo doble `n` en el nombre, no renombrar sin actualizar imports.
- `HealthViewModel.kt.kt` — doble extensión, misma precaución.
- Coil 3 + Kotlin 2.0.21: no subir ninguno sin verificar compatibilidad.
- `WorkoutRepository` y `FeedRepository` son `object` singletons con `AuthStateListener`. `FoodRepository` ahora también.
- FatSecret debe ir **siempre** por Cloud Functions, nunca HTTP directo desde Android en producción.
- `git push` por terminal puede fallar por credenciales HTTPS — usar GitHub connector.
- No borrar ramas auxiliares `backup-main-before-sync-*` sin permiso.

## Comandos Útiles

```bash
./gradlew :app:assembleDebug          # build principal
firebase deploy --only functions      # deploy cuando tenga Blaze
git -C /home/adnan/Project status --short --branch
git -C /home/adnan/Project log --oneline --decorate -8
```
