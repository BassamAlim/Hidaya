package bassamalim.hidaya.core.utils

import android.content.Context
import android.util.Log
import androidx.room.Room
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.other.Global

object DBUtils {

    private const val DB_NAME = "HidayaDB"

    fun getDB(context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
            .createFromAsset("databases/HidayaDB.db")
            .allowMainThreadQueries()
            .build()

    fun needsRevival(
        lastDbVersion: Int,
        test: () -> Unit,
    ): Boolean {
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
        favoriteSuraMap: Map<Int, Boolean>,
        setFavoriteSuraMap: suspend (Map<Int, Boolean>) -> Unit,
        favoriteReciterMap: Map<Int, Boolean>,
        setFavoriteReciterMap: suspend (Map<Int, Boolean>) -> Unit,
        favoriteRemembranceMap: Map<Int, Boolean>,
        setFavoriteRemembranceMap: suspend (Map<Int, Boolean>) -> Unit,
        setLastDbVersion: suspend (Int) -> Unit,
    ) {
        context.deleteDatabase(DB_NAME)

        if (favoriteSuraMap.isNotEmpty())
            setFavoriteSuraMap(favoriteSuraMap)

        if (favoriteReciterMap.isNotEmpty())
            setFavoriteReciterMap(favoriteReciterMap)

        if (favoriteRemembranceMap.isNotEmpty())
            setFavoriteRemembranceMap(favoriteRemembranceMap)

        setLastDbVersion(Global.DB_VERSION)

        Log.i(Global.TAG, "Database Revived")
    }

}