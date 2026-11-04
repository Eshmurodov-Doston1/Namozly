package uz.dostonbek.namozly.core.data.source

import uz.dostonbek.namozly.core.model.Gender
import uz.dostonbek.namozly.core.model.Lesson
import uz.dostonbek.namozly.core.model.LessonIcon
import uz.dostonbek.namozly.core.model.LessonRecitation
import uz.dostonbek.namozly.core.model.LessonStep
import uz.dostonbek.namozly.core.model.Posture
import uz.dostonbek.namozly.core.model.uzText

/**
 * Namoz o'rganish darslari (Hanafiy mazhabi bo'yicha). Matnlar umumqabul qilingan
 * fiqh qo'llanmalariga asoslangan; erkak va ayol uchun farqlar alohida ko'rsatilgan.
 */
internal object LessonsData {

    private val takbir = LessonRecitation("اللَّهُ أَكْبَرُ", "Allohu akbar", uzText("Alloh buyukdir", "Аллах велик", "Allah is the Greatest"))
    private val sano = LessonRecitation(
        "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ وَتَبَارَكَ اسْمُكَ وَتَعَالَى جَدُّكَ وَلَا إِلَهَ غَيْرُكَ",
        "Subhaanakallohumma va bihamdika va tabaarokasmuka va ta'aalaa jadduka va laa ilaaha g'oyruk.",
        uzText(
            "Allohim, Seni hamd bilan poklayman. Isming muborak, ulug'liging yuksak, Sendan o'zga iloh yo'q.",
            "О Аллах, пречист Ты, и хвала Тебе. Благословенно имя Твоё, превыше всего величие Твоё, и нет божества, кроме Тебя.",
            "Glory be to You, O Allah, and praise. Blessed is Your name, exalted is Your majesty, there is no god but You.",
        ),
    )
    private val auzu = LessonRecitation(
        "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ ۝ بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
        "A'uuzu billahi minash-shaytonir rojiym. Bismillahir rohmanir rohiym.",
        uzText("Quvilgan shaytondan Allohga panoh so'rayman. Mehribon va rahmli Alloh nomi bilan.", "Прибегаю к Аллаху от проклятого шайтана. С именем Аллаха Милостивого, Милосердного.", "I seek refuge in Allah from the accursed Satan. In the name of Allah, the Most Gracious, the Most Merciful."),
    )
    private val fatiha = LessonRecitation(
        "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَٰلَمِينَ ۝ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ ۝ مَٰلِكِ يَوْمِ ٱلدِّينِ ۝ إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ ۝ ٱهْدِنَا ٱلصِّرَٰطَ ٱلْمُسْتَقِيمَ ۝ صِرَٰطَ ٱلَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ ٱلْمَغْضُوبِ عَلَيْهِمْ وَلَا ٱلضَّآلِّينَ",
        "Alhamdu lillahi robbil 'aalamiyn. Ar-rohmanir rohiym. Maaliki yavmid-diyn. Iyyaaka na'budu va iyyaaka nasta'iyn. Ihdinas-sirootol mustaqiym. Sirootol-laziyna an'amta 'alayhim g'oyril mag'zuubi 'alayhim va laz-zoolliyn. (Omiyn)",
        uzText(
            "Hamd olamlar Robbi Allohgadir. U mehribon va rahmli, qiyomat kunining egasidir. Faqat Senga ibodat qilamiz va faqat Sendan yordam so'raymiz. Bizni to'g'ri yo'lga hidoyat qil — ne'mat berganlaring yo'liga, g'azabga uchraganlar va adashganlarning yo'liga emas.",
            "Хвала Аллаху, Господу миров, Милостивому, Милосердному, Властелину Дня воздаяния. Тебе одному мы поклоняемся и Тебя одного молим о помощи. Веди нас прямым путём — путём тех, кого Ты облагодетельствовал, не тех, на кого пал гнев, и не заблудших.",
            "All praise is for Allah, Lord of the worlds, the Most Gracious, the Most Merciful, Master of the Day of Judgement. You alone we worship and You alone we ask for help. Guide us to the straight path — the path of those You have blessed, not of those who earned anger nor of those who went astray.",
        ),
    )
    private val ikhlas = LessonRecitation(
        "قُلْ هُوَ ٱللَّهُ أَحَدٌ ۝ ٱللَّهُ ٱلصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُن لَّهُۥ كُفُوًا أَحَدٌۢ",
        "Qul huvallohu ahad. Allohus-somad. Lam yalid va lam yuvlad. Va lam yakul-lahu kufuvan ahad.",
        uzText("Ayting: U Alloh yagonadir. Alloh behojatdir. U tug'magan va tug'ilmagan. Unga hech kim teng emas.", "Скажи: Он — Аллах Единый, Аллах Самодостаточный. Он не родил и не был рождён, и нет никого, равного Ему.", "Say: He is Allah, the One. Allah, the Eternal Refuge. He neither begets nor is born, and there is none comparable to Him."),
    )
    private val rukuTasbih = LessonRecitation("سُبْحَانَ رَبِّيَ الْعَظِيمِ", "Subhaana robbiyal 'aziym", uzText("Ulug' Robbimni poklab yod etaman", "Пречист мой Великий Господь", "Glory be to my Lord, the Magnificent"), 3)
    private val qavma = LessonRecitation("سَمِعَ اللَّهُ لِمَنْ حَمِدَهُ ۝ رَبَّنَا لَكَ الْحَمْدُ", "Sami'allohu liman hamidah. Robbanaa lakal hamd.", uzText("Alloh O'ziga hamd aytganni eshitdi. Robbimiz, hamd Sengadir.", "Аллах слышит восхваляющего Его. Господь наш, Тебе хвала.", "Allah hears the one who praises Him. Our Lord, to You be praise."))
    private val sajdaTasbih = LessonRecitation("سُبْحَانَ رَبِّيَ الْأَعْلَى", "Subhaana robbiyal a'laa", uzText("Oliy Robbimni poklab yod etaman", "Пречист мой Всевышний Господь", "Glory be to my Lord, the Most High"), 3)
    private val tashahhud = LessonRecitation(
        "التَّحِيَّاتُ لِلَّهِ وَالصَّلَوَاتُ وَالطَّيِّبَاتُ السَّلَامُ عَلَيْكَ أَيُّهَا النَّبِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ السَّلَامُ عَلَيْنَا وَعَلَى عِبَادِ اللَّهِ الصَّالِحِينَ أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا اللَّهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
        "Attahiyyaatu lillahi vas-solavaatu vat-toyyibaat. Assalaamu 'alayka ayyuhan-nabiyyu va rohmatullohi va barokaatuh. Assalaamu 'alaynaa va 'alaa 'ibaadillahis-soolihiyn. Ashhadu allaa ilaaha illallohu va ashhadu anna Muhammadan 'abduhu va rosuuluh.",
        uzText(
            "Barcha tahiyyalar, duolar va pok so'zlar Allohgadir. Ey Nabiy, sizga salom, Allohning rahmati va barakoti bo'lsin. Bizga va Allohning solih bandalariga salom bo'lsin. Allohdan o'zga iloh yo'qligiga va Muhammad Uning bandasi va Rasuli ekaniga guvohlik beraman.",
            "Приветствия, молитвы и благие слова — Аллаху. Мир тебе, о Пророк, милость Аллаха и Его благословение. Мир нам и праведным рабам Аллаха. Свидетельствую, что нет божества, кроме Аллаха, и что Мухаммад — Его раб и посланник.",
            "All greetings, prayers and good words are for Allah. Peace be upon you, O Prophet, and the mercy of Allah and His blessings. Peace be upon us and upon the righteous servants of Allah. I bear witness that there is no god but Allah and that Muhammad is His servant and messenger.",
        ),
    )
    private val salawat = LessonRecitation(
        "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ كَمَا صَلَّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ ۝ اللَّهُمَّ بَارِكْ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ كَمَا بَارَكْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ",
        "Allohumma solli 'alaa Muhammadin va 'alaa aali Muhammad, kamaa sollayta 'alaa Ibroohiyma va 'alaa aali Ibroohiym, innaka hamiydum majiyd. Allohumma baarik 'alaa Muhammadin va 'alaa aali Muhammad, kamaa baarakta 'alaa Ibroohiyma va 'alaa aali Ibroohiym, innaka hamiydum majiyd.",
        uzText(
            "Allohim, Ibrohim va uning oilasiga salovot yo'llaganingdek, Muhammad va uning oilasiga ham salovot yo'lla. Ibrohim va uning oilasiga baraka berganingdek, Muhammad va uning oilasiga ham baraka ber. Albatta, Sen maqtovga loyiq va ulug'san.",
            "О Аллах, благослови Мухаммада и семью Мухаммада, как Ты благословил Ибрахима и семью Ибрахима. Даруй благодать Мухаммаду и семье Мухаммада, как даровал Ибрахиму и семье Ибрахима. Воистину, Ты Достохвальный, Славный.",
            "O Allah, send prayers upon Muhammad and the family of Muhammad as You sent prayers upon Ibrahim and his family, and bless Muhammad and his family as You blessed Ibrahim and his family. You are Praiseworthy, Glorious.",
        ),
    )
    private val rabbana = LessonRecitation(
        "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
        "Robbanaa aatinaa fid-dunyaa hasanatan va fil-aaxiroti hasanatan va qinaa 'azaaban-naar.",
        uzText("Robbimiz, bizga dunyoda ham, oxiratda ham yaxshilik ber va do'zax azobidan saqla.", "Господь наш, даруй нам благо в этом мире и в Последней жизни и защити от мучений Огня.", "Our Lord, give us good in this world and in the Hereafter and protect us from the punishment of the Fire."),
    )
    private val salam = LessonRecitation("السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ", "Assalaamu 'alaykum va rohmatulloh", uzText("Sizlarga salom va Allohning rahmati bo'lsin", "Мир вам и милость Аллаха", "Peace and the mercy of Allah be upon you"))
    private val wuduDua = LessonRecitation(
        "أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
        "Ashhadu allaa ilaaha illallohu vahdahuu laa shariyka lah, va ashhadu anna Muhammadan 'abduhuu va rosuuluh.",
        uzText("Yolg'iz, sherigi yo'q Allohdan o'zga iloh yo'qligiga va Muhammad Uning bandasi va Rasuli ekaniga guvohlik beraman.", "Свидетельствую, что нет божества, кроме одного Аллаха, у Которого нет сотоварищей, и что Мухаммад — Его раб и посланник.", "I bear witness that there is no god but Allah alone, without partner, and that Muhammad is His servant and messenger."),
    )
    private val qunut = LessonRecitation(
        "اللَّهُمَّ إِنَّا نَسْتَعِينُكَ وَنَسْتَغْفِرُكَ وَنُؤْمِنُ بِكَ وَنَتَوَكَّلُ عَلَيْكَ وَنُثْنِي عَلَيْكَ الْخَيْرَ كُلَّهُ نَشْكُرُكَ وَلَا نَكْفُرُكَ وَنَخْلَعُ وَنَتْرُكُ مَنْ يَفْجُرُكَ ۝ اللَّهُمَّ إِيَّاكَ نَعْبُدُ وَلَكَ نُصَلِّي وَنَسْجُدُ وَإِلَيْكَ نَسْعَى وَنَحْفِدُ نَرْجُو رَحْمَتَكَ وَنَخْشَى عَذَابَكَ إِنَّ عَذَابَكَ بِالْكُفَّارِ مُلْحِقٌ",
        "Allohumma innaa nasta'iynuka va nastag'firuka va nu'minu bika va natavakkalu 'alayka va nusniy 'alaykal xoyra kullahu nashkuruka va laa nakfuruka va naxla'u va natruku man yafjuruk. Allohumma iyyaaka na'budu va laka nusolliy va nasjudu va ilayka nas'aa va nahfidu narjuu rohmataka va naxshaa 'azaabaka inna 'azaabaka bil kuffaari mulhiq.",
        uzText(
            "Allohim, Sendan yordam va mag'firat so'raymiz, Senga iymon keltirib, tavakkal qilamiz, Seni yaxshilik bilan maqtaymiz, shukr qilamiz, noshukrlik qilmaymiz, Senga osiylik qilganni tark etamiz. Allohim, faqat Senga ibodat qilamiz, Senga namoz o'qib sajda qilamiz, Sen tomon shoshilamiz, rahmatingni umid qilib, azobingdan qo'rqamiz — azobing kofirlarga yetguvchidir.",
            "О Аллах, мы просим Тебя о помощи и прощении, веруем в Тебя и уповаем на Тебя, восхваляем Тебя, благодарим и не отвергаем Тебя, оставляем тех, кто ослушается Тебя. О Аллах, Тебе мы поклоняемся, Тебе молимся и падаем ниц, к Тебе стремимся, надеемся на Твою милость и боимся Твоего наказания.",
            "O Allah, we seek Your help and forgiveness, believe in You and rely on You, praise You, thank You and are not ungrateful, and we abandon whoever disobeys You. O Allah, You alone we worship, to You we pray and prostrate, to You we hasten, hoping for Your mercy and fearing Your punishment.",
        ),
    )

