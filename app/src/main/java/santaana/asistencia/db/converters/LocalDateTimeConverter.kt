package santaana.asistencia.db.converters

import androidx.room.TypeConverter
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class LocalDateTimeConverter {

    @TypeConverter
    fun fromLocalDateTimeToString(date: LocalDateTime) = date.toString()

    @TypeConverter
    fun fromStringToLocalDateTime(dateTimeInString: String) = LocalDateTime.parse(dateTimeInString)
}