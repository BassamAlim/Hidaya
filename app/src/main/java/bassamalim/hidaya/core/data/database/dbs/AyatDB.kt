package bassamalim.hidaya.core.data.database.dbs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ayat")
data class AyatDB(
    @field:ColumnInfo(name = "id") @field:PrimaryKey val id: Int,
    @field:ColumnInfo(name = "jozz") val juz: Int,
    @field:ColumnInfo(name = "sura_no") val suraNum: Int,
    @field:ColumnInfo(name = "page") val page: Int,
    @field:ColumnInfo(name = "aya_no") val ayaNum: Int,
    @field:ColumnInfo(name = "aya_text") val ayaText: String,
    @field:ColumnInfo(name = "aya_text_emlaey") val clearAyaText: String,
    @field:ColumnInfo(name = "aya_translation_en") val translationEn: String,
    @field:ColumnInfo(name = "aya_tafseer") val tafseer: String
)