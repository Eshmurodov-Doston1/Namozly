package uz.dostonbek.namozly.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import uz.dostonbek.namozly.core.database.entity.AyahEntity
import uz.dostonbek.namozly.core.database.entity.BookPageEntity
import uz.dostonbek.namozly.core.database.entity.JsonCacheEntity
import uz.dostonbek.namozly.core.database.entity.NotificationEntity
import uz.dostonbek.namozly.core.database.entity.PrayerDayEntity
import uz.dostonbek.namozly.core.database.entity.QazaEntity
import uz.dostonbek.namozly.core.database.entity.TasbihEntryEntity

@Dao
interface PrayerDayDao {
    @Query("SELECT * FROM prayer_days WHERE locationKey = :key AND date = :date LIMIT 1")
    fun observe(key: String, date: String): Flow<PrayerDayEntity?>

    @Query("SELECT * FROM prayer_days WHERE locationKey = :key AND date BETWEEN :from AND :to ORDER BY date")
    fun observeRange(key: String, from: String, to: String): Flow<List<PrayerDayEntity>>

    @Query("SELECT * FROM prayer_days WHERE locationKey = :key AND date BETWEEN :from AND :to ORDER BY date")
    suspend fun range(key: String, from: String, to: String): List<PrayerDayEntity>

    @Upsert
    suspend fun upsert(days: List<PrayerDayEntity>)

    @Query("DELETE FROM prayer_days WHERE fetchedAt < :before")
    suspend fun deleteOlderThan(before: Long)
}

@Dao
interface AyahDao {
    @Query("SELECT * FROM ayahs WHERE surah = :surah AND language = :language ORDER BY number")
    fun observeSurah(surah: Int, language: String): Flow<List<AyahEntity>>

    @Query("SELECT COUNT(*) FROM ayahs WHERE surah = :surah AND language = :language")
    suspend fun count(surah: Int, language: String): Int

    @Query("SELECT * FROM ayahs WHERE surah = :surah AND number = :number AND language = :language LIMIT 1")
    suspend fun get(surah: Int, number: Int, language: String): AyahEntity?

    @Upsert
    suspend fun upsert(ayahs: List<AyahEntity>)
}

@Dao
interface TasbihDao {
    @Insert
    suspend fun insert(entry: TasbihEntryEntity)

    @Query("SELECT * FROM tasbih_entries ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<TasbihEntryEntity>>

    @Query("SELECT * FROM tasbih_entries")
    fun observeAll(): Flow<List<TasbihEntryEntity>>
}

@Dao
interface QazaDao {
    @Query("SELECT * FROM qaza")
    fun observeAll(): Flow<List<QazaEntity>>

    @Query("SELECT * FROM qaza WHERE prayer = :prayer LIMIT 1")
    suspend fun get(prayer: String): QazaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entity: QazaEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC LIMIT 200")
    fun observeAll(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE read = 0")
    fun unreadCount(): Flow<Int>

    @Insert
    suspend fun insert(entity: NotificationEntity)

    @Query("UPDATE notifications SET read = 1")
    suspend fun markAllRead()

    @Query("DELETE FROM notifications")
    suspend fun clear()
}

@Dao
interface BookPageDao {
    @Query("SELECT * FROM book_pages WHERE bookId = :bookId AND number = :number AND language = :language LIMIT 1")
    suspend fun get(bookId: String, number: Int, language: String): BookPageEntity?

    @Query("SELECT COUNT(*) FROM book_pages WHERE bookId = :bookId AND language = :language")
    suspend fun count(bookId: String, language: String): Int

    @Query("SELECT number FROM book_pages WHERE bookId = :bookId AND language = :language")
    suspend fun numbers(bookId: String, language: String): List<Int>

    @Upsert
    suspend fun upsert(page: BookPageEntity)
}

@Dao
interface JsonCacheDao {
    @Query("SELECT * FROM json_cache WHERE cacheKey = :key LIMIT 1")
    suspend fun get(key: String): JsonCacheEntity?

    @Upsert
    suspend fun put(entity: JsonCacheEntity)
}
