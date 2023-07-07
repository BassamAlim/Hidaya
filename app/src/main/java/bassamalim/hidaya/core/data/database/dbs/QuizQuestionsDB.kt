package bassamalim.hidaya.core.data.database.dbs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "quiz_questions")
data class QuizQuestionsDB(
    @field:ColumnInfo(name = "question_id") @field:PrimaryKey val questionId: Int,
    @field:ColumnInfo(name = "question_text") val questionText: String?,
    @field:ColumnInfo(name = "correct_answer_id") val correctAnswerId: Int
) : Serializable