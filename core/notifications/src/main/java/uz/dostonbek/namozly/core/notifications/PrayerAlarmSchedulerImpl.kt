package uz.dostonbek.namozly.core.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import uz.dostonbek.namozly.core.common.TimeProvider
import uz.dostonbek.namozly.core.domain.repository.PrayerAlarmScheduler
import uz.dostonbek.namozly.core.domain.repository.PrayerTimesRepository
import uz.dostonbek.namozly.core.domain.repository.SettingsRepository
import uz.dostonbek.namozly.core.domain.util.HijriCalendar
import uz.dostonbek.namozly.core.model.Prayer
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keyingi 2 kun uchun namoz signallarini AlarmManager orqali rejalashtiradi.
 * Har bir signal kelganda (va qurilma qayta yoqilganda) jadval yangilanadi — WorkManager kerak emas.
 */
@Singleton
class PrayerAlarmSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prayerTimes: PrayerTimesRepository,
    private val settings: SettingsRepository,
    private val time: TimeProvider,
) : PrayerAlarmScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override suspend fun rescheduleAll() {
        cancelAll()
        val s = settings.settings.first()
        if (!s.notifications.enabled) return
        val now = time.now()
        val days = prayerTimes.getRange(now.toLocalDate(), 2)
        var requestCode = 0

        scheduleDailyRefresh(now)

        days.forEach { day ->
            val isRamadan = HijriCalendar.toHijri(day.date).month == 9
            Prayer.entries.forEach { prayer ->
                val at = day.dateTime(prayer)
                if (prayer in s.notifications.prayers && at.isAfter(now)) {
                    schedule(at, PrayerAlarmReceiver.intent(context, prayer, AlarmKind.PRAYER, 0), requestCode++)
                }
                val pre = s.notifications.preAlertMinutes
                if (pre > 0 && prayer.isFard && prayer in s.notifications.prayers) {
                    val preAt = at.minusMinutes(pre.toLong())
                    if (preAt.isAfter(now)) {
                        schedule(preAt, PrayerAlarmReceiver.intent(context, prayer, AlarmKind.PRE_ALERT, pre), requestCode++)
                    }
                }
            }
            if (isRamadan && s.notifications.ramadanAlerts) {
                val suhoor = day.dateTime(Prayer.FAJR).minusMinutes(SUHOOR_WARNING_MIN)
                if (suhoor.isAfter(now)) {
                    schedule(suhoor, PrayerAlarmReceiver.intent(context, Prayer.FAJR, AlarmKind.SUHOOR, SUHOOR_WARNING_MIN.toInt()), requestCode++)
                }
                val iftar = day.dateTime(Prayer.MAGHRIB)
                if (iftar.isAfter(now)) {
                    schedule(iftar, PrayerAlarmReceiver.intent(context, Prayer.MAGHRIB, AlarmKind.IFTAR, 0), requestCode++)
                }
            }
        }
    }

    /** Har kuni yarim tundan keyin jadvalni yangilash (zanjir uzilib qolmasligi uchun). */
    private fun scheduleDailyRefresh(now: LocalDateTime) {
        val am = alarmManager ?: return
        val at = now.toLocalDate().plusDays(1).atTime(0, 10).atZone(time.zone()).toInstant().toEpochMilli()
        val pi = PendingIntent.getBroadcast(
            context, REFRESH_CODE, Intent(context, RescheduleReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
    }

    override fun cancelAll() {
        for (code in 0 until MAX_ALARMS) {
            val pi = PendingIntent.getBroadcast(
                context, code, Intent(context, PrayerAlarmReceiver::class.java).setAction(PrayerAlarmReceiver.ACTION_ALARM),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            ) ?: continue
            alarmManager?.cancel(pi)
            pi.cancel()
        }
    }

    private fun schedule(at: LocalDateTime, intent: Intent, requestCode: Int) {
        val am = alarmManager ?: return
        if (requestCode >= MAX_ALARMS) return
        val triggerAt = at.atZone(time.zone()).toInstant().toEpochMilli()
        val pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
        if (canExact) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            // Aniq signal ruxsati berilmagan bo'lsa — bir necha daqiqa kechikishi mumkin.
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    private companion object {
        const val MAX_ALARMS = 40
        const val REFRESH_CODE = 9_001
        const val SUHOOR_WARNING_MIN = 30L
    }
}

enum class AlarmKind { PRAYER, PRE_ALERT, SUHOOR, IFTAR }

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsModule {
    @Binds
    abstract fun bindScheduler(impl: PrayerAlarmSchedulerImpl): PrayerAlarmScheduler
}
