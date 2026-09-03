package com.diegoguerrero.futtracker.domain.model

enum class Posicion { POR, DFC, LI, LD, MC, EI, ED, DC }

enum class TipoFutbol(val nJugadoresCampo: Int) {
    FUTSAL(5), // 4 de campo + 1 POR
    FUT_6(6),  // 5 de campo + 1 POR
    FUT_7(7)   // 6 de campo + 1 POR
}

data class Formacion(
    val id: String,
    val nombre: String,
    val tipo: TipoFutbol,
    val posicionesRequeridas: List<Posicion>
)

// --- FUTBOL SALA (4 + POR) ---
val FORMACIONES_FUTSAL = listOf(
    Formacion(
        id = "FUTSAL_1_2_1",
        nombre = "1-2-1",
        tipo = TipoFutbol.FUTSAL,
        posicionesRequeridas = listOf(Posicion.DFC, Posicion.EI, Posicion.ED, Posicion.DC)
    ),
    Formacion(
        id = "FUTSAL_2_2_0",
        nombre = "2-2-0",
        tipo = TipoFutbol.FUTSAL,
        posicionesRequeridas = listOf(Posicion.DFC, Posicion.DFC, Posicion.DC, Posicion.DC)
    ),
    Formacion(
        id = "FUTSAL_3_0_1",
        nombre = "3-0-1",
        tipo = TipoFutbol.FUTSAL,
        posicionesRequeridas = listOf(Posicion.LI, Posicion.DFC, Posicion.LD, Posicion.DC)
    ),
    Formacion(
        id = "FUTSAL_2_1_1",
        nombre = "2-1-1",
        tipo = TipoFutbol.FUTSAL,
        posicionesRequeridas = listOf(Posicion.DFC, Posicion.DFC, Posicion.MC, Posicion.DC)
    )
).sortedBy { it.nombre }

// --- FUTBOL 6 (5 + POR) ---
val FORMACIONES_FUT_6 = listOf(
    Formacion(
        id = "FUT6_3_1_1",
        nombre = "3-1-1",
        tipo = TipoFutbol.FUT_6,
        posicionesRequeridas = listOf(Posicion.LI, Posicion.DFC, Posicion.LD, Posicion.MC, Posicion.DC)
    ),
    Formacion(
        id = "FUT6_3_2_0",
        nombre = "3-2-0",
        tipo = TipoFutbol.FUT_6,
        posicionesRequeridas = listOf(Posicion.LI, Posicion.DFC, Posicion.LD, Posicion.DC, Posicion.DC)
    ),
    Formacion(
        id = "FUT6_2_2_1",
        nombre = "2-2-1",
        tipo = TipoFutbol.FUT_6,
        posicionesRequeridas = listOf(Posicion.DFC, Posicion.DFC, Posicion.EI, Posicion.ED, Posicion.DC)
    ),
    Formacion(
        id = "FUT6_1_3_1",
        nombre = "1-3-1",
        tipo = TipoFutbol.FUT_6,
        posicionesRequeridas = listOf(Posicion.DFC, Posicion.EI, Posicion.MC, Posicion.ED, Posicion.DC)
    ),
    Formacion(
        id = "FUT6_2_3_0",
        nombre = "2-3-0",
        tipo = TipoFutbol.FUT_6,
        posicionesRequeridas = listOf(Posicion.DFC, Posicion.DFC, Posicion.EI, Posicion.MC, Posicion.ED)
    )
).sortedBy { it.nombre }

