package bassamalim.hidaya.core.data.dataSources.preferences.migrations

import android.content.Context
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import bassamalim.hidaya.core.data.preferences.objects.RecitationsPreferences
import bassamalim.hidaya.core.enums.VerseRepeatMode
import bassamalim.hidaya.features.recitations.recitersMenu.domain.LastPlayedMedia
import com.google.gson.Gson
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentMapOf

object RecitationsPreferencesMigration {

    fun getMigration(context: Context) =
        SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = bassamalim.hidaya.core.data.dataSources.preferences.PreferencesFileNames.RECITATIONS_PREFERENCES_NAME
        ) { sharedPrefs: SharedPreferencesView, currentData: RecitationsPreferences ->
            currentData.copy(
                reciterFavorites = persistentMapOf<Int, Boolean>().mutate {
                    Gson().fromJson(
                        sharedPrefs.getString(
                            key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.FavoriteReciters.key,
                            defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.FavoriteReciters.default as String
                        )!!,
                        Array<Any>::class.java
                    ).mapIndexed { index, fav ->
                        index to ((fav as Double).toInt() == 1)
                    }
                },
                narrationSelections = persistentMapOf<Int, Boolean>().mutate {
                    Gson().fromJson(
                        sharedPrefs.getString(
                            key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.SelectedNarrations.key,
                            defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.SelectedNarrations.default as String
                        )!!,
                        BooleanArray::class.java
                    ).mapIndexed { index, selected ->
                        index to selected
                    }
                },
                repeatMode = sharedPrefs.getInt(
                    key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.RecitationsRepeatMode.key,
                    defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.RecitationsRepeatMode.default as Int
                ),
                shuffleMode = sharedPrefs.getInt(
                    key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.RecitationsShuffleMode.key,
                    defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.RecitationsShuffleMode.default as Int
                ),
                lastPlayedMedia = LastPlayedMedia(
                    mediaId = sharedPrefs.getString(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.LastPlayedMediaId.key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.LastPlayedMediaId.default as String
                    )!!,
                    progress = sharedPrefs.getInt(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.LastRecitationProgress.key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.LastRecitationProgress.default as Int
                    ).toLong()
                ),
                verseReciterId = sharedPrefs.getString(
                    key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.VerseReciter.key,
                    defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.VerseReciter.default as String
                )!!.toInt(),
                verseRepeatMode = VerseRepeatMode.NONE,
                shouldStopOnPageEnd = sharedPrefs.getBoolean(
                    key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.StopOnPageEnd.key,
                    defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.StopOnPageEnd.default as Boolean
                ),
                shouldStopOnSuraEnd = sharedPrefs.getBoolean(
                    key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.StopOnSuraEnd.key,
                    defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.StopOnSuraEnd.default as Boolean
                ),
            )
        }

}