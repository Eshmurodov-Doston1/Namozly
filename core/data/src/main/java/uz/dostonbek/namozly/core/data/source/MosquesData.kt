package uz.dostonbek.namozly.core.data.source

import uz.dostonbek.namozly.core.model.GeoPoint
import uz.dostonbek.namozly.core.model.Mosque

/**
 * O'zbekiston va dunyo bo'ylab masjidlarning to'liq oflayn katalogi.
 * Koordinatalar, shahar, davlat va Wikipedia ma'lumotlari bilan.
 */
object MosquesData {

    data class Item(
        val id: Long,
        val name: String,
        val city: String,
        val country: String,
        val point: GeoPoint,
        val address: String? = null,
        val wikiTitle: String? = null,
        val photoUrl: String? = null,
    ) {
        fun toMosque(calculatedDistance: Double? = null) = Mosque(
            id = id,
            name = name,
            point = point,
            distanceMeters = calculatedDistance,
            address = address,
            city = city,
            country = country,
            photoUrl = photoUrl ?: wikiTitle?.let { "https://en.wikipedia.org/wiki/Special:FilePath/${it.replace(' ', '_')}?width=600" },
        )
    }

    // ==========================================
    // 1. O'ZBEKISTON MASJIDLARI (14 ta hudud)
    // ==========================================
    val uzbekistan: List<Item> = listOf(
        // --- Toshkent shahri ---
        Item(1001, "Hazrati Imom (Xasti Imom) majmuasi", "Toshkent", "O'zbekiston", GeoPoint(41.3386, 69.2405), "Qorasaroy ko'chasi, Olmazor", "Hazrati_Imam_Complex"),
        Item(1002, "Minor masjidi", "Toshkent", "O'zbekiston", GeoPoint(41.3264, 69.2878), "Kichik halqa yo'li, Yunusobod", "Minor_Mosque"),
        Item(1003, "Shayx Zayniddin (Ko'kcha) masjidi", "Toshkent", "O'zbekiston", GeoPoint(41.3248, 69.2135), "Mannop Uyg'ur ko'chasi, Shayxontohur", "Kukcha_Mosque"),
        Item(1004, "Novza masjidi", "Toshkent", "O'zbekiston", GeoPoint(41.2858, 69.2272), "Muqimiy ko'chasi, Chilonzor"),
        Item(1005, "Islom Ota masjidi", "Toshkent", "O'zbekiston", GeoPoint(41.2934, 69.3015), "Farg'ona yo'li ko'chasi, Yashnobod"),
        Item(1006, "Suzuk Ota majmuasi", "Toshkent", "O'zbekiston", GeoPoint(41.3142, 69.2311), "Suzuk ota mahallasi, Shayxontohur"),
        Item(1007, "Xo'ja Ahror Valiy (Jome) masjidi", "Toshkent", "O'zbekiston", GeoPoint(41.3235, 69.2361), "Chorsu maydoni, Shayxontohur"),
        Item(1008, "Mirzo Ulug'bek masjidi", "Toshkent", "O'zbekiston", GeoPoint(41.3418, 69.3402), "Oliy Himmat mahallasi, Mirzo Ulug'bek"),
        Item(1009, "Do'mbrobod masjidi", "Toshkent", "O'zbekiston", GeoPoint(41.2589, 69.2064), "Do'mbrobod ko'chasi, Chilonzor"),
        Item(1010, "To'xtaboy masjidi", "Toshkent", "O'zbekiston", GeoPoint(41.3492, 69.2238), "Farobiy ko'chasi, Olmazor"),
        Item(1011, "Cho'pon Ota masjidi", "Toshkent", "O'zbekiston", GeoPoint(41.2905, 69.1764), "Cho'pon ota ko'chasi, Uchtepa"),
        Item(1012, "Bo'rijar masjidi", "Toshkent", "O'zbekiston", GeoPoint(41.2721, 69.2415), "Shohjahon ko'chasi, Yakkasaroy"),
        Item(1013, "Usmon bin Maz'un masjidi", "Toshkent", "O'zbekiston", GeoPoint(41.2612, 69.2541), "Kichik halqa yo'li, Yakkasaroy"),
        Item(1014, "Rakat masjidi", "Toshkent", "O'zbekiston", GeoPoint(41.2891, 69.2562), "Rakatboshi ko'chasi, Yakkasaroy"),
        Item(1015, "Ko'zirobod masjidi", "Toshkent", "O'zbekiston", GeoPoint(41.2687, 69.2231), "Chilonzor 2-mavze"),
        Item(1016, "Qatortol masjidi", "Toshkent", "O'zbekiston", GeoPoint(41.2774, 69.2119), "Qatortol ko'chasi, Chilonzor"),
        Item(1017, "Pul yemas ota masjidi", "Toshkent", "O'zbekiston", GeoPoint(41.2854, 69.1712), "Beshqayrag'och ko'chasi, Uchtepa"),
        Item(1018, "Hazrati Ali masjidi", "Toshkent", "O'zbekiston", GeoPoint(41.2982, 69.1824), "Istiqlol ko'chasi, Uchtepa"),
        Item(1019, "Imom Termiziy masjidi", "Toshkent", "O'zbekiston", GeoPoint(41.3592, 69.2861), "Yunusobod 13-mavze"),
        Item(1020, "Kulol Qo'rg'on masjidi", "Toshkent", "O'zbekiston", GeoPoint(41.3812, 69.2945), "Yunusobod 19-mavze"),
        Item(1021, "Mirzo Yusuf (Bodomzor) masjidi", "Toshkent", "O'zbekiston", GeoPoint(41.3392, 69.2789), "Bodomzor yo'li, Yunusobod"),
        Item(1022, "Otamozor masjidi", "Toshkent", "O'zbekiston", GeoPoint(41.3571, 69.2412), "Qorasaroy ko'chasi, Olmazor"),
        Item(1023, "Imom Buxoriy masjidi", "Toshkent", "O'zbekiston", GeoPoint(41.3642, 69.2289), "G'alaba mahallasi, Olmazor"),
        Item(1024, "Siroj Solih masjidi", "Toshkent", "O'zbekiston", GeoPoint(41.2892, 69.2812), "Mirobod ko'chasi"),
        Item(1025, "Qo'yliq ota masjidi", "Toshkent", "O'zbekiston", GeoPoint(41.2384, 69.3361), "Qo'yliq mavzesi, Bektemir"),
        Item(1026, "Ali ibn Abu Tolib masjidi", "Toshkent", "O'zbekiston", GeoPoint(41.2912, 69.3245), "Cho'lpon ko'chasi, Yashnobod"),
        Item(1027, "Yalang'och ota masjidi", "Toshkent", "O'zbekiston", GeoPoint(41.3482, 69.3491), "Buyuk Ipak Yo'li, Mirzo Ulug'bek"),

        // --- Toshkent viloyati ---
        Item(1030, "Zangiota majmuasi va jome masjidi", "Zangiota", "O'zbekiston", GeoPoint(41.2062, 69.1554), "Zangiota tumani", "Zangiata_Complex"),
        Item(1031, "Chirchiq markaziy jome masjidi", "Chirchiq", "O'zbekiston", GeoPoint(41.4682, 69.5821), "Chirchiq shahri"),
        Item(1032, "Olmaliq jome masjidi", "Olmaliq", "O'zbekiston", GeoPoint(40.8542, 69.6012), "Olmaliq shahri"),
        Item(1033, "Angren jome masjidi", "Angren", "O'zbekiston", GeoPoint(41.0162, 70.1432), "Angren shahri"),
        Item(1034, "Bekobod jome masjidi", "Bekobod", "O'zbekiston", GeoPoint(40.2182, 69.2312), "Bekobod shahri"),
        Item(1035, "G'azalkent jome masjidi", "Bo'stonliq", "O'zbekiston", GeoPoint(41.5612, 69.7712), "G'azalkent"),
        Item(1036, "Yangiyo'l markaziy jome masjidi", "Yangiyo'l", "O'zbekiston", GeoPoint(41.1162, 69.0512), "Yangiyo'l shahri"),

        // --- Samarqand viloyati ---
        Item(1040, "Imom Buxoriy xalqaro majmuasi", "Samarqand", "O'zbekiston", GeoPoint(39.8175, 66.9389), "Payariq tumani, Hartang", "Imam_Bukhari_Complex"),
        Item(1041, "Registon Tillakori masjidi", "Samarqand", "O'zbekiston", GeoPoint(39.6547, 66.9758), "Registon maydoni", "Registan"),
        Item(1042, "Bibixonim jome masjidi", "Samarqand", "O'zbekiston", GeoPoint(39.6608, 66.9796), "Bibixonim ko'chasi", "Bibi-Khanym_Mosque"),
        Item(1043, "Hazrati Xizr masjidi", "Samarqand", "O'zbekiston", GeoPoint(39.6639, 66.9831), "Shohi Zinda ro'parasi", "Hazrat_Khizr_Mosque"),
        Item(1044, "Shohi Zinda majmuasi", "Samarqand", "O'zbekiston", GeoPoint(39.6622, 66.9875), "Afrosiyob tepaligi", "Shah-i-Zinda"),
        Item(1045, "Ruhobod masjidi", "Samarqand", "O'zbekiston", GeoPoint(39.6492, 66.9678), "Go'ri Amir yaqinida"),
        Item(1046, "Xo'ja Ahror Valiy masjidi", "Samarqand", "O'zbekiston", GeoPoint(39.6231, 66.9452), "Ufarg'ona mahallasi"),
        Item(1047, "Xo'ja Abdu Darun masjidi", "Samarqand", "O'zbekiston", GeoPoint(39.6412, 67.0012), "Sadriddin Ayniy ko'chasi"),
        Item(1048, "Imom Moturidiy majmuasi", "Samarqand", "O'zbekiston", GeoPoint(39.6562, 66.9892), "Chokardiza qabristoni"),
        Item(1049, "Kattaqo'rg'on markaziy jome masjidi", "Kattaqo'rg'on", "O'zbekiston", GeoPoint(39.8972, 66.2589), "Amir Temur ko'chasi"),
        Item(1050, "Urgut markaziy jome masjidi", "Urgut", "O'zbekiston", GeoPoint(39.4042, 67.2452), "Urgut shahri"),

        // --- Buxoro viloyati ---
        Item(1060, "Poyi Kalon (Kalon) masjidi", "Buxoro", "O'zbekiston", GeoPoint(39.7759, 64.4153), "Kalon minorasi maydoni", "Po-i-Kalyan"),
        Item(1061, "Bolo Hovuz masjidi", "Buxoro", "O'zbekiston", GeoPoint(39.7778, 64.4072), "Ark ro'parasi, Registan", "Bolo_Hauz_Mosque"),
        Item(1062, "Mag'oki Attori masjidi", "Buxoro", "O'zbekiston", GeoPoint(39.7732, 64.4182), "Labihovuz yaqinida", "Magoki_Attari_Mosque"),
        Item(1063, "Chor Minor masjidi", "Buxoro", "O'zbekiston", GeoPoint(39.7747, 64.4278), "Chor Minor ko'chasi", "Chor_Minor"),
        Item(1064, "Bahouddin Naqshband majmuasi", "Buxoro", "O'zbekiston", GeoPoint(39.8012, 64.5362), "Qasri Orifon, Kogon tumani"),
        Item(1065, "Sayyid Mir Kulol majmuasi", "Kogon", "O'zbekiston", GeoPoint(39.7082, 64.5512), "Kogon tumani"),
        Item(1066, "G'ijduvon Abdulxoliq G'ijduvoniy masjidi", "G'ijduvon", "O'zbekiston", GeoPoint(40.1012, 64.6712), "Yusuf Hamadoniy ko'chasi"),

        // --- Andijon viloyati ---
        Item(1070, "Bog'i Eram jome masjidi", "Andijon", "O'zbekiston", GeoPoint(40.7821, 72.3442), "Bobur shoh ko'chasi"),
        Item(1071, "Devonaboy jome masjidi", "Andijon", "O'zbekiston", GeoPoint(40.7852, 72.3582), "Eski shahar, Andijon"),
        Item(1072, "Mirpo'stin ota masjidi", "Andijon", "O'zbekiston", GeoPoint(40.7612, 72.3312), "Mirpo'stin mahallasi"),
        Item(1073, "Shahrixon markaziy jome masjidi", "Shahrixon", "O'zbekiston", GeoPoint(40.7121, 72.0582), "Shahrixon shahri"),
        Item(1074, "Asaka jome masjidi", "Asaka", "O'zbekiston", GeoPoint(40.6412, 72.2382), "Asaka markazi"),
        Item(1075, "Xonobod markaziy jome masjidi", "Xonobod", "O'zbekiston", GeoPoint(40.8082, 73.0012), "Xonobod shahri"),

        // --- Namangan viloyati ---
        Item(1080, "Yusufxon Eshon jome masjidi", "Namangan", "O'zbekiston", GeoPoint(40.9982, 71.6721), "Bobur bog'i yaqinida"),
        Item(1081, "Ota Valixon Turat masjidi", "Namangan", "O'zbekiston", GeoPoint(41.0021, 71.6782), "Namangan markazi"),
        Item(1082, "Mulla Qirg'iz masjidi", "Namangan", "O'zbekiston", GeoPoint(40.9962, 71.6812), "Eski shahar"),
        Item(1083, "Chust markaziy jome masjidi", "Chust", "O'zbekiston", GeoPoint(41.0062, 71.2282), "Chust shahri"),
        Item(1084, "Kosonsoy markaziy jome masjidi", "Kosonsoy", "O'zbekiston", GeoPoint(41.2582, 71.5482), "Kosonsoy shahri"),
        Item(1085, "To'raqo'rg'on jome masjidi", "To'raqo'rg'on", "O'zbekiston", GeoPoint(41.0012, 71.5121), "To'raqo'rg'on markazi"),

        // --- Farg'ona viloyati ---
        Item(1090, "Farg'ona markaziy jome masjidi", "Farg'ona", "O'zbekiston", GeoPoint(40.3842, 71.7842), "Al-Farg'oniy ko'chasi"),
        Item(1091, "Qo'qon Juma masjidi", "Qo'qon", "O'zbekiston", GeoPoint(40.5312, 70.9412), "Xudoyorxon o'rdasi yaqinida", "Jami_Mosque_of_Kokand"),
        Item(1092, "Sohibzoda Hazrati Ali masjidi", "Marg'ilon", "O'zbekiston", GeoPoint(40.4712, 71.7212), "Marg'ilon shahri"),
        Item(1093, "Xonaqoh jome masjidi", "Marg'ilon", "O'zbekiston", GeoPoint(40.4752, 71.7182), "Burhoniddin Marg'inoniy majmuasi"),
        Item(1094, "Rishton markaziy jome masjidi", "Rishton", "O'zbekiston", GeoPoint(40.3582, 71.2842), "Rishton markazi"),
        Item(1095, "Oltiariq jome masjidi", "Oltiariq", "O'zbekiston", GeoPoint(40.4012, 71.4982), "Oltiariq markazi"),

        // --- Xorazm viloyati ---
        Item(1100, "Juma masjidi (Ichan Qal'a)", "Xiva", "O'zbekiston", GeoPoint(41.3782, 60.3592), "Ichan Qal'a markazi", "Juma_Mosque_(Khiva)"),
        Item(1101, "Sayyid Olovuddin masjidi", "Xiva", "O'zbekiston", GeoPoint(41.3772, 60.3612), "Ichan Qal'a"),
        Item(1102, "Urganch markaziy jome masjidi", "Urganch", "O'zbekiston", GeoPoint(41.5512, 60.6312), "Al-Xorazmiy shoh ko'chasi"),
        Item(1103, "Oxunbobo jome masjidi", "Urganch", "O'zbekiston", GeoPoint(41.5582, 60.6242), "Urganch markazi"),
        Item(1104, "Xonqa jome masjidi", "Xonqa", "O'zbekiston", GeoPoint(41.4782, 60.7712), "Xonqa tumani markazi"),

        // --- Qoraqalpog'iston Respublikasi ---
        Item(1110, "Imom Eshon Muhammad markaziy masjidi", "Nukus", "O'zbekiston", GeoPoint(42.4612, 59.6121), "Dosnazarov ko'chasi"),
        Item(1111, "Beruniy jome masjidi", "Beruniy", "O'zbekiston", GeoPoint(41.6912, 60.7512), "Beruniy shahri"),
        Item(1112, "To'rtko'l jome masjidi", "To'rtko'l", "O'zbekiston", GeoPoint(41.5512, 60.9882), "To'rtko'l shahri"),
        Item(1113, "Xo'jayli jome masjidi", "Xo'jayli", "O'zbekiston", GeoPoint(42.4012, 59.4582), "Mizdaxqon yaqinida"),

        // --- Qashqadaryo viloyati ---
        Item(1120, "Qarshi Ko'kgumbaz jome masjidi", "Qarshi", "O'zbekiston", GeoPoint(38.8612, 65.7912), "Eski shahar, Qarshi"),
        Item(1121, "Hazrati Sulton masjidi", "Qarshi", "O'zbekiston", GeoPoint(38.8542, 65.8012), "Nasaf ko'chasi"),
        Item(1122, "Shahrisabz Ko'kgumbaz masjidi", "Shahrisabz", "O'zbekiston", GeoPoint(39.0512, 66.8312), "Dorut Tilovat majmuasi", "Kok_Gumbaz_Mosque"),
        Item(1123, "Kitob markaziy jome masjidi", "Kitob", "O'zbekiston", GeoPoint(39.1212, 66.8812), "Kitob markazi"),
        Item(1124, "Koson jome masjidi", "Koson", "O'zbekiston", GeoPoint(39.0382, 65.5882), "Koson tumani"),

        // --- Surxondaryo viloyati ---
        Item(1130, "Imom Termiziy jome masjidi", "Termiz", "O'zbekiston", GeoPoint(37.2282, 67.2782), "At-Termiziy ko'chasi"),
        Item(1131, "Al-Hakim at-Termiziy majmuasi", "Termiz", "O'zbekiston", GeoPoint(37.2882, 67.1882), "Amudaryo sohilida"),
        Item(1132, "Denov markaziy jome masjidi", "Denov", "O'zbekiston", GeoPoint(38.2712, 67.8982), "Denov shahri"),
        Item(1133, "Boysun markaziy jome masjidi", "Boysun", "O'zbekiston", GeoPoint(38.2012, 67.2012), "Boysun tumani"),
        Item(1134, "Sherobod markaziy jome masjidi", "Sherobod", "O'zbekiston", GeoPoint(37.6712, 66.9882), "Sherobod markazi"),

        // --- Navoiy viloyati ---
        Item(1140, "Alisher Navoiy markaziy jome masjidi", "Navoiy", "O'zbekiston", GeoPoint(40.0842, 65.3782), "G'alaba shoh ko'chasi"),
        Item(1141, "Qosim Shayx xonaqohi va masjidi", "Karmana", "O'zbekiston", GeoPoint(40.1382, 65.3612), "Karmana markazi"),
        Item(1142, "Xatirchi markaziy jome masjidi", "Xatirchi", "O'zbekiston", GeoPoint(40.0282, 65.9812), "Xatirchi tumani"),
        Item(1143, "Zarafshon jome masjidi", "Zarafshon", "O'zbekiston", GeoPoint(41.5712, 64.1982), "Zarafshon shahri"),

        // --- Jizzax viloyati ---
        Item(1150, "Jizzax markaziy jome masjidi", "Jizzax", "O'zbekiston", GeoPoint(40.1158, 67.8422), "Sh.Rashidov shoh ko'chasi"),
        Item(1151, "Sayfinboy jome masjidi", "Jizzax", "O'zbekiston", GeoPoint(40.1282, 67.8312), "Jizzax markazi"),
        Item(1152, "G'allaorol markaziy jome masjidi", "G'allaorol", "O'zbekiston", GeoPoint(40.0212, 67.5882), "G'allaorol tumani"),
        Item(1153, "Zomin markaziy jome masjidi", "Zomin", "O'zbekiston", GeoPoint(39.9612, 68.3982), "Zomin tumani"),

        // --- Sirdaryo viloyati ---
        Item(1160, "Guliston markaziy jome masjidi", "Guliston", "O'zbekiston", GeoPoint(40.4892, 68.7842), "O'zbekiston ko'chasi"),
        Item(1161, "Yangiyer jome masjidi", "Yangiyer", "O'zbekiston", GeoPoint(40.2712, 68.8182), "Yangiyer shahri"),
        Item(1162, "Shirin jome masjidi", "Shirin", "O'zbekiston", GeoPoint(40.2282, 69.1782), "Shirin shahri"),
        Item(1163, "Boyovut jome masjidi", "Boyovut", "O'zbekiston", GeoPoint(40.3882, 69.0512), "Boyovut tumani"),
    )

