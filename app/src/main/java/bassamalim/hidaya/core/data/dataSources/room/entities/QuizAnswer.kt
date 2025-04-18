package bassamalim.hidaya.core.data.dataSources.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "quiz_answers",
    primaryKeys = ["question_id", "id"],
    foreignKeys = [ForeignKey(
        entity = QuizQuestion::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("question_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class QuizAnswer(
    @field:ColumnInfo(name = "id") val id: Int,
    @field:ColumnInfo(name = "question_id") val questionId: Int,
    @field:ColumnInfo(name = "answer") val answer: String
)