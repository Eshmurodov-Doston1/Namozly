package uz.dostonbek.namozly.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uz.dostonbek.namozly.core.data.repository.RadioRepositoryImpl
import uz.dostonbek.namozly.core.domain.repository.RadioRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RadioModule {
    @Binds abstract fun bindRadio(impl: RadioRepositoryImpl): RadioRepository
}
