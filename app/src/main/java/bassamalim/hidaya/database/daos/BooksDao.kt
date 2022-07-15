package bassamalim.hidaya.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.database.dbs.BooksDB

@Dao
interface BooksDao {

    @Query("SELECT * FROM books")
    fun getAll(): List<BooksDB>

}