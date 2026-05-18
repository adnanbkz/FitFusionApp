# Devlog FitFusion

Aquí voy apuntando los marrones que me he ido encontrando y cómo los he resuelto. No es documentación oficial, es más un cuaderno para no tropezar dos veces con la misma piedra.

---

## Siete arreglos en una sesión: onboarding, entreno, social y calorías (18 may 2026)

Otra tanda larga de una tacada: que la fecha de nacimiento se formatee sola, implementar el botón "Añadir ejercicio" (estaba muerto), enseñar la foto de los entrenos en el historial, arreglar la pestaña "Me gusta" del perfil, que el campo de comentar no quede tapado por la barra del sistema, que los contadores de seguidores se actualicen de verdad, y calcular las calorías diarias en el backend de C#.

### 1 · La fecha de nacimiento se formatea sola

Antes el usuario tenía que escribir las barras a mano. Ahora `OnboardingViewModel.onBirthDateChange` filtra solo los dígitos, corta a 8 y mete las `/` en las posiciones 2 y 4 — tecleas `12031990` y ves `12/03/1990`. El campo pasa a teclado numérico y el wizard solo deja avanzar con la fecha completa (10 caracteres), así no se guarda un `12/0` a medias.

### 2 · El botón "Añadir ejercicio" que no hacía nada

En `ActiveWorkoutScreen` había un `OutlinedButton` con `onClick = { }` — vacío. Y de regalo, `contentColor` y `containerColor` ambos a `primary`: texto del color del fondo, invisible.

El marrón no era el `onClick`, era el flujo. `AddWorkoutScreen` (el catálogo) tiene un `LaunchedEffect` que, si hay sesión activa, te redirige a `ActiveWorkoutScreen`. Reusarlo tal cual para añadir ejercicios me redirigía solo antes de elegir nada. Solución: un `pickerMode` que viaja por la ruta de navegación. En modo picker no redirige, y el FAB pasa de "Iniciar" a "Añadir · N" — por cada ejercicio elegido llama `ActiveWorkoutManager.addExercise()` y hace `popBackStack()`. El `addExercise` ya dedup-aba por `documentId`, así que reañadir uno que ya está es inofensivo.

### 3 · Fotos de los entrenos en el historial

Los entrenos guardan `mediaUrls` pero `WorkoutHistoryCard` solo enseñaba nombre, fecha y chips. Añadí una portada con la primera foto (`AsyncImage`, `ContentScale.Crop`) y un badge `+N` si hay varias. El dato ya venía de Firestore, era puro UI.

### 4 · La pestaña "Me gusta" siempre vacía

El bug bonito. `FeedRepository.refreshLikedPosts` hacía:

```kotlin
firestore.collectionGroup("likes").whereEqualTo("userId", uid).get()
```

Una query de *collection group* necesita un índice declarado explícitamente en Firestore — el de campo único no se autocrea para scope de grupo. Sin el índice, la query revienta con `FAILED_PRECONDITION`. ¿Y dónde acababa el error? En un `catch (_: Exception) { }`. Otra vez el catch silencioso (ya me mordió con FatSecret, más abajo). La pestaña quedaba vacía sin un solo log.

En vez de pelearme con el índice, quité la query entera. `FeedRepository` ya carga todos los posts en `baseItems` y mantiene `likedPostIds` con un listener por post. Así que los "me gusta" son `baseItems.filter { it.postId in likedPostIds }`, calculado en `emitItems()`. Cero índices y encima reactivo: das like y aparece en la pestaña al instante.

### 5 · El campo de comentar tapado por la barra de navegación

`PostDetailScreen` tenía la barra de comentar con `Modifier.imePadding()` — eso la sube por encima del teclado, pero no por encima de la barra de gestos/botones del sistema. Cambiado a `windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))`: el `union` coge el inset mayor, así que con teclado cerrado respeta la barra de navegación y con teclado abierto usa el del IME (que ya cubre esa zona). Sin doble margen.

### 6 · Seguir / Siguiendo que no se actualizaba

`toggleFollow` escribía los docs en las subcolecciones `following`/`followers` pero nunca tocaba los campos `followersCount`/`followingCount` del documento de usuario — que es justo lo que pintaban las pantallas. Un contador que nadie mantiene.

En vez de ponerme a incrementar campos en el doc de otro usuario (lío de permisos), los contadores ahora se derivan del tamaño real de las subcolecciones: un listener sobre `users/{uid}/followers` y otro sobre `following`, y `snapshot.size()` al estado. Siempre cuadra con la verdad, en `UserScreenViewModel` y en `ProfileViewModel`. El botón además revierte el estado optimista si la escritura falla.

