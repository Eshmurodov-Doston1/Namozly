package uz.dostonbek.namozly.core.domain.util

import java.time.LocalDate

/** "Kun oyati": har kuni tanlangan qisqa oyatlardan biri (sana bo'yicha deterministik). */
object AyahOfDayPicker {
    private val picks = listOf(
        2 to 152, 2 to 186, 2 to 286, 3 to 139, 13 to 28, 94 to 5, 94 to 6, 65 to 3, 39 to 53,
        2 to 153, 3 to 8, 20 to 114, 40 to 60, 29 to 69, 33 to 41, 2 to 45, 7 to 56, 16 to 128,
        49 to 13, 55 to 13, 93 to 5, 17 to 80, 14 to 7, 8 to 46, 3 to 200, 11 to 115, 12 to 87,
        21 to 87, 25 to 74, 112 to 1,
    )

    fun pick(date: LocalDate): Pair<Int, Int> = picks[(date.toEpochDay() % picks.size).toInt()]
}
