package uz.dostonbek.namozly.core.network.api

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

/** mp3quran.net — Qur'on radiolari ro'yxati (bepul, kalitsiz). */
interface RadioService {
    @GET("api/v3/radios")
    suspend fun radios(@Query("language") language: String = "eng"): RadiosResponse

    companion object { const val BASE_URL = "https://mp3quran.net/" }
}

@Serializable
data class RadiosResponse(val radios: List<RadioDto> = emptyList())

@Serializable
data class RadioDto(val id: Int = 0, val name: String = "", val url: String = "")
