package bassamalim.hidaya.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.models.Ayah
import bassamalim.hidaya.replacements.DoubleClickLMM

class ListQuranViewerAdapter(
    private val mContext: Context, private val resourceLayout: Int, items: List<Ayah>,
    private val THEME: String, language: String
) : ArrayAdapter<Ayah>(mContext, resourceLayout, items) {

    private var textSize = 0
    private var translate = false

    init {
        textSize = PreferenceManager.getDefaultSharedPreferences(mContext)
            .getInt(mContext.getString(R.string.quran_text_size_key), 15)
        if (language == "en") translate = true
    }

    class ViewHolder(view: View) {
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

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        if (v == null) v = LayoutInflater.from(mContext).inflate(resourceLayout, null)!!

        val item = getItem(position)!!
        val vh = ViewHolder(v)

        if (THEME == "ThemeM") {
            vh.suraNameTv.setBackgroundResource(R.drawable.surah_header)
            vh.textTv.movementMethod = DoubleClickLMM.getInstance(
                mContext.resources.getColor(R.color.highlight_M, mContext.theme)
            )
        }
        else {
            vh.suraNameTv.setBackgroundResource(R.drawable.surah_header_light)
            vh.textTv.movementMethod = DoubleClickLMM.getInstance(
                mContext.resources.getColor(R.color.highlight_L, mContext.theme)
            )
        }

        vh.suraNameTv.textSize = (textSize + 5).toFloat()
        vh.basmalah.textSize = textSize.toFloat()
        vh.textTv.textSize = textSize.toFloat()
        vh.translationTv.textSize = (textSize - 5).toFloat()

        header(vh, item)

        vh.textTv.text = item.getSS()

        if (translate) vh.translationTv.text = item.getTranslation()
        else vh.translationTv.visibility = View.GONE

        item.setScreen(vh.textTv)

        return v
    }

    private fun header(vh: ViewHolder, aya: Ayah) {
        if (aya.getAyahNum() == 1) {
            vh.suraNameTv.text = aya.getSurahName()
            vh.suraNameTv.visibility = View.VISIBLE

            if (aya.getSurahNum() != 1 && aya.getAyahNum() != 9)  // surat al-fatiha and At-Taubah
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

}