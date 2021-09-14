package santaana.asistencia.di

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import santaana.asistencia.BuildConfig
import santaana.asistencia.networking.AsistenciaApi
import java.util.concurrent.TimeUnit

val networkModule = module {
    single {
        Retrofit.Builder()
            .baseUrl(get<String>(qualifier = named("URL_SERVICE")))
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single {
        val client = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            client.addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }
        client.build()
    }

    single {
        get<Retrofit>().create(AsistenciaApi::class.java)
    }

    single(named("URL_SERVICE")) { "http://190.143.138.226:1818/OTAg/" }
}