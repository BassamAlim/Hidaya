package bassamalim.hidaya.database.dbs;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(tableName = "quiz_answers", primaryKeys = {"question_id", "answer_id"},
        foreignKeys = @ForeignKey(entity = QuizQuestionsDB.class,
                parentColumns = "question_id", childColumns = "question_id",
                onUpdate = ForeignKey.CASCADE, onDelete = ForeignKey.SET_DEFAULT))
public class QuizAnswersDB {

    @ColumnInfo(name = "answer_id")
    private final int answer_id;
    @ColumnInfo(name = "answer_text")
    private final String answer_text;
    @ColumnInfo(name = "question_id")
    private final int question_id;

    public QuizAnswersDB(int answer_id, String answer_text, int question_id) {
        this.answer_id = answer_id;
        this.answer_text = answer_text;
        this.question_id = question_id;
    }

    public int getAnswer_id() {
        return answer_id;
    }

    public String getAnswer_text() {
        return answer_text;
    }

    public int getQuestion_id() {
        return question_id;
    }
}