Caveat: leer la subcolección `followers` de otro usuario depende de que las reglas de Firestore lo permitan. Dejé un `TODO` en el código; si las reglas lo bloquean, el contador se queda en 0 sin petar.

### 7 · Calorías diarias calculadas en el backend C#

El objetivo de calorías estaba clavado a 2000 (`DayLog.kcalGoal`) y los macros hardcodeados (160/210/65). El encargo: calcularlos según las métricas del usuario, y a poder ser en el backend de C#.

El backend existe (`FitFusion.Api`, fuera de este repo). Añadí ahí:

- `CalorieCalculator` — Mifflin-St Jeor para el metabolismo basal, multiplicador por nivel de actividad para el mantenimiento (TDEE), ajuste por objetivo (déficit 20 % / superávit 12 % / mantener) y reparto de macros.
- `NutritionController` con `POST /api/nutrition/calorie-goal`.

Como la app no recoge el sexo, uso una constante neutra en Mifflin-St Jeor (-78, la media de +5 hombres / -161 mujeres). El cálculo es determinista, sin IA — por eso va en su propio controller, no en `AiController`.

Lado Android: contrato en `AiContract.kt`, `AiRepository.calculateCalorieGoal`, y `TrackingViewModel` lo llama al abrir Seguimiento con altura/peso/edad/actividad/objetivo del perfil. Si el backend no responde, se queda en los valores por defecto — degradación limpia, la pantalla no se bloquea.

### Revisión posterior: bugs reales y la IA que estaba escondida

Después revisé los dos últimos commits como diff cerrado (`HEAD~2..HEAD`) y apareció una cosa importante: varias piezas estaban técnicamente implementadas, pero o tenían un bug de estado o no había una ruta clara para llegar a ellas.

1. **Comidas custom: renombrar ocultaba alimentos.**
   `DayLog.byMeal` agrupaba por el objeto completo `MealSlot`. Al renombrar una comida custom, el slot nuevo tenía el mismo `id` pero otro `name`, mientras los `foodLogs` seguían guardados con el nombre antiguo. Resultado: las kcal contaban, pero los alimentos desaparecían de la sección. Ahora se agrupa por `mealSlot.id` usando el slot canónico del día.

2. **Comidas custom: borrar con alimentos era incoherente.**
   Si borrabas una comida custom vacía, desaparecía. Si tenía alimentos, `rebuildDayLogs()` la reañadía desde los `foodLogs`, así que parecía que el botón no funcionaba. Decisión simple: solo se muestra borrar en comidas custom vacías y `FoodRepository.removeMealFromDay()` ignora slots con entradas.

3. **Perfil: altura/peso no se podían limpiar.**
   `UserRepository.updateUserProfile()` omitía `heightCm` y `weightKg` cuando eran `null`; con `SetOptions.merge()` eso conserva el valor viejo en Firestore. Vuelve a escribir esos campos aunque sean `null`, para que limpiar el formulario limpie el dato real.

4. **IA de workout: estaba hecha, pero sin puerta de entrada.**
   `AiContract`, `AiRepository.generateRoutine()` y toda la hoja IA de `CreateRoutineScreen` ya existían. El problema era navegación: `PlannerScreen`, `CreateRoutineScreen` y `CreateWeeklyPlanScreen` estaban en `Screens.kt`, pero no registrados en `MainActivity` ni enlazados desde el hub de entreno. Añadí las rutas y dos accesos en `WorkoutScreen`: `Rutina IA` (abre directamente la hoja IA) y `Planificador`.

5. **Kcal de entreno por IA: contrato muerto.**
   `AiWorkoutEstimate` también existía, pero nadie lo llamaba. `WorkoutFinishViewModel` ahora intenta estimar kcal al guardar el entreno. Tiene timeout corto y fallback al cálculo local (`duración * 6.5`) para no bloquear si el backend IA no está levantado o el usuario no está autenticado.

6. **Segundo barrido de alcanzabilidad IA.**
   No quedaba ningún método público de `AiRepository` sin caller, pero dos estaban demasiado escondidos: `estimatePlate()` era solo un icono en el top bar de Añadir alimento, y `refineRecipeKcal()` era un icono minúsculo dentro del campo kcal de receta. Añadí un botón visible `Estimar plato con IA` y cambié el refinado de kcal a un botón textual `IA`. También registré la ruta standalone `TrackingScreen`, aunque Seguimiento ya se ve embebido desde Home.

### Verificación

`./gradlew :app:assembleDebug` verde y `git diff --check` limpio tras los cambios Android. El backend `FitFusion.Api` se publicó en Azure App Service (`fitfusion-api`) con `Gemini__ApiKey` en app settings; la API ya responde `401 {"error":"No autenticado"}` en endpoints protegidos, que es lo esperado sin token Firebase.

