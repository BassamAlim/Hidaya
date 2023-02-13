package bassamalim.hidaya.data.database.dbs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "quiz_questions")
class QuizQuestionsDB(
    @field:ColumnInfo(name = "question_id") @field:PrimaryKey private val questionId: Int,
    @field:ColumnInfo(name = "question_text") private val questionText: String?,
    @field:ColumnInfo(name = "correct_answer_id") private val correctAnswerId: Int
) : Serializable {

    fun getQuestionId(): Int {
        return questionId
    }

    fun getQuestionText(): String {
        return questionText!!
    }

    fun getCorrectAnswerId(): Int {
        return correctAnswerId
    }
}