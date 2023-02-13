package bassamalim.hidaya.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.data.database.dbs.QuizQuestionsDB

@Dao
interface QuizQuestionsDao {

    @get:Query("SELECT * FROM quiz_questions")
    val all: List<QuizQuestionsDB>

    @Query("SELECT * FROM quiz_questions WHERE question_id = :id")
    fun getQuestion(id: Int): QuizQuestionsDB


}