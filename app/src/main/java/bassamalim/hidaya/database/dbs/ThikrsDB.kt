package bassamalim.hidaya.database.dbs

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

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
    @field:ColumnInfo(name = "thikr_id") @field:NonNull private val thikr_id: Int,
    @field:ColumnInfo(name = "title") private val title: String?,
    @field:ColumnInfo(name = "title_en") private val title_en: String?,
    @field:ColumnInfo(name = "text") private val text: String?,
    @field:ColumnInfo(name = "text_en") private val text_en: String?,
    @field:ColumnInfo(name = "text_en_translation") private val text_en_translation: String?,
    @field:ColumnInfo(name = "repetition") private val repetition: String?,
    @field:ColumnInfo(name = "repetition_en") private val repetition_en: String?,
    @field:ColumnInfo(name = "fadl") private val fadl: String?,
    @field:ColumnInfo(name = "fadl_en") private val fadl_en: String?,
    @field:ColumnInfo(name = "reference") private val reference: String?,
    @field:ColumnInfo(name = "reference_en") private val reference_en: String?,
    @field:ColumnInfo(name = "athkar_id") @field:NonNull private val athkar_id: Int
) {
    fun getThikr_id(): Int {
        return thikr_id
    }

    fun getTitle(): String {
        return title!!
    }

    fun getTitle_en(): String {
        return title_en!!
    }

    fun getText(): String {
        return text!!
    }

    fun getText_en(): String {
        return text_en!!
    }

    fun getText_en_translation(): String? {
        return text_en_translation
    }

    fun getRepetition(): String {
        return repetition!!
    }

    fun getRepetition_en(): String {
        return repetition_en!!
    }

    fun getFadl(): String {
        return fadl!!
    }

    fun getFadl_en(): String {
        return fadl_en!!
    }

    fun getReference(): String {
        return reference!!
    }

    fun getReference_en(): String {
        return reference_en!!
    }

    fun getAthkar_id(): Int {
        return athkar_id
    }
}