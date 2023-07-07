package bassamalim.hidaya.core.data.database.dbs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BooksDB(
    @field:ColumnInfo(name = "id") @field:PrimaryKey val id: Int,
    @field:ColumnInfo(name = "title") val title: String,
    @field:ColumnInfo(name = "title_en") val titleEn: String,
    @field:ColumnInfo(name = "author") val author: String,
    @field:ColumnInfo(name = "author_en") val authorEn: String,
    @field:ColumnInfo(name = "url") val url: String,
    @field:ColumnInfo(name = "favorite") val favorite: Int
)