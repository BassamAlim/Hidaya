package bassamalim.hidaya.utils

import android.content.SharedPreferences
import bassamalim.hidaya.Prefs
import bassamalim.hidaya.enum.Language
import bassamalim.hidaya.enum.Theme

object PrefUtils {

    fun getLanguage(pref: SharedPreferences): Language {
        val lang = getString(
            pref,
            Prefs.Language.key,
            Prefs.Language.default as String
        )
        return if (lang == "en") Language.ENGLISH
        else Language.ARABIC
    }

    fun getNumeralsLanguage(pref: SharedPreferences): Language {
        val lang = getString(
            pref,
            Prefs.NumeralsLanguage.key,
            Prefs.NumeralsLanguage.default as String
        )
        return if (lang == "en") Language.ENGLISH
        else Language.ARABIC
    }

    fun getTheme(pref: SharedPreferences): Theme {
        val themeStr = getString(
            pref,
            Prefs.Theme.key,
            Prefs.Theme.default as String
        )

        return when(themeStr) {
            "Dark" -> Theme.DARK
            "Night" -> Theme.NIGHT
            else -> Theme.LIGHT
        }
    }

    /**
     * Created as a fail safe in case the data type of the preference is changed
     * "If the value at the given key is a string, return it, otherwise, set the value at the given key
     * to the given default value and return the default value."
     *
     *
     * @param key The key of the preference you want to get
     * @param default The default value to return if the preference doesn't exist.
     * @param pref SharedPreferences - The SharedPreferences object to get the value from
     * @param context Context
     * @return The value with the key in the SharedPreferences object.
     */
    fun getString(
        pref: SharedPreferences,
        key: String,
        default: String
    ): String {
        return try {
            pref.getString(key, default)!!
        } catch (e: java.lang.ClassCastException) {
            pref.edit()
                .putString(key, default)
                .apply()

            getString(pref, key, default)
        }
    }

    fun getInt(
        pref: SharedPreferences,
        key: String,
        default: Int
    ): Int {
        return try {
            pref.getInt(key, default)
        } catch (e: java.lang.ClassCastException) {
            pref.edit()
                .putInt(key, default)
                .apply()

            getInt(pref, key, default)
        }
    }

    fun getFloat(
        pref: SharedPreferences,
        key: String,
        default: Float
    ): Float {
        return try {
            pref.getFloat(key, default)
        } catch (e: java.lang.ClassCastException) {
            pref.edit()
                .putFloat(key, default)
                .apply()

            getFloat(pref, key, default)
        }
    }

    fun getBoolean(
        pref: SharedPreferences,
        key: String,
        default: Boolean
    ): Boolean {
        return try {
            pref.getBoolean(key, default)
        } catch (e: java.lang.ClassCastException) {
            pref.edit()
                .putBoolean(key, default)
                .apply()

            getBoolean(pref, key, default)
        }
    }

    fun getLong(
        pref: SharedPreferences,
        key: String,
        default: Long
    ): Long {
        return try {
            pref.getLong(key, default)
        } catch (e: java.lang.ClassCastException) {
            pref.edit()
                .putLong(key, default)
                .apply()

            getLong(pref, key, default)
        }
    }

}