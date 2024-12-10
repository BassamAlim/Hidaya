package bassamalim.hidaya.features.prayers.settings.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.NotificationsPaused
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.ui.components.DialogDismissButton
import bassamalim.hidaya.core.ui.components.DialogSubmitButton
import bassamalim.hidaya.core.ui.components.DialogTitle
import bassamalim.hidaya.core.ui.components.MyText

@Composable
fun PrayerSettingsDialog(viewModel: PrayerSettingsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = viewModel::onDismiss,
        dismissButton = { DialogDismissButton { viewModel.onDismiss() } },
        confirmButton = {
            DialogSubmitButton(text = stringResource(R.string.save), onSubmit = viewModel::onSave)
        },
        title = {
            DialogTitle(String.format(stringResource(R.string.notification_type), state.prayerName))
        },
        text = { DialogContent(viewModel, state) }
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Athan
        if (prayer != Prayer.SUNRISE) {
            NotificationTypeOption(
                name = stringResource(R.string.athan_speaker),
                icon = Icons.Default.Campaign,
                isSelected = selection == NotificationType.ATHAN,
                onSelection = { onSelect(NotificationType.ATHAN) }
            )
        }

        // Notification
        NotificationTypeOption(
            name = stringResource(R.string.enable_notification),
            icon = Icons.Default.Notifications,
            isSelected = selection == NotificationType.NOTIFICATION,
            onSelection = { onSelect(NotificationType.NOTIFICATION) }
        )

        // Silent Notification
        NotificationTypeOption(
            name = stringResource(R.string.silent_notification),
            icon = Icons.Default.NotificationsPaused,
            isSelected = selection == NotificationType.SILENT,
            onSelection = { onSelect(NotificationType.SILENT) }
        )

        // Off
        NotificationTypeOption(
            name = stringResource(R.string.disable_notification),
            icon = Icons.Default.NotificationsOff,
            isSelected = selection == NotificationType.OFF,
            onSelection = { onSelect(NotificationType.OFF) }
        )
    }
}

@Composable
private fun NotificationTypeOption(
    name: String,
    icon: ImageVector,
    isSelected: Boolean,
    onSelection: () -> Unit
) {
    Button(
        onClick = onSelection,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        border = BorderStroke(
            width = 2.dp,
            color =
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
        ),
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
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