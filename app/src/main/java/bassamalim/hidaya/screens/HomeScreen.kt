package bassamalim.hidaya.screens

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.CountDownTimer
import android.view.LayoutInflater
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import bassamalim.hidaya.R
import bassamalim.hidaya.activities.QuranViewer
import bassamalim.hidaya.helpers.PrayTimes
import bassamalim.hidaya.replacements.AnalogClock
import bassamalim.hidaya.ui.components.MyButton
import bassamalim.hidaya.ui.components.MyClickableText
import bassamalim.hidaya.ui.components.MySurface
import bassamalim.hidaya.ui.components.MyText
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.ui.theme.Positive
import bassamalim.hidaya.utils.LangUtils
import bassamalim.hidaya.utils.PTUtils
import bassamalim.hidaya.utils.PrefUtils
import java.util.*

class HomeScreen(
    private val context: Context,
    private val pref: SharedPreferences,
    private val located: Boolean,
    private val location: Location?
): NavigationScreen() {

    private lateinit var times: Array<Calendar?>
    private lateinit var formattedTimes: ArrayList<String>
    private lateinit var tomorrowFajr: Calendar
    private lateinit var formattedTomorrowFajr: String
    private var timer: CountDownTimer? = null
    private var tomorrow = false
    private val upcomingPrayer = mutableStateOf(0)
    private val upcomingPrayerTime = mutableStateOf("")
    private val remainingTime = mutableStateOf("")
    private val werdDone = mutableStateOf(false)
    private val pastTime = mutableStateOf(0L)
    private val upcomingTime = mutableStateOf(0L)
    private val remaining = mutableStateOf(0L)

    init {
        if (located) setupPrayersCard()
    }

    override fun onPause() {
        timer?.cancel()
    }

    override fun onResume() {
        if (pref.getBoolean("werd_done", false)) werdDone.value = true
    }

    private fun setupPrayersCard() {
        getTimes(location!!)
        setupUpcomingPrayer()
    }

    private fun getTimes(location: Location) {
        val utcOffset = PTUtils.getUTCOffset(context, pref)
        val timeFormat = PrefUtils.getTimeFormat(context, pref)

        val prayTimes = PrayTimes(context)

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
        upcomingPrayer.value = findUpcoming()

        tomorrow = false
        if (upcomingPrayer.value == -1) {
            tomorrow = true
            upcomingPrayer.value = 0
        }

        if (tomorrow) upcomingPrayerTime.value = formattedTomorrowFajr
        else upcomingPrayerTime.value = formattedTimes[upcomingPrayer.value]

        var till = times[upcomingPrayer.value]!!.timeInMillis
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

                remainingTime.value = String.format(
                    context.getString(R.string.remaining),
                    LangUtils.translateNums(context, hms, true)
                )

                val past =
                    if (upcomingPrayer.value == 0) -1L
                    else times[upcomingPrayer.value - 1]!!.timeInMillis

                pastTime.value = past
                upcomingTime.value = times[upcomingPrayer.value]!!.timeInMillis
                remaining.value = millisUntilFinished
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

    private fun getTelawatRecord(): String {
        val millis = pref.getLong("telawat_playback_record", 0L)

        val hours = millis / (60 * 60 * 1000) % 24
        val minutes = millis / (60 * 1000) % 60
        val seconds = millis / 1000 % 60

        return LangUtils.translateNums(
            context, String.format(
                Locale.US, "%02d:%02d:%02d",
                hours, minutes, seconds
            ), false
        )
    }

    @Composable
    fun HomeUI() {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MySurface(
                Modifier.padding(top = 3.dp)
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AndroidView(
                        factory = { context ->
                            val view = LayoutInflater.from(context).inflate(
                                R.layout.clock_view, null, false
                            ) as AnalogClock
                            // do whatever you want...
                            view // return the view
                        },
                        update = { view ->
                            // Update the view
                            view.update(pastTime, upcomingTime, remaining)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    )

                    MyText(
                        text =
                            if (located)
                                stringArrayResource(id = R.array.prayer_names)[upcomingPrayer.value]
                            else "",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(3.dp)
                    )

                    MyText(
                        text = upcomingPrayerTime.value,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(3.dp)
                    )

                    MyText(
                        text = remainingTime.value,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(top = 3.dp, bottom = 15.dp)
                    )
                }
            }

            MySurface {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MyText(stringResource(id = R.string.telawat_time_record_title))

                    MyText(getTelawatRecord(), fontSize = 30.sp)
                }
            }

            MySurface {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MyText(
                        stringResource(id = R.string.quran_pages_record_title),
                        textAlign = TextAlign.Start
                    )

                    MyText(
                        LangUtils.translateNums(
                            context,
                            pref.getInt("quran_pages_record", 0).toString(),
                            false),
                        fontSize = 30.sp
                    )
                }
            }

            MySurface(
                Modifier.padding(bottom = 3.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val werdPage = pref.getInt("today_werd_page", 25)
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MyText(stringResource(id = R.string.today_werd), fontSize = 22.sp)

                        MyText(
                            context.getString(R.string.page) +
                                    " ${LangUtils.translateNums(context, werdPage.toString())}",
                            fontSize = 22.sp
                        )
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MyClickableText(
                            stringResource(R.string.go_to_page),
                            textColor = AppTheme.colors.accent,
                            modifier = Modifier.padding(top = 10.dp, bottom = 5.dp)
                        ) {
                            val intent = Intent(context, QuranViewer::class.java)
                            intent.action = "by_page"
                            intent.putExtra("page", werdPage)
                            context.startActivity(intent)
                        }

                        Icon(
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = stringResource(
                                id = R.string.already_read_description
                            ),
                            tint = Positive,
                            modifier = Modifier
                                .size(40.dp)
                                .alpha(if (werdDone.value) 1F else 0F)
                        )
                    }
                }
            }
        }
    }

}

