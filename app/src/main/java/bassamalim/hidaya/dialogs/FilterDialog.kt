package bassamalim.hidaya.dialogs

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import bassamalim.hidaya.R
import bassamalim.hidaya.adapters.CheckboxListviewAdapter
import bassamalim.hidaya.models.CheckboxListItem
import bassamalim.hidaya.replacements.FilteredRecyclerAdapter
import com.google.gson.Gson
import java.util.*

class FilterDialog<VH : RecyclerView.ViewHolder>(
    private val context: Context,
    private val view: View,
    title: String,
    private val strArr: Array<String>,
    private val selected: BooleanArray,
    private val filteredAdapter: FilteredRecyclerAdapter<VH>,
    private val filterIb: ImageButton,
    private val prefKey: String) {

    private lateinit var popup: PopupWindow
    private val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val gson: Gson = Gson()
    private lateinit var cbListAdapter: CheckboxListviewAdapter
    private lateinit var items: MutableList<CheckboxListItem>

    init {
        showPopup(title)
    }

    private fun showPopup(title: String) {
        val inflater: LayoutInflater =
            view.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val popupView: View = inflater.inflate(
            R.layout.dialog_filter, LinearLayout(context), false
        )

        popup = PopupWindow(
            popupView, LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT, true
        )

        popup.elevation = 10f
        popup.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popup.isOutsideTouchable = false

        popup.showAtLocation(view, Gravity.CENTER, 0, 50)

        (popup.contentView.findViewById<View>(R.id.dialog_title_tv) as TextView).text = title
        setupListview()
        setListeners()
    }

    private fun setupListview() {
        items = ArrayList<CheckboxListItem>()
        for (i in strArr.indices)
            items.add(CheckboxListItem(strArr[i], selected[i]))

        val listView: ListView = popup.contentView.findViewById(R.id.listview)
        cbListAdapter = CheckboxListviewAdapter(
            context, 0, items as ArrayList<CheckboxListItem>, selected
        )
        listView.adapter = cbListAdapter
    }

    private fun setListeners() {
        popup.contentView.findViewById<View>(R.id.select_all_btn).setOnClickListener {
            for (i in items.indices) items[i].isSelected = true
            cbListAdapter.notifyDataSetChanged()

            Arrays.fill(selected, true)
        }

        popup.contentView.findViewById<View>(R.id.unselect_all_btn).setOnClickListener {
            for (i in items.indices) items[i].isSelected = false
            cbListAdapter.notifyDataSetChanged()

            Arrays.fill(selected, false)
        }

        popup.contentView.findViewById<View>(R.id.finish_btn).setOnClickListener {
            popup.dismiss()
        }

        popup.setOnDismissListener {
            save()
            setFilterIb()
        }
    }

    private fun setFilterIb() {
        var changed = false
        for (bool in selected) {
            if (!bool) {
                changed = true
                break
            }
        }
        if (changed) filterIb.setImageDrawable(
            AppCompatResources.getDrawable(context, R.drawable.ic_filtered)
        )
        else filterIb.setImageDrawable(
            AppCompatResources.getDrawable(context, R.drawable.ic_filter)
        )
    }

    private fun save() {
        val str: String = gson.toJson(selected)
        val editor: SharedPreferences.Editor = pref.edit()
        editor.putString(prefKey, str)
        editor.apply()
        filteredAdapter.filter(null, selected)
    }

}