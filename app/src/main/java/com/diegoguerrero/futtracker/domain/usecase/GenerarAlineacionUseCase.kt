package com.diegoguerrero.futtracker.domain.usecase

import com.diegoguerrero.futtracker.domain.model.Formacion
import com.diegoguerrero.futtracker.domain.model.Jugador
import com.diegoguerrero.futtracker.domain.model.Posicion

class GenerarAlineacionUseCase {

    operator fun invoke(convocados: List<Jugador>, formacion: Formacion): List<Pair<Posicion, Jugador>> {
        val asignaciones = mutableListOf<Pair<Posicion, Jugador>>()
        val jugadoresDisponibles = convocados.toMutableList()
        val posicionesPendientes = mutableListOf(Posicion.POR).apply { 
            addAll(formacion.posicionesRequeridas) 
        }

        // 1. Asignación prioritaria del Portero
        if (posicionesPendientes.contains(Posicion.POR)) {
            val mejorPortero = extraerMejorCandidato(Posicion.POR, jugadoresDisponibles)
            if (mejorPortero != null) {
                asignaciones.add(Posicion.POR to mejorPortero)
                jugadoresDisponibles.remove(mejorPortero)
                posicionesPendientes.remove(Posicion.POR)
            }
        }

        // 2. Asignación del resto de posiciones de campo por orden de escasez de especialistas
        while (posicionesPendientes.isNotEmpty() && jugadoresDisponibles.isNotEmpty()) {
            // Seleccionamos la posición que tiene menos candidatos idóneos en plantilla
            val posicionPrioritaria = posicionesPendientes.minByOrNull { pos ->
                jugadoresDisponibles.count { pos in it.posicionesPrimarias }
            } ?: posicionesPendientes.first()

            val candidato = extraerMejorCandidato(posicionPrioritaria, jugadoresDisponibles)

            if (candidato != null) {
                asignaciones.add(posicionPrioritaria to candidato)
                jugadoresDisponibles.remove(candidato)
            }
            
            posicionesPendientes.remove(posicionPrioritaria)
        }

        return asignaciones
    }

    private fun extraerMejorCandidato(posicion: Posicion, disponibles: List<Jugador>): Jugador? {
        if (disponibles.isEmpty()) return null

        // Evaluación de afines (2 puntos por Primaria, 1 por Secundaria)
        val afinidad = disponibles.map { jugador ->
            val puntos = when {
                posicion in jugador.posicionesPrimarias -> 2
                posicion in jugador.posicionesSecundarias -> 1
                else -> 0
            }
            jugador to puntos
        }

        // Seleccionamos el jugador con mayor puntuación de afinidad
        val mejorPorAfinidad = afinidad.maxByOrNull { it.second }

        return if (mejorPorAfinidad != null && mejorPorAfinidad.second > 0) {
            mejorPorAfinidad.first
        } else {
            // Fallback: si no hay nadie afin para la posición, se asigna el jugador disponible restante
            disponibles.firstOrNull()
        }
    }

    fun sugerirMejorFormacion(convocados: List<Jugador>, formaciones: List<Formacion>): Formacion {
        if (formaciones.isEmpty()) throw IllegalArgumentException("La lista de formaciones no puede estar vacía")
        if (convocados.isEmpty()) return formaciones.first()

        return formaciones.maxByOrNull { formacion ->
            evaluarAfinidadTotal(convocados, formacion)
        } ?: formaciones.first()
    }

    fun evaluarAfinidadTotal(convocados: List<Jugador>, formacion: Formacion): Int {
        val asignaciones = invoke(convocados, formacion)
        var total = 0
        for ((pos, jugador) in asignaciones) {
            val puntos: Int = when {
                pos in jugador.posicionesPrimarias -> if (pos == Posicion.POR) 5 else 3
                pos in jugador.posicionesSecundarias -> if (pos == Posicion.POR) 2 else 1
                else -> 0
            }
            total += puntos
        }
        return total
    }
}