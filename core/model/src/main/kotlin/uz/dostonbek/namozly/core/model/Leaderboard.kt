package uz.dostonbek.namozly.core.model

/** Musobaqa doirasi. Mamlakat chegarasi server tomonda qat'iy: boshqa davlat statistikasi ko'rinmaydi. */
enum class LeaderboardScope { MAHALLA, DISTRICT, REGION, COUNTRY }

enum class LeaderboardPeriod { DAY, WEEK, MONTH }

data class UserProfile(
    val uid: String,
    val displayName: String,
    val countryCode: String,
    val region: String?,
    val district: String?,
    val mahalla: String?,
    val phone: String? = null,
    /** Yashirin rejim: reytingda "Mo'min #1234" ko'rinadi. */
    val anonymous: Boolean = false,
) {
    /** Reytingdagi ko'rinish: "Abdulloh R." yoki "Mo'min #2741". */
    val publicName: String
        get() = if (anonymous) "Mo'min #" + (uid.hashCode().toUInt() % 9000u + 1000u) else displayName
}

data class LeaderboardEntry(
    val rank: Int,
    val displayName: String,
    val count: Long,
    val isMe: Boolean,
)

data class Leaderboard(
    val scope: LeaderboardScope,
    val period: LeaderboardPeriod,
    val areaName: String,
    val entries: List<LeaderboardEntry>,
    /** Maxfiylik: 10 kishidan kam bo'lsa ro'yxat yashiriladi. */
    val hiddenForPrivacy: Boolean = false,
)
