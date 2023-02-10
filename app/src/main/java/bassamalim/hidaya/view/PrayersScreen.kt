package bassamalim.hidaya.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import bassamalim.hidaya.R
import bassamalim.hidaya.enums.PID
import bassamalim.hidaya.state.PrayersState
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.ui.theme.nsp
import bassamalim.hidaya.viewmodel.PrayersVM

@Composable
fun PrayersUI(
    nc: NavController = rememberNavController(),
    vm: PrayersVM = hiltViewModel()
) {
    val st by vm.uiState.collectAsState()

    DisposableEffect(key1 = vm) {
        vm.onStart()
        onDispose {}
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
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

                MyIconBtn(
                    iconId = R.drawable.ic_location,
                    description = stringResource(R.string.locate),
                    tint = AppTheme.colors.text,
                    modifier = Modifier.padding(end = 8.dp),
                    size = 32.dp
                ) {
                    vm.onLocatorClick(nc)
                }
            }
        }

        Column(
            Modifier
                .weight(1F)
                .padding(vertical = 10.dp, horizontal = 6.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            PrayerCards(
                viewModel = vm,
                state = st
            )
        }

        MySurface(
            Modifier.padding(bottom = 5.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MyIconBtn(
                    iconId = R.drawable.ic_left_arrow,
                    description = stringResource(R.string.previous_day_button_description),
                    tint = AppTheme.colors.text
                ) {
                    vm.onPreviousDayClk()
                }

                MyClickableText(
                    text = st.dateText,
                    fontSize = 24.sp,
                    innerPadding = PaddingValues(vertical = 3.dp, horizontal = 15.dp)
                ) {
                    vm.goToToday()
                }

                MyIconBtn(
                    iconId = R.drawable.ic_right_arrow,
                    description = stringResource(R.string.next_day_button_description),
                    tint = AppTheme.colors.text
                ) {
                    vm.onNextDayClk()
                }
            }
        }
    }

    PrayerDialog(
        shown = st.isSettingsDialogShown,
        pid = st.settingsDialogPID,
        notificationType = st.notificationTypes[st.settingsDialogPID.ordinal],
        timeOffset = st.timeOffsets[st.settingsDialogPID.ordinal],
        onNotificationTypeChange = { vm.onNotificationTypeChange(it) },
        onOffsetChange = { vm.onTimeOffsetChange(it) },
        onDismiss = { vm.onSettingsDialogDismiss() }
    )

    TutorialDialog(
        shown = st.isTutorialDialogShown,
        textResId = R.string.prayers_tips
    ) {
        vm.onTutorialDialogDismiss(it)
    }
}

@Composable
private fun PrayerCards(viewModel: PrayersVM, state: PrayersState) {
    for (i in state.prayerTexts.indices) {
        PrayerCard(
            vm = viewModel,
            state = state,
            number = i,
            text = state.prayerTexts[i]
        )
    }
}

@Composable
private fun PrayerCard(vm: PrayersVM, state: PrayersState, number: Int, text: String) {
    MyClickableSurface(
        onClick = { vm.onPrayerClick(PID.values()[number]) },
        cornerRadius = 15.dp
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Prayer name
            MyText(
                text,
                fontSize = 33.nsp,
                fontWeight = FontWeight.Bold
            )

            if (vm.location != null) {
                Row {
                    // Delay
                    MyText(vm.formatTimeOffset(state.timeOffsets[number]))

                    // Notification type
                    Icon(
                        painter = painterResource(
                            vm.getNotificationTypeIconID(state.notificationTypes[number])
                        ),
                        contentDescription = stringResource(R.string.notification_image_description),
                        tint = AppTheme.colors.accent,
                        modifier = Modifier.size(35.dp)
                    )
                }
            }
        }
    }
}