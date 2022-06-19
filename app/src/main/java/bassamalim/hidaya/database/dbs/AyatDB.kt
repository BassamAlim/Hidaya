package bassamalim.hidaya.database.dbs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import javax.annotation.Nullable

@Entity(tableName = "hafs_ayat")
class AyatDB(
    @field:ColumnInfo(name = "id") @field:PrimaryKey val id: Int,
    @field:ColumnInfo(name = "jozz") val jozz: Int,
    @field:ColumnInfo(name = "sura_no") val sura_no: Int,
    @field:ColumnInfo(name = "page") val page: Int,
    @field:ColumnInfo(name = "aya_no") val aya_no: Int,
    @field:ColumnInfo(name = "aya_text") val aya_text: String?,
    @field:ColumnInfo(name = "aya_text_en") val aya_text_en: String?,
    @field:ColumnInfo(name = "aya_text_emlaey") val aya_text_emlaey: String?,
    @field:ColumnInfo(name = "aya_translation_en") val aya_translation_en: String?,
    @field:ColumnInfo(name = "aya_tafseer") val aya_tafseer: String?
)