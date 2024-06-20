package bassamalim.hidaya.features.prayers

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
import androidx.compose.runtime.DisposableEffect
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
fun PrayersUI(
    vm: PrayersViewModel
) {
    val st by vm.uiState.collectAsStateWithLifecycle()

    DisposableEffect(key1 = vm) {
        vm.onStart()
        onDispose {}
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LocationCard(vm, st)

        PrayersSpace(vm, st)

        DayCard(vm, st)
    }

    TutorialDialog(
        shown = st.tutorialDialogShown,
        textResId = R.string.prayers_tips,
        onDismiss = { vm.onTutorialDialogDismiss(it) }
    )
}

@Composable
private fun LocationCard(
    vm: PrayersViewModel,
    st: PrayersState
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
                text = st.locationName,
                modifier = Modifier
                    .widthIn(1.dp, 300.dp)
                    .padding(start = 15.dp)
            )

            MyIconButton(
                iconId = R.drawable.ic_location,
                description = stringResource(R.string.locate),
                tint = AppTheme.colors.text,
                modifier = Modifier.padding(end = 8.dp),
                size = 32.dp
            ) {
                vm.onLocatorClk()
            }
        }
    }
}

@Composable
private fun ColumnScope.PrayersSpace(
    vm: PrayersViewModel,
    st: PrayersState
) {
    Column(
        Modifier
            .weight(1F)
            .padding(vertical = 10.dp, horizontal = 6.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceAround
    ) {
        st.prayersData.forEachIndexed { i, data ->
            PrayerSpace(vm, st, i, data)
        }
    }
}

@Composable
private fun PrayerSpace(
    vm: PrayersViewModel,
    st: PrayersState,
    idx: Int,
    data: PrayerData
) {
    MyRow {
        PrayerCard(
            vm = vm,
            idx = idx,
            data = data
        )

        ReminderCard(
            vm = vm,
            idx = idx,
            reminderOffset = data.settings.reminderOffset
        )
    }
}

@Composable
private fun RowScope.PrayerCard(
    vm: PrayersViewModel,
    idx: Int,
    data: PrayerData
) {
    MyClickableSurface(
        modifier = Modifier.weight(1f),
        cornerRadius = 15.dp,
        padding = PaddingValues(vertical = 3.dp, horizontal = 4.dp),
        onClick = { vm.onPrayerCardClk(PID.entries[idx]) }
    ) {
        MyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MyText(
                data.getText(),
                fontSize = 29.nsp,
                fontWeight = FontWeight.Medium
            )

            if (vm.location != null) {
                MyRow {
                    // Time offset
                    if (data.settings.timeOffset != 0) {
                        MyText(
                            vm.formatOffset(data.settings.timeOffset),
                            textColor = AppTheme.colors.accent,
                            modifier = Modifier.padding(end = 3.dp)
                        )
                    }

                    // Notification type
                    Icon(
                        painter = painterResource(
                            vm.getNotificationTypeIconID(data.settings.notificationType)
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
    vm: PrayersViewModel,
    idx: Int,
    reminderOffset: Int
) {
    MyClickableSurface(
        modifier = Modifier.fillMaxWidth(0.19f),
        cornerRadius = 15.dp,
        padding = PaddingValues(horizontal = 3.dp),
        onClick = { vm.onReminderCardClk(PID.entries[idx]) }
    ) {
        if (vm.location != null) {
            MyRow(
                Modifier.padding(vertical = 19.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                if (reminderOffset != 0) {
                    MyText(
                        vm.formatOffset(reminderOffset),
                        textColor = AppTheme.colors.accent,
                        modifier = Modifier.padding(end = 3.dp)
                    )
                }

                Icon(
                    painter = painterResource(R.drawable.ic_add_reminder),
                    contentDescription = stringResource(R.string.notification_image_description),
                    tint =
                        if (reminderOffset == 0) AppTheme.colors.text
                        else AppTheme.colors.accent,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun DayCard(
    vm: PrayersViewModel,
    st: PrayersState
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
                onClick = { vm.onPreviousDayClk() }
            )

            MyClickableText(
                text = st.dateText,
                fontSize = 24.sp,
                textColor = AppTheme.colors.text,
                innerPadding = PaddingValues(vertical = 3.dp, horizontal = 15.dp),
                onClick = { vm.onDateClk() }
            )

            MyIconButton(
                iconId = R.drawable.ic_right_arrow,
                description = stringResource(R.string.next_day_button_description),
                modifier = Modifier.padding(2.dp),
                innerPadding = 10.dp,
                tint = AppTheme.colors.text,
                onClick = { vm.onNextDayClk() }
            )
        }
    }
}