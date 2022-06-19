package bassamalim.hidaya.database.dbs

import androidx.annotation.NonNull
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
    @field:ColumnInfo(name = "rec_id") @field:NonNull private val rec_id: Int,
    @field:ColumnInfo(name = "rate") @field:NonNull private val rate: Int,
    @field:ColumnInfo(name = "source") private val source: String?
) {
    fun getRec_id(): Int {
        return rec_id
    }

    fun getRate(): Int {
        return rate
    }

    fun getSource(): String {
        return source!!
    }
}