### Lo que me llevo

- El `catch (_: Exception) { }` vuelve a morder. Si una feature en desarrollo falla en silencio, pierdes una tarde. Loguea o relanza, nunca te lo tragues.
- Las queries de *collection group* necesitan índice explícito. Si puedes resolver lo mismo con datos que ya tienes en memoria, te ahorras el índice y encima sale reactivo.
- Un campo contador que se escribe en un sitio y se lee en otro siempre se desincroniza. Si puedes contar la fuente de verdad (el tamaño de una subcolección), cuéntala.
- `imePadding()` sube cosas por encima del teclado, no por encima de la barra de navegación. Para las dos, `navigationBars.union(ime)`.
- Para un cálculo determinista (Mifflin-St Jeor) el backend está bien: centraliza la fórmula y se afina sin tocar la app. Distinto de la campanita de récord, que va en cliente porque es feedback inmediato.
- Que exista contrato + ViewModel no significa que la feature exista para el usuario. Hay que comprobar ruta, botón visible y fallback si el backend IA no está disponible.

---

## Las cinco features del navbar de entreno y social (17 may 2026)

Cinco cosas pedidas de una tacada: poder entrar a cada ejercicio, notificación de entreno más estética, cambiar la UI de entreno para que enseñe historial y progreso, una campanita de récord de serie dentro del entreno, y que los botones de compartir posts manden un link de verdad. Lo primero, mirar qué estaba ya hecho antes de picar nada.

### Cómo estaba la cosa

- **Detalle de ejercicio:** `ExerciseDetailScreen` ya existía entera (músculos, equipo, técnica, vídeos), pero solo se abría tocando un ejercicio en el selector en modo rutina.
- **Notificación de entreno:** existía y funcionaba (`WorkoutForegroundService`), pero el icono pequeño era un `.jpeg`.
- **Historial / progreso:** no había pantalla; la pestaña "Ejercicio" del navbar era el selector de ejercicios pelado.
- **Campanita de volumen:** no existía. Y no hay backend .NET en el repo.
- **Compartir posts:** los botones abrían el selector del sistema pero mandaban solo texto, sin link.

### Qué hice

1. **Entrar a cada ejercicio.** La pantalla ya estaba; solo faltaban accesos. Añadí un icono de info en las filas del selector en modo log (`AddWorkoutScreen`) y en la cabecera de cada ejercicio del entreno activo (`ActiveWorkoutScreen`); en esos dos sitios no había manera de abrir el detalle.

2. **Notificación más estética.** El marrón de fondo era `ic_dumbbell.jpeg` como icono pequeño: un JPEG no tiene canal alpha y el sistema usa el alpha como máscara del icono de la barra de estado, así que se veía como un cuadrado blanco. Creé iconos vectoriales (`ic_stat_workout` con la mancuerna, más pausa/play/stop para las acciones), color de acento verde de marca (`setColor`), subtítulo con "N ejercicios · M series" y las acciones ya con icono. Todo con APIs estándar de `NotificationCompat`, sin `RemoteViews` (que se renderiza distinto en cada fabricante).

3. **UI de entreno con historial y progreso.** La pestaña "Ejercicio" pasa de selector pelado a un hub: `WorkoutScreen` + `WorkoutViewModel` nuevos. Estadísticas (entrenos, esta semana, volumen total), botón de empezar entreno, y dos pestañas — Historial (tarjetas expandibles por sesión) y Progreso (una tarjeta por ejercicio con mejor serie, último volumen y una mini-gráfica de evolución dibujada con `Canvas`). La persistencia ya estaba toda en `WorkoutRepository`, así que esto es puro UI + un VM de solo lectura. *El rediseño no traía las acciones de editar/borrar entrenos del historial; eso se restauró justo después (punto 7 de la entrada anterior).*

4. **Campanita de récord de serie.** Decisión tomada: client-side, nada de API C# .NET. La campanita es un evento en tiempo real dentro del entreno y el dato ya vive en `ActiveWorkoutManager`; meter un backend solo añade latencia y un proyecto más que mantener. En `ActiveWorkoutManager.toggleSetCompleted`, al marcar una serie como completada se compara su volumen (peso×reps) con la mejor serie previa de ese ejercicio — histórico de `WorkoutRepository` más las series ya completadas de la sesión en curso. Si lo supera, emite un `SetRecordEvent` por un `SharedFlow` y `ActiveWorkoutScreen` enseña un banner animado con campana y vibración. De-dup por clave `docId#índice` para que la misma serie no vuelva a sonar si la destildas y la marcas otra vez.

