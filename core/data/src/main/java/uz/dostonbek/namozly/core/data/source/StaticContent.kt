package uz.dostonbek.namozly.core.data.source

import uz.dostonbek.namozly.core.model.Dhikr
import uz.dostonbek.namozly.core.model.Dua
import uz.dostonbek.namozly.core.model.DuaCategory
import uz.dostonbek.namozly.core.model.GeoPoint
import uz.dostonbek.namozly.core.model.IslamicEvent
import uz.dostonbek.namozly.core.model.LiveChannel
import uz.dostonbek.namozly.core.model.Mosque
import uz.dostonbek.namozly.core.model.uzText

internal object DuasData {
    val all: List<Dua> = listOf(
        Dua(
            id = "suhoor_niyat", category = DuaCategory.RAMADAN,
            title = uzText("Saharlik (ro'za tutish) niyati", "Намерение поста (сухур)", "Intention of fasting (Suhoor)"),
            arabic = "نَوَيْتُ أَنْ أَصُومَ صَوْمَ شَهْرِ رَمَضَانَ مِنَ الْفَجْرِ إِلَى الْمَغْرِبِ خَالِصًا لِلَّهِ تَعَالَى",
            transliteration = "Navaytu an asuvma sovma shahri ramazona minal fajri ilal mag'ribi, xolisan lillahi ta'aalaa.",
            translation = uzText(
                "Ramazon oyining ro'zasini subhdan to kun botguncha, xolis Alloh uchun tutishni niyat qildim.",
                "Я намерился держать пост месяца Рамадан от рассвета до заката искренне ради Аллаха.",
                "I intend to fast the month of Ramadan from dawn until sunset, sincerely for the sake of Allah.",
            ),
            source = "Hanafiy mazhabida niyat qalb bilan bo'ladi; tilda aytish mustahab",
        ),
        Dua(
            id = "iftar", category = DuaCategory.RAMADAN,
            title = uzText("Iftorlik duosi", "Дуа при разговении", "Dua for breaking the fast"),
            arabic = "اللَّهُمَّ لَكَ صُمْتُ وَبِكَ آمَنْتُ وَعَلَيْكَ تَوَكَّلْتُ وَعَلَى رِزْقِكَ أَفْطَرْتُ",
            transliteration = "Allohumma laka sumtu va bika amantu va a'layka tavakkaltu va a'laa rizqika aftartu.",
            translation = uzText(
                "Allohim, Sen uchun ro'za tutdim, Senga iymon keltirdim, Senga tavakkal qildim va bergan rizqing bilan iftor qildim.",
                "О Аллах, ради Тебя я постился, в Тебя уверовал, на Тебя уповаю и Твоим уделом разговляюсь.",
                "O Allah, for You I fasted, in You I believe, upon You I rely, and with Your provision I break my fast.",
            ),
            source = "Abu Dovud (qisqa shakli: \"Allohumma laka sumtu va a'laa rizqika aftartu\")",
        ),
        Dua(
            id = "after_iftar", category = DuaCategory.RAMADAN,
            title = uzText("Iftordan keyin", "После разговения", "After breaking the fast"),
            arabic = "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ وَثَبَتَ الْأَجْرُ إِنْ شَاءَ اللَّهُ",
            transliteration = "Zahabaz zoma'u vabtallatil 'uruqu va sabatal ajru in shaa Alloh.",
            translation = uzText(
                "Chanqoq ketdi, tomirlar namlandi va inshaalloh, ajr sobit bo'ldi.",
                "Ушла жажда, увлажнились жилы, и утвердилась награда, если пожелает Аллах.",
                "Thirst is gone, the veins are moistened, and the reward is assured, if Allah wills.",
            ),
            source = "Abu Dovud, 2357",
        ),
        Dua(
            id = "after_salah", category = DuaCategory.PRAYER,
            title = uzText("Namozdan keyin", "После намаза", "After prayer"),
            arabic = "اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ",
            transliteration = "Allohumma antas salaamu va minkas salaamu, tabaarokta yaa zal jalaali val ikroom.",
            translation = uzText(
                "Allohim, Sen Salomsan, salomatlik Sendandir. Ey ulug'lik va ikrom egasi, Sen barakotlisan.",
                "О Аллах, Ты — Мир, и от Тебя мир. Благословен Ты, о Обладатель величия и щедрости.",
                "O Allah, You are Peace and from You is peace. Blessed are You, O Owner of majesty and honour.",
            ),
            source = "Muslim, 591",
        ),
        Dua(
            id = "sayyidul_istighfar", category = DuaCategory.MORNING_EVENING,
            title = uzText("Sayyidul istig'for", "Лучшая мольба о прощении", "Master of seeking forgiveness"),
            arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ خَلَقْتَنِي وَأَنَا عَبْدُكَ وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            transliteration = "Allohumma anta robbii laa ilaaha illaa anta, xolaqtanii va ana 'abduka, va ana 'alaa 'ahdika va va'dika mastato'tu, a'uuzu bika min sharri maa sona'tu, abuu'u laka bini'matika 'alayya, va abuu'u bizanbii, fag'firlii, fa innahuu laa yag'firuz zunuuba illaa anta.",
            translation = uzText(
                "Allohim, Sen mening Robbimsan, Sendan o'zga iloh yo'q. Meni yaratding, men Sening bandangman, qodir bo'lganimcha Senga bergan ahd va va'damdaman. Qilgan yomonligimning sharridan Sendan panoh so'rayman. Menga bergan ne'matlaringni e'tirof etaman, gunohimni ham e'tirof etaman. Meni mag'firat qil, chunki gunohlarni Sendan boshqa hech kim mag'firat qilmaydi.",
                "О Аллах, Ты — мой Господь, нет божества, кроме Тебя. Ты сотворил меня, а я — Твой раб, и я буду верен договору с Тобой, насколько смогу. Прибегаю к Тебе от зла того, что я сделал, признаю милость Твою ко мне и признаю свой грех. Прости же меня, ибо никто не прощает грехи, кроме Тебя.",
                "O Allah, You are my Lord, there is no god but You. You created me and I am Your servant, and I keep Your covenant and promise as best I can. I seek refuge in You from the evil I have done. I acknowledge Your favour upon me and I acknowledge my sin, so forgive me, for none forgives sins but You.",
            ),
            source = "Buxoriy, 6306",
        ),
        Dua(
            id = "before_sleep", category = DuaCategory.SLEEP,
            title = uzText("Uxlashdan oldin", "Перед сном", "Before sleeping"),
            arabic = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
            transliteration = "Bismika Allohumma amuutu va ahyaa.",
            translation = uzText("Allohim, Sening isming bilan o'laman va tirilaman.", "С Твоим именем, о Аллах, я умираю и оживаю.", "In Your name, O Allah, I die and I live."),
            source = "Buxoriy, 6324",
        ),
        Dua(
            id = "wake_up", category = DuaCategory.SLEEP,
            title = uzText("Uyg'onganda", "При пробуждении", "Upon waking up"),
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
            transliteration = "Alhamdu lillahil-lazii ahyaanaa ba'da maa amaatanaa va ilayhin nushuur.",
            translation = uzText(
                "Bizni o'ldirganidan keyin tiriltirgan Allohga hamd bo'lsin. Qaytish Uning huzuriga.",
                "Хвала Аллаху, Который оживил нас после того, как умертвил, и к Нему воскрешение.",
                "Praise be to Allah who gave us life after causing us to die, and to Him is the resurrection.",
            ),
            source = "Buxoriy, 6312",
        ),
        Dua(
            id = "before_food", category = DuaCategory.FOOD,
            title = uzText("Taomdan oldin", "Перед едой", "Before eating"),
            arabic = "بِسْمِ اللَّهِ",
            transliteration = "Bismillah. (Unutilsa: Bismillahi avvalahu va oxirahu)",
            translation = uzText("Alloh nomi bilan.", "С именем Аллаха.", "In the name of Allah."),
            source = "Abu Dovud, 3767; Termiziy, 1858",
        ),
        Dua(
            id = "after_food", category = DuaCategory.FOOD,
            title = uzText("Taomdan keyin", "После еды", "After eating"),
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنِي هَذَا وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ",
            transliteration = "Alhamdu lillahil-lazii at'amanii haazaa va rozaqoniihi min g'oyri havlin minnii va laa quvvah.",
            translation = uzText(
                "Menga bu taomni yedirgan va mening kuch-quvvatimsiz rizq qilib bergan Allohga hamd bo'lsin.",
                "Хвала Аллаху, Который накормил меня этим и наделил этим без моей силы и мощи.",
                "Praise be to Allah who fed me this and provided it for me without any might or power on my part.",
            ),
            source = "Abu Dovud, 4023; Termiziy, 3458",
        ),
        Dua(
            id = "enter_mosque", category = DuaCategory.MOSQUE,
            title = uzText("Masjidga kirishda", "При входе в мечеть", "Entering the mosque"),
            arabic = "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
            transliteration = "Allohummaftah lii abvaaba rohmatik.",
            translation = uzText("Allohim, menga rahmating eshiklarini och.", "О Аллах, открой для меня врата Твоей милости.", "O Allah, open for me the gates of Your mercy."),
            source = "Muslim, 713",
        ),
        Dua(
            id = "leave_mosque", category = DuaCategory.MOSQUE,
            title = uzText("Masjiddan chiqishda", "При выходе из мечети", "Leaving the mosque"),
            arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ",
            transliteration = "Allohumma innii as'aluka min fazlik.",
            translation = uzText("Allohim, men Sendan fazlingni so'rayman.", "О Аллах, я прошу Тебя о Твоей милости.", "O Allah, I ask You of Your bounty."),
            source = "Muslim, 713",
        ),
        Dua(
            id = "leave_home", category = DuaCategory.HOME,
            title = uzText("Uydan chiqishda", "При выходе из дома", "Leaving home"),
            arabic = "بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            transliteration = "Bismillahi tavakkaltu 'alallohi va laa havla va laa quvvata illaa billah.",
            translation = uzText(
                "Alloh nomi bilan, Allohga tavakkal qildim. Kuch-quvvat faqat Alloh bilandir.",
                "С именем Аллаха, уповаю на Аллаха, нет мощи и силы, кроме как у Аллаха.",
                "In the name of Allah, I rely on Allah; there is no might nor power except with Allah.",
            ),
            source = "Abu Dovud, 5095; Termiziy, 3426",
        ),
        Dua(
            id = "travel", category = DuaCategory.TRAVEL,
            title = uzText("Safar duosi", "Дуа путешествия", "Travel supplication"),
            arabic = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ",
            transliteration = "Subhaanal-lazii saxxoro lanaa haazaa va maa kunnaa lahuu muqriniin, va innaa ilaa robbinaa lamunqolibuun.",
            translation = uzText(
                "Buni bizga bo'ysundirib qo'ygan Zot pokdir, biz bunga qodir emas edik. Albatta, biz Robbimizga qaytuvchimiz.",
                "Пречист Тот, Кто подчинил нам это, ведь мы не были способны на это, и, воистину, мы вернёмся к нашему Господу.",
                "Glory be to Him who has subjected this to us, for we could never have done it ourselves, and to our Lord we shall return.",
            ),
            source = "Zuxruf, 13–14; Muslim, 1342",
        ),
        Dua(
            id = "rabbana_atina", category = DuaCategory.GENERAL,
            title = uzText("Robbanaa aatinaa", "Господь наш, даруй нам", "Our Lord, give us"),
            arabic = "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
            transliteration = "Robbanaa aatinaa fid-dunyaa hasanatan va fil-aaxiroti hasanatan va qinaa 'azaaban-naar.",
            translation = uzText(
                "Robbimiz, bizga bu dunyoda ham yaxshilik, oxiratda ham yaxshilik ber va bizni do'zax azobidan saqla.",
                "Господь наш, даруй нам благо в этом мире и благо в Последней жизни и защити нас от мучений Огня.",
                "Our Lord, give us good in this world and good in the Hereafter, and protect us from the punishment of the Fire.",
            ),
            source = "Baqara, 201",
        ),
    )
}

