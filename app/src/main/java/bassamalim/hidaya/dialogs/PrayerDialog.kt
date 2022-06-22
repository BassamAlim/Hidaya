package bassamalim.hidaya.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.enums.ID
import bassamalim.hidaya.helpers.Alarms
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.other.Utils

class PrayerDialog(
    private val context: Context, private val view: View, private val id: ID, title: String
) {

    private lateinit var popup: PopupWindow
    private val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private lateinit var radioGroup: RadioGroup
    private lateinit var rButtons: Array<RadioButton?>
    private lateinit var images: Array<ImageView?>
    private lateinit var delayTvs: Array<TextView?>
    private lateinit var drawables: IntArray

    init {
        showPopup()
        populate(title)
    }

    private fun showPopup() {
        val inflater: LayoutInflater = view.context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val popupView: View = inflater.inflate(
            R.layout.dialog_prayer, LinearLayout(context), false
        )

        if (id == ID.SHOROUQ) popupView.findViewById<View>(R.id.athan_rb).visibility = View.GONE

        popup = PopupWindow(
            popupView, LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT, true
        )

        popup.elevation = 10f
        popup.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popup.isOutsideTouchable = true
        popup.animationStyle = R.style.PrayerDialogAnimation

        popup.showAtLocation(view, Gravity.START, 30, getY())
    }

    @SuppressLint("StringFormatInvalid")
    private fun populate(title: String) {
        val nameScreen: TextView = popup.contentView.findViewById(R.id.prayer_name_tv)
        nameScreen.text = String.format(context.getString(R.string.settings_of), title)

        setViews()

        val defaultState =
            if (id == ID.SHOROUQ) 0
            else 2
        val state: Int = pref.getInt(id.toString() + "notification_type", defaultState)
        radioGroup.check(rButtons[state]!!.id)

        radioGroup.setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
            selectedAlertState(getIndex(checkedId))
        }

        setupSpinner()
    }

    private fun setViews() {
        radioGroup = popup.contentView.findViewById(R.id.prayer_alert_rg)

        rButtons = arrayOfNulls(4)
        rButtons[0] = popup.contentView.findViewById(R.id.disable_rb)
        rButtons[1] = popup.contentView.findViewById(R.id.silent_rb)
        rButtons[2] = popup.contentView.findViewById(R.id.notify_rb)
        rButtons[3] = popup.contentView.findViewById(R.id.athan_rb)

        images = arrayOfNulls(6)
        images[0] = view.findViewById(R.id.fajr_image)
        images[1] = view.findViewById(R.id.shorouq_image)
        images[2] = view.findViewById(R.id.duhr_image)
        images[3] = view.findViewById(R.id.asr_image)
        images[4] = view.findViewById(R.id.maghrib_image)
        images[5] = view.findViewById(R.id.ishaa_image)

        delayTvs = arrayOfNulls(6)
        delayTvs[0] = view.findViewById(R.id.fajr_delay_tv)
        delayTvs[1] = view.findViewById(R.id.shorouq_delay_tv)
        delayTvs[2] = view.findViewById(R.id.duhr_delay_tv)
        delayTvs[3] = view.findViewById(R.id.asr_delay_tv)
        delayTvs[4] = view.findViewById(R.id.maghrib_delay_tv)
        delayTvs[5] = view.findViewById(R.id.ishaa_delay_tv)

        drawables = IntArray(4)
        drawables[0] = R.drawable.ic_disabled
        drawables[1] = R.drawable.ic_silent
        drawables[2] = R.drawable.ic_sound
        drawables[3] = R.drawable.ic_speaker
    }

    private fun setupSpinner() {
        val spinner: Spinner = popup.contentView.findViewById(R.id.time_setting_spinner)

        val time: Int = pref.getInt(id.toString() + "spinner_last", 6)
        spinner.setSelection(time)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View, position: Int, vId: Long
            ) {
                val editor: SharedPreferences.Editor = pref.edit()
                val min = context.resources.getStringArray(
                    R.array.time_settings_values)[parent.selectedItemPosition].toInt()
                Log.i(Global.TAG, "delay is set to: $min")

                if (min > 0) {
                    val positive = Utils.translateNumbers(context, "+$min")
                    delayTvs[id.ordinal]!!.text = positive
                }
                else if (min < 0)
                    delayTvs[id.ordinal]!!.text = Utils.translateNumbers(context, min.toString())
                else
                    delayTvs[id.ordinal]!!.text = ""

                Alarms(context, id)

                val millis = min * 60000L
                editor.putLong(id.toString() + "time_adjustment", millis)
                editor.putInt(id.toString() + "spinner_last", position)
                editor.apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun selectedAlertState(choice: Int) {
        val editor: SharedPreferences.Editor = pref.edit()
        editor.putInt(id.toString() + "notification_type", choice)
        editor.apply()

        images[id.ordinal]!!.setImageDrawable(
            ResourcesCompat.getDrawable(context.resources, drawables[choice], context.theme)
        )

        Alarms(context, id)
    }

    private fun getIndex(checkedId: Int): Int {
        for (i in rButtons.indices) {
            if (rButtons[i]!!.id == checkedId) return i
        }
        return 2
    }

    private fun getY(): Int {
        return when (id) {
            ID.FAJR -> -400
            ID.SHOROUQ -> -350
            ID.DUHR -> -180
            ID.ASR -> 100
            ID.MAGHRIB -> 350
            ID.ISHAA -> 510
            else -> 0
        }
    }

}