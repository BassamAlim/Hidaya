package bassamalim.hidaya.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import bassamalim.hidaya.Prefs
import bassamalim.hidaya.enum.Language
import bassamalim.hidaya.enum.Theme

object PrefUtils {

    fun getPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun getLanguage(pref: SharedPreferences): Language {
        return when (getString(pref, Prefs.Language)) {
            "en" -> Language.ENGLISH
            else -> Language.ARABIC
        }
    }

    fun getNumeralsLanguage(pref: SharedPreferences): Language {
        return when (getString(pref, Prefs.NumeralsLanguage)) {
            "en" -> Language.ENGLISH
            else -> Language.ARABIC
        }
    }

    fun getTheme(pref: SharedPreferences): Theme {
        return when (getString(pref, Prefs.Theme)) {
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
     * @param pref SharedPreferences object
     * @param prefObj an object that identifies a preference, contains key, default
     * @return The value with the key in the SharedPreferences object.
     */

    fun getString(
        pref: SharedPreferences,
        prefObj: Prefs
    ): String {
        val key = prefObj.key
        val default = prefObj.default as String
        return try {
            pref.getString(key, default)!!
        } catch (e: java.lang.ClassCastException) {
            pref.edit()
                .putString(key, default)
                .apply()

            getString(pref, prefObj)
        }
    }

    fun getInt(
        pref: SharedPreferences,
        prefObj: Prefs
    ): Int {
        val key = prefObj.key
        val default = prefObj.default as Int
        return try {
            pref.getInt(key, default)
        } catch (e: java.lang.ClassCastException) {
            pref.edit()
                .putInt(key, default)
                .apply()

            getInt(pref, prefObj)
        }
    }

    fun getFloat(
        pref: SharedPreferences,
        prefObj: Prefs
    ): Float {
        val key = prefObj.key
        val default = prefObj.default as Float
        return try {
            pref.getFloat(key, default)
        } catch (e: java.lang.ClassCastException) {
            pref.edit()
                .putFloat(key, default)
                .apply()

            getFloat(pref, prefObj)
        }
    }

    fun getBoolean(
        pref: SharedPreferences,
        prefObj: Prefs
    ): Boolean {
        val key = prefObj.key
        val default = prefObj.default as Boolean
        return try {
            pref.getBoolean(key, default)
        } catch (e: java.lang.ClassCastException) {
            pref.edit()
                .putBoolean(key, default)
                .apply()

            getBoolean(pref, prefObj)
        }
    }

    fun getLong(
        pref: SharedPreferences,
        prefObj: Prefs
    ): Long {
        val key = prefObj.key
        val default = prefObj.default as Long
        return try {
            pref.getLong(key, default)
        } catch (e: java.lang.ClassCastException) {
            pref.edit()
                .putLong(key, default)
                .apply()

            getLong(pref, prefObj)
        }
    }

}