package bassamalim.hidaya.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.helpers.PrayTimes

object PrefUtils {

    fun getLanguage(
        context: Context,
        pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    ): String {
        return pref.getString(
            context.getString(R.string.language_key), context.getString(R.string.default_language)
        )!!
    }

    fun getNumeralsLanguage(
        context: Context,
        pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    ): String {
        return pref.getString(
            context.getString(R.string.numerals_language_key),
            context.getString(R.string.default_language)
        )!!
    }

    fun getTimeFormat(
        context: Context,
        pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    ): PrayTimes.TF {
        val str = pref.getString(
            context.getString(R.string.time_format_key),
            context.getString(R.string.default_time_format)
        )!!

        return when(str) {
            "24h" -> PrayTimes.TF.H24
            else -> PrayTimes.TF.H12
        }
    }

    fun getTheme(
        context: Context,
        pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    ) : String {

        return pref.getString(
            context.getString(R.string.theme_key),
            context.getString(R.string.default_theme)
        )!!
    }

}