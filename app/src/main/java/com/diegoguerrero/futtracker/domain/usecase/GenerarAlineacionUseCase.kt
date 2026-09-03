package com.diegoguerrero.futtracker.domain.usecase

import com.diegoguerrero.futtracker.domain.model.*

class GenerarAlineacionUseCase {

    fun ejecutar(
        convocados: List<Jugador>,
        formacion: Formacion
    ): Map<Pair<TipoPosicion, CoordenadaCampo>, Jugador?> {
        val disponibles = convocados.toMutableList()
        val resultado = mutableMapOf<Pair<TipoPosicion, CoordenadaCampo>, Jugador?>()

        for (slot in formacion.posiciones) {
            val tipoRequerido = slot.first

            // Asignación por prioridades (1ª > 2ª > 3ª preferencia)
            val candidato = disponibles.maxByOrNull { jugador ->
                when (tipoRequerido) {
                    jugador.posPrincipal -> 100
                    jugador.posSecundaria -> 50
                    jugador.posTercera -> 20
                    else -> 0
                }
            }

            if (candidato != null) {
                resultado[slot] = candidato
                disponibles.remove(candidato)
            } else {
                resultado[slot] = null
            }
        }
        return resultado
    }
}