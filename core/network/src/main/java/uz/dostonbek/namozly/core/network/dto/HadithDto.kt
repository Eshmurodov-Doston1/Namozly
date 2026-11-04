package uz.dostonbek.namozly.core.network.dto

import kotlinx.serialization.Serializable

/** fawazahmed0/hadith-api (jsDelivr CDN) — bepul, kalitsiz. */
@Serializable
data class HadithResponse(
    val metadata: HadithMetaDto? = null,
    val hadiths: List<HadithDto> = emptyList(),
)

@Serializable
data class HadithMetaDto(
    val name: String = "",
    val section: Map<String, String> = emptyMap(),
    /** Butun nashr faylida bo'limlar shu nom bilan keladi. */
    val sections: Map<String, String> = emptyMap(),
)

@Serializable
data class HadithDto(
    val hadithnumber: Double = 0.0,
    val text: String = "",
    val reference: HadithRefDto? = null,
)

@Serializable
data class HadithRefDto(val book: Int = 0, val hadith: Int = 0)
