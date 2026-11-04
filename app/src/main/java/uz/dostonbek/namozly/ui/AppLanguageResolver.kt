package uz.dostonbek.namozly.ui

import android.content.Context
import android.telephony.TelephonyManager
import uz.dostonbek.namozly.core.model.AppLanguage
import uz.dostonbek.namozly.core.model.UserLocation
import java.util.Locale

/**
 * Ilova tilini mamlakatga qarab tanlaydi (foydalanuvchi qo'lda tanlamaguncha):
 *  1) joylashuv mamlakati (GPS yoki qo'lda kiritilgan joy);
 *  2) SIM-karta / mobil tarmoq mamlakati;
 *  3) telefon tili.
 * O'zbekiston — o'zbekcha (lotin); MDH davlatlari — ruscha; qolganlari — telefon tili (uz/ru) yoki inglizcha.
 */
object AppLanguageResolver {

    private val russianSpeaking = setOf("RU", "KZ", "KG", "TJ", "TM", "BY", "AZ", "AM", "GE", "MD", "UA")

    fun resolve(context: Context, location: UserLocation): AppLanguage {
        val fromLocation = location.countryCode.takeIf { location != UserLocation.Default && it.isNotBlank() }
        val country = (fromLocation ?: telephonyCountry(context) ?: location.countryCode.ifBlank { null } ?: Locale.getDefault().country)
            .uppercase(Locale.ROOT)
        return when {
            country == "UZ" -> if (Locale.getDefault().script.equals("Cyrl", true)) AppLanguage.UZ_CYRL else AppLanguage.UZ_LATN
            country in russianSpeaking -> AppLanguage.RU
            else -> when (Locale.getDefault().language) {
                "uz" -> AppLanguage.UZ_LATN
                "ru" -> AppLanguage.RU
                else -> AppLanguage.EN
            }
        }
    }

    private fun telephonyCountry(context: Context): String? = runCatching {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        tm?.networkCountryIso?.takeIf { it.length == 2 } ?: tm?.simCountryIso?.takeIf { it.length == 2 }
    }.getOrNull()
}
