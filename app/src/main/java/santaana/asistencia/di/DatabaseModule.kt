package santaana.asistencia.di

import androidx.room.Room
import org.koin.dsl.module
import santaana.asistencia.db.AsistenciaDatabase
import santaana.asistencia.guardadas.AsistenciaRepository

val databaseModule = module {
    single {
        Room.databaseBuilder(
            get(),
            AsistenciaDatabase::class.java,
            "ASISTENCIA_DB"
        ).build()
    }

    single {
        get<AsistenciaDatabase>().getAsistenciaEmpleadoDao()
    }
    single {
        get<AsistenciaDatabase>().getArchivoHuellaEmpleadoDao()
    }

    single {
        AsistenciaRepository(get())
    }

}