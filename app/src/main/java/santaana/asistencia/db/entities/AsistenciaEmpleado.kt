package santaana.asistencia.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

@Entity
class AsistenciaEmpleado(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val codigoEmpleado:Int,
    val fecha: LocalDateTime
) {
}