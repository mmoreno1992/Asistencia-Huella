package santaana.asistencia

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import pub.devrel.easypermissions.EasyPermissions
import santaana.asistencia.asistencia.TomarAsistenciaActivity
import santaana.asistencia.databinding.ActivityMenuBinding
import santaana.asistencia.registrar.RegistrarHuellaActivity
import santaana.asistencia.services.SincronizationService
import santaana.asistencia.util.toAsistenciaDto
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

        binding.button.setOnClickListener {
            val fecha =
                DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now())

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            val fecha2 = formatter.format(LocalDateTime.now())
            Log.i(TAG, "onCreate: $fecha")
            Log.i(TAG, "onCreate: $fecha2")
        }

        val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        EasyPermissions.requestPermissions(
            this, "Permiso necesario para almacenar las imagenes con las huellas",
            50, *perms
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_principal, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_sincronizar) {
            val intent = Intent(this, SincronizationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Toast.makeText(this, "1", Toast.LENGTH_SHORT).show()
                startForegroundService(intent)
            } else {
                Toast.makeText(this, "2", Toast.LENGTH_SHORT).show()
                startService(intent)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}