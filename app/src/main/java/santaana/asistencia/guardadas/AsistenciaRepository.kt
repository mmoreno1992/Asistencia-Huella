package santaana.asistencia.guardadas

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import santaana.asistencia.db.dao.AsistenciaEmpleadoDao
import santaana.asistencia.db.entities.AsistenciaEmpleado

class AsistenciaRepository(val asistenciaEmpleado: AsistenciaEmpleadoDao) {

    fun getAsistenciasEmpleados() = asistenciaEmpleado.getAsistenciasEmpleados()

    suspend fun registraAsistenciaNueva(codigo: Int) =
        asistenciaEmpleado.insert(
            AsistenciaEmpleado(
                codigoEmpleado = codigo,
                fecha = LocalDateTime.now()
            )
        )

}