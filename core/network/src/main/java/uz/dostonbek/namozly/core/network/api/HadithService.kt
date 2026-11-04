package uz.dostonbek.namozly.core.network.api

import retrofit2.http.GET
import retrofit2.http.Path
import uz.dostonbek.namozly.core.network.dto.HadithResponse

interface HadithService {
    /** Masalan: editions/ara-bukhari/1.min.json */
    @GET("gh/fawazahmed0/hadith-api@1/editions/{edition}/{number}.min.json")
    suspend fun hadith(@Path("edition") edition: String, @Path("number") number: Int): HadithResponse

    /** Butun kitob bitta faylda (oflayn yuklab olish uchun): editions/eng-bukhari.min.json */
    @GET("gh/fawazahmed0/hadith-api@1/editions/{edition}.min.json")
    suspend fun edition(@Path("edition") edition: String): HadithResponse

    companion object { const val BASE_URL = "https://cdn.jsdelivr.net/" }
}