5. **Compartir posts con link.** La mitad ya estaba: el cargado de `fitfusion://post/{id}` se arregló en su día (punto 6 de la entrada anterior — el fallback por ID en `FeedRepository`/`PostDetailViewModel`). Faltaba la entrada del link: `intent-filter` con `scheme=fitfusion`/`host=post` en el manifest, el enrutado en `MainActivity` (arranque en frío leyendo `intent.data` + `onNewIntent`, con `launchMode=singleTop` para que el arranque en caliente reuse la activity en vez de apilar otra), y que los tres botones de compartir (`PostDetailScreen` y las dos tarjetas del feed en `HomeScreen`) metieran `fitfusion://post/{id}` en el texto.

### Verificación

`./gradlew :app:assembleDebug` verde y `git diff --check` limpio. Sin commitear.

### Lo que me llevo

- "Compruébalo y si no, hazlo": dos de las cinco (detalle de ejercicio y notificación) ya existían a medias. Mirar antes de picar evitó rehacer pantallas enteras.
- Icono de notificación = vector, nunca un `.jpeg`/`.png` con fondo opaco: el sistema lo trata como silueta por el canal alpha.
- Para feedback inmediato durante el entreno, el cálculo va en el cliente. El dato ya está en RAM; una API solo aporta latencia y mantenimiento.
- El deep link de esquema propio (`fitfusion://`) abre la app, pero las apps de mensajería no lo hacen clicable. Si se quiere que el link viaje bien por WhatsApp/Telegram, el siguiente paso es un App Link con dominio propio y su `assetlinks.json`.

---

## Revisión post-GLM 5.1 y apagado inesperado (16 may 2026)

### Qué revisé

El último commit había tocado integración IA de plan de comidas, Tracking, posts de workout y perfil. Después de un apagado del ordenador comprobé el workspace:

- `git diff --check` limpio.
- `./gradlew :app:assembleDebug` verde tras reiniciar Gradle.
- Quedó `.kotlin/sessions/` como artefacto local de Kotlin/Gradle; se añadió `.kotlin/` a `.gitignore`.

### Arreglos aplicados

1. **Foto de perfil no podía fallar en silencio**
   - Problema: `AccountViewModel.saveProfile()` tragaba cualquier error de Firebase Storage. Si fallaba la subida, podía guardar un `content://` local en Firestore/Auth y mostrar éxito falso.
   - Fix: la URI local ya no se considera URL remota válida; si hay foto pendiente, se sube a Storage, se cierra el `InputStream` con `use`, y si falla se muestra error en vez de guardar basura.

2. **Posts de entrenamiento mostraban reps/peso mal**
   - Problema: `PostRepository` guardaba `reps` como suma total y `weightKg` como peso de la primera serie. Eso podía renderizar cosas falsas como `60 kg · 3×24`.
   - Fix: se guarda `summary` y `setBreakdown` desde `WorkoutExercise`, además de `totalReps`. El feed y detalle usan esos textos cuando existen. Para posts legacy sin `summary`, el fallback trata `reps` como reps totales (`3 series · 24 reps totales`) y no como reps por serie (`3×24`).

3. **Contraste del feed en modo claro**
   - Problema: nombres de ejercicios y títulos seguían en blanco sobre degradados/containers claros.
   - Fix: se usa `overlayTextColor` dependiente de tema en `WorkoutPostCard`, y el fondo de la lista de ejercicios es más legible en claro.

4. **Regresión en Tracking: GLM quitó `+ Comida` y duplicó `Plan IA`**
   - Problema: el botón IA quedó dos veces en el header y se eliminó la posibilidad de crear comidas personalizadas.
   - Fix: se dejó un solo `Plan IA` y se restauró `+ Comida`, `AddMealDialog` y el estado/métodos del `TrackingViewModel`. También se ajustó `FoodRepository.rebuildDayLogs()` para que una comida custom no oculte Desayuno/Almuerzo/Merienda/Cena en días vacíos, y las comidas custom vacías se persisten en `users/{uid}/mealSlots/{yyyy-MM-dd}_{mealId}`.

5. **Archivo local de Android Studio versionado**
   - Problema: `.idea/deploymentTargetSelector.xml` entró en el commit anterior.
   - Fix: eliminado del árbol y añadido a `.gitignore`.

6. **Deep links a posts podían quedarse cargando**
   - Problema: `PostDetailViewModel` solo buscaba en `FeedRepository.items`; sin sesión, el feed queda vacío y `fitfusion://post/{id}` podía mostrar `Cargando publicación` para siempre.
   - Fix: `FeedRepository` expone carga puntual por ID y el detalle usa ese fallback antes de quedarse escuchando el feed.

