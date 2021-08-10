package santaana.asistencia.registrar

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import santaana.asistencia.BluetoothDataService
import santaana.asistencia.FS28DemoActivity
import santaana.asistencia.FS28DemoActivity.Companion.MESSAGE_DEVICE_NAME
import santaana.asistencia.R
import santaana.asistencia.databinding.ActivityRegistrarHuellaBinding

class RegistrarHuellaActivity : AppCompatActivity() {

    /*  companion object {
        const val DEVICE_NAME = "device_name"
        const val MESSAGE_STATE_CHANGE = 1
    }

    lateinit var binding: ActivityRegistrarHuellaBinding
    private var mBTService: BluetoothDataService? = null
    var deviceName: String? = null
    var connected = false
    var mustStop = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarHuellaBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupCommunication() {
        if (mBTService != null) {
            // Only if the state is STATE_NONE, we haven't started yet
            if (mBTService?.state == BluetoothDataService.STATE_NONE) {
                mBTService?.start()
            }
        } else {
            mBTService = BluetoothDataService(mHandler)
            mBTService?.start()
        }

        binding.mensajes.text = getString(R.string.escuchando_conexiones)
    }

    // The Handler that gets information back from the BluetoothChatService
    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_STATE_CHANGE -> when (msg.arg1) {
                    BluetoothDataService.STATE_CONNECTED -> {
                        binding.mensajes.text = getString(
                            R.string.conectado_al_lector,
                            deviceName
                        )
                    }
                    BluetoothDataService.STATE_CONNECTING ->
                        binding.mensajes.text = getString(R.string.conectando)

                    BluetoothDataService.STATE_LISTEN,
                    BluetoothDataService.STATE_NONE -> {
                        binding.mensajes.text = getString(R.string.esperando_intento_conexion)
                        //No hay conexión
                        if (connected) {
                            connected = false
                            if (!mustStop) {
                                if (mBTService != null) {
                                    mBTService?.stop()
                                    mBTService = null
                                }
                                setupCommunication()
                            }
                        }
                    }
                }
                FS28DemoActivity.MESSAGE_SHOW_MSG -> {
                    val showMsg = msg.obj as String
                    mMessage!!.text = showMsg
                }
                FS28DemoActivity.MESSAGE_SHOW_PROGRESSBAR -> {
                    mButtonOpen?.isEnabled = false
                    mButtonSave?.isEnabled = false
                    mProgressbar1?.progress = FS28DemoActivity.mStep
                }
                FS28DemoActivity.MESSAGE_SHOW_IMAGE -> {
                    mProgressbar1!!.progress = 0
                    if (FS28DemoActivity.mReceivedDataType == BluetoothDataService.DATA_TYPE_WSQIMAGE
                        || FS28DemoActivity.mReceivedDataType == BluetoothDataService.DATA_TYPE_RAWIMAGE
                    ) showBitmap() else mFingerImage!!.setImageBitmap(
                        null
                    )
                    mButtonSave!!.isEnabled = true
                    mButtonOpen!!.isEnabled = true
                }
                MESSAGE_DEVICE_NAME -> {
                    deviceName = msg.data.getString(FS28DemoActivity.DEVICE_NAME)
                    *//* Toast.makeText(
                         applicationContext, "Conectado a:  "
                                 + mConnectedDeviceName, Toast.LENGTH_SHORT
                     ).show()*//*
                }
                FS28DemoActivity.MESSAGE_TOAST -> Toast.makeText(
                    applicationContext, msg.data.getString(FS28DemoActivity.TOAST),
                    Toast.LENGTH_SHORT
                ).show()

            }*/
}