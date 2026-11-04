package uz.dostonbek.namozly.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uz.dostonbek.namozly.core.data.repository.BookRepositoryImpl
import uz.dostonbek.namozly.core.domain.repository.BookRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class BookModule {
    @Binds abstract fun bindBooks(impl: BookRepositoryImpl): BookRepository
}
