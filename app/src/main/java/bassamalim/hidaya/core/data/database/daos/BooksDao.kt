package bassamalim.hidaya.core.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.database.models.Book

@Dao
interface BooksDao {

    @Query("SELECT * FROM books")
    fun getAll(): List<Book>

    @Query("SELECT title_ar FROM books")
    fun getTitlesAr(): List<String>

    @Query("SELECT title_en FROM books")
    fun getTitlesEn(): List<String>

}