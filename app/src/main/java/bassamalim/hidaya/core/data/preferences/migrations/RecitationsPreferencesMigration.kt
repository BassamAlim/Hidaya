package bassamalim.hidaya.core.data.preferences.migrations

import android.content.Context
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesFileNames
import bassamalim.hidaya.core.data.preferences.objects.RecitationsPreferences
import com.google.gson.Gson
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentMapOf

object RecitationsPreferencesMigration {

    fun getMigration(context: Context) =
        SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = PreferencesFileNames.RECITATIONS_PREFERENCES_NAME
        ) { sharedPrefs: SharedPreferencesView, currentData: RecitationsPreferences ->
            currentData.copy(
                reciterFavorites = persistentMapOf<Int, Int>().mutate {
                    Gson().fromJson(
                        sharedPrefs.getString(
                            key = Preference.FavoriteReciters.key,
                            defValue = Preference.FavoriteReciters.default as String
                        )!!,
                        Array<Any>::class.java
                    ).mapIndexed { index, fav ->
                        index to (fav as Double).toInt()
                    }
                },
                narrationSelections = persistentMapOf<Int, Boolean>().mutate {
                    Gson().fromJson(
                        sharedPrefs.getString(
                            key = Preference.SelectedRewayat.key,
                            defValue = Preference.SelectedRewayat.default as String
                        )!!,
                        BooleanArray::class.java
                    ).mapIndexed { index, selected ->
                        index to selected
                    }
                },
                repeatMode = sharedPrefs.getInt(
                    key = Preference.TelawatRepeatMode.key,
                    defValue = Preference.TelawatRepeatMode.default as Int
                ),
                shuffleMode = sharedPrefs.getInt(
                    key = Preference.TelawatShuffleMode.key,
                    defValue = Preference.TelawatShuffleMode.default as Int
                ),
                lastPlayedMediaId = sharedPrefs.getString(
                    key = Preference.LastPlayedMediaId.key,
                    defValue = Preference.LastPlayedMediaId.default as String
                )!!,
                lastProgress = sharedPrefs.getInt(
                    key = Preference.LastTelawaProgress.key,
                    defValue = Preference.LastTelawaProgress.default as Int
                ).toLong(),
            )
        }

}