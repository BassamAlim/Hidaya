package bassamalim.hidaya.features.prayers.settings.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.ui.components.DialogDismissButton
import bassamalim.hidaya.core.ui.components.DialogSubmitButton
import bassamalim.hidaya.core.ui.components.MyClickableSurface
import bassamalim.hidaya.core.ui.components.MyText

@Composable
fun PrayerSettingsDialog(viewModel: PrayerSettingsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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
                text = String.format(stringResource(R.string.settings_of), state.prayerName),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            DialogContent(viewModel, state)
        }
    )
}

@Composable
private fun DialogContent(viewModel: PrayerSettingsViewModel, state: PrayerSettingsUiState) {
    NotificationTypesRadioGroup(
        prayer = state.prayer,
        selection = state.notificationType,
        onSelect = viewModel::onNotificationTypeChange
    )
}

@Composable
private fun NotificationTypesRadioGroup(
    prayer: Prayer,
    selection: NotificationType,
    onSelect: (NotificationType) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Athan
        if (prayer != Prayer.SUNRISE) {
            NotificationTypeOption(
                name = stringResource(R.string.athan_speaker),
                icon = painterResource(R.drawable.ic_speaker),
                isSelected = selection == NotificationType.ATHAN,
                onSelection = { onSelect(NotificationType.ATHAN) }
            )
        }

        // Notification
        NotificationTypeOption(
            name = stringResource(R.string.enable_notification),
            icon = painterResource(R.drawable.ic_sound),
            isSelected = selection == NotificationType.NOTIFICATION,
            onSelection = { onSelect(NotificationType.NOTIFICATION) }
        )

        // Silent Notification
        NotificationTypeOption(
            name = stringResource(R.string.silent_notification),
            icon = painterResource(R.drawable.ic_silent),
            isSelected = selection == NotificationType.SILENT,
            onSelection = { onSelect(NotificationType.SILENT) }
        )

        // Off
        NotificationTypeOption(
            name = stringResource(R.string.disable_notification),
            icon = painterResource(R.drawable.ic_block),
            isSelected = selection == NotificationType.OFF,
            onSelection = { onSelect(NotificationType.OFF) }
        )
    }
}

@Composable
private fun NotificationTypeOption(
    name: String,
    icon: Painter,
    isSelected: Boolean,
    onSelection: () -> Unit
) {
    Box(
        Modifier.padding(vertical = 6.dp)
    ) {
        MyClickableSurface(
            padding = PaddingValues(vertical = 0.dp),
            modifier =
                if (isSelected)
                    Modifier.border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(10.dp)
                    )
                else Modifier,
            onClick = onSelection
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = icon,
                    contentDescription = name,
                    tint =
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                )

                MyText(
                    text = name,
                    modifier = Modifier.padding(start = 20.dp),
                    textColor =
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}