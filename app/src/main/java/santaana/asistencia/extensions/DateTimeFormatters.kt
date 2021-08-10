package santaana.asistencia.extensions

import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

fun formatDateTime(dateTime: LocalDateTime) = dateTimeFormatter.format(dateTime)
