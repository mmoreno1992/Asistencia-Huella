package santaana.asistencia.di

import androidx.room.Room
import org.koin.dsl.module
import santaana.asistencia.db.AsistenciaDatabase

val databaseModule = module {
  single {
    Room.databaseBuilder(
      get(),
      AsistenciaDatabase::class.java,
      "ASISTENCIA_DB"
    )
  }
}