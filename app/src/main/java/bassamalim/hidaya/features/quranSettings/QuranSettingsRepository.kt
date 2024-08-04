package bassamalim.hidaya.features.quranSettings

import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import bassamalim.hidaya.features.quranReader.QuranViewType
import javax.inject.Inject

class QuranSettingsRepository @Inject constructor(
    private val preferencesDS: PreferencesDataSource,
    private val db: AppDatabase
) {

    fun getViewType() =
        QuranViewType.valueOf(preferencesDS.getString(Preference.QuranViewType))

    fun setViewType(type: QuranViewType) {
        preferencesDS.setString(Preference.QuranViewType, type.name)
    }

    fun getReciterNames() = db.ayatRecitersDao().getNames()

}