package com.diegoguerrero.futtracker.domain.model

enum class ModoJuego(val jugadores: Int) { FUT_6(6), FUT_7(7) }

data class CoordenadaCampo(val x: Float, val y: Float) // Porcentajes (0.0 - 1.0)

sealed class Formacion(
    val nombre: String,
    val modo: ModoJuego,
    val posiciones: List<Pair<TipoPosicion, CoordenadaCampo>>
) {
    object Fut7_231 : Formacion(
        nombre = "2-3-1",
        modo = ModoJuego.FUT_7,
        posiciones = listOf(
            TipoPosicion.POR to CoordenadaCampo(0.5f, 0.90f),
            TipoPosicion.DEF to CoordenadaCampo(0.3f, 0.72f),
            TipoPosicion.DEF to CoordenadaCampo(0.7f, 0.72f),
            TipoPosicion.MED to CoordenadaCampo(0.2f, 0.45f),
            TipoPosicion.MED to CoordenadaCampo(0.5f, 0.45f),
            TipoPosicion.MED to CoordenadaCampo(0.8f, 0.45f),
            TipoPosicion.DEL to CoordenadaCampo(0.5f, 0.18f)
        )
    )

    object Fut6_221 : Formacion(
        nombre = "2-2-1",
        modo = ModoJuego.FUT_6,
        posiciones = listOf(
            TipoPosicion.POR to CoordenadaCampo(0.5f, 0.90f),
            TipoPosicion.DEF to CoordenadaCampo(0.32f, 0.70f),
            TipoPosicion.DEF to CoordenadaCampo(0.68f, 0.70f),
            TipoPosicion.MED to CoordenadaCampo(0.32f, 0.42f),
            TipoPosicion.MED to CoordenadaCampo(0.68f, 0.42f),
            TipoPosicion.DEL to CoordenadaCampo(0.5f, 0.18f)
        )
    )
}