internal object DhikrData {
    val all: List<Dhikr> = listOf(
        Dhikr("subhanallah", "سُبْحَانَ اللَّهِ", uzText("Subhanalloh", "Субханаллах", "SubhanAllah"), uzText("Alloh barcha nuqsonlardan pokdir", "Пречист Аллах", "Glory be to Allah"), 33),
        Dhikr("alhamdulillah", "الْحَمْدُ لِلَّهِ", uzText("Alhamdulillah", "Альхамдулиллях", "Alhamdulillah"), uzText("Allohga hamd bo'lsin", "Хвала Аллаху", "Praise be to Allah"), 33),
        Dhikr("allahuakbar", "اللَّهُ أَكْبَرُ", uzText("Allohu akbar", "Аллаху акбар", "Allahu Akbar"), uzText("Alloh buyukdir", "Аллах велик", "Allah is the Greatest"), 34),
        Dhikr("tahlil", "لَا إِلَهَ إِلَّا اللَّهُ", uzText("La ilaha illalloh", "Ля иляха илляллах", "La ilaha illallah"), uzText("Allohdan o'zga iloh yo'q", "Нет божества, кроме Аллаха", "There is no god but Allah"), 100),
        Dhikr("istighfar", "أَسْتَغْفِرُ اللَّهَ", uzText("Astag'firulloh", "Астагфируллах", "Astaghfirullah"), uzText("Allohdan mag'firat so'rayman", "Прошу прощения у Аллаха", "I seek forgiveness from Allah"), 100),
        Dhikr("salawat", "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ", uzText("Salovot", "Салават", "Salawat"), uzText("Allohim, Muhammadga salovot yo'lla", "О Аллах, благослови Мухаммада", "O Allah, send blessings upon Muhammad"), 100),
        Dhikr("hawqala", "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ", uzText("La havla va la quvvata illa billah", "Ля хауля ва ля куввата илля биллях", "La hawla wa la quwwata illa billah"), uzText("Kuch-quvvat faqat Alloh bilandir", "Нет мощи и силы, кроме как у Аллаха", "No might nor power except with Allah"), 33),
    )
}

