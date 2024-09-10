package bassamalim.hidaya.core.data.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "remembrance_passages",
    primaryKeys = ["id", "remembrance_id"],
    foreignKeys = [
        ForeignKey(
            entity = Remembrance::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("remembrance_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.SET_DEFAULT
        )
    ]
)
data class RemembrancePassage(
    @field:ColumnInfo(name = "id") val id: Int,
    @field:ColumnInfo(name = "remembrance_id") val remembranceId: Int,
    @field:ColumnInfo(name = "title_ar") val titleAr: String?,
    @field:ColumnInfo(name = "title_en") val titleEn: String?,
    @field:ColumnInfo(name = "text_ar") val textAr: String?,
    @field:ColumnInfo(name = "text_en") val textEn: String?,
    @field:ColumnInfo(name = "text_en_translation") val textEnTranslation: String?,
    @field:ColumnInfo(name = "repetition_ar", defaultValue = "1") val repetitionAr: String,
    @field:ColumnInfo(name = "repetition_en", defaultValue = "1") val repetitionEn: String,
    @field:ColumnInfo(name = "virtue_ar") val virtueAr: String?,
    @field:ColumnInfo(name = "virtue_en") val virtueEn: String?,
    @field:ColumnInfo(name = "reference_ar") val referenceAr: String?,
    @field:ColumnInfo(name = "reference_en") val referenceEn: String?
)