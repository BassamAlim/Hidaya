package bassamalim.hidaya.database.dbs;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "quiz_questions")
public class QuizQuestionsDB implements Serializable {

    @PrimaryKey
    @ColumnInfo(name = "question_id")
    private final int question_id;
    @ColumnInfo(name = "question_text")
    private final String question_text;
    @ColumnInfo(name = "correct_answer_id")
    private final int correct_answer_id;

    public QuizQuestionsDB(int question_id, String question_text, int correct_answer_id) {
        this.question_id = question_id;
        this.question_text = question_text;
        this.correct_answer_id = correct_answer_id;
    }

    public int getQuestion_id() {
        return question_id;
    }

    public String getQuestion_text() {
        return question_text;
    }

    public int getCorrect_answer_id() {
        return correct_answer_id;
    }
}
