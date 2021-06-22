package santaana.asistencia.registrar

import androidx.lifecycle.ViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import santaana.asistencia.db.Repo

class RegistrarHuellaViewModel : ViewModel(), KoinComponent {
  val repo: Repo by inject()
}