package bassamalim.hidaya.features.prayers.board.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.ui.components.LoadingScreen
import bassamalim.hidaya.core.ui.components.MyClickableSurface
import bassamalim.hidaya.core.ui.components.MyClickableText
import bassamalim.hidaya.core.ui.components.MyIconButton
import bassamalim.hidaya.core.ui.components.MyRow
import bassamalim.hidaya.core.ui.components.MySurface
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.ParentColumn
import bassamalim.hidaya.core.ui.components.TutorialDialog
import bassamalim.hidaya.core.ui.theme.nsp
import java.util.SortedMap

@Composable
fun PrayersBoardScreen(viewModel: PrayersBoardViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) return LoadingScreen()

    ParentColumn {
        LocationCard(
            isLocationAvailable = state.isLocationAvailable,
            locationName = state.locationName,
            onLocatorClick = viewModel::onLocatorClick
        )

        PrayersSpace(
            prayersData = state.prayersData,
            isLocationAvailable = state.isLocationAvailable,
            onPrayerCardClick = { prayer, isLocationAvailable ->
                viewModel.onPrayerCardClick(prayer, isLocationAvailable)
            },
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
        onDismiss = viewModel::onTutorialDialogDismiss
    )
}

@Composable
private fun LocationCard(
    isLocationAvailable: Boolean,
    locationName: String,
    onLocatorClick: () -> Unit
) {
    MySurface(
        Modifier.padding(top = 5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MyText(
                text =
                    if (isLocationAvailable) locationName
                    else stringResource(R.string.click_to_locate),
                modifier = Modifier
                    .widthIn(1.dp, 300.dp)
                    .padding(start = 15.dp)
            )

            MyIconButton(
                iconId = R.drawable.ic_location,
                description = stringResource(R.string.locate),
                modifier = Modifier.padding(end = 8.dp),
                tint = MaterialTheme.colorScheme.onSurface,
                iconSize = 32.dp,
                onClick = onLocatorClick
            )
        }
    }
}

@Composable
private fun ColumnScope.PrayersSpace(
    prayersData: SortedMap<Prayer, PrayerCardData>,
    isLocationAvailable: Boolean,
    onPrayerCardClick: (Prayer, Boolean) -> Unit,
    onReminderCardClick: (Prayer) -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1F)
            .padding(vertical = 10.dp, horizontal = 6.dp)
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
        }
    }
}

@Composable
private fun PrayerSpace(
    prayer: Prayer,
    data: PrayerCardData,
    isLocationAvailable: Boolean,
    onPrayerCardClick: (Prayer, Boolean) -> Unit,
    onReminderCardClick: (Prayer) -> Unit
) {
    MyRow {
        PrayerCard(
            prayer = prayer,
            data = data,
            isLocationAvailable = isLocationAvailable,
            onPrayerCardClick = onPrayerCardClick
        )

        ExtraReminderCard(
            prayer = prayer,
            isReminderOffsetSpecified = data.isExtraReminderOffsetSpecified,
            reminderOffsetText = data.extraReminderOffset,
            isLocationAvailable = isLocationAvailable,
            onReminderCardClick = onReminderCardClick
        )
    }
}

@Composable
private fun RowScope.PrayerCard(
    prayer: Prayer,
    data: PrayerCardData,
    isLocationAvailable: Boolean,
    onPrayerCardClick: (Prayer, Boolean) -> Unit
) {
    MyClickableSurface(
        modifier = Modifier.weight(1f),
        cornerRadius = 15.dp,
        padding = PaddingValues(vertical = 3.dp, horizontal = 4.dp),
        onClick = { onPrayerCardClick(prayer, isLocationAvailable) }
    ) {
        MyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MyText(
                text = data.text,
                fontSize = 29.nsp,
                fontWeight = FontWeight.Medium
            )

            if (isLocationAvailable) {
                MyRow {
                    // Notification type
                    Icon(
                        painter = painterResource(
                            when (data.notificationType) {
                                NotificationType.ATHAN -> R.drawable.ic_speaker
                                NotificationType.NOTIFICATION -> R.drawable.ic_sound
                                NotificationType.SILENT -> R.drawable.ic_silent
                                NotificationType.OFF -> R.drawable.ic_block
                            }
                        ),
                        contentDescription = stringResource(R.string.notification_image_description),
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ExtraReminderCard(
    prayer: Prayer,
    isReminderOffsetSpecified: Boolean,
    reminderOffsetText: String,
    isLocationAvailable: Boolean,
    onReminderCardClick: (Prayer) -> Unit
) {
    MyClickableSurface(
        modifier = Modifier.fillMaxWidth(0.19f),
        cornerRadius = 15.dp,
        padding = PaddingValues(horizontal = 3.dp),
        onClick = { onReminderCardClick(prayer) }
    ) {
        if (isLocationAvailable) {
            MyRow(
                modifier = Modifier.padding(vertical = 19.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                if (isReminderOffsetSpecified) {
                    MyText(
                        text = reminderOffsetText,
                        modifier = Modifier.padding(end = 3.dp),
                        textColor = MaterialTheme.colorScheme.primary
                    )
                }

                Icon(
                    painter = painterResource(R.drawable.ic_add_reminder),
                    contentDescription = stringResource(R.string.extra_notifications),
                    modifier = Modifier.size(32.dp),
                    tint =
                        if (isReminderOffsetSpecified) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
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
    MySurface(
        Modifier.padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MyIconButton(
                iconId = R.drawable.ic_left_arrow,
                description = stringResource(R.string.previous_day_button_description),
                modifier = Modifier.padding(2.dp),
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = onPreviousDayClick
            )

            MyClickableText(
                text =
                    if (isNoDateOffset) stringResource(R.string.day)
                    else dateText,
                fontSize = 24.sp,
                textColor = MaterialTheme.colorScheme.onSurface,
                innerPadding = PaddingValues(vertical = 3.dp, horizontal = 15.dp),
                onClick = onDateClick
            )

            MyIconButton(
                iconId = R.drawable.ic_right_arrow,
                description = stringResource(R.string.next_day_button_description),
                modifier = Modifier.padding(2.dp),
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = onNextDayClick
            )
        }
    }
}