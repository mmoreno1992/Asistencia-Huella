package santaana.asistencia.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import santaana.asistencia.db.converters.EstadoSincronizacionConverter
import santaana.asistencia.db.dao.ArchivoHuellaEmpleadoDao
import santaana.asistencia.db.entities.ArchivoHuellaEmpleado

@Database(
  entities = [ArchivoHuellaEmpleado::class],
  version = 1,
  exportSchema = false
)
@TypeConverters(EstadoSincronizacionConverter::class)
abstract class AsistenciaDatabase : RoomDatabase() {
  abstract fun getArchivoHuellaEmpleadoDao(): ArchivoHuellaEmpleadoDao
}