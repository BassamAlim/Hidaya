package bassamalim.hidaya.core.data.database.dbs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suar")
data class SuarDB(
    @field:ColumnInfo(name = "sura_id") @field:PrimaryKey val suraId: Int,
    @field:ColumnInfo(name = "sura_name") val suraName: String?,
    @field:ColumnInfo(name = "sura_name_en") val suraNameEn: String?,
    @field:ColumnInfo(name = "search_name") val searchName: String?,
    @field:ColumnInfo(name = "search_name_en") val searchNameEn: String?,
    @field:ColumnInfo(name = "tanzeel") val tanzeel: Int,
    @field:ColumnInfo(name = "start_page") val startPage: Int,
    @field:ColumnInfo(name = "favorite") val favorite: Int
)