    private class T(val uz: String, val ru: String, val en: String)
    private fun t(uz: String, ru: String, en: String) = T(uz, ru, en)

    private fun step(
        title: T, text: T, posture: Posture, rakat: Int? = null, recite: LessonRecitation? = null, short: T = title,
    ) = LessonStep(uzText(title.uz, title.ru, title.en), uzText(text.uz, text.ru, text.en), posture, rakat, recite, uzText(short.uz, short.ru, short.en))

    /** Erkak/ayol uchun matn tanlash. */
    private fun Gender.pick(male: T, female: T) = if (this == Gender.MALE) male else female

    // ─────────────────────────── Namoz qadamlari ───────────────────────────

    private fun preparation(g: Gender) = step(
        t("Avrat va libos", "Аурат и одежда", "Covering and clothing"),
        g.pick(
            t(
                "Erkak kishining avrati kindikdan tizzagacha (tizza ham kiradi). Namozga toza, yelkani yopadigan kiyimda turing. Qiblaga yuzlaning, oyoqlar orasi to'rt barmoqcha ochiq bo'lsin.",
                "Аурат мужчины — от пупка до колен (включая колени). Встаньте в чистой одежде, закрывающей плечи, лицом к Кибле; между стопами — около четырёх пальцев.",
                "A man's awrah is from the navel to the knees (knees included). Stand in clean clothes covering the shoulders, facing the Qibla, feet about four fingers apart.",
            ),
            t(
                "Ayolning yuzi, kaftlari va oyoq panjalaridan boshqa butun badani avrat. Sochlar, quloqlar, bo'yin va bilaklar to'liq yopilsin; kiyim keng va badanni ko'rsatmaydigan bo'lsin. Qiblaga yuzlaning, oyoqlar bir-biriga yaqin turadi.",
                "Аурат женщины — всё тело, кроме лица, кистей рук и стоп. Волосы, уши, шея и запястья должны быть полностью закрыты, одежда — свободной и непрозрачной. Встаньте лицом к Кибле, стопы рядом.",
                "A woman's awrah is the whole body except the face, hands and feet. Hair, ears, neck and wrists must be fully covered, clothing loose and opaque. Face the Qibla with the feet close together.",
            ),
        ),
        Posture.NIYAT, null, null, t("Libos", "Одежда", "Clothing"),
    )

