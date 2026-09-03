package com.diegoguerrero.futtracker.domain.model

data class Coordenada(val x: Float, val y: Float)

fun Formacion.obtenerCoordenadas(): List<Pair<Posicion, Coordenada>> {
    val mapa = mutableListOf<Pair<Posicion, Coordenada>>()
    
    // El portero siempre ocupa el centro abajo
    mapa.add(Posicion.POR to Coordenada(0.50f, 0.88f))

    when (id) {
        // --- FUTBOL SALA ---
        "FUTSAL_1_2_1" -> {
            mapa.add(Posicion.DFC to Coordenada(0.50f, 0.65f))
            mapa.add(Posicion.EI to Coordenada(0.25f, 0.42f))
            mapa.add(Posicion.ED to Coordenada(0.75f, 0.42f))
            mapa.add(Posicion.DC to Coordenada(0.50f, 0.18f))
        }
        "FUTSAL_2_2_0" -> {
            mapa.add(Posicion.DFC to Coordenada(0.32f, 0.65f))
            mapa.add(Posicion.DFC to Coordenada(0.68f, 0.65f))
            mapa.add(Posicion.DC to Coordenada(0.35f, 0.22f))
            mapa.add(Posicion.DC to Coordenada(0.65f, 0.22f))
        }
        "FUTSAL_3_0_1" -> {
            mapa.add(Posicion.LI to Coordenada(0.20f, 0.65f))
            mapa.add(Posicion.DFC to Coordenada(0.50f, 0.68f))
            mapa.add(Posicion.LD to Coordenada(0.80f, 0.65f))
            mapa.add(Posicion.DC to Coordenada(0.50f, 0.20f))
        }
        "FUTSAL_2_1_1" -> {
            mapa.add(Posicion.DFC to Coordenada(0.32f, 0.65f))
            mapa.add(Posicion.DFC to Coordenada(0.68f, 0.65f))
            mapa.add(Posicion.MC to Coordenada(0.50f, 0.45f))
            mapa.add(Posicion.DC to Coordenada(0.50f, 0.20f))
        }
        "FUTSAL_1_1_2" -> {
            mapa.add(Posicion.DFC to Coordenada(0.50f, 0.68f))
            mapa.add(Posicion.MC to Coordenada(0.50f, 0.45f))
            mapa.add(Posicion.DC to Coordenada(0.35f, 0.20f))
            mapa.add(Posicion.DC to Coordenada(0.65f, 0.20f))
        }
        "FUTSAL_2_0_2" -> {
            mapa.add(Posicion.DFC to Coordenada(0.32f, 0.65f))
            mapa.add(Posicion.DFC to Coordenada(0.68f, 0.65f))
            mapa.add(Posicion.EI to Coordenada(0.25f, 0.30f))
            mapa.add(Posicion.ED to Coordenada(0.75f, 0.30f))
        }
        "FUTSAL_1_3_0" -> {
            mapa.add(Posicion.DFC to Coordenada(0.50f, 0.68f))
            mapa.add(Posicion.EI to Coordenada(0.20f, 0.42f))
            mapa.add(Posicion.MC to Coordenada(0.50f, 0.40f))
            mapa.add(Posicion.ED to Coordenada(0.80f, 0.42f))
        }
        "FUTSAL_4_0_0" -> {
            mapa.add(Posicion.LI to Coordenada(0.18f, 0.60f))
            mapa.add(Posicion.DFC to Coordenada(0.38f, 0.65f))
            mapa.add(Posicion.DFC to Coordenada(0.62f, 0.65f))
            mapa.add(Posicion.LD to Coordenada(0.82f, 0.60f))
        }

        // --- FUTBOL 6 ---
        "FUT6_3_1_1" -> {
            mapa.add(Posicion.LI to Coordenada(0.20f, 0.65f))
            mapa.add(Posicion.DFC to Coordenada(0.50f, 0.68f))
            mapa.add(Posicion.LD to Coordenada(0.80f, 0.65f))
            mapa.add(Posicion.MC to Coordenada(0.50f, 0.45f))
            mapa.add(Posicion.DC to Coordenada(0.50f, 0.20f))
        }
        "FUT6_3_2_0" -> {
            mapa.add(Posicion.LI to Coordenada(0.20f, 0.65f))
            mapa.add(Posicion.DFC to Coordenada(0.50f, 0.68f))
            mapa.add(Posicion.LD to Coordenada(0.80f, 0.65f))
            mapa.add(Posicion.DC to Coordenada(0.35f, 0.25f))
            mapa.add(Posicion.DC to Coordenada(0.65f, 0.25f))
        }
        "FUT6_2_2_1" -> {
            mapa.add(Posicion.DFC to Coordenada(0.30f, 0.65f))
            mapa.add(Posicion.DFC to Coordenada(0.70f, 0.65f))
            mapa.add(Posicion.EI to Coordenada(0.25f, 0.45f))
            mapa.add(Posicion.ED to Coordenada(0.75f, 0.45f))
            mapa.add(Posicion.DC to Coordenada(0.50f, 0.20f))
        }
        "FUT6_1_3_1" -> {
            mapa.add(Posicion.DFC to Coordenada(0.50f, 0.65f))
            mapa.add(Posicion.EI to Coordenada(0.20f, 0.45f))
            mapa.add(Posicion.MC to Coordenada(0.50f, 0.45f))
            mapa.add(Posicion.ED to Coordenada(0.80f, 0.45f))
            mapa.add(Posicion.DC to Coordenada(0.50f, 0.20f))
        }
        "FUT6_2_3_0" -> {
            mapa.add(Posicion.DFC to Coordenada(0.30f, 0.65f))
            mapa.add(Posicion.DFC to Coordenada(0.70f, 0.65f))
            mapa.add(Posicion.EI to Coordenada(0.20f, 0.38f))
            mapa.add(Posicion.MC to Coordenada(0.50f, 0.38f))
            mapa.add(Posicion.ED to Coordenada(0.80f, 0.38f))
        }
        "FUT6_2_1_2" -> {
            mapa.add(Posicion.DFC to Coordenada(0.30f, 0.65f))
            mapa.add(Posicion.DFC to Coordenada(0.70f, 0.65f))
            mapa.add(Posicion.MC to Coordenada(0.50f, 0.45f))
            mapa.add(Posicion.DC to Coordenada(0.35f, 0.20f))
            mapa.add(Posicion.DC to Coordenada(0.65f, 0.20f))
        }
        "FUT6_1_2_2" -> {
            mapa.add(Posicion.DFC to Coordenada(0.50f, 0.68f))
            mapa.add(Posicion.MC to Coordenada(0.32f, 0.45f))
            mapa.add(Posicion.MC to Coordenada(0.68f, 0.45f))
            mapa.add(Posicion.DC to Coordenada(0.35f, 0.20f))
            mapa.add(Posicion.DC to Coordenada(0.65f, 0.20f))
        }
        "FUT6_1_4_0" -> {
            mapa.add(Posicion.DFC to Coordenada(0.50f, 0.68f))
            mapa.add(Posicion.LI to Coordenada(0.18f, 0.45f))
            mapa.add(Posicion.MC to Coordenada(0.39f, 0.45f))
            mapa.add(Posicion.MC to Coordenada(0.61f, 0.45f))
            mapa.add(Posicion.LD to Coordenada(0.82f, 0.45f))
        }
        "FUT6_2_0_3" -> {
            mapa.add(Posicion.DFC to Coordenada(0.30f, 0.65f))
            mapa.add(Posicion.DFC to Coordenada(0.70f, 0.65f))
            mapa.add(Posicion.EI to Coordenada(0.20f, 0.30f))
            mapa.add(Posicion.DC to Coordenada(0.50f, 0.20f))
            mapa.add(Posicion.ED to Coordenada(0.80f, 0.30f))
        }

        // --- FUTBOL 7 ---
        "FUT7_2_3_1" -> {
            mapa.add(Posicion.DFC to Coordenada(0.30f, 0.65f))
            mapa.add(Posicion.DFC to Coordenada(0.70f, 0.65f))
            mapa.add(Posicion.EI to Coordenada(0.20f, 0.45f))
            mapa.add(Posicion.MC to Coordenada(0.50f, 0.45f))
            mapa.add(Posicion.ED to Coordenada(0.80f, 0.45f))
            mapa.add(Posicion.DC to Coordenada(0.50f, 0.20f))
        }
        "FUT7_2_2_2" -> {
            mapa.add(Posicion.DFC to Coordenada(0.30f, 0.65f))
            mapa.add(Posicion.DFC to Coordenada(0.70f, 0.65f))
            mapa.add(Posicion.EI to Coordenada(0.25f, 0.45f))
            mapa.add(Posicion.ED to Coordenada(0.75f, 0.45f))
            mapa.add(Posicion.DC to Coordenada(0.35f, 0.20f))
            mapa.add(Posicion.DC to Coordenada(0.65f, 0.20f))
        }
        "FUT7_3_2_1_CERRADA" -> {
            mapa.add(Posicion.LI to Coordenada(0.20f, 0.65f))
            mapa.add(Posicion.DFC to Coordenada(0.50f, 0.68f))
            mapa.add(Posicion.LD to Coordenada(0.80f, 0.65f))
            mapa.add(Posicion.MC to Coordenada(0.35f, 0.45f))
            mapa.add(Posicion.MC to Coordenada(0.65f, 0.45f))
            mapa.add(Posicion.DC to Coordenada(0.50f, 0.20f))
        }
        "FUT7_3_2_1_ABIERTA" -> {
            mapa.add(Posicion.LI to Coordenada(0.20f, 0.65f))
            mapa.add(Posicion.DFC to Coordenada(0.50f, 0.68f))
            mapa.add(Posicion.LD to Coordenada(0.80f, 0.65f))
            mapa.add(Posicion.EI to Coordenada(0.25f, 0.45f))
            mapa.add(Posicion.ED to Coordenada(0.75f, 0.45f))
            mapa.add(Posicion.DC to Coordenada(0.50f, 0.20f))
        }
        "FUT7_4_1_1" -> {
            mapa.add(Posicion.LI to Coordenada(0.15f, 0.65f))
            mapa.add(Posicion.DFC to Coordenada(0.38f, 0.68f))
            mapa.add(Posicion.DFC to Coordenada(0.62f, 0.68f))
            mapa.add(Posicion.LD to Coordenada(0.85f, 0.65f))
            mapa.add(Posicion.MC to Coordenada(0.50f, 0.45f))
            mapa.add(Posicion.DC to Coordenada(0.50f, 0.20f))
        }
        "FUT7_3_3_0" -> {
            mapa.add(Posicion.LI to Coordenada(0.20f, 0.65f))
            mapa.add(Posicion.DFC to Coordenada(0.50f, 0.68f))
            mapa.add(Posicion.LD to Coordenada(0.80f, 0.65f))
            mapa.add(Posicion.EI to Coordenada(0.20f, 0.38f))
            mapa.add(Posicion.MC to Coordenada(0.50f, 0.38f))
            mapa.add(Posicion.ED to Coordenada(0.80f, 0.38f))
        }
        "FUT7_4_2_0" -> {
            mapa.add(Posicion.LI to Coordenada(0.15f, 0.65f))
            mapa.add(Posicion.DFC to Coordenada(0.38f, 0.68f))
            mapa.add(Posicion.DFC to Coordenada(0.62f, 0.68f))
            mapa.add(Posicion.LD to Coordenada(0.85f, 0.65f))
            mapa.add(Posicion.MC to Coordenada(0.35f, 0.40f))
            mapa.add(Posicion.MC to Coordenada(0.65f, 0.40f))
        }
        "FUT7_1_3_2" -> {
            mapa.add(Posicion.DFC to Coordenada(0.50f, 0.68f))
            mapa.add(Posicion.EI to Coordenada(0.20f, 0.45f))
            mapa.add(Posicion.MC to Coordenada(0.50f, 0.45f))
            mapa.add(Posicion.ED to Coordenada(0.80f, 0.45f))
            mapa.add(Posicion.DC to Coordenada(0.35f, 0.20f))
            mapa.add(Posicion.DC to Coordenada(0.65f, 0.20f))
        }
        "FUT7_2_1_3" -> {
            mapa.add(Posicion.DFC to Coordenada(0.30f, 0.68f))
            mapa.add(Posicion.DFC to Coordenada(0.70f, 0.68f))
            mapa.add(Posicion.MC to Coordenada(0.50f, 0.48f))
            mapa.add(Posicion.EI to Coordenada(0.20f, 0.25f))
            mapa.add(Posicion.ED to Coordenada(0.80f, 0.25f))
            mapa.add(Posicion.DC to Coordenada(0.50f, 0.20f))
        }
        "FUT7_3_1_2" -> {
            mapa.add(Posicion.LI to Coordenada(0.20f, 0.65f))
            mapa.add(Posicion.DFC to Coordenada(0.50f, 0.68f))
            mapa.add(Posicion.LD to Coordenada(0.80f, 0.65f))
            mapa.add(Posicion.MC to Coordenada(0.50f, 0.45f))
            mapa.add(Posicion.DC to Coordenada(0.35f, 0.20f))
            mapa.add(Posicion.DC to Coordenada(0.65f, 0.20f))
        }
        "FUT7_1_4_1" -> {
            mapa.add(Posicion.DFC to Coordenada(0.50f, 0.68f))
            mapa.add(Posicion.LI to Coordenada(0.15f, 0.45f))
            mapa.add(Posicion.MC to Coordenada(0.38f, 0.45f))
            mapa.add(Posicion.MC to Coordenada(0.62f, 0.45f))
            mapa.add(Posicion.LD to Coordenada(0.85f, 0.45f))
            mapa.add(Posicion.DC to Coordenada(0.50f, 0.20f))
        }
        "FUT7_2_4_0" -> {
            mapa.add(Posicion.DFC to Coordenada(0.32f, 0.68f))
            mapa.add(Posicion.DFC to Coordenada(0.68f, 0.68f))
            mapa.add(Posicion.LI to Coordenada(0.18f, 0.42f))
            mapa.add(Posicion.MC to Coordenada(0.40f, 0.42f))
            mapa.add(Posicion.MC to Coordenada(0.60f, 0.42f))
            mapa.add(Posicion.LD to Coordenada(0.82f, 0.42f))
        }
    }
    return mapa
}