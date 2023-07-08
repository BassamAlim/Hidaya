package bassamalim.hidaya.core.data.database.dbs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "ayat_telawa",
    primaryKeys = ["rec_id", "rate"],
    foreignKeys = [ForeignKey(
        entity = AyatRecitersDB::class,
        parentColumns = arrayOf("rec_id"),
        childColumns = arrayOf("rec_id"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.SET_DEFAULT
    )]
)
data class AyatTelawaDB(
    @field:ColumnInfo(name = "rec_id") val reciterId: Int,
    @field:ColumnInfo(name = "rate") val rate: Int,
    @field:ColumnInfo(name = "source") val source: String
)