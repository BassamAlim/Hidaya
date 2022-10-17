package bassamalim.hidaya.dialogs

import android.annotation.SuppressLint
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
import bassamalim.hidaya.activities.MainActivity
import bassamalim.hidaya.enums.PID
import bassamalim.hidaya.helpers.Alarms
import bassamalim.hidaya.helpers.Keeper
import bassamalim.hidaya.other.DialogCallback
import bassamalim.hidaya.utils.LangUtils


class PrayerDialog(
    private val context: Context, private val view: View, private val pid: PID, title: String,
    private val callback: DialogCallback
) {

    private lateinit var popup: PopupWindow
    private val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private lateinit var radioGroup: RadioGroup
    private lateinit var rButtons: Array<RadioButton?>
    private lateinit var drawables: IntArray
    private lateinit var seekbar: SeekBar
    private lateinit var seekbarTv: TextView
    private val offsetMin = 30

    init {
        showPopup()
        populate(title)
    }

    private fun showPopup() {
        val inflater =
            view.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val popupView = inflater.inflate(
            R.layout.dialog_prayer, LinearLayout(context), false
        )

        if (pid == PID.SHOROUQ) popupView.findViewById<View>(R.id.athan_rb).visibility = View.GONE

        popup = PopupWindow(
            popupView, LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT, true
        )

        popup.elevation = 10f
        popup.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popup.isOutsideTouchable = true
        popup.animationStyle = R.style.PrayerDialogAnimation

        popup.showAtLocation(view, Gravity.START, 30, getY())

        popup.setOnDismissListener {
            callback.refresh()

            Keeper(context, MainActivity.location!!)
            Alarms(context, pid)
        }
    }

    @SuppressLint("StringFormatInvalid")
    private fun populate(title: String) {
        val nameScreen: TextView = popup.contentView.findViewById(R.id.prayer_name_tv)
        nameScreen.text = String.format(context.getString(R.string.settings_of), title)

        setViews()

        setupRadioGroup()

        setupSeekbar()

        retrieveState()
    }

    private fun setViews() {
        rButtons = arrayOfNulls(4)
        rButtons[0] = popup.contentView.findViewById(R.id.disable_rb)
        rButtons[1] = popup.contentView.findViewById(R.id.silent_rb)
        rButtons[2] = popup.contentView.findViewById(R.id.notify_rb)
        rButtons[3] = popup.contentView.findViewById(R.id.athan_rb)

        drawables = IntArray(4)
        drawables[0] = R.drawable.ic_block
        drawables[1] = R.drawable.ic_silent
        drawables[2] = R.drawable.ic_sound
        drawables[3] = R.drawable.ic_speaker
    }

    private fun setupRadioGroup() {
        radioGroup = popup.contentView.findViewById(R.id.prayer_alert_rg)

        radioGroup.setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
            pref.edit()
                .putInt("$pid notification_type", getIndex(checkedId))
                .apply()

            Alarms(context, pid)
        }
    }

    private fun setupSeekbar() {
        seekbar = popup.contentView.findViewById(R.id.time_offset_seekbar)
        seekbarTv = popup.contentView.findViewById(R.id.seekbar_tv)

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val offset = p1 - offsetMin
                setOffsetTv(offset)

                pref.edit()
                    .putInt("$pid offset", offset)
                    .apply()
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    private fun retrieveState() {
        val defaultState = if (pid == PID.SHOROUQ) 0 else 2
        val notificationState = pref.getInt("$pid notification_type", defaultState)
        radioGroup.check(rButtons[notificationState]!!.id)

        val offsetState = pref.getInt("$pid offset", 0)
        seekbar.progress = offsetState + offsetMin
        setOffsetTv(offsetState)
    }

    private fun setOffsetTv(offset: Int) {
        var offsetStr = offset.toString()
        if (offset > 0) offsetStr += "+"
        seekbarTv.text = LangUtils.translateNums(context, offsetStr)
    }

    private fun getIndex(checkedId: Int): Int {
        for (i in rButtons.indices) {
            if (rButtons[i]!!.id == checkedId) return i
        }
        return 2
    }

    private fun getY(): Int {
        return when (pid) {
            PID.FAJR -> -400
            PID.SHOROUQ -> -350
            PID.DUHR -> -180
            PID.ASR -> 100
            PID.MAGHRIB -> 350
            PID.ISHAA -> 510
            else -> 0
        }
    }

}