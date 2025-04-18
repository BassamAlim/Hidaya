package bassamalim.hidaya.core.data.dataSources.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "verses")
data class Verse(
    @field:ColumnInfo(name = "id") @field:PrimaryKey val id: Int,
    @field:ColumnInfo(name = "num") val num: Int,
    @field:ColumnInfo(name = "juz_num") val juzNum: Int,
    @field:ColumnInfo(name = "sura_num") val suraNum: Int,
    @field:ColumnInfo(name = "page_num") val pageNum: Int,
    @field:ColumnInfo(name = "decorated_text") val decoratedText: String,
    @field:ColumnInfo(name = "plain_text") val plainText: String,
    @field:ColumnInfo(name = "start_line_num") val startLineNum: Int,
    @field:ColumnInfo(name = "end_line_num") val endLineNum: Int,
    @field:ColumnInfo(name = "translation_en") val translationEn: String,
    @field:ColumnInfo(name = "interpretation") val interpretation: String
)