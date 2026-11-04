package uz.dostonbek.namozly.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uz.dostonbek.namozly.core.data.repository.PrayerLogRepositoryImpl
import uz.dostonbek.namozly.core.domain.repository.PrayerLogRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class PrayerLogModule {
    @Binds
    abstract fun bindPrayerLog(impl: PrayerLogRepositoryImpl): PrayerLogRepository
}
