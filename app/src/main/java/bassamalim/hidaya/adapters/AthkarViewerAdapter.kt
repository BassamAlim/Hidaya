package bassamalim.hidaya.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import bassamalim.hidaya.R
import bassamalim.hidaya.models.Thikr

class AthkarViewerAdapter(context: Context, cards: ArrayList<Thikr>, private val LANGUAGE: String) :
    RecyclerView.Adapter<AthkarViewerAdapter.ViewHolder?>() {

    private val margin = 15
    private val items = cards
    private var textSize: Int

    init {
        textSize = PreferenceManager.getDefaultSharedPreferences(context)
            .getInt(context.getString(R.string.alathkar_text_size_key), 15) + margin
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTv: TextView
        val textTv: TextView
        val textTranslationTv: TextView
        val fadlTv: TextView
        val referenceBtn: ImageButton
        val repetitionTv: TextView
        val repetitionDiv: View
        val fadlDiv: View
        val referenceDiv: View

        init {
            titleTv = view.findViewById(R.id.title_tv)
            textTv = view.findViewById(R.id.text_tv)
            textTranslationTv = view.findViewById(R.id.text_translation_tv)
            fadlTv = view.findViewById(R.id.fadl_tv)
            referenceBtn = view.findViewById(R.id.reference_btn)
            repetitionTv = view.findViewById(R.id.repetition_tv)
            repetitionDiv = view.findViewById(R.id.repetition_div)
            fadlDiv = view.findViewById(R.id.fadl_div)
            referenceDiv = view.findViewById(R.id.reference_div)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = ViewHolder(
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_athkar_viewer, viewGroup, false)
        )

        viewHolder.titleTv.textSize = textSize.toFloat()
        viewHolder.textTv.textSize = textSize.toFloat()
        viewHolder.textTranslationTv.textSize = textSize.toFloat()
        viewHolder.fadlTv.textSize = (textSize - 8).toFloat()
        viewHolder.repetitionTv.textSize = textSize.toFloat()

        return viewHolder
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val card: Thikr = items[position]

        viewHolder.textTv.text = card.getText()

        if (card.getRepetition() == "1") {
            viewHolder.repetitionTv.visibility = View.GONE
            viewHolder.repetitionDiv.visibility = View.GONE
        }
        else viewHolder.repetitionTv.text = card.getRepetition()

        if (card.getTitle() == null || card.getTitle()!!.isEmpty())
            viewHolder.titleTv.visibility = View.GONE
        else viewHolder.titleTv.text = card.getTitle()

        if (LANGUAGE == "ar"
            || card.getTextTranslation() == null || card.getTextTranslation()!!.isEmpty())
            viewHolder.textTranslationTv.visibility = View.GONE
        else viewHolder.textTranslationTv.text = card.getTextTranslation()

        if (card.getFadl() == null || card.getFadl()!!.isEmpty()) {
            viewHolder.fadlTv.visibility = View.GONE
            viewHolder.fadlDiv.visibility = View.GONE
        }
        else viewHolder.fadlTv.text = card.getFadl()

        if (card.getReference() == null || card.getReference()!!.isEmpty()) {
            viewHolder.referenceBtn.visibility = View.GONE
            viewHolder.referenceDiv.visibility = View.GONE
        }

        viewHolder.referenceBtn.setOnClickListener(card.getReferenceListener())
    }

    fun setTextSize(textSize: Int) {
        this.textSize = textSize + margin
    }

    override fun getItemCount(): Int {
        return items.size
    }

}