package bassamalim.hidaya.core.utils

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.room.Room
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import bassamalim.hidaya.core.other.Global
import com.google.gson.Gson
import java.util.*

object DBUtils {

    fun getDB(context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "HidayaDB")
            .createFromAsset("databases/HidayaDB.db")
            .allowMainThreadQueries()
            .build()

    fun needsRevival(
        preferencesDS: PreferencesDataSource,
        db: AppDatabase
    ): Boolean {
        val lastVer = preferencesDS.getInt(Preference.LastDBVersion)
        if (Global.DB_VERSION > lastVer) return true

        return try {  // if there is a problem in the db it will cause an error
            db.surasDao().observeIsFavorites()
            false
        } catch (e: Exception) {
            true
        }
    }

    fun reviveDB(
        ctx: Context,
        preferencesDS: PreferencesDataSource = PreferencesDataSource(
            PreferenceManager.getDefaultSharedPreferences(ctx)
        ),
        db: AppDatabase = getDB(ctx)
    ) {
        ctx.deleteDatabase("HidayaDB")

        val suarJson = preferencesDS.getString(Preference.FavoriteSuras)
        val recitersJson = preferencesDS.getString(Preference.FavoriteReciters)
        val remembrancesJson = preferencesDS.getString(Preference.RemembranceFavorites)

        val gson = Gson()

        if (suarJson.isNotEmpty()) {
            val favSuar = gson.fromJson(suarJson, IntArray::class.java)
            for (i in favSuar.indices) db.surasDao().setIsFavorite(i, favSuar[i])
        }

        if (recitersJson.isNotEmpty()) {
            val favReciters: Array<Any> = gson.fromJson(recitersJson, Array<Any>::class.java)
            for (i in favReciters.indices) {
                val d = favReciters[i] as Double
                db.recitationRecitersDao().setIsFavorite(i, d.toInt())
            }
        }

        if (remembrancesJson.isNotEmpty()) {
            val favRemembrances: Array<Any> = gson.fromJson(remembrancesJson, Array<Any>::class.java)
            for (i in favRemembrances.indices) {
                val d = favRemembrances[i] as Double
                db.remembrancesDao().setIsFavorite(i, d.toInt())
            }
        }

        preferencesDS.setInt(Preference.LastDBVersion, Global.DB_VERSION)

        Log.i(Global.TAG, "Database Revived")
    }

}