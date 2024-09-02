package bassamalim.hidaya.core.utils

import android.content.Context
import android.util.Log
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

    suspend fun reviveDB(
        context: Context,
        suraFavorites: Map<Int, Boolean>,
        setSuraFavorites: suspend (Map<Int, Boolean>) -> Unit,
        reciterFavorites: Map<Int, Boolean>,
        setReciterFavorites: suspend (Map<Int, Boolean>) -> Unit,
        remembranceFavorites: Map<Int, Boolean>,
        setRemembranceFavorites: suspend (Map<Int, Boolean>) -> Unit,
        setLastDbVersion: suspend (Int) -> Unit,
    ) {
        context.deleteDatabase(DB_NAME)

        if (suraFavorites.isNotEmpty())
            setSuraFavorites(suraFavorites)

        if (reciterFavorites.isNotEmpty())
            setReciterFavorites(reciterFavorites)

        if (remembranceFavorites.isNotEmpty())
            setRemembranceFavorites(remembranceFavorites)

        setLastDbVersion(Global.DB_VERSION)

        Log.i(Global.TAG, "Database Revived")
    }

}