package santaana.asistencia

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import santaana.asistencia.di.databaseModule
import santaana.asistencia.di.networkModule

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this);
        startKoin {
            androidContext(this@App)
            modules(databaseModule, networkModule)
        }
    }
}