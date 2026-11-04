package uz.dostonbek.namozly.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AladhanCalendarResponse(
    val code: Int,
    val data: List<AladhanDayDto> = emptyList(),
)

@Serializable
data class AladhanDayDto(
    val timings: AladhanTimingsDto,
    val date: AladhanDateDto,
)

@Serializable
data class AladhanTimingsDto(
    @SerialName("Fajr") val fajr: String,
    @SerialName("Sunrise") val sunrise: String,
    @SerialName("Dhuhr") val dhuhr: String,
    @SerialName("Asr") val asr: String,
    @SerialName("Maghrib") val maghrib: String,
    @SerialName("Isha") val isha: String,
)

@Serializable
data class AladhanDateDto(
    val gregorian: AladhanGregorianDto,
    val hijri: AladhanHijriDto,
)

@Serializable
data class AladhanGregorianDto(
    /** "DD-MM-YYYY" */
    val date: String,
)

@Serializable
data class AladhanHijriDto(
    val day: String,
    val month: AladhanMonthDto,
    val year: String,
)

@Serializable
data class AladhanMonthDto(val number: Int)
