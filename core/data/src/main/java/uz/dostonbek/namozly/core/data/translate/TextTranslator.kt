package uz.dostonbek.namozly.core.data.translate

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import uz.dostonbek.namozly.core.common.runSuspendCatching
import uz.dostonbek.namozly.core.network.api.MyMemoryService
import uz.dostonbek.namozly.core.network.api.TranslateService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ochiq mashina tarjimasi. Asosiy — Google (gtx), zaxira — MyMemory.
 * Natija chaqiruvchi tomonda Room'ga saqlanadi, shuning uchun har bir matn faqat bir marta tarjima qilinadi.
 */
@Singleton
class TextTranslator @Inject constructor(
    private val google: TranslateService,
    private val memory: MyMemoryService,
    private val json: Json,
) {
    /** [from]/[to] — ISO kodlar: "en", "ru", "uz". */
    suspend fun translate(text: String, from: String, to: String): String {
        if (text.isBlank() || from == to) return text
        // joinToString lambdasi suspend emas — shuning uchun oddiy sikl.
        val out = ArrayList<String>()
        for (piece in chunks(text, GOOGLE_CHUNK)) {
            val google = runSuspendCatching { viaGoogle(piece, from, to) }.getOrNull()?.takeIf { it.isNotBlank() }
            if (google != null) { out += google; continue }
            for (small in chunks(piece, MEMORY_CHUNK)) out += viaMemory(small, from, to)
        }
        return out.joinToString(" ")
    }

    private suspend fun viaGoogle(text: String, from: String, to: String): String {
        val root = json.parseToJsonElement(google.translate(from, to, text).string()).jsonArray
        return root[0].jsonArray.joinToString("") { seg -> seg.jsonArray.getOrNull(0)?.jsonPrimitive?.contentOrNull.orEmpty() }
    }

    private suspend fun viaMemory(text: String, from: String, to: String): String {
        val obj = json.parseToJsonElement(memory.translate(text, "$from|$to").string()).jsonObject
        return obj["responseData"]?.jsonObject?.get("translatedText")?.jsonPrimitive?.contentOrNull
            ?: error("translation failed")
    }

    /** Gap chegaralari bo'yicha bo'laklash (har biri [max] belgidan oshmaydi). */
    private fun chunks(text: String, max: Int): List<String> {
        if (text.length <= max) return listOf(text)
        val out = mutableListOf<String>()
        val sb = StringBuilder()
        val sentences = text.split(Regex("(?<=[.!?;:\\n])\\s+"))
        for (s in sentences) {
            if (s.length > max) {
                s.split(' ').forEach { w ->
                    if (sb.length + w.length + 1 > max && sb.isNotEmpty()) { out += sb.toString(); sb.clear() }
                    if (sb.isNotEmpty()) sb.append(' ')
                    sb.append(w)
                }
                continue
            }
            if (sb.length + s.length + 1 > max && sb.isNotEmpty()) { out += sb.toString(); sb.clear() }
            if (sb.isNotEmpty()) sb.append(' ')
            sb.append(s)
        }
        if (sb.isNotEmpty()) out += sb.toString()
        return out
    }

    private companion object {
        const val GOOGLE_CHUNK = 1500
        const val MEMORY_CHUNK = 450
    }
}
