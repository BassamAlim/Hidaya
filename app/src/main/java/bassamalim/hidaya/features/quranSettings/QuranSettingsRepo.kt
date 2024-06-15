package bassamalim.hidaya.features.quranSettings

import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import bassamalim.hidaya.core.enums.QuranViewTypes
import javax.inject.Inject

class QuranSettingsRepo @Inject constructor(
    private val preferencesDS: PreferencesDataSource,
    private val db: AppDatabase
) {

    fun getViewType() =
        QuranViewTypes.valueOf(preferencesDS.getString(Preference.QuranViewType))

    fun setViewType(type: QuranViewTypes) {
        preferencesDS.setString(Preference.QuranViewType, type.name)
    }

    fun getReciterNames() = db.ayatRecitersDao().getNames()

}