package santaana.asistencia.networking

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import santaana.asistencia.networking.dto.AsistenciaDto


interface AsistenciaApi {
    @POST("asistencia")
    suspend fun enviaAsistencia(@Body asistencia: AsistenciaDto): Response<Unit>
}