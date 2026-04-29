# Devlog FitFusion

Aquí voy apuntando los marrones que me he ido encontrando y cómo los he resuelto. No es documentación oficial, es más un cuaderno para no tropezar dos veces con la misma piedra.

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
