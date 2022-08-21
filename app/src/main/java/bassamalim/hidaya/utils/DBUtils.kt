package bassamalim.hidaya.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.room.Room
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
            getDB(context).suarDao().getFav()
        } catch (e: Exception) {
            reviveDB(context)
        }

        val lastVer: Int = pref.getInt("last_db_version", 1)
        if (Global.dbVer > lastVer) reviveDB(context)
    }

    fun reviveDB(context: Context) {
        val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        context.deleteDatabase("HidayaDB")

        val db = getDB(context)

        val surasJson: String? = pref.getString("favorite_suras", "")
        val recitersJson: String? = pref.getString("favorite_reciters", "")
        val athkarJson: String? = pref.getString("favorite_athkar", "")

        val gson = Gson()

        if (surasJson!!.isNotEmpty()) {
            val favSuras: Array<Any> = gson.fromJson(surasJson, Array<Any>::class.java)
            for (i in favSuras.indices) {
                val d = favSuras[i] as Double
                db.suarDao().setFav(i, d.toInt())
            }
        }

        if (recitersJson!!.isNotEmpty()) {
            val favReciters: Array<Any> = gson.fromJson(recitersJson, Array<Any>::class.java)
            for (i in favReciters.indices) {
                val d = favReciters[i] as Double
                db.telawatRecitersDao().setFav(i, d.toInt())
            }
        }

        if (athkarJson!!.isNotEmpty()) {
            val favAthkar: Array<Any> = gson.fromJson(athkarJson, Array<Any>::class.java)
            for (i in favAthkar.indices) {
                val d = favAthkar[i] as Double
                db.athkarDao().setFav(i, d.toInt())
            }
        }

        val editor: SharedPreferences.Editor = pref.edit()
        editor.putInt("last_db_version", Global.dbVer)
        editor.apply()

        Log.i(Global.TAG, "Database Revived")
    }

}