package santaana.asistencia.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import santaana.asistencia.R
import santaana.asistencia.guardadas.AsistenciaRepository

class SincronizationService : Service(), KoinComponent {

    val repository: AsistenciaRepository by inject()

    var isRunning = false

    override fun onBind(intent: Intent?) = null

    private lateinit var notificationManager: NotificationManager

    companion object {
        const val ENVIA_INFO_CHANNEL_NAME = "ENVIA_ASISTENCIA_CHANNEL"
        const val ENVIA_INFO_CHANNEL_ID = "ASISTENCIA_CHANNEL"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            isRunning = true
            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            showNotification()
            GlobalScope.launch {
                sincronizaAsistencia()
            }
        }
        return START_NOT_STICKY
    }

    private suspend fun sincronizaAsistencia() {
        val list = repository.getRegistrosNoEnviados()
        list.forEach {

        }
    }

    private fun showNotification() {
        createNotificationChannel()
        startForeground(1, getEnviaInfoNotification())
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChanel = NotificationChannel(
                ENVIA_INFO_CHANNEL_ID,
                ENVIA_INFO_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChanel)
        }
    }

    private fun getEnviaInfoNotification() =
        NotificationCompat.Builder(this, ENVIA_INFO_CHANNEL_ID)
            .setContentTitle("Sincronizando Asistencias.")
            .setContentText("Enviando...")
            .setOngoing(true)
            .setProgress(100, 50, true)
            .setSmallIcon(R.drawable.ic_stat_name)
            .build()

}