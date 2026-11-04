package uz.dostonbek.namozly.core.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import uz.dostonbek.namozly.core.common.ApplicationScope
import uz.dostonbek.namozly.core.common.Dispatcher
import uz.dostonbek.namozly.core.common.NamozlyDispatchers
import uz.dostonbek.namozly.core.common.SystemTimeProvider
import uz.dostonbek.namozly.core.common.TimeProvider
import uz.dostonbek.namozly.core.data.location.LocationRepositoryImpl
import uz.dostonbek.namozly.core.data.prayer.PrayerTimesRepositoryImpl
import uz.dostonbek.namozly.core.data.repository.ContentRepositoryImpl
import uz.dostonbek.namozly.core.data.repository.LocalLeaderboardRepository
import uz.dostonbek.namozly.core.data.repository.MosqueRepositoryImpl
import uz.dostonbek.namozly.core.data.repository.NotificationRepositoryImpl
import uz.dostonbek.namozly.core.data.repository.QazaRepositoryImpl
import uz.dostonbek.namozly.core.data.repository.QuranRepositoryImpl
import uz.dostonbek.namozly.core.data.repository.SettingsRepositoryImpl
import uz.dostonbek.namozly.core.data.repository.TasbihRepositoryImpl
import uz.dostonbek.namozly.core.domain.repository.ContentRepository
import uz.dostonbek.namozly.core.domain.repository.LeaderboardRepository
import uz.dostonbek.namozly.core.domain.repository.LocationRepository
import uz.dostonbek.namozly.core.domain.repository.MosqueRepository
import uz.dostonbek.namozly.core.domain.repository.NotificationRepository
import uz.dostonbek.namozly.core.domain.repository.PrayerTimesRepository
import uz.dostonbek.namozly.core.domain.repository.QazaRepository
import uz.dostonbek.namozly.core.domain.repository.QuranRepository
import uz.dostonbek.namozly.core.domain.repository.SettingsRepository
import uz.dostonbek.namozly.core.domain.repository.TasbihRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds abstract fun settings(impl: SettingsRepositoryImpl): SettingsRepository
    @Binds abstract fun location(impl: LocationRepositoryImpl): LocationRepository
    @Binds abstract fun prayerTimes(impl: PrayerTimesRepositoryImpl): PrayerTimesRepository
    @Binds abstract fun quran(impl: QuranRepositoryImpl): QuranRepository
    @Binds abstract fun content(impl: ContentRepositoryImpl): ContentRepository
    @Binds abstract fun mosques(impl: MosqueRepositoryImpl): MosqueRepository
    @Binds abstract fun tasbih(impl: TasbihRepositoryImpl): TasbihRepository
    @Binds abstract fun qaza(impl: QazaRepositoryImpl): QazaRepository
    @Binds abstract fun notifications(impl: NotificationRepositoryImpl): NotificationRepository
    @Binds abstract fun leaderboard(impl: LocalLeaderboardRepository): LeaderboardRepository
}

@Module
@InstallIn(SingletonComponent::class)
object CoroutinesModule {
    @Provides
    @Dispatcher(NamozlyDispatchers.IO)
    fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Dispatcher(NamozlyDispatchers.Default)
    fun defaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Singleton
    @ApplicationScope
    fun applicationScope(@Dispatcher(NamozlyDispatchers.Default) dispatcher: CoroutineDispatcher): CoroutineScope =
        CoroutineScope(SupervisorJob() + dispatcher)

    @Provides
    @Singleton
    fun timeProvider(): TimeProvider = SystemTimeProvider()
}
