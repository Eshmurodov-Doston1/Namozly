package uz.dostonbek.namozly.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import uz.dostonbek.namozly.core.common.TimeProvider
import uz.dostonbek.namozly.core.database.dao.NotificationDao
import uz.dostonbek.namozly.core.database.dao.QazaDao
import uz.dostonbek.namozly.core.database.dao.TasbihDao
import uz.dostonbek.namozly.core.database.entity.NotificationEntity
import uz.dostonbek.namozly.core.database.entity.QazaEntity
import uz.dostonbek.namozly.core.database.entity.TasbihEntryEntity
import uz.dostonbek.namozly.core.datastore.NamozlyPreferencesDataSource
import uz.dostonbek.namozly.core.domain.repository.LeaderboardRepository
import uz.dostonbek.namozly.core.domain.repository.NotificationRepository
import uz.dostonbek.namozly.core.domain.repository.QazaRepository
import uz.dostonbek.namozly.core.domain.repository.SettingsRepository
import uz.dostonbek.namozly.core.domain.repository.TasbihRepository
import uz.dostonbek.namozly.core.model.AppLanguage
import uz.dostonbek.namozly.core.model.AppNotification
import uz.dostonbek.namozly.core.model.Leaderboard
import uz.dostonbek.namozly.core.model.LeaderboardEntry
import uz.dostonbek.namozly.core.model.LeaderboardPeriod
import uz.dostonbek.namozly.core.model.LeaderboardScope
import uz.dostonbek.namozly.core.model.NotificationSettings
import uz.dostonbek.namozly.core.model.NotificationType
import uz.dostonbek.namozly.core.model.PrayerSettings
import uz.dostonbek.namozly.core.model.QazaPrayer
import uz.dostonbek.namozly.core.model.QazaRecord
import uz.dostonbek.namozly.core.model.TasbihEntry
import uz.dostonbek.namozly.core.model.TasbihStats
import uz.dostonbek.namozly.core.model.TasbihStyle
import uz.dostonbek.namozly.core.model.ThemeMode
import uz.dostonbek.namozly.core.model.UserProfile
import uz.dostonbek.namozly.core.model.UserSettings
import java.time.DayOfWeek
import java.time.Instant
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val prefs: NamozlyPreferencesDataSource,
) : SettingsRepository {
    override val settings: Flow<UserSettings> = prefs.settings
    override suspend fun setLanguage(language: AppLanguage) = prefs.setLanguage(language).let { }
    override val languageChosenByUser: Flow<Boolean> = prefs.languageManual
    override suspend fun setLanguageAuto(language: AppLanguage) = prefs.setLanguageAuto(language).let { }
    override suspend fun setThemeMode(mode: ThemeMode) = prefs.setTheme(mode)
    override suspend fun setPrayerSettings(settings: PrayerSettings) = prefs.setPrayerSettings(settings)
    override suspend fun setNotificationSettings(settings: NotificationSettings) = prefs.setNotificationSettings(settings)
    override suspend fun setTasbihStyle(style: TasbihStyle) = prefs.setTasbihStyle(style)
    override suspend fun setTasbihTarget(target: Int) = prefs.setTasbihTarget(target)
    override suspend fun setHaptics(enabled: Boolean) = prefs.setHaptics(enabled)
    override suspend fun setOnboardingDone() = prefs.setOnboardingDone()
}

@Singleton
class TasbihRepositoryImpl @Inject constructor(
    private val dao: TasbihDao,
    private val time: TimeProvider,
) : TasbihRepository {
    override fun observeStats(): Flow<TasbihStats> = dao.observeAll().map { all ->
        val zone = time.zone()
        val today = time.today()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        fun dateOf(ts: Long) = Instant.ofEpochMilli(ts).atZone(zone).toLocalDate()
        TasbihStats(
            today = all.filter { dateOf(it.timestamp) == today }.sumOf { it.count },
            week = all.filter { !dateOf(it.timestamp).isBefore(weekStart) }.sumOf { it.count },
            month = all.filter { dateOf(it.timestamp).let { d -> d.year == today.year && d.month == today.month } }.sumOf { it.count },
            total = all.sumOf { it.count },
            perDhikr = all.groupBy { it.dhikrId }.mapValues { (_, v) -> v.sumOf { it.count } },
        )
    }

    override fun observeRecent(limit: Int): Flow<List<TasbihEntry>> = dao.observeRecent(limit).map { list ->
        list.map { TasbihEntry(it.id, it.dhikrId, it.count, it.target, it.timestamp) }
    }

    override suspend fun save(entry: TasbihEntry) {
        if (entry.count <= 0) return
        dao.insert(TasbihEntryEntity(dhikrId = entry.dhikrId, count = entry.count, target = entry.target, timestamp = entry.timestamp))
    }
}

