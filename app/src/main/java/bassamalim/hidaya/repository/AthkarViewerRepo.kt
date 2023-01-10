package bassamalim.hidaya.repository

import android.content.Context
import android.content.SharedPreferences
import bassamalim.hidaya.Prefs
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.ThikrsDB
import bassamalim.hidaya.enum.Language
import bassamalim.hidaya.utils.PrefUtils
import javax.inject.Inject

class AthkarViewerRepo @Inject constructor(
    private val context: Context,
    private val pref: SharedPreferences,
    private val db: AppDatabase
) {

    val language = PrefUtils.getLanguage(pref)

    fun getTextSize(): Int {
        return PrefUtils.getInt(pref, Prefs.AthkarTextSize.key, Prefs.AthkarTextSize.default as Int)
    }

    fun updateTextSize(textSize: Float) {
        pref.edit()
            .putInt(context.getString(R.string.athkar_text_size_key), textSize.toInt())
            .apply()
    }

    fun getTitle(id: Int): String {
        return when(language) {
            Language.ARABIC -> db.athkarDao().getName(id)
            Language.ENGLISH -> db.athkarDao().getNameEn(id)
        }
    }

    fun getThikrs(id: Int): List<ThikrsDB> {
        return db.thikrsDao().getThikrs(id)
    }

}