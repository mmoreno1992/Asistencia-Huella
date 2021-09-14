package santaana.asistencia

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import pub.devrel.easypermissions.EasyPermissions
import santaana.asistencia.asistencia.TomarAsistenciaActivity
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
            startActivity(Intent(this, TomarAsistenciaActivity::class.java))
        }

        val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        EasyPermissions.requestPermissions(
            this, "Permiso necesario para almacenar las imagenes con las huellas",
            50, *perms
        )
    }
}