package uz.dostonbek.namozly.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class OverpassResponse(val elements: List<OverpassElementDto> = emptyList())

@Serializable
data class OverpassElementDto(
    val type: String,
    val id: Long,
    val lat: Double? = null,
    val lon: Double? = null,
    val center: OverpassCenterDto? = null,
    val tags: Map<String, String> = emptyMap(),
)

@Serializable
data class OverpassCenterDto(val lat: Double, val lon: Double)
