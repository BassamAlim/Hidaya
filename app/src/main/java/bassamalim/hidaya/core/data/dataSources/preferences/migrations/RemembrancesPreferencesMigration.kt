package bassamalim.hidaya.core.data.dataSources.preferences.migrations

import android.content.Context
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import bassamalim.hidaya.core.data.dataSources.preferences.Preference
import bassamalim.hidaya.core.data.dataSources.preferences.PreferencesFileNames
import bassamalim.hidaya.core.data.preferences.objects.RemembrancesPreferences
import com.google.gson.Gson
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentMapOf

object RemembrancesPreferencesMigration {

    fun getMigration(context: Context) =
        SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = bassamalim.hidaya.core.data.dataSources.preferences.PreferencesFileNames.REMEMBRANCES_PREFERENCES_NAME
        ) { sharedPrefs: SharedPreferencesView, currentData: RemembrancesPreferences ->
            currentData.copy(
                favorites = persistentMapOf<Int, Int>().mutate {
                    Gson().fromJson(
                        sharedPrefs.getString(
                            key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.RemembranceFavorites.key,
                            defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.RemembranceFavorites.default as String
                        )!!,
                        Array<Any>::class.java
                    ).mapIndexed { index, fav ->
                        index to (fav as Double).toInt()
                    }
                },
                textSize = sharedPrefs.getFloat(
                    key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.RemembrancesTextSize.key,
                    defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.RemembrancesTextSize.default as Float
                ),
            )
        }

}