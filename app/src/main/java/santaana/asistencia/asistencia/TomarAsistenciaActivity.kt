package santaana.asistencia.asistencia

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.graphics.*
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.machinezoo.sourceafis.FingerprintImage
import com.machinezoo.sourceafis.FingerprintImageOptions
import com.machinezoo.sourceafis.FingerprintMatcher
import com.machinezoo.sourceafis.FingerprintTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.component.inject
import org.threeten.bp.LocalDate
import pub.devrel.easypermissions.EasyPermissions
import santaana.asistencia.*
import santaana.asistencia.databinding.ActivityRegistrarHuellaBinding
import santaana.asistencia.databinding.ActivityTomarAsistenciaBinding
import santaana.asistencia.db.TipoAsistencia
import santaana.asistencia.guardadas.AsistenciaGuardadaActivity
import santaana.asistencia.guardadas.AsistenciaRepository
import santaana.asistencia.registrar.RegistrarHuellaBluetoothDataService
import java.io.File
import java.io.FileOutputStream

class TomarAsistenciaActivity : AppCompatActivity() {
    var mIn = true
    private var mBitmapFP: Bitmap? = null
    private var mMessage: TextView? = null
    private var mFingerImage: ImageView? = null
    private var mProgressbar1: ProgressBar? = null
    private var mConnectedDeviceName: String? = null
    private lateinit var codigoEmpleado: TextInputEditText

    lateinit var binding: ActivityTomarAsistenciaBinding

    var itemConectarConLector: MenuItem? = null
    var itemGuardarHuella: MenuItem? = null