internal object EventsData {
    val all = listOf(
        IslamicEvent(uzText("Hijriy yangi yil", "Исламский Новый год", "Islamic New Year"), 1, 1),
        IslamicEvent(uzText("Ashuro kuni", "День Ашура", "Day of Ashura"), 1, 10),
        IslamicEvent(uzText("Mavlid", "Мавлид", "Mawlid"), 3, 12),
        IslamicEvent(uzText("Me'roj kechasi", "Ночь Мирадж", "Isra and Mi'raj"), 7, 27),
        IslamicEvent(uzText("Barot kechasi", "Ночь Бараат", "Mid-Sha'ban night"), 8, 15),
        IslamicEvent(uzText("Ramazon boshlanishi", "Начало Рамадана", "Start of Ramadan"), 9, 1),
        IslamicEvent(uzText("Qadr kechasi (taxminiy)", "Ночь Предопределения (ориент.)", "Laylat al-Qadr (approx.)"), 9, 27),
        IslamicEvent(uzText("Ramazon hayiti", "Ураза-байрам", "Eid al-Fitr"), 10, 1),
        IslamicEvent(uzText("Arafa kuni", "День Арафа", "Day of Arafah"), 12, 9),
        IslamicEvent(uzText("Qurbon hayiti", "Курбан-байрам", "Eid al-Adha"), 12, 10),
    )
}

