package bassamalim.hidaya.adapters

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.models.CheckboxListItem
import com.google.gson.Gson

class CheckboxSpinnerAdapter(
    private val gContext: Context, resource: Int, private val items: List<CheckboxListItem>,
    private val selected: BooleanArray, private val prefKey: String
) : ArrayAdapter<CheckboxListItem?>(gContext, resource, items) {

    private val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(gContext)
    private val gson: Gson = Gson()
    private var isFromView = false

    private class ViewHolder(view: View?) {
        val tv: TextView
        val cb: CheckBox

        init {
            tv = view!!.findViewById(R.id.text_tv)
            cb = view.findViewById(R.id.checkbox)
        }
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView)!!
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView)!!
    }

    private fun getCustomView(position: Int, convertView: View?): View? {
        var convertV = convertView
        val vh: ViewHolder
        if (convertV == null) {
            val layoutInflater: LayoutInflater = LayoutInflater.from(gContext)
            convertV = layoutInflater.inflate(R.layout.item_checkbox_list, null)
            vh = ViewHolder(convertV)
            convertV.tag = vh
        }
        else vh = convertV.tag as ViewHolder

        vh.tv.text = items[position].text

        // To check weather checked event fire from getView() or user input
        isFromView = true
        vh.cb.isChecked = items[position].isSelected
        isFromView = false

        if (position == 0) vh.cb.visibility = View.INVISIBLE
        else vh.cb.visibility = View.VISIBLE

        if (position != 0) {
            vh.cb.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                if (!isFromView) {
                    selected[position - 1] = isChecked
                    items[position].isSelected = isChecked
                    updatePref()
                }
            }
        }

        return convertV
    }

    private fun updatePref() {
        val str: String = gson.toJson(selected)
        val editor: SharedPreferences.Editor = pref.edit()
        editor.putString(prefKey, str)
        editor.apply()
    }

}