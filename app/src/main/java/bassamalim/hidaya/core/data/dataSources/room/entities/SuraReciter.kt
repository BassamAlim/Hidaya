package bassamalim.hidaya.core.data.dataSources.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sura_reciters")
data class SuraReciter(
    @field:ColumnInfo(name = "id") @field:PrimaryKey val id: Int,
    @field:ColumnInfo(name = "name_ar") val nameAr: String,
    @field:ColumnInfo(name = "name_en") val nameEn: String,
    @field:ColumnInfo(name = "is_favorite") val isFavorite: Int
)