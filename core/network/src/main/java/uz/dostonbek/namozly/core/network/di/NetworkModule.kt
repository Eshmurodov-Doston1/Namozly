package uz.dostonbek.namozly.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import uz.dostonbek.namozly.core.network.BuildConfig
import uz.dostonbek.namozly.core.network.api.AlQuranService
import uz.dostonbek.namozly.core.network.api.AladhanService
import uz.dostonbek.namozly.core.network.api.NominatimService
import uz.dostonbek.namozly.core.network.api.OverpassService
import uz.dostonbek.namozly.core.network.api.QuranEncService
import uz.dostonbek.namozly.core.network.api.WikipediaService
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            // OSM xizmatlari (Nominatim/Overpass) identifikatsiya qiluvchi User-Agent talab qiladi.
            chain.proceed(
                chain.request().newBuilder()
                    .header("User-Agent", "Namozly-Android/1.0 (uz.dostonbek.namozly)")
                    .build(),
            )
        }
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            }
        }
        .build()

    private inline fun <reified T> Retrofit.Builder.createService(url: String): T =
        baseUrl(url).build().create(T::class.java)

    private fun builder(client: OkHttpClient, json: Json): Retrofit.Builder = Retrofit.Builder()
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))

    @Provides @Singleton
    fun provideAladhan(client: OkHttpClient, json: Json): AladhanService =
        builder(client, json).createService(AladhanService.BASE_URL)

    @Provides @Singleton
    fun provideAlQuran(client: OkHttpClient, json: Json): AlQuranService =
        builder(client, json).createService(AlQuranService.BASE_URL)

    @Provides @Singleton
    fun provideQuranEnc(client: OkHttpClient, json: Json): QuranEncService =
        builder(client, json).createService(QuranEncService.BASE_URL)

    @Provides @Singleton
    fun provideOverpass(client: OkHttpClient, json: Json): OverpassService =
        builder(client, json).createService(OverpassService.BASE_URL)

    @Provides @Singleton
    fun provideNominatim(client: OkHttpClient, json: Json): NominatimService =
        builder(client, json).createService(NominatimService.BASE_URL)

    /** Inglizcha Wikipedia — eng ko'p masjid sahifalari shu yerda. */
    @Provides @Singleton
    fun provideWikipedia(client: OkHttpClient, json: Json): WikipediaService =
        builder(client, json).createService(WikipediaService.baseUrl("en"))
}
