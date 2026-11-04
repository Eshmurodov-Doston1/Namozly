package uz.dostonbek.namozly.core.model

data class Dhikr(
    val id: String,
    val arabic: String,
    val text: LocalizedText,
    val meaning: LocalizedText,
    val defaultTarget: Int = 33,
)

data class TasbihEntry(
    val id: Long = 0,
    val dhikrId: String,
    val count: Int,
    val target: Int,
    val timestamp: Long,
)

data class TasbihStats(
    val today: Int,
    val week: Int,
    val month: Int,
    val total: Int,
    val perDhikr: Map<String, Int>,
)

enum class QazaPrayer { FAJR, DHUHR, ASR, MAGHRIB, ISHA, WITR }

data class QazaRecord(val prayer: QazaPrayer, val remaining: Int, val completed: Int)

enum class NotificationType { PRAYER, PRE_ALERT, RAMADAN, FRIDAY, AYAH, NEWS, SYSTEM }

data class AppNotification(
    val id: Long,
    val type: NotificationType,
    val title: String,
    val body: String,
    val timestamp: Long,
    val read: Boolean,
)
