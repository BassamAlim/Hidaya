package bassamalim.hidaya.fragments

import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.activities.MainActivity
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.databinding.FragmentPrayersBinding
import bassamalim.hidaya.dialogs.PrayerDialog
import bassamalim.hidaya.dialogs.TutorialDialog
import bassamalim.hidaya.helpers.PrayTimes
import bassamalim.hidaya.utils.DBUtils
import bassamalim.hidaya.utils.PTUtils
import bassamalim.hidaya.utils.PrefUtils
import bassamalim.hidaya.utils.LangUtils
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import java.util.*

class PrayersFragment : Fragment() {

    private var binding: FragmentPrayersBinding? = null
    private lateinit var db: AppDatabase
    private lateinit var location: Location
    private lateinit var prayerNames: Array<String>
    private lateinit var times: Array<Calendar?>
    private val cards = arrayOfNulls<CardView>(6)
    private val screens = arrayOfNulls<TextView>(6)
    private val images = arrayOfNulls<ImageView>(6)
    private val delayTvs = arrayOfNulls<TextView>(6)
    private lateinit var dayScreen: TextView
    private lateinit var pref: SharedPreferences
    private var currentDayChange = 0
    private lateinit var selectedDay: Calendar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPrayersBinding.inflate(inflater, container, false)
        val root: View = binding!!.root

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        db = DBUtils.getDB(requireContext())

        if (MainActivity.located) {
            init()

            checkFirstTime()
        }
        else
            binding!!.locationChangeBtn.setOnClickListener {
                parentFragmentManager.beginTransaction().replace(
                    R.id.nav_host_fragment_activity_main,
                    LocationFragment.newInstance("normal")
                ).commit()
            }

        return root
    }

    override fun onStart() {
        super.onStart()

        if (MainActivity.located) {
            goToToday()

            setupLocationCard()

            setInitialState()
            setupListeners()
        }
    }

    private fun init() {
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

        screens[0] = binding!!.fajrScreen
        screens[1] = binding!!.shorouqScreen
        screens[2] = binding!!.duhrScreen
        screens[3] = binding!!.asrScreen
        screens[4] = binding!!.maghribScreen
        screens[5] = binding!!.ishaaScreen

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
        currentDayChange = 0
        getTimes(0)
        updateDayScreen()
    }

    /**
     * It gets the prayer times for the current day and the next day, and sets the text of the
     * prayer time screens to the prayer times
     *
     * @param change The number of days to add to the current date.
     */
    private fun getTimes(change: Int) {
        val timeFormat = PrefUtils.getTimeFormat(requireContext())

        val prayTimes = PrayTimes(requireContext())

        val calendar = Calendar.getInstance()
        calendar[Calendar.DATE] = calendar[Calendar.DATE] + change

        selectedDay = calendar

        val utcOffset = PTUtils.getUTCOffset(requireContext(), pref)

        times = prayTimes.getPrayerTimes(
            location.latitude, location.longitude, utcOffset.toDouble(), calendar
        )
        val formattedTimes = prayTimes.getStrPrayerTimes(
            location.latitude, location.longitude, utcOffset.toDouble(), calendar, timeFormat
        )

        for (i in formattedTimes.indices) {
            val str = prayerNames[i] + ": " + formattedTimes[i]
            screens[i]!!.text = str
        }
    }

    private fun setInitialState() {
        for (i in cards.indices) {
            when (pref.getInt("${PTUtils.mapID(i)} notification_type", 2)) {
                3 -> images[i]!!.setImageDrawable(ResourcesCompat.getDrawable(requireContext()
                                .resources, R.drawable.ic_speaker, requireContext().theme))
                1 -> images[i]!!.setImageDrawable(ResourcesCompat.getDrawable(requireContext()
                                    .resources, R.drawable.ic_silent, requireContext().theme))
                0 -> images[i]!!.setImageDrawable(ResourcesCompat.getDrawable(requireContext()
                                    .resources, R.drawable.ic_disabled, requireContext().theme))
            }

            val delay = pref.getInt("${PTUtils.mapID(i)} offset", 0)

            if (delay > 0)
                delayTvs[i]!!.text =
                    LangUtils.translateNumbers(requireContext(), "+$delay", false)
            else if (delay < 0)
                delayTvs[i]!!.text =
                    LangUtils.translateNumbers(requireContext(), delay.toString(), false)
            else delayTvs[i]!!.text = ""
        }
    }

    private fun setupLocationCard() {
        val language = PrefUtils.getLanguage(requireContext(), pref)

        var countryId = pref.getInt("country_id", -1)
        var cityId = pref.getInt("city_id", -1)

        if (pref.getString("location_type", "auto") == "auto" || countryId == -1 || cityId == -1) {
            val closest = db.cityDao().getClosest(location.latitude, location.longitude)
            countryId = closest.countryId
            cityId = closest.id
        }

        val countryName =
            if (language == "en") db.countryDao().getNameEn(countryId)
            else db.countryDao().getNameAr(countryId)
        val cityName =
            if (language == "en") db.cityDao().getCity(cityId).nameEn
            else db.cityDao().getCity(cityId).nameAr

        val str = "$countryName, $cityName"
        binding!!.locationTv.text = str
    }

    private fun setupListeners() {
        val refresher = object : PrayerDialog.Refresher {
            override fun refresh() {
                onStart()
            }
        }
        for (i in cards.indices)
            cards[i]!!.setOnClickListener { v: View? ->
                PrayerDialog(requireContext(), v!!, PTUtils.mapID(i)!!, prayerNames[i], refresher)
            }

        binding!!.previousDayButton.setOnClickListener { previousDay() }
        binding!!.nextDayButton.setOnClickListener { nextDay() }
        binding!!.dayScreen.setOnClickListener { goToToday() }

        binding!!.locationChangeBtn.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(
                R.id.nav_host_fragment_activity_main, LocationFragment.newInstance("normal")
            ).commit()
        }
    }

    private fun checkFirstTime() {
        val prefKey = "is_first_time_in_prayers"
        if (pref.getBoolean(prefKey, true))
            TutorialDialog.newInstance(
                getString(R.string.prayers_tips),
                prefKey
            ).show(requireActivity().supportFragmentManager, TutorialDialog.TAG)
    }

    private fun previousDay() {
        getTimes(--currentDayChange)
        updateDayScreen()
    }

    private fun nextDay() {
        getTimes(++currentDayChange)
        updateDayScreen()
    }

    private fun updateDayScreen() {
        if (currentDayChange == 0) dayScreen.text = getString(R.string.day)
        else {
            val hijri: Calendar = UmmalquraCalendar()
            hijri.time = selectedDay.time

            val year = LangUtils.translateNumbers(
                requireContext(), hijri[Calendar.YEAR].toString(), false
            )
            val month = resources.getStringArray(R.array.hijri_months)[hijri[Calendar.MONTH]]
            val day = LangUtils.translateNumbers(
                requireContext(), hijri[Calendar.DATE].toString(), false
            )

            val str = "$day $month $year"
            dayScreen.text = str
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}