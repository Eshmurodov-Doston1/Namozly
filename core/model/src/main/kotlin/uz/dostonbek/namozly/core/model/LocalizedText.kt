package uz.dostonbek.namozly.core.model

/** Ilova qo'llab-quvvatlaydigan tillar. [tag] — BCP-47 (AppCompat per-app locale uchun). */
enum class AppLanguage(val tag: String, val nativeName: String) {
    UZ_LATN("uz", "O'zbekcha"),
    UZ_CYRL("uz-Cyrl", "Ўзбекча"),
    RU("ru", "Русский"),
    EN("en", "English");

    companion object {
        fun fromTag(tag: String?): AppLanguage = entries.firstOrNull { it.tag.equals(tag, true) } ?: UZ_LATN
    }
}

/** 4 tildagi matn. Tarjima yo'q bo'lsa lotincha o'zbekchaga qaytadi. */
data class LocalizedText(
    val uz: String,
    val uzCyrl: String = uz,
    val ru: String = uz,
    val en: String = uz,
) {
    operator fun get(language: AppLanguage): String = when (language) {
        AppLanguage.UZ_LATN -> uz
        AppLanguage.UZ_CYRL -> uzCyrl
        AppLanguage.RU -> ru
        AppLanguage.EN -> en
    }
}
