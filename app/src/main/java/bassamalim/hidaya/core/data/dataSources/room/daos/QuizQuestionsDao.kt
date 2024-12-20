package bassamalim.hidaya.core.data.dataSources.room.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.dataSources.room.entities.QuizQuestion

@Dao
interface QuizQuestionsDao {

    @Query("SELECT * FROM quiz_questions")
    fun getAll(): List<QuizQuestion>

    @Query("SELECT id FROM quiz_questions")
    fun getAllIds(): List<Int>

    @Query("SELECT * FROM quiz_questions WHERE id = :id")
    fun getQuestion(id: Int): QuizQuestion

}