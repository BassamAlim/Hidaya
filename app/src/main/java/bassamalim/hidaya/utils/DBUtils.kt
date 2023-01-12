package bassamalim.hidaya.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.room.Room
import bassamalim.hidaya.Prefs
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.other.Global
import com.google.gson.Gson
import java.util.*

object DBUtils {

    fun getDB(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "HidayaDB")
            .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build()
    }

    fun testDB(
        context: Context,
        pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    ) {
        try {  // if there is a problem in the db it will cause an error
            getDB(context).suarDao().getFavs()
        } catch (e: Exception) {
            reviveDB(context)
        }

        val lastVer = PrefUtils.getInt(pref, Prefs.LastDBVersion)
        if (Global.dbVer > lastVer) reviveDB(context)
    }

    fun reviveDB(context: Context) {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        context.deleteDatabase("HidayaDB")

        val db = getDB(context)

        val surasJson = PrefUtils.getString(pref, Prefs.FavoriteSuras)
        val recitersJson = PrefUtils.getString(pref, Prefs.FavoriteReciters)
        val athkarJson = PrefUtils.getString(pref, Prefs.FavoriteAthkar)

        val gson = Gson()

        if (surasJson.isNotEmpty()) {
            val favSuras = gson.fromJson(surasJson, IntArray::class.java)
            for (i in favSuras.indices) db.suarDao().setFav(i, favSuras[i])
        }

        if (recitersJson.isNotEmpty()) {
            val favReciters: Array<Any> = gson.fromJson(recitersJson, Array<Any>::class.java)
            for (i in favReciters.indices) {
                val d = favReciters[i] as Double
                db.telawatRecitersDao().setFav(i, d.toInt())
            }
        }

        if (athkarJson.isNotEmpty()) {
            val favAthkar: Array<Any> = gson.fromJson(athkarJson, Array<Any>::class.java)
            for (i in favAthkar.indices) {
                val d = favAthkar[i] as Double
                db.athkarDao().setFav(i, d.toInt())
            }
        }

        pref.edit()
            .putInt(Prefs.LastDBVersion.key, Global.dbVer)
            .apply()

        Log.i(Global.TAG, "Database Revived")
    }

}