package bassamalim.hidaya.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R
import bassamalim.hidaya.enum.NotificationType
import bassamalim.hidaya.enum.PID
import bassamalim.hidaya.ui.components.MyClickableSurface
import bassamalim.hidaya.ui.components.MyDialog
import bassamalim.hidaya.ui.components.MyText
import bassamalim.hidaya.ui.components.MyValuedSlider
import bassamalim.hidaya.ui.theme.AppTheme

@Composable
fun PrayerDialog(
    shown: Boolean,
    pid: PID,
    notificationTypes: List<NotificationType>,
    offsets: List<Int>,
    onNotificationTypeChange: (NotificationType) -> Unit,
    onOffsetChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val notificationOptions = listOf(
        Pair(R.string.athan_speaker, R.drawable.ic_speaker),
        Pair(R.string.enable_notification, R.drawable.ic_sound),
        Pair(R.string.silent_notification, R.drawable.ic_silent),
        Pair(R.string.disable_notification, R.drawable.ic_block)
    )
    val offsetMin = 30f
    val sliderProgress = offsets[pid.ordinal] + offsetMin

    MyDialog(
        shown,
        onDismiss = { onDismiss() }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MyText(
                String.format(
                    stringResource(R.string.settings_of),
                    context.resources.getStringArray(R.array.prayer_names)[pid.ordinal]
                ),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 5.dp, bottom = 20.dp)
            )

            CustomRadioGroup(
                pid = pid,
                options = notificationOptions,
                selection = notificationTypes[pid.ordinal],
                onSelect = { selection -> onNotificationTypeChange(selection) }
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
                onValueChange = { value -> onOffsetChange(value.toInt()) }
            )
        }
    }
}

@Composable
fun CustomRadioGroup(
    pid: PID,
    options: List<Pair<Int, Int>>,
    selection: NotificationType,
    onSelect: (NotificationType) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        options.forEachIndexed { i, pair ->
            if (!(pid == PID.SUNRISE && i == 0)) {
                val text = stringResource(pair.first)

                Box(
                    Modifier.padding(vertical = 6.dp)
                ) {
                    MyClickableSurface(
                        padding = PaddingValues(vertical = 0.dp),
                        modifier =
                        if (i == selection.ordinal)
                            Modifier.border(
                                width = 3.dp,
                                color = AppTheme.colors.accent,
                                shape = RoundedCornerShape(10.dp)
                            )
                        else Modifier,
                        onClick = { onSelect(selection) }
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp, horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(pair.second),
                                contentDescription = text
                            )

                            MyText(text,
                                textColor =
                                    if (i == selection.ordinal) AppTheme.colors.accent
                                    else AppTheme.colors.text,
                                modifier = Modifier.padding(start = 20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}