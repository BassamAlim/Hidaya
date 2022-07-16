package bassamalim.hidaya.fragments

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.location.Location
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.activities.MainActivity
import bassamalim.hidaya.databinding.FragmentPrayersBinding
import bassamalim.hidaya.dialogs.PrayerDialog
import bassamalim.hidaya.dialogs.TutorialDialog
import bassamalim.hidaya.helpers.PrayTimes
import bassamalim.hidaya.other.Utils
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import java.util.*

class PrayersFragment : Fragment() {

    private var binding: FragmentPrayersBinding? = null
    private lateinit var location: Location
    private lateinit var prayerNames: Array<String>
    private lateinit var times: Array<Calendar?>
    private lateinit var tomorrowFajr: Calendar
    private val cards = arrayOfNulls<CardView>(6)
    private val cls = arrayOfNulls<ConstraintLayout>(6)
    private val screens = arrayOfNulls<TextView>(6)
    private val counters = arrayOfNulls<TextView>(6)
    private val images = arrayOfNulls<ImageView>(6)
    private val delayTvs = arrayOfNulls<TextView>(6)
    private lateinit var dayScreen: TextView
    private var timer: CountDownTimer? = null
    private lateinit var pref: SharedPreferences
    private var currentChange = 0
    private lateinit var selectedDay: Calendar
    private var upcoming = 0
    private val constraintSet = ConstraintSet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPrayersBinding.inflate(inflater, container, false)
        val root: View = binding!!.root

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        if (MainActivity.located) {
            initiate()
            goToToday()
            setInitialState()
            setListeners()
        }

        checkFirstTime()

