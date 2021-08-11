package santaana.asistencia

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import santaana.asistencia.databinding.ActivityMenuBinding
import santaana.asistencia.registrar.RegistrarHuellaActivity
import java.io.File

class MenuActivity : AppCompatActivity() {
  lateinit var binding: ActivityMenuBinding
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMenuBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setSupportActionBar(binding.toolbar)
    binding.registrarHuella.setOnClickListener {
      startActivity(Intent(this, RegistrarHuellaActivity::class.java))
    }

    binding.registrarAsistencia.setOnClickListener {
      startActivity(Intent(this, FS28DemoActivity::class.java))
    }
    val extStorageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val dir = File(extStorageDirectory!!.absolutePath)

  }
}