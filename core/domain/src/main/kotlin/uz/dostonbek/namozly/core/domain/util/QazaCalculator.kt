package uz.dostonbek.namozly.core.domain.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object QazaCalculator {
    /** Balog'atdan namoz boshlagan sanagacha bo'lgan kunlar soni — har bir farz (va vitr) uchun. */
    fun missedDays(from: LocalDate, until: LocalDate): Int =
        ChronoUnit.DAYS.between(from, until).coerceAtLeast(0).toInt()

    fun missedDaysForYears(years: Int, months: Int = 0): Int = years * 354 + months * 30
}
