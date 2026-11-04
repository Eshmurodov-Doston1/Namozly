package uz.dostonbek.namozly.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import uz.dostonbek.namozly.core.network.api.MyMemoryService
import uz.dostonbek.namozly.core.network.api.TranslateService
import javax.inject.Singleton

/** Javob ResponseBody sifatida olinadi — konverter kerak emas. */
@Module
@InstallIn(SingletonComponent::class)
object TranslateNetworkModule {
    @Provides
    @Singleton
    fun provideTranslateService(client: OkHttpClient): TranslateService = Retrofit.Builder()
        .baseUrl(TranslateService.BASE_URL)
        .client(client)
        .build()
        .create(TranslateService::class.java)

    @Provides
    @Singleton
    fun provideMyMemoryService(client: OkHttpClient): MyMemoryService = Retrofit.Builder()
        .baseUrl(MyMemoryService.BASE_URL)
        .client(client)
        .build()
        .create(MyMemoryService::class.java)
}
