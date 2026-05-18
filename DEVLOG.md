# Devlog FitFusion

Aquí voy apuntando los marrones que me he ido encontrando y cómo los he resuelto. No es documentación oficial, es más un cuaderno para no tropezar dos veces con la misma piedra.

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

- No hay backend .NET dentro de este repo; Android solo tiene contrato/cliente `AiRepository` y espera `AI_API_BASE_URL`. Para producción falta asegurar despliegue HTTPS real del backend IA y configuración de `local.properties`/variables.
- No hay `functions/` ni Cloud Functions actuales; la documentación vieja de FatSecret quedó obsoleta. La ruta vigente es Open Food Facts directo desde Android.
- Falta probar en dispositivo real el flujo completo: cambiar foto de perfil, publicar workout con series heterogéneas, abrir detalle del post y aplicar plan IA a Tracking.

---

## FatSecret + Cloud Functions: el agujero negro (29 abr – 4 may 2026) — DESCARTADO

### Decisión final

Después de todo lo que viene abajo, decidimos tirar la toalla con FatSecret y Cloud Functions y pasarnos a **Open Food Facts** (API pública, sin autenticación, llamada directa desde Android). Los alimentos que el usuario seleccione se guardan en nuestra Firestore directamente como si fueran alimentos propios — sin depender de ninguna API externa en runtime.

Las Cloud Functions (`searchFoods`, `getFoodDetail`) quedan borradas del proyecto.

### Nota de implementación Open Food Facts

La búsqueda de texto no debe usar `/api/v2/search?q=...`: ese endpoint v2 es para filtros estructurados y devuelve resultados generales si se le pasa `q`. Para buscar por nombre/marca usamos:

```text
https://es.openfoodfacts.org/cgi/search.pl?search_terms=<query>&search_simple=1&action=process&json=1
```

La integración Android limita `fields`, prioriza `product_name_es`, ordena por popularidad cuando el endpoint lo permite, cachea resultados en memoria y evita llamadas con menos de 3 caracteres para no chocar con los rate limits públicos de Open Food Facts.

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