@Singleton
class QazaRepositoryImpl @Inject constructor(
    private val dao: QazaDao,
) : QazaRepository {
    override fun observe(): Flow<List<QazaRecord>> = dao.observeAll().map { list ->
        val byName = list.associateBy { it.prayer }
        QazaPrayer.entries.map { p ->
            val e = byName[p.name]
            QazaRecord(p, e?.remaining ?: 0, e?.completed ?: 0)
        }
    }

    override suspend fun setRemaining(prayer: QazaPrayer, remaining: Int) {
        val e = dao.get(prayer.name)
        dao.put(QazaEntity(prayer.name, remaining.coerceAtLeast(0), e?.completed ?: 0))
    }

    override suspend fun complete(prayer: QazaPrayer, count: Int) {
        val e = dao.get(prayer.name) ?: QazaEntity(prayer.name, 0, 0)
        val done = count.coerceAtMost(e.remaining).coerceAtLeast(0)
        dao.put(e.copy(remaining = e.remaining - done, completed = e.completed + done))
    }

    override suspend fun undo(prayer: QazaPrayer) {
        val e = dao.get(prayer.name) ?: return
        if (e.completed <= 0) return
        dao.put(e.copy(remaining = e.remaining + 1, completed = e.completed - 1))
    }
}

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val dao: NotificationDao,
    private val time: TimeProvider,
) : NotificationRepository {
    override fun observe(): Flow<List<AppNotification>> = dao.observeAll().map { list ->
        list.map {
            AppNotification(
                id = it.id,
                type = NotificationType.entries.firstOrNull { t -> t.name == it.type } ?: NotificationType.SYSTEM,
                title = it.title, body = it.body, timestamp = it.timestamp, read = it.read,
            )
        }
    }
    override fun unreadCount(): Flow<Int> = dao.unreadCount()
    override suspend fun add(type: NotificationType, title: String, body: String) =
        dao.insert(NotificationEntity(type = type.name, title = title, body = body, timestamp = time.millis()))
    override suspend fun markAllRead() = dao.markAllRead()
    override suspend fun clear() = dao.clear()
}

/**
 * Lokal (Firebase'siz) musobaqa: profil qurilmada saqlanadi, reytingda faqat foydalanuvchining
 * o'zi ko'rinadi va maxfiylik bayrog'i yoqilgan bo'ladi. Firebase ulanganda bu klass
 * FirestoreLeaderboardRepository bilan almashtiriladi (DataModule'dagi @Binds).
 */
@Singleton
class LocalLeaderboardRepository @Inject constructor(
    private val prefs: NamozlyPreferencesDataSource,
    private val tasbih: TasbihRepository,
) : LeaderboardRepository {
    override val profile: Flow<UserProfile?> = prefs.profile

    override suspend fun register(profile: UserProfile) = prefs.setProfile(profile)

    override suspend fun signOut() = prefs.setProfile(null)

    override fun leaderboard(scope: LeaderboardScope, period: LeaderboardPeriod): Flow<Leaderboard> =
        combine(prefs.profile, tasbih.observeStats()) { profile, stats ->
            val count = when (period) {
                LeaderboardPeriod.DAY -> stats.today
                LeaderboardPeriod.WEEK -> stats.week
                LeaderboardPeriod.MONTH -> stats.month
            }.toLong()
            val area = when (scope) {
                LeaderboardScope.MAHALLA -> profile?.mahalla
                LeaderboardScope.DISTRICT -> profile?.district
                LeaderboardScope.REGION -> profile?.region
                LeaderboardScope.COUNTRY -> profile?.countryCode
            }.orEmpty()
            Leaderboard(
                scope = scope,
                period = period,
                areaName = area,
                entries = if (profile == null) emptyList()
                else listOf(LeaderboardEntry(1, profile.publicName, count, isMe = true)),
                hiddenForPrivacy = true,
            )
        }

    override suspend fun submit(count: Int) {
        // Lokal rejimda tasbeh yozuvlari TasbihRepository orqali allaqachon saqlanadi.
    }
}
