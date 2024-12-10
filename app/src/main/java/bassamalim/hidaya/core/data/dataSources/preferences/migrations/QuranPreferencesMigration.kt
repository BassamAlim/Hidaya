package bassamalim.hidaya.core.data.dataSources.preferences.migrations

import android.content.Context
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import bassamalim.hidaya.core.data.dataSources.preferences.Preference
import bassamalim.hidaya.core.data.dataSources.preferences.PreferencesFileNames
import bassamalim.hidaya.core.data.dataSources.preferences.objects.QuranPreferences
import bassamalim.hidaya.features.quran.reader.ui.QuranViewType
import com.google.gson.Gson
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentMapOf

object QuranPreferencesMigration {

    fun getMigration(context: Context) =
        SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = PreferencesFileNames.QURAN_PREFERENCES_NAME
        ) { sharedPrefs: SharedPreferencesView, currentData: QuranPreferences ->
            currentData.copy(
                suraFavorites = persistentMapOf<Int, Boolean>().mutate {
                    Gson().fromJson(
                        sharedPrefs.getString(
                            key = Preference.FavoriteSuras.key,
                            defValue = Preference.FavoriteSuras.default as String
                        )!!,
                        IntArray::class.java
                    ).mapIndexed { index, fav -> index to fav }
                },
                viewType = QuranViewType.valueOf(
                    sharedPrefs.getString(
                        key = Preference.QuranViewType.key,
                        defValue = Preference.QuranViewType.default as String
                    )!!
                ),
                textSize = sharedPrefs.getFloat(
                    key = Preference.QuranTextSize.key,
                    defValue = Preference.QuranTextSize.default as Float
                ),
                searchMaxMatches = 10,
                shouldShowMenuTutorial = sharedPrefs.getBoolean(
                    key = Preference.ShowQuranTutorial.key,
                    defValue = Preference.ShowQuranTutorial.default as Boolean
                ),
                shouldShowReaderTutorial = sharedPrefs.getBoolean(
                    key = Preference.ShowQuranViewerTutorial.key,
                    defValue = Preference.ShowQuranViewerTutorial.default as Boolean
                ),
                werdPageNum = sharedPrefs.getInt(
                    key = Preference.WerdPage.key,
                    defValue = Preference.WerdPage.default as Int
                ),
                isWerdDone = sharedPrefs.getBoolean(
                    key = Preference.WerdDone.key,
                    defValue = Preference.WerdDone.default as Boolean
                ),
            )
        }

}