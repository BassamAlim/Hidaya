package bassamalim.hidaya.core.data.preferences.migrations

import android.content.Context
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesFileNames
import bassamalim.hidaya.core.data.preferences.objects.AppStatePreferences

object AppStatePreferencesMigration {

    fun getMigration(context: Context) =
        SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = PreferencesFileNames.APP_STATE_PREFERENCES_NAME
        ) { sharedPrefs: SharedPreferencesView, currentData: AppStatePreferences ->
            currentData.copy(
                isOnboardingCompleted = !sharedPrefs.getBoolean(
                    key = Preference.FirstTime.key,
                    defValue = Preference.FirstTime.default as Boolean
                ),
                lastDailyUpdateMillis = 0,
                lastDBVersion = sharedPrefs.getInt(
                    key = Preference.LastDBVersion.key,
                    defValue =  Preference.LastDBVersion.default as Int
                ),
            )
        }

}