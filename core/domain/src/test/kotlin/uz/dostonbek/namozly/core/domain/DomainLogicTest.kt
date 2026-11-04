package uz.dostonbek.namozly.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import uz.dostonbek.namozly.core.domain.util.PrayerMomentCalculator
import uz.dostonbek.namozly.core.domain.util.QiblaCalculator
import uz.dostonbek.namozly.core.model.DailyPrayerTimes
import uz.dostonbek.namozly.core.model.GeoPoint
import uz.dostonbek.namozly.core.model.Prayer
import uz.dostonbek.namozly.core.model.UzTransliterator
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class DomainLogicTest {

    @Test
    fun `Toshkentdan qibla burchagi taxminan 240 gradus`() {
        val bearing = QiblaCalculator.bearing(GeoPoint(41.2995, 69.2401))
        assertTrue("bearing=$bearing", bearing in 238.0..243.0)
    }

    @Test
    fun `Toshkent - Makka masofasi taxminan 3530 km`() {
        val km = QiblaCalculator.distanceKm(GeoPoint(41.2995, 69.2401))
        assertTrue("km=$km", km in 3450.0..3600.0)
    }

    private val date = LocalDate.of(2026, 11, 4)
    private fun day(d: LocalDate) = DailyPrayerTimes(
        date = d,
        times = mapOf(
            Prayer.FAJR to LocalTime.of(5, 50),
            Prayer.SUNRISE to LocalTime.of(7, 15),
            Prayer.DHUHR to LocalTime.of(12, 10),
            Prayer.ASR to LocalTime.of(15, 5),
            Prayer.MAGHRIB to LocalTime.of(17, 0),
            Prayer.ISHA to LocalTime.of(18, 20),
        ),
    )

    @Test
    fun `bomdoddan oldin joriy namoz xufton`() {
        val m = PrayerMomentCalculator.compute(LocalDateTime.of(date, LocalTime.of(4, 0)), day(date), day(date.plusDays(1)))
        assertEquals(Prayer.ISHA, m.current)
        assertEquals(Prayer.FAJR, m.next)
    }

    @Test
    fun `quyosh chiqqandan keyin joriy namoz yo'q`() {
        val m = PrayerMomentCalculator.compute(LocalDateTime.of(date, LocalTime.of(9, 0)), day(date), day(date.plusDays(1)))
        assertNull(m.current)
        assertEquals(Prayer.DHUHR, m.next)
    }

    @Test
    fun `xuftondan keyin keyingisi ertangi bomdod`() {
        val m = PrayerMomentCalculator.compute(LocalDateTime.of(date, LocalTime.of(22, 0)), day(date), day(date.plusDays(1)))
        assertEquals(Prayer.ISHA, m.current)
        assertEquals(Prayer.FAJR, m.next)
        assertEquals(date.plusDays(1), m.nextAt.toLocalDate())
    }

    @Test
    fun `lotin-kirill transliteratsiya`() {
        assertEquals("Ўзбекистон", UzTransliterator.toCyrillic("O'zbekiston"))
        assertEquals("Шаҳар", UzTransliterator.toCyrillic("Shahar"))
        assertEquals("Ғалаба", UzTransliterator.toCyrillic("G'alaba"))
        assertEquals("Эрта", UzTransliterator.toCyrillic("Erta"))
    }
}
