package bassamalim.hidaya.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.database.dbs.QuizAnswersDB

@Dao
interface QuizAnswersDao {
    @Query("SELECT * FROM quiz_answers WHERE question_id = :qID ORDER BY answer_id")
    fun getAnswers(qID: Int): List<QuizAnswersDB>
}