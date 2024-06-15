package bassamalim.hidaya.core.data.preferences

import android.content.SharedPreferences
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.enums.TimeFormat

class PreferencesDataSource(
    private val sharedPreferences: SharedPreferences
) {

    fun getLanguage() =
        Language.valueOf(getString(Preference.Language))

    fun getNumeralsLanguage() =
        Language.valueOf(getString(Preference.NumeralsLanguage))

    fun getTheme() =
        Theme.valueOf(getString(Preference.Theme))

    fun getTimeFormat() =
        TimeFormat.valueOf(getString(Preference.TimeFormat))

    /**
     * Created as a fail safe in case the data type of the preference is changed
     * "If the value at the given key is a string, return it, otherwise, set the value at the given key
     * to the given default value and return the default value."
     *
     *
     * @param preference an object that identifies a preference, contains key, default
     * @return The value with the key in the SharedPreferences object.
     */

    fun getString(preference: Preference): String {
        val key = preference.key
        val default = preference.default as String
        return try {
            sharedPreferences.getString(key, default)!!
        } catch (e: java.lang.ClassCastException) {
            setString(preference, default)
            getString(preference)
        }
    }

    fun setString(preference: Preference, value: String) {
        sharedPreferences.edit()
            .putString(preference.key, value)
            .apply()
    }

    fun getInt(preference: Preference): Int {
        val key = preference.key
        val default = preference.default as Int
        return try {
            sharedPreferences.getInt(key, default)
        } catch (e: java.lang.ClassCastException) {
            setInt(preference, default)
            getInt(preference)
        }
    }

    fun setInt(preference: Preference, value: Int) {
        sharedPreferences.edit()
            .putInt(preference.key, value)
            .apply()
    }

    fun getFloat(preference: Preference): Float {
        val key = preference.key
        val default = preference.default as Float
        return try {
            sharedPreferences.getFloat(key, default)
        } catch (e: java.lang.ClassCastException) {
            setFloat(preference, default)
            getFloat(preference)
        }
    }

    fun setFloat(preference: Preference, value: Float) {
        sharedPreferences.edit()
            .putFloat(preference.key, value)
            .apply()
    }

    fun getBoolean(preference: Preference): Boolean {
        val key = preference.key
        val default = preference.default as Boolean
        return try {
            sharedPreferences.getBoolean(key, default)
        } catch (e: java.lang.ClassCastException) {
            setBoolean(preference, default)
            getBoolean(preference)
        }
    }

    fun setBoolean(preference: Preference, value: Boolean) {
        sharedPreferences.edit()
            .putBoolean(preference.key, value)
            .apply()
    }

    fun getLong(preference: Preference): Long {
        val key = preference.key
        val default = preference.default as Long
        return try {
            sharedPreferences.getLong(key, default)
        } catch (e: java.lang.ClassCastException) {
            setLong(preference, default)
            getLong(preference)
        }
    }

    fun setLong(preference: Preference, value: Long) {
        sharedPreferences.edit()
            .putLong(preference.key, value)
            .apply()
    }

}