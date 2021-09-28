package santaana.asistencia.util

import org.threeten.bp.format.DateTimeFormatter
import santaana.asistencia.db.entities.AsistenciaEmpleado
import santaana.asistencia.networking.dto.AsistenciaDto

val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

fun AsistenciaEmpleado.toAsistenciaDto() =
    AsistenciaDto(
        codigoEmpleado,
        fecha.format(formatter),
        entradaSalida.toString(),
        uuid
    )
