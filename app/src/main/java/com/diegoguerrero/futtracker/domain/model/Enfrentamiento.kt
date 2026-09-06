package com.diegoguerrero.futtracker.domain.model

data class EstadisticasJugadorCruzadas(
    val jugador: Jugador,
    // Como compañero en mi equipo
    val partidosComoCompanero: Int = 0,
    val victoriasComoCompanero: Int = 0,
    val empatesComoCompanero: Int = 0,
    val derrotasComoCompanero: Int = 0,
    val golesFavorComoCompanero: Int = 0,
    val golesContraComoCompanero: Int = 0,
    val golesMarcadosComoCompanero: Int = 0,
    // Como rival en el equipo contrario
    val partidosComoRival: Int = 0,
    val victoriasComoRival: Int = 0,
    val empatesComoRival: Int = 0,
    val derrotasComoRival: Int = 0,
    val golesFavorComoRival: Int = 0,
    val golesContraComoRival: Int = 0,
    val golesMarcadosComoRival: Int = 0,
    // Estadísticas individuales de este jugador en los partidos compartidos (Total)
    val goles: Int = 0,
    val asistencias: Int = 0,
    val tirosAlPalo: Int = 0,
    val fueraArea: Int = 0,
    val chilena: Int = 0,
    val tacon: Int = 0,
    val golesEncajados: Int = 0,
    val paradas: Int = 0,
    val haJugadoPortero: Boolean = false,
    // Como compañero (este jugador)
    val golesComoCompaneroInd: Int = 0,
    val asistenciasComoCompaneroInd: Int = 0,
    val tirosAlPaloComoCompaneroInd: Int = 0,
    val fueraAreaComoCompaneroInd: Int = 0,
    val chilenaComoCompaneroInd: Int = 0,
    val taconComoCompaneroInd: Int = 0,
    val golesEncajadosComoCompaneroInd: Int = 0,
    val paradasComoCompaneroInd: Int = 0,
    val haJugadoPorteroComoCompaneroInd: Boolean = false,
    // Como rival (este jugador)
    val golesComoRivalInd: Int = 0,
    val asistenciasComoRivalInd: Int = 0,
    val tirosAlPaloComoRivalInd: Int = 0,
    val fueraAreaComoRivalInd: Int = 0,
    val chilenaComoRivalInd: Int = 0,
    val taconComoRivalInd: Int = 0,
    val golesEncajadosComoRivalInd: Int = 0,
    val paradasComoRivalInd: Int = 0,
    val haJugadoPorteroComoRivalInd: Boolean = false,
    // Estadísticas del jugador inspeccionado (target) en estos mismos partidos (Total)
    val golesTarget: Int = 0,
    val asistenciasTarget: Int = 0,
    val tirosAlPaloTarget: Int = 0,
    val fueraAreaTarget: Int = 0,
    val chilenaTarget: Int = 0,
    val taconTarget: Int = 0,
    val golesEncajadosTarget: Int = 0,
    val paradasTarget: Int = 0,
    val haJugadoPorteroTarget: Boolean = false,
    // Como compañero (target)
    val golesTargetComoCompaneroInd: Int = 0,
    val asistenciasTargetComoCompaneroInd: Int = 0,
    val tirosAlPaloTargetComoCompaneroInd: Int = 0,
    val fueraAreaTargetComoCompaneroInd: Int = 0,
    val chilenaTargetComoCompaneroInd: Int = 0,
    val taconTargetComoCompaneroInd: Int = 0,
    val golesEncajadosTargetComoCompaneroInd: Int = 0,
    val paradasTargetComoCompaneroInd: Int = 0,
    val haJugadoPorteroTargetComoCompaneroInd: Boolean = false,
    // Como rival (target)
    val golesTargetComoRivalInd: Int = 0,
    val asistenciasTargetComoRivalInd: Int = 0,
    val tirosAlPaloTargetComoRivalInd: Int = 0,
    val fueraAreaTargetComoRivalInd: Int = 0,
    val chilenaTargetComoRivalInd: Int = 0,
    val taconTargetComoRivalInd: Int = 0,
    val golesEncajadosTargetComoRivalInd: Int = 0,
    val paradasTargetComoRivalInd: Int = 0,
    val haJugadoPorteroTargetComoRivalInd: Boolean = false
) {
    val totalPartidos: Int
        get() = partidosComoCompanero + partidosComoRival

    val porcentajeVictoriasCompanero: Float
        get() = if (partidosComoCompanero > 0) (victoriasComoCompanero.toFloat() / partidosComoCompanero) * 100f else 0f

    val porcentajeVictoriasRival: Float
        get() = if (partidosComoRival > 0) (victoriasComoRival.toFloat() / partidosComoRival) * 100f else 0f

    val diferenciaGolesCompanero: Int
        get() = golesFavorComoCompanero - golesContraComoCompanero

    val diferenciaGolesRival: Int
        get() = golesFavorComoRival - golesContraComoRival
}

data class DuoEstadisticas(
    val jugador1: Jugador,
    val jugador2: Jugador,
    val partidosJuntos: Int = 0,
    val victorias: Int = 0,
    val empates: Int = 0,
    val derrotas: Int = 0,
    val golesFavor: Int = 0,
    val golesContra: Int = 0
) {
    val porcentajeVictorias: Float
        get() = if (partidosJuntos > 0) (victorias.toFloat() / partidosJuntos) * 100f else 0f

    val diferenciaGoles: Int
        get() = golesFavor - golesContra
}

data class ComparativaCaraACara(
    val jugadorA: Jugador,
    val jugadorB: Jugador,
    val partidosEnfrentados: Int = 0,
    val victoriasA: Int = 0,
    val victoriasB: Int = 0,
    val empates: Int = 0,
    val golesEquipoA: Int = 0,
    val golesEquipoB: Int = 0,
    val partidosJuntos: Int = 0,
    val victoriasJuntos: Int = 0,
    val empatesJuntos: Int = 0,
    val derrotasJuntos: Int = 0,
    val partidosHistorial: List<Partido> = emptyList()
) {
    val porcentajeVictoriasA: Float
        get() = if (partidosEnfrentados > 0) (victoriasA.toFloat() / partidosEnfrentados) * 100f else 0f

    val porcentajeVictoriasB: Float
        get() = if (partidosEnfrentados > 0) (victoriasB.toFloat() / partidosEnfrentados) * 100f else 0f
}

data class DestacadosEnfrentamientos(
    val companerosMasGanan: List<EstadisticasJugadorCruzadas> = emptyList(),
    val companerosMasPierden: List<EstadisticasJugadorCruzadas> = emptyList(),
    val rivalesMasGanan: List<EstadisticasJugadorCruzadas> = emptyList(),
    val rivalesMasPierden: List<EstadisticasJugadorCruzadas> = emptyList()
) {
    val companeroMasGana: EstadisticasJugadorCruzadas? get() = companerosMasGanan.firstOrNull()
    val companeroMasPierde: EstadisticasJugadorCruzadas? get() = companerosMasPierden.firstOrNull()
    val rivalMasGana: EstadisticasJugadorCruzadas? get() = rivalesMasGanan.firstOrNull()
    val rivalMasPierde: EstadisticasJugadorCruzadas? get() = rivalesMasPierden.firstOrNull()
}
