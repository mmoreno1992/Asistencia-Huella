package santaana.asistencia.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import santaana.asistencia.db.entities.AsistenciaEmpleado

@Dao
abstract class AsistenciaEmpleadoDao {

    @Insert
    abstract suspend fun insert(vararg assitencia: AsistenciaEmpleado)

    @Query("Select * From AsistenciaEmpleado")
    abstract fun getAsistenciasEmpleados(): LiveData<List<AsistenciaEmpleado>>

}