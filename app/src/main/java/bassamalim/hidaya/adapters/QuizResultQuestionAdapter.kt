package bassamalim.hidaya.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import bassamalim.hidaya.R
import bassamalim.hidaya.models.QuizResultQuestion

class QuizResultQuestionAdapter(
    private val context: Context, private val items: ArrayList<QuizResultQuestion>
) : RecyclerView.Adapter<QuizResultQuestionAdapter.ViewHolder>() {

    private val question: String = context.getString(R.string.question)

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView
        val qNumTv: TextView
        val qTextTv: TextView
        val a1Tv: TextView
        val a2Tv: TextView
        val a3Tv: TextView
        val a4Tv: TextView
        val a1iv: ImageView
        val a2iv: ImageView
        val a3iv: ImageView
        val a4iv: ImageView

        init {
            card = view.findViewById(R.id.question_result_model)
            qNumTv = view.findViewById(R.id.question_number)
            qTextTv = view.findViewById(R.id.question_text)
            a1Tv = view.findViewById(R.id.answer1)
            a2Tv = view.findViewById(R.id.answer2)
            a3Tv = view.findViewById(R.id.answer3)
            a4Tv = view.findViewById(R.id.answer4)
            a1iv = view.findViewById(R.id.answer1_image)
            a2iv = view.findViewById(R.id.answer2_image)
            a3iv = view.findViewById(R.id.answer3_image)
            a4iv = view.findViewById(R.id.answer4_image)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_quiz_result_question, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val qNum = question + " " + (items[position].getQuestionNumber() + 1)
        viewHolder.qNumTv.text = qNum
        viewHolder.qTextTv.text = items[position].getQuestionText()

        viewHolder.a1Tv.text = items[position].getAnswer1()
        viewHolder.a2Tv.text = items[position].getAnswer2()
        viewHolder.a3Tv.text = items[position].getAnswer3()
        viewHolder.a4Tv.text = items[position].getAnswer4()

        setImage(position, 0, viewHolder.a1iv)
        setImage(position, 1, viewHolder.a2iv)
        setImage(position, 2, viewHolder.a3iv)
        setImage(position, 3, viewHolder.a4iv)
    }

    private fun setImage(position: Int, num: Int, iv: ImageView) {
        val chosen = items[position].getChosenAnswer()
        val correct = items[position].getCorrectAnswer()

        if (num == correct)
            iv.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_correct))
        else if (chosen != correct && num == chosen)
            iv.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_wrong))
        else
            iv.setImageDrawable(null)
    }

    override fun getItemCount(): Int {
        return items.size
    }

}