package bassamalim.hidaya.features.prayers.settings.ui

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
import bassamalim.hidaya.core.ui.components.MyClickableSurface
import bassamalim.hidaya.core.ui.components.MyDialog
import bassamalim.hidaya.core.ui.components.MyHorizontalButton
import bassamalim.hidaya.core.ui.components.MyRow
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.theme.AppTheme

@Composable
fun PrayerSettingsDialog(
    viewModel: PrayerSettingViewModel
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
                String.format(stringResource(R.string.settings_of), state.prayerName),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 5.dp, bottom = 20.dp)
            )

            NotificationTypesRadioGroup(
                prayer = state.prayer,
                selection = state.notificationType,
                onSelect = viewModel::onNotificationTypeChange
            )

            MyRow(
                Modifier.padding(top = 20.dp)
            ) {
                SaveBtn(onSave = viewModel::onSave)

                CancelBtn(onDismiss = viewModel::onDismiss)
            }
        }
    }
}

@Composable
private fun NotificationTypesRadioGroup(
    prayer: Prayer,
    selection: NotificationType,
    onSelect: (NotificationType) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
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
                        color = AppTheme.colors.accent,
                        shape = RoundedCornerShape(10.dp)
                    )
                else Modifier,
            onClick = onSelection
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = icon,
                    contentDescription = name
                )

                MyText(
                    name,
                    Modifier.padding(start = 20.dp),
                    textColor =
                        if (isSelected) AppTheme.colors.accent
                        else AppTheme.colors.text
                )
            }
        }
    }
}

@Composable
private fun RowScope.SaveBtn(onSave: () -> Unit) {
    MyHorizontalButton(
        text = stringResource(R.string.save),
        fontSize = 24.sp,
        modifier = Modifier.weight(1f),
        onClick = onSave
    )
}

@Composable
private fun RowScope.CancelBtn(onDismiss: () -> Unit) {
    MyHorizontalButton(
        text = stringResource(R.string.cancel),
        fontSize = 24.sp,
        modifier = Modifier.weight(1f),
        onClick = onDismiss
    )
}