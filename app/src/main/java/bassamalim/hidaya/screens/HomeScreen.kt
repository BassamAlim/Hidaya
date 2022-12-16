package bassamalim.hidaya.screens

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.CountDownTimer
import android.view.LayoutInflater
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import bassamalim.hidaya.other.AnalogClock
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
    private var upcomingPrayer = 0
    private var upcomingPrayerTime = ""
    private var pastTime = 0L
    private var upcomingTime = 0L
    private var remaining = 0L
    private val remainingTime = mutableStateOf("")
    private val werdDone = mutableStateOf(false)

    init {
        onResume()
    }

    override fun onPause() {
        timer?.cancel()
    }

    override fun onResume() {
        if (located) setupPrayersCard()

        werdDone.value = PrefUtils.getBoolean(pref, "werd_done", false)
    }

    private fun setupPrayersCard() {
        getTimes(location!!)
        setupUpcomingPrayer()
    }

    private fun getTimes(location: Location) {
        val utcOffset = PTUtils.getUTCOffset(context, pref)

        val prayTimes = PrayTimes(context)

        val today = Calendar.getInstance()
        times = prayTimes.getPrayerTimes(
            location.latitude, location.longitude, utcOffset.toDouble(), today
        )
        formattedTimes = prayTimes.getStrPrayerTimes(
            location.latitude, location.longitude, utcOffset.toDouble(), today
        )

        val tomorrow = Calendar.getInstance()
        tomorrow[Calendar.DATE]++
        tomorrowFajr = prayTimes.getPrayerTimes(
            location.latitude, location.longitude, utcOffset.toDouble(), tomorrow
        )[0]!!
        tomorrowFajr[Calendar.DATE]++
        formattedTomorrowFajr = prayTimes.getStrPrayerTimes(
            location.latitude, location.longitude, utcOffset.toDouble(), tomorrow
        )[0]
    }

    private fun setupUpcomingPrayer() {
        upcomingPrayer = findUpcoming()

        tomorrow = false
        if (upcomingPrayer == -1) {
            tomorrow = true
            upcomingPrayer = 0
        }

        upcomingPrayerTime =
            if (tomorrow) formattedTomorrowFajr
            else formattedTimes[upcomingPrayer]

        var till = times[upcomingPrayer]!!.timeInMillis
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
                    if (upcomingPrayer == 0) -1L
                    else times[upcomingPrayer - 1]!!.timeInMillis

                pastTime = past
                upcomingTime = times[upcomingPrayer]!!.timeInMillis
                remaining = millisUntilFinished
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
        val millis = PrefUtils.getLong(pref, "telawat_playback_record", 0L)

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

    @OptIn(ExperimentalAnimationApi::class)
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
                                stringArrayResource(id = R.array.prayer_names)[upcomingPrayer]
                            else "",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(3.dp)
                    )

                    MyText(
                        text = upcomingPrayerTime,
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
                    MyText(
                        stringResource(R.string.telawat_time_record_title),
                        Modifier.widthIn(1.dp, 200.dp)
                    )

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
                        stringResource(R.string.quran_pages_record_title),
                        textAlign = TextAlign.Start,
                        modifier = Modifier.widthIn(1.dp, 280.dp)
                    )

                    MyText(
                        LangUtils.translateNums(
                            context,
                            PrefUtils.getInt(pref, "quran_pages_record", 0).toString(),
                            false
                        ),
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
                    val werdPage = PrefUtils.getInt(pref, "today_werd_page", 25)
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MyText(stringResource(R.string.today_werd), fontSize = 22.sp)

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

                        AnimatedVisibility(
                            visible = werdDone.value,
                            enter = scaleIn()
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = stringResource(R.string.already_read_description),
                                tint = Positive,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
            }
        }
    }

}

