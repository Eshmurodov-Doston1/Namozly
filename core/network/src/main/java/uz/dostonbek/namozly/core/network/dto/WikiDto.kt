package uz.dostonbek.namozly.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class WikiSummaryDto(
    val title: String,
    val extract: String? = null,
    val thumbnail: WikiImageDto? = null,
    val originalimage: WikiImageDto? = null,
)

@Serializable
data class WikiImageDto(val source: String, val width: Int = 0, val height: Int = 0)
