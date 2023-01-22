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
import bassamalim.hidaya.enum.PID
import bassamalim.hidaya.state.PrayersState
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.ui.theme.nsp
import bassamalim.hidaya.viewmodel.PrayersVM

@Composable
fun PrayersUI(
    navController: NavController = rememberNavController(),
    viewModel: PrayersVM = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
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
                    text = state.locationName,
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
                    viewModel.onLocatorClick(navController)
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
                viewModel = viewModel,
                state = state,
                times = state.prayerTexts
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
                    viewModel.previousDay()
                }

                MyClickableText(
                    text = state.dateText,
                    fontSize = 24.sp,
                    innerPadding = PaddingValues(vertical = 3.dp, horizontal = 15.dp)
                ) {
                    viewModel.goToToday()
                }

                MyIconBtn(
                    iconId = R.drawable.ic_right_arrow,
                    description = stringResource(R.string.next_day_button_description),
                    tint = AppTheme.colors.text
                ) {
                    viewModel.nextDay()
                }
            }
        }
    }

    PrayerDialog(
        shown = state.isSettingsDialogShown,
        pid = state.settingsDialogPID,
        notificationTypes = viewModel.getNotificationTypes(),
        offsets = viewModel.getTimeOffsets(),
        onNotificationTypeChange = { viewModel.onNotificationTypeChange(it) },
        onOffsetChange = { viewModel.onTimeOffsetChange(it) }
    ) {
       viewModel.onSettingsDialogDismiss()
    }

    TutorialDialog(
        textResId = R.string.prayers_tips,
        shown = state.isTutorialDialogShown
    ) {
        viewModel.onTutorialDialogDismiss(it)
    }
}

@Composable
private fun PrayerCards(viewModel: PrayersVM, state: PrayersState, times: List<String>) {
    for (i in times.indices) {
        PrayerCard(
            viewModel = viewModel,
            state = state,
            number = i,
            text = times[i]
        )
    }
}

@Composable
private fun PrayerCard(viewModel: PrayersVM, state: PrayersState, number: Int, text: String) {
    MyClickableSurface(
        onClick = { viewModel.onPrayerClick(PID.values()[number]) },
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

            Row {
                // Delay
                MyText(state.timeOffsetTexts[number])

                // Notification type
                Icon(
                    painter = painterResource(state.notificationTypeIconIDs[number]),
                    contentDescription = stringResource(R.string.notification_image_description),
                    tint = AppTheme.colors.accent,
                    modifier = Modifier.size(35.dp)
                )
            }
        }
    }
}