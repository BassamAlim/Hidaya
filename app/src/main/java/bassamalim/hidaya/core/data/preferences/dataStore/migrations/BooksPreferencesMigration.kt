package bassamalim.hidaya.core.data.preferences.dataStore.migrations

import android.content.Context
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.dataStore.PreferencesFileNames
import bassamalim.hidaya.core.data.preferences.dataStore.objects.BooksPreferences
import kotlinx.collections.immutable.persistentMapOf

object BooksPreferencesMigration {

    fun getMigration(context: Context) =
        SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = PreferencesFileNames.BOOKS_PREFERENCES_NAME,
        ) { sharedPrefs: SharedPreferencesView, currentData: BooksPreferences ->
            currentData.copy(
                textSize = sharedPrefs.getFloat(
                    key = Preference.BooksTextSize.key,
                    defValue = Preference.BooksTextSize.default as Float
                ),
                searcherMaxMatches = 10,
                chaptersFavs = persistentMapOf(),
                shouldShowTutorial = sharedPrefs.getBoolean(
                    key = Preference.ShowBooksTutorial.key,
                    defValue = Preference.ShowBooksTutorial.default as Boolean
                ),
                searchSelections = persistentMapOf(),
            )
        }

}