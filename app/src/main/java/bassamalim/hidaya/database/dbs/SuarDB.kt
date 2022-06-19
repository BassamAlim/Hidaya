package bassamalim.hidaya.database.dbs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suar")
class SuarDB(
    @field:ColumnInfo(name = "sura_id") @field:PrimaryKey val sura_id: Int,
    @field:ColumnInfo(name = "sura_name") val sura_name: String?,
    @field:ColumnInfo(name = "sura_name_en") val sura_name_en: String?,
    @field:ColumnInfo(name = "search_name") val search_name: String?,
    @field:ColumnInfo(name = "search_name_en") val search_name_en: String?,
    @field:ColumnInfo(name = "tanzeel") val tanzeel: Int,
    @field:ColumnInfo(name = "start_page") val start_page: Int,
    @field:ColumnInfo(name = "favorite") val favorite: Int
)