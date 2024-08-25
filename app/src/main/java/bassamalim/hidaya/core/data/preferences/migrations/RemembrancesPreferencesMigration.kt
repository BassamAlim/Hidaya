package bassamalim.hidaya.core.data.preferences.migrations

import android.content.Context
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesFileNames
import bassamalim.hidaya.core.data.preferences.objects.RemembrancesPreferences
import com.google.gson.Gson
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentMapOf

object RemembrancesPreferencesMigration {

    fun getMigration(context: Context) =
        SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = PreferencesFileNames.REMEMBRANCES_PREFERENCES_NAME
        ) { sharedPrefs: SharedPreferencesView, currentData: RemembrancesPreferences ->
            currentData.copy(
                favorites = persistentMapOf<Int, Int>().mutate {
                    Gson().fromJson(
                        sharedPrefs.getString(
                            key = Preference.RemembranceFavorites.key,
                            defValue = Preference.RemembranceFavorites.default as String
                        )!!,
                        Array<Any>::class.java
                    ).mapIndexed { index, fav ->
                        index to (fav as Double).toInt()
                    }
                },
                textSize = sharedPrefs.getFloat(
                    key = Preference.RemembrancesTextSize.key,
                    defValue = Preference.RemembrancesTextSize.default as Float
                ),
            )
        }

}