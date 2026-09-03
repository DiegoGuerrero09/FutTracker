package com.diegoguerrero.futtracker.domain.usecase

import java.time.LocalDate

enum class TipoFiltroFecha { ANIO_NATURAL, TEMPORADA_DEPORTIVA }

class GetEstadisticasTemporadaUseCase {

    fun obtenerRango(fecha: LocalDate = LocalDate.now(), filtro: TipoFiltroFecha): Pair<LocalDate, LocalDate> {
        val anio = fecha.year
        return when (filtro) {
            TipoFiltroFecha.ANIO_NATURAL -> {
                LocalDate.of(anio, 1, 1) to LocalDate.of(anio, 12, 31)
            }
            TipoFiltroFecha.TEMPORADA_DEPORTIVA -> {
                if (fecha.monthValue >= 9) {
                    LocalDate.of(anio, 9, 1) to LocalDate.of(anio + 1, 8, 31)
                } else {
                    LocalDate.of(anio - 1, 9, 1) to LocalDate.of(anio, 8, 31)
                }
            }
        }
    }
}