// --- FUTBOL 7 (6 + POR) ---
val FORMACIONES_FUT_7 = listOf(
    Formacion(
        id = "FUT7_2_3_1",
        nombre = "2-3-1",
        tipo = TipoFutbol.FUT_7,
        posicionesRequeridas = listOf(Posicion.DFC, Posicion.DFC, Posicion.EI, Posicion.MC, Posicion.ED, Posicion.DC)
    ),
    Formacion(
        id = "FUT7_2_2_2",
        nombre = "2-2-2",
        tipo = TipoFutbol.FUT_7,
        posicionesRequeridas = listOf(Posicion.DFC, Posicion.DFC, Posicion.EI, Posicion.ED, Posicion.DC, Posicion.DC)
    ),
    Formacion(
        id = "FUT7_3_2_1_CERRADA",
        nombre = "3-2-1 Cerrada",
        tipo = TipoFutbol.FUT_7,
        posicionesRequeridas = listOf(Posicion.LI, Posicion.DFC, Posicion.LD, Posicion.MC, Posicion.MC, Posicion.DC)
    ),
    Formacion(
        id = "FUT7_3_2_1_ABIERTA",
        nombre = "3-2-1 Abierta",
        tipo = TipoFutbol.FUT_7,
        posicionesRequeridas = listOf(Posicion.LI, Posicion.DFC, Posicion.LD, Posicion.EI, Posicion.ED, Posicion.DC)
    ),
    Formacion(
        id = "FUT7_4_1_1",
        nombre = "4-1-1",
        tipo = TipoFutbol.FUT_7,
        posicionesRequeridas = listOf(Posicion.LI, Posicion.DFC, Posicion.DFC, Posicion.LD, Posicion.MC, Posicion.DC)
    ),
    Formacion(
        id = "FUT7_3_3_0",
        nombre = "3-3-0",
        tipo = TipoFutbol.FUT_7,
        posicionesRequeridas = listOf(Posicion.LI, Posicion.DFC, Posicion.LD, Posicion.EI, Posicion.MC, Posicion.ED)
    ),
    Formacion(
        id = "FUT7_4_2_0",
        nombre = "4-2-0",
        tipo = TipoFutbol.FUT_7,
        posicionesRequeridas = listOf(Posicion.LI, Posicion.DFC, Posicion.DFC, Posicion.LD, Posicion.MC, Posicion.MC)
    ),
    Formacion(
        id = "FUT7_1_3_2",
        nombre = "1-3-2",
        tipo = TipoFutbol.FUT_7,
        posicionesRequeridas = listOf(Posicion.DFC, Posicion.EI, Posicion.MC, Posicion.ED, Posicion.DC, Posicion.DC)
    )
).sortedBy { it.nombre }

