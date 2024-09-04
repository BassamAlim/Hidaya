package bassamalim.hidaya.features.prayers.prayersBoard.ui

import android.widget.Toast
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
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.ui.components.MyClickableSurface
import bassamalim.hidaya.core.ui.components.MyClickableText
import bassamalim.hidaya.core.ui.components.MyIconButton
import bassamalim.hidaya.core.ui.components.MyRow
import bassamalim.hidaya.core.ui.components.MySurface
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.TutorialDialog
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.ui.theme.nsp

@Composable
fun PrayersBoardScreen(
    viewModel: PrayersBoardViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LocationCard(
            isLocationAvailable = state.isLocationAvailable,
            locationName = state.locationName,
            onLocatorClick = viewModel::onLocatorClick
        )

        PrayersSpace(
            prayersData = state.prayersData,
            isLocationAvailable = state.locationName.isNotEmpty(),
            onPrayerCardClick = viewModel::onPrayerCardClick,
            onReminderCardClick = viewModel::onReminderCardClick
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
        shown = state.tutorialDialogShown,
        textResId = R.string.prayers_tips,
        onDismiss = viewModel::onTutorialDialogDismiss
    )

    if (state.shouldShowLocationFailedToast)
        LocationFailedToast()
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
            Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MyText(
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
                tint = AppTheme.colors.text,
                size = 32.dp,
                onClick = onLocatorClick
            )
        }
    }
}

@Composable
private fun ColumnScope.PrayersSpace(
    prayersData: List<PrayerCardData>,
    isLocationAvailable: Boolean,
    onPrayerCardClick: (PID) -> Unit,
    onReminderCardClick: (PID) -> Unit
) {
    Column(
        Modifier
            .weight(1F)
            .padding(vertical = 10.dp, horizontal = 6.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceAround
    ) {
        prayersData.forEachIndexed { i, data ->
            PrayerSpace(
                idx = i,
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
    idx: Int,
    data: PrayerCardData,
    isLocationAvailable: Boolean,
    onPrayerCardClick: (PID) -> Unit,
    onReminderCardClick: (PID) -> Unit
) {
    MyRow {
        PrayerCard(
            idx = idx,
            data = data,
            isLocationAvailable = isLocationAvailable,
            onPrayerCardClick = onPrayerCardClick
        )

        ReminderCard(
            idx = idx,
            isReminderOffsetSpecified = data.isReminderOffsetSpecified,
            reminderOffsetText = data.reminderOffset,
            isLocationAvailable = isLocationAvailable,
            onReminderCardClick = onReminderCardClick
        )
    }
}

@Composable
private fun RowScope.PrayerCard(
    idx: Int,
    data: PrayerCardData,
    isLocationAvailable: Boolean,
    onPrayerCardClick: (PID) -> Unit
) {
    MyClickableSurface(
        modifier = Modifier.weight(1f),
        cornerRadius = 15.dp,
        padding = PaddingValues(vertical = 3.dp, horizontal = 4.dp),
        onClick = { onPrayerCardClick(PID.entries[idx]) }
    ) {
        MyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MyText(
                data.text,
                fontSize = 29.nsp,
                fontWeight = FontWeight.Medium
            )

            if (isLocationAvailable) {
                MyRow {
                    // Time offset
                    if (data.isTimeOffsetSpecified) {
                        MyText(
                            data.timeOffset,
                            textColor = AppTheme.colors.accent,
                            modifier = Modifier.padding(end = 3.dp)
                        )
                    }

                    // Notification type
                    Icon(
                        painter = painterResource(
                            when (data.notificationType) {
                                NotificationType.ATHAN -> R.drawable.ic_speaker
                                NotificationType.NOTIFICATION -> R.drawable.ic_sound
                                NotificationType.SILENT -> R.drawable.ic_silent
                                NotificationType.NONE -> R.drawable.ic_block
                            }
                        ),
                        contentDescription = stringResource(R.string.notification_image_description),
                        tint = AppTheme.colors.accent,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminderCard(
    idx: Int,
    isReminderOffsetSpecified: Boolean,
    reminderOffsetText: String,
    isLocationAvailable: Boolean,
    onReminderCardClick: (PID) -> Unit
) {
    MyClickableSurface(
        modifier = Modifier.fillMaxWidth(0.19f),
        cornerRadius = 15.dp,
        padding = PaddingValues(horizontal = 3.dp),
        onClick = { onReminderCardClick(PID.entries[idx]) }
    ) {
        if (isLocationAvailable) {
            MyRow(
                Modifier.padding(vertical = 19.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                if (isReminderOffsetSpecified) {
                    MyText(
                        reminderOffsetText,
                        Modifier.padding(end = 3.dp),
                        textColor = AppTheme.colors.accent
                    )
                }

                Icon(
                    painter = painterResource(R.drawable.ic_add_reminder),
                    contentDescription = stringResource(R.string.notification_image_description),
                    modifier = Modifier.size(32.dp),
                    tint =
                        if (isReminderOffsetSpecified) AppTheme.colors.text
                        else AppTheme.colors.accent
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
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MyIconButton(
                iconId = R.drawable.ic_left_arrow,
                description = stringResource(R.string.previous_day_button_description),
                modifier = Modifier.padding(2.dp),
                innerPadding = 10.dp,
                tint = AppTheme.colors.text,
                onClick = onPreviousDayClick
            )

            MyClickableText(
                text =
                    if (isNoDateOffset) stringResource(R.string.day)
                    else dateText,
                fontSize = 24.sp,
                textColor = AppTheme.colors.text,
                innerPadding = PaddingValues(vertical = 3.dp, horizontal = 15.dp),
                onClick = onDateClick
            )

            MyIconButton(
                iconId = R.drawable.ic_right_arrow,
                description = stringResource(R.string.next_day_button_description),
                modifier = Modifier.padding(2.dp),
                innerPadding = 10.dp,
                tint = AppTheme.colors.text,
                onClick = onNextDayClick
            )
        }
    }
}

@Composable
private fun LocationFailedToast() {
    val context = LocalContext.current
    LaunchedEffect(null) {
        Toast.makeText(
            context,
            context.getString(R.string.location_failed),
            Toast.LENGTH_SHORT
        ).show()
    }
}