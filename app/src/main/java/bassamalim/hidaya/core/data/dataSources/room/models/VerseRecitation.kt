package bassamalim.hidaya.core.data.dataSources.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "verse_recitations",
    primaryKeys = ["reciter_id", "bitrate"],
    foreignKeys = [ForeignKey(
        entity = VerseReciter::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("reciter_id"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.SET_DEFAULT
    )]
)
data class VerseRecitation(
    @field:ColumnInfo(name = "reciter_id") val reciterId: Int,
    @field:ColumnInfo(name = "bitrate") val bitrate: Int,
    @field:ColumnInfo(name = "source") val source: String
)