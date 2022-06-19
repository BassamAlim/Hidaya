package bassamalim.hidaya.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.database.dbs.QuizQuestionsDB

@Dao
interface QuizQuestionsDao {
    @get:Query("SELECT * FROM quiz_questions")
    val all: List<QuizQuestionsDB>
}