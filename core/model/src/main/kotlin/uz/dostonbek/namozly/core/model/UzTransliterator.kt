package uz.dostonbek.namozly.core.model

/**
 * O'zbek lotin -> kirill transliteratsiyasi (rasmiy 1995 alifbosi qoidalari asosida).
 * Kirillcha matni alohida berilmagan kontent uchun ishlatiladi.
 */
object UzTransliterator {
    private val apostrophes = charArrayOf('\'', '’', 'ʻ', 'ʼ', '`', '‘')

    private val digraphs = linkedMapOf(
        "sh" to "ш", "ch" to "ч", "yo" to "ё", "yu" to "ю", "ya" to "я", "ye" to "е",
    )
    private val singles = mapOf(
        'a' to "а", 'b' to "б", 'd' to "д", 'e' to "е", 'f' to "ф", 'g' to "г", 'h' to "ҳ",
        'i' to "и", 'j' to "ж", 'k' to "к", 'l' to "л", 'm' to "м", 'n' to "н", 'o' to "о",
        'p' to "п", 'q' to "қ", 'r' to "р", 's' to "с", 't' to "т", 'u' to "у", 'v' to "в",
        'x' to "х", 'y' to "й", 'z' to "з", 'c' to "с", 'w' to "в",
    )

    fun toCyrillic(latin: String): String {
        val out = StringBuilder(latin.length)
        var i = 0
        val s = latin
        while (i < s.length) {
            val c = s[i]
            val lower = c.lowercaseChar()
            val next = s.getOrNull(i + 1)
            val upper = c.isUpperCase()
            val wordStart = i == 0 || !s[i - 1].isLetter() && s[i - 1] !in apostrophes

            // o' / g'
            if ((lower == 'o' || lower == 'g') && next != null && next in apostrophes) {
                out.append((if (lower == 'o') "ў" else "ғ").cased(upper))
                i += 2; continue
            }
            // tutuq belgisi -> ъ
            if (c in apostrophes) {
                out.append(if (i > 0 && s[i - 1].isLetter()) "ъ" else c.toString())
                i++; continue
            }
            // "yo'" = й + ў (masalan, Yo'l -> Йўл), "yo" digrafi emas
            if (lower == 'y' && next?.lowercaseChar() == 'o' && s.getOrNull(i + 2)?.let { it in apostrophes } == true) {
                out.append("й".cased(upper))
                i++; continue
            }
            if (next != null) {
                val pair = "$lower${next.lowercaseChar()}"
                val d = digraphs[pair]
                if (d != null && !(pair == "ye" && !wordStart)) {
                    val allUpper = upper && next.isUpperCase()
                    out.append(if (allUpper) d.uppercase() else d.cased(upper))
                    i += 2; continue
                }
            }
            if (lower == 'e' && wordStart) {
                out.append("э".cased(upper)); i++; continue
            }
            val mapped = singles[lower]
            out.append(mapped?.cased(upper) ?: c.toString())
            i++
        }
        return out.toString()
    }

    private fun String.cased(upper: Boolean) = if (upper) replaceFirstChar { it.uppercaseChar() } else this

    private val cyrToLat = mapOf(
        'а' to "a", 'б' to "b", 'в' to "v", 'г' to "g", 'ғ' to "g'", 'д' to "d", 'ё' to "yo", 'ж' to "j", 'з' to "z",
        'и' to "i", 'й' to "y", 'к' to "k", 'қ' to "q", 'л' to "l", 'м' to "m", 'н' to "n", 'о' to "o", 'п' to "p",
        'р' to "r", 'с' to "s", 'т' to "t", 'у' to "u", 'ў' to "o'", 'ф' to "f", 'х' to "x", 'ҳ' to "h", 'ч' to "ch",
        'ш' to "sh", 'щ' to "sh", 'ъ' to "'", 'ь' to "", 'э' to "e", 'ю' to "yu", 'я' to "ya", 'ы' to "i",
    )
    private const val CYR_VOWELS = "аеёиоуўэюяъь"

    /** O'zbek kirill -> lotin (rasmiy 1995 alifbosi). */
    fun toLatin(cyrillic: String): String {
        val s = cyrillic
        val out = StringBuilder(s.length + 16)
        for (i in s.indices) {
            val c = s[i]
            val lower = c.lowercaseChar()
            val upper = c.isUpperCase()
            val prev = s.getOrNull(i - 1)?.lowercaseChar()
            val nextUpper = s.getOrNull(i + 1)?.let { it.isLetter() && it.isUpperCase() } == true
            val wordStart = prev == null || !prev.isLetter()
            val mapped: String? = when (lower) {
                'е' -> if (wordStart || (prev != null && prev in CYR_VOWELS)) "ye" else "e"
                'ц' -> if (wordStart) "s" else "ts"
                else -> cyrToLat[lower]
            }
            if (mapped == null) { out.append(c); continue }
            out.append(
                when {
                    !upper || mapped.isEmpty() -> mapped
                    nextUpper || (mapped.length > 1 && mapped[1] != '\'' && s.getOrNull(i + 1)?.isLetter() != true && prev?.isUpperCase() == true) -> mapped.uppercase()
                    else -> mapped.replaceFirstChar { it.uppercaseChar() }
                },
            )
        }
        return out.toString()
    }

    /** Matnning asosiy yozuvi kirillmi? */
    fun isMostlyCyrillic(text: String): Boolean {
        var cyr = 0; var lat = 0
        for (ch in text) {
            if (ch in '\u0400'..'\u04FF') cyr++ else if (ch in 'a'..'z' || ch in 'A'..'Z') lat++
        }
        return cyr > lat
    }

    /** O'zbekcha matnni kerakli yozuvga keltiradi (manba qaysi yozuvda bo'lishidan qat'i nazar). */
    fun toScript(text: String, cyrillic: Boolean): String = when {
        cyrillic && !isMostlyCyrillic(text) -> toCyrillic(text)
        !cyrillic && isMostlyCyrillic(text) -> toLatin(text)
        else -> text
    }
}

/** Kirillchasi avtomatik hosil qilinadigan LocalizedText. */
fun uzText(uz: String, ru: String = uz, en: String = uz): LocalizedText =
    LocalizedText(uz = uz, uzCyrl = UzTransliterator.toCyrillic(uz), ru = ru, en = en)
