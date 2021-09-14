package santaana.asistencia.di

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.koin.dsl.module
import santaana.asistencia.db.AsistenciaDatabase
import santaana.asistencia.guardadas.AsistenciaRepository

val databaseModule = module {
    single {
        Room.databaseBuilder(
            get(),
            AsistenciaDatabase::class.java,
            "ASISTENCIA_DB"
        ).addMigrations(
            object : Migration(1, 2) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE AsistenciaEmpleado ADD Column Enviado text")
                }

            }
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