package bassamalim.hidaya.core.data.dataSources.room.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.room.models.QuizAnswer

@Dao
interface QuizAnswersDao {

    @Query("SELECT * FROM quiz_answers WHERE question_id = :questionId ORDER BY id")
    fun getAnswers(questionId: Int): List<QuizAnswer>

}