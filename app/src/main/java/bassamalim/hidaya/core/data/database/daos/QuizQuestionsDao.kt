package bassamalim.hidaya.core.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.database.dbs.QuizQuestionsDB

@Dao
interface QuizQuestionsDao {

    @Query("SELECT * FROM quiz_questions")
    fun getAll(): List<QuizQuestionsDB>

    @Query("SELECT * FROM quiz_questions WHERE question_id = :id")
    fun getQuestion(id: Int): QuizQuestionsDB

}