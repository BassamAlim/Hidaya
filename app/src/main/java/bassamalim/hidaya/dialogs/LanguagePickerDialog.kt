package bassamalim.hidaya.dialogs

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R

class LanguagePickerDialog(private val context: Context, view: View) {
    private var popup: PopupWindow? = null
    private val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private var listView: ListView? = null
    private fun showPopup(view: View) {
        val inflater: LayoutInflater = view.context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(
            R.layout.dialog_filter,
            LinearLayout(context), false
        )
        popup = PopupWindow(
            popupView, LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT, true
        )
        popup!!.elevation = 10f
        popup!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popup!!.isOutsideTouchable = false
        popup!!.showAtLocation(view, Gravity.CENTER, 0, 50)
        setupListview()
        setListeners()
    }

    private fun setupListview() {
        listView = popup!!.getContentView().findViewById(R.id.listview)
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            context,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            context.resources.getStringArray(R.array.languages_values)
        )
        listView!!.adapter = adapter
    }

    private fun setListeners() {
        listView!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                val editor: SharedPreferences.Editor = pref.edit()
                editor.putString(
                    context.resources.getString(R.string.language_key),
                    context.resources.getStringArray(R.array.languages_values)[position]
                )
                editor.apply()
                popup!!.dismiss()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    init {
        showPopup(view)
    }
}