    /** Namozning umumiy boshlanishi: niyat → takbir → sano → qiroat → ruku → qavma → sajda. */
    private fun opening(g: Gender, prayerUz: String, prayerRu: String, prayerEn: String, rakats: Int): List<LessonStep> = listOf(
        preparation(g),
        step(
            t("Niyat", "Намерение", "Intention"),
            g.pick(
                t(
                    "Qalbingizda \"Alloh roziligi uchun bugungi $prayerUz namozining $rakats rakat farzini o'qishni niyat qildim\" deb niyat qiling. Farzni masjidda jamoat bilan o'qish afzal; yolg'iz o'qisangiz, iqomat aytish sunnat.",
                    "Сделайте в сердце намерение совершить $rakats ракаата фарда намаза $prayerRu ради Аллаха. Фард лучше совершать в мечети с джамаатом; молясь один, произнесите икамат (сунна).",
                    "Make the intention in your heart to pray the $rakats rakats of fard of $prayerEn for the sake of Allah. Fard is best prayed in congregation at the mosque; praying alone, saying the iqamah is sunnah.",
                ),
                t(
                    "Qalbingizda \"Alloh roziligi uchun bugungi $prayerUz namozining $rakats rakat farzini o'qishni niyat qildim\" deb niyat qiling. Ayollar azon va iqomat aytmaydi; ayolning namozi uyida o'qishi afzal.",
                    "Сделайте в сердце намерение совершить $rakats ракаата фарда намаза $prayerRu ради Аллаха. Женщины не произносят азан и икамат; женщине предпочтительнее молиться дома.",
                    "Make the intention in your heart to pray the $rakats rakats of fard of $prayerEn for the sake of Allah. Women do not call the adhan or iqamah; it is better for a woman to pray at home.",
                ),
            ),
            Posture.NIYAT, 1,
        ),
        step(
            t("Iftitoh takbiri", "Начальный такбир", "Opening takbir"),
            g.pick(
                t(
                    "Qo'llarni kaftlari qiblaga qaragan holda ko'tarib, bosh barmoqlarni quloq yumshog'iga tekkizing va \"Allohu akbar\" deng.",
                    "Поднимите руки ладонями к Кибле, коснувшись большими пальцами мочек ушей, и скажите «Аллаху акбар».",
                    "Raise your hands, palms facing the Qibla, thumbs touching the earlobes, and say \"Allahu Akbar\".",
                ),
                t(
                    "Qo'llarni yengdan chiqarmasdan, barmoq uchlari yelka baravariga yetadigan qilib ko'taring va \"Allohu akbar\" deng. Ayollar qo'lni quloqqacha ko'tarmaydi.",
                    "Не вынимая рук из рукавов, поднимите их так, чтобы кончики пальцев были на уровне плеч, и скажите «Аллаху акбар». Женщины не поднимают руки до ушей.",
                    "Without taking the hands out of the sleeves, raise them so the fingertips reach shoulder level and say \"Allahu Akbar\". Women do not raise the hands to the ears.",
                ),
            ),
            Posture.TAKBIR, 1, takbir, t("Takbir", "Такбир", "Takbir"),
        ),
        step(
            t("Qiyom va Sano", "Кыям и Сана", "Standing and Thana"),
            g.pick(
                t(
                    "O'ng qo'l bilan chap qo'lning bilagini ushlab, kindik ostida bog'lang. Nigoh sajda joyida. Sano duosini ichingizda o'qing.",
                    "Обхватите правой рукой запястье левой и положите руки ниже пупка. Взгляд — на место земного поклона. Про себя прочитайте Сана.",
                    "Grasp the left wrist with the right hand and place the hands below the navel. Look at the place of prostration and silently recite the Thana.",
                ),
                t(
                    "O'ng kaftni chap qo'l ustiga (ushlamasdan) qo'yib, qo'llarni ko'krak ustida tuting. Nigoh sajda joyida. Sano duosini ichingizda o'qing.",
                    "Положите правую ладонь на левую руку (не обхватывая) на груди. Взгляд — на место земного поклона. Про себя прочитайте Сана.",
                    "Place the right palm over the left hand (without grasping) on the chest. Look at the place of prostration and silently recite the Thana.",
                ),
            ),
            Posture.QIYAM, 1, sano, t("Qiyom", "Кыям", "Qiyam"),
        ),
        step(
            t("Qiroat: Fotiha va sura", "Чтение: Фатиха и сура", "Recitation: Fatiha and surah"),
            g.pick(
                t(
                    "Ichingizda \"A'uzu\" va \"Bismillah\"ni, so'ng Fotiha surasini o'qing, \"Omiyn\" deng va qisqa sura (masalan, Ixlos) qo'shing. Bomdod, shom va xuftonning farzida imom qiroatni ovoz chiqarib o'qiydi; yolg'iz bo'lsangiz ixtiyoriy.",
                    "Про себя произнесите «Аузу» и «Бисмиллях», прочитайте аль-Фатиху, скажите «Амин» и добавьте короткую суру. В фарде фаджра, магриба и иши имам читает вслух; молящийся в одиночку — по желанию.",
                    "Silently say the Ta'awwudh and Basmala, recite al-Fatiha, say \"Amin\" and add a short surah. In the fard of Fajr, Maghrib and Isha the imam recites aloud; praying alone it is optional.",
                ),
                t(
                    "Ichingizda \"A'uzu\" va \"Bismillah\"ni, so'ng Fotiha surasini o'qing, \"Omiyn\" deng va qisqa sura (masalan, Ixlos) qo'shing. Ayollar barcha namozlarda qiroatni ovoz chiqarmasdan, faqat o'zi eshitadigan qilib o'qiydi.",
                    "Про себя произнесите «Аузу» и «Бисмиллях», прочитайте аль-Фатиху, скажите «Амин» и добавьте короткую суру. Женщины во всех намазах читают негромко, так, чтобы слышать только себя.",
                    "Silently say the Ta'awwudh and Basmala, recite al-Fatiha, say \"Amin\" and add a short surah. Women recite quietly in every prayer, only loud enough to hear themselves.",
                ),
            ),
            Posture.QIYAM, 1, fatiha, t("Fotiha", "Фатиха", "Fatiha"),
        ),
        step(
            t("Ruku'", "Руку", "Ruku"),
            g.pick(
                t(
                    "\"Allohu akbar\" deb egiling: bel va bosh bir tekis, oyoqlar tik, qo'llar barmoqlar ochiq holda tizzalarni mahkam ushlasin, tirsaklar biqindan ochiq. Nigoh oyoq uchlarida.",
                    "Со словами «Аллаху акбар» наклонитесь: спина и голова на одной линии, ноги прямые, ладони с раскрытыми пальцами обхватывают колени, локти отведены от боков.",
                    "Say \"Allahu Akbar\" and bow: back and head level, legs straight, hands gripping the knees with fingers spread, elbows away from the sides.",
                ),
                t(
                    "\"Allohu akbar\" deb biroz egiling — bel to'liq tekislanmaydi. Qo'llarni barmoqlar jips holda tizzalar ustiga qo'ying (ushlamay), tizzalar biroz bukilgan, tirsaklar biqinga yopishgan.",
                    "Со словами «Аллаху акбар» слегка наклонитесь — спина не выпрямляется полностью. Положите ладони с сомкнутыми пальцами на колени (не обхватывая), колени немного согнуты, локти прижаты к бокам.",
                    "Say \"Allahu Akbar\" and bow slightly — the back is not fully level. Rest the hands on the knees with fingers together (without gripping), knees slightly bent, elbows against the sides.",
                ),
            ),
            Posture.RUKU, 1, rukuTasbih, t("Ruku'", "Руку", "Ruku"),
        ),
        step(
            t("Qavma", "Кавма", "Standing up"),
            t(
                "\"Sami'allohu liman hamidah\" deb rostlaning va tik turib \"Robbanaa lakal hamd\" deng. Qo'llar yon tomonda tushirilgan.",
                "Выпрямитесь со словами «Сами‘аллаху ли ман хамидах» и, стоя, скажите «Раббана лякаль-хамд». Руки опущены по бокам.",
                "Rise saying \"Sami'Allahu liman hamidah\", then standing say \"Rabbana lakal hamd\". The arms hang at the sides.",
            ),
            Posture.QAVMA, 1, qavma, t("Qavma", "Кавма", "Qawma"),
        ),
        step(
            t("Sajda", "Сажда", "Sujud"),
            g.pick(
                t(
                    "\"Allohu akbar\" deb avval tizza, so'ng qo'llar, keyin burun va peshonani yerga qo'ying. Qorin sondan, tirsaklar yerdan va biqindan ochiq; oyoq barmoqlari qiblaga qaratib tik turadi. So'ng o'tirib (jalsa), ikkinchi sajdani qiling.",
                    "Со словами «Аллаху акбар» опуститесь: колени, руки, затем нос и лоб. Живот не касается бёдер, локти подняты и отведены от боков, пальцы ног направлены к Кибле. Сядьте и совершите второй поклон.",
                    "Say \"Allahu Akbar\" and go down: knees, hands, then nose and forehead. The belly is away from the thighs, elbows raised and away from the sides, toes pointing to the Qibla. Sit, then make the second prostration.",
                ),
                t(
                    "\"Allohu akbar\" deb sajdaga boring va jipslashing: qorinni sonlarga tekkizing, bilaklarni yerga yotqizib, tirsaklarni biqinga yopishtiring; oyoqlarni o'ng tomonga chiqarib yotqizish mumkin. So'ng o'tirib (jalsa), ikkinchi sajdani qiling.",
                    "Со словами «Аллаху акбар» опуститесь в поклон, собравшись: живот прижат к бёдрам, предплечья лежат на полу, локти прижаты к бокам; стопы можно вывести вправо. Сядьте и совершите второй поклон.",
                    "Say \"Allahu Akbar\" and prostrate compactly: belly against the thighs, forearms resting on the floor, elbows against the sides; the feet may be laid out to the right. Sit, then make the second prostration.",
                ),
            ),
            Posture.SAJDA, 1, sajdaTasbih, t("Sajda", "Сажда", "Sajda"),
        ),
    )

