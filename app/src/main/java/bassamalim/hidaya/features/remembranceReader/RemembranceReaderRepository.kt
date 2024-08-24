package bassamalim.hidaya.features.remembranceReader

import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import bassamalim.hidaya.core.enums.Language
import javax.inject.Inject

class RemembranceReaderRepository @Inject constructor(
    private val preferencesDS: PreferencesDataSource,
    private val db: AppDatabase
) {

    fun getLanguage() = preferencesDS.getLanguage()

    fun getTextSize() = preferencesDS.getFloat(Preference.RemembrancesTextSize)

    fun setTextSize(textSize: Float) {
        preferencesDS.setInt(Preference.RemembrancesTextSize, textSize.toInt())
    }

    fun getTitle(id: Int): String =
        when(getLanguage()) {
            Language.ARABIC -> db.remembrancesDao().getNameAr(id)
            Language.ENGLISH -> db.remembrancesDao().getNameEn(id)
        }

    fun getRemembrancePassages(id: Int) = db.remembrancePassagesDao().getRemembrancePassages(id)

}