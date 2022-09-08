package bassamalim.hidaya.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import bassamalim.hidaya.R
import bassamalim.hidaya.activities.QuranViewer
import bassamalim.hidaya.models.Ayah
import bassamalim.hidaya.utils.LangUtils

class QuranSearcherAdapter(private val context: Context, private val items: List<Ayah>) :
    RecyclerView.Adapter<QuranSearcherAdapter.ViewHolder>() {

    private val suraStr: String = context.getString(R.string.sura)
    private val tafseerString: String = context.getString(R.string.tafseer)
    private val pageNumString: String = context.getString(R.string.page_number)
    private val ayaNumString: String = context.getString(R.string.aya_number)

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val suraNameTv: TextView
        val pageNumTv: TextView
        val ayaNumTv: TextView
        val ayaTextTv: TextView
        val ayaTafseerTv: TextView
        val gotoPageBtn: Button

        init {
            suraNameTv = view.findViewById(R.id.sura_name_tv)
            pageNumTv = view.findViewById(R.id.page_num_tv)
            ayaNumTv = view.findViewById(R.id.aya_num_tv)
            ayaTextTv = view.findViewById(R.id.aya_text_tv)
            ayaTafseerTv = view.findViewById(R.id.aya_tafseer_tv)
            gotoPageBtn = view.findViewById(R.id.goto_page)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_quran_searcher, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val card = items[position]

        val suraNameStr = suraStr + " " + card.getSurahName()
        viewHolder.suraNameTv.text = suraNameStr

        val pageNumStr = "$pageNumString " +
                LangUtils.translateNums(context, card.getPageNum().toString(), false)
        viewHolder.pageNumTv.text = pageNumStr

        val ayaNumStr = "$ayaNumString " +
                LangUtils.translateNums(context, card.getAyahNum().toString(), false)
        viewHolder.ayaNumTv.text = ayaNumStr

        viewHolder.ayaTextTv.text = card.getSS()

        val ayaTafseerStr = tafseerString + ": " + card.getTafseer()
        viewHolder.ayaTafseerTv.text = ayaTafseerStr

        viewHolder.gotoPageBtn.setOnClickListener {
            val intent = Intent(context, QuranViewer::class.java)
            intent.action = "by_page"
            intent.putExtra("page", card.getPageNum())
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

}