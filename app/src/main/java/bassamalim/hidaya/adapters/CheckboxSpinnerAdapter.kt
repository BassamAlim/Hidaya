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
    private val gContext: Context,
    resource: Int,
    objects: List<CheckboxListItem>,
    selected: BooleanArray,
    prefKey: String
) : ArrayAdapter<CheckboxListItem?>(
    gContext, resource, objects
) {
    private val pref: SharedPreferences
    private val prefKey: String
    private val gson: Gson
    private val items: List<CheckboxListItem>
    private val selected: BooleanArray
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
        var convertView = convertView
        val vh: ViewHolder
        if (convertView == null) {
            val layoutInflater: LayoutInflater = LayoutInflater.from(gContext)
            convertView = layoutInflater.inflate(R.layout.item_checkbox_list, null)
            vh = ViewHolder(convertView)
            convertView.tag = vh
        } else vh = convertView.tag as ViewHolder
        vh.tv.text = items[position].text

        // To check weather checked event fire from getView() or user input
        isFromView = true
        vh.cb.isChecked = items[position].isSelected
        isFromView = false
        if (position == 0) vh.cb.visibility = View.INVISIBLE else vh.cb.visibility = View.VISIBLE
        if (position != 0) {
            vh.cb.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                if (!isFromView) {
                    selected[position - 1] = isChecked
                    items[position].isSelected = isChecked
                    updatePref()
                }
            }
        }
        return convertView
    }

    private fun updatePref() {
        val str: String = gson.toJson(selected)
        val editor: SharedPreferences.Editor = pref.edit()
        editor.putString(prefKey, str)
        editor.apply()
    }

    init {
        items = objects
        this.selected = selected
        this.prefKey = prefKey
        pref = PreferenceManager.getDefaultSharedPreferences(gContext)
        gson = Gson()
    }
}