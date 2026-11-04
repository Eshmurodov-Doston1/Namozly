package uz.dostonbek.namozly.core.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.dostonbek.namozly.core.model.Prayer
import java.time.LocalDate

/** "Bugungi namozlarim" — o'qilgan farz namozlarni belgilash va ketma-ketlik (streak). */
interface PrayerLogRepository {
    /** Sana -> o'qilgan namozlar. */
    fun observe(): Flow<Map<LocalDate, Set<Prayer>>>
    suspend fun toggle(date: LocalDate, prayer: Prayer)
}

object PrayerStreak {
    val fard = listOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)

    /** Bugundan (yoki bugun to'liq bo'lmasa — kechadan) orqaga barcha 5 farz o'qilgan kunlar soni. */
    fun streak(log: Map<LocalDate, Set<Prayer>>, today: LocalDate): Int {
        fun full(d: LocalDate) = log[d]?.containsAll(fard) == true
        var day = if (full(today)) today else today.minusDays(1)
        var count = 0
        while (full(day)) { count++; day = day.minusDays(1) }
        return count
    }
}
