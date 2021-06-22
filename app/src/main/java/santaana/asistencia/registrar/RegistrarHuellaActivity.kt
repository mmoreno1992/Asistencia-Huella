package santaana.asistencia.registrar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import santaana.asistencia.databinding.ActivityRegistrarHuellaBinding

class RegistrarHuellaActivity : AppCompatActivity() {

  lateinit var binding: ActivityRegistrarHuellaBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityRegistrarHuellaBinding.inflate(layoutInflater)
    setContentView(binding.root)
  }

}