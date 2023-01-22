package bassamalim.hidaya.repository

import android.content.SharedPreferences
import bassamalim.hidaya.Prefs
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.enum.Language
import bassamalim.hidaya.utils.PrefUtils
import javax.inject.Inject

class AthkarViewerRepo @Inject constructor(
    private val pref: SharedPreferences,
    private val db: AppDatabase
) {

    fun getLanguage() = PrefUtils.getLanguage(pref)

    fun getTextSize() = PrefUtils.getInt(pref, Prefs.AthkarTextSize)

    fun updateTextSize(textSize: Float) {
        pref.edit()
            .putInt(Prefs.AthkarTextSize.key, textSize.toInt())
            .apply()
    }

    fun getTitle(id: Int): String {
        return when(getLanguage()) {
            Language.ARABIC -> db.athkarDao().getName(id)
            Language.ENGLISH -> db.athkarDao().getNameEn(id)
        }
    }

    fun getThikrs(id: Int) = db.thikrsDao().getThikrs(id)

}