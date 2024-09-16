package bassamalim.hidaya.core.data.dataSources.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suras")
data class Sura(
    @field:ColumnInfo(name = "id") @field:PrimaryKey val id: Int,
    @field:ColumnInfo(name = "decorated_name_ar") val decoratedNameAr: String,
    @field:ColumnInfo(name = "decorated_name_en") val decoratedNameEn: String,
    @field:ColumnInfo(name = "plain_name_ar") val plainNameAr: String,
    @field:ColumnInfo(name = "plain_name_en") val plainNameEn: String?,
    @field:ColumnInfo(name = "revelation") val revelation: Int,
    @field:ColumnInfo(name = "start_page") val startPage: Int,
    @field:ColumnInfo(name = "is_favorite") val isFavorite: Int
)