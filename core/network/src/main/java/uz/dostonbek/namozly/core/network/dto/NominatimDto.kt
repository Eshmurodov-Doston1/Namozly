package uz.dostonbek.namozly.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** OpenStreetMap Nominatim — shahar qidiruvi (foydalanish siyosati: 1 so'rov/soniya, User-Agent majburiy). */
@Serializable
data class NominatimPlaceDto(
    val lat: String,
    val lon: String,
    @SerialName("display_name") val displayName: String,
    val address: NominatimAddressDto? = null,
)

@Serializable
data class NominatimAddressDto(
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val state: String? = null,
    val county: String? = null,
    val country: String? = null,
    @SerialName("country_code") val countryCode: String? = null,
)
