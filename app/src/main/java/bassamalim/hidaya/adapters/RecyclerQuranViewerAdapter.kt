package bassamalim.hidaya.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import bassamalim.hidaya.R
import bassamalim.hidaya.models.Ayah
import bassamalim.hidaya.replacements.DoubleClickLMM

class RecyclerQuranViewerAdapter(
    private val context: Context, private val items: List<Ayah>,
    private val THEME: String, language: String, private val surahIndex: Int) :
    RecyclerView.Adapter<RecyclerQuranViewerAdapter.ViewHolder?>() {

    private var textSize: Int
    private var translate = false

    init {
        textSize =
            PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getString(R.string.quran_text_size_key), 15)
        if (language == "en") translate = true
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val suraNameTv: TextView
        val basmalah: TextView
        val textTv: TextView
        val translationTv: TextView

        init {
            suraNameTv = view.findViewById(R.id.sura_name_tv)
            basmalah = view.findViewById(R.id.basmalah_tv)
            textTv = view.findViewById(R.id.text_tv)
            translationTv = view.findViewById(R.id.translation_tv)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = ViewHolder(
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_recycler_quran_viewer, viewGroup, false)
        )

        if (THEME == "ThemeM") {
            viewHolder.suraNameTv.setBackgroundResource(R.drawable.surah_header)
            viewHolder.textTv.movementMethod = DoubleClickLMM.getInstance(
                context.resources.getColor(R.color.highlight_M, context.theme)
            )
        }
        else {
            viewHolder.suraNameTv.setBackgroundResource(R.drawable.surah_header_light)
            viewHolder.textTv.movementMethod = DoubleClickLMM.getInstance(
                context.resources.getColor(R.color.highlight_L, context.theme)
            )
        }

        viewHolder.suraNameTv.textSize = (textSize + 5).toFloat()
        viewHolder.basmalah.textSize = textSize.toFloat()
        viewHolder.textTv.textSize = textSize.toFloat()
        viewHolder.translationTv.textSize = (textSize - 5).toFloat()

        return viewHolder
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val aya: Ayah = items[position]

        header(viewHolder, aya)

        viewHolder.textTv.text = aya.getSS()

        if (translate) viewHolder.translationTv.text = aya.getTranslation()
        else viewHolder.translationTv.visibility = View.GONE

        aya.setScreen(viewHolder.textTv)
    }

    private fun header(vh: ViewHolder, aya: Ayah) {
        if (aya.getAyahNum() == 1) {
            vh.suraNameTv.text = aya.getSurahName()
            vh.suraNameTv.visibility = View.VISIBLE

            if (aya.getSurahNum() != 1 && aya.getAyahNum() != 9) // surat al-fatiha and At-Taubah
                vh.basmalah.visibility = View.VISIBLE
        }
        else {
            vh.suraNameTv.visibility = View.GONE
            vh.basmalah.visibility = View.GONE
        }
    }

    fun setTextSize(textSize: Int) {
        this.textSize = textSize
    }

    override fun getItemCount(): Int {
        return items.size
    }

}