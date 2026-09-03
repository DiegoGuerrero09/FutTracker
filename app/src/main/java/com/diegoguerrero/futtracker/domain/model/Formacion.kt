package com.diegoguerrero.futtracker.domain.model

enum class Posicion { POR, DFC, LI, LD, MC, EI, ED, DC }

enum class TipoFutbol(val nJugadoresCampo: Int) {
    FUTSAL(4), // 4 de campo + 1 POR
    FUT_6(5), // 5 de campo + 1 POR
    FUT_7(6)  // 6 de campo + 1 POR
}

data class Formacion(
    val nombre: String,
    val tipo: TipoFutbol,
    val posicionesRequeridas: List<Posicion>
)

val FORMACIONES_FUTSAL = listOf(
    Formacion("1-2-1", TipoFutbol.FUTSAL, listOf(Posicion.DFC, Posicion.EI, Posicion.ED, Posicion.DC)),
    Formacion("2-2-0", TipoFutbol.FUTSAL, listOf(Posicion.DFC, Posicion.DFC, Posicion.MC, Posicion.DC)),
    Formacion("3-0-1", TipoFutbol.FUTSAL, listOf(Posicion.LI, Posicion.DFC, Posicion.LD, Posicion.DC)),
    Formacion("2-1-1", TipoFutbol.FUTSAL, listOf(Posicion.DFC, Posicion.DFC, Posicion.MC, Posicion.DC))
)

val FORMACIONES_FUT_6 = listOf(
    Formacion("3-1-1", TipoFutbol.FUT_6, listOf(Posicion.LI, Posicion.DFC, Posicion.LD, Posicion.MC, Posicion.DC)),
    Formacion("3-2-0", TipoFutbol.FUT_6, listOf(Posicion.LI, Posicion.DFC, Posicion.LD, Posicion.MC, Posicion.MC)),
    Formacion("2-2-1", TipoFutbol.FUT_6, listOf(Posicion.DFC, Posicion.DFC, Posicion.EI, Posicion.ED, Posicion.DC)),
    Formacion("1-3-1", TipoFutbol.FUT_6, listOf(Posicion.DFC, Posicion.EI, Posicion.ED, Posicion.MC, Posicion.DC)),
    Formacion("2-3-0", TipoFutbol.FUT_6, listOf(Posicion.DFC, Posicion.DFC, Posicion.EI, Posicion.ED, Posicion.MC))
)

val FORMACIONES_FUT_7 = listOf(
    Formacion("2-3-1", TipoFutbol.FUT_7, listOf(Posicion.DFC, Posicion.DFC, Posicion.EI, Posicion.ED, Posicion.MC, Posicion.DC)),
    Formacion("2-2-2", TipoFutbol.FUT_7, listOf(Posicion.DFC, Posicion.DFC, Posicion.EI, Posicion.ED, Posicion.DC, Posicion.DC)),
    Formacion("3-2-1 Cerrada", TipoFutbol.FUT_7, listOf(Posicion.LI, Posicion.DFC, Posicion.LD, Posicion.MC, Posicion.MC, Posicion.DC)),
    Formacion("3-2-1 Abierta", TipoFutbol.FUT_7, listOf(Posicion.LI, Posicion.DFC, Posicion.LD, Posicion.EI, Posicion.ED, Posicion.DC)),
    Formacion("4-1-1", TipoFutbol.FUT_7, listOf(Posicion.LI, Posicion.DFC, Posicion.DFC, Posicion.LD, Posicion.MC, Posicion.DC)),
    Formacion("3-3-0", TipoFutbol.FUT_7, listOf(Posicion.LI, Posicion.DFC, Posicion.LD, Posicion.EI, Posicion.MC, Posicion.ED)),
    Formacion("4-2-0", TipoFutbol.FUT_7, listOf(Posicion.LI, Posicion.DFC, Posicion.DFC, Posicion.LD, Posicion.MC, Posicion.MC)),
    Formacion("1-3-2", TipoFutbol.FUT_7, listOf(Posicion.DFC, Posicion.EI, Posicion.MC, Posicion.ED, Posicion.DC, Posicion.DC))
)

