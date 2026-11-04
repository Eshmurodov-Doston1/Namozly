package uz.dostonbek.namozly.core.network.dto

import kotlinx.serialization.Serializable

/** api.alquran.cloud — /v1/surah/{n}/editions/{a},{b} */
@Serializable
data class AlQuranEditionsResponse(
    val code: Int,
    val data: List<AlQuranSurahEditionDto> = emptyList(),
)

@Serializable
data class AlQuranSurahEditionDto(
    val number: Int,
    val ayahs: List<AlQuranAyahDto>,
    val edition: AlQuranEditionDto,
)

@Serializable
data class AlQuranAyahDto(
    val number: Int,
    val text: String,
    val numberInSurah: Int,
)

@Serializable
data class AlQuranEditionDto(val identifier: String)

/** quranenc.com — /api/v1/translation/sura/{key}/{n} */
@Serializable
data class QuranEncResponse(val result: List<QuranEncAyahDto> = emptyList())

@Serializable
data class QuranEncAyahDto(
    val sura: String,
    val aya: String,
    val translation: String,
    val footnotes: String? = null,
)
