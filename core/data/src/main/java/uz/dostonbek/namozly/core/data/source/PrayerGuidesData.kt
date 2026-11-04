package uz.dostonbek.namozly.core.data.source

import uz.dostonbek.namozly.core.model.Gender
import uz.dostonbek.namozly.core.model.PrayerGuide
import uz.dostonbek.namozly.core.model.PrayerStep

/** Eski loyihadan ko'chirilgan namoz o'rganish ma'lumotlari. */
internal object PrayerGuidesData {
    fun generate(gender: Gender): List<PrayerGuide> {
        val prefix = if (gender == Gender.MALE) "Erkaklar uchun" else "Ayollar uchun"
        return listOf(
            PrayerGuide(
                id = "fajr_sunnah_${gender.name.lowercase()}",
                prayerName = "Bomdod namozi (2 rakat sunnat)",
                rakatDescription = "$prefix · 2 rakat sunnat",
                gender = gender,
                steps = listOf(
                    PrayerStep(
                        stepNumber = 1,
                        titleUz = "1. Niyat",
                        titleRu = "1. Намерение (Ният)",
                        titleEn = "1. Intention (Niyyah)",
                        titleKril = "1. Ният",
                        descriptionUz = "Qiblaga yuzlanib: 'Alloh roziligi uchun bugungi bomdod namozining ikki rakat sunnatini o'qishni niyat qildim' deb ko'ngildan o'tkaziladi.",
                        descriptionRu = "Повернувшись к Кибле, совершите намерение в сердце совершить 2 ракаата сунны утреннего намаза.",
                        descriptionEn = "Facing the Qiblah, make sincere intention in your heart to perform 2 rakats of Sunnah Fajr prayer.",
                        descriptionKril = "Қиблага юзланиб: 'Аллоҳ розилиги учун бугунги бомдод намозининг икки ракат суннатини ўқишни ният қилдим' деб кўнгилдан ўтказилади."
                    ),
                    PrayerStep(
                        stepNumber = 2,
                        titleUz = "2. Iftitoh takbiri",
                        titleRu = "2. Вступительный такбир",
                        titleEn = "2. Opening Takbir",
                        titleKril = "2. Ифтитоҳ такбири",
                        descriptionUz = if (gender == Gender.MALE)
                            "Qo'llar kafti qiblaga qaratilib, bosh barmoqlar quloq yumshog'iga tekkiziladi va 'Allohu akbar' deb takbir aytiladi."
                        else
                            "Qo'llar ko'krak baravarida ko'tarilib, 'Allohu akbar' deb takbir aytiladi.",
                        descriptionRu = "Поднимите руки и произнесите: 'Аллаху акбар'.",
                        descriptionEn = "Raise hands and say: 'Allahu Akbar'.",
                        descriptionKril = "Қўлларни кўтариб, 'Аллоҳу акбар' деб такбир айтилади.",
                        arabicText = "اللَّهُ أَكْبَرُ",
                        pronunciation = "Allohu Akbar",
                        translationUz = "Alloh buyukdir."
                    ),
                    PrayerStep(
                        stepNumber = 3,
                        titleUz = "3. Qiyom va Sano duosi",
                        titleRu = "3. Кыям и дуа Сана",
                        titleEn = "3. Standing & Subhanaka",
                        titleKril = "3. Қиём ва Сано дуоси",
                        descriptionUz = if (gender == Gender.MALE)
                            "O'ng qo'l chap qo'l ustiga qo'yilib, kindik ostida bog'lanadi. Sano duosi o'qiladi."
                        else
                            "O'ng qo'l chap qo'l ustiga qo'yilib, ko'krak ustida bog'lanadi. Sano duosi o'qiladi.",
                        descriptionRu = "Сложите руки и прочтите дуа 'Субханака'.",
                        descriptionEn = "Fold hands and recite the opening supplication (Subhanaka).",
                        descriptionKril = "Қўлларни боғлаб, Сано дуоси ўқилади.",
                        arabicText = "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ، وَتَبَارَكَ اسْمُكَ، وَتَعَالَىٰ جَدُّكَ، وَلَا إِلَٰهَ غَيْرُكَ",
                        pronunciation = "Subhaanakallohumma va bihamdika va tabaarokasmuka va ta'aalaa jadduka va laa ilaaha g'oyruk.",
                        translationUz = "Allohim! Senga hamd aytib, Seni nuqsonlardan poklayman. Isming muborak, ulug'liging yuksakdir va Sendan o'zga iloh yo'qdir."
                    ),
                    PrayerStep(
                        stepNumber = 4,
                        titleUz = "4. Fotiha va Zam sura",
                        titleRu = "4. Сура Аль-Фатиха и дополнительная сура",
                        titleEn = "4. Al-Fatihah & Surah",
                        titleKril = "4. Фотиҳа ва Зам сура",
                        descriptionUz = "'A'uzu billahi minash-shaytonir rojiym. Bismillahir rohmanir rohiym' aytilib, Fotiha surasi va bitta zam sura (masalan, Ixlos) o'qiladi.",
                        descriptionRu = "Прочтите суру Аль-Фатиха и дополнительную суру (например, Аль-Ихлас).",
                        descriptionEn = "Recite Surah Al-Fatihah followed by another short Surah (e.g. Al-Ikhlas).",
                        descriptionKril = "Фотиҳа сураси ва зам сура (масалан, Ихлос) ўқилади.",
                        arabicText = "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ",
                        pronunciation = "Qul huvallohu ahad. Allohus-somad. Lam yalid va lam yuvlad. Va lam yakul-lahu kufuvan ahad.",
                        translationUz = "Ayt: 'U Alloh yagonadir. Alloh behojatdir. U tug'magan va tug'ilmagan. Va hech kim Unga teng bo'lmagan'."
                    ),
                    PrayerStep(
                        stepNumber = 5,
                        titleUz = "5. Ruku (Egilish)",
                        titleRu = "5. Руку (Поясной поклон)",
                        titleEn = "5. Ruku (Bowing)",
                        titleKril = "5. Рукуъ (Эгилиш)",
                        descriptionUz = "'Allohu akbar' deb rukuga egilinadi. Tizzalar qo'l bilan ushlanadi. Kamida 3 marta 'Subhana robbiyal 'aziym' deyiladi.",
                        descriptionRu = "Сделайте поясной поклон со словами 'Аллаху акбар' и произнесите 3 раза 'Субхана роббияль-'азыйм'.",
                        descriptionEn = "Bow down saying 'Allahu Akbar' and recite 3 times 'Subhana Rabbiyal Azeem'.",
                        descriptionKril = "'Аллоҳу акбар' деб рукуга эгилинади ва 3 марта 'Субҳана роббиял ъазийм' дейилади.",
                        arabicText = "سُبْحَانَ رَبِّيَ الْعَظِيمِ",
                        pronunciation = "Subhaana robbiyal 'aziym",
                        translationUz = "Buyuk Rabbim barcha nuqsonlardan pokdir."
                    ),
                    PrayerStep(
                        stepNumber = 6,
                        titleUz = "6. Qavma (Rukudan qaytish)",
                        titleRu = "6. Выпрямление после руку",
                        titleEn = "6. Standing from Ruku",
                        titleKril = "6. Қавма (Рукуъдан қайтиш)",
                        descriptionUz = "Qad rostlanib tik turiladi va 'Sami'allohu liman hamidah', so'ngra 'Robbana lakal hamd' deyiladi.",
                        descriptionRu = "Выпрямитесь, сказав: 'Сами'аллаху лиман хамида', затем 'Роббана лякаль-хамд'.",
                        descriptionEn = "Stand upright saying 'Sami Allahu liman hamidah', then 'Rabbana lakal hamd'.",
                        descriptionKril = "Қад ростланиб, 'Самиъаллоҳу лиман ҳамидаҳ', сўнг 'Роббана лакал ҳамд' дейилади.",
                        arabicText = "سَمِعَ اللَّهُ لِمَنْ حَمِدَهُ ۝ رَبَّنَا لَكَ الْحَمْدُ",
                        pronunciation = "Sami'allohu liman hamidah. Robbanaa lakal hamd.",
                        translationUz = "Alloh O'ziga hamd aytuvchini eshitdi. Ey Rabbimiz, Senga hamd bo'lsin."
                    ),
                    PrayerStep(
                        stepNumber = 7,
                        titleUz = "7. Sajda",
                        titleRu = "7. Сажда (Земной поклон)",
                        titleEn = "7. Sujood (Prostration)",
                        titleKril = "7. Сажда",
                        descriptionUz = "'Allohu akbar' deb sajdaga boriladi. Peshona, burun, ikki kaft, tizzalar va oyoq barmoqlari yerga tekkiziladi. 3 marta tasbeh aytiladi.",
                        descriptionRu = "Сделайте земной поклон, сказав 'Аллаху акбар', и произнесите 3 раза: 'Субхана роббияль-А'ля'.",
                        descriptionEn = "Prostrate saying 'Allahu Akbar' and recite 3 times: 'Subhana Rabbiyal A'la'.",
                        descriptionKril = "'Аллоҳу акбар' деб саждага борилади ва 3 марта тасбеҳ айтилади.",
                        arabicText = "سُبْحَانَ رَبِّيَ الأَعْلَى",
                        pronunciation = "Subhaana robbiyal a'laa",
                        translationUz = "Eng oliy zot bo'lgan Rabbim pokdir."
                    ),
                    PrayerStep(
                        stepNumber = 8,
                        titleUz = "8. Qa'da (Attahiyyot va Salom)",
                        titleRu = "8. Сидение и приветствие (Ташаххуд и Салям)",
                        titleEn = "8. Sitting & Final Salam",
                        titleKril = "8. Қаъда (Аттаҳийёт ва Салом)",
                        descriptionUz = "Ikkinchi rakat sajdasidan keyin o'tiriladi. Attahiyyot, Salovot va Robbana atina duolari o'qilib, o'ng va chap yelkaga salom beriladi.",
                        descriptionRu = "Прочтите дуа 'Аттахият', 'Салават' и совершите салям направо и налево.",
                        descriptionEn = "Recite Tashahhud, Salawat and conclude with Salam to the right and left.",
                        descriptionKril = "Аттаҳийёт, Саловот ва дуолар ўқилиб, ўнг ва чап томонга салом берилади.",
                        arabicText = "السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ",
                        pronunciation = "Assalaamu 'alaykum va rohmatulloh",
                        translationUz = "Sizlarga Allohning tinchligi va rahmati bo'lsin."
                    )
                )
            ),
            PrayerGuide(
                id = "fajr_fard_${gender.name.lowercase()}",
                prayerName = "Bomdod namozi (2 rakat farz)",
                rakatDescription = "$prefix · 2 rakat farz",
                gender = gender,
                steps = emptyList() // Qadamlar sunnatga o'xshash tartibda
            ),
            PrayerGuide(
                id = "dhuhr_${gender.name.lowercase()}",
                prayerName = "Peshin namozi (4 rakat farz)",
                rakatDescription = "$prefix · 4 rakat farz",
                gender = gender,
                steps = emptyList()
            ),
            PrayerGuide(
                id = "asr_${gender.name.lowercase()}",
                prayerName = "Asr namozi (4 rakat farz)",
                rakatDescription = "$prefix · 4 rakat farz",
                gender = gender,
                steps = emptyList()
            ),
            PrayerGuide(
                id = "maghrib_${gender.name.lowercase()}",
                prayerName = "Shom namozi (3 rakat farz)",
                rakatDescription = "$prefix · 3 rakat farz",
                gender = gender,
                steps = emptyList()
            ),
            PrayerGuide(
                id = "isha_${gender.name.lowercase()}",
                prayerName = "Xufton namozi (4 rakat farz)",
                rakatDescription = "$prefix · 4 rakat farz va 3 rakat vitr",
                gender = gender,
                steps = emptyList()
            )
        )
    }
}
