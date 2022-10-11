package bassamalim.hidaya.screens

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentManager
import bassamalim.hidaya.R
import bassamalim.hidaya.dialogs.TutorialDialog
import bassamalim.hidaya.helpers.PrayTimes
import bassamalim.hidaya.ui.components.MyIconBtn
import bassamalim.hidaya.ui.components.MySurface
import bassamalim.hidaya.ui.components.MyText
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.DBUtils
import bassamalim.hidaya.utils.LangUtils
import bassamalim.hidaya.utils.PTUtils
import bassamalim.hidaya.utils.PrefUtils
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import java.util.*

class PrayersScreen(
    private val context: Context,
    private val pref: SharedPreferences,
    private val located: Boolean,
    private val location: Location?,
    private val supportFragmentManager: FragmentManager
) {

    private val db = DBUtils.getDB(context)
    private val prayTimes = PrayTimes(context)
    private val prayerNames = context.resources.getStringArray(R.array.prayer_names)
    private lateinit var times: Array<Calendar?>
    private lateinit var formattedTimes: List<String>
    private val calendar = Calendar.getInstance()
    private val dateText = mutableStateOf(context.getString(R.string.day))
    private var currentDayChange = 0

    init {
        if (located) {
            checkFirstTime()

            goToToday()
        }
    }

    fun onResume() {
        if (located) updateDayScreen()
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
        val timeFormat = PrefUtils.getTimeFormat(context)

        calendar.timeInMillis = System.currentTimeMillis()
        calendar[Calendar.DATE] = calendar[Calendar.DATE] + change

        val utcOffset = PTUtils.getUTCOffset(context, pref)

        times = prayTimes.getPrayerTimes(
            location!!.latitude, location.longitude, utcOffset.toDouble(), calendar
        )
        formattedTimes = prayTimes.getStrPrayerTimes(
            location.latitude, location.longitude, utcOffset.toDouble(), calendar, timeFormat
        )
    }

    private fun getLocationName(): String {
        val language = PrefUtils.getLanguage(context, pref)

        var countryId = pref.getInt("country_id", -1)
        var cityId = pref.getInt("city_id", -1)

        if (pref.getString("location_type", "auto") == "auto" || countryId == -1 || cityId == -1) {
            val closest = db.cityDao().getClosest(location!!.latitude, location.longitude)
            countryId = closest.countryId
            cityId = closest.id
        }

        val countryName =
            if (language == "en") db.countryDao().getNameEn(countryId)
            else db.countryDao().getNameAr(countryId)
        val cityName =
            if (language == "en") db.cityDao().getCity(cityId).nameEn
            else db.cityDao().getCity(cityId).nameAr

        return "$countryName, $cityName"
    }

    private fun checkFirstTime() {
        val prefKey = "is_first_time_in_prayers"
        if (pref.getBoolean(prefKey, true))
            TutorialDialog.newInstance(
                context.getString(R.string.prayers_tips),
                prefKey
            ).show(supportFragmentManager, TutorialDialog.TAG)
    }

    private fun previousDay() {
        if (located) {
            getTimes(--currentDayChange)
            updateDayScreen()
        }
    }

    private fun nextDay() {
        if (located) {
            getTimes(++currentDayChange)
            updateDayScreen()
        }
    }

    private fun updateDayScreen() {
        if (currentDayChange == 0) dateText.value = context.getString(R.string.day)
        else {
            val hijri = UmmalquraCalendar()
            hijri.time = calendar.time

            val year = LangUtils.translateNums(
                context, hijri[Calendar.YEAR].toString(), false
            )
            val month = context.resources.getStringArray(R.array.hijri_months)[hijri[Calendar.MONTH]]
            val day = LangUtils.translateNums(
                context, hijri[Calendar.DATE].toString(), false
            )

            val str = "$day $month $year"
            dateText.value = str
        }
    }

    @Composable
    fun PrayersUI() {
        Column(
            Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MySurface(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp, horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MyText(text = if (located) getLocationName() else "")

                    MyIconBtn(
                        iconId = R.drawable.ic_location,
                        description = stringResource(id = R.string.locate),
                        tint = AppTheme.colors.text
                    ) {
                        supportFragmentManager.beginTransaction().replace(
                            R.id.nav_host_fragment_activity_main,
                            LocationScreen.newInstance("normal")
                        ).commit()
                    }
                }
            }

            Column(
                Modifier
                    .weight(1F)
                    .padding(vertical = 10.dp, horizontal = 6.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceAround
            ) {
                for (pid in prayerNames.indices) PrayerCard(pid)
            }

            MySurface(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 5.dp, end = 5.dp, bottom = 5.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MyIconBtn(
                        iconId = R.drawable.ic_left_arrow,
                        description = stringResource(id = R.string.previous_day_button_description),
                        tint = AppTheme.colors.text
                    ) {
                        previousDay()
                    }

                    MyText(
                        text = dateText.value,
                        fontSize = 24.sp,
                        modifier = Modifier.clickable { goToToday() }
                    )

                    MyIconBtn(
                        iconId = R.drawable.ic_right_arrow,
                        description = stringResource(id = R.string.next_day_button_description),
                        tint = AppTheme.colors.text
                    ) {
                        nextDay()
                    }
                }
            }
        }
    }

    @Composable
    private fun PrayerCard(pid: Int) {
        val delay = pref.getInt("${PTUtils.mapID(pid)} offset", 0)

        MySurface(
            Modifier
                .fillMaxWidth()
                .clickable {
//                    if (located) PrayerDialog(context, v!!, PTUtils.mapID(i)!!, prayerNames[i], refresher)
                },
            cornerRadius = 15.dp
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Prayer name
                MyText(
                    if (located) "${prayerNames[pid]}: ${formattedTimes[pid]}" else "",
                    fontSize = 33.sp,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    // Delay
                    MyText(
                        if (delay > 0) LangUtils.translateNums(
                            context, "+$delay", false
                        )
                        else if (delay < 0) LangUtils.translateNums(
                            context, delay.toString(), false
                        )
                        else ""
                    )

                    // Notification type
                    val notificationType = pref.getInt(
                        "${PTUtils.mapID(pid)} notification_type",
                        if (pid == 1) 0 else 2
                    )
                    Icon(
                        painter = painterResource(
                            when (notificationType) {
                                3 -> R.drawable.ic_speaker
                                1 -> R.drawable.ic_silent
                                0 -> R.drawable.ic_block
                                else -> R.drawable.ic_sound
                            }
                        ),
                        contentDescription = stringResource(
                            id = R.string.notification_image_description
                        ),
                        tint = AppTheme.colors.accent,
                        modifier = Modifier.size(35.dp)
                    )
                }
            }
        }
    }

}