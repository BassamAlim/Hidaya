package bassamalim.hidaya.core.data.database.dbs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ayat_reciters")
data class AyatRecitersDB(
    @field:ColumnInfo(name = "rec_id") @field:PrimaryKey val rec_id: Int,
    @field:ColumnInfo(name = "rec_name") val rec_name: String?
)