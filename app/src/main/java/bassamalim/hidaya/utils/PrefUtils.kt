package bassamalim.hidaya.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import bassamalim.hidaya.data.Prefs
import bassamalim.hidaya.enums.Language
import bassamalim.hidaya.enums.Theme
import bassamalim.hidaya.enums.TimeFormat

object PrefUtils {

    fun getPreferences(context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    fun getLanguage(sp: SharedPreferences) =
        Language.valueOf(getString(sp, Prefs.Language))

    fun getNumeralsLanguage(sp: SharedPreferences) =
        Language.valueOf(getString(sp, Prefs.NumeralsLanguage))

    fun getTheme(sp: SharedPreferences) =
        Theme.valueOf(getString(sp, Prefs.Theme))

    fun getTimeFormat(sp: SharedPreferences) =
        TimeFormat.valueOf(getString(sp, Prefs.TimeFormat))

    /**
     * Created as a fail safe in case the data type of the preference is changed
     * "If the value at the given key is a string, return it, otherwise, set the value at the given key
     * to the given default value and return the default value."
     *
     *
     * @param sp SharedPreferences object
     * @param pref an object that identifies a preference, contains key, default
     * @return The value with the key in the SharedPreferences object.
     */

    fun getString(
        sp: SharedPreferences,
        pref: Prefs
    ): String {
        val key = pref.key
        val default = pref.default as String
        return try {
            sp.getString(key, default)!!
        } catch (e: java.lang.ClassCastException) {
            sp.edit()
                .putString(key, default)
                .apply()

            getString(sp, pref)
        }
    }

    fun getInt(
        sp: SharedPreferences,
        pref: Prefs
    ): Int {
        val key = pref.key
        val default = pref.default as Int
        return try {
            sp.getInt(key, default)
        } catch (e: java.lang.ClassCastException) {
            sp.edit()
                .putInt(key, default)
                .apply()

            getInt(sp, pref)
        }
    }

    fun getFloat(
        sp: SharedPreferences,
        pref: Prefs
    ): Float {
        val key = pref.key
        val default = pref.default as Float
        return try {
            sp.getFloat(key, default)
        } catch (e: java.lang.ClassCastException) {
            sp.edit()
                .putFloat(key, default)
                .apply()

            getFloat(sp, pref)
        }
    }

    fun getBoolean(
        sp: SharedPreferences,
        pref: Prefs
    ): Boolean {
        val key = pref.key
        val default = pref.default as Boolean
        return try {
            sp.getBoolean(key, default)
        } catch (e: java.lang.ClassCastException) {
            sp.edit()
                .putBoolean(key, default)
                .apply()

            getBoolean(sp, pref)
        }
    }

    fun getLong(
        sp: SharedPreferences,
        pref: Prefs
    ): Long {
        val key = pref.key
        val default = pref.default as Long
        return try {
            sp.getLong(key, default)
        } catch (e: java.lang.ClassCastException) {
            sp.edit()
                .putLong(key, default)
                .apply()

            getLong(sp, pref)
        }
    }

}