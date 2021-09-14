package santaana.asistencia.guardadas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import santaana.asistencia.R
import santaana.asistencia.db.entities.AsistenciaEmpleado
import santaana.asistencia.extensions.formatDateTime

class RvAsistenciaGuardadaAdapter :
    ListAdapter<AsistenciaEmpleado, RvAsistenciaGuardadaAdapter.ViewHolder>(
        AsistenciaEmpleadoDiffUtil()
    ) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_asistencia_guardada, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val codigoEmpleado = itemView.findViewById<TextView>(R.id.codigoEmpleadoItem)
        val fecha = itemView.findViewById<TextView>(R.id.fechaAsistenciaItem)
        val tipoAsistencia = itemView.findViewById<TextView>(R.id.tvEntradaSalida)
        fun bind(item: AsistenciaEmpleado?) {
            item?.let {
                codigoEmpleado.text = it.codigoEmpleado.toString()
                fecha.text = formatDateTime(it.fecha)
                tipoAsistencia.text = it.entradaSalida.toString()
            }
        }
    }
}