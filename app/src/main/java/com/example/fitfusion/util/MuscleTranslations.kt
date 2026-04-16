package com.example.fitfusion.util

/**
 * Traducciones de nombres de músculos (latín/inglés → idioma de la app).
 *
 * Las claves están en minúsculas para permitir comparación insensible a mayúsculas.
 * Para añadir un nuevo idioma, crea un mapa equivalente y pásalo en [translate].
 */
object MuscleTranslations {

    private val spanish: Map<String, String> = mapOf(
        // ── "Todos" especial ────────────────────────────────────────────────
        "todos"                     to "Todos",
        "all"                       to "Todos",
        "other"                     to "Otros",
        "others"                    to "Otros",

        // ── Grupos amplios (muscleGroup) ────────────────────────────────────
        "chest"                     to "Pecho",
        "back"                      to "Espalda",
        "shoulders"                 to "Hombros",
        "arms"                      to "Brazos",
        "upper arms"                to "Parte superior del brazo",
        "lower arms"                to "Antebrazos",
        "forearms"                  to "Antebrazos",
        "core"                      to "Core / Abdomen",
        "legs"                      to "Piernas",
        "glutes"                    to "Glúteos",
        "calves"                    to "Pantorrillas",
        "calf"                      to "Pantorrilla",
        "neck"                      to "Cuello",
        "hips"                      to "Caderas",
        "upper body"                to "Tren superior",
        "lower body"                to "Tren inferior",
        "full body"                 to "Cuerpo completo",
        "push muscles"              to "Músculos de empuje",
        "pull muscles"              to "Músculos de tirón",
        "cardio"                    to "Cardio",
        "olympic"                   to "Olímpico",
        "powerlifting"              to "Powerlifting",

        // ── Pecho — inglés informal / regional ──────────────────────────────
        "pectorals"                 to "Pectorales",
        "pecs"                      to "Pectorales",
        "upper chest"               to "Pectoral superior",
        "lower chest"               to "Pectoral inferior",
        "inner chest"               to "Pectoral interno",
        "outer chest"               to "Pectoral externo",
        "chest (upper)"             to "Pectoral superior",
        "chest (lower)"             to "Pectoral inferior",
        // latín
        "pectoralis major"          to "Pectoral mayor",
        "pectoralis minor"          to "Pectoral menor",
        "pec major"                 to "Pectoral mayor",
        "pec minor"                 to "Pectoral menor",
        "serratus anterior"         to "Serrato anterior",
        "serratus"                  to "Serrato anterior",

        // ── Espalda — inglés informal / regional ────────────────────────────
        "upper back"                to "Espalda superior",
        "mid back"                  to "Espalda media",
        "middle back"               to "Espalda media",
        "lower back"                to "Zona lumbar",
        "lower back muscles"        to "Zona lumbar",
        "spinal erectors"           to "Erectores de la columna",
        "spinal extensors"          to "Extensores de la columna",
        // latín
        "latissimus dorsi"          to "Dorsal ancho",
        "lats"                      to "Dorsales",
        "trapezius"                 to "Trapecio",
        "traps"                     to "Trapecio",
        "upper trapezius"           to "Trapecio superior",
        "middle trapezius"          to "Trapecio medio",
        "lower trapezius"           to "Trapecio inferior",
        "rhomboids"                 to "Romboides",
        "rhomboid major"            to "Romboides mayor",
        "rhomboid minor"            to "Romboides menor",
        "erector spinae"            to "Erectores espinales",
        "multifidus"                to "Multífido",
        "quadratus lumborum"        to "Cuadrado lumbar",
        "teres major"               to "Redondo mayor",
        "teres minor"               to "Redondo menor",
        "infraspinatus"             to "Infraespinoso",
        "supraspinatus"             to "Supraespinoso",
        "subscapularis"             to "Subescapular",
        "rotator cuff"              to "Manguito rotador",
        "shoulder blade"            to "Escápula",
        "shoulder girdle"           to "Cintura escapular",

        // ── Hombros — inglés informal ───────────────────────────────────────
        "delts"                     to "Deltoides",
        "deltoids"                  to "Deltoides",
        "front shoulders"           to "Deltoides anterior",
        "side shoulders"            to "Deltoides lateral",
        "rear shoulders"            to "Deltoides posterior",
        "front raises"              to "Deltoides anterior",
        // latín
        "deltoid"                   to "Deltoides",
        "anterior deltoid"          to "Deltoides anterior",
        "lateral deltoid"           to "Deltoides lateral",
        "posterior deltoid"         to "Deltoides posterior",
        "front deltoid"             to "Deltoides anterior",
        "side deltoid"              to "Deltoides lateral",
        "rear deltoid"              to "Deltoides posterior",

        // ── Brazos — inglés informal ────────────────────────────────────────
        "upper arm"                 to "Parte superior del brazo",
        "lower arm"                 to "Antebrazo",
        "forearm"                   to "Antebrazo",
        "finger flexors"            to "Flexores de dedos",
        "finger extensors"          to "Extensores de dedos",
        "grip"                      to "Agarre / Antebrazo",
        // latín
        "biceps brachii"            to "Bíceps",
        "biceps"                    to "Bíceps",
        "triceps brachii"           to "Tríceps",
        "triceps"                   to "Tríceps",
        "brachialis"                to "Braquial",
        "brachioradialis"           to "Braquiorradial",
        "wrist flexors"             to "Flexores de muñeca",
        "wrist extensors"           to "Extensores de muñeca",
        "flexor carpi radialis"     to "Flexor carpi radial",
        "flexor carpi ulnaris"      to "Flexor carpi cubital",
        "extensor carpi radialis"   to "Extensor carpi radial",

        // ── Core / Abdomen — inglés informal ────────────────────────────────
        "abdominals"                to "Abdominales",
        "abs"                       to "Abdominales",
        "upper abs"                 to "Abdominales superiores",
        "lower abs"                 to "Abdominales inferiores",
        "side abs"                  to "Oblicuos",
        "love handles"              to "Oblicuos",
        "stomach"                   to "Abdominales",
        "midsection"                to "Core / Abdomen",
        "pelvic floor"              to "Suelo pélvico",
        // latín
        "rectus abdominis"          to "Abdominales",
        "obliques"                  to "Oblicuos",
        "external obliques"         to "Oblicuos externos",
        "internal obliques"         to "Oblicuos internos",
        "transversus abdominis"     to "Transverso abdominal",
        "transverse abdominis"      to "Transverso abdominal",
        "diaphragm"                 to "Diafragma",

        // ── Piernas — inglés informal ────────────────────────────────────────
        "quads"                     to "Cuádriceps",
        "hams"                      to "Isquiotibiales",
        "thighs"                    to "Muslos",
        "inner thigh"               to "Cara interna del muslo",
        "outer thigh"               to "Cara externa del muslo",
        "knee flexors"              to "Flexores de rodilla",
        "knee extensors"            to "Extensores de rodilla",
        "hip extensors"             to "Extensores de cadera",
        "hip rotators"              to "Rotadores de cadera",
        "hip abductors"             to "Abductores de cadera",
        "hip adductors"             to "Aductores de cadera",
        "shin"                      to "Tibial anterior",
        "shins"                     to "Tibial anterior",
        // latín
        "quadriceps"                to "Cuádriceps",
        "rectus femoris"            to "Recto femoral",
        "vastus lateralis"          to "Vasto lateral",
        "vastus medialis"           to "Vasto medial",
        "vastus intermedius"        to "Vasto intermedio",
        "hamstrings"                to "Isquiotibiales",
        "biceps femoris"            to "Bíceps femoral",
        "semitendinosus"            to "Semitendinoso",
        "semimembranosus"           to "Semimembranoso",
        "adductors"                 to "Aductores",
        "abductors"                 to "Abductores",
        "hip flexors"               to "Flexores de cadera",
        "iliopsoas"                 to "Iliopsoas",
        "iliacus"                   to "Ilíaco",
        "psoas major"               to "Psoas mayor",
        "tensor fasciae latae"      to "Tensor de la fascia lata",
        "sartorius"                 to "Sartorio",

        // ── Glúteos — inglés informal ────────────────────────────────────────
        "glute"                     to "Glúteo",
        "butt"                      to "Glúteos",
        "buttocks"                  to "Glúteos",
        "glute max"                 to "Glúteo mayor",
        "glute med"                 to "Glúteo medio",
        "glute min"                 to "Glúteo menor",
        // latín
        "gluteus maximus"           to "Glúteo mayor",
        "gluteus medius"            to "Glúteo medio",
        "gluteus minimus"           to "Glúteo menor",
        "piriformis"                to "Piriforme",

        // ── Pantorrillas — inglés informal ───────────────────────────────────
        "calves muscles"            to "Pantorrillas",
        "ankle flexors"             to "Flexores de tobillo",
        "ankle extensors"           to "Extensores de tobillo",
        "ankle"                     to "Tobillo",
        // latín
        "gastrocnemius"             to "Gemelos",
        "soleus"                    to "Sóleo",
        "tibialis anterior"         to "Tibial anterior",
        "peroneus longus"           to "Peroneo largo",
        "peroneus brevis"           to "Peroneo corto",
        "fibularis longus"          to "Peroneo largo",
        "fibularis brevis"          to "Peroneo corto",

        // ── Cuello — inglés ───────────────────────────────────────────────────
        "neck flexors"              to "Flexores del cuello",
        "neck extensors"            to "Extensores del cuello",
        "sternocleidomastoid"       to "Esternocleidomastoideo",
        "scm"                       to "Esternocleidomastoideo",
        "levator scapulae"          to "Elevador de la escápula",
    )

