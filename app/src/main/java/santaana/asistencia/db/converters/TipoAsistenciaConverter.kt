package santaana.asistencia.db.converters

import androidx.room.TypeConverter
import santaana.asistencia.db.TipoAsistencia

class TipoAsistenciaConverter {
    @TypeConverter
    fun fromEnumToString(tipoAsistencia: TipoAsistencia) = tipoAsistencia.toString()

    @TypeConverter
    fun fromStringToEnum(tipoAsistencia: String) = TipoAsistencia.valueOf(tipoAsistencia)
}