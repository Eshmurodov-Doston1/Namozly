package uz.dostonbek.namozly.core.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import uz.dostonbek.namozly.core.common.Dispatcher
import uz.dostonbek.namozly.core.common.NamozlyDispatchers
import uz.dostonbek.namozly.core.common.runSuspendCatching
import uz.dostonbek.namozly.core.data.source.QuranSurahData
import uz.dostonbek.namozly.core.database.dao.AyahDao
import uz.dostonbek.namozly.core.database.entity.AyahEntity
import uz.dostonbek.namozly.core.datastore.NamozlyPreferencesDataSource
import uz.dostonbek.namozly.core.domain.repository.QuranRepository
import uz.dostonbek.namozly.core.model.AppLanguage
import uz.dostonbek.namozly.core.model.Ayah
import uz.dostonbek.namozly.core.model.LocalizedText
import uz.dostonbek.namozly.core.model.Revelation
import uz.dostonbek.namozly.core.model.Surah
import uz.dostonbek.namozly.core.model.UzTransliterator
import uz.dostonbek.namozly.core.network.api.AlQuranService
import uz.dostonbek.namozly.core.network.api.QuranEncService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Matn manbalari:
 * - Arabcha: alquran.cloud "quran-uthmani"
 * - O'zbekcha (lotin): alquran.cloud "uz.sodik" (Muhammad Sodiq Muhammad Yusuf)
 * - O'zbekcha (kirill): quranenc.com "uzbek_sadiq"
 * - Ruscha: "ru.kuliev", Inglizcha: "en.sahih"
 * Bir marta yuklanadi va Room'da saqlanadi (oflayn o'qish).
 */
@Singleton
class QuranRepositoryImpl @Inject constructor(
    private val alQuran: AlQuranService,
    private val quranEnc: QuranEncService,
    private val dao: AyahDao,
    private val preferences: NamozlyPreferencesDataSource,
    @Dispatcher(NamozlyDispatchers.IO) private val io: CoroutineDispatcher,
) : QuranRepository {

    private val surahs: List<Surah> by lazy {
        QuranSurahData.allSurahs.map {
            Surah(
                number = it.number,
                arabicName = it.arabicName,
                name = LocalizedText(it.nameUz, it.nameKril, it.nameRu, it.nameEn),
                ayahCount = it.versesCount,
                revelation = if (it.revelationTypeEn.startsWith("Mecc")) Revelation.MECCAN else Revelation.MEDINAN,
            )
        }
    }

    override fun surahs(): List<Surah> = surahs

    override fun observeAyahs(surah: Int, language: AppLanguage): Flow<List<Ayah>> =
        dao.observeSurah(surah, language.tag).map { list -> list.map { it.toModel(language) } }

    override suspend fun sync(surah: Int, language: AppLanguage): Result<Unit> = withContext(io) {
        val expected = surahs.getOrNull(surah - 1)?.ayahCount ?: return@withContext Result.failure(IllegalArgumentException("surah $surah"))
        if (dao.count(surah, language.tag) >= expected) return@withContext Result.success(Unit)
        runSuspendCatching {
            val entities = if (language == AppLanguage.UZ_CYRL) fetchCyrillic(surah) else fetchAlQuran(surah, language)
            dao.upsert(entities)
        }
    }

    private suspend fun fetchAlQuran(surah: Int, language: AppLanguage): List<AyahEntity> {
        val edition = when (language) {
            AppLanguage.RU -> AlQuranService.RU
            AppLanguage.EN -> AlQuranService.EN
            else -> AlQuranService.UZ_LATN
        }
        val response = alQuran.surah(surah, "${AlQuranService.ARABIC},$edition")
        val arabic = response.data.first { it.edition.identifier == AlQuranService.ARABIC }.ayahs
        val translation = response.data.first { it.edition.identifier == edition }.ayahs.associateBy { it.numberInSurah }
        return arabic.map { a ->
            AyahEntity(
                surah = surah,
                number = a.numberInSurah,
                language = language.tag,
                globalNumber = a.number,
                arabic = a.text.stripBismillah(surah, a.numberInSurah),
                translation = translation[a.numberInSurah]?.text.orEmpty(),
            )
        }
    }

    private suspend fun fetchCyrillic(surah: Int): List<AyahEntity> = coroutineScope {
        val arabicDeferred = async { alQuran.surah(surah, AlQuranService.ARABIC) }
        val cyrlDeferred = async { quranEnc.surah(QuranEncService.UZ_CYRL, surah) }
        val arabic = arabicDeferred.await().data.first().ayahs
        val cyrl = cyrlDeferred.await().result.associateBy { it.aya.toIntOrNull() ?: 0 }
        arabic.map { a ->
            AyahEntity(
                surah = surah,
                number = a.numberInSurah,
                language = AppLanguage.UZ_CYRL.tag,
                globalNumber = a.number,
                arabic = a.text.stripBismillah(surah, a.numberInSurah),
                translation = cyrl[a.numberInSurah]?.translation.orEmpty(),
            )
        }
    }

    override suspend fun ayah(surah: Int, ayah: Int, language: AppLanguage): Ayah? = withContext(io) {
        dao.get(surah, ayah, language.tag)?.toModel(language) ?: run {
            sync(surah, language)
            dao.get(surah, ayah, language.tag)?.toModel(language)
        }
    }

    override val lastRead: Flow<Pair<Int, Int>?> = preferences.lastRead

    override suspend fun setLastRead(surah: Int, ayah: Int) = preferences.setLastRead(surah, ayah)

    /** O'zbekcha tarjima manbadan qaysi yozuvda kelmasin, tanlangan yozuvda (lotin/kirill) ko'rsatiladi. */
    private fun AyahEntity.toModel(language: AppLanguage) = Ayah(
        surah, number, globalNumber, arabic,
        when (language) {
            AppLanguage.UZ_LATN -> UzTransliterator.toScript(translation, cyrillic = false)
            AppLanguage.UZ_CYRL -> UzTransliterator.toScript(translation, cyrillic = true)
            else -> translation
        },
    )

    /**
     * alquran.cloud har bir suraning 1-oyatiga "Bismillah"ni qo'shib beradi (Fotiha va Tavbadan tashqari).
     * Bismillah sarlavha sifatida alohida ko'rsatiladi, shuning uchun matndan olib tashlanadi.
     */
    private fun String.stripBismillah(surah: Int, ayah: Int): String {
        if (surah == 1 || surah == 9 || ayah != 1) return this
        val words = trim().trimStart('\uFEFF').split(' ')
        return if (words.size > 4 && words[0].startsWith("بِس")) words.drop(4).joinToString(" ") else this
    }
}
