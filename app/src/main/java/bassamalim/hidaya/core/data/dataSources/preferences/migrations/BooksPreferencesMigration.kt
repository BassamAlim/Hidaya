package bassamalim.hidaya.core.data.dataSources.preferences.migrations

import android.content.Context
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import bassamalim.hidaya.core.data.dataSources.preferences.Preference
import bassamalim.hidaya.core.data.dataSources.preferences.PreferencesFileNames
import bassamalim.hidaya.core.data.preferences.objects.BooksPreferences
import kotlinx.collections.immutable.persistentMapOf

object BooksPreferencesMigration {

    fun getMigration(context: Context) =
        SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = bassamalim.hidaya.core.data.dataSources.preferences.PreferencesFileNames.BOOKS_PREFERENCES_NAME
        ) { sharedPrefs: SharedPreferencesView, currentData: BooksPreferences ->
            currentData.copy(
                chapterFavorites = persistentMapOf(),
                textSize = sharedPrefs.getFloat(
                    key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.BooksTextSize.key,
                    defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.BooksTextSize.default as Float
                ),
                searchSelections = persistentMapOf(),
                searchMaxMatches = 10,
                shouldShowTutorial = sharedPrefs.getBoolean(
                    key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.ShowBooksTutorial.key,
                    defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.ShowBooksTutorial.default as Boolean
                ),
            )
        }

}