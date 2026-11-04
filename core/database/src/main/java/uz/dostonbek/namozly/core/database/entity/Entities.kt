package uz.dostonbek.namozly.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * API'dan olingan kunlik vaqtlar keshi. [locationKey] — koordinata (2 xona) + method + madhab,
 * shuning uchun joy yoki sozlama o'zgarsa eski kesh ishlatilmaydi.
 */
@Entity(tableName = "prayer_days", primaryKeys = ["locationKey", "date"])
data class PrayerDayEntity(
    val locationKey: String,
    /** ISO yyyy-MM-dd */
    val date: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val hijriDay: Int,
    val hijriMonth: Int,
    val hijriYear: Int,
    val fetchedAt: Long,
)

@Entity(tableName = "ayahs", primaryKeys = ["surah", "number", "language"])
data class AyahEntity(
    val surah: Int,
    val number: Int,
    val language: String,
    val globalNumber: Int,
    val arabic: String,
    val translation: String,
)

@Entity(tableName = "tasbih_entries", indices = [Index("timestamp")])
data class TasbihEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dhikrId: String,
    val count: Int,
    val target: Int,
    val timestamp: Long,
)

@Entity(tableName = "qaza")
data class QazaEntity(
    @PrimaryKey val prayer: String,
    val remaining: Int,
    val completed: Int,
)

@Entity(tableName = "notifications", indices = [Index("timestamp")])
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val title: String,
    val body: String,
    val timestamp: Long,
    val read: Boolean = false,
)

/** Kitob sahifasi (hadis) — oflayn o'qish uchun til bo'yicha saqlanadi. */
@Entity(tableName = "book_pages", primaryKeys = ["bookId", "number", "language"])
data class BookPageEntity(
    val bookId: String,
    val number: Int,
    val language: String,
    val arabic: String,
    val translation: String,
    val section: String?,
)

/** Umumiy oflayn kesh: tarmoq javoblari (masjidlar, radio va h.k.) JSON ko'rinishida. */
@Entity(tableName = "json_cache")
data class JsonCacheEntity(
    @PrimaryKey val cacheKey: String,
    val json: String,
    val updatedAt: Long,
)
