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

        public ViewHolder(View view) {
            super(view);
            card = view.findViewById(R.id.question_result_model);
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
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_quiz_question, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        String qNum = "سؤال " + (questionsCards.get(position).getQuestionNumber() + 1);
        ((TextView) viewHolder.getCard().findViewById(R.id.question_number)).setText(qNum);

        ((TextView) viewHolder.getCard().findViewById(R.id.question_text))
                .setText(questionsCards.get(position).getQuestionText());

        ((TextView) (viewHolder.getCard().findViewById(R.id.answer1)))
                .setText(questionsCards.get(position).getAnswer1());

        ((TextView) (viewHolder.getCard().findViewById(R.id.answer2)))
                .setText(questionsCards.get(position).getAnswer2());

        ((TextView) (viewHolder.getCard().findViewById(R.id.answer3)))
                .setText(questionsCards.get(position).getAnswer3());

        ((TextView) (viewHolder.getCard().findViewById(R.id.answer4)))
                .setText(questionsCards.get(position).getAnswer4());

        setImage(position, 0, viewHolder.getCard().findViewById(R.id.answer1_image));
        setImage(position, 1, viewHolder.getCard().findViewById(R.id.answer2_image));
        setImage(position, 2, viewHolder.getCard().findViewById(R.id.answer3_image));
        setImage(position, 3, viewHolder.getCard().findViewById(R.id.answer4_image));
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