    // Claves que no deben usarse como filtro de categoría en Firestore
    private val nonFilterableKeys = setOf("todos", "all", "other", "others")

    /**
     * Devuelve el nombre localizado del músculo.
     * Si no existe traducción, aplica Title Case al valor original como fallback.
     *
     * @param key   Nombre del músculo tal como viene de Firebase (latín/inglés).
     * @param locale Código de idioma ("es" por defecto).
     */
    fun translate(key: String, locale: String = "es"): String {
        val map = when (locale) {
            "es" -> spanish
            else -> spanish  // Fallback a español hasta añadir más idiomas
        }
        return map[key.trim().lowercase()]
            ?: key.trim().split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercaseChar() }
            }
    }

    /**
     * Traducción inversa: dado un nombre en el idioma de la app, devuelve la clave
     * en inglés que se almacena en Firestore. Devuelve null si no hay coincidencia
     * o si la clave es un comodín como "Todos" / "Otros".
     *
     * Ejemplo: reverseTranslate("Pecho") → "chest"
     *
     * @param value  Nombre tal como lo muestra la UI (e.g. "Pecho", "Espalda").
     * @param locale Código de idioma ("es" por defecto).
     */
    fun reverseTranslate(value: String, locale: String = "es"): String? {
        val map = when (locale) {
            "es" -> spanish
            else -> spanish
        }
        val normalized = value.trim().lowercase()
        return map.entries
            .firstOrNull { (key, translation) ->
                key !in nonFilterableKeys && translation.lowercase() == normalized
            }
            ?.key
    }
}