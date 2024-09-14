package bassamalim.hidaya.features.prayers.reminderSettings.ui

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
fun PrayerReminderSettingsDialog(
    viewModel: PrayerReminderSettingsViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MyDialog(
        shown = true,
        onDismiss = viewModel::onDismiss
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
                    state.prayerName.removePrefix("ا")
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
                value = state.offset + viewModel.offsetMin,
                valueRange = 0F..60F,
                modifier = Modifier.fillMaxWidth(),
                numeralsLanguage = viewModel.numeralsLanguage,
                progressMin = viewModel.offsetMin,
                sliderFraction = 0.875F,
                onValueChange = { value -> viewModel.onOffsetChange(value.toInt()) }
            )

            MyRow {
                SaveButton(onSave = viewModel::onSave)

                CancelButton(onDismiss = viewModel::onDismiss)
            }
        }
    }
}

@Composable
private fun RowScope.SaveButton(
    onSave: () -> Unit
) {
    MyHorizontalButton(
        text = stringResource(R.string.save),
        modifier = Modifier.weight(1f),
        fontSize = 24.sp,
        onClick = onSave
    )
}

@Composable
private fun RowScope.CancelButton(
    onDismiss: () -> Unit
) {
    MyHorizontalButton(
        text = stringResource(R.string.cancel),
        modifier = Modifier.weight(1f),
        fontSize = 24.sp,
        onClick = onDismiss
    )
}