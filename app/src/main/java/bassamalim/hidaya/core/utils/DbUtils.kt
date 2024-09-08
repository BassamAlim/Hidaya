package bassamalim.hidaya.core.utils

import android.content.Context
import android.util.Log
import androidx.room.Room
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.other.Global

object DbUtils {

    private const val DB_NAME = "HidayaDB"

    fun shouldReviveDb(lastDbVersion: Int, test: () -> Unit): Boolean {
        if (Global.DB_VERSION > lastDbVersion) return true

        return try {  // if there is a problem in the db it will cause an error
            test()
            false
        } catch (e: Exception) {
            true
        }
    }

    fun resetDB(context: Context) {
        context.deleteDatabase(DB_NAME)
        Log.i(Global.TAG, "Database Deleted")

        Room.databaseBuilder(
            context = context.applicationContext,
            klass = AppDatabase::class.java,
            name = "HidayaDB"
        ).createFromAsset("databases/HidayaDB.db").build()
        Log.i(Global.TAG, "Database Revived")
    }

    suspend fun restoreDbData(
        suraFavorites: Map<Int, Boolean>,
        setSuraFavorites: suspend (Map<Int, Boolean>) -> Unit,
        reciterFavorites: Map<Int, Boolean>,
        setReciterFavorites: suspend (Map<Int, Boolean>) -> Unit,
        remembranceFavorites: Map<Int, Boolean>,
        setRemembranceFavorites: suspend (Map<Int, Boolean>) -> Unit,
    ) {
        if (suraFavorites.isNotEmpty())
            setSuraFavorites(suraFavorites)

        if (reciterFavorites.isNotEmpty())
            setReciterFavorites(reciterFavorites)

        if (remembranceFavorites.isNotEmpty())
            setRemembranceFavorites(remembranceFavorites)
    }

}