fun obtenerCoordenadas(formacion: Formacion): List<Pair<Posicion, Pair<Float, Float>>> {
    return when (formacion.id) {
        // --- FUTBOL SALA ---
        "FUTSAL_1_2_1" -> listOf(
            Posicion.POR to (0.5f to 0.88f),
            Posicion.DFC to (0.5f to 0.65f),
            Posicion.EI  to (0.25f to 0.42f),
            Posicion.ED  to (0.75f to 0.42f),
            Posicion.DC  to (0.5f to 0.18f)
        )
        "FUTSAL_2_2_0" -> listOf(
            Posicion.POR to (0.5f to 0.88f),
            Posicion.DFC to (0.32f to 0.65f),
            Posicion.DFC to (0.68f to 0.65f),
            Posicion.DC  to (0.35f to 0.22f),
            Posicion.DC  to (0.65f to 0.22f)
        )
        "FUTSAL_3_0_1" -> listOf(
            Posicion.POR to (0.5f to 0.88f),
            Posicion.LI  to (0.20f to 0.65f),
            Posicion.DFC to (0.50f to 0.68f),
            Posicion.LD  to (0.80f to 0.65f),
            Posicion.DC  to (0.50f to 0.20f)
        )
        "FUTSAL_2_1_1" -> listOf(
            Posicion.POR to (0.5f to 0.88f),
            Posicion.DFC to (0.32f to 0.65f),
            Posicion.DFC to (0.68f to 0.65f),
            Posicion.MC  to (0.50f to 0.45f),
            Posicion.DC  to (0.50f to 0.20f)
        )

        // --- FUTBOL 6 ---
        "FUT6_3_1_1" -> listOf(
            Posicion.POR to (0.5f to 0.88f),
            Posicion.LI  to (0.2f to 0.65f),
            Posicion.DFC to (0.5f to 0.68f),
            Posicion.LD  to (0.8f to 0.65f),
            Posicion.MC  to (0.5f to 0.45f),
            Posicion.DC  to (0.5f to 0.20f)
        )
        "FUT6_3_2_0" -> listOf(
            Posicion.POR to (0.5f to 0.88f),
            Posicion.LI  to (0.2f to 0.65f),
            Posicion.DFC to (0.5f to 0.68f),
            Posicion.LD  to (0.8f to 0.65f),
            Posicion.DC  to (0.35f to 0.25f),
            Posicion.DC  to (0.65f to 0.25f)
        )
        "FUT6_2_2_1" -> listOf(
            Posicion.POR to (0.5f to 0.88f),
            Posicion.DFC to (0.3f to 0.65f),
            Posicion.DFC to (0.7f to 0.65f),
            Posicion.EI  to (0.25f to 0.45f),
            Posicion.ED  to (0.75f to 0.45f),
            Posicion.DC  to (0.5f to 0.20f)
        )
        "FUT6_1_3_1" -> listOf(
            Posicion.POR to (0.5f to 0.88f),
            Posicion.DFC to (0.5f to 0.65f),
            Posicion.EI  to (0.2f to 0.45f),
            Posicion.MC  to (0.5f to 0.45f),
            Posicion.ED  to (0.8f to 0.45f),
            Posicion.DC  to (0.5f to 0.20f)
        )
        "FUT6_2_3_0" -> listOf(
            Posicion.POR to (0.5f to 0.88f),
            Posicion.DFC to (0.3f to 0.65f),
            Posicion.DFC to (0.7f to 0.65f),
            Posicion.EI  to (0.2f to 0.38f),
            Posicion.MC  to (0.5f to 0.38f),
            Posicion.ED  to (0.8f to 0.38f)
        )

        // --- FUTBOL 7 ---
        "FUT7_2_3_1" -> listOf(
            Posicion.POR to (0.5f to 0.88f),
            Posicion.DFC to (0.3f to 0.65f),
            Posicion.DFC to (0.7f to 0.65f),
            Posicion.EI  to (0.2f to 0.45f),
            Posicion.MC  to (0.5f to 0.45f),
            Posicion.ED  to (0.8f to 0.45f),
            Posicion.DC  to (0.5f to 0.20f)
        )
        "FUT7_2_2_2" -> listOf(
            Posicion.POR to (0.5f to 0.88f),
            Posicion.DFC to (0.3f to 0.65f),
            Posicion.DFC to (0.7f to 0.65f),
            Posicion.EI  to (0.25f to 0.45f),
            Posicion.ED  to (0.75f to 0.45f),
            Posicion.DC  to (0.35f to 0.20f),
            Posicion.DC  to (0.65f to 0.20f)
        )
        "FUT7_3_2_1_CERRADA" -> listOf(
            Posicion.POR to (0.5f to 0.88f),
            Posicion.LI  to (0.2f to 0.65f),
            Posicion.DFC to (0.5f to 0.68f),
            Posicion.LD  to (0.8f to 0.65f),
            Posicion.MC  to (0.35f to 0.45f),
            Posicion.MC  to (0.65f to 0.45f),
            Posicion.DC  to (0.5f to 0.20f)
        )
        "FUT7_3_2_1_ABIERTA" -> listOf(
            Posicion.POR to (0.5f to 0.88f),
            Posicion.LI  to (0.2f to 0.65f),
            Posicion.DFC to (0.5f to 0.68f),
            Posicion.LD  to (0.8f to 0.65f),
            Posicion.EI  to (0.25f to 0.45f),
            Posicion.ED  to (0.75f to 0.45f),
            Posicion.DC  to (0.5f to 0.20f)
        )
        "FUT7_4_1_1" -> listOf(
            Posicion.POR to (0.5f to 0.88f),
            Posicion.LI  to (0.15f to 0.65f),
            Posicion.DFC to (0.38f to 0.68f),
            Posicion.DFC to (0.62f to 0.68f),
            Posicion.LD  to (0.85f to 0.65f),
            Posicion.MC  to (0.5f to 0.45f),
            Posicion.DC  to (0.5f to 0.20f)
        )
        "FUT7_3_3_0" -> listOf(
            Posicion.POR to (0.5f to 0.88f),
            Posicion.LI  to (0.2f to 0.65f),
            Posicion.DFC to (0.5f to 0.68f),
            Posicion.LD  to (0.8f to 0.65f),
            Posicion.EI  to (0.2f to 0.38f),
            Posicion.MC  to (0.5f to 0.38f),
            Posicion.ED  to (0.8f to 0.38f)
        )
        "FUT7_4_2_0" -> listOf(
            Posicion.POR to (0.5f to 0.88f),
            Posicion.LI  to (0.15f to 0.65f),
            Posicion.DFC to (0.38f to 0.68f),
            Posicion.DFC to (0.62f to 0.68f),
            Posicion.LD  to (0.85f to 0.65f),
            Posicion.MC  to (0.35f to 0.40f),
            Posicion.MC  to (0.65f to 0.40f)
        )
        "FUT7_1_3_2" -> listOf(
            Posicion.POR to (0.5f to 0.88f),
            Posicion.DFC to (0.5f to 0.68f),
            Posicion.EI  to (0.2f to 0.45f),
            Posicion.MC  to (0.5f to 0.45f),
            Posicion.ED  to (0.8f to 0.45f),
            Posicion.DC  to (0.35f to 0.20f),
            Posicion.DC  to (0.65f to 0.20f)
        )
        else -> emptyList()
    }
}