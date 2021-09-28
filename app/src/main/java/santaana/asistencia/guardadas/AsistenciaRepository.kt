package santaana.asistencia.guardadas

import org.threeten.bp.LocalDateTime
import santaana.asistencia.db.TipoAsistencia
import santaana.asistencia.db.dao.AsistenciaEmpleadoDao
import santaana.asistencia.db.entities.AsistenciaEmpleado

class AsistenciaRepository(val dao: AsistenciaEmpleadoDao) {

    fun getAsistenciasEmpleados() = dao.getAsistenciasEmpleados()

    suspend fun registraAsistenciaNueva(
        codigo: Int,
        tipoAsistencia: TipoAsistencia
    ) =
        dao.insert(
            AsistenciaEmpleado(
                codigoEmpleado = codigo,
                fecha = LocalDateTime.now(),
                entradaSalida = tipoAsistencia
            )
        )

    suspend fun getRegistrosNoEnviados() = dao.getRegistrosNoEnviados()

    suspend fun marcaRegistroComoEnviado(id: Long) = dao.marcaRegistroComoEnviado(id)

}