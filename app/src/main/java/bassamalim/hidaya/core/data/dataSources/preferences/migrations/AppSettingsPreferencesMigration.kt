package bassamalim.hidaya.core.data.dataSources.preferences.migrations

import android.content.Context
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import bassamalim.hidaya.core.data.dataSources.preferences.Preference
import bassamalim.hidaya.core.data.dataSources.preferences.PreferencesFileNames
import bassamalim.hidaya.core.data.preferences.objects.AppSettingsPreferences
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.enums.TimeFormat

object AppSettingsPreferencesMigration {

    fun getMigration(context: Context) =
        SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = bassamalim.hidaya.core.data.dataSources.preferences.PreferencesFileNames.APP_SETTINGS_PREFERENCES_NAME
        ) { sharedPrefs: SharedPreferencesView, currentData: AppSettingsPreferences ->
            currentData.copy(
                language = Language.valueOf(
                    sharedPrefs.getString(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.Language.key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.Language.default as String
                    )!!
                ),
                numeralsLanguage = Language.valueOf(
                    sharedPrefs.getString(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.NumeralsLanguage.key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.NumeralsLanguage.default as String
                    )!!
                ),
                theme = Theme.valueOf(
                    sharedPrefs.getString(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.Theme.key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.Theme.default as String
                    )!!
                ),
                timeFormat = TimeFormat.valueOf(
                    sharedPrefs.getString(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.TimeFormat.key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.TimeFormat.default as String
                    )!!
                ),
                dateOffset = sharedPrefs.getInt(
                    key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.DateOffset.key,
                    defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.DateOffset.default as Int
                ),
            )
        }

}