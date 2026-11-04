package uz.dostonbek.namozly.core.common

import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/** Test qilinadigan vaqt manbai. */
interface TimeProvider {
    fun now(): LocalDateTime
    fun today(): LocalDate = now().toLocalDate()
    fun zone(): ZoneId
    fun millis(): Long
}

class SystemTimeProvider(private val clock: Clock = Clock.systemDefaultZone()) : TimeProvider {
    override fun now(): LocalDateTime = LocalDateTime.now(clock)
    override fun zone(): ZoneId = clock.zone
    override fun millis(): Long = clock.millis()
}
