package santaana.asistencia.db.converters

import androidx.room.TypeConverter
import santaana.asistencia.db.util.EstadoSincronizacion


class EstadoSincronizacionConverter {
  @TypeConverter
  fun fromEstadoSincronizacionEnumToString(estadoSincronizacion: EstadoSincronizacion) =
    estadoSincronizacion.name

  @TypeConverter
  fun fromStringToEstadoSincronizacionEnum(estadoSincronizacion: String) =
    EstadoSincronizacion.valueOf(estadoSincronizacion)
}