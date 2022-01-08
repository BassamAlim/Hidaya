package bassamalim.hidaya.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.QuizAnswersDB;
import bassamalim.hidaya.database.dbs.QuizQuestionsDB;
import bassamalim.hidaya.databinding.ActivityQuizBinding;
import bassamalim.hidaya.other.Global;

public class QuizActivity extends AppCompatActivity {

    private ActivityQuizBinding binding;
    private AppDatabase db;
    private List<QuizQuestionsDB> questions;
    private int current = 0;
    private int[] cAnswers;
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityQuizBinding.inflate(getLayoutInflater());
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        db = Room.databaseBuilder(this, AppDatabase.class, "HidayaDB")
                .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build();

        selectQuestions(getQuestions());

        setListeners();

        ask(questions.get(current));
    }

    private List<QuizQuestionsDB> getQuestions() {
        return db.quizQuestionDao().getAll();
    }

    private void selectQuestions(List<QuizQuestionsDB> rawQuestions) {
        Collections.shuffle(rawQuestions);
        questions = new ArrayList<>(rawQuestions.subList(0, 10));
    }

    private void setListeners() {
        cAnswers = new int[10];

        binding.answer1.setOnClickListener(view -> answered(0));
        binding.answer2.setOnClickListener(view -> answered(1));
        binding.answer3.setOnClickListener(view -> answered(2));
        binding.answer4.setOnClickListener(view -> answered(3));
    }

    private void answered(int a) {
        cAnswers[current] = a;

        if (a == questions.get(current).getCorrect_answer_id())
            score++;

        current++;

        if (current == 10) {
            Intent intent = new Intent(this, QuizResultActivity.class);
            intent.putExtra("cAnswers", cAnswers);
            intent.putExtra("score", score);
            intent.putExtra("questions", (Serializable) questions);
            startActivity(intent);

            finish();
        }
        else
            ask(questions.get(current));
    }

    private void ask(QuizQuestionsDB q) {
        String qNum = "سؤال " + (current+1);
        binding.questionNumber.setText(qNum);

        binding.questionScreen.setText(questions.get(current).getQuestion_text());

        List<QuizAnswersDB> answers = getAnswers(q.getQuestion_id());
        binding.answer1.setText(answers.get(0).getAnswer_text());
        binding.answer2.setText(answers.get(1).getAnswer_text());
        binding.answer3.setText(answers.get(2).getAnswer_text());
        binding.answer4.setText(answers.get(3).getAnswer_text());
    }

    private List<QuizAnswersDB> getAnswers(int qId) {
        return db.quizAnswerDao().getAnswers(qId);
    }
}