package bassamalim.hidaya.database.daos;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import bassamalim.hidaya.database.dbs.QuizAnswersDB;

@Dao
public interface QuizAnswersDao {

    @Query("SELECT * FROM quiz_answers WHERE question_id = :qID ORDER BY answer_id")
    List<QuizAnswersDB> getAnswers(int qID);

}
