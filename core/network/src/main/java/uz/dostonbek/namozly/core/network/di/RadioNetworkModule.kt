package uz.dostonbek.namozly.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import uz.dostonbek.namozly.core.network.api.RadioService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RadioNetworkModule {
    @Provides
    @Singleton
    fun provideRadioService(client: OkHttpClient, json: Json): RadioService = Retrofit.Builder()
        .baseUrl(RadioService.BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(RadioService::class.java)
}
