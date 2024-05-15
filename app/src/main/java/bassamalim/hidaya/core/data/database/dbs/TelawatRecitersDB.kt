package bassamalim.hidaya.core.data.database.dbs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "telawat_reciters")
data class TelawatRecitersDB(
    @field:ColumnInfo(name = "id") @field:PrimaryKey val id: Int,
    @field:ColumnInfo(name = "name_ar") val name: String,
    @field:ColumnInfo(name = "name_en") val nameEn: String,
    @field:ColumnInfo(name = "favorite") val favorite: Int
)