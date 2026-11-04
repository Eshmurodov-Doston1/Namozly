package uz.dostonbek.namozly.core.domain.util

import uz.dostonbek.namozly.core.model.DailyPrayerTimes
import uz.dostonbek.namozly.core.model.Prayer
import uz.dostonbek.namozly.core.model.PrayerMoment
import java.time.LocalDateTime

object PrayerMomentCalculator {
    /**
     * Hozirgi va keyingi namozni topadi.
     * - Bomdoddan oldin: joriy = Xufton (kechagi), keyingi = Bomdod.
     * - Quyosh chiqqandan Peshingacha: joriy = yo'q.
     * - Xuftondan keyin: keyingi = ertangi Bomdod.
     */
    fun compute(now: LocalDateTime, today: DailyPrayerTimes, tomorrow: DailyPrayerTimes): PrayerMoment {
        val passed = Prayer.entries.lastOrNull { !today.dateTime(it).isAfter(now) }
        val next = Prayer.entries.firstOrNull { today.dateTime(it).isAfter(now) }
        val current = when (passed) {
            null -> Prayer.ISHA
            Prayer.SUNRISE -> null
            else -> passed
        }
        return if (next != null) {
            PrayerMoment(current = current, next = next, nextAt = today.dateTime(next))
        } else {
            PrayerMoment(current = current, next = Prayer.FAJR, nextAt = tomorrow.dateTime(Prayer.FAJR))
        }
    }
}
