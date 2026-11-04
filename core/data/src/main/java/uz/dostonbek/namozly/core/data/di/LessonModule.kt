package uz.dostonbek.namozly.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uz.dostonbek.namozly.core.data.repository.LessonProgressRepositoryImpl
import uz.dostonbek.namozly.core.domain.repository.LessonProgressRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class LessonModule {
    @Binds abstract fun bindLessonProgress(impl: LessonProgressRepositoryImpl): LessonProgressRepository
}
