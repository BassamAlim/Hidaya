package bassamalim.hidaya.core.data.database.dbs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "athkar_parts",
    primaryKeys = ["part_id", "athkar_id"],
    foreignKeys = [ForeignKey(
        entity = AthkarDB::class,
        parentColumns = arrayOf("athkar_id"),
        childColumns = arrayOf("athkar_id"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.SET_DEFAULT
    )]
)
data class AthkarPartsDB(
    @field:ColumnInfo(name = "part_id") val partId: Int,
    @field:ColumnInfo(name = "title") val title: String?,
    @field:ColumnInfo(name = "title_en") val titleEn: String?,
    @field:ColumnInfo(name = "text") val text: String?,
    @field:ColumnInfo(name = "text_en") val textEn: String?,
    @field:ColumnInfo(name = "text_en_translation") val textEnTranslation: String?,
    @field:ColumnInfo(name = "repetition", defaultValue = "1") val repetition: String,
    @field:ColumnInfo(name = "repetition_en", defaultValue = "1") val repetitionEn: String,
    @field:ColumnInfo(name = "fadl") val fadl: String?,
    @field:ColumnInfo(name = "fadl_en") val fadlEn: String?,
    @field:ColumnInfo(name = "reference") val reference: String?,
    @field:ColumnInfo(name = "reference_en") val referenceEn: String?,
    @field:ColumnInfo(name = "athkar_id") val athkarId: Int
)