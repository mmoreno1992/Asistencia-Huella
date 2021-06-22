package santaana.asistencia.db

import santaana.asistencia.db.dao.ArchivoHuellaEmpleadoDao

class Repo(val huellaDao: ArchivoHuellaEmpleadoDao) {

  suspend fun agregarHuella(empleado:Int, foto:BitMap){

  }
}