package bassamalim.hidaya.database.dbs

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
class AyatTelawaDB(
    @field:ColumnInfo(name = "rec_id") private val recId: Int,
    @field:ColumnInfo(name = "rate") private val rate: Int,
    @field:ColumnInfo(name = "source") private val source: String?
) {
    fun getRecId(): Int {
        return recId
    }

    fun getRate(): Int {
        return rate
    }

    fun getSource(): String {
        return source!!
    }
}