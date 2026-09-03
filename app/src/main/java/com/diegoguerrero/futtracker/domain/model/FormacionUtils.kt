package com.diegoguerrero.futtracker.domain.model

data class Coordenada(val x: Float, val y: Float)

fun Formacion.obtenerCoordenadas(): List<Pair<Posicion, Coordenada>> {
    val mapa = mutableListOf<Pair<Posicion, Coordenada>>()
    
    // El portero siempre ocupa el centro abajo
    mapa.add(Posicion.POR to Coordenada(0.50f, 0.90f))

    when (nombre) {
        // --- FUT 6 ---
        "3-1-1" -> {
            mapa.add(Posicion.LI to Coordenada(0.20f, 0.70f))
            mapa.add(Posicion.DFC to Coordenada(0.50f, 0.72f))
            mapa.add(Posicion.LD to Coordenada(0.80f, 0.70f))
            mapa.add(Posicion.MC to Coordenada(0.50f, 0.45f))
            mapa.add(Posicion.DC to Coordenada(0.50f, 0.20f))
        }
        "3-2-0" -> {
            mapa.add(Posicion.LI to Coordenada(0.20f, 0.70f))
            mapa.add(Posicion.DFC to Coordenada(0.50f, 0.72f))
            mapa.add(Posicion.LD to Coordenada(0.80f, 0.70f))
            mapa.add(Posicion.MC to Coordenada(0.35f, 0.35f))
            mapa.add(Posicion.MC to Coordenada(0.65f, 0.35f))
        }
        "2-2-1" -> {
            mapa.add(Posicion.DFC to Coordenada(0.35f, 0.70f))
            mapa.add(Posicion.DFC to Coordenada(0.65f, 0.70f))
            mapa.add(Posicion.EI to Coordenada(0.25f, 0.45f))
            mapa.add(Posicion.ED to Coordenada(0.75f, 0.45f))
            mapa.add(Posicion.DC to Coordenada(0.50f, 0.20f))
        }
        "1-3-1" -> {
            mapa.add(Posicion.DFC to Coordenada(0.50f, 0.72f))
            mapa.add(Posicion.EI to Coordenada(0.20f, 0.45f))
            mapa.add(Posicion.MC to Coordenada(0.50f, 0.48f))
            mapa.add(Posicion.ED to Coordenada(0.80f, 0.45f))
            mapa.add(Posicion.DC to Coordenada(0.50f, 0.20f))
        }
        "2-3-0" -> {
            mapa.add(Posicion.DFC to Coordenada(0.35f, 0.70f))
            mapa.add(Posicion.DFC to Coordenada(0.65f, 0.70f))
            mapa.add(Posicion.EI to Coordenada(0.20f, 0.40f))
            mapa.add(Posicion.MC to Coordenada(0.50f, 0.42f))
            mapa.add(Posicion.ED to Coordenada(0.80f, 0.40f))
        }
        "1-2-2" -> {
            mapa.add(Posicion.DFC to Coordenada(0.50f, 0.72f))
            mapa.add(Posicion.EI to Coordenada(0.25f, 0.45f))
            mapa.add(Posicion.ED to Coordenada(0.75f, 0.45f))
            mapa.add(Posicion.DC to Coordenada(0.35f, 0.20f))
            mapa.add(Posicion.DC to Coordenada(0.65f, 0.20f))
        }

        // --- FUT 7 ---
        "2-3-1" -> {
            mapa.add(Posicion.DFC to Coordenada(0.35f, 0.72f))
            mapa.add(Posicion.DFC to Coordenada(0.65f, 0.72f))
            mapa.add(Posicion.EI to Coordenada(0.20f, 0.45f))
            mapa.add(Posicion.MC to Coordenada(0.50f, 0.48f))
            mapa.add(Posicion.ED to Coordenada(0.80f, 0.45f))
            mapa.add(Posicion.DC to Coordenada(0.50f, 0.18f))
        }
        "2-2-2" -> {
            mapa.add(Posicion.DFC to Coordenada(0.35f, 0.72f))
            mapa.add(Posicion.DFC to Coordenada(0.65f, 0.72f))
            mapa.add(Posicion.EI to Coordenada(0.25f, 0.45f))
            mapa.add(Posicion.ED to Coordenada(0.75f, 0.45f))
            mapa.add(Posicion.DC to Coordenada(0.35f, 0.18f))
            mapa.add(Posicion.DC to Coordenada(0.65f, 0.18f))
        }
        "3-2-1 Cerrada" -> {
            mapa.add(Posicion.LI to Coordenada(0.20f, 0.70f))
            mapa.add(Posicion.DFC to Coordenada(0.50f, 0.72f))
            mapa.add(Posicion.LD to Coordenada(0.80f, 0.70f))
            mapa.add(Posicion.MC to Coordenada(0.35f, 0.45f))
            mapa.add(Posicion.MC to Coordenada(0.65f, 0.45f))
            mapa.add(Posicion.DC to Coordenada(0.50f, 0.18f))
        }
        "3-2-1 Abierta" -> {
            mapa.add(Posicion.LI to Coordenada(0.20f, 0.70f))
            mapa.add(Posicion.DFC to Coordenada(0.50f, 0.72f))
            mapa.add(Posicion.LD to Coordenada(0.80f, 0.70f))
            mapa.add(Posicion.EI to Coordenada(0.25f, 0.45f))
            mapa.add(Posicion.ED to Coordenada(0.75f, 0.45f))
            mapa.add(Posicion.DC to Coordenada(0.50f, 0.18f))
        }
        "4-1-1" -> {
            mapa.add(Posicion.LI to Coordenada(0.15f, 0.70f))
            mapa.add(Posicion.DFC to Coordenada(0.38f, 0.72f))
            mapa.add(Posicion.DFC to Coordenada(0.62f, 0.72f))
            mapa.add(Posicion.LD to Coordenada(0.85f, 0.70f))
            mapa.add(Posicion.MC to Coordenada(0.50f, 0.45f))
            mapa.add(Posicion.DC to Coordenada(0.50f, 0.18f))
        }
        "3-3-0" -> {
            mapa.add(Posicion.LI to Coordenada(0.20f, 0.70f))
            mapa.add(Posicion.DFC to Coordenada(0.50f, 0.72f))
            mapa.add(Posicion.LD to Coordenada(0.80f, 0.70f))
            mapa.add(Posicion.EI to Coordenada(0.20f, 0.38f))
            mapa.add(Posicion.MC to Coordenada(0.50f, 0.40f))
            mapa.add(Posicion.ED to Coordenada(0.80f, 0.38f))
        }
        "4-2-0" -> {
            mapa.add(Posicion.LI to Coordenada(0.15f, 0.70f))
            mapa.add(Posicion.DFC to Coordenada(0.38f, 0.72f))
            mapa.add(Posicion.DFC to Coordenada(0.62f, 0.72f))
            mapa.add(Posicion.LD to Coordenada(0.85f, 0.70f))
            mapa.add(Posicion.MC to Coordenada(0.35f, 0.40f))
            mapa.add(Posicion.MC to Coordenada(0.65f, 0.40f))
        }
        "1-3-2" -> {
            mapa.add(Posicion.DFC to Coordenada(0.50f, 0.72f))
            mapa.add(Posicion.EI to Coordenada(0.20f, 0.45f))
            mapa.add(Posicion.MC to Coordenada(0.50f, 0.48f))
            mapa.add(Posicion.ED to Coordenada(0.80f, 0.45f))
            mapa.add(Posicion.DC to Coordenada(0.35f, 0.18f))
            mapa.add(Posicion.DC to Coordenada(0.65f, 0.18f))
        }
        "3-1-2" -> {
            mapa.add(Posicion.LI to Coordenada(0.20f, 0.70f))
            mapa.add(Posicion.DFC to Coordenada(0.50f, 0.72f))
            mapa.add(Posicion.LD to Coordenada(0.80f, 0.70f))
            mapa.add(Posicion.MC to Coordenada(0.50f, 0.45f))
            mapa.add(Posicion.DC to Coordenada(0.35f, 0.18f))
            mapa.add(Posicion.DC to Coordenada(0.65f, 0.18f))
        }
        "2-1-2-1" -> {
            mapa.add(Posicion.DFC to Coordenada(0.35f, 0.72f))
            mapa.add(Posicion.DFC to Coordenada(0.65f, 0.72f))
            mapa.add(Posicion.MC to Coordenada(0.50f, 0.52f))
            mapa.add(Posicion.EI to Coordenada(0.25f, 0.35f))
            mapa.add(Posicion.ED to Coordenada(0.75f, 0.35f))
            mapa.add(Posicion.DC to Coordenada(0.50f, 0.18f))
        }
    }
    return mapa
}