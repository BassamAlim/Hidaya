package bassamalim.hidaya.core.data.preferences.dataStore.migrations

import android.content.Context
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.dataStore.PreferencesFileNames
import bassamalim.hidaya.core.data.preferences.dataStore.objects.AppSettingsPreferences
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.enums.TimeFormat

object AppSettingsPreferencesMigration {

    fun getMigration(context: Context) =
        SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = PreferencesFileNames.APP_SETTINGS_PREFERENCES_NAME
        ) { sharedPrefs: SharedPreferencesView, currentData: AppSettingsPreferences ->
            currentData.copy(
                language = Language.valueOf(
                    sharedPrefs.getString(
                        key = Preference.Language.key,
                        defValue = Preference.Language.default as String
                    )!!
                ),
                numeralsLanguage = Language.valueOf(
                    sharedPrefs.getString(
                        key = Preference.NumeralsLanguage.key,
                        defValue = Preference.NumeralsLanguage.default as String
                    )!!
                ),
                timeFormat = TimeFormat.valueOf(
                    sharedPrefs.getString(
                        key = Preference.TimeFormat.key,
                        defValue = Preference.TimeFormat.default as String
                    )!!
                ),
                theme = Theme.valueOf(
                    sharedPrefs.getString(
                        key = Preference.Theme.key,
                        defValue = Preference.Theme.default as String
                    )!!
                ),
                dateOffset = sharedPrefs.getInt(
                    key = Preference.DateOffset.key,
                    defValue = Preference.DateOffset.default as Int
                ),
            )
        }

}