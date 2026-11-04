package uz.dostonbek.namozly.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uz.dostonbek.namozly.core.data.repository.BookmarkRepositoryImpl
import uz.dostonbek.namozly.core.domain.repository.BookmarkRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class BookmarkModule {
    @Binds abstract fun bindBookmarks(impl: BookmarkRepositoryImpl): BookmarkRepository
}
