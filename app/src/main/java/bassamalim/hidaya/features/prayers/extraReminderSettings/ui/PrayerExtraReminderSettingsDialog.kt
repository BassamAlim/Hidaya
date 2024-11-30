package bassamalim.hidaya.features.prayers.extraReminderSettings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
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
import bassamalim.hidaya.core.ui.components.DialogDismissButton
import bassamalim.hidaya.core.ui.components.DialogSubmitButton
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.MyValuedSlider

@Composable
fun PrayerExtraReminderSettingsDialog(viewModel: PrayerExtraReminderSettingsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) return

    AlertDialog(
        onDismissRequest = viewModel::onDismiss,
        dismissButton = {
            DialogDismissButton { viewModel.onDismiss() }
        },
        confirmButton = {
            DialogSubmitButton(text = stringResource(R.string.save)) {
                viewModel.onSave()
            }
        },
        title = {
            MyText(
                text = String.format(
                    stringResource(R.string.reminder_of),
                    state.prayerName.removePrefix("ا")
                ),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 5.dp, bottom = 20.dp)
            )
        },
        text = {
            DialogContent(viewModel, state)
        }
    )
}

@Composable
private fun DialogContent(
    viewModel: PrayerExtraReminderSettingsViewModel,
    state: PrayerExtraReminderSettingsUiState
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MyText(
            text = stringResource(R.string.reminder_time),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            textAlign = TextAlign.Start
        )

        MyValuedSlider(
            value = state.offset + viewModel.offsetMin,
            valueRange = 0F..60F,
            modifier = Modifier.fillMaxWidth(),
            progressMin = viewModel.offsetMin,
            valueFormatter = viewModel::formatSliderValue,
            onValueChange = { value -> viewModel.onOffsetChange(value.toInt()) }
        )
    }
}