    // ==========================================
    // 2. DUNYO MASJIDLARI (150+ mashhur masjid)
    // ==========================================
    val world: List<Item> = listOf(
        // Saudiya Arabistoni
        Item(1, "Masjidul Harom", "Makka", "Saudiya Arabistoni", GeoPoint(21.4225, 39.8262), "Ka'ba atrofi", "Great_Mosque_of_Mecca"),
        Item(2, "Masjidun Nabaviy", "Madina", "Saudiya Arabistoni", GeoPoint(24.4672, 39.6111), "Rasululloh (s.a.v.) ravzalari", "Prophet's_Mosque"),
        Item(3, "Quba masjidi", "Madina", "Saudiya Arabistoni", GeoPoint(24.4393, 39.6173), "Islomdagi ilk masjid", "Quba_Mosque"),
        Item(31, "Qiblatayn masjidi", "Madina", "Saudiya Arabistoni", GeoPoint(24.4842, 39.5786), "Ikki qibla masjidi", "Mosque_of_the_Two_Qiblas"),
        Item(32, "Namira masjidi", "Arafot", "Saudiya Arabistoni", GeoPoint(21.3533, 39.9839), "Arafot vodiysi", "Nimra_Mosque"),
        Item(33, "Al-Xayf masjidi", "Mino", "Saudiya Arabistoni", GeoPoint(21.4131, 39.8736), "Mino vodiysi", "Al-Khaif_Mosque"),
        Item(34, "Bilol masjidi", "Madina", "Saudiya Arabistoni", GeoPoint(24.4631, 39.6052), "Bilol Habashiy masjidi"),

        // Falastin
        Item(4, "Masjidul Aqso", "Quddus", "Falastin", GeoPoint(31.7761, 35.2358), "Qadimgi shahar", "Al-Aqsa_Mosque"),
        Item(35, "Qubbat as-Saxra (Qoya gumbazi)", "Quddus", "Falastin", GeoPoint(31.7780, 35.2354), "Haram ash-Sharif", "Dome_of_the_Rock"),
        Item(36, "Ibrohimiy masjidi", "Al-Xalil (Hebron)", "Falastin", GeoPoint(31.5247, 35.1108), "Ibrohim (a.s.) ziyoratgohi", "Cave_of_the_Patriarchs"),
        Item(37, "Umar masjidi", "Baytlahm", "Falastin", GeoPoint(31.7047, 35.2078), "Manger maydoni", "Mosque_of_Omar_(Bethlehem)"),

        // Turkiya
        Item(9, "Sulton Ahmad (Moviy masjid)", "Istanbul", "Turkiya", GeoPoint(41.0054, 28.9768), "Sultonahmad maydoni", "Blue_Mosque"),
        Item(19, "Sulaymoniya masjidi", "Istanbul", "Turkiya", GeoPoint(41.0162, 28.9639), "Sulaymon Qonuniy masjidi", "Süleymaniye_Mosque"),
        Item(38, "Ayasofiya jome masjidi", "Istanbul", "Turkiya", GeoPoint(41.0086, 28.9802), "Sultonahmad", "Hagia_Sophia"),
        Item(39, "Chamlidja masjidi", "Istanbul", "Turkiya", GeoPoint(41.0344, 29.0700), "Osiyodagi eng yirik masjid", "Grand_Çamlıca_Mosque"),
        Item(40, "Fotih masjidi", "Istanbul", "Turkiya", GeoPoint(41.0194, 28.9497), "Fotih tumani", "Fatih_Mosque,_Istanbul"),
        Item(41, "Eyüp Sulton masjidi", "Istanbul", "Turkiya", GeoPoint(41.0478, 28.9342), "Abu Ayyub al-Ansoriy", "Eyüp_Sultan_Mosque"),
        Item(42, "Rustam Posho masjidi", "Istanbul", "Turkiya", GeoPoint(41.0175, 28.9689), "Misr bozori yaqinida", "Rüstem_Pasha_Mosque"),
        Item(20, "Salimiya masjidi", "Edirne", "Turkiya", GeoPoint(41.6781, 26.5594), "Me'mor Sinon asari", "Selimiye_Mosque"),
        Item(43, "Ulug' Jome masjidi", "Bursa", "Turkiya", GeoPoint(40.1833, 29.0628), "20 gumbazli jome", "Grand_Mosque_of_Bursa"),
        Item(44, "Kocatepe masjidi", "Anqara", "Turkiya", GeoPoint(39.9167, 32.8608), "Anqara markazi", "Kocatepe_Mosque"),

        // BAA
        Item(10, "Shayx Zoid Katta masjidi", "Abu-Dabi", "BAA", GeoPoint(24.4128, 54.4750), "Abu-Dabi markazi", "Sheikh_Zayed_Grand_Mosque"),
        Item(45, "Jumeirah masjidi", "Dubay", "BAA", GeoPoint(25.2339, 55.2619), "Jumeirah Road", "Jumeirah_Mosque"),
        Item(46, "Al-Fotih masjidi", "Dubay", "BAA", GeoPoint(25.2631, 55.3089), "Deira"),
        Item(47, "Al-Nur masjidi", "Sharja", "BAA", GeoPoint(25.3464, 55.3853), "Xolid ko'li bo'yi", "Al_Noor_Mosque_(Sharjah)"),
        Item(48, "Shayx Zoid masjidi", "Fujayra", "BAA", GeoPoint(25.1306, 56.3267), "Fujayra markazi", "Sheikh_Zayed_Mosque,_Fujairah"),

        // Misr
        Item(14, "Al-Azhar masjidi", "Qohira", "Misr", GeoPoint(30.0457, 31.2627), "Islomiy universitet markazi", "Al-Azhar_Mosque"),
        Item(21, "Muhammad Ali masjidi", "Qohira", "Misr", GeoPoint(30.0287, 31.2599), "Qohira qal'asi", "Mosque_of_Muhammad_Ali"),
        Item(49, "Sulton Hasan masjidi", "Qohira", "Misr", GeoPoint(30.0322, 31.2561), "Mamluklar me'morchiligi", "Mosque-Madrasa_of_Sultan_Hassan"),
        Item(50, "Ibn Tulun masjidi", "Qohira", "Misr", GeoPoint(30.0289, 31.2494), "Qohiradagi eng qadimiy masjid", "Mosque_of_Ibn_Tulun"),
        Item(51, "Imom Husayn masjidi", "Qohira", "Misr", GeoPoint(30.0483, 31.2631), "Xon al-Xaliliy ro'parasi", "Al-Hussein_Mosque"),
        Item(52, "Amr ibn al-Os masjidi", "Qohira", "Misr", GeoPoint(30.0103, 31.2333), "Afrikadagi ilk masjid", "Mosque_of_Amr_ibn_al-As"),
        Item(53, "Abu al-Abbos al-Mursiy", "Iskandariya", "Misr", GeoPoint(31.2058, 29.8822), "Anfoushi maydoni", "Abu_al-Abbas_al-Mursi_Mosque"),

        // Marokash
        Item(11, "Hasan II masjidi", "Kasablanka", "Marokash", GeoPoint(33.6083, -7.6328), "Atlantika okeani sohilida", "Hassan_II_Mosque"),
        Item(54, "Kutubiya masjidi", "Marrakesh", "Marokash", GeoPoint(31.6247, -7.9936), "Jamoa al-Fna yaqinida", "Koutoubia_Mosque"),
        Item(55, "Al-Qaraviyyin masjidi", "Fos", "Marokash", GeoPoint(34.0647, -4.9733), "Dunyoning eng qadimiy universiteti", "University_of_al-Qarawiyyin"),
        Item(56, "Tingis Jome masjidi", "Tanjer", "Marokash", GeoPoint(35.7872, -5.8117), "Tanjer markazi"),
        Item(57, "Hasan minorasi masjidi", "Rabot", "Marokash", GeoPoint(34.0242, -6.8228), "Rabot markazi", "Hassan_Tower"),

        // Qatar
        Item(58, "Imom Muhammad ibn Abdulvahhob masjidi", "Doha", "Qatar", GeoPoint(25.3228, 51.5117), "Qatar Davlat Katta masjidi", "Imam_Muhammad_ibn_Abd_al-Wahhab_Mosque"),
        Item(59, "Fanar Islom madaniy markazi", "Doha", "Qatar", GeoPoint(25.2897, 51.5367), "Spiral minora", "Fanar,_Qatar_Islamic_Cultural_Center"),
        Item(60, "Education City masjidi", "Doha", "Qatar", GeoPoint(25.3147, 51.4406), "Zamonaviy me'morchilik"),
        Item(61, "Katara masjidi", "Doha", "Qatar", GeoPoint(25.3589, 51.5244), "Moviy koshinli masjid"),

        // Quvayt
        Item(62, "Quvayt Katta masjidi", "Al-Quvayt", "Quvayt", GeoPoint(29.3797, 47.9744), "Arab ko'rfazi bo'yida", "Grand_Mosque_(Kuwait)"),
        Item(63, "Fotima masjidi", "Al-Quvayt", "Quvayt", GeoPoint(29.3589, 47.9892), "Zamonaviy gumbaz"),

        // Ummon
        Item(64, "Sulton Qobus Katta masjidi", "Maskat", "Ummon", GeoPoint(23.5839, 58.3886), "Eng katta qo'lda to'qilgan gilam", "Sultan_Qaboos_Grand_Mosque"),
        Item(65, "Muhammad al-Amin masjidi", "Maskat", "Ummon", GeoPoint(23.5786, 58.4069), "Bavshar tepaligi"),
        Item(66, "Sulton Qobus masjidi", "Salala", "Ummon", GeoPoint(17.0194, 54.0925), "Zufar viloyati"),

        // Iordaniya
        Item(67, "Qirol Abdulloh I masjidi", "Ammon", "Iordaniya", GeoPoint(31.9619, 35.9128), "Moviy gumbazli jome", "King_Abdullah_I_Mosque"),
        Item(68, "Qirol Husayn masjidi", "Ammon", "Iordaniya", GeoPoint(31.9889, 35.8294), "Dabuk tepaligi", "King_Hussein_Mosque"),
        Item(69, "Abu Darvish masjidi", "Ammon", "Iordaniya", GeoPoint(31.9425, 35.9406), "Qora-oq toshli masjid"),

        // Indoneziya
        Item(16, "Istiqlol masjidi", "Jakarta", "Indoneziya", GeoPoint(-6.1702, 106.8314), "Janubi-Sharqiy Osiyodagi eng katta masjid", "Istiqlal_Mosque,_Jakarta"),
        Item(70, "Bayturrahmon Katta masjidi", "Banda Aceh", "Indoneziya", GeoPoint(5.5536, 95.3192), "Tsunami mo'jizasi", "Baiturrahman_Grand_Mosque"),
        Item(71, "Al-Jabbar masjidi", "Bandung", "Indoneziya", GeoPoint(-6.9536, 107.7039), "Suv ustidagi masjid", "Al-Jabbar_Grand_Mosque"),
        Item(72, "Buyuk Semarang masjidi", "Semarang", "Indoneziya", GeoPoint(-6.9839, 110.4464), "Ochiluvchi ulkan soyabonlar", "Great_Mosque_of_Central_Java"),
        Item(73, "Dian Al-Mahri (Oltin gumbaz)", "Depok", "Indoneziya", GeoPoint(-6.3853, 106.7725), "Haqiqiy oltin gumbaz", "Dian_Al-Mahri_Mosque"),
        Item(74, "Menara Kudus masjidi", "Kudus", "Indoneziya", GeoPoint(-6.8044, 110.8328), "Qadimiy minora", "Menara_Kudus_Mosque"),

        // Malayziya
        Item(24, "Putra masjidi", "Putrajaya", "Malayziya", GeoPoint(2.9360, 101.6895), "Pushti gumbazli masjid", "Putra_Mosque"),
        Item(75, "Sulton Salohiddin Abdul Aziz Shoh", "Shoh Olam", "Malayziya", GeoPoint(3.0783, 101.5208), "Moviy masjid", "Sultan_Salahuddin_Abdul_Aziz_Mosque"),
        Item(76, "Milliy masjid (Masjid Negara)", "Kuala-Lumpur", "Malayziya", GeoPoint(3.1419, 101.6919), "16 qirrali yulduz gumbaz", "National_Mosque_of_Malaysia"),
        Item(77, "Zahir masjidi", "Alor Setar", "Malayziya", GeoPoint(6.1197, 100.3653), "Kedah shtati", "Zahir_Mosque"),
        Item(78, "Kristal masjidi", "Kuala Terengganu", "Malayziya", GeoPoint(5.3217, 103.1206), "Shisha va po'lat me'morchiligi", "Crystal_Mosque"),
        Item(79, "Wiloyat masjidi", "Kuala-Lumpur", "Malayziya", GeoPoint(3.1714, 101.6708), "Usmoniy uslubidagi jome", "Federal_Territory_Mosque"),

        // Pokiston
        Item(12, "Faysal masjidi", "Islomobod", "Pokiston", GeoPoint(33.7299, 73.0372), "Margalla tepaliklari etagida", "Faisal_Mosque"),
        Item(13, "Podshohiy masjid", "Lahor", "Pokiston", GeoPoint(31.5879, 74.3096), "Boburiylar me'morchiligi", "Badshahi_Mosque"),
        Item(80, "Vazir Xon masjidi", "Lahor", "Pokiston", GeoPoint(31.5831, 74.3214), "Freska va naqshlar", "Wazir_Khan_Mosque"),
        Item(81, "To'ba masjidi", "Karachi", "Pokiston", GeoPoint(24.8436, 67.0583), "Dunyoning eng katta yagona gumbazi", "Masjid_e_Tooba"),
        Item(82, "Mahabat Xon masjidi", "Peshovar", "Pokiston", GeoPoint(34.0097, 71.5714), "Eski shahar", "Mahabat_Khan_Mosque"),

        // Hindiston
        Item(23, "Jome masjidi", "Dehli", "Hindiston", GeoPoint(28.6507, 77.2334), "Shoh Jahon masjidi", "Jama_Masjid,_Delhi"),
        Item(83, "Toj Mahal masjidi", "Agra", "Hindiston", GeoPoint(27.1750, 78.0422), "Toj Mahal majmuasi", "Taj_Mahal"),
        Item(84, "Makka masjidi", "Haydarobod", "Hindiston", GeoPoint(17.3608, 78.4736), "Makka g'ishtlari bilan qurilgan", "Makkah_Masjid,_Hyderabad"),
        Item(85, "Toj-ul-Masjid", "Bhopal", "Hindiston", GeoPoint(23.2619, 77.3917), "Masjidlar toji", "Taj-ul-Masajid"),
        Item(86, "Cheraman Juma masjidi", "Kodungallur, Kerala", "Hindiston", GeoPoint(10.2158, 76.1969), "Hindistondagi ilk masjid (629-yil)", "Cheraman_Juma_Mosque"),

        // Bangladesh
        Item(87, "Baytul Mukarram Milliy masjidi", "Dakka", "Bangladesh", GeoPoint(23.7294, 90.4128), "Bangladesh Milliy masjidi", "Baitul_Mukarram_National_Mosque"),
        Item(88, "60 gumbazli masjid", "Bagerhat", "Bangladesh", GeoPoint(22.6739, 89.7417), "YuNESKO merosi", "Sixty_Dome_Mosque"),
        Item(89, "Tara (Yulduz) masjidi", "Dakka", "Bangladesh", GeoPoint(23.7153, 90.4014), "Yulduzli mozaika", "Tara_Mosque"),

        // Eron
        Item(22, "Imom masjidi (Shoh masjidi)", "Isfahon", "Eron", GeoPoint(32.6546, 51.6777), "Naqshi Jahon maydoni", "Shah_Mosque"),
        Item(90, "Shayx Lutfulloh masjidi", "Isfahon", "Eron", GeoPoint(32.6575, 51.6789), "Minorasiz noyob masjid", "Sheikh_Lotfollah_Mosque"),
        Item(91, "Nosir al-Mulk (Pushti masjid)", "Sheroz", "Eron", GeoPoint(29.6083, 52.5486), "Rangli vitrajlar", "Nasir_al-Mulk_Mosque"),
        Item(92, "Imom Rizo majmuasi", "Mashhad", "Eron", GeoPoint(36.2878, 59.6158), "Dunyodagi eng katta maydonli masjid", "Imam_Reza_shrine"),
        Item(93, "Fotima Masuma ziyoratgohi", "Qum", "Eron", GeoPoint(34.6417, 50.8789), "Oltin gumbaz", "Fatima_Masumeh_Shrine"),

        // Iroq
        Item(94, "Imom Ali ziyoratgohi", "Najaf", "Iroq", GeoPoint(31.9961, 44.3142), "Ali ibn Abu Tolib ravzasi", "Imam_Ali_Shrine"),
        Item(95, "Imom Husayn ziyoratgohi", "Karbalo", "Iroq", GeoPoint(32.6164, 44.0325), "Husayn ibn Ali ravzasi", "Imam_Husayn_Shrine"),
        Item(96, "Katta Samarra masjidi (Malviya)", "Samarra", "Iroq", GeoPoint(34.2064, 43.8803), "Spiral minora", "Great_Mosque_of_Samarra"),
        Item(97, "Abu Hanifa masjidi", "Bag'dod", "Iroq", GeoPoint(33.3728, 44.3592), "Imomi A'zam masjidi", "Abu_Hanifa_Mosque"),

        // Suriya
        Item(15, "Umaviylar masjidi", "Damashq", "Suriya", GeoPoint(33.5116, 36.3066), "Qadimgi Damashq", "Umayyad_Mosque"),
        Item(98, "Halab Katta masjidi", "Halab", "Suriya", GeoPoint(36.1992, 37.1567), "Zakariyo (a.s.) qabri", "Great_Mosque_of_Aleppo"),
        Item(99, "Xolid ibn Valid masjidi", "Homs", "Suriya", GeoPoint(34.7369, 36.7161), "Al-Xolidiyya", "Khalid_ibn_al-Walid_Mosque"),

        // Livan
        Item(100, "Muhammad al-Amin masjidi", "Bayrut", "Livan", GeoPoint(33.8953, 35.5061), "Shahidlar maydoni", "Mohammad_Al-Amin_Mosque"),

        // Qozog'iston
        Item(101, "Ostona Katta masjidi", "Ostona", "Qozog'iston", GeoPoint(51.0906, 71.4117), "Markaziy Osiyodagi eng yirik masjid", "Astana_Grand_Mosque"),
        Item(102, "Hazrati Sulton masjidi", "Ostona", "Qozog'iston", GeoPoint(51.1258, 71.4722), "Mustaqillik maydoni", "Hazrat_Sultan_Mosque"),
        Item(103, "Nur-Ostona masjidi", "Ostona", "Qozog'iston", GeoPoint(51.1264, 71.4161), "Qabantoy botir ko'chasi", "Nur-Astana_Mosque"),
        Item(104, "Olmaota Markaziy masjidi", "Olmaota", "Qozog'iston", GeoPoint(43.2681, 76.9536), "Pushkin ko'chasi", "Central_Mosque_(Almaty)"),
        Item(105, "Xo'ja Ahmad Yassaviy majmuasi", "Turkiston", "Qozog'iston", GeoPoint(43.2975, 68.2178), "Amir Temur qurdirgan maqbara", "Mausoleum_of_Khoja_Ahmed_Yasawi"),

        // Qirg'iziston
        Item(106, "Imom Saraxsiy Markaziy masjidi", "Bishkek", "Qirg'iziston", GeoPoint(42.8758, 74.6289), "Jibek Jolu shoh ko'chasi", "Imam_Sarakhsi_Mosque"),
        Item(107, "Sulaymon-Too masjidi", "O'sh", "Qirg'iziston", GeoPoint(40.5289, 72.7847), "Sulaymon tog'i etagida"),
        Item(108, "Ravza masjidi", "O'sh", "Qirg'iziston", GeoPoint(40.5361, 72.7958), "O'sh markazi"),

        // Tojikiston
        Item(109, "Tojikiston Katta masjidi", "Dushanbe", "Tojikiston", GeoPoint(38.6019, 68.7758), "Markaziy Osiyodagi eng yiriklaridan", "Dushanbe_Central_Mosque"),
        Item(110, "Hoji Yaqub masjidi", "Dushanbe", "Tojikiston", GeoPoint(38.5831, 68.7889), "Shodmoniy ko'chasi"),
        Item(111, "Shayx Muslihiddin majmuasi", "Xo'jand", "Tojikiston", GeoPoint(40.2819, 69.6225), "Panjshanbe bozori yonida"),

        // Turkmaniston
        Item(112, "Turkmanboshi Ruhi masjidi", "Ashxobod", "Turkmaniston", GeoPoint(38.0242, 58.2517), "Qipchoq qishlog'i", "Türkmenbaşy_Ruhy_Mosque"),
        Item(113, "Ertug'rul G'ozi masjidi", "Ashxobod", "Turkmaniston", GeoPoint(37.9317, 58.3972), "Usmoniy uslubidagi masjid", "Ertuğrul_Gazi_Mosque"),

        // Ozarbayjon
        Item(114, "Haydar masjidi", "Boku", "Ozarbayjon", GeoPoint(40.4289, 49.8219), "Binaqadi tumani", "Heydar_Mosque"),
        Item(115, "Bibi-Heybat masjidi", "Boku", "Ozarbayjon", GeoPoint(40.3081, 49.8206), "Boku qo'ltig'i bo'yida", "Bibi-Heybat_Mosque"),
        Item(116, "Tazapir masjidi", "Boku", "Ozarbayjon", GeoPoint(40.3719, 49.8286), "Mirzo Fatali Oxundov ko'chasi", "Taza_Pir_Mosque"),
        Item(117, "Shamaxa Juma masjidi", "Shamaxa", "Ozarbayjon", GeoPoint(40.6319, 48.6417), "Kavkazdagi eng qadimiy masjidlardan", "Juma_Mosque,_Shamakhi"),

        // Rossiya
        Item(17, "Qul Sharif masjidi", "Qozon", "Rossiya", GeoPoint(55.7984, 49.1052), "Qozon Kremli", "Qolşärif_Mosque"),
        Item(18, "Moskva Jome masjidi", "Moskva", "Rossiya", GeoPoint(55.7792, 37.6264), "Olimpiyskiy shoh ko'chasi", "Moscow_Cathedral_Mosque"),
        Item(118, "Sankt-Peterburg Jome masjidi", "Sankt-Peterburg", "Rossiya", GeoPoint(59.9553, 30.3239), "Petrograd tomoni, moviy koshinlar", "Saint_Petersburg_Mosque"),
        Item(119, "Checheniston yuragi (Ahmad Qodirov)", "Grozniy", "Rossiya", GeoPoint(43.3178, 45.6942), "Sunja daryosi bo'yida", "Akhmad_Kadyrov_Mosque"),
        Item(120, "Musulmonlar iftixori masjidi", "Shali", "Rossiya", GeoPoint(43.1472, 45.9036), "Oq marmarli ulkan masjid", "Pride_of_Muslims_Mosque"),
        Item(121, "Lola-Lolalar masjidi", "Ufa", "Rossiya", GeoPoint(54.8197, 56.0592), "Lolalar shaklidagi minoralar", "Lala_Tulip"),
        Item(122, "Maxachqala Markaziy Jome masjidi", "Maxachqala", "Rossiya", GeoPoint(42.9694, 47.4981), "Dog'iston poytaxti"),

        // Buyuk Britaniya
        Item(123, "London Markaziy masjidi (Regents Park)", "London", "Buyuk Britaniya", GeoPoint(51.5297, -0.1661), "Regent's Park", "London_Central_Mosque"),
        Item(124, "Sharqiy London masjidi", "London", "Buyuk Britaniya", GeoPoint(51.5178, -0.0653), "Whitechapel Road", "East_London_Mosque"),
        Item(125, "Kembrij Markaziy masjidi", "Kembrij", "Buyuk Britaniya", GeoPoint(52.1978, 0.1417), "Yevropadagi ilk eko-masjid", "Cambridge_Central_Mosque"),
        Item(126, "Birmingem Markaziy masjidi", "Birmingem", "Buyuk Britaniya", GeoPoint(52.4642, -1.8906), "Highgate", "Birmingham_Central_Mosque"),

        // AQSH
        Item(127, "Vashington Islomiy Markazi", "Vashington", "AQSH", GeoPoint(38.9172, -77.0544), "Massachusetts Avenue", "Islamic_Center_of_Washington"),
        Item(128, "Nyu-York Islom madaniy markazi", "Nyu-York", "AQSH", GeoPoint(40.7850, -73.9519), "96th Street, Manhattan", "Islamic_Cultural_Center_of_New_York"),
        Item(129, "Diyanet Amerika markazi", "Lanham, Maryland", "AQSH", GeoPoint(38.9819, -76.8436), "Usmoniy me'morchiligi", "Diyanet_Center_of_America"),
        Item(130, "Dirborn Islom markazi", "Dirborn, Michigan", "AQSH", GeoPoint(42.3486, -83.1878), "Ford Road"),

        // Germaniya
        Item(131, "Kyoln Markaziy masjidi", "Kyoln", "Germaniya", GeoPoint(50.9458, 6.9272), "Venloer Strasse", "Cologne_Central_Mosque"),
        Item(132, "Sehitlik masjidi", "Berlin", "Germaniya", GeoPoint(52.4819, 13.4111), "Neukölln", "Şehitlik_Mosque"),

        // Fransiya
        Item(133, "Parij Katta masjidi", "Parij", "Fransiya", GeoPoint(48.8419, 2.3550), "Lotin kvartali", "Grand_Mosque_of_Paris"),
        Item(134, "Lion Katta masjidi", "Lion", "Fransiya", GeoPoint(45.7347, 4.8872), "Boulevard Pinel", "Grand_Mosque_of_Lyon"),

        // Yaponiya
        Item(135, "Tokio Jome masjidi", "Tokio", "Yaponiya", GeoPoint(35.6681, 139.6769), "Shibuya, Oyama-cho", "Tokyo_Camii"),
        Item(136, "Kobe masjidi", "Kobe", "Yaponiya", GeoPoint(34.6983, 135.1883), "Yaponiyadagi ilk masjid (1935-yil)", "Kobe_Mosque"),

        // Janubiy Koreya
        Item(137, "Seul Markaziy masjidi", "Seul", "Janubiy Koreya", GeoPoint(37.5333, 126.9969), "Itaewon", "Seoul_Central_Mosque"),

        // Xitoy
        Item(138, "Niujie masjidi", "Pekin", "Xitoy", GeoPoint(39.8858, 116.3578), "Pekindagi eng qadimiy masjid (996-yil)", "Niujie_Mosque"),
        Item(139, "Id Kah masjidi", "Qashqar", "Xitoy", GeoPoint(39.4719, 75.9897), "Shinjondagi eng katta masjid", "Id_Kah_Mosque"),
        Item(140, "Buyuk Syan masjidi", "Xian", "Xitoy", GeoPoint(34.2625, 108.9389), "Ipak yo'li markazi", "Great_Mosque_of_Xi'an"),

        // Janubiy Afrika
        Item(141, "Nizomiye masjidi", "Yoxannesburg", "Janubiy Afrika", GeoPoint(-26.0125, 28.1139), "Midrand", "Nizamiye_Mosque"),
        Item(142, "Avval masjidi", "Keyptaun", "Janubiy Afrika", GeoPoint(-33.9197, 18.4147), "Bo-Kaap, Janubiy Afrikadagi ilk masjid", "Auwal_Mosque"),

        // Avstraliya
        Item(143, "Lakemba masjidi", "Sidney", "Avstraliya", GeoPoint(-33.9219, 151.0772), "Imom Ali ibn Abu Tolib masjidi", "Lakemba_Mosque"),
        Item(144, "Melburn Markaziy masjidi", "Melburn", "Avstraliya", GeoPoint(-37.7958, 144.9606), "Carlton"),

        // Singapur & Bruney
        Item(145, "Sulton masjidi", "Singapur", "Singapur", GeoPoint(1.3025, 103.8592), "Kampong Glam", "Sultan_Mosque"),
        Item(146, "Sulton Umar Ali Sayfuddin", "Bandar-Seri-Begavan", "Bruney", GeoPoint(4.8894, 114.9394), "Oltin gumbazli ko'l masjidi", "Sultan_Omar_Ali_Saifuddien_Mosque"),
        Item(147, "Jame' Asr Hassanil Bolkiah", "Bandar-Seri-Begavan", "Bruney", GeoPoint(4.9011, 114.9225), "29 ta oltin gumbaz", "Jame'_'Asr_Hassanil_Bolkiah"),
    )

    /** Barcha masjidlar (O'zbekiston + Dunyo). */
    val all: List<Item> = uzbekistan + world
}
