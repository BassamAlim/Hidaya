package bassamalim.hidaya.core.data.database.dbs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "telawat_versions",
    primaryKeys = ["reciter_id", "version_id"],
    foreignKeys = [ForeignKey(
        entity = TelawatRecitersDB::class,
        parentColumns = arrayOf("reciter_id"),
        childColumns = arrayOf("reciter_id"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.SET_DEFAULT
    )]
)
class TelawatVersionsDB(
    @field:ColumnInfo(name = "reciter_id") private val reciterId: Int,
    @field:ColumnInfo(name = "version_id") private val versionId: Int,
    @field:ColumnInfo(name = "rewaya") private val rewaya: String?,
    @field:ColumnInfo(name = "url") private val url: String?,
    @field:ColumnInfo(name = "count") private val count: Int,
    @field:ColumnInfo(name = "suras") private val suras: String?
) {
    fun getReciterId(): Int {
        return reciterId
    }

    fun getVersionId(): Int {
        return versionId
    }

    fun getRewaya(): String {
        return rewaya!!
    }

    fun getUrl(): String {
        return url!!
    }

    fun getCount(): Int {
        return count
    }

    fun getSuras(): String {
        return suras!!
    }
}