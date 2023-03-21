package bassamalim.hidaya.core.data.database.dbs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "telawat_reciters")
class TelawatRecitersDB(
    @field:ColumnInfo(name = "reciter_id") @field:PrimaryKey val id: Int,
    @field:ColumnInfo(name = "reciter_name") val name: String?,
    @field:ColumnInfo(name = "favorite") val favorite: Int
)