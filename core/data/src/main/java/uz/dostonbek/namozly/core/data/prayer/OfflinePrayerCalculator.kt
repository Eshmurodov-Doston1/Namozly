package uz.dostonbek.namozly.core.data.prayer

import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.HighLatitudeRule
import com.batoulapps.adhan.data.DateComponents
import uz.dostonbek.namozly.core.domain.util.HijriCalendar
import uz.dostonbek.namozly.core.model.CalculationMethod
import uz.dostonbek.namozly.core.model.DailyPrayerTimes
import uz.dostonbek.namozly.core.model.GeoPoint
import uz.dostonbek.namozly.core.model.Madhab
import uz.dostonbek.namozly.core.model.Prayer
import uz.dostonbek.namozly.core.model.PrayerSettings
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import com.batoulapps.adhan.CalculationMethod as AdhanMethod
import com.batoulapps.adhan.Madhab as AdhanMadhab
import com.batoulapps.adhan.PrayerTimes as AdhanPrayerTimes

/** Internet bo'lmasa ham ishlaydigan hisoblash (adhan-java). */
@Singleton
class OfflinePrayerCalculator @Inject constructor() {

    fun calculate(
        date: LocalDate,
        point: GeoPoint,
        settings: PrayerSettings,
        zone: ZoneId = ZoneId.systemDefault(),
    ): DailyPrayerTimes {
        val params = parameters(settings.method).apply {
            madhab = if (settings.madhab == Madhab.HANAFI) AdhanMadhab.HANAFI else AdhanMadhab.SHAFI
            // Shimoliy kengliklarda (Rossiya) yozda bomdod/xufton yo'qolib qolmasligi uchun.
            highLatitudeRule = HighLatitudeRule.SEVENTH_OF_THE_NIGHT
        }
        val pt = AdhanPrayerTimes(
            Coordinates(point.latitude, point.longitude),
            DateComponents(date.year, date.monthValue, date.dayOfMonth),
            params,
        )
        val times = mapOf(
            Prayer.FAJR to pt.fajr.toLocal(zone),
            Prayer.SUNRISE to pt.sunrise.toLocal(zone),
            Prayer.DHUHR to pt.dhuhr.toLocal(zone),
            Prayer.ASR to pt.asr.toLocal(zone),
            Prayer.MAGHRIB to pt.maghrib.toLocal(zone),
            Prayer.ISHA to pt.isha.toLocal(zone),
        )
        return DailyPrayerTimes(
            date = date,
            times = times,
            hijri = HijriCalendar.toHijri(date),
            source = DailyPrayerTimes.Source.OFFLINE,
        )
    }

    private fun parameters(method: CalculationMethod): CalculationParameters = when (method) {
        CalculationMethod.MUSLIM_WORLD_LEAGUE -> AdhanMethod.MUSLIM_WORLD_LEAGUE.parameters
        CalculationMethod.KARACHI -> AdhanMethod.KARACHI.parameters
        CalculationMethod.ISNA -> AdhanMethod.NORTH_AMERICA.parameters
        CalculationMethod.UMM_AL_QURA -> AdhanMethod.UMM_AL_QURA.parameters
        CalculationMethod.EGYPTIAN -> AdhanMethod.EGYPTIAN.parameters
        CalculationMethod.DUBAI -> AdhanMethod.DUBAI.parameters
        // adhan-java 1.2.1'da yo'q — burchaklar Aladhan bilan bir xil.
        CalculationMethod.TURKEY -> CalculationParameters(18.0, 17.0)
        CalculationMethod.RUSSIA -> CalculationParameters(16.0, 15.0)
    }

    private fun Date.toLocal(zone: ZoneId): LocalTime = toInstant().atZone(zone).toLocalTime().withSecond(0).withNano(0)
}

/** Foydalanuvchi tuzatishlarini (daqiqa) qo'llaydi. */
internal fun DailyPrayerTimes.adjusted(adjustments: Map<Prayer, Int>): DailyPrayerTimes =
    if (adjustments.isEmpty()) this
    else copy(times = times.mapValues { (p, t) -> t.plusMinutes((adjustments[p] ?: 0).toLong()) })
