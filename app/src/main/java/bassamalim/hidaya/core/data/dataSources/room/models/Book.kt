package bassamalim.hidaya.core.data.dataSources.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @field:ColumnInfo(name = "id") @field:PrimaryKey val id: Int,
    @field:ColumnInfo(name = "title_ar") val titleAr: String,
    @field:ColumnInfo(name = "title_en") val titleEn: String,
    @field:ColumnInfo(name = "author_ar") val authorAr: String,
    @field:ColumnInfo(name = "author_en") val authorEn: String,
    @field:ColumnInfo(name = "url") val url: String,
    @field:ColumnInfo(name = "is_favorite") val isFavorite: Int
)