/**
 * Jonli efirlar. Makka va Madina — ilova ichidagi pleyerda HLS oqim (streams). Oqimlar ishlamasa
 * YouTube zaxira: youtubeChannelId ("UC...") yoki qidiruv.
 */
internal object LiveChannelsData {
    val all = listOf(
        LiveChannel(
            id = "haram",
            name = uzText("Masjidul Harom", "Мечеть аль-Харам", "Masjid al-Haram"),
            city = uzText("Makka", "Мекка", "Makkah"),
            youtubeChannelId = "",
            searchQuery = "Makkah live Masjid al-Haram",
            // Saudiya teleradio korporatsiyasi (SBA) — "Al-Qur'on al-Karim" kanali, Masjidul Haromdan jonli.
            streams = listOf(
                "https://win.holol.com/live/quran/playlist.m3u8",
                "https://cdn-globecast.akamaized.net/live/eds/saudi_quran/hls_roku/index.m3u8",
                "http://m.live.net.sa:1935/live/quran/playlist.m3u8",
            ),
        ),
        LiveChannel(
            id = "nabawi",
            name = uzText("Masjidun Nabaviy", "Мечеть Пророка", "Masjid an-Nabawi"),
            city = uzText("Madina", "Медина", "Madinah"),
            youtubeChannelId = "",
            searchQuery = "Madinah live Masjid an-Nabawi",
            // SBA — "As-Sunna an-Nabaviya" kanali, Masjidun Nabaviydan jonli.
            streams = listOf(
                "https://win.holol.com/live/sunnah/playlist.m3u8",
                "https://cdn-globecast.akamaized.net/live/eds/saudi_sunnah/hls_roku/index.m3u8",
                "http://m.live.net.sa:1935/live/sunnah/playlist.m3u8",
            ),
        ),
        LiveChannel(
            id = "aqsa",
            name = uzText("Masjidul Aqso", "Мечеть аль-Акса", "Masjid al-Aqsa"),
            city = uzText("Quddus", "Иерусалим", "Jerusalem"),
            youtubeChannelId = "",
            searchQuery = "Masjid al-Aqsa live",
        ),
    )
}

