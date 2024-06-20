package bassamalim.hidaya.features.prayerReminder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyDialog
import bassamalim.hidaya.core.ui.components.MyHorizontalButton
import bassamalim.hidaya.core.ui.components.MyRow
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.MyValuedSlider

@Composable
fun PrayerReminderDialog(
    vm: PrayerReminderViewModel
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
                String.format(
                    stringResource(R.string.reminder_of),
                    st.prayerName.removePrefix("ا")
                ),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 5.dp, bottom = 20.dp)
            )

            MyText(
                stringResource(R.string.reminder_time),
                Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp, bottom = 5.dp),
                textAlign = TextAlign.Start
            )

            MyValuedSlider(
                initialValue = st.offset + vm.offsetMin,
                valueRange = 0F..60F,
                modifier = Modifier.fillMaxWidth(),
                progressMin = vm.offsetMin,
                sliderFraction = 0.875F,
                onValueChange = { value -> vm.onOffsetChange(value.toInt()) }
            )

            MyRow {
                SaveBtn(vm)

                CancelBtn(vm)
            }
        }
    }
}

@Composable
private fun RowScope.SaveBtn(vm: PrayerReminderViewModel) {
    MyHorizontalButton(
        text = stringResource(R.string.save),
        fontSize = 24.sp,
        modifier = Modifier.weight(1f),
        onClick = { vm.onSave() }
    )
}

@Composable
private fun RowScope.CancelBtn(vm: PrayerReminderViewModel) {
    MyHorizontalButton(
        text = stringResource(R.string.cancel),
        fontSize = 24.sp,
        modifier = Modifier.weight(1f),
        onClick = { vm.onDismiss() }
    )
}