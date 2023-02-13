package bassamalim.hidaya.database.dbs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "thikrs",
    primaryKeys = ["thikr_id", "athkar_id"],
    foreignKeys = [ForeignKey(
        entity = AthkarDB::class,
        parentColumns = arrayOf("athkar_id"),
        childColumns = arrayOf("athkar_id"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.SET_DEFAULT
    )]
)
class ThikrsDB(
    @field:ColumnInfo(name = "thikr_id") private val thikrId: Int,
    @field:ColumnInfo(name = "title") private val title: String?,
    @field:ColumnInfo(name = "title_en") private val titleEn: String?,
    @field:ColumnInfo(name = "text") private val text: String?,
    @field:ColumnInfo(name = "text_en") private val textEn: String?,
    @field:ColumnInfo(name = "text_en_translation") private val textEnTranslation: String?,
    @field:ColumnInfo(name = "repetition", defaultValue = "1") private val repetition: String,
    @field:ColumnInfo(name = "repetition_en", defaultValue = "1") private val repetitionEn: String,
    @field:ColumnInfo(name = "fadl") private val fadl: String?,
    @field:ColumnInfo(name = "fadl_en") private val fadlEn: String?,
    @field:ColumnInfo(name = "reference") private val reference: String?,
    @field:ColumnInfo(name = "reference_en") private val referenceEn: String?,
    @field:ColumnInfo(name = "athkar_id") private val athkarId: Int
) {

    fun getThikrId(): Int {
        return thikrId
    }

    fun getTitle(): String? {
        return title
    }

    fun getTitleEn(): String? {
        return titleEn
    }

    fun getText(): String? {
        return text
    }

    fun getTextEn(): String? {
        return textEn
    }

    fun getTextEnTranslation(): String? {
        return textEnTranslation
    }

    fun getRepetition(): String {
        return repetition
    }

    fun getRepetitionEn(): String {
        return repetitionEn
    }

    fun getFadl(): String? {
        return fadl
    }

    fun getFadlEn(): String? {
        return fadlEn
    }

    fun getReference(): String? {
        return reference
    }

    fun getReferenceEn(): String? {
        return referenceEn
    }

    fun getAthkarId(): Int {
        return athkarId
    }
}