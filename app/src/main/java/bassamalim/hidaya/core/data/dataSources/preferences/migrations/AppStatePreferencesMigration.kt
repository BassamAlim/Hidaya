package bassamalim.hidaya.core.data.dataSources.preferences.migrations

import android.content.Context
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import bassamalim.hidaya.core.data.dataSources.preferences.Preference
import bassamalim.hidaya.core.data.dataSources.preferences.PreferencesFileNames
import bassamalim.hidaya.core.data.preferences.objects.AppStatePreferences

object AppStatePreferencesMigration {

    fun getMigration(context: Context) =
        SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = bassamalim.hidaya.core.data.dataSources.preferences.PreferencesFileNames.APP_STATE_PREFERENCES_NAME
        ) { sharedPrefs: SharedPreferencesView, currentData: AppStatePreferences ->
            currentData.copy(
                isOnboardingCompleted = !sharedPrefs.getBoolean(
                    key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.FirstTime.key,
                    defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.FirstTime.default as Boolean
                ),
                lastDailyUpdateMillis = 0,
                lastDBVersion = sharedPrefs.getInt(
                    key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.LastDBVersion.key,
                    defValue =  bassamalim.hidaya.core.data.dataSources.preferences.Preference.LastDBVersion.default as Int
                ),
            )
        }

}