7. **Historial de entrenos perdió editar/eliminar**
   - Problema: el rediseño de `WorkoutScreen` mantenía tarjetas expandibles, pero había quitado las acciones para corregir o borrar entrenamientos loggeados.
   - Fix: se restauraron editar/eliminar sobre la tarjeta actual y los métodos `WorkoutViewModel` que llaman a `WorkoutRepository`.

### Verificación cruzada de los cambios

- Revisados los cambios sin commitear (AccountViewModel, PostRepository/FeedRepository, HomeScreen/PostDetailScreen, Tracking): coherentes y compilan. `:app:assembleDebug` y `dotnet build` de `FitFusion.Api` verdes.
- El punto 5 estaba a medias: añadir el archivo a `.gitignore` **no** destrackea uno ya versionado. `.idea/deploymentTargetSelector.xml` seguía en el índice de git. Ejecutado `git rm --cached .idea/deploymentTargetSelector.xml`; ahora sí está fuera del control de versiones (el archivo permanece en disco).
- Punto 4 (`+ Comida`): confirmado con decisión de producto que se mantiene restaurado y convive con el botón de Plan IA. No es regresión a revertir.

### Bug corregido: las imágenes de los posts no se subían a Firebase

- **Problema:** `ProfileViewModel.publishPost()` escribía la URI **local** directamente en el documento de Firestore — `nutritionPhotoUri` en posts de nutrición y `workoutVideoUri` en posts de entreno. Una `content://` del picker o de la cámara solo es válida en el dispositivo que la creó; en el feed de cualquier otro usuario la imagen no carga. La imagen nunca llegaba a Firebase Storage.
- **Fix:** `publishPost()` ahora sube el medio a Storage vía un helper `uploadPostMedia()` (`users/{uid}/posts/{ts}.{jpg|mp4}`, el mismo árbol que la media de workouts que sí funcionaba) y guarda la URL `https` de descarga. Si la subida falla, el post falla con error visible en vez de publicar una imagen rota — mismo criterio que el arreglo 1 de la foto de perfil.
- Las fotos de workout (`workoutMediaUrls`) ya se subían bien desde `WorkoutFinishViewModel`; no se tocaron.
- **Caveat:** los posts antiguos con `content://` ya guardado en Firestore son datos rotos históricos; el fix solo cubre publicaciones nuevas, no migra las viejas. Pendiente de confirmar en dispositivo que las reglas de Storage permiten escribir en `users/{uid}/posts/...` (no están en el repo, `firebase.json` está vacío).

### Rediseño: vista de entreno en el detalle de post

- **Contexto:** al abrir un entreno desde el perfil (pestaña Publicaciones → post de workout) se llega a `PostDetailScreen`. La lista de ejercicios era una fila apretada de una sola línea (`• Nombre  10 reps @ 60 kg · 10 reps @ 60 kg…`), poco legible.
- **Cambio:** la rama de workout de `PostDetailScreen` ahora muestra una cabecera de sección `EJERCICIOS · N` (estilo de las cabeceras de Tracking: punto + texto en mayúsculas) y una tarjeta por ejercicio: badge numerado, nombre, pill de resumen y desglose serie a serie (partiendo `setBreakdown` por `" · "`). La tarjeta calca `NeonSurfaceContainerLow` de Tracking (`RoundedCornerShape(20)`, `SurfaceContainerLow` + borde `SurfaceContainerHigh`).
- No se tocó `PostStatsCard` (la cabecera con foto/stats ya estaba bien) ni likes/comentarios/compartir. `DetailExerciseRow` se eliminó (era su único uso).
- Posts antiguos sin `setBreakdown` guardado: muestran solo la pill de resumen, sin desglose por serie (no se reconstruye desde datos legacy).

### Pendiente real detectado

- No hay backend .NET dentro de este repo; Android solo tiene contrato/cliente `AiRepository` y espera `AI_API_BASE_URL`. El despliegue inicial HTTPS ya quedó en Azure App Service; en local `AI_API_BASE_URL` apunta a esa URL desde `local.properties` (ignorado por git). Falta probarlo end-to-end desde la app con token Firebase real.
- No hay `functions/` ni Cloud Functions actuales; la documentación vieja de FatSecret quedó obsoleta. La ruta vigente es Open Food Facts directo desde Android.
- Falta probar en dispositivo real el flujo completo: cambiar foto de perfil, publicar workout con series heterogéneas, abrir detalle del post y aplicar plan IA a Tracking.

---

## FatSecret + Cloud Functions: el agujero negro (29 abr – 4 may 2026) — DESCARTADO

### Decisión final

Después de todo lo que viene abajo, decidimos tirar la toalla con FatSecret y Cloud Functions y pasarnos a **Open Food Facts** (API pública, sin autenticación, llamada directa desde Android). Los alimentos que el usuario seleccione se guardan en nuestra Firestore directamente como si fueran alimentos propios — sin depender de ninguna API externa en runtime.

