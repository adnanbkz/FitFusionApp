package com.example.fitfusion.util

/**
 * Traducciones de nombres de equipamiento (inglés → idioma de la app).
 *
 * Las claves están en minúsculas para comparación insensible a mayúsculas.
 * Para añadir un nuevo idioma, crea un mapa equivalente y añade un case en [translate].
 */
object EquipmentTranslations {

    private val spanish: Map<String, String> = mapOf(
        // ── Sin equipamiento ────────────────────────────────────────────────
        "bodyweight"                to "Peso corporal",
        "body weight"               to "Peso corporal",
        "no equipment"              to "Sin equipamiento",
        "none"                      to "Sin equipamiento",

        // ── Pesas libres ─────────────────────────────────────────────────────
        "barbell"                   to "Barra",
        "bar"                       to "Barra",
        "dumbbell"                  to "Mancuerna",
        "dumbbells"                 to "Mancuernas",
        "db"                        to "Mancuerna",
        "kettlebell"                to "Kettlebell",
        "kettlebells"               to "Kettlebells",
        "kb"                        to "Kettlebell",
        "medicine ball"             to "Balón medicinal",
        "med ball"                  to "Balón medicinal",
        "sandbag"                   to "Saco de arena",
        "weight plate"              to "Disco",
        "plates"                    to "Discos",
        "plate"                     to "Disco",
        "weight vest"               to "Chaleco lastrado",
        "weighted vest"             to "Chaleco lastrado",
        "ankle weights"             to "Tobilleras con peso",

        // ── Barras especiales ─────────────────────────────────────────────────
        "ez bar"                    to "Barra EZ",
        "ez-bar"                    to "Barra EZ",
        "curl bar"                  to "Barra EZ",
        "trap bar"                  to "Barra trampa",
        "hex bar"                   to "Barra hexagonal",
        "safety bar"                to "Barra de seguridad",
        "safety squat bar"          to "Barra sentadilla segura",
        "cambered bar"              to "Barra arqueada",
        "log bar"                   to "Barra log",
        "axle bar"                  to "Barra axle",
        "swiss bar"                 to "Barra suiza",
        "football bar"              to "Barra football",

        // ── Máquinas ──────────────────────────────────────────────────────────
        "machine"                   to "Máquina",
        "machines"                  to "Máquinas",
        "smith machine"             to "Máquina Smith",
        "cable"                     to "Polea",
        "cables"                    to "Poleas",
        "cable machine"             to "Máquina de poleas",
        "cable tower"               to "Torre de poleas",
        "functional trainer"        to "Entrenador funcional",
        "leg press"                 to "Prensa de piernas",
        "leg press machine"         to "Prensa de piernas",
        "hack squat machine"        to "Máquina hack squat",
        "leg extension machine"     to "Máquina de extensión de piernas",
        "leg curl machine"          to "Máquina de curl de piernas",
        "seated leg curl"           to "Curl femoral sentado",
        "lying leg curl"            to "Curl femoral tumbado",
        "calf raise machine"        to "Máquina de elevación de talones",
        "seated calf raise"         to "Elevación de talones sentado",
        "standing calf raise"       to "Elevación de talones de pie",
        "chest press machine"       to "Máquina de press de pecho",
        "pec deck"                  to "Pec deck",
        "pec deck machine"          to "Máquina pec deck",
        "fly machine"               to "Máquina de aperturas",
        "lat pulldown machine"      to "Máquina jalón al pecho",
        "seated row machine"        to "Máquina de remo sentado",
        "back extension machine"    to "Máquina de extensión lumbar",
        "hyperextension machine"    to "Máquina de hiperextensión",
        "shoulder press machine"    to "Máquina de press de hombros",
        "lateral raise machine"     to "Máquina de elevaciones laterales",
        "bicep curl machine"        to "Máquina de curl de bíceps",
        "tricep extension machine"  to "Máquina de extensión de tríceps",
        "preacher curl machine"     to "Máquina predicador",
        "abductor machine"          to "Máquina abductora",
        "adductor machine"          to "Máquina aductora",
        "hip thrust machine"        to "Máquina de hip thrust",
        "glute machine"             to "Máquina de glúteos",
        "assisted pull-up machine"  to "Máquina de dominadas asistidas",
        "assisted dip machine"      to "Máquina de fondos asistidos",
        "rowing machine"            to "Máquina de remo",
        "ski erg"                   to "Ski erg",
        "air bike"                  to "Bicicleta air bike",
        "assault bike"              to "Assault bike",
        "stationary bike"           to "Bicicleta estática",
        "treadmill"                 to "Cinta de correr",
        "elliptical"                to "Elíptica",
        "stair climber"             to "Escaladora",
        "stairmaster"               to "StairMaster",
        "ski machine"               to "Máquina de esquí",

        // ── Accesorios y equipamiento de soporte ──────────────────────────────
        "bench"                     to "Banco",
        "flat bench"                to "Banco plano",
        "incline bench"             to "Banco inclinado",
        "decline bench"             to "Banco declinado",
        "adjustable bench"          to "Banco ajustable",
        "preacher bench"            to "Banco predicador",
        "pull-up bar"               to "Barra de dominadas",
        "pullup bar"                to "Barra de dominadas",
        "chin-up bar"               to "Barra de dominadas",
        "dip bar"                   to "Barras paralelas",
        "dip bars"                  to "Barras paralelas",
        "parallel bars"             to "Barras paralelas",
        "gymnastics rings"          to "Anillas",
        "rings"                     to "Anillas",
        "trx"                       to "TRX",
        "suspension trainer"        to "Entrenador en suspensión",
        "resistance band"           to "Banda elástica",
        "resistance bands"          to "Bandas elásticas",
        "bands"                     to "Bandas elásticas",
        "band"                      to "Banda elástica",
        "loop band"                 to "Banda de bucle",
        "mini band"                 to "Mini banda",
        "pull-up band"              to "Banda para dominadas",
        "foam roller"               to "Rodillo de espuma",
        "ab wheel"                  to "Rueda abdominal",
        "ab roller"                 to "Rueda abdominal",
        "bosu ball"                 to "Bosu",
        "stability ball"            to "Pelota de estabilidad",
        "swiss ball"                to "Pelota suiza",
        "exercise ball"             to "Pelota de ejercicio",
        "balance board"             to "Tabla de equilibrio",
        "step"                      to "Step",
        "aerobic step"              to "Step aeróbico",
        "box"                       to "Cajón pliométrico",
        "plyo box"                  to "Cajón pliométrico",
        "plyometric box"            to "Cajón pliométrico",
        "rope"                      to "Cuerda",
        "jump rope"                 to "Comba",
        "battle rope"               to "Cuerda de batalla",
        "battle ropes"              to "Cuerdas de batalla",
        "pull-up station"           to "Estación de dominadas",
        "power rack"                to "Jaula de potencia",
        "squat rack"                to "Rack de sentadillas",
        "rack"                      to "Rack",
        "cage"                      to "Jaula",

        // ── Equipamiento especializado (CrossFit / funcional) ─────────────────
        "landmine"                  to "Landmine",
        "t-bar"                     to "Barra T",
        "t-bar row"                 to "Remo en T",
        "sled"                      to "Trineo",
        "prowler"                   to "Prowler",
        "tire"                      to "Neumático",
        "chains"                    to "Cadenas",
        "atlas stone"               to "Piedra atlas",
        "yoke"                      to "Yugo",
        "log"                       to "Tronco",
        "drag sled"                 to "Trineo de arrastre",
        "erg"                       to "Ergómetro",
        "concept2"                  to "Concept2",
        "rower"                     to "Máquina de remo",

        // ── Otro ─────────────────────────────────────────────────────────────
        "other"                     to "Otro",
        "others"                    to "Otros",
        "various"                   to "Varios",
    )

    /**
     * Devuelve el nombre localizado del equipamiento.
     * Si no existe traducción, aplica Title Case al valor original como fallback.
     *
     * @param key    Nombre del equipamiento tal como viene de Firebase (inglés).
     * @param locale Código de idioma ("es" por defecto).
     */
    fun translate(key: String, locale: String = "es"): String {
        val map = when (locale) {
            "es" -> spanish
            else -> spanish
        }
        return map[key.trim().lowercase()]
            ?: key.trim().split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercaseChar() }
            }
    }
}
