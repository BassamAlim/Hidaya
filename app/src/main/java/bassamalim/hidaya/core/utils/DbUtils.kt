package bassamalim.hidaya.core.utils

import android.content.Context
import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.room.Room
import bassamalim.hidaya.core.data.dataSources.room.AppDatabase
import bassamalim.hidaya.core.other.Global
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

object DbUtils {

    suspend fun shouldReviveDb(
        lastDbVersion: Int,
        test: () -> Unit,
        dispatcher: CoroutineDispatcher
    ): Boolean {
        if (Global.DB_VERSION > lastDbVersion) return true

        return try {  // if there is a problem in the db it will cause an error
            withContext(dispatcher) {
                test()
            }
            false
        } catch (e: IllegalStateException) {
            Log.e(Global.TAG, "DB Error: ${e.message}")
            e.printStackTrace()
            true
        } catch (e: SQLiteException) {
            Log.e(Global.TAG, "DB Error: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    fun resetDB(context: Context) {
        context.deleteDatabase(Global.DB_NAME)
        Log.i(Global.TAG, "Database Deleted")

        Room.databaseBuilder(
            context = context,
            klass = AppDatabase::class.java,
            name = Global.DB_NAME
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