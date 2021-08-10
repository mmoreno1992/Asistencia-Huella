package santaana.asistencia.db

import santaana.asistencia.db.dao.ArchivoHuellaEmpleadoDao

class Repo(val huellaDao: ArchivoHuellaEmpleadoDao) {

    suspend fun agregarHuella(path: String, empleado: Int, foto: ByteArray) {

    }
}