package bassamalim.hidaya.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import bassamalim.hidaya.R;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.QuizAnswersDB;
import bassamalim.hidaya.database.dbs.QuizQuestionsDB;
import bassamalim.hidaya.databinding.ActivityQuizBinding;
import bassamalim.hidaya.other.Utils;

public class QuizActivity extends AppCompatActivity {

    private ActivityQuizBinding binding;
    private AppDatabase db;
    private List<QuizQuestionsDB> questions;
    private int current = 0;
    private final int[] cAnswers = new int[10];
    private Button nextBtn;
    private Button prevBtn;
    private RadioGroup radioGroup;
    private final RadioButton[] answerBtns = new RadioButton[4];
    private TypedValue colorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.myOnActivityCreated(this);
        binding = ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.home.setOnClickListener(v -> onBackPressed());

        db = Room.databaseBuilder(this, AppDatabase.class, "HidayaDB")
                .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build();

        colorText = new TypedValue();
        getTheme().resolveAttribute(R.attr.myText, colorText, true);

        selectQuestions(getQuestions());

        Arrays.fill(cAnswers, -1);

        initViews();

        setListeners();

        ask(current);
    }

    private void selectQuestions(List<QuizQuestionsDB> rawQuestions) {
        Collections.shuffle(rawQuestions);
        questions = new ArrayList<>(rawQuestions.subList(0, 10));
    }

    private void initViews() {
        nextBtn = binding.nextQuestion;
        prevBtn = binding.previousQuestion;

        radioGroup = binding.answersRadioGroup;

        answerBtns[0] = binding.answer1;
        answerBtns[1] = binding.answer2;
        answerBtns[2] = binding.answer3;
        answerBtns[3] = binding.answer4;
    }

    private void setListeners() {
        for (int i = 0; i < answerBtns.length; i++) {
            int finalI = i;
            answerBtns[i].setOnClickListener(v -> answered(finalI));
        }

        prevBtn.setOnClickListener(v -> previousQ());
        nextBtn.setOnClickListener(v -> nextQ());
    }

    private void ask(int num) {
        QuizQuestionsDB q = questions.get(num);

        String qNum = getString(R.string.question) + " " + (current+1);
        binding.topBarTitle.setText(qNum);

        binding.questionScreen.setText(questions.get(current).getQuestion_text());

        List<QuizAnswersDB> answers = getAnswers(q.getQuestion_id());
        for (int i = 0; i < answerBtns.length; i++)
            answerBtns[i].setText(answers.get(i).getAnswer_text());

        adjustButtons();
    }

    private void adjustButtons() {
        radioGroup.clearCheck();
        if (cAnswers[current] != -1)
            radioGroup.check(answerBtns[cAnswers[current]].getId());

        if (current == 0) {
            prevBtn.setEnabled(false);
            prevBtn.setTextColor(getResources().getColor(R.color.grey, getTheme()));
        }
        else if (current == 9) {
            if (allAnswered()) {
                nextBtn.setText(getString(R.string.finish_quiz));
                nextBtn.setEnabled(true);
                nextBtn.setTextColor(colorText.data);
            }
            else {
                nextBtn.setText(getString(R.string.answer_all_questions));
                nextBtn.setEnabled(false);
                nextBtn.setTextColor(getResources().getColor(R.color.grey, getTheme()));
            }
        }
        else {
            prevBtn.setEnabled(true);
            prevBtn.setTextColor(colorText.data);

            nextBtn.setEnabled(true);
            nextBtn.setText(getString(R.string.next_question));
            nextBtn.setTextColor(colorText.data);
        }
    }

    private void answered(int a) {
        cAnswers[current] = a;

        adjustButtons();

        if (current != 9)
            nextQ();
    }

    private void nextQ() {
        if (current == 9)
            endQuiz();
        else
            ask(++current);
    }

    private void previousQ() {
        if (current > 0)
            ask(--current);
    }

    private boolean allAnswered() {
        for (int cAnswer : cAnswers) {
            if (cAnswer == -1)
                return false;
        }
        return true;
    }

    private void endQuiz() {
        int score = 0;
        for (int i = 0; i < 10; i++) {
            if (cAnswers[i] == questions.get(i).getCorrect_answer_id())
                score++;
        }

        Intent intent = new Intent(this, QuizResultActivity.class);
        intent.putExtra("cAnswers", cAnswers);
        intent.putExtra("score", score);
        intent.putExtra("questions", (Serializable) questions);
        startActivity(intent);

        finish();
    }

    private List<QuizQuestionsDB> getQuestions() {
        return db.quizQuestionDao().getAll();
    }

    private List<QuizAnswersDB> getAnswers(int qId) {
        return db.quizAnswerDao().getAnswers(qId);
    }
}