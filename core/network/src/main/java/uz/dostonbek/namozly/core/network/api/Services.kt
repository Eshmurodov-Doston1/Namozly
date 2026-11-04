package uz.dostonbek.namozly.core.network.api

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import uz.dostonbek.namozly.core.network.dto.AlQuranEditionsResponse
import uz.dostonbek.namozly.core.network.dto.AladhanCalendarResponse
import uz.dostonbek.namozly.core.network.dto.NominatimPlaceDto
import uz.dostonbek.namozly.core.network.dto.OverpassResponse
import uz.dostonbek.namozly.core.network.dto.QuranEncResponse

/** https://api.aladhan.com — bepul, kalitsiz. */
interface AladhanService {
    @GET("v1/calendar/{year}/{month}")
    suspend fun calendar(
        @Path("year") year: Int,
        @Path("month") month: Int,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int,
        /** 0 = Shofiiy, 1 = Hanafiy */
        @Query("school") school: Int,
        @Query("iso8601") iso8601: Boolean = false,
    ): AladhanCalendarResponse

    companion object { const val BASE_URL = "https://api.aladhan.com/" }
}

/** https://alquran.cloud/api — arabcha matn + tarjimalar (uz.sodik, ru.kuliev, en.sahih). */
interface AlQuranService {
    @GET("v1/surah/{surah}/editions/{editions}")
    suspend fun surah(
        @Path("surah") surah: Int,
        @Path("editions") editions: String,
    ): AlQuranEditionsResponse

    companion object {
        const val BASE_URL = "https://api.alquran.cloud/"
        const val ARABIC = "quran-uthmani"
        const val UZ_LATN = "uz.sodik"
        const val RU = "ru.kuliev"
        const val EN = "en.sahih"
    }
}

/** https://quranenc.com — Muhammad Sodiq Muhammad Yusuf tarjimasi (kirill). */
interface QuranEncService {
    @GET("api/v1/translation/sura/{key}/{surah}")
    suspend fun surah(
        @Path("key") key: String,
        @Path("surah") surah: Int,
    ): QuranEncResponse

    companion object {
        const val BASE_URL = "https://quranenc.com/"
        const val UZ_CYRL = "uzbek_sadiq"
    }
}

/**
 * OpenStreetMap Overpass — atrofdagi masjidlar. Asosiy server tez-tez band bo'ladi (429/504),
 * shuning uchun bir nechta ochiq nusxa (mirror) navbat bilan sinaladi — [MIRRORS].
 */
interface OverpassService {
    @FormUrlEncoded
    @POST
    suspend fun query(@Url url: String, @Field("data") query: String): OverpassResponse

    companion object {
        const val BASE_URL = "https://overpass-api.de/"

        val MIRRORS = listOf(
            "https://overpass-api.de/api/interpreter",
            "https://overpass.kumi.systems/api/interpreter",
            "https://maps.mail.ru/osm/tools/overpass/api/interpreter",
            "https://overpass.private.coffee/api/interpreter",
        )

        /**
         * Masjidlar turli teglarda belgilanadi: amenity=place_of_worship + religion=muslim,
         * building=mosque, yoki faqat nomida "masjid/mosque/мечеть". nwr — nuqta, chiziq va relation.
         */
        fun mosquesAround(lat: Double, lon: Double, radius: Int, limit: Int = 80): String {
            val a = "(around:$radius,$lat,$lon)"
            return "[out:json][timeout:20];(" +
                "nwr[\"amenity\"=\"place_of_worship\"][\"religion\"=\"muslim\"]$a;" +
                "nwr[\"building\"=\"mosque\"]$a;" +
                "nwr[\"amenity\"=\"place_of_worship\"][\"name\"~\"masjid|mosque|мечеть|масжид|jome|жоме\",i]$a;" +
                ");out center tags $limit;"
        }
    }
}

/** OpenStreetMap Nominatim — shahar qidirish va teskari geokodlash. */
interface NominatimService {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "jsonv2",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("accept-language") language: String = "uz",
    ): List<NominatimPlaceDto>

    companion object { const val BASE_URL = "https://nominatim.openstreetmap.org/" }
}

/** Wikipedia REST — masjid rasmlari (thumbnail) uchun. Litsenziya: CC BY-SA, manba ko'rsatiladi. */
interface WikipediaService {
    @GET("api/rest_v1/page/summary/{title}")
    suspend fun summary(@Path("title", encoded = false) title: String): uz.dostonbek.namozly.core.network.dto.WikiSummaryDto

    companion object {
        fun baseUrl(lang: String) = "https://$lang.wikipedia.org/"
    }
}
