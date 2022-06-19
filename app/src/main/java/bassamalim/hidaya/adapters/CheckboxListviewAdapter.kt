package bassamalim.hidaya.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import bassamalim.hidaya.R
import bassamalim.hidaya.models.CheckboxListItem

class CheckboxListviewAdapter(
    private val gContext: Context,
    resource: Int,
    private val items: List<CheckboxListItem>,
    private val selected: BooleanArray
) : ArrayAdapter<CheckboxListItem?>(
    gContext, resource, items
) {
    private var isFromView = false

    private class ViewHolder(view: View?) {
        val tv: TextView
        val cb: CheckBox

        init {
            tv = view!!.findViewById(R.id.text_tv)
            cb = view.findViewById(R.id.checkbox)
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val result: View
        val vh: ViewHolder
        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(gContext)
            convertView = layoutInflater.inflate(R.layout.item_checkbox_list, null)
            vh = ViewHolder(convertView)
            result = convertView!!
            convertView.tag = vh
        } else {
            vh = convertView.tag as ViewHolder
            result = convertView
        }
        val item = items[position]
        vh.tv.text = item.text
        isFromView = true
        vh.cb.isChecked = item.isSelected
        isFromView = false
        vh.cb.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (!isFromView) {
                item.isSelected = isChecked
                selected[position] = isChecked
            }
        }
        return result
    }
}