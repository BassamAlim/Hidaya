package bassamalim.hidaya.dialogs

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.activities.MainActivity
import bassamalim.hidaya.enums.PID
import bassamalim.hidaya.helpers.Alarms
import bassamalim.hidaya.helpers.Keeper
import bassamalim.hidaya.ui.components.CustomRadioGroup
import bassamalim.hidaya.ui.components.MyDialog
import bassamalim.hidaya.ui.components.MyText
import bassamalim.hidaya.ui.components.MyValuedSlider

class PrayerDialog(
    private val context: Context, private val pid: PID, private val prayerName: String,
    private val shown: MutableState<Boolean>, private val refresh: () -> Unit
) {

    private val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val offsetMin = 30
    private val notificationType = mutableStateOf(0)
    private val offset = mutableStateOf(0)
    private var sliderProgress = 30

    init {
        retrieveState()
    }

    private fun retrieveState() {
        val defaultState = if (pid == PID.SHOROUQ) 0 else 2
        val notificationState = pref.getInt("$pid notification_type", defaultState)
        notificationType.value = notificationState

        offset.value = pref.getInt("$pid offset", 0)
        sliderProgress = offset.value + offsetMin
    }

    private fun getNotificationTypes(): List<Pair<Int, Int>> {
        val lst = mutableListOf(
            Pair(R.string.disable_notification, R.drawable.ic_block),
            Pair(R.string.silent_notification, R.drawable.ic_silent),
            Pair(R.string.enable_notification, R.drawable.ic_sound)
        )
        if (pid != PID.SHOROUQ) lst.add(Pair(R.string.athan_speaker, R.drawable.ic_speaker))
        return lst.toList()
    }

    @Composable
    fun Dialog() {
        MyDialog(
            shown,
            onDismiss = {
                refresh()

                Keeper(context, MainActivity.location!!)
                Alarms(context, pid)
            }
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MyText(
                    String.format(context.getString(R.string.settings_of), prayerName),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 5.dp, bottom = 20.dp)
                )

                CustomRadioGroup(
                    options = getNotificationTypes(),
                    selection = notificationType,
                    onSelect = { selection ->
                        pref.edit()
                            .putInt("$pid notification_type", selection)
                            .apply()

                        Alarms(context, pid)
                    }
                )

                MyText(
                    stringResource(R.string.adjust_prayer_notification_time),
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 15.dp, bottom = 5.dp),
                    textAlign = TextAlign.Start
                )

                MyValuedSlider(
                    initialValue = sliderProgress,
                    valueRange = 0F..60F,
                    modifier = Modifier.fillMaxWidth(),
                    progressMin = offsetMin,
                    sliderFraction = 0.875F,
                    onValueChange = { value ->
                        pref.edit()
                            .putInt("$pid offset", value)
                            .apply()
                    }
                )
            }
        }
    }

}