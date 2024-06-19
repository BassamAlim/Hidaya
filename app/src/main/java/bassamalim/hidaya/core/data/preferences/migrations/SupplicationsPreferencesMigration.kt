package bassamalim.hidaya.core.data.preferences.migrations

import android.content.Context
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesFileNames
import bassamalim.hidaya.core.data.preferences.dataStore.objects.SupplicationsPreferences
import com.google.gson.Gson
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentMapOf

object SupplicationsPreferencesMigration {

    fun getMigration(context: Context) =
        SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = PreferencesFileNames.SUPPLICATIONS_PREFERENCES_NAME,
        ) { sharedPrefs: SharedPreferencesView, currentData: SupplicationsPreferences ->
            currentData.copy(
                textSize = sharedPrefs.getFloat(
                    key = Preference.AthkarTextSize.key,
                    defValue = Preference.AthkarTextSize.default as Float
                ),
                favorites = persistentMapOf<Int, Int>().mutate {
                    Gson().fromJson(
                        sharedPrefs.getString(
                            key = Preference.FavoriteAthkar.key,
                            defValue = Preference.FavoriteAthkar.default as String
                        )!!,
                        Array<Any>::class.java
                    ).mapIndexed { index, fav ->
                        index to (fav as Double).toInt()
                    }
                },
            )
        }

}