Las Cloud Functions (`searchFoods`, `getFoodDetail`) quedan borradas del proyecto.

### Nota de implementación Open Food Facts

La búsqueda de texto no debe usar `/api/v2/search?q=...`: ese endpoint v2 es para filtros estructurados y devuelve resultados generales si se le pasa `q`.

La primera integración usaba `cgi/search.pl`, pero desde la app se volvió demasiado lento/inestable (`503` y respuestas de ~27s para búsquedas normales como `yogur`). Se cambió a Search-a-licious, el buscador oficial de Open Food Facts:

```text
https://search.openfoodfacts.org/search?q=<query>&langs=es,en&page_size=20&fields=...
```

La integración Android limita `fields`, prioriza `product_name_es`, cachea resultados en memoria, evita llamadas con menos de 3 caracteres y serializa búsquedas externas con una espera local para no convertirlo en search-as-you-type agresivo.

---

### El camino completo del sufrimiento

#### 1. IAM en el primer deploy

`firebase deploy --only functions` falló con:

```
Build failed: Access to bucket gcf-sources-... denied.
You must grant Storage Object Viewer permission to
113203546446-compute@developer.gserviceaccount.com.
```

Fix: dar `roles/storage.objectViewer` a la service account de Compute desde la consola IAM. Al segundo intento desplegó.

#### 2. App Check sin inicializar

La dependencia `firebase-appcheck-playintegrity` estaba en `build.gradle` pero no había ni una línea de código que la arrancara. Sin `installAppCheckProviderFactory`, el proveedor nunca inicializa y el cliente adjunta tokens vacíos o incorrectos. Lo arreglamos añadiendo en `MainActivity.onCreate`:

```kotlin
FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
    if (BuildConfig.DEBUG) DebugAppCheckProviderFactory.getInstance()
    else PlayIntegrityAppCheckProviderFactory.getInstance()
)
```

#### 3. El catch silencioso

`AddFoodViewModel` tenía esto:

```kotlin
val fatSecretDeferred = async {
    try { FatSecretRepository.searchFoods(query) } catch (_: Exception) { emptyList() }
}
```

Cualquier error desaparecía sin dejar rastro. Lo cambiamos a `Log.e`. Lección: catch silencioso en features en desarrollo es veneno.

#### 4. UNAUTHENTICATED — el error que mentía

El log real tras quitar el catch:

```
E FatSecret: FirebaseFunctionsException: UNAUTHENTICATED
```

Pasamos horas pensando que era un problema de auth de Firebase. El usuario estaba logueado (`user=AHA162XzsdVbXYabs7TdXx4hExz2`), App Check parecía configurado... pero la función seguía rechazando.

La causa real era **Cloud IAM**: la función estaba desplegada pero Google Cloud no tenía a `allUsers` como `Cloud Functions Invoker`. Firebase CLI no asigna ese permiso automáticamente en proyectos nuevos. El SDK de Firebase envía el token de Firebase Auth (no un token de IAM de Google Cloud), Cloud lo rechazaba con 401, y el SDK mapeaba ese 401 a `UNAUTHENTICATED`. Perfecto para confundir.

Fix: desde Google Cloud Console → Cloud Functions → Permisos → añadir `allUsers` con rol `Cloud Functions Invoker`.

```bash
gcloud functions add-iam-policy-binding searchFoods \
  --region=us-central1 \
  --member="allUsers" \
  --role="roles/cloudfunctions.invoker"
```

#### 5. "Invalid signature" de FatSecret

Con el IAM arreglado, la función ya llegaba a FatSecret. Pero FatSecret devolvía:

```json
{"error":{"code":8,"message":"Invalid signature: please refer to the documentation"}}
```

La implementación usaba `https.get` con `qs.stringify` (el módulo `querystring` deprecado de Node). La codificación de caracteres en la URL no era 100% consistente con lo que FatSecret esperaba para verificar la firma OAuth 1.0a. Lo reescribimos para usar `fetch` con POST y `encodeURIComponent` directo — que es lo que FatSecret documenta en sus ejemplos — pero el problema persistió.

En ese punto, con cuatro capas de problemas encadenadas (IAM, App Check, auth SDK, firma OAuth), decidimos que FatSecret no valía la pena para lo que necesitábamos.

### Lo que me llevo

- Si Firebase CLI no dice explícitamente que está configurando permisos públicos, compruébalo tú en Cloud Console. No lo da por sentado.
- `UNAUTHENTICATED` de Firebase Functions puede significar cosas muy distintas: auth de la app, App Check, o IAM de Google Cloud. Antes de tocar el código, descarta capas con `curl` directo a la URL de la función.
- OAuth 1.0a es frágil. Cualquier diferencia mínima en codificación de caracteres rompe la firma. Si puedes evitarlo, evítalo.
- Para una app de fitness, Open Food Facts da lo mismo que FatSecret sin ninguno de estos marrones.

