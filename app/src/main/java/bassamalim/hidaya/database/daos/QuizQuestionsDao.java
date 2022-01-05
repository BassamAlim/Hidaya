package bassamalim.hidaya.database.daos;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import bassamalim.hidaya.database.dbs.QuizQuestionsDB;

@Dao
public interface QuizQuestionsDao {

    @Query("SELECT * FROM quiz_questions")
    List<QuizQuestionsDB> getAll();

}
