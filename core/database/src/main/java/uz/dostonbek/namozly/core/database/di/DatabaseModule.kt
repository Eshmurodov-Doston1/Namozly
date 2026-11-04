package uz.dostonbek.namozly.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import uz.dostonbek.namozly.core.database.NamozlyDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NamozlyDatabase =
        Room.databaseBuilder(context, NamozlyDatabase::class.java, "namozly.db")
            .addMigrations(NamozlyDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides fun prayerDayDao(db: NamozlyDatabase) = db.prayerDayDao()
    @Provides fun ayahDao(db: NamozlyDatabase) = db.ayahDao()
    @Provides fun bookPageDao(db: NamozlyDatabase) = db.bookPageDao()
    @Provides fun jsonCacheDao(db: NamozlyDatabase) = db.jsonCacheDao()
    @Provides fun tasbihDao(db: NamozlyDatabase) = db.tasbihDao()
    @Provides fun qazaDao(db: NamozlyDatabase) = db.qazaDao()
    @Provides fun notificationDao(db: NamozlyDatabase) = db.notificationDao()
}
