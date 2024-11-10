package bassamalim.hidaya.core.data.dataSources.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "quiz_questions")
data class QuizQuestion(
    @field:ColumnInfo(name = "id") @field:PrimaryKey val id: Int,
    @field:ColumnInfo(name = "question") val question: String,
    @field:ColumnInfo(name = "description") val description: String?,
    @field:ColumnInfo(name = "type_ar") val typeAr: String?,
    @field:ColumnInfo(name = "type_en") val typeEn: String?
) : Serializable