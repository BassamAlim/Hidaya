package bassamalim.hidaya.features.prayers.timeCalculationSettings.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.HighLatitudesAdjustmentMethod
import bassamalim.hidaya.core.enums.PrayerTimeCalculationMethod
import bassamalim.hidaya.core.enums.PrayerTimeJuristicMethod
import bassamalim.hidaya.core.ui.components.DialogDismissButton
import bassamalim.hidaya.core.ui.components.DialogSubmitButton
import bassamalim.hidaya.core.ui.components.DialogTitle
import bassamalim.hidaya.core.ui.components.MyHorizontalDivider
import bassamalim.hidaya.features.settings.ui.MenuSetting
import bassamalim.hidaya.features.settings.ui.SwitchSetting

@Composable
fun PrayerTimeCalculationSettingsDialog(viewModel: PrayerSettingsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current!!

    AlertDialog(
        onDismissRequest = viewModel::onDismiss,
        dismissButton = {
            DialogDismissButton { viewModel.onDismiss() }
        },
        confirmButton = {
            DialogSubmitButton(
                text = stringResource(R.string.save),
                onSubmit = { viewModel.onSave(activity) }
            )
        },
        title = {
            DialogTitle(stringResource(R.string.prayer_time_settings))
        },
        text = {
            DialogContent(viewModel = viewModel, state = state)
        }
    )
}

@Composable
private fun DialogContent(
    viewModel: PrayerSettingsViewModel,
    state: PrayerTimeCalculationSettingsUiState
) {
    Column {
        // Continuous Notification
        SwitchSetting(
            value = state.continuousPrayersNotificationEnabled,
            title = stringResource(R.string.continuous_prayer_notification),
            summary = stringResource(R.string.continuous_prayers_notification_summary),
            onSwitch = { enabled ->
                viewModel.onContinuousNotificationsSwitch(isEnabled = enabled)
            }
        )

        MyHorizontalDivider(Modifier.padding(horizontal = 16.dp))

        // Calculation method
        MenuSetting(
            selection = state.calculationMethod,
            items = PrayerTimeCalculationMethod.entries.toTypedArray(),
            entries = stringArrayResource(R.array.prayer_times_calc_method_entries),
            title = stringResource(R.string.calculation_method_title),
            onSelection = viewModel::onPrayerTimesCalculationMethodChange
        )

        MyHorizontalDivider(Modifier.padding(horizontal = 16.dp))

        // Juristic method
        MenuSetting(
            selection = state.juristicMethod,
            items = PrayerTimeJuristicMethod.entries.toTypedArray(),
            entries = stringArrayResource(R.array.juristic_method_entries),
            title = stringResource(R.string.juristic_method_title),
            onSelection = viewModel::onPrayerTimesJuristicMethodChange
        )

        MyHorizontalDivider(Modifier.padding(horizontal = 16.dp))

        // High latitude adjustment
        MenuSetting(
            selection = state.highLatitudesAdjustment,
            items = HighLatitudesAdjustmentMethod.entries.toTypedArray(),
            entries = stringArrayResource(R.array.high_lat_adjustment_entries),
            title = stringResource(R.string.high_lat_adjustment_title),
            onSelection = viewModel::onPrayerTimesHighLatitudesAdjustmentChange
        )
    }
}