---

## El crash del foreground service al iniciar workout (28 abr 2026)

### Qué pasaba

Refactoricé todo el flujo de workouts: ahora hay un `ActiveWorkoutManager` singleton que arranca el cronómetro solo, levanta una notificación persistente y un banner global por toda la app. Hasta ahí bonito en simulador.

Cojo el Moto G05 (Android 14, API 36) y al darle a "Iniciar"… petazo. La app se cierra entera.

Lo metí por `adb logcat` y salió esto:

```
FATAL EXCEPTION: main
java.lang.SecurityException: Starting FGS with type health
  callerApp=ProcessRecord{... com.example.fitfusion} targetSDK=36
  requires permissions:
    all of the permissions allOf=true [android.permission.FOREGROUND_SERVICE_HEALTH]
    any of the permissions allOf=false [
      android.permission.ACTIVITY_RECOGNITION,
      android.permission.BODY_SENSORS,
      android.permission.HIGH_SAMPLING_RATE_SENSORS
    ]
  at com.example.fitfusion.service.WorkoutForegroundService.startForegroundCompat
```

### Por qué

A partir de Android 14, si declaras un foreground service con `foregroundServiceType="health"` el sistema te exige `FOREGROUND_SERVICE_HEALTH` **y** uno de los permisos de sensores (actividad física, ritmo cardíaco, etc). Yo había puesto `health` porque "es un workout, va de salud", pero la app no lee sensores en directo, solo lleva el cronómetro y enseña la noti.

Resultado: petición no autorizada → `SecurityException` → crash.

### Cómo lo arreglé

Cambié el tipo a `dataSync`, que no pide permisos extra y deja correr el servicio hasta 6h de cada 24h (de sobra para un entreno).

Manifest:

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC"/>
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>

<service
    android:name=".service.WorkoutForegroundService"
    android:foregroundServiceType="dataSync"
    android:exported="false"/>
```

Y en el servicio, la llamada a `startForeground` con el tipo correcto en API 34+:

```kotlin
private fun startForegroundCompat(notification: Notification) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        startForeground(
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
        )
    } else {
        startForeground(NOTIFICATION_ID, notification)
    }
}
```

Verificado en el Moto G05: build, install, "Iniciar", abre la pantalla activa con cronómetro corriendo, noti permanente arriba, y `dumpsys activity services com.example.fitfusion` muestra `types=0x00000001` (dataSync). Sin `SecurityException` en logcat.

### Lo que me llevo

- El `foregroundServiceType` no es decorativo, mete restricciones de permisos detrás. Cuando lo elijas, asegúrate de que la app realmente cumple lo que ese tipo promete.
- `dataSync` es el "default razonable" para tareas que solo necesitan vivir en background con noti, sin tocar hardware sensible.
- En Android 14+ siempre probar en device real, el simulador se traga muchas cosas que el sistema sí filtra.

---

## La sheet de log workout que se cerraba sola (mismo día)

### Qué pasaba

El flujo viejo era: añades ejercicios → se abre un `ModalBottomSheet` con el cronómetro y los sets. Si tocabas fuera o arrastrabas, la sheet se cerraba y perdías el estado del entreno. Una mierda.

### Cómo lo arreglé

Lo saqué de la sheet y lo subí a una pantalla full-screen propia (`ActiveWorkoutScreen`). Y todo el estado lo movi a un singleton `ActiveWorkoutManager` para que sobreviva a navegación y rotaciones:

```kotlin
object ActiveWorkoutManager {

