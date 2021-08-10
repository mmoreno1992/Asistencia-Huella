package santaana.asistencia.guardadas

import androidx.recyclerview.widget.DiffUtil
import santaana.asistencia.db.entities.AsistenciaEmpleado

class AsistenciaEmpleadoDiffUtil : DiffUtil.ItemCallback<AsistenciaEmpleado>() {
    override fun areItemsTheSame(
        oldItem: AsistenciaEmpleado,
        newItem: AsistenciaEmpleado
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: AsistenciaEmpleado,
        newItem: AsistenciaEmpleado
    ): Boolean {
        return oldItem.codigoEmpleado == newItem.codigoEmpleado
                && oldItem.fecha == newItem.fecha
    }
}