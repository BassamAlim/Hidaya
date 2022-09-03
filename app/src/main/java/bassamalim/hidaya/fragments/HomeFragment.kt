package bassamalim.hidaya.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.activities.MainActivity
import bassamalim.hidaya.activities.QuranViewer
import bassamalim.hidaya.databinding.FragmentHomeBinding
import bassamalim.hidaya.helpers.PrayTimes
import bassamalim.hidaya.utils.LangUtils
import bassamalim.hidaya.utils.PTUtils
import bassamalim.hidaya.utils.PrefUtils
import java.util.*

class HomeFragment : Fragment() {

    private var binding: FragmentHomeBinding? = null
    private lateinit var pref: SharedPreferences
    private lateinit var location: Location
    private lateinit var times: Array<Calendar?>
    private lateinit var formattedTimes: ArrayList<String>
    private lateinit var tomorrowFajr: Calendar
    private lateinit var formattedTomorrowFajr: String
    private var timer: CountDownTimer? = null
    private var upcoming = 0
    private var tomorrow = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding!!.root

        if (MainActivity.located) setupPrayersCard()

        setupTelawatRecordCard()
        setupQuranRecordCard()

        return root
    }

    override fun onResume() {
        super.onResume()

        setupWerdCard()
    }

    private fun setupPrayersCard() {
        location = MainActivity.location!!
        getTimes(location)
        setupUpcomingPrayer()
    }

    private fun getTimes(location: Location) {
        val utcOffset = PTUtils.getUTCOffset(requireContext(), pref)
        val timeFormat = PrefUtils.getTimeFormat(requireContext(), pref)

        val prayTimes = PrayTimes(requireContext())

        val today = Calendar.getInstance()
        times = prayTimes.getPrayerTimes(
            location.latitude, location.longitude, utcOffset.toDouble(), today
        )
        formattedTimes = prayTimes.getStrPrayerTimes(
            location.latitude, location.longitude, utcOffset.toDouble(), today, timeFormat
        )

        val tomorrow = Calendar.getInstance()
        tomorrow[Calendar.DATE]++
        tomorrowFajr = prayTimes.getPrayerTimes(
            location.latitude, location.longitude, utcOffset.toDouble(), tomorrow
        )[0]!!
        tomorrowFajr[Calendar.DATE]++
        formattedTomorrowFajr = prayTimes.getStrPrayerTimes(
            location.latitude, location.longitude, utcOffset.toDouble(), tomorrow, timeFormat
        )[0]
    }

    private fun setupUpcomingPrayer() {
        upcoming = findUpcoming()

        tomorrow = false
        if (upcoming == -1) {
            tomorrow = true
            upcoming = 0
        }

        binding!!.prayerNameTv.text = resources.getStringArray(R.array.prayer_names)[upcoming]
        if (tomorrow) binding!!.prayerTimeTv.text = formattedTomorrowFajr
        else binding!!.prayerTimeTv.text = formattedTimes[upcoming]

        var till = times[upcoming]!!.timeInMillis
        if (tomorrow) till = tomorrowFajr.timeInMillis

        count(till)
    }

    private fun count(till: Long) {
        timer = object : CountDownTimer(
            till - System.currentTimeMillis(), 1000
        ) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = millisUntilFinished / (60 * 60 * 1000) % 24
                val minutes = millisUntilFinished / (60 * 1000) % 60
                val seconds = millisUntilFinished / 1000 % 60

                val hms = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

                if (binding != null) {
                    binding!!.remainingTimeTv.text = String.format(
                        getString(R.string.remaining),
                        LangUtils.translateNums(requireContext(), hms, true)
                    )

                    binding!!.clock.setRemaining(millisUntilFinished)
                }
                else cancel()
            }

            override fun onFinish() {
                setupUpcomingPrayer()
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

    private fun setupTelawatRecordCard() {
        val millis = pref.getLong("telawat_playback_record", 0L)

        val hours = millis / (60 * 60 * 1000) % 24
        val minutes = millis / (60 * 1000) % 60
        val seconds = millis / 1000 % 60
        val hms = LangUtils.translateNums(
            requireContext(), String.format(Locale.US, "%02d:%02d:%02d",
                hours, minutes, seconds), false
        )

        binding!!.telawatTimeDuration.text = hms
    }

    private fun setupQuranRecordCard() {
        binding!!.quranPagesNum.text = LangUtils.translateNums(
            requireContext(), pref.getInt("quran_pages_record", 0).toString(), false
        )
    }

    private fun setupWerdCard() {
        val page = pref.getInt("today_werd_page", 25)
        val text = "${getString(R.string.page)} ${LangUtils.translateNums(requireContext(), page.toString())}"
        binding!!.werdText.text = text

        if (pref.getBoolean("werd_done", false)) binding!!.werdIv.visibility = View.VISIBLE

        binding!!.readBtn.setOnClickListener {
            val intent = Intent(context, QuranViewer::class.java)
            intent.action = "by_page"
            intent.putExtra("page", page)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        timer?.cancel()
    }

}