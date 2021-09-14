package santaana.asistencia.networking.dto

import com.google.gson.annotations.SerializedName

class AsistenciaDto(
    val empleado: Int,
    val uuid: String,
    @SerializedName("tipo_asistencia")
    val tipoAsistencia: String,
    val fecha: String
)