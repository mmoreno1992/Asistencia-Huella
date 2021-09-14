package santaana.asistencia.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import santaana.asistencia.db.converters.EstadoSincronizacionConverter
import santaana.asistencia.db.converters.LocalDateTimeConverter
import santaana.asistencia.db.converters.TipoAsistenciaConverter
import santaana.asistencia.db.dao.ArchivoHuellaEmpleadoDao
import santaana.asistencia.db.dao.AsistenciaEmpleadoDao
import santaana.asistencia.db.entities.ArchivoHuellaEmpleado
import santaana.asistencia.db.entities.AsistenciaEmpleado

@Database(
    entities = [ArchivoHuellaEmpleado::class,
        AsistenciaEmpleado::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(
    EstadoSincronizacionConverter::class,
    LocalDateTimeConverter::class,
    TipoAsistenciaConverter::class
)
abstract class AsistenciaDatabase : RoomDatabase() {
    abstract fun getArchivoHuellaEmpleadoDao(): ArchivoHuellaEmpleadoDao
    abstract fun getAsistenciaEmpleadoDao(): AsistenciaEmpleadoDao
}