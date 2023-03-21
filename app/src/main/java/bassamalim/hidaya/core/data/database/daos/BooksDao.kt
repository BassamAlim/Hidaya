package bassamalim.hidaya.core.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.database.dbs.BooksDB

@Dao
interface BooksDao {

    @Query("SELECT * FROM books")
    fun getAll(): List<BooksDB>

    @Query("SELECT title FROM books")
    fun getTitles(): List<String>

    @Query("SELECT title_en FROM books")
    fun getTitlesEn(): List<String>

}