    // Local Bluetooth adapter
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBTService: TomarAsistenciaBluetoothDataService? = null
    val repository: AsistenciaRepository by inject()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTomarAsistenciaBinding.inflate(layoutInflater)
        // Get local Bluetooth adapter
        setContentView(binding.root)

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        mMessage = findViewById(R.id.tvMessage)
        mFingerImage = findViewById(R.id.imageFinger)
        mProgressbar1 = findViewById(R.id.progressBar)
        mProgressbar1?.max = 100
        codigoEmpleado = findViewById(R.id.codigoEmpleado)
        setSupportActionBar(binding.toolbar)
        setListeners()
        conectarConLector()
    }

    private fun setListeners() {
        binding.botonTomarAsistencia.setOnClickListener {
            if (connected) {
                binding.botonTomarAsistencia.isEnabled = false
                muestraProgressBar(true)
                mMessage?.text = "Comparando Imagenes..."
                if (codigoEmpleado.text.toString().isEmpty()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        compararImagenes()
                    }
                } else {
                    lifecycleScope.launch(Dispatchers.IO) {
                        compararImagenesPorCodigo(codigoEmpleado.text.toString().toInt())
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    "Conecte antes el lector, para recibir la huella",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun muestraProgressBar(muestra: Boolean) {
        if (muestra)
            binding.progressBar2.visibility = View.VISIBLE
        else
            binding.progressBar2.visibility = View.INVISIBLE
    }

    private suspend fun compararImagenesPorCodigo(codigo: Int) {
        var huellaEncontrada = false

        val fileBMP = MyBitmapFile(320, 480, TomarAsistenciaActivity.mImageFP)
        val candidate = FingerprintTemplate(
            FingerprintImage(
                fileBMP.toBytes(),
                FingerprintImageOptions()
                    .dpi(500.0)
            )
        )
        run loop@{
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.listFiles { _, name ->
                name.contains(
                    codigo.toString()
                )
            }?.forEach {

                if (!huellaEncontrada) {
                    val probe = FingerprintTemplate(
                        FingerprintImage(
                            it.readBytes(),
                            FingerprintImageOptions()
                                .dpi(500.0)
                        )
                    )

                    val score = FingerprintMatcher(probe)
                        .match(candidate)
                    if (score >= 40) {
                        repository.registraAsistenciaNueva(codigo, getTipoAsistencia())
                        huellaEncontrada = true
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        if (score >= 40) {
                            mMessage?.text = "Se registr?? la asistencia del colaborador $codigo"
                            mMessage?.postDelayed({
                                mFingerImage?.setImageResource(R.drawable.ic_picture)
                            }, 1000)
                        }
                    }
                } else {
                    return@loop
                }

            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            binding.botonTomarAsistencia.isEnabled = true
            if (!huellaEncontrada) {
                mMessage?.text = "No se encontr?? una huella que coincidiera"
                muestraProgressBar(false)
            }
        }

    }

    private fun getTipoAsistencia() =
        if (binding.rbEntrada.isChecked)
            TipoAsistencia.ENTRADA
        else
            TipoAsistencia.SALIDA


    private suspend fun compararImagenes() {
        var huellaEncontrada = false


        val fileBMP = MyBitmapFile(320, 480, TomarAsistenciaActivity.mImageFP)

        val candidate = FingerprintTemplate(
            FingerprintImage(
                fileBMP.toBytes(),
                FingerprintImageOptions()
                    .dpi(500.0)
            )
        )
        run loop@{
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.listFiles()?.forEach {
                if (!huellaEncontrada) {

                    CoroutineScope(Dispatchers.Main).launch {
                        mMessage?.text = "Revisando el archivo ${it.name}"
                    }
                    val probe = FingerprintTemplate(
                        FingerprintImage(
                            it.readBytes(),
                            FingerprintImageOptions()
                                .dpi(500.0)
                        )
                    )

                    val score = FingerprintMatcher(probe)
                        .match(candidate)
                    if (score >= 40) {
                        huellaEncontrada = true
                        repository.registraAsistenciaNueva(
                            it.name.split("_")[0].toInt(),
                            getTipoAsistencia()
                        )
                        CoroutineScope(Dispatchers.Main).launch {
                            mMessage?.text =
                                "Se registr?? asistencia para el colaborador ${it.name.split("_")[0]} "//Archivo ${it.name} COINCIDE con la huella de it.name.split(\"_\")[0]"
                        }
                    }
                } else {
                    return@loop
                }
                if (!huellaEncontrada) {
                    CoroutineScope(Dispatchers.Main).launch {
                        mMessage?.text =
                            "No se encontr?? una huella que coincidiera, no se registr?? asistencia. "
                    }
                }
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            binding.botonTomarAsistencia.isEnabled = true
            muestraProgressBar(false)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_tomar_asistencia, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        itemConectarConLector = menu?.findItem(R.id.action_conecta_lector)
        itemConectarConLector?.isEnabled = stopped
        return super.onPrepareOptionsMenu(menu)
    }

    private fun conectarConLector() {
        if (stopped) {
            stopped = false
            setupCommunication()
            invalidateOptionsMenu()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_conecta_lector -> {
            conectarConLector()
            true
        }
        R.id.action_asistencia_tomada -> {
            startActivity(Intent(this, AsistenciaGuardadaActivity::class.java))
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    public override fun onStart() {
        super.onStart()
        // If BT is not on, request that it be enabled.
        if (mBluetoothAdapter?.isEnabled != true) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
        }
    }

    private fun setupCommunication() {
        if (mBTService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBTService?.state == BluetoothDataService.STATE_NONE) {
                // Start the Bluetooth services
                mBTService?.start()
            }
        } else {
            mBTService = TomarAsistenciaBluetoothDataService(bluetoothHandler)
            mBTService?.start()
        }
        mMessage?.text = getString(R.string.esperando_conexiones_lector)
    }

    public override fun onDestroy() {
        super.onDestroy()
        // Stop the Bluetooth services
        mBTService?.stop()
        stopped = true
    }

    fun exitProgram() {
        finish()
    }

    private fun saveImage() {
        if (binding.codigoEmpleado.text.isNullOrBlank()) {
            Toast.makeText(this, "Ingrese el codigo de colaborador.", Toast.LENGTH_SHORT).show()
            return
        }
        val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            val extStorageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val dir = File(extStorageDirectory!!.absolutePath)
            val fileFormat = ".bmp"
            val date = LocalDate.now()
            val fileName =
                "$dir/${codigoEmpleado.text.toString()}_${date.dayOfMonth}${date.monthValue}${date.year}.bmp"
            saveImageByFileFormat(fileFormat, fileName)
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(
                this, "Permiso necesario para almacenar las huellas",
                50, *perms
            )
        }
    }

    private fun saveImageByFileFormat(fileFormat: String, fileName: String) {
        if (mImageFP != null) {
            val file = File(fileName)
            try {
                val out = FileOutputStream(file)
                val fileBMP = MyBitmapFile(320, 480, mImageFP)
                out.write(fileBMP.toBytes())
                out.close()
                mMessage?.text = "Imagen guardada como: $fileName"
            } catch (e: Exception) {
                mMessage?.text = "Exception in saving file" + e.message
            }
        }
    }

    // The Handler that gets information back from the BluetoothChatService
    private val bluetoothHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_STATE_CHANGE -> when (msg.arg1) {
                    BluetoothDataService.STATE_CONNECTED -> {
                        mMessage!!.text = "Conectado al lector: "
                        mMessage!!.append(mConnectedDeviceName)
                        invalidateOptionsMenu()
                    }
                    BluetoothDataService.STATE_CONNECTING -> mMessage!!.text = "Connectando..."
                    BluetoothDataService.STATE_LISTEN, BluetoothDataService.STATE_NONE -> {
                        mMessage!!.text = "Esperando intento de conexi??n..."
                        if (connected) {
                            connected = false
                            if (!stopped) {
                                if (mBTService != null) {
                                    mBTService!!.stop()
                                    mBTService = null
                                }
                                if (mIn) setupCommunication()
                            }
                        } else invalidateOptionsMenu()
                    }
                }
                MESSAGE_SHOW_MSG -> {
                    val showMsg = msg.obj as String
                    mMessage!!.text = showMsg
                }
                MESSAGE_SHOW_PROGRESSBAR -> {
                    invalidateOptionsMenu()
                    itemGuardarHuella?.isEnabled = false
//                    mButtonSave?.isEnabled = false
                    mProgressbar1?.progress = mStep
                }
                MESSAGE_SHOW_IMAGE -> {
                    mProgressbar1!!.progress = 0
                    if (mReceivedDataType == BluetoothDataService.DATA_TYPE_WSQIMAGE
                        || mReceivedDataType == BluetoothDataService.DATA_TYPE_RAWIMAGE
                    ) showBitmap() else mFingerImage!!.setImageBitmap(
                        null
                    )
                    itemGuardarHuella?.isEnabled = true
                    //mButtonSave!!.isEnabled = true
                    invalidateOptionsMenu()
                }
                MESSAGE_DEVICE_NAME -> {
                    // save the connected device's name
                    mConnectedDeviceName = msg.data.getString(DEVICE_NAME)
                    Toast.makeText(
                        applicationContext, "Conectado a:  "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT
                    ).show()
                }
                MESSAGE_TOAST -> Toast.makeText(
                    applicationContext, msg.data.getString(TOAST),
                    Toast.LENGTH_SHORT
                ).show()
                MESSAGE_DATA_RECEIVING -> {
                    invalidateOptionsMenu()
                    itemGuardarHuella?.isEnabled = false
//                    mButtonSave!!.isEnabled = false
                }
                MESSAGE_DATA_ERROR -> {
                    invalidateOptionsMenu()
                    when (msg.arg1) {
                        BluetoothDataService.ERROR_TIMEOUT -> {
                            mStep = 0
                            mProgressbar1!!.progress = 0
                            mMessage!!.text = "Time out to receive data!"
                            try {
                                Thread.sleep(1000)
                            } catch (e: InterruptedException) {
                                // TODO Auto-generated catch block
                                e.printStackTrace()
                            }
                            if (connected) {
                                connected = false
                                if (!stopped) {
                                    if (mBTService != null) {
                                        mBTService!!.stop()
                                        mBTService = null
                                    }
                                    if (mIn) setupCommunication() else {
                                        stopped = true
                                    }
                                }
                            }
                        }
                        BluetoothDataService.ERROR_INVALID_COMMAND_DATA -> mMessage!!.text =
                            "Invalid command data."
                        BluetoothDataService.ERROR_CHECKSUM_ERROR -> mMessage!!.text =
                            "Checksum error."
                        BluetoothDataService.ERROR_UNKNOWN_COMMAND -> mMessage!!.text =
                            "Unknown command."
                        BluetoothDataService.ERROR_IMAGE_SIZE_TOO_LARGE -> mMessage!!.text =
                            "Image size is too large. > 153602"
                    }
                }
                MESSAGE_EXIT_PROGRAM -> exitProgram()
            }
        }
    }

    private fun showBitmap() {
        val pixels = IntArray(153600)
        for (i in 0..153599) pixels[i] = mImageFP[i].toInt()
        val emptyBmp = Bitmap.createBitmap(pixels, 320, 480, Bitmap.Config.RGB_565)
        val height: Int = emptyBmp.height
        val width: Int = emptyBmp.width
        mBitmapFP = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val c = Canvas(mBitmapFP as Bitmap)
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        val f = ColorMatrixColorFilter(cm)
        paint.colorFilter = f
        c.drawBitmap(emptyBmp, 0f, 0f, paint)
        mFingerImage!!.setImageBitmap(mBitmapFP)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ENABLE_BT ->                 // When the request to enable Bluetooth returns
                if (resultCode == RESULT_OK) {
                    // Bluetooth is now enabled, so set up a session
                    setupCommunication()
                } else {
                    // User did not enable Bluetooth or an error occured
                    Toast.makeText(this, "BT not enabled", Toast.LENGTH_SHORT).show()
                    finish()
                }
            REQUEST_CONNECT_DEVICE ->                 // When DeviceListActivity returns with a device to connect
                if (resultCode == RESULT_OK) {
                    // Get the device MAC address
                    val address = data!!.extras
                        ?.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS)
                    setupCommunication()
                    // Get the BLuetoothDevice object
                    val device = mBluetoothAdapter!!.getRemoteDevice(address)
                    // Attempt to connect to the device
                    mBTService!!.connect(device)
                } else {
                    Toast.makeText(
                        this@TomarAsistenciaActivity,
                        "No se pudo conectar ",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            REQUEST_FILE_FORMAT ->
                if (resultCode == RESULT_OK) {
                    // Get the file format
                    val extraString =
                        data!!.extras!!.getStringArray(SelectFileFormatActivity.EXTRA_FILE_FORMAT)
                    val fileFormat = extraString!![0]
                    val fileName = extraString[1]
                    saveImageByFileFormat(fileFormat, fileName)
                } else mMessage?.text = "Se cancel?? la operaci??n"
        }
    }

    companion object {
        private const val TAG = "FS28V3.1"

        // Message types sent from the BluetoothDataService Handler
        const val MESSAGE_STATE_CHANGE = 1
        const val MESSAGE_READ = 2
        const val MESSAGE_WRITE = 3
        const val MESSAGE_DEVICE_NAME = 4
        const val MESSAGE_TOAST = 5
        const val MESSAGE_SHOW_MSG = 6
        const val MESSAGE_SHOW_IMAGE = 7
        const val MESSAGE_SHOW_PROGRESSBAR = 8
        const val MESSAGE_DATA_RECEIVING = 9
        const val MESSAGE_DATA_ERROR = 10
        const val MESSAGE_EXIT_PROGRAM = 11

        // Key names received from the BluetoothDataService Handler
        const val DEVICE_NAME = "device_name"
        const val TOAST = "toast"

        // Intent request codes
        private const val REQUEST_CONNECT_DEVICE = 1
        private const val REQUEST_ENABLE_BT = 2
        private const val REQUEST_FILE_FORMAT = 3
        lateinit var mWsqImageFP: ByteArray
        lateinit var mANSISample: ByteArray
        lateinit var mISOSample: ByteArray


        @JvmField
        var stopped = true

        @JvmField
        var connected = false

        @JvmField
        var mImageFP = ByteArray(153602)

        @JvmField
        var mStep = 0

        @JvmField
        var mReceivedDataType = BluetoothDataService.DATA_TYPE_RAWIMAGE

        @JvmField
        var mHostSample = ByteArray(669)
    }
}