package bassamalim.hidaya.features.prayerSetting

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.ui.components.MyClickableSurface
import bassamalim.hidaya.core.ui.components.MyDialog
import bassamalim.hidaya.core.ui.components.MyHorizontalButton
import bassamalim.hidaya.core.ui.components.MyRow
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.MyValuedSlider
import bassamalim.hidaya.core.ui.theme.AppTheme

@Composable
fun PrayerSettingsDialog(
    vm: PrayerSettingVM
) {
    val st by vm.uiState.collectAsStateWithLifecycle()

    MyDialog(
        shown = true,
        onDismiss = { vm.onDismiss() }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MyText(
                String.format(stringResource(R.string.settings_of), st.prayerName),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 5.dp, bottom = 20.dp)
            )

            CustomRadioGroup(
                vm = vm,
                pid = st.pid,
                selection = st.notificationType,
                onSelect = { selection -> vm.onNotificationTypeChange(selection) }
            )

            MyText(
                stringResource(R.string.adjust_prayer_notification_time),
                Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp, bottom = 5.dp),
                textAlign = TextAlign.Start
            )

            MyValuedSlider(
                initialValue = st.timeOffset + vm.offsetMin,
                valueRange = 0F..60F,
                modifier = Modifier.fillMaxWidth(),
                progressMin = vm.offsetMin,
                sliderFraction = 0.875F,
                onValueChange = { value -> vm.onTimeOffsetChange(value.toInt()) }
            )

            MyRow {
                SaveBtn(vm)

                CancelBtn(vm)
            }
        }
    }
}

@Composable
private fun CustomRadioGroup(
    vm: PrayerSettingVM,
    pid: PID,
    selection: NotificationType,
    onSelect: (NotificationType) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        vm.notificationTypeOptions.forEachIndexed { i, pair ->
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
                        onClick = { onSelect(NotificationType.values()[i]) }
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

@Composable
private fun RowScope.SaveBtn(vm: PrayerSettingVM) {
    MyHorizontalButton(
        text = stringResource(R.string.save),
        fontSize = 24.sp,
        modifier = Modifier.weight(1f),
        onClick = { vm.onSave() }
    )
}

@Composable
private fun RowScope.CancelBtn(vm: PrayerSettingVM) {
    MyHorizontalButton(
        text = stringResource(R.string.cancel),
        fontSize = 24.sp,
        modifier = Modifier.weight(1f),
        onClick = { vm.onDismiss() }
    )
}