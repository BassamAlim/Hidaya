package bassamalim.hidaya.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import bassamalim.hidaya.R;
import bassamalim.hidaya.models.QuizResultQuestion;

public class QuizQuestionAdapter extends RecyclerView.Adapter<QuizQuestionAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<QuizResultQuestion> questionsCards;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView card;
        private final TextView qNumTv;
        private final TextView qTextTv;
        private final TextView a1Tv, a2Tv, a3Tv, a4Tv;
        private final ImageView a1iv, a2iv, a3iv, a4iv;

        public ViewHolder(View view) {
            super(view);
            card = view.findViewById(R.id.question_result_model);
            qNumTv = view.findViewById(R.id.question_number);
            qTextTv = view.findViewById(R.id.question_text);
            a1Tv = view.findViewById(R.id.answer1);
            a2Tv = view.findViewById(R.id.answer2);
            a3Tv = view.findViewById(R.id.answer3);
            a4Tv = view.findViewById(R.id.answer4);
            a1iv = view.findViewById(R.id.answer1_image);
            a2iv = view.findViewById(R.id.answer2_image);
            a3iv = view.findViewById(R.id.answer3_image);
            a4iv = view.findViewById(R.id.answer4_image);
        }

        public CardView getCard() {
            return card;
        }
    }

    public QuizQuestionAdapter(Context context, ArrayList<QuizResultQuestion> cards) {
        this.context = context;
        questionsCards = new ArrayList<>(cards);
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_quiz_question, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        String qNum = "سؤال " + (questionsCards.get(position).getQuestionNumber() + 1);
        viewHolder.qNumTv.setText(qNum);
        viewHolder.qTextTv.setText(questionsCards.get(position).getQuestionText());

        viewHolder.a1Tv.setText(questionsCards.get(position).getAnswer1());
        viewHolder.a2Tv.setText(questionsCards.get(position).getAnswer2());
        viewHolder.a3Tv.setText(questionsCards.get(position).getAnswer3());
        viewHolder.a4Tv.setText(questionsCards.get(position).getAnswer4());

        setImage(position, 0, viewHolder.a1iv);
        setImage(position, 1, viewHolder.a2iv);
        setImage(position, 2, viewHolder.a3iv);
        setImage(position, 3, viewHolder.a4iv);
    }

    private void setImage(int position, int num, ImageView iv) {
        int chosen = questionsCards.get(position).getChosenAnswer();
        int correct = questionsCards.get(position).getCorrectAnswer();

        if (num == correct)
            iv.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_correct));
        else if (chosen != correct && num == chosen)
            iv.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_wrong));
        else
            iv.setImageDrawable(null);
    }

    @Override
    public int getItemCount() {
        return questionsCards.size();
    }
}
