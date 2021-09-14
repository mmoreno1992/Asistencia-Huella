package santaana.asistencia.guardadas

import org.koin.core.component.getScopeId
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import santaana.asistencia.db.TipoAsistencia
import santaana.asistencia.db.dao.AsistenciaEmpleadoDao
import santaana.asistencia.db.entities.AsistenciaEmpleado
import santaana.asistencia.networking.dto.AsistenciaDto

class AsistenciaRepository(val asistenciaEmpleado: AsistenciaEmpleadoDao) {

    fun getAsistenciasEmpleados() = asistenciaEmpleado.getAsistenciasEmpleados()

    suspend fun registraAsistenciaNueva(codigo: Int,
    tipoAsistencia: TipoAsistencia) =
        asistenciaEmpleado.insert(
            AsistenciaEmpleado(
                codigoEmpleado = codigo,
                fecha = LocalDateTime.now(),
                entradaSalida = tipoAsistencia
            )
        )

    suspend fun getRegistrosNoEnviados() = asistenciaEmpleado.getRegistrosNoEnviados()

   // suspend fun marcaRegistroComoEnviado(asistencia:AsistenciaDto) = asistenciaEmpleado.marcaRegistroComoEnviado(asistencia.)
}