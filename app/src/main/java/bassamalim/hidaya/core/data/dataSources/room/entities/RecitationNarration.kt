package bassamalim.hidaya.core.data.dataSources.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "recitation_narrations",
    primaryKeys = ["reciter_id", "id"],
    foreignKeys = [ForeignKey(
        entity = SuraReciter::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("reciter_id"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.SET_DEFAULT
    )]
)
data class RecitationNarration(
    @field:ColumnInfo(name = "id") val id: Int,
    @field:ColumnInfo(name = "reciter_id") val reciterId: Int,
    @field:ColumnInfo(name = "name_ar") val nameAr: String,
    @field:ColumnInfo(name = "name_en") val nameEn: String,
    @field:ColumnInfo(name = "url") val url: String,
    @field:ColumnInfo(name = "available_suras") val availableSuras: String
)