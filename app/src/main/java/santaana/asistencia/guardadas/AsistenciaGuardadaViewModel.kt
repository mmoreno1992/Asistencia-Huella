package santaana.asistencia.guardadas

import androidx.lifecycle.ViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AsistenciaGuardadaViewModel : ViewModel(), KoinComponent {
    val repository: AsistenciaRepository by inject()

    fun getAsistenciasEmpleados() = repository.getAsistenciasEmpleados()
}