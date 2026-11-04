package uz.dostonbek.namozly.core.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import uz.dostonbek.namozly.core.common.Dispatcher
import uz.dostonbek.namozly.core.common.NamozlyDispatchers
import uz.dostonbek.namozly.core.common.runSuspendCatching
import uz.dostonbek.namozly.core.data.cache.OfflineCache
import uz.dostonbek.namozly.core.data.source.WorldMosquesData
import uz.dostonbek.namozly.core.domain.repository.MosqueRepository
import uz.dostonbek.namozly.core.domain.util.QiblaCalculator
import uz.dostonbek.namozly.core.model.GeoPoint
import uz.dostonbek.namozly.core.model.Mosque
import uz.dostonbek.namozly.core.network.api.OverpassService
import uz.dostonbek.namozly.core.network.api.WikipediaService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Masjidlar: OpenStreetMap (Overpass) — bepul, kalitsiz. Rasm: OSM "image"/"wikipedia" teglari
 * yoki Wikipedia REST thumbnail. Rasm topilmasa UI o'zining illyustratsiyasini chizadi.
 */
@Singleton
class MosqueRepositoryImpl @Inject constructor(
    private val overpass: OverpassService,
    private val offline: OfflineCache,
    private val wikipedia: WikipediaService,
    @Dispatcher(NamozlyDispatchers.IO) private val io: CoroutineDispatcher,
) : MosqueRepository {

    private val photoCache = mutableMapOf<String, String?>()
    private val cacheMutex = Mutex()
    private var worldCache: List<Mosque>? = null

    override suspend fun nearby(point: GeoPoint, radiusMeters: Int): Result<List<Mosque>> = withContext(io) {
        // Atrofda masjid topilmasa — radiusni kengaytiramiz (shahar chekkasi, qishloqlar).
        var result: Result<List<Mosque>> = Result.success(emptyList())
        for (r in listOf(radiusMeters, radiusMeters * 3, radiusMeters * 8)) {
            result = fetchNearby(point, r)
            if (result.isFailure || result.getOrNull().orEmpty().isNotEmpty()) break
        }
        result.onSuccess { list -> if (list.isNotEmpty()) runSuspendCatching { offline.putMosques(OfflineCache.MOSQUES_LAST, list) } }
        if (result.isSuccess) return@withContext result
        // Internet yo'q — oxirgi saqlangan ro'yxat (masofalar joriy joydan qayta hisoblanadi).
        val cached = offline.mosques(OfflineCache.MOSQUES_LAST)
            ?.map { it.copy(distanceMeters = QiblaCalculator.distanceKm(point, it.point) * 1000) }
            ?.sortedBy { it.distanceMeters }
        if (!cached.isNullOrEmpty()) Result.success(cached) else result
    }

    private suspend fun fetchNearby(point: GeoPoint, radiusMeters: Int): Result<List<Mosque>> =
        runSuspendCatching {
            val response = queryMirrors(OverpassService.mosquesAround(point.latitude, point.longitude, radiusMeters))
            val mosques = response.elements.mapNotNull { e ->
                val lat = e.lat ?: e.center?.lat ?: return@mapNotNull null
                val lon = e.lon ?: e.center?.lon ?: return@mapNotNull null
                val p = GeoPoint(lat, lon)
                val tags = e.tags
                val name = tags["name:uz"] ?: tags["name"] ?: tags["name:en"] ?: tags["name:ru"]
                Pair(
                    Mosque(
                        id = e.id,
                        name = name ?: "Masjid",
                        point = p,
                        distanceMeters = QiblaCalculator.distanceKm(point, p) * 1000,
                        address = listOfNotNull(tags["addr:street"], tags["addr:housenumber"]).joinToString(" ").ifBlank { null },
                        photoUrl = tags["image"]?.takeIf { it.startsWith("https://") },
                        city = tags["addr:city"],
                    ),
                    tags["wikipedia"],
                )
            }.distinctBy { it.first.id }.sortedBy { it.first.distanceMeters }

            // Eng yaqin 12 ta masjid uchun Wikipedia rasmini qidiramiz.
            coroutineScope {
                mosques.mapIndexed { index, (mosque, wiki) ->
                    async {
                        if (mosque.photoUrl == null && wiki != null && index < 12 && wiki.startsWith("en:")) {
                            mosque.copy(photoUrl = photo(wiki.removePrefix("en:")))
                        } else mosque
                    }
                }.awaitAll()
            }
        }

    /** Overpass nusxalarini navbat bilan sinaydi; birinchi muvaffaqiyatli javob qaytariladi. */
    private suspend fun queryMirrors(query: String): uz.dostonbek.namozly.core.network.dto.OverpassResponse {
        var last: Throwable? = null
        for (url in OverpassService.MIRRORS) {
            val r = runSuspendCatching { overpass.query(url, query) }
            r.getOrNull()?.let { return it }
            last = r.exceptionOrNull()
        }
        throw last ?: IllegalStateException("overpass")
    }

    override suspend fun worldFamous(): List<Mosque> = withContext(io) {
        worldCache?.let { return@withContext it }
        if (photoCache.isEmpty()) offline.stringMap(OfflineCache.WORLD_PHOTOS)?.let { saved -> cacheMutex.withLock { photoCache.putAll(saved.filterValues { it != null }) } }
        val list = coroutineScope {
            WorldMosquesData.all.map { item ->
                async { WorldMosquesData.toMosque(item, photo(item.wikiTitle)) }
            }.awaitAll()
        }
        if (list.any { it.photoUrl != null }) {
            worldCache = list
            runSuspendCatching { offline.putStringMap(OfflineCache.WORLD_PHOTOS, cacheMutex.withLock { photoCache.toMap() }) }
        }
        list
    }

    private suspend fun photo(title: String): String? {
        cacheMutex.withLock { if (photoCache.containsKey(title)) return photoCache[title] }
        val url = runSuspendCatching { wikipedia.summary(title.replace(' ', '_')) }
            .getOrNull()
            ?.let { it.thumbnail?.source ?: it.originalimage?.source }
        cacheMutex.withLock { photoCache[title] = url }
        return url
    }
}
