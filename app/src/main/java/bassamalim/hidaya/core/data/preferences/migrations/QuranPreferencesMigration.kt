package bassamalim.hidaya.core.data.preferences.migrations

import android.content.Context
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesFileNames
import bassamalim.hidaya.core.data.preferences.objects.QuranPreferences
import bassamalim.hidaya.core.enums.AyaRepeat
import bassamalim.hidaya.core.enums.QuranViewType
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
                suraFavorites = persistentMapOf<Int, Int>().mutate {
                    Gson().fromJson(
                        sharedPrefs.getString(
                            key = Preference.FavoriteSuar.key,
                            defValue = Preference.FavoriteSuar.default as String
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
                ayaReciterId = sharedPrefs.getString(
                    key = Preference.AyaReciter.key,
                    defValue = Preference.AyaReciter.default as String
                )!!.toInt(),
                ayaRepeat = AyaRepeat.NONE,
                shouldStopOnPageEnd = sharedPrefs.getBoolean(
                    key = Preference.StopOnPageEnd.key,
                    defValue = Preference.StopOnPageEnd.default as Boolean
                ),
                shouldStopOnSuraEnd = sharedPrefs.getBoolean(
                    key = Preference.StopOnSuraEnd.key,
                    defValue = Preference.StopOnSuraEnd.default as Boolean
                ),
                bookmarkedPage = sharedPrefs.getInt(
                    key = Preference.BookmarkedPage.key,
                    defValue = Preference.BookmarkedPage.default as Int
                ),
                bookmarkedSura = sharedPrefs.getInt(
                    key = Preference.BookmarkedSura.key,
                    defValue = Preference.BookmarkedSura.default as Int
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
                werdPage = sharedPrefs.getInt(
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