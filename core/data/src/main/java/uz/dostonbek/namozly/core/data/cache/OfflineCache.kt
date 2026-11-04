package uz.dostonbek.namozly.core.data.cache

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import uz.dostonbek.namozly.core.database.dao.JsonCacheDao
import uz.dostonbek.namozly.core.database.entity.JsonCacheEntity
import uz.dostonbek.namozly.core.model.GeoPoint
import uz.dostonbek.namozly.core.model.Mosque
import uz.dostonbek.namozly.core.model.RadioStation
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tarmoq javoblarini Room'da saqlaydi — internet bo'lmaganda oxirgi ma'lumot ko'rsatiladi.
 * Model sinflari serializatsiya plaginisiz, shuning uchun JSON qo'lda quriladi.
 */
@Singleton
class OfflineCache @Inject constructor(private val dao: JsonCacheDao, private val json: Json) {

    suspend fun putMosques(key: String, list: List<Mosque>) = put(key, buildJsonArray {
        list.forEach { m ->
            add(buildJsonObject {
                put("id", m.id); put("name", m.name); put("lat", m.point.latitude); put("lon", m.point.longitude)
                m.distanceMeters?.let { put("d", it) }
                m.address?.let { put("addr", it) }; m.photoUrl?.let { put("photo", it) }
                m.city?.let { put("city", it) }; m.country?.let { put("country", it) }
            })
        }
    })

    suspend fun mosques(key: String): List<Mosque>? = get(key)?.let { el ->
        runCatching {
            el.jsonArray.map { it.jsonObject }.map { o ->
                Mosque(
                    id = o.long("id") ?: 0, name = o.str("name").orEmpty(),
                    point = GeoPoint(o.double("lat") ?: 0.0, o.double("lon") ?: 0.0),
                    distanceMeters = o.double("d"), address = o.str("addr"), photoUrl = o.str("photo"),
                    city = o.str("city"), country = o.str("country"),
                )
            }
        }.getOrNull()
    }

    suspend fun putRadios(list: List<RadioStation>) = put(RADIOS, buildJsonArray {
        list.forEach { r -> add(buildJsonObject { put("id", r.id); put("name", r.name); put("url", r.url) }) }
    })

    suspend fun radios(): List<RadioStation>? = get(RADIOS)?.let { el ->
        runCatching { el.jsonArray.map { it.jsonObject }.map { o -> RadioStation(o.int("id") ?: 0, o.str("name").orEmpty(), o.str("url").orEmpty()) } }.getOrNull()
    }

    suspend fun putStringMap(key: String, map: Map<String, String?>) = put(key, buildJsonObject { map.forEach { (k, v) -> put(k, v) } })

    suspend fun stringMap(key: String): Map<String, String?>? = get(key)?.let { el ->
        runCatching { el.jsonObject.mapValues { it.value.jsonPrimitive.contentOrNull } }.getOrNull()
    }

    private suspend fun put(key: String, value: kotlinx.serialization.json.JsonElement) =
        dao.put(JsonCacheEntity(key, json.encodeToString(kotlinx.serialization.json.JsonElement.serializer(), value), System.currentTimeMillis()))

    private suspend fun get(key: String) = dao.get(key)?.let { runCatching { json.parseToJsonElement(it.json) }.getOrNull() }

    private fun JsonObject.str(k: String) = this[k]?.jsonPrimitive?.contentOrNull
    private fun JsonObject.double(k: String) = this[k]?.jsonPrimitive?.doubleOrNull
    private fun JsonObject.long(k: String) = this[k]?.jsonPrimitive?.longOrNull
    private fun JsonObject.int(k: String) = this[k]?.jsonPrimitive?.intOrNull

    companion object {
        const val RADIOS = "radios"
        const val MOSQUES_LAST = "mosques:last"
        const val WORLD_PHOTOS = "world:photos"
    }
}
