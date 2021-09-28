package santaana.asistencia.networking.dto

import com.google.gson.annotations.SerializedName

class AsistenciaDto(
    val empleado: Int,
    val fecha: String,
    val tipoAsistencia: String,
    val uuid: String
)