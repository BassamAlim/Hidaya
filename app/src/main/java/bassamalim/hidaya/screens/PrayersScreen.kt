package bassamalim.hidaya.screens

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
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
import bassamalim.hidaya.R
import bassamalim.hidaya.activities.LocationActivity
import bassamalim.hidaya.dialogs.PrayerDialog
import bassamalim.hidaya.helpers.PrayTimes
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.ui.theme.nsp
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
    private val location: Location?
): NavigationScreen() {

    private val db = DBUtils.getDB(context)
    private val prayTimes = PrayTimes(context)
    private val prayerNames = context.resources.getStringArray(R.array.prayer_names)
    private val calendar = Calendar.getInstance()
    private var dayChange = mutableStateOf(0)
    private val settingsDialogShown = mutableStateOf(false)
    private var clickedPID = -1

    override fun onResume() {
        if (located) goToToday()
    }

    private fun goToToday() {
        dayChange.value = 0
        getTimes(0)
    }

    /**
     * It gets the prayer times for the current day and the next day, and sets the text of the
     * prayer time screens to the prayer times
     *
     * @param change The number of days to add to the current date.
     */
    private fun getTimes(change: Int): List<String> {
        calendar.timeInMillis = System.currentTimeMillis()
        calendar[Calendar.DATE] = calendar[Calendar.DATE] + change

        val utcOffset = PTUtils.getUTCOffset(context, pref)

        return prayTimes.getStrPrayerTimes(
            location!!.latitude, location.longitude, utcOffset.toDouble(), calendar
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

    private fun previousDay() {
        if (located) getTimes(--dayChange.value)
    }

    private fun nextDay() {
        if (located) getTimes(++dayChange.value)
    }

    private fun getDayText(): String {
        if (dayChange.value == 0) return context.getString(R.string.day)
        else {
            val hijri = UmmalquraCalendar()
            hijri.time = calendar.time

            val year = LangUtils.translateNums(
                context, hijri[Calendar.YEAR].toString(), false
            )
            val month = context.resources.getStringArray(
                R.array.hijri_months
            )[hijri[Calendar.MONTH]]
            val day = LangUtils.translateNums(
                context, hijri[Calendar.DATE].toString(), false
            )

            return "$day $month $year"
        }
    }

    @Composable
    fun PrayersUI() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MySurface(
                Modifier.padding(top = 5.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MyText(
                        text = if (located) getLocationName() else "",
                        modifier = Modifier
                            .widthIn(1.dp, 300.dp)
                            .padding(start = 15.dp)
                    )

                    MyIconBtn(
                        iconId = R.drawable.ic_location,
                        description = stringResource(R.string.locate),
                        tint = AppTheme.colors.text,
                        modifier = Modifier.padding(end = 8.dp),
                        size = 32.dp
                    ) {
                        val intent = Intent(context, LocationActivity::class.java)
                        intent.action = "normal"
                        context.startActivity(intent)
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
                PrayerCards(
                    if (located) getTimes(dayChange.value)
                    else listOf("", "", "", "", "", "")
                )
            }

            MySurface(
                Modifier.padding(bottom = 5.dp)
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
                        description = stringResource(R.string.previous_day_button_description),
                        tint = AppTheme.colors.text
                    ) {
                        previousDay()
                    }

                    MyClickableText(
                        text = getDayText(),
                        fontSize = 24.sp,
                        innerPadding = PaddingValues(vertical = 3.dp, horizontal = 15.dp)
                    ) {
                        goToToday()
                    }

                    MyIconBtn(
                        iconId = R.drawable.ic_right_arrow,
                        description = stringResource(R.string.next_day_button_description),
                        tint = AppTheme.colors.text
                    ) {
                        nextDay()
                    }
                }
            }
        }

        if (settingsDialogShown.value) {
            PrayerDialog(
                context, PTUtils.mapID(clickedPID)!!, prayerNames[clickedPID], settingsDialogShown
            ) {

            }.Dialog()
        }

        TutorialDialog(
            textResId = R.string.prayers_tips,
            prefKey = "is_first_time_in_prayers"
        )
    }

    @Composable
    private fun PrayerCards(times: List<String>) {
        for (pid in times.indices) PrayerCard(pid = pid, time = times[pid])
    }

    @Composable
    private fun PrayerCard(pid: Int, time: String) {
        val delay = pref.getInt("${PTUtils.mapID(pid)} offset", 0)

        MyClickableSurface(
            onClick = {
                if (located) {
                    clickedPID = pid
                    settingsDialogShown.value = true
                }
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
                    "${prayerNames[pid]}: $time",
                    fontSize = 33.nsp,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    // Delay
                    MyText(
                        if (delay > 0)
                            LangUtils.translateNums(context, "+$delay", false)
                        else if (delay < 0)
                            LangUtils.translateNums(context, delay.toString(), false)
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
                        contentDescription = stringResource(R.string.notification_image_description),
                        tint = AppTheme.colors.accent,
                        modifier = Modifier.size(35.dp)
                    )
                }
            }
        }
    }

}