    private fun sitting(g: Gender): T = g.pick(
        t(
            "Chap oyoqni yotqizib, ustiga o'tiring; o'ng oyoq barmoqlarini qiblaga qaratib tik qo'ying. Qo'llar tizzalar ustida, barmoqlar ochiq.",
            "Сядьте на левую стопу, правую поставьте вертикально, пальцами к Кибле. Руки на коленях, пальцы свободно.",
            "Sit on the left foot with the right foot upright, toes towards the Qibla. Hands on the knees, fingers relaxed.",
        ),
        t(
            "Chap sonning ustiga yerga o'tirib, ikkala oyoqni o'ng tomonga chiqaring (tavarruk). Qo'llar sonlar ustida, barmoqlar jips.",
            "Сядьте на пол на левое бедро, выведя обе стопы вправо (таваррук). Руки на бёдрах, пальцы сомкнуты.",
            "Sit on the floor on the left hip with both feet out to the right (tawarruk). Hands on the thighs, fingers together.",
        ),
    )

    private fun firstSitting(g: Gender): LessonStep {
        val s = sitting(g)
        return step(
            t("Qa'dai ulo", "Первое сидение", "First sitting"),
            t(
                "${s.uz} Ikkinchi rakatdan keyin faqat Attahiyyotni o'qing va \"Allohu akbar\" deb uchinchi rakatga turing.",
                "${s.ru} После второго ракаата прочитайте только ат-Тахият и встаньте на третий ракаат.",
                "${s.en} After the second rakat recite only the Tashahhud and stand for the third rakat.",
            ),
            Posture.QADA, 2, tashahhud, t("Qa'dai ulo", "1-е сидение", "1st sitting"),
        )
    }

