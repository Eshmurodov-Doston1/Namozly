package uz.dostonbek.namozly.core.domain.util

import uz.dostonbek.namozly.core.model.HijriDate
import java.time.LocalDate
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

/**
 * Umm al-Qura (java.time HijrahChronology) asosida. [dayOffset] — mahalliy oy ko'rinishiga
 * moslash uchun (O'zbekistonda ko'pincha -1 yoki 0).
 */
object HijriCalendar {
    fun toHijri(date: LocalDate, dayOffset: Int = 0): HijriDate {
        val h = HijrahDate.from(date.plusDays(dayOffset.toLong()))
        return HijriDate(
            day = h.get(ChronoField.DAY_OF_MONTH),
            month = h.get(ChronoField.MONTH_OF_YEAR),
            year = h.get(ChronoField.YEAR),
        )
    }

    fun toGregorian(hijri: HijriDate, dayOffset: Int = 0): LocalDate? = runCatching {
        LocalDate.from(HijrahChronology.INSTANCE.date(hijri.year, hijri.month, hijri.day))
            .minusDays(dayOffset.toLong())
    }.getOrNull()

    fun monthLength(year: Int, month: Int): Int = runCatching {
        HijrahChronology.INSTANCE.date(year, month, 1).lengthOfMonth()
    }.getOrDefault(30)
}
