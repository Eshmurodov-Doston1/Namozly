package uz.dostonbek.namozly.core.model

data class GeoPoint(val latitude: Double, val longitude: Double)

data class UserLocation(
    val point: GeoPoint,
    val city: String,
    val region: String? = null,
    val district: String? = null,
    val country: String,
    val countryCode: String,
    val isAutomatic: Boolean = true,
    val mahalla: String? = null,
    val street: String? = null,
    /** GPS aniqligi (metr). */
    val accuracyMeters: Int? = null,
) {
    val displayName: String get() = listOfNotNull(city.takeIf { it.isNotBlank() }, country.takeIf { it.isNotBlank() }).joinToString(", ")

    companion object {
        /** Joylashuv aniqlanmaganda standart: Toshkent. */
        val Default = UserLocation(
            point = GeoPoint(41.2995, 69.2401),
            city = "Toshkent",
            region = "Toshkent shahri",
            country = "O'zbekiston",
            countryCode = "UZ",
            isAutomatic = false,
        )
    }
}
