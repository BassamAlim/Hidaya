package bassamalim.hidaya.core.data.database.dbs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "quiz_answers",
    primaryKeys = ["question_id", "answer_id"],
    foreignKeys = [ForeignKey(
        entity = QuizQuestionsDB::class,
        parentColumns = arrayOf("question_id"),
        childColumns = arrayOf("question_id"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.SET_DEFAULT
    )]
)
data class QuizAnswersDB(
    @field:ColumnInfo(name = "answer_id") val answer_id: Int,
    @field:ColumnInfo(name = "answer_text") val answer_text: String?,
    @field:ColumnInfo(name = "question_id") val question_id: Int
)