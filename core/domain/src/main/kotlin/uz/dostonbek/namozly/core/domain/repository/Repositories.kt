package uz.dostonbek.namozly.core.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import uz.dostonbek.namozly.core.model.AllahName
import uz.dostonbek.namozly.core.model.AppLanguage
import uz.dostonbek.namozly.core.model.AppNotification
import uz.dostonbek.namozly.core.model.Ayah
import uz.dostonbek.namozly.core.model.DailyPrayerTimes
import uz.dostonbek.namozly.core.model.Dhikr
import uz.dostonbek.namozly.core.model.Dua
import uz.dostonbek.namozly.core.model.Gender
import uz.dostonbek.namozly.core.model.GeoPoint
import uz.dostonbek.namozly.core.model.IslamicEvent
import uz.dostonbek.namozly.core.model.Leaderboard
import uz.dostonbek.namozly.core.model.LeaderboardPeriod
import uz.dostonbek.namozly.core.model.LeaderboardScope
import uz.dostonbek.namozly.core.model.LiveChannel
import uz.dostonbek.namozly.core.model.Mosque
import uz.dostonbek.namozly.core.model.NotificationSettings
import uz.dostonbek.namozly.core.model.NotificationType
import uz.dostonbek.namozly.core.model.PlaybackState
import uz.dostonbek.namozly.core.model.PrayerGuide
import uz.dostonbek.namozly.core.model.PrayerSettings
import uz.dostonbek.namozly.core.model.QazaPrayer
import uz.dostonbek.namozly.core.model.QazaRecord
import uz.dostonbek.namozly.core.model.Surah
import uz.dostonbek.namozly.core.model.TasbihEntry
import uz.dostonbek.namozly.core.model.TasbihStats
import uz.dostonbek.namozly.core.model.TasbihStyle
import uz.dostonbek.namozly.core.model.ThemeMode
import uz.dostonbek.namozly.core.model.UserLocation
import uz.dostonbek.namozly.core.model.UserProfile
import uz.dostonbek.namozly.core.model.UserSettings
import java.time.LocalDate

interface SettingsRepository {
    val settings: Flow<UserSettings>
    /** Foydalanuvchi tanlovi — bundan keyin til avtomatik o'zgarmaydi. */
    suspend fun setLanguage(language: AppLanguage)
    /** true — til qo'lda tanlangan; false — mamlakatga qarab avtomatik. */
    val languageChosenByUser: Flow<Boolean>
    /** Mamlakatga qarab avtomatik til (qo'lda tanlov bo'lmaganda). */
    suspend fun setLanguageAuto(language: AppLanguage)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setPrayerSettings(settings: PrayerSettings)
    suspend fun setNotificationSettings(settings: NotificationSettings)
    suspend fun setTasbihStyle(style: TasbihStyle)
    suspend fun setTasbihTarget(target: Int)
    suspend fun setHaptics(enabled: Boolean)
    suspend fun setOnboardingDone()
}

interface LocationRepository {
    val location: Flow<UserLocation>
    /** GPS orqali joylashuvni yangilaydi. Ruxsat yo'q bo'lsa false qaytaradi. */
    suspend fun refreshFromDevice(): Boolean
    suspend fun setManual(location: UserLocation)
    suspend fun search(query: String): List<UserLocation>
}

interface PrayerTimesRepository {
    /** Joriy joylashuv + sozlamalar bo'yicha kunlik vaqtlar (sozlama o'zgarsa qayta hisoblanadi). */
    fun observeDay(date: LocalDate): Flow<DailyPrayerTimes>
    fun observeMonth(year: Int, month: Int): Flow<List<DailyPrayerTimes>>
    /** Bildirishnomalarni rejalashtirish uchun (oflayn ham ishlaydi). */
    suspend fun getRange(from: LocalDate, days: Int): List<DailyPrayerTimes>
    /** Fon yangilash: joriy va keyingi oylar vaqtlarini tarmoqdan olib saqlaydi. */
    suspend fun refresh(months: Int = 2)
}

interface QuranRepository {
    fun surahs(): List<Surah>
    fun observeAyahs(surah: Int, language: AppLanguage): Flow<List<Ayah>>
    /** Tarmoqdan yuklab, Room'ga saqlaydi. */
    suspend fun sync(surah: Int, language: AppLanguage): kotlin.Result<Unit>
    suspend fun ayah(surah: Int, ayah: Int, language: AppLanguage): Ayah?
    val lastRead: Flow<Pair<Int, Int>?>
    suspend fun setLastRead(surah: Int, ayah: Int)
}

/** Qur'on audio pleeri — core:media'da ExoPlayer bilan amalga oshirilgan. */
interface QuranPlayer {
    val state: StateFlow<PlaybackState>
    fun playSurah(surah: Int)
    fun playAyah(surah: Int, ayah: Int, globalNumber: Int)
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(positionMs: Long)
    fun setSpeed(speed: Float)
    fun playStream(url: String, title: String)
}

interface RadioRepository {
    suspend fun stations(): List<uz.dostonbek.namozly.core.model.RadioStation>
}

interface ContentRepository {
    fun allahNames(): List<AllahName>
    fun duas(): List<Dua>
    fun prayerGuides(gender: Gender): List<PrayerGuide>
    fun lessons(gender: Gender): List<uz.dostonbek.namozly.core.model.Lesson>
    fun liveChannels(): List<LiveChannel>
    fun islamicEvents(): List<IslamicEvent>
    fun dhikrs(): List<Dhikr>
}

interface MosqueRepository {
    suspend fun nearby(point: GeoPoint, radiusMeters: Int = 3000): kotlin.Result<List<Mosque>>
    suspend fun worldFamous(): List<Mosque>
}

interface TasbihRepository {
    fun observeStats(): Flow<TasbihStats>
    fun observeRecent(limit: Int = 20): Flow<List<TasbihEntry>>
    suspend fun save(entry: TasbihEntry)
}

interface QazaRepository {
    fun observe(): Flow<List<QazaRecord>>
    suspend fun setRemaining(prayer: QazaPrayer, remaining: Int)
    suspend fun complete(prayer: QazaPrayer, count: Int = 1)
    suspend fun undo(prayer: QazaPrayer)
}

interface NotificationRepository {
    fun observe(): Flow<List<AppNotification>>
    fun unreadCount(): Flow<Int>
    suspend fun add(type: NotificationType, title: String, body: String)
    suspend fun markAllRead()
    suspend fun clear()
}

/** Namoz bildirishnomalarini rejalashtiruvchi (core:notifications). */
interface PrayerAlarmScheduler {
    suspend fun rescheduleAll()
    fun cancelAll()
}

/**
 * Tasbeh musobaqasi. Hozircha lokal amalga oshirilgan; Firebase (Phone Auth + Firestore)
 * ulanganda faqat shu interfeys implementatsiyasi almashtiriladi.
 */
interface LeaderboardRepository {
    val profile: Flow<UserProfile?>
    suspend fun register(profile: UserProfile)
    suspend fun signOut()
    fun leaderboard(scope: LeaderboardScope, period: LeaderboardPeriod): Flow<Leaderboard>
    suspend fun submit(count: Int)
}
