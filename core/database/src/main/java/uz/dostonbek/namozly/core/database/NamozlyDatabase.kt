package uz.dostonbek.namozly.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import uz.dostonbek.namozly.core.database.dao.BookPageDao
import uz.dostonbek.namozly.core.database.dao.JsonCacheDao
import uz.dostonbek.namozly.core.database.entity.BookPageEntity
import uz.dostonbek.namozly.core.database.entity.JsonCacheEntity
import uz.dostonbek.namozly.core.database.dao.AyahDao
import uz.dostonbek.namozly.core.database.dao.NotificationDao
import uz.dostonbek.namozly.core.database.dao.PrayerDayDao
import uz.dostonbek.namozly.core.database.dao.QazaDao
import uz.dostonbek.namozly.core.database.dao.TasbihDao
import uz.dostonbek.namozly.core.database.entity.AyahEntity
import uz.dostonbek.namozly.core.database.entity.NotificationEntity
import uz.dostonbek.namozly.core.database.entity.PrayerDayEntity
import uz.dostonbek.namozly.core.database.entity.QazaEntity
import uz.dostonbek.namozly.core.database.entity.TasbihEntryEntity

@Database(
    entities = [
        PrayerDayEntity::class,
        AyahEntity::class,
        TasbihEntryEntity::class,
        QazaEntity::class,
        NotificationEntity::class,
        BookPageEntity::class,
        JsonCacheEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class NamozlyDatabase : RoomDatabase() {
    abstract fun prayerDayDao(): PrayerDayDao
    abstract fun ayahDao(): AyahDao
    abstract fun tasbihDao(): TasbihDao
    abstract fun qazaDao(): QazaDao
    abstract fun notificationDao(): NotificationDao
    abstract fun bookPageDao(): BookPageDao
    abstract fun jsonCacheDao(): JsonCacheDao

    companion object {
        /** 1 → 2: kitob sahifalari va oflayn kesh jadvallari (mavjud tasbeh/qazo ma'lumotlari saqlanadi). */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `book_pages` (`bookId` TEXT NOT NULL, `number` INTEGER NOT NULL, `language` TEXT NOT NULL, " +
                        "`arabic` TEXT NOT NULL, `translation` TEXT NOT NULL, `section` TEXT, PRIMARY KEY(`bookId`, `number`, `language`))",
                )
                db.execSQL("CREATE TABLE IF NOT EXISTS `json_cache` (`cacheKey` TEXT NOT NULL, `json` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`cacheKey`))")
            }
        }
    }
}
