package santaana.asistencia

val TAG = "FINGER_PRINT_APP"
const val MESSAGE_STATE_CHANGE = 1
const val MESSAGE_DEVICE_NAME = 4
const val DEVICE_NAME = "device_name"
const val MESSAGE_TOAST = 5
const val MESSAGE_SHOW_MSG = 6
const val MESSAGE_SHOW_IMAGE = 7
const val MESSAGE_SHOW_PROGRESSBAR = 8
const val MESSAGE_DATA_RECEIVING = 9
const val MESSAGE_DATA_ERROR = 10
const val MESSAGE_EXIT_PROGRAM = 11
const val TOAST = "toast"

@JvmField
var mStop = true

@JvmField
var mConnected = false