    private fun finalSitting(g: Gender, rakat: Int): LessonStep {
        val s = sitting(g)
        return step(
            t("Qa'da: Tashahhud, salovot, duo", "Сидение: ташаххуд, салават, дуа", "Final sitting"),
            t(
                "${s.uz} Attahiyyotni o'qing (\"laa ilaaha\" deganda ko'rsatkich barmoqni ko'tarib, \"illalloh\"da tushiring), so'ng Salovot va duo o'qing.",
                "${s.ru} Прочитайте ат-Тахият (на словах «ля иляха» поднимите указательный палец, на «илляллах» опустите), затем салават и дуа.",
                "${s.en} Recite the Tashahhud (raise the index finger at \"la ilaha\" and lower it at \"illallah\"), then the Salawat and a supplication.",
            ),
            Posture.QADA, rakat, tashahhud, t("Qa'da", "Када", "Sitting"),
        )
    }

    private val salamStep = step(
        t("Salom", "Салям", "Salam"),
        t(
            "Boshni avval o'ngga, so'ng chapga burib, har safar \"Assalaamu 'alaykum va rohmatulloh\" deng. Namoz tugadi.",
            "Поверните голову вправо, затем влево, каждый раз произнося «Ассаляму алейкум ва рахматуллах». Намаз завершён.",
            "Turn your head to the right, then to the left, saying \"Assalamu alaykum wa rahmatullah\" each time. The prayer is complete.",
        ),
        Posture.SALAM, null, salam, t("Salom", "Салям", "Salam"),
    )

    private fun secondRakat() = step(
        t("Ikkinchi rakat", "Второй ракаат", "Second rakat"),
        t(
            "\"Allohu akbar\" deb turing. Fotiha va sura o'qing, so'ng ruku', qavma va ikki sajdani birinchi rakatdagidek bajaring.",
            "Встаньте со словами «Аллаху акбар», прочитайте Фатиху и суру, затем руку, кавму и два земных поклона, как в первом ракаате.",
            "Stand with \"Allahu Akbar\", recite al-Fatiha and a surah, then ruku, qawma and two prostrations as in the first rakat.",
        ),
        Posture.QIYAM, 2, ikhlas, t("2-rakat", "2-й ракаат", "Rakat 2"),
    )

    private fun stepCount(n: Int) = uzText("$n qadam", "$n шагов", "$n steps")

    private fun lesson(id: String, g: Gender, title: T, sub: T?, icon: LessonIcon, steps: List<LessonStep>): Lesson {
        val count = stepCount(steps.size)
        val subtitle = sub?.let { uzText("${count.uz} · ${it.uz}", "${count.ru} · ${it.ru}", "${count.en} · ${it.en}") } ?: count
        return Lesson(if (g == Gender.FEMALE) "f_$id" else id, uzText(title.uz, title.ru, title.en), subtitle, icon, steps)
    }

