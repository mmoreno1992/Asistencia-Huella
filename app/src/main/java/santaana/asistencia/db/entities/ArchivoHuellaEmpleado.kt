package santaana.asistencia.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import santaana.asistencia.db.util.EstadoSincronizacion

@Entity(tableName = "ARCHIVO_HUELLA_EMPLEADO")
class ArchivoHuellaEmpleado(
  @PrimaryKey
  val nombre: String,
  val empleado: Int,
  var correlativo: Int,
  @ColumnInfo(name = "estado_sincronizacion")
  val estadoSincronizacion: EstadoSincronizacion
) {
}