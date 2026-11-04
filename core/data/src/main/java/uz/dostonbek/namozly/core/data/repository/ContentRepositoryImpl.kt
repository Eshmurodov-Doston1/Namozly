package uz.dostonbek.namozly.core.data.repository

import uz.dostonbek.namozly.core.data.source.All99NamesOfAllah
import uz.dostonbek.namozly.core.data.source.DhikrData
import uz.dostonbek.namozly.core.data.source.DuasData
import uz.dostonbek.namozly.core.data.source.EventsData
import uz.dostonbek.namozly.core.data.source.LessonsData
import uz.dostonbek.namozly.core.data.source.LiveChannelsData
import uz.dostonbek.namozly.core.data.source.PrayerGuidesData
import uz.dostonbek.namozly.core.domain.repository.ContentRepository
import uz.dostonbek.namozly.core.model.AllahName
import uz.dostonbek.namozly.core.model.Dhikr
import uz.dostonbek.namozly.core.model.Dua
import uz.dostonbek.namozly.core.model.Gender
import uz.dostonbek.namozly.core.model.IslamicEvent
import uz.dostonbek.namozly.core.model.LiveChannel
import uz.dostonbek.namozly.core.model.LocalizedText
import uz.dostonbek.namozly.core.model.PrayerGuide
import uz.dostonbek.namozly.core.model.PrayerStep
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepositoryImpl @Inject constructor() : ContentRepository {

    private val names by lazy {
        All99NamesOfAllah.names.map {
            AllahName(
                number = it.number,
                arabic = it.arabicName,
                name = LocalizedText(it.nameUz, it.nameKril, it.nameRu, it.nameEn),
                meaning = LocalizedText(it.meaningUz, it.meaningKril, it.meaningRu, it.meaningEn),
            )
        }
    }

    override fun allahNames(): List<AllahName> = names
    override fun duas(): List<Dua> = DuasData.all
    override fun liveChannels(): List<LiveChannel> = LiveChannelsData.all
    override fun islamicEvents(): List<IslamicEvent> = EventsData.all
    override fun dhikrs(): List<Dhikr> = DhikrData.all
    override fun lessons(gender: Gender): List<uz.dostonbek.namozly.core.model.Lesson> = LessonsData.lessons(gender)

    /**
     * Bomdod sunnati to'liq qadamlar bilan berilgan. Qolgan namozlar uchun xuddi shu
     * rakat tuzilmasi ishlatiladi, faqat niyat va qa'da qadamlari moslashtiriladi.
     */
    override fun prayerGuides(gender: Gender): List<PrayerGuide> {
        val guides = PrayerGuidesData.generate(gender)
        val base = guides.firstOrNull { it.steps.isNotEmpty() }?.steps.orEmpty()
        return guides.map { guide ->
            if (guide.steps.isNotEmpty() || base.isEmpty()) guide
            else guide.copy(steps = adaptSteps(base, guide))
        }
    }

    private fun adaptSteps(base: List<PrayerStep>, guide: PrayerGuide): List<PrayerStep> {
        val rakats = Regex("(\\d+) rakat").find(guide.prayerName)?.groupValues?.get(1)?.toIntOrNull() ?: 2
        val name = guide.prayerName.substringBefore(" (")
        val adapted = base.map { step ->
            if (step.stepNumber == 1) step.copy(
                descriptionUz = "Qiblaga yuzlanib: 'Alloh roziligi uchun bugungi ${name.lowercase()}ining $rakats rakat farzini o'qishni niyat qildim' deb ko'ngildan o'tkaziladi.",
                descriptionKril = step.descriptionKril,
                descriptionRu = "Повернувшись к Кибле, совершите намерение в сердце совершить $rakats ракаата фарда.",
                descriptionEn = "Facing the Qiblah, make the intention in your heart to perform $rakats rakats of fard prayer.",
            ) else step
        }
        if (rakats <= 2) return adapted
        val extra = PrayerStep(
            stepNumber = adapted.size + 1,
            titleUz = "${adapted.size + 1}. Qolgan rakatlar",
            titleRu = "${adapted.size + 1}. Оставшиеся ракааты",
            titleEn = "${adapted.size + 1}. Remaining rakats",
            descriptionUz = "Ikkinchi rakatdan keyin qa'dai ulo qilinib, faqat Attahiyyot o'qiladi va uchinchi rakatga turiladi. " +
                "Farzning 3-${if (rakats == 4) "4-" else ""}rakatlarida faqat Fotiha o'qiladi. " +
                "Oxirgi rakatdan keyin qa'dai oxirda Attahiyyot, Salovot va duo o'qilib, salom beriladi.",
            descriptionRu = "После второго ракаата — первое сидение (только ат-Тахият), затем встаньте. В последних ракаатах фарда читается только Фатиха. В конце — ат-Тахият, салават, дуа и салям.",
            descriptionEn = "After the 2nd rakat sit briefly reciting only at-Tahiyyat, then stand. In the remaining fard rakats recite only al-Fatiha. In the final sitting recite at-Tahiyyat, salawat and dua, then give salam.",
            iconName = "qada",
        )
        return adapted + extra
    }
}