        return root
    }

    private fun initiate() {
        location = MainActivity.location!!
        prayerNames = resources.getStringArray(R.array.prayer_names)
        setViews()
    }

    private fun setViews() {
        cards[0] = binding!!.fajrCard
        cards[1] = binding!!.shorouqCard
        cards[2] = binding!!.duhrCard
        cards[3] = binding!!.asrCard
        cards[4] = binding!!.maghribCard
        cards[5] = binding!!.ishaaCard
        cls[0] = binding!!.fajrCl
        cls[1] = binding!!.shorouqCl
        cls[2] = binding!!.duhrCl
        cls[3] = binding!!.asrCl
        cls[4] = binding!!.maghribCl
        cls[5] = binding!!.ishaaCl

        screens[0] = binding!!.fajrScreen
        screens[1] = binding!!.shorouqScreen
        screens[2] = binding!!.duhrScreen
        screens[3] = binding!!.asrScreen
        screens[4] = binding!!.maghribScreen
        screens[5] = binding!!.ishaaScreen

        counters[0] = binding!!.fajrCounter
        counters[1] = binding!!.shorouqCounter
        counters[2] = binding!!.duhrCounter
        counters[3] = binding!!.asrCounter
        counters[4] = binding!!.maghribCounter
        counters[5] = binding!!.ishaaCounter

        images[0] = binding!!.fajrImage
        images[1] = binding!!.shorouqImage
        images[2] = binding!!.duhrImage
        images[3] = binding!!.asrImage
        images[4] = binding!!.maghribImage
        images[5] = binding!!.ishaaImage

        delayTvs[0] = binding!!.fajrDelayTv
        delayTvs[1] = binding!!.shorouqDelayTv
        delayTvs[2] = binding!!.duhrDelayTv
        delayTvs[3] = binding!!.asrDelayTv
        delayTvs[4] = binding!!.maghribDelayTv
        delayTvs[5] = binding!!.ishaaDelayTv

        dayScreen = binding!!.dayScreen
    }

    private fun goToToday() {
        currentChange = 0
        getTimes(0)
        updateDayScreen()
        count()
    }

    /**
     * It gets the prayer times for the current day and the next day, and sets the text of the
     * prayer time screens to the prayer times
     *
     * @param change The number of days to add to the current date.
     */
    private fun getTimes(change: Int) {
        val prayTimes = PrayTimes(context!!)

        val calendar = Calendar.getInstance()
        val date = Date()
        calendar.time = date
        calendar[Calendar.DATE] = calendar[Calendar.DATE] + change

        selectedDay = calendar

        val timeZoneObj = TimeZone.getDefault()
        val millis = timeZoneObj.getOffset(date.time).toLong()
        val timezone = millis / 3600000.0

        times = prayTimes.getPrayerTimesArray(
            calendar, location.latitude, location.longitude, timezone
        )
        val formattedTimes = prayTimes.getPrayerTimes(
            calendar, location.latitude, location.longitude, timezone
        )
        tomorrowFajr = prayTimes.getTomorrowFajr(
            calendar, location.latitude, location.longitude, timezone
        )
        tomorrowFajr[Calendar.SECOND] = 0

        for (i in formattedTimes.indices) {
            val text = prayerNames[i] + ": " + formattedTimes[i]
            screens[i]!!.text = text

            times[i]!![Calendar.SECOND] = 0
        }
    }

    private fun setInitialState() {
        for (i in cards.indices) {
            when (pref.getInt(Utils.mapID(i).toString() + "notification_type", 2)) {
                3 -> images[i]!!.setImageDrawable(ResourcesCompat.getDrawable(requireContext()
                                .resources, R.drawable.ic_speaker, requireContext().theme))
                1 -> images[i]!!.setImageDrawable(ResourcesCompat.getDrawable(requireContext()
                                    .resources, R.drawable.ic_silent, requireContext().theme))
                0 -> images[i]!!.setImageDrawable(ResourcesCompat.getDrawable(requireContext()
                                    .resources, R.drawable.ic_disabled, requireContext().theme))
            }

            val delayPosition = pref.getInt(Utils.mapID(i).toString() + "spinner_last", 6)
            val min = resources.getStringArray(R.array.time_settings_values)[delayPosition].toInt()

            if (min > 0) {
                val positive = Utils.translateNumbers(context!!, "+$min")
                delayTvs[i]!!.text = positive
            }
            else if (min < 0) delayTvs[i]!!.text = Utils.translateNumbers(context!!, min.toString())
            else delayTvs[i]!!.text = ""
        }
    }

    private fun setListeners() {
        for (i in cards.indices) {
            cards[i]!!.setOnClickListener { v: View? ->
                PrayerDialog(context!!, v!!, Utils.mapID(i)!!, prayerNames[i])
            }
        }

        binding!!.previousDayButton.setOnClickListener {previousDay()}
        binding!!.nextDayButton.setOnClickListener {nextDay()}
        binding!!.dayScreen.setOnClickListener {goToToday()}
    }

    private fun checkFirstTime() {
        val prefKey = "is_first_time_in_prayers"
        if (pref.getBoolean(prefKey, true))
            TutorialDialog.newInstance(
                getString(R.string.prayers_tips),
                prefKey
            ).show(requireActivity().supportFragmentManager, TutorialDialog.TAG)
    }

    private fun count() {
        upcoming = findUpcoming()

        var tomorrow = false
        if (upcoming == -1) {
            tomorrow = true
            upcoming = 0
        }

        counters[upcoming]!!.visibility = View.VISIBLE
        constraintSet.clone(cls[upcoming])
        constraintSet.clear(screens[upcoming]!!.id, ConstraintSet.BOTTOM)
        constraintSet.applyTo(cls[upcoming])

        var till = times[upcoming]!!.timeInMillis
        if (tomorrow) till = tomorrowFajr.timeInMillis

        val restart = booleanArrayOf(true)
        timer = object : CountDownTimer(till - System.currentTimeMillis(),
            1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = millisUntilFinished / (60 * 60 * 1000) % 24
                val minutes = millisUntilFinished / (60 * 1000) % 60
                val seconds = millisUntilFinished / 1000 % 60

                val hms = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

                if (context != null)
                    counters[upcoming]!!.text =
                        String.format(getString(R.string.remaining), Utils.translateNumbers(requireContext(), hms))
                else {
                    restart[0] = false
                    cancelTimer()
                }
            }

            override fun onFinish() {
                constraintSet.connect(
                    screens[upcoming]!!.id, ConstraintSet.BOTTOM,
                    cls[upcoming]!!.id, ConstraintSet.BOTTOM
                )
                constraintSet.applyTo(cls[upcoming])
                counters[upcoming]!!.visibility = View.GONE

                if (restart[0])
                    count()
            }
        }.start()
    }

    private fun findUpcoming(): Int {
        val currentMillis = System.currentTimeMillis()
        for (i in times.indices) {
            val millis = times[i]!!.timeInMillis
            if (millis > currentMillis) return i
        }
        return -1
    }

    private fun previousDay() {
        getTimes(--currentChange)
        updateDayScreen()
        cancelTimer()
        if (currentChange == 0) count()
    }

    private fun nextDay() {
        getTimes(++currentChange)
        updateDayScreen()
        cancelTimer()
        if (currentChange == 0) count()
    }

    private fun updateDayScreen() {
        if (currentChange == 0)
            dayScreen.text = getString(R.string.day)
        else {
            var text = ""

            val hijri: Calendar = UmmalquraCalendar()
            hijri.time = selectedDay.time

            val year = " " + hijri[Calendar.YEAR]
            val month = " " + resources.getStringArray(R.array.hijri_months)[Calendar.MONTH]
            val day = "" + hijri[Calendar.DATE]

            text += Utils.translateNumbers(context!!, day) + month +
                    Utils.translateNumbers(context!!, year)
            dayScreen.text = text
        }
    }

    private fun cancelTimer() {
        if (timer != null) {
            timer!!.cancel()
            constraintSet.connect(
                screens[upcoming]!!.id, ConstraintSet.BOTTOM,
                cls[upcoming]!!.id, ConstraintSet.BOTTOM
            )
            constraintSet.applyTo(cls[upcoming])
            counters[upcoming]!!.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        cancelTimer()
    }

}