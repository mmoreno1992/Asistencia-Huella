package santaana.asistencia

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.graphics.*
import android.os.*
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.machinezoo.sourceafis.FingerprintImage
import com.machinezoo.sourceafis.FingerprintImageOptions
import com.machinezoo.sourceafis.FingerprintMatcher
import com.machinezoo.sourceafis.FingerprintTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.FileOutputStream


//import android.os.Environment;
class FS28DemoActivity : AppCompatActivity() {
  private var mButtonOpen: Button? = null
  private var mButtonSave: Button? = null
  private var mMessage: TextView? = null
  private var mFingerImage: ImageView? = null
  private var mProgressbar1: ProgressBar? = null
  private var mRadioIn: RadioButton? = null
  private var mRadioOut: RadioButton? = null
  private var mToolbar: Toolbar? = null
  private var mConnectedDeviceName: String? = null

  // Local Bluetooth adapter
  private var mBluetoothAdapter: BluetoothAdapter? = null
  private var mBTService: BluetoothDataService? = null
  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    // Get local Bluetooth adapter
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    // If the adapter is null, then Bluetooth is not supported
    if (mBluetoothAdapter == null) {
      Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show()
      finish()
      return
    }
    mMessage = findViewById(R.id.tvMessage)
    mFingerImage = findViewById(R.id.imageFinger)
    mProgressbar1 = findViewById(R.id.progressBar1)
    mProgressbar1?.setMax(100)
    // Initialize the send button with a listener that for click events
    mButtonOpen = findViewById(R.id.btn_open_bt)
    mButtonSave = findViewById(R.id.btn_save)
    mRadioIn = findViewById(R.id.radioIn)
    mRadioOut = findViewById(R.id.radioOut)
    mButtonSave?.setEnabled(false)
    mToolbar = findViewById(R.id.toolbar)
    setSupportActionBar(mToolbar)
    //check the status of buttons from destroy
    /*  if (!mIn)
        mRadioOut.setChecked(true);*/
    mButtonOpen?.setOnClickListener {
      compararImagenes()

      /*if (mStop) {
        if (!mIn) {
          startDeviceListActivity()
        } else {
          mStop = false
          setupCommunication()
          mButtonOpen?.text = "Desconectar"
          mRadioIn?.isEnabled = false
          mRadioOut?.isEnabled = false
        }
      } else {
        mStop = true
        if (mBTService != null) {
          mBTService!!.stop()
          mBTService = null
        }
        mButtonOpen?.text = "Conectar con Lector"
        mRadioIn?.isEnabled = true
        mRadioOut?.isEnabled = true
      }*/
    }
    mButtonSave?.setOnClickListener {
      saveImage()
    }
    mRadioIn?.setOnClickListener { mIn = true }
    mRadioOut?.setOnClickListener { mIn = false }


  }

  private fun compararImagenes() {
    GlobalScope.launch {
      Log.i(TAG, "compararImagenes: 1")

      val file1 = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "1.bmp")
      Log.i(TAG, "compararImagenes: 2")

      val probe = FingerprintTemplate(
        FingerprintImage(
          file1.readBytes(),
          FingerprintImageOptions()
            .dpi(500.0)
        )
      )
      Log.i(TAG, "compararImagenes: 3")


      val file2 = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "3.bmp")
      val candidate = FingerprintTemplate(
        FingerprintImage(
          file2.readBytes(),
          FingerprintImageOptions()
            .dpi(500.0)
        )
      )

      val score = FingerprintMatcher(probe)
        .match(candidate)
      Log.i(TAG, "compararImagenes: $score")

      CoroutineScope(Dispatchers.Main).launch {
        if (score >= 40) {
          Toast.makeText(this@FS28DemoActivity, "Coinciden!", Toast.LENGTH_SHORT).show()
        } else {
          Toast.makeText(this@FS28DemoActivity, "NOOOO Coinciden!", Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

  fun conectarConLector() {
    if (mStop) {
      if (!mIn) {
        startDeviceListActivity()
      } else {
        mStop = false
        setupCommunication()
        //    mButtonOpen.setText("Desconectar");
        mRadioIn?.isEnabled = false
        mRadioOut?.isEnabled = false
      }
    }
  }

  fun desconectarLector() {
    mStop = true
    if (mBTService != null) {
      mBTService?.stop()
      mBTService = null
    }
    //            mButtonOpen.setText("Conectar con Lector");
    mRadioIn?.isEnabled = true
    mRadioOut?.isEnabled = true
  }

  fun startDeviceListActivity() {
    val serverIntent = Intent(this, DeviceListActivity::class.java)
    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE)
  }

  public override fun onStart() {
    super.onStart()
    if (D) Log.e(TAG, "++ ON START ++")

    // If BT is not on, request that it be enabled.
    // setupChat() will then be called during onActivityResult
    if (!mBluetoothAdapter?.isEnabled!!) {
      val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
      startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
    }
  }

  @Synchronized
  public override fun onResume() {
    super.onResume()
    if (D) Log.e(TAG, "+ ON RESUME +")
  }

  private fun setupCommunication() {
    Log.d(TAG, "setupCommunication()")
    if (mBTService != null) {
      // Only if the state is STATE_NONE, do we know that we haven't started already
      if (mBTService?.state == BluetoothDataService.STATE_NONE) {
        // Start the Bluetooth services
        mBTService?.start()
      }
    } else {
      mBTService = BluetoothDataService(this, mHandler)
      mBTService?.start()
    }
    mMessage?.text = "Escuchando conexiones con lector..."
  }

  @Synchronized
  public override fun onPause() {
    super.onPause()
    if (D) Log.e(TAG, "- ON PAUSE -")
  }

  public override fun onStop() {
    super.onStop()
    if (D) Log.e(TAG, "-- ON STOP --")
  }

  public override fun onDestroy() {
    super.onDestroy()
    // Stop the Bluetooth services
    mStop = true
    if (mBTService != null) mBTService?.stop()
    if (D) Log.e(TAG, "--- ON DESTROY ---")
  }

  /* @Override
  public void onBackPressed() {
      //super.OnBackPressed();
      new AlertDialog.Builder(this)
              .setTitle("Exit")
              .setMessage("Do you want to exit this program?")
              .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int whichButton) {
                      //send message to exit
                      mHandler.obtainMessage(MESSAGE_EXIT_PROGRAM).sendToTarget();
                  }
              })
              .setNegativeButton("No", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int whichButton) {
                  }
              })
              .setCancelable(false)
              .show();
  }*/
  fun exitProgram() {
    finish()
  }

  private fun saveImage() {

    val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    if (EasyPermissions.hasPermissions(this, *perms)) {
      val serverIntent = Intent(this, SelectFileFormatActivity::class.java)
      serverIntent.putExtra("DEFAULT_FILE_FORMAT", BluetoothDataService.DATA_TYPE_RAWIMAGE)
      startActivityForResult(serverIntent, REQUEST_FILE_FORMAT)
    } else {
      // Do not have permissions, request them now
      EasyPermissions.requestPermissions(
        this, "Permiso necesario para almacenar las huellas",
        50, *perms
      )
    }
  }

  private fun saveImageByFileFormat(fileFormat: String, fileName: String) {

    // 0 - save bitmap file
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

  fun savePublicly(view: View?) {
    // Requesting Permission to access External Storage
    ActivityCompat.requestPermissions(
      this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
      25
    )
    // getExternalStoragePublicDirectory() represents root of external storage, we are using DOWNLOADS
    // We can use following directories: MUSIC, PODCASTS, ALARMS, RINGTONES, NOTIFICATIONS, PICTURES, MOVIES
    val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    // Storing the data in file with name as geeksData.txt
    // File file = new File(folder, "geeksData.txt");
    // writeTextData(file, editTextData);
    // editText.setText("");
  }

  // The Handler that gets information back from the BluetoothChatService
  private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
    override fun handleMessage(msg: Message) {
      when (msg.what) {
        MESSAGE_STATE_CHANGE -> when (msg.arg1) {
          BluetoothDataService.STATE_CONNECTED -> {
            mMessage!!.text = "Conectado al lector: "
            mMessage!!.append(mConnectedDeviceName)
          }
          BluetoothDataService.STATE_CONNECTING -> mMessage!!.text = "Connectando..."
          BluetoothDataService.STATE_LISTEN, BluetoothDataService.STATE_NONE -> {
            mMessage!!.text = "Esperando intento de conexión..."
            if (mConnected) {
              mConnected = false
              if (!mStop) {
                if (mBTService != null) {
                  mBTService!!.stop()
                  mBTService = null
                }
                if (mIn) setupCommunication()
              }
            } else mButtonOpen!!.isEnabled = true
          }
        }
        MESSAGE_SHOW_MSG -> {
          val showMsg = msg.obj as String
          mMessage!!.text = showMsg
        }
        MESSAGE_SHOW_PROGRESSBAR -> {
          mButtonOpen?.isEnabled = false
          mButtonSave?.isEnabled = false
          mProgressbar1?.progress = mStep
        }
        MESSAGE_SHOW_IMAGE -> {
          mProgressbar1!!.progress = 0
          if (mReceivedDataType == BluetoothDataService.DATA_TYPE_WSQIMAGE
            || mReceivedDataType == BluetoothDataService.DATA_TYPE_RAWIMAGE
          ) showBitmap() else mFingerImage!!.setImageBitmap(
            null
          )
          mButtonSave!!.isEnabled = true
          mButtonOpen!!.isEnabled = true
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
          mButtonOpen!!.isEnabled = false
          mButtonSave!!.isEnabled = false
        }
        MESSAGE_DATA_ERROR -> {
          mButtonOpen!!.isEnabled = true
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
              if (mConnected) {
                mConnected = false
                if (!mStop) {
                  if (mBTService != null) {
                    mBTService!!.stop()
                    mBTService = null
                  }
                  if (mIn) setupCommunication() else {
                    mStop = true
                    mButtonOpen!!.text = "Open BT Comm"
                    mRadioIn!!.isEnabled = true
                    mRadioOut!!.isEnabled = true
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
    /*if( mReceivedDataType == BluetoothDataService.DATA_TYPE_FT_SAMPLE )
  {
    mFingerImage.setImageBitmap(null);
    return;
  }*/
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
    if (D) Log.d(TAG, "onActivityResult $resultCode")
    when (requestCode) {
      REQUEST_ENABLE_BT ->                 // When the request to enable Bluetooth returns
        if (resultCode == RESULT_OK) {
          // Bluetooth is now enabled, so set up a session
          setupCommunication()
        } else {
          // User did not enable Bluetooth or an error occured
          Log.d(TAG, "BT not enabled")
          Toast.makeText(this, "BT not enabled", Toast.LENGTH_SHORT).show()
          finish()
        }
      REQUEST_CONNECT_DEVICE ->                 // When DeviceListActivity returns with a device to connect
        if (resultCode == RESULT_OK) {
          // Get the device MAC address
          val address = data!!.extras
            ?.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS)
          mStop = false
          setupCommunication()
          mButtonOpen!!.text = "Close BT Comm"
          mRadioIn!!.isEnabled = false
          mRadioOut!!.isEnabled = false
          // Get the BLuetoothDevice object
          val device = mBluetoothAdapter!!.getRemoteDevice(address)
          // Attempt to connect to the device
          mBTService!!.connect(device)
        } else mMessage!!.text = "No está conectado."
      REQUEST_FILE_FORMAT ->
        if (resultCode == RESULT_OK) {
          // Get the file format
          val extraString =
            data!!.extras!!.getStringArray(SelectFileFormatActivity.EXTRA_FILE_FORMAT)
          val fileFormat = extraString!![0]
          val fileName = extraString[1]
          saveImageByFileFormat(fileFormat, fileName)
        } else mMessage?.text = "Se canceló la operación"
    }
  }

  companion object {
    lateinit var mISOSample: ByteArray
    lateinit var mANSISample: ByteArray
    lateinit var mWsqImageFP: ByteArray

    /**
     * Called when the activity is first created.
     */
    // Debugging
    private const val TAG = "FS28V3.1"
    private const val D = false

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
    const val SHOW_MESSAGE = "show_message"
    const val TOAST = "toast"

    // Intent request codes
    private const val REQUEST_CONNECT_DEVICE = 1
    private const val REQUEST_ENABLE_BT = 2
    private const val REQUEST_FILE_FORMAT = 3

    @JvmField
    var mStop = true

    @JvmField
    var mConnected = false

    @JvmField
    var mStep = 0
    var mIn = true

    @JvmField
    var mImageFP = ByteArray(153602)
    private var mBitmapFP: Bitmap? = null

    @JvmField
    var mHostSample = ByteArray(669)

    //public static int mReceivedDataType = BluetoothDataService.DATA_TYPE_WSQIMAGE;
    @JvmField
    var mReceivedDataType = BluetoothDataService.DATA_TYPE_RAWIMAGE
  }
}