    private val _session = MutableStateFlow<ActiveWorkoutSession?>(null)
    val session: StateFlow<ActiveWorkoutSession?> = _session.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    fun startSession(name: String, exercises: List<ExerciseCatalogItem>) {
        if (_session.value != null) return
        val now = System.currentTimeMillis()
        _session.value = ActiveWorkoutSession(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { buildAutoName(exercises) },
            startedAtMs = now,
            accumulatedSeconds = 0L,
            resumedAtMs = now,
            isPaused = false,
            exercises = exercises.map { it.toActiveEntry() },
        )
        startTicker()
        appContext?.let { WorkoutForegroundService.start(it) }
    }
    // ...
}
```

Los ViewModels solo observan, no duplican estado. Y el banner global (`ActiveWorkoutBanner`) lee directamente del manager, así si te vas a Home o Tracking sigues viendo el cronómetro arriba.

### Lo que me llevo

- Bottom sheets están bien para acciones cortas. Para algo de varios minutos en el que el usuario se va a mover por la app, full-screen + estado en singleton.
- Si dos pantallas necesitan el mismo estado vivo, no pongas el estado en un VM, pónlo más abajo.

---

## FatSecret y el "no se puede llamar desde Android" (27 abr 2026)

### Qué pasaba

FatSecret usa OAuth 1.0a con secret. Si lo metes en el cliente Android, cualquiera con `apktool` te saca las credenciales. Y aparte, FatSecret tiene una whitelist de IPs por servidor.

### Cómo

Cloud Functions (callable) hacen de proxy:

```js
exports.searchFoods = onCall(async (req) => {
  const { query, maxResults = 20 } = req.data;
  const oauthParams = buildOAuthParams(
    process.env.FATSECRET_KEY,
    process.env.FATSECRET_SECRET,
    "foods.search",
    { search_expression: query, max_results: maxResults }
  );
  const res = await fetch("https://platform.fatsecret.com/rest/server.api", {
    method: "POST",
    body: new URLSearchParams(oauthParams),
  });
  return normalize(await res.json());
});
```

Y en Android, el cliente solo llama la callable:

```kotlin
object FatSecretRepository {
    private val functions = Firebase.functions
    suspend fun searchFoods(query: String, maxResults: Int = 20): List<Food> {
        val data = mapOf("query" to query, "maxResults" to maxResults)
        val result = functions.getHttpsCallable("searchFoods").call(data).await()
        return parse(result.data)
    }
}
```

### Lo que me llevo

- Cualquier credencial que no sea pública vive en backend, fin del debate.
- Pendiente: deploy real de las functions cuando active Blaze. Mientras tanto la búsqueda FatSecret en `AddFoodViewModel` falla en runtime, pero la búsqueda local (Algolia / Firestore) sigue tirando.

---

## DailySummaries pisándose entre fuentes

### Qué pasaba

Tres sitios escriben en `users/{uid}/dailySummaries/{yyyy-MM-dd}`: comida, workouts y Health Connect. La primera versión hacía `.set(doc)` plano y cada una se cargaba los campos de las otras dos.

### Cómo

`SetOptions.merge()` y partir el escritor en tres funciones que solo tocan lo suyo:

```kotlin
suspend fun mergeFoodSummary(
    date: LocalDate,
    kcalConsumed: Int, proteinG: Int, carbsG: Int, fatG: Int,
) {
    val ref = userDoc()?.collection("dailySummaries")
        ?.document(date.toString()) ?: return
    val data = mapOf(
        "date" to date.toString(),
        "kcalConsumed" to kcalConsumed,
        "proteinG" to proteinG,
        "carbsG" to carbsG,
        "fatG" to fatG,
        "updatedAt" to FieldValue.serverTimestamp(),
    )
    ref.set(data, SetOptions.merge()).await()
}
```

Lo mismo para `mergeWorkoutSummary` y la parte de health. Cada repo dispara su `pushXxxSummary` después de cada write y el doc consolidado siempre tiene la foto completa del día sin pisarse.

### Lo que me llevo

- Si más de un caller escribe en el mismo doc, `merge()` por defecto siempre.
- Mejor un doc por día con `set+merge` que un montón de docs pequeños que luego hay que agregar en el cliente.

---

## FoodRepository que era todo mock (27 abr 2026)

### Qué pasaba

`FoodRepository` tenía una lista hardcoded de 12 alimentos in-memory. Funcionaba para demo pero al reiniciar la app se perdía todo.

### Cómo

Lo reescribí entero con el mismo patrón que `WorkoutRepository`: `object` singleton, `AuthStateListener` para enganchar/desenganchar listener al cambiar de usuario, y colección `users/{uid}/foodLogs/{logId}` con un doc por entrada.

```kotlin
object FoodRepository {
    private var registration: ListenerRegistration? = null

    init {
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            registration?.remove()
            registration = auth.currentUser?.let { user ->
                Firebase.firestore
                    .collection("users").document(user.uid)
                    .collection("foodLogs")
                    .addSnapshotListener { snap, _ ->
                        snap?.let { rebuildDayLogs(it.documents) }
                    }
            }
        }
    }
    // ...
}
```

`addFood`/`removeFood`/`updateFood` ahora son `suspend`, escriben en Firestore y llaman `pushFoodSummary(date)` para alimentar `dailySummaries`.

### Lo que me llevo

- Auth listener para enganchar el listener Firestore es el patrón que mejor me funciona en singletons. Si conviertes el singleton en clase normal, hay que rehacerlo.
- `suspend` se contagia: la cadena de callers también pasa a `suspend`/`viewModelScope.launch`. Asumirlo desde el principio.
