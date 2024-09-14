package bassamalim.hidaya.core.data.dataSources.preferences.migrations

import android.content.Context
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import bassamalim.hidaya.core.data.dataSources.preferences.Preference
import bassamalim.hidaya.core.data.dataSources.preferences.PreferencesFileNames
import bassamalim.hidaya.core.data.preferences.objects.QuranPreferences
import bassamalim.hidaya.core.models.QuranPageBookmark
import bassamalim.hidaya.features.quran.reader.ui.QuranViewType
import com.google.gson.Gson
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentMapOf

object QuranPreferencesMigration {

    fun getMigration(context: Context) =
        SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = bassamalim.hidaya.core.data.dataSources.preferences.PreferencesFileNames.QURAN_PREFERENCES_NAME
        ) { sharedPrefs: SharedPreferencesView, currentData: QuranPreferences ->
            currentData.copy(
                suraFavorites = persistentMapOf<Int, Boolean>().mutate {
                    Gson().fromJson(
                        sharedPrefs.getString(
                            key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.FavoriteSuras.key,
                            defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.FavoriteSuras.default as String
                        )!!,
                        IntArray::class.java
                    ).mapIndexed { index, fav -> index to fav }
                },
                viewType = QuranViewType.valueOf(
                    sharedPrefs.getString(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.QuranViewType.key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.QuranViewType.default as String
                    )!!
                ),
                textSize = sharedPrefs.getFloat(
                    key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.QuranTextSize.key,
                    defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.QuranTextSize.default as Float
                ),
                pageBookmark = QuranPageBookmark(
                    pageNum = sharedPrefs.getInt(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.BookmarkedPage.key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.BookmarkedPage.default as Int
                    ),
                    suraId = sharedPrefs.getInt(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.BookmarkedSura.key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.BookmarkedSura.default as Int
                    )
                ),
                searchMaxMatches = 10,
                shouldShowMenuTutorial = sharedPrefs.getBoolean(
                    key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.ShowQuranTutorial.key,
                    defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.ShowQuranTutorial.default as Boolean
                ),
                shouldShowReaderTutorial = sharedPrefs.getBoolean(
                    key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.ShowQuranViewerTutorial.key,
                    defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.ShowQuranViewerTutorial.default as Boolean
                ),
                werdPage = sharedPrefs.getInt(
                    key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.WerdPage.key,
                    defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.WerdPage.default as Int
                ),
                isWerdDone = sharedPrefs.getBoolean(
                    key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.WerdDone.key,
                    defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.WerdDone.default as Boolean
                ),
            )
        }

}