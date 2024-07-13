package bassamalim.hidaya.core.data.preferences.migrations

import android.content.Context
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesFileNames
import bassamalim.hidaya.core.data.preferences.objects.BooksPreferences
import kotlinx.collections.immutable.persistentMapOf

object BooksPreferencesMigration {

    fun getMigration(context: Context) =
        SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = PreferencesFileNames.BOOKS_PREFERENCES_NAME
        ) { sharedPrefs: SharedPreferencesView, currentData: BooksPreferences ->
            currentData.copy(
                chapterFavorites = persistentMapOf(),
                textSize = sharedPrefs.getFloat(
                    key = Preference.BooksTextSize.key,
                    defValue = Preference.BooksTextSize.default as Float
                ),
                searchSelections = persistentMapOf(),
                searchMaxMatches = 10,
                shouldShowTutorial = sharedPrefs.getBoolean(
                    key = Preference.ShowBooksTutorial.key,
                    defValue = Preference.ShowBooksTutorial.default as Boolean
                ),
            )
        }

}