package bassamalim.hidaya.features.prayers.board.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.NotificationsPaused
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.ui.components.LoadingScreen
import bassamalim.hidaya.core.ui.components.MyIconButton
import bassamalim.hidaya.core.ui.components.MyRow
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.MyTextButton
import bassamalim.hidaya.core.ui.components.ParentColumn
import bassamalim.hidaya.core.ui.components.TutorialDialog
import bassamalim.hidaya.core.ui.theme.nsp
import java.util.SortedMap

@Composable
fun PrayersBoardScreen(viewModel: PrayersBoardViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) return LoadingScreen()

    ParentColumn(Modifier.padding(vertical = 16.dp)) {
        LocationCard(
            isLocationAvailable = state.isLocationAvailable,
            locationName = state.locationName,
            onLocatorClick = viewModel::onLocatorClick
        )

        PrayersSpace(
            prayersData = state.prayersData,
            isLocationAvailable = state.isLocationAvailable,
            onPrayerCardClick = viewModel::onPrayerCardClick,
            onReminderCardClick = viewModel::onExtraReminderCardClick
        )

        DayCard(
            dateText = state.dateText,
            isNoDateOffset = state.isNoDateOffset,
            onDateClick = viewModel::onDateClick,
            onPreviousDayClick = viewModel::onPreviousDayClick,
            onNextDayClick = viewModel::onNextDayClick
        )
    }

    TutorialDialog(
        shown = state.isTutorialDialogShown,
        text = stringResource(R.string.prayers_tips),
        onDismissRequest = viewModel::onTutorialDialogDismiss
    )
}

@Composable
private fun LocationCard(
    isLocationAvailable: Boolean,
    locationName: String,
    onLocatorClick: () -> Unit
) {
    OutlinedCard(
        Modifier
            .fillMaxWidth(0.8f)
            .clickable(onClick = onLocatorClick)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = stringResource(R.string.locate),
                modifier = Modifier.size(26.dp)
            )

            MyText(
                text =
                    if (isLocationAvailable) locationName
                    else stringResource(R.string.click_to_locate),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun ColumnScope.PrayersSpace(
    prayersData: SortedMap<Prayer, PrayerCardData>,
    isLocationAvailable: Boolean,
    onPrayerCardClick: (Prayer) -> Unit,
    onReminderCardClick: (Prayer) -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1F)
            .padding(vertical = 16.dp, horizontal = 6.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceAround
    ) {
        prayersData.forEach { (prayer, data) ->
            PrayerSpace(
                prayer = prayer,
                data = data,
                isLocationAvailable = isLocationAvailable,
                onPrayerCardClick = onPrayerCardClick,
                onReminderCardClick = onReminderCardClick
            )

            if (prayer != prayersData.lastKey())
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = DividerDefaults.color.copy(alpha = 0.2f)
                )
        }
    }
}

@Composable
private fun PrayerSpace(
    prayer: Prayer,
    data: PrayerCardData,
    isLocationAvailable: Boolean,
    onPrayerCardClick: (Prayer) -> Unit,
    onReminderCardClick: (Prayer) -> Unit
) {
    MyRow {
        MyText(
            text = data.text,
            modifier = Modifier
                .weight(1f)
                .padding(start = 6.dp),
            fontSize = 29.nsp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Start
        )

        if (isLocationAvailable) {
            NotificationType(
                prayer = prayer,
                data = data,
                onPrayerCardClick = onPrayerCardClick
            )

            ExtraReminderButton(
                prayer = prayer,
                isReminderOffsetSpecified = data.isExtraReminderOffsetSpecified,
                reminderOffsetText = data.extraReminderOffset,
                onReminderCardClick = onReminderCardClick
            )
        }
    }
}

@Composable
private fun RowScope.NotificationType(
    prayer: Prayer,
    data: PrayerCardData,
    onPrayerCardClick: (Prayer) -> Unit
) {
    Box(Modifier.padding(horizontal = 6.dp)) {
        FilledTonalIconButton(
            onClick = { onPrayerCardClick(prayer) },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = when (data.notificationType) {
                    NotificationType.ATHAN -> Icons.Filled.Campaign
                    NotificationType.NOTIFICATION -> Icons.Filled.Notifications
                    NotificationType.SILENT -> Icons.Filled.NotificationsPaused
                    NotificationType.OFF -> Icons.Filled.NotificationsOff
                },
                contentDescription = stringResource(R.string.notification_type),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun ExtraReminderButton(
    prayer: Prayer,
    isReminderOffsetSpecified: Boolean,
    reminderOffsetText: String,
    onReminderCardClick: (Prayer) -> Unit
) {
    Box(Modifier.padding(horizontal = 6.dp)) {
        FilledTonalIconButton(
            onClick = { onReminderCardClick(prayer) },
            modifier = Modifier.size(48.dp)
        ) {
            MyRow(horizontalArrangement = Arrangement.Center) {
                if (isReminderOffsetSpecified) {
                    MyText(
                        text = reminderOffsetText,
                        modifier = Modifier.padding(end = 3.dp),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Icon(
                    imageVector = Icons.Default.AddAlert,
                    contentDescription = stringResource(R.string.extra_notifications),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun DayCard(
    dateText: String,
    isNoDateOffset: Boolean,
    onDateClick: () -> Unit,
    onPreviousDayClick: () -> Unit,
    onNextDayClick: () -> Unit,
) {
    OutlinedCard(Modifier.fillMaxWidth(0.8f)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MyIconButton(
                imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                description = stringResource(R.string.previous_day_button_description),
                onClick = onPreviousDayClick
            )

            MyTextButton(
                text =
                    if (isNoDateOffset) stringResource(R.string.day)
                    else dateText,
                onClick = onDateClick,
                fontSize = 20.sp,
                textColor = MaterialTheme.colorScheme.onSurface
            )

            MyIconButton(
                imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                description = stringResource(R.string.next_day_button_description),
                onClick = onNextDayClick
            )
        }
    }
}