/** Dunyoning mashhur masjidlari. wikiTitle — inglizcha Wikipedia sahifasi (rasm uchun). */
internal object WorldMosquesData {
    data class Item(val id: Long, val name: String, val city: String, val country: String, val point: GeoPoint, val wikiTitle: String)

    val all = listOf(
        Item(1, "Masjidul Harom", "Makka", "Saudiya Arabistoni", GeoPoint(21.4225, 39.8262), "Great_Mosque_of_Mecca"),
        Item(2, "Masjidun Nabaviy", "Madina", "Saudiya Arabistoni", GeoPoint(24.4672, 39.6111), "Prophet's_Mosque"),
        Item(3, "Quba masjidi", "Madina", "Saudiya Arabistoni", GeoPoint(24.4393, 39.6173), "Quba_Mosque"),
        Item(4, "Masjidul Aqso", "Quddus", "Falastin", GeoPoint(31.7761, 35.2358), "Al-Aqsa_Mosque"),
        Item(5, "Hazrati Imom majmuasi", "Toshkent", "O'zbekiston", GeoPoint(41.3386, 69.2405), "Hazrati_Imam_Complex"),
        Item(6, "Minor masjidi", "Toshkent", "O'zbekiston", GeoPoint(41.3264, 69.2878), "Minor_Mosque"),
        Item(7, "Kalon masjidi", "Buxoro", "O'zbekiston", GeoPoint(39.7759, 64.4153), "Po-i-Kalyan"),
        Item(8, "Bibixonim masjidi", "Samarqand", "O'zbekiston", GeoPoint(39.6608, 66.9796), "Bibi-Khanym_Mosque"),
        Item(9, "Sulton Ahmad masjidi", "Istanbul", "Turkiya", GeoPoint(41.0054, 28.9768), "Blue_Mosque"),
        Item(19, "Sulaymoniya masjidi", "Istanbul", "Turkiya", GeoPoint(41.0162, 28.9639), "Süleymaniye_Mosque"),
        Item(20, "Salimiya masjidi", "Edirne", "Turkiya", GeoPoint(41.6781, 26.5594), "Selimiye_Mosque"),
        Item(10, "Shayx Zoid masjidi", "Abu-Dabi", "BAA", GeoPoint(24.4128, 54.4750), "Sheikh_Zayed_Grand_Mosque"),
        Item(11, "Hasan II masjidi", "Kasablanka", "Marokash", GeoPoint(33.6083, -7.6328), "Hassan_II_Mosque"),
        Item(12, "Faysal masjidi", "Islomobod", "Pokiston", GeoPoint(33.7299, 73.0372), "Faisal_Mosque"),
        Item(13, "Podshohiy masjid", "Lahor", "Pokiston", GeoPoint(31.5879, 74.3096), "Badshahi_Mosque"),
        Item(14, "Al-Azhar masjidi", "Qohira", "Misr", GeoPoint(30.0457, 31.2627), "Al-Azhar_Mosque"),
        Item(21, "Muhammad Ali masjidi", "Qohira", "Misr", GeoPoint(30.0287, 31.2599), "Mosque_of_Muhammad_Ali"),
        Item(15, "Umaviylar masjidi", "Damashq", "Suriya", GeoPoint(33.5116, 36.3066), "Umayyad_Mosque"),
        Item(16, "Istiqlol masjidi", "Jakarta", "Indoneziya", GeoPoint(-6.1702, 106.8314), "Istiqlal_Mosque,_Jakarta"),
        Item(17, "Qul Sharif masjidi", "Qozon", "Rossiya", GeoPoint(55.7984, 49.1052), "Qolşärif_Mosque"),
        Item(18, "Moskva Jome masjidi", "Moskva", "Rossiya", GeoPoint(55.7792, 37.6264), "Moscow_Cathedral_Mosque"),
        Item(22, "Imom masjidi", "Isfahon", "Eron", GeoPoint(32.6546, 51.6777), "Shah_Mosque"),
        Item(23, "Jome masjidi", "Dehli", "Hindiston", GeoPoint(28.6507, 77.2334), "Jama_Masjid,_Delhi"),
        Item(24, "Putra masjidi", "Putrajaya", "Malayziya", GeoPoint(2.9360, 101.6895), "Putra_Mosque"),
    )

    fun toMosque(item: Item, photoUrl: String?) = Mosque(
        id = -item.id, name = item.name, point = item.point, photoUrl = photoUrl,
        city = item.city, country = item.country,
    )
}
