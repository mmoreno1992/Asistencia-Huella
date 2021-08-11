package santaana.asistencia

import android.os.Environment

fun isExternalStorageWritable(): Boolean {
  return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}