fun obtenerCoordenadas(formacion: Formacion): List<Pair<Posicion, Pair<Float, Float>>> {
    return when (formacion.nombre) {
        // --- FUTBOL 5 ---
        "1-2-1" -> listOf(
            Posicion.POR to (0.5f to 0.90f),
            Posicion.DFC to (0.5f to 0.72f),
            Posicion.EI to (0.25f to 0.45f),
            Posicion.ED to (0.75f to 0.45f),
            Posicion.DC to (0.5f to 0.20f)
        )
        "2-2-0" -> listOf(
            Posicion.POR to (0.5f to 0.90f),
            Posicion.DFC to (0.3f to 0.72f),
            Posicion.DFC to (0.7f to 0.72f),
            Posicion.MC to (0.3f to 0.40f),
            Posicion.DC to (0.7f to 0.40f)
        )
        "3-0-1" -> listOf(
            Posicion.POR to (0.5f to 0.90f),
            Posicion.LI to (0.2f to 0.72f),
            Posicion.DFC to (0.5f to 0.76f),
            Posicion.LD to (0.8f to 0.72f),
            Posicion.DC to (0.5f to 0.22f)
        )
        "2-1-1" -> listOf(
            Posicion.POR to (0.5f to 0.90f),
            Posicion.DFC to (0.3f to 0.75f),
            Posicion.DFC to (0.7f to 0.75f),
            Posicion.MC to (0.5f to 0.48f),
            Posicion.DC to (0.5f to 0.20f)
        )

        // --- FUTBOL 6 ---
        "3-1-1" -> listOf(
            Posicion.POR to (0.5f to 0.90f),
            Posicion.LI to (0.2f to 0.75f),
            Posicion.DFC to (0.5f to 0.78f),
            Posicion.LD to (0.8f to 0.75f),
            Posicion.MC to (0.5f to 0.48f),
            Posicion.DC to (0.5f to 0.20f)
        )
        "3-2-0" -> listOf(
            Posicion.POR to (0.5f to 0.90f),
            Posicion.LI to (0.2f to 0.75f),
            Posicion.DFC to (0.5f to 0.78f),
            Posicion.LD to (0.8f to 0.75f),
            Posicion.MC to (0.35f to 0.45f),
            Posicion.MC to (0.65f to 0.45f)
        )
        "2-2-1" -> listOf(
            Posicion.POR to (0.5f to 0.90f),
            Posicion.DFC to (0.3f to 0.75f),
            Posicion.DFC to (0.7f to 0.75f),
            Posicion.EI to (0.25f to 0.48f),
            Posicion.ED to (0.75f to 0.48f),
            Posicion.DC to (0.5f to 0.20f)
        )
        "1-3-1" -> listOf(
            Posicion.POR to (0.5f to 0.90f),
            Posicion.DFC to (0.5f to 0.75f),
            Posicion.EI to (0.2f to 0.48f),
            Posicion.MC to (0.5f to 0.48f),
            Posicion.ED to (0.8f to 0.48f),
            Posicion.DC to (0.5f to 0.20f)
        )
        "2-3-0" -> listOf(
            Posicion.POR to (0.5f to 0.90f),
            Posicion.DFC to (0.3f to 0.75f),
            Posicion.DFC to (0.7f to 0.75f),
            Posicion.EI to (0.2f to 0.45f),
            Posicion.MC to (0.5f to 0.45f),
            Posicion.ED to (0.8f to 0.45f)
        )

        // --- FUTBOL 7 ---
        "2-3-1" -> listOf(
            Posicion.POR to (0.5f to 0.90f),
            Posicion.DFC to (0.3f to 0.75f),
            Posicion.DFC to (0.7f to 0.75f),
            Posicion.EI to (0.2f to 0.48f),
            Posicion.MC to (0.5f to 0.48f),
            Posicion.ED to (0.8f to 0.48f),
            Posicion.DC to (0.5f to 0.20f)
        )
        "2-2-2" -> listOf(
            Posicion.POR to (0.5f to 0.90f),
            Posicion.DFC to (0.3f to 0.75f),
            Posicion.DFC to (0.7f to 0.75f),
            Posicion.EI to (0.25f to 0.48f),
            Posicion.ED to (0.75f to 0.48f),
            Posicion.DC to (0.35f to 0.22f),
            Posicion.DC to (0.65f to 0.22f)
        )
        "3-2-1 Cerrada" -> listOf(
            Posicion.POR to (0.5f to 0.90f),
            Posicion.LI to (0.2f to 0.75f),
            Posicion.DFC to (0.5f to 0.78f),
            Posicion.LD to (0.8f to 0.75f),
            Posicion.MC to (0.35f to 0.48f),
            Posicion.MC to (0.65f to 0.48f),
            Posicion.DC to (0.5f to 0.20f)
        )
        "3-2-1 Abierta" -> listOf(
            Posicion.POR to (0.5f to 0.90f),
            Posicion.LI to (0.2f to 0.75f),
            Posicion.DFC to (0.5f to 0.78f),
            Posicion.LD to (0.8f to 0.75f),
            Posicion.EI to (0.25f to 0.48f),
            Posicion.ED to (0.75f to 0.48f),
            Posicion.DC to (0.5f to 0.20f)
        )
        "4-1-1" -> listOf(
            Posicion.POR to (0.5f to 0.90f),
            Posicion.LI to (0.15f to 0.75f),
            Posicion.DFC to (0.38f to 0.78f),
            Posicion.DFC to (0.62f to 0.78f),
            Posicion.LD to (0.85f to 0.75f),
            Posicion.MC to (0.5f to 0.48f),
            Posicion.DC to (0.5f to 0.20f)
        )
        "3-3-0" -> listOf(
            Posicion.POR to (0.5f to 0.90f),
            Posicion.LI to (0.2f to 0.75f),
            Posicion.DFC to (0.5f to 0.78f),
            Posicion.LD to (0.8f to 0.75f),
            Posicion.EI to (0.2f to 0.45f),
            Posicion.MC to (0.5f to 0.45f),
            Posicion.ED to (0.8f to 0.45f)
        )
        "4-2-0" -> listOf(
            Posicion.POR to (0.5f to 0.90f),
            Posicion.LI to (0.15f to 0.75f),
            Posicion.DFC to (0.38f to 0.78f),
            Posicion.DFC to (0.62f to 0.78f),
            Posicion.LD to (0.85f to 0.75f),
            Posicion.MC to (0.35f to 0.45f),
            Posicion.MC to (0.65f to 0.45f)
        )
        "1-3-2" -> listOf(
            Posicion.POR to (0.5f to 0.90f),
            Posicion.DFC to (0.5f to 0.78f),
            Posicion.EI to (0.2f to 0.48f),
            Posicion.MC to (0.5f to 0.48f),
            Posicion.ED to (0.8f to 0.48f),
            Posicion.DC to (0.35f to 0.22f),
            Posicion.DC to (0.65f to 0.22f)
        )
        else -> emptyList()
    }
}