package bassamalim.hidaya.features.athkarViewer

import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import bassamalim.hidaya.core.enums.Language
import javax.inject.Inject

class AthkarViewerRepo @Inject constructor(
    private val preferencesDS: PreferencesDataSource,
    private val db: AppDatabase
) {

    fun getLanguage() = preferencesDS.getLanguage()

    fun getTextSize() = preferencesDS.getFloat(Preference.AthkarTextSize)

    fun setTextSize(textSize: Float) {
        preferencesDS.setInt(Preference.AthkarTextSize, textSize.toInt())
    }

    fun getTitle(id: Int): String =
        when(getLanguage()) {
            Language.ARABIC -> db.athkarDao().getName(id)
            Language.ENGLISH -> db.athkarDao().getNameEn(id)
        }

    fun getThikrParts(id: Int) = db.athkarPartsDao().getThikrParts(id)

}