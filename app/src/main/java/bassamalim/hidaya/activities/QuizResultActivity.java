package bassamalim.hidaya.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bassamalim.hidaya.adapters.QuizQuestionAdapter;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.QuizAnswersDB;
import bassamalim.hidaya.database.dbs.QuizQuestionsDB;
import bassamalim.hidaya.databinding.ActivityQuizResultBinding;
import bassamalim.hidaya.models.QuizResultQuestion;

public class QuizResultActivity extends AppCompatActivity {

    private ActivityQuizResultBinding binding;
    private AppDatabase db;
    private int score;
    private int[] cAnswers;
    private List<QuizQuestionsDB> questions;
    private RecyclerView recyclerView;
    private QuizQuestionAdapter adapter;
    private ArrayList<QuizResultQuestion> questionCards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityQuizResultBinding.inflate(getLayoutInflater());
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        db = Room.databaseBuilder(this, AppDatabase.class, "HidayaDB")
                .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build();

        Intent intent = getIntent();
        score = intent.getIntExtra("score", 10);
        cAnswers = intent.getIntArrayExtra("cAnswers");
        questions = (List<QuizQuestionsDB>) intent.getSerializableExtra("questions");

        questionCards = makeQuestionCards();

        setupRecycler();

        show();
    }

    private ArrayList<QuizResultQuestion> makeQuestionCards() {
        ArrayList<QuizResultQuestion> cards = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            List<QuizAnswersDB> answers = getAnswers(questions.get(i).getQuestion_id());

            QuizResultQuestion model = new QuizResultQuestion(i,
                    questions.get(i).getQuestion_text(), questions.get(i).getCorrect_answer_id(),
                    cAnswers[i], answers.get(0).getAnswer_text(), answers.get(1).getAnswer_text(),
                    answers.get(2).getAnswer_text());

            cards.add(model);
        }

        return cards;
    }

    private List<QuizAnswersDB> getAnswers(int qId) {
        return db.quizAnswerDao().getAnswers(qId);
    }

    private void setupRecycler() {
        recyclerView = binding.recycler;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new QuizQuestionAdapter(this, questionCards);
        recyclerView.setAdapter(adapter);
    }

    private void show() {
        String resultText = "نتيجتك هي " + (score * 10) + "%";
        binding.resultScreen.setText(resultText);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
        recyclerView.setAdapter(null);
        adapter = null;
    }
}