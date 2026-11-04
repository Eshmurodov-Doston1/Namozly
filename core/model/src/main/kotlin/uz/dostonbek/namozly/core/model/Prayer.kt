package uz.dostonbek.namozly.core.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class Prayer(val isFard: Boolean) {
    FAJR(true), SUNRISE(false), DHUHR(true), ASR(true), MAGHRIB(true), ISHA(true);

    companion object {
        val notifiable = listOf(FAJR, SUNRISE, DHUHR, ASR, MAGHRIB, ISHA)
    }
}

/** Aladhan method id'lari bilan mos keladi (https://aladhan.com/calculation-methods). */
enum class CalculationMethod(val aladhanId: Int) {
    MUSLIM_WORLD_LEAGUE(3),
    KARACHI(1),
    ISNA(2),
    UMM_AL_QURA(4),
    EGYPTIAN(5),
    DUBAI(16),
    TURKEY(13),
    RUSSIA(14),
}

enum class Madhab { SHAFI, HANAFI }

data class HijriDate(val day: Int, val month: Int, val year: Int) {
    companion object {
        val monthNames = listOf(
            LocalizedText("Muharram", "Муҳаррам", "Мухаррам", "Muharram"),
            LocalizedText("Safar", "Сафар", "Сафар", "Safar"),
            LocalizedText("Rabiul avval", "Рабиул аввал", "Раби аль-авваль", "Rabi' al-Awwal"),
            LocalizedText("Rabiul oxir", "Рабиул охир", "Раби ас-сани", "Rabi' al-Thani"),
            LocalizedText("Jumodul avval", "Жумодул аввал", "Джумада аль-уля", "Jumada al-Ula"),
            LocalizedText("Jumodul oxir", "Жумодул охир", "Джумада ас-сания", "Jumada al-Akhirah"),
            LocalizedText("Rajab", "Ражаб", "Раджаб", "Rajab"),
            LocalizedText("Sha'bon", "Шаъбон", "Шабан", "Sha'ban"),
            LocalizedText("Ramazon", "Рамазон", "Рамадан", "Ramadan"),
            LocalizedText("Shavvol", "Шаввол", "Шавваль", "Shawwal"),
            LocalizedText("Zulqa'da", "Зулқаъда", "Зуль-када", "Dhu al-Qi'dah"),
            LocalizedText("Zulhijja", "Зулҳижжа", "Зуль-хиджа", "Dhu al-Hijjah"),
        )
    }
    fun monthName(language: AppLanguage): String = monthNames[(month - 1).coerceIn(0, 11)][language]
}

data class DailyPrayerTimes(
    val date: LocalDate,
    val times: Map<Prayer, LocalTime>,
    val hijri: HijriDate? = null,
    val source: Source = Source.OFFLINE,
) {
    enum class Source { API, OFFLINE }

    operator fun get(prayer: Prayer): LocalTime = times.getValue(prayer)

    fun dateTime(prayer: Prayer): LocalDateTime = LocalDateTime.of(date, get(prayer))
}

/** Hozirgi va keyingi namoz — bosh sahifa uchun. */
data class PrayerMoment(
    val current: Prayer?,
    val next: Prayer,
    val nextAt: LocalDateTime,
)
