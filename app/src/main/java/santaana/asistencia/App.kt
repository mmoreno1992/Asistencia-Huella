package santaana.asistencia

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import santaana.asistencia.di.databaseModule

class App : Application() {

  override fun onCreate() {
    super.onCreate()
    startKoin {
      // declare used Android context
      androidContext(this@App)
      // declare modules
      modules(databaseModule)
    }
  }
}