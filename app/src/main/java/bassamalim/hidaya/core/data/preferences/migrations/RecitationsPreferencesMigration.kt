package bassamalim.hidaya.core.data.preferences.migrations

import android.content.Context
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesFileNames
import bassamalim.hidaya.core.data.preferences.objects.RecitationsPreferences
import bassamalim.hidaya.core.enums.VerseRepeatMode
import bassamalim.hidaya.features.recitations.recitationRecitersMenu.domain.LastPlayedRecitation
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
                            key = Preference.SelectedNarrations.key,
                            defValue = Preference.SelectedNarrations.default as String
                        )!!,
                        BooleanArray::class.java
                    ).mapIndexed { index, selected ->
                        index to selected
                    }
                },
                repeatMode = sharedPrefs.getInt(
                    key = Preference.RecitationsRepeatMode.key,
                    defValue = Preference.RecitationsRepeatMode.default as Int
                ),
                shuffleMode = sharedPrefs.getInt(
                    key = Preference.RecitationsShuffleMode.key,
                    defValue = Preference.RecitationsShuffleMode.default as Int
                ),
                lastPlayed = LastPlayedRecitation(
                    mediaId = sharedPrefs.getString(
                        key = Preference.LastPlayedMediaId.key,
                        defValue = Preference.LastPlayedMediaId.default as String
                    )!!,
                    progress = sharedPrefs.getInt(
                        key = Preference.LastRecitationProgress.key,
                        defValue = Preference.LastRecitationProgress.default as Int
                    ).toLong()
                ),
                verseReciterId = sharedPrefs.getString(
                    key = Preference.AyaReciter.key,
                    defValue = Preference.AyaReciter.default as String
                )!!.toInt(),
                verseRepeatMode = VerseRepeatMode.NONE,
                shouldStopOnPageEnd = sharedPrefs.getBoolean(
                    key = Preference.StopOnPageEnd.key,
                    defValue = Preference.StopOnPageEnd.default as Boolean
                ),
                shouldStopOnSuraEnd = sharedPrefs.getBoolean(
                    key = Preference.StopOnSuraEnd.key,
                    defValue = Preference.StopOnSuraEnd.default as Boolean
                ),
            )
        }

}