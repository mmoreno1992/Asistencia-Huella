package santaana.asistencia.registrar

import androidx.lifecycle.ViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import santaana.asistencia.FS28DemoActivity
import santaana.asistencia.MyBitmapFile
import santaana.asistencia.db.Repo
import java.io.File
import java.io.FileOutputStream

class RegistrarHuellaViewModel : ViewModel(), KoinComponent {
    /*val repo: Repo by inject()

    suspend fun guardarImagen(empleado: Int, foto: ByteArray) {

        val file = File()
        try {
            val out = FileOutputStream(file)
            val fileBMP = MyBitmapFile(320, 480, FS28DemoActivity.mImageFP)
            out.write(fileBMP.toBytes())
            out.close()
            //  mMessage?.text = "Imagen guardada como: $fileName"
        } catch (e: Exception) {
            //  mMessage?.text = "Exception in saving file" + e.message
        }
    }*/
}