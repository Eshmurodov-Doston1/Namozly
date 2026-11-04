package uz.dostonbek.namozly.core.network.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Mashina tarjimasi (kalitsiz ochiq endpoint). Javob — ichma-ich JSON massiv:
 * [[["tarjima","asl",...], ...], null, "en", ...]. Matn bo'laklab yuboriladi (URL uzunligi chegarasi).
 */
interface TranslateService {
    @GET("translate_a/single")
    suspend fun translate(
        @Query("sl") from: String,
        @Query("tl") to: String,
        @Query("q") text: String,
        @Query("client") client: String = "gtx",
        @Query("dt") dt: String = "t",
        @Query("ie") ie: String = "UTF-8",
        @Query("oe") oe: String = "UTF-8",
    ): ResponseBody

    companion object {
        const val BASE_URL = "https://translate.googleapis.com/"
    }
}

/** Zaxira tarjima xizmati (MyMemory, 500 belgigacha bo'laklar). */
interface MyMemoryService {
    @GET("get")
    suspend fun translate(@Query("q") text: String, @Query("langpair") pair: String): ResponseBody

    companion object {
        const val BASE_URL = "https://api.mymemory.translated.net/"
    }
}
