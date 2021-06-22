package santaana.asistencia.db.dao

import androidx.room.*
import santaana.asistencia.db.entities.ArchivoHuellaEmpleado

@Dao
abstract class ArchivoHuellaEmpleadoDao {

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  abstract fun insert(vararg huellas: ArchivoHuellaEmpleado)

  @Delete
  abstract fun delete(huella: ArchivoHuellaEmpleado)

  @Query("UPDATE archivo_huella_empleado SET estadoSincronizacion = ")
  abstract fun cambiaEstadoSincronizacion()
}