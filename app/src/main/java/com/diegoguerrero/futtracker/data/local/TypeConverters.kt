package com.diegoguerrero.futtracker.data.local

import androidx.room.TypeConverter
import com.diegoguerrero.futtracker.domain.model.TipoPosicion

class Converters {
    @TypeConverter
    fun fromTipoPosicion(value: TipoPosicion?): String? = value?.name

    @TypeConverter
    fun toTipoPosicion(value: String?): TipoPosicion? = value?.let { enumValueOf<TipoPosicion>(it) }
}