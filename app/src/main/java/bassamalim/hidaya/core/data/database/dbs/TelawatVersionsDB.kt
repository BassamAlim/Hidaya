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
data class TelawatVersionsDB(
    @field:ColumnInfo(name = "reciter_id") val reciterId: Int,
    @field:ColumnInfo(name = "version_id") val versionId: Int,
    @field:ColumnInfo(name = "rewaya") val rewaya: String?,
    @field:ColumnInfo(name = "url") val url: String?,
    @field:ColumnInfo(name = "count") val count: Int,
    @field:ColumnInfo(name = "suar") val suar: String?
)