    fun lessons(g: Gender): List<Lesson> = listOfNotNull(
        lesson(
            "wudu", g, t("Tahorat", "Омовение (вуду)", "Wudu"), t("4 daqiqa", "4 мин", "4 min"), LessonIcon.WUDU,
            listOf(
                step(t("Niyat va Bismillah", "Намерение и Бисмиллях", "Intention and Bismillah"),
                    t("Qalbda tahorat niyatini qiling va \"Bismillahir rohmanir rohiym\" deb boshlang.",
                        "Сделайте намерение и начните со слов «Бисмилляхи-р-рахмани-р-рахим».",
                        "Make the intention and begin with \"Bismillahir-Rahmanir-Rahim\"."), Posture.NIYAT),
                step(t("Qo'llarni yuvish", "Мытьё рук", "Washing hands"),
                    g.pick(
                        t("Ikkala qo'lni bilakkacha uch marta yuving, barmoqlar orasini ham ishqalang. Uzuk bo'lsa, ostiga suv yetkazing.",
                            "Трижды вымойте руки до запястий, промывая между пальцами. Если есть кольцо — пошевелите его.",
                            "Wash both hands up to the wrists three times, between the fingers too. Move any ring so water reaches beneath it."),
                        t("Ikkala qo'lni bilakkacha uch marta yuving, barmoqlar orasini ham ishqalang. Uzuk va bilaguzuklarni qimirlatib, ostiga suv yetkazing; tirnoqdagi lak suv o'tkazmasa, tahorat durust bo'lmaydi.",
                            "Трижды вымойте руки до запястий, промывая между пальцами. Пошевелите кольца и браслеты; лак на ногтях, не пропускающий воду, делает омовение недействительным.",
                            "Wash both hands up to the wrists three times, between the fingers too. Move rings and bracelets; nail polish that blocks water invalidates wudu."),
                    ), Posture.WUDU_HANDS),
                step(t("Og'iz va burun", "Рот и нос", "Mouth and nose"),
                    t("O'ng qo'l bilan og'izga uch marta suv olib chayqang, so'ng burunga uch marta suv tortib, chap qo'l bilan qoqing.",
                        "Трижды прополощите рот правой рукой, затем трижды втяните воду в нос и высморкайте левой рукой.",
                        "Rinse the mouth three times with the right hand, then draw water into the nose three times and blow it out with the left."), Posture.WUDU_MOUTH),
                step(t("Yuzni yuvish (farz)", "Мытьё лица (фард)", "Washing the face (fard)"),
                    g.pick(
                        t("Yuzni peshonadagi soch chizig'idan iyak ostigacha, bir quloqdan ikkinchisigacha uch marta yuving. Soqol quyuq bo'lsa, barmoqlar bilan xilol qiling.",
                            "Трижды вымойте лицо от линии волос до подбородка и от уха до уха. Густую бороду прочешите мокрыми пальцами.",
                            "Wash the face three times from hairline to chin and ear to ear. Run wet fingers through a thick beard."),
                        t("Yuzni peshonadagi soch chizig'idan iyak ostigacha, bir quloqdan ikkinchisigacha uch marta yuving. Pardoz suv o'tishiga to'sqinlik qilsa, avval uni yuvib tashlang.",
                            "Трижды вымойте лицо от линии волос до подбородка и от уха до уха. Косметику, не пропускающую воду, смойте заранее.",
                            "Wash the face three times from hairline to chin and ear to ear. Remove any make-up that blocks water first."),
                    ), Posture.WUDU_FACE),
                step(t("Qo'llarni tirsak bilan (farz)", "Руки с локтями (фард)", "Arms to the elbows (fard)"),
                    t("Avval o'ng, so'ng chap qo'lni tirsaklar bilan birga uch martadan yuving.",
                        "Трижды вымойте правую, затем левую руку вместе с локтями.",
                        "Wash the right then the left arm including the elbows, three times each."), Posture.WUDU_ARMS),
                step(t("Boshga mas'h (farz)", "Масх головы (фард)", "Wiping the head (fard)"),
                    g.pick(
                        t("Ho'l qo'llar bilan boshning kamida to'rtdan biriga (sunnat — hammasiga) mas'h torting, so'ng quloq va bo'yinga mas'h qiling.",
                            "Влажными руками протрите не менее четверти головы (сунна — всю), затем уши и шею.",
                            "With wet hands wipe at least a quarter of the head (sunnah: all of it), then the ears and neck."),
                        t("Ro'molni yechib, ho'l qo'llar bilan to'g'ridan-to'g'ri sochga — boshning kamida to'rtdan biriga mas'h torting; ro'mol ustidan qilingan mas'h durust emas. So'ng quloq va bo'yinga mas'h qiling.",
                            "Сняв платок, влажными руками протрите волосы — не менее четверти головы; масх поверх платка недействителен. Затем протрите уши и шею.",
                            "Remove the scarf and wipe the hair directly with wet hands — at least a quarter of the head; wiping over the scarf is not valid. Then wipe the ears and neck."),
                    ), Posture.WUDU_HEAD),
                step(t("Oyoqlarni yuvish (farz)", "Мытьё ног (фард)", "Washing the feet (fard)"),
                    t("Avval o'ng, so'ng chap oyoqni to'piqlar bilan birga uch martadan yuving, barmoqlar orasini chap qo'lning jimjilog'i bilan tozalang.",
                        "Трижды вымойте правую, затем левую ногу вместе с щиколотками, промывая между пальцами мизинцем левой руки.",
                        "Wash the right then the left foot including the ankles, three times each, cleaning between the toes with the left little finger."), Posture.WUDU_FEET),
                step(t("Tahoratdan keyingi duo", "Дуа после омовения", "Dua after wudu"),
                    t("Tahorat tugagach qiblaga qarab kalimai shahodatni o'qing.",
                        "После омовения, повернувшись к Кибле, произнесите шахаду.",
                        "After wudu, facing the Qibla, recite the testimony of faith."), Posture.NIYAT, recite = wuduDua),
            ),
        ),
        lesson(
            "ghusl", g, t("G'usl va tayammum", "Гусль и таяммум", "Ghusl and tayammum"), t("3 daqiqa", "3 мин", "3 min"), LessonIcon.GHUSL,
            listOfNotNull(
                step(t("Niyat", "Намерение", "Intention"),
                    g.pick(
                        t("Qalbda g'usl (to'liq poklanish) niyatini qiling va \"Bismillah\" deng. Junublikdan so'ng g'usl farz.",
                            "Сделайте намерение гусля и скажите «Бисмиллях». После состояния джанаба гусль обязателен.",
                            "Make the intention for ghusl and say \"Bismillah\". Ghusl is obligatory after janabah."),
                        t("Qalbda g'usl (to'liq poklanish) niyatini qiling va \"Bismillah\" deng. Junublikdan, hayz va nifos tugagandan so'ng g'usl farz.",
                            "Сделайте намерение гусля и скажите «Бисмиллях». Гусль обязателен после джанаба, а также по окончании хайда и нифаса.",
                            "Make the intention for ghusl and say \"Bismillah\". Ghusl is obligatory after janabah and at the end of menstruation (hayd) and postnatal bleeding (nifas)."),
                    ), Posture.NIYAT),
                step(t("Qo'l va avratni yuvish", "Руки и аурат", "Hands and private parts"),
                    t("Qo'llarni yuving, avrat va badandagi najosatni ketkazing.",
                        "Вымойте руки, интимные места и удалите нечистоты с тела.",
                        "Wash the hands and private parts and remove any impurity from the body."), Posture.WUDU_HANDS),
                step(t("Tahorat qilish", "Омовение", "Perform wudu"),
                    t("Namozdagidek to'liq tahorat qiling.",
                        "Совершите полное омовение, как перед намазом.",
                        "Perform a complete wudu as for prayer."), Posture.WUDU_FACE),
                step(t("Butun badanni yuvish (farz)", "Омыть всё тело (фард)", "Wash the whole body (fard)"),
                    g.pick(
                        t("Og'iz va burunni chayqang (Hanafiyda farz), so'ng boshdan oyoqqacha butun badanga uch marta suv quying — quruq joy qolmasin.",
                            "Прополощите рот и нос (у ханафитов — фард), затем трижды облейте всё тело с головы до ног — сухих мест не должно остаться.",
                            "Rinse the mouth and nose (fard for Hanafis), then pour water over the whole body three times — no dry spot may remain."),
                        t("Og'iz va burunni chayqang (Hanafiyda farz), so'ng butun badanga uch marta suv quying. Sochi o'rilgan bo'lsa, suv soch tublariga yetsa o'rimni yechish shart emas; quloq teshiklari va kindikka ham suv yetkazing.",
                            "Прополощите рот и нос (у ханафитов — фард), затем трижды облейте всё тело. Заплетённые косы распускать не обязательно, если вода доходит до корней волос; промойте также проколы в ушах и пупок.",
                            "Rinse the mouth and nose (fard for Hanafis), then pour water over the whole body three times. Braids need not be undone if water reaches the roots; let water reach ear piercings and the navel too."),
                    ), Posture.GHUSL),
                step(t("Tayammum", "Таяммум", "Tayammum"),
                    t("Suv bo'lmasa yoki ishlatib bo'lmasa: niyat qilib, ikki kaftni pok tuproqqa urib yuzga mas'h torting; yana urib qo'llarni tirsakkacha mas'h qiling.",
                        "Если воды нет или её нельзя использовать: сделав намерение, ударьте ладонями о чистую землю и протрите лицо; снова ударьте и протрите руки до локтей.",
                        "If there is no water or it cannot be used: make the intention, strike clean earth with both palms and wipe the face; strike again and wipe the arms to the elbows."), Posture.TAYAMMUM),
            ),
        ),
        if (g == Gender.FEMALE) lesson(
            "hayz", g, t("Hayz va nifos", "Хайд и нифас", "Menstruation and nifas"), t("ayollar uchun", "для женщин", "for women"), LessonIcon.SPECIAL,
            listOf(
                step(t("Hayz (oylik)", "Хайд (менструация)", "Hayd (menstruation)"),
                    t("Hanafiy mazhabida hayz eng kami 3 kun, eng ko'pi 10 kun. Bu kunlarda namoz o'qilmaydi va qazosi ham o'qilmaydi; ro'za tutilmaydi, lekin keyin qazosi tutiladi.",
                        "У ханафитов хайд длится не менее 3 и не более 10 дней. В эти дни намаз не совершается и не восполняется; пост не держится, но потом восполняется.",
                        "In the Hanafi school hayd lasts at least 3 and at most 10 days. During it prayer is not performed and not made up; fasting is not kept but is made up later."), Posture.BOOK, short = t("Hayz", "Хайд", "Hayd")),
                step(t("Nifos (tug'ruqdan keyin)", "Нифас (после родов)", "Nifas (after childbirth)"),
                    t("Tug'ruqdan keyingi qon — nifos; eng ko'pi 40 kun, eng kamiga chegara yo'q. Hukmlari hayz bilan bir xil. Qon 40 kundan oldin to'xtasa, g'usl qilib namozni boshlaysiz.",
                        "Кровотечение после родов — нифас; максимум 40 дней, минимума нет. Правила те же, что при хайде. Если кровь прекратилась раньше 40 дней — совершите гусль и начните молиться.",
                        "Bleeding after childbirth is nifas; at most 40 days, with no minimum. Its rules are the same as hayd. If it stops before 40 days, perform ghusl and resume praying."), Posture.BOOK, short = t("Nifos", "Нифас", "Nifas")),
                step(t("Bu kunlarda nima mumkin emas", "Что запрещено в эти дни", "What is not allowed"),
                    t("Namoz, ro'za, Ka'bani tavof qilish, masjidga kirish, Qur'onni ushlash va o'qish mumkin emas. Zikr, duo, salovot aytish va Qur'onni tinglash mumkin.",
                        "Нельзя совершать намаз, держать пост, обходить Каабу, входить в мечеть, касаться и читать Коран. Зикр, дуа, салават и слушание Корана разрешены.",
                        "Prayer, fasting, circling the Kaaba, entering the mosque, touching and reciting the Quran are not allowed. Dhikr, dua, salawat and listening to the Quran are allowed."), Posture.BOOK, short = t("Taqiqlar", "Запреты", "Rules")),
                step(t("Poklanish", "Очищение", "Becoming pure"),
                    t("Qon to'xtagach g'usl qiling. Agar namoz vaqti chiqishidan oldin g'usl qilib \"Allohu akbar\" deyishga ulgurish mumkin bo'lsa, o'sha vaqt namozi farz bo'ladi va o'qiladi (yoki qazosi o'qiladi).",
                        "Когда кровь прекратилась, совершите гусль. Если до конца времени намаза остаётся время на гусль и начальный такбир, этот намаз становится обязательным.",
                        "When bleeding stops, perform ghusl. If there is enough time before the prayer time ends to bathe and say the opening takbir, that prayer becomes due."), Posture.GHUSL, short = t("G'usl", "Гусль", "Ghusl")),
                step(t("Istihoza", "Истихада", "Istihadah"),
                    t("3 kundan kam yoki 10 kundan ortiq (nifosda 40 kundan ortiq) kelgan qon kasallik qoni — istihoza. Bunda namoz va ro'za tark etilmaydi: har namoz vaqti kirganda yangi tahorat qilib, o'sha vaqt namozlarini o'qiysiz.",
                        "Кровь менее 3 или более 10 дней (при нифасе — более 40) — истихада, болезненное кровотечение. Намаз и пост не оставляются: с наступлением каждого времени намаза совершайте новое омовение.",
                        "Bleeding of less than 3 or more than 10 days (more than 40 for nifas) is istihadah. Prayer and fasting are not left: make a fresh wudu when each prayer time begins and pray."), Posture.WUDU_HANDS, short = t("Istihoza", "Истихада", "Istihadah")),
            ),
        ) else null,
        lesson(
            "two_rakat", g, t("Ikki rakatli namoz", "Намаз в два ракаата", "Two-rakat prayer"), t("Bomdod farzi misolida", "на примере фаджра", "Fajr fard"), LessonIcon.TWO,
            opening(g, "bomdod", "фаджр", "Fajr", 2) + listOf(secondRakat(), finalSitting(g, 2), salamStep),
        ),
        lesson(
            "four_rakat", g, t("To'rt rakatli namoz", "Намаз в четыре ракаата", "Four-rakat prayer"), t("Peshin, Asr, Xufton", "Зухр, Аср, Иша", "Dhuhr, Asr, Isha"), LessonIcon.FOUR,
            opening(g, "peshin", "зухр", "Dhuhr", 4) + listOf(
                secondRakat(),
                firstSitting(g),
                step(t("3- va 4-rakatlar", "3-й и 4-й ракааты", "Rakats 3 and 4"),
                    t("Farzning oxirgi ikki rakatida faqat Fotiha o'qiladi (sura qo'shilmaydi), so'ng ruku' va sajdalar.",
                        "В последних двух ракаатах фарда читается только Фатиха, затем руку и поклоны.",
                        "In the last two rakats of fard recite only al-Fatiha, then ruku and prostrations."), Posture.QIYAM, 3, fatiha, t("3-4-rakat", "3-4 ракаат", "Rakats 3-4")),
                finalSitting(g, 4),
                salamStep,
            ),
        ),
        lesson(
            "duas", g, t("Namoz duolari va suralari", "Молитвы и суры намаза", "Prayer recitations"), t("Sano, Fotiha, Tashahhud", "Сана, Фатиха, Ташаххуд", "Thana, Fatiha, Tashahhud"), LessonIcon.DUAS,
            listOf(
                step(t("Sano", "Сана", "Thana"), t("Iftitoh takbiridan keyin o'qiladi.", "Читается после начального такбира.", "Recited after the opening takbir."), Posture.BOOK, recite = sano),
                step(t("A'uzu va Bismillah", "Аузу и Бисмиллях", "Ta'awwudh and Basmala"), t("Sanodan keyin, Fotihadan oldin ichda o'qiladi.", "Читается про себя после Сана, перед Фатихой.", "Recited silently after the Thana, before al-Fatiha."), Posture.BOOK, recite = auzu),
                step(t("Fotiha surasi", "Сура аль-Фатиха", "Surah al-Fatiha"), t("Har bir rakatda o'qiladi.", "Читается в каждом ракаате.", "Recited in every rakat."), Posture.BOOK, recite = fatiha),
                step(t("Ixlos surasi", "Сура аль-Ихлас", "Surah al-Ikhlas"), t("Fotihadan keyin qo'shiladigan qisqa sura.", "Короткая сура после Фатихи.", "A short surah after al-Fatiha."), Posture.BOOK, recite = ikhlas),
                step(t("Tashahhud (Attahiyyot)", "Ташаххуд", "Tashahhud"), t("Har bir qa'dada o'qiladi.", "Читается в каждом сидении.", "Recited in every sitting."), Posture.BOOK, recite = tashahhud),
                step(t("Salovot", "Салават", "Salawat"), t("Oxirgi qa'dada Attahiyyotdan keyin.", "В последнем сидении после ташаххуда.", "In the final sitting after the Tashahhud."), Posture.BOOK, recite = salawat),
                step(t("Duo", "Дуа", "Supplication"), t("Salovotdan keyin salomdan oldin.", "После салавата перед салямом.", "After the Salawat, before the salam."), Posture.BOOK, recite = rabbana),
            ),
        ),
        lesson(
            "special", g, t("Vitr, Juma va hayit namozlari", "Витр, Джума и праздничные намазы", "Witr, Jumu'ah and Eid"), null, LessonIcon.SPECIAL,
            listOf(
                step(t("Vitr namozi (vojib)", "Витр (ваджиб)", "Witr (wajib)"),
                    t("Xuftondan keyin uch rakat o'qiladi. Uchinchi rakatda Fotiha va suradan so'ng \"Allohu akbar\" deb qo'llarni ko'tarib, qayta bog'lang va Qunut duosini o'qing, so'ng ruku'ga boring." + if (g == Gender.FEMALE) " Ayollar qo'lni yelka baravarida ko'taradi." else "",
                        "Три ракаата после иши. В третьем ракаате после Фатихи и суры поднимите руки с такбиром, снова сложите их и прочитайте дуа Кунут, затем совершите руку." + if (g == Gender.FEMALE) " Женщины поднимают руки до плеч." else "",
                        "Three rakats after Isha. In the third rakat, after al-Fatiha and a surah, raise the hands with takbir, fold them again and recite the Qunut, then go into ruku." + if (g == Gender.FEMALE) " Women raise the hands to the shoulders." else ""),
                    Posture.QUNUT, 3, qunut, t("Vitr", "Витр", "Witr")),
                step(t("Juma namozi", "Джума-намаз", "Jumu'ah prayer"),
                    g.pick(
                        t("Juma kuni peshin o'rniga masjidda jamoat bilan o'qiladi va balog'atga yetgan, muqim, sog' erkaklarga farz: 4 rakat sunnat, xutba, imom bilan 2 rakat farz, so'ng 4 rakat sunnat.",
                            "В пятницу вместо зухра в мечети с джамаатом; обязателен для взрослых, оседлых, здоровых мужчин: 4 ракаата сунны, хутба, 2 ракаата фарда с имамом, затем 4 сунны.",
                            "On Friday in place of Dhuhr, in congregation at the mosque; obligatory for adult, resident, healthy men: 4 sunnah, the khutbah, 2 fard with the imam, then 4 sunnah."),
                        t("Juma namozi ayollarga farz emas: juma kuni uyingizda odatdagidek peshin namozini (4 rakat sunnat, 4 farz, 2 sunnat) o'qiysiz. Masjidda imom bilan juma o'qisangiz, peshin o'rniga o'tadi.",
                            "Джума не обязательна для женщин: в пятницу совершите дома обычный зухр (4 сунны, 4 фарда, 2 сунны). Если вы помолились джуму с имамом в мечети, она заменяет зухр.",
                            "Jumu'ah is not obligatory for women: on Friday pray the usual Dhuhr at home (4 sunnah, 4 fard, 2 sunnah). If you pray Jumu'ah with the imam at the mosque, it replaces Dhuhr."),
                    ), Posture.QIYAM, null, null, t("Juma", "Джума", "Jumu'ah")),
                step(t("Hayit namozi", "Праздничный намаз", "Eid prayer"),
                    g.pick(
                        t("Ramazon va Qurbon hayitlarida quyosh ko'tarilgach jamoat bilan 2 rakat vojib namoz. Hanafiyda har rakatda uchtadan qo'shimcha takbir aytiladi, so'ng xutba tinglanadi.",
                            "Два ракаата ваджиб с джамаатом после восхода солнца в Ураза- и Курбан-байрам; у ханафитов по три дополнительных такбира в каждом ракаате, затем хутба.",
                            "Two wajib rakats in congregation after sunrise on both Eids; Hanafis add three extra takbirs in each rakat, followed by the khutbah."),
                        t("Hanafiy mazhabida hayit namozi ayollarga vojib emas. Hayit kuni uyda nafl namoz o'qish, takbir aytish, g'usl qilish va yangi kiyim kiyish sunnat.",
                            "У ханафитов праздничный намаз не обязателен для женщин. В день праздника сунна — совершить гусль, надеть лучшую одежду, произносить такбир и молиться нафль дома.",
                            "In the Hanafi school the Eid prayer is not wajib for women. On Eid it is sunnah to bathe, wear one's best clothes, say the takbir and pray voluntary prayers at home."),
                    ), Posture.TAKBIR, null, takbir, t("Hayit", "Байрам", "Eid")),
            ),
        ),
    )
}
