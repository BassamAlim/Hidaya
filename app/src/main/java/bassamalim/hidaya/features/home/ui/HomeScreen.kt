package bassamalim.hidaya.features.home.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.TimeOfDay
import bassamalim.hidaya.core.ui.components.AnalogClock
import bassamalim.hidaya.core.ui.components.MyColumn
import bassamalim.hidaya.core.ui.components.MyRow
import bassamalim.hidaya.core.ui.components.MySurface
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.ParentColumn
import bassamalim.hidaya.core.ui.theme.Positive

@Composable
fun HomeScreen(viewModel: HomeViewModel, bottomNavController: NavHostController) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose { viewModel.onStop() }
    }

    if (state.isLoading) return

    ParentColumn {
        PrayerCard(
            previousPrayerName = state.previousPrayerName,
            previousPrayerTimeText = state.previousPrayerTimeText,
            passed = state.passed,
            nextPrayerName = state.nextPrayerName,
            nextPrayerTimeText = state.nextPrayerTimeText,
            remaining = state.remaining,
            previousPrayerTime = state.previousPrayerTime,
            nextPrayerTime = state.nextPrayerTime,
            numeralsLanguage = state.numeralsLanguage,
            onPrayerCardClick = { viewModel.onPrayerCardClick(bottomNavController) }
        )

        TodayWerdCard(
            werdPage = state.werdPage,
            isWerdDone = state.isWerdDone,
            onTodayWerdClick = viewModel::onTodayWerdCardClick
        )

        RecordsCard(
            recitationsRecord = state.recitationsRecord,
            quranPagesRecord = state.quranRecord,
            isLeaderboardEnabled = state.isLeaderboardEnabled,
            onLeaderboardClick = viewModel::onLeaderboardClick
        )
    }
}

@Composable
private fun PrayerCard(
    previousPrayerName: String,
    previousPrayerTimeText: String,
    passed: String,
    nextPrayerName: String,
    nextPrayerTimeText: String,
    remaining: String,
    previousPrayerTime: TimeOfDay?,
    nextPrayerTime: TimeOfDay?,
    numeralsLanguage: Language,
    onPrayerCardClick: () -> Unit
) {
    MySurface {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onPrayerCardClick),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnalogClock(
                previousPrayerTime = previousPrayerTime,
                nextPrayerTime = nextPrayerTime,
                numeralsLanguage = numeralsLanguage
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    MyText(
                        text = stringResource(R.string.previous_prayer),
                        modifier = Modifier.padding(3.dp),
                        fontSize = 20.sp
                    )

                    MyText(
                        text = previousPrayerName,
                        modifier = Modifier.padding(3.dp),
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )

                    MyText(
                        text = previousPrayerTimeText,
                        modifier = Modifier.padding(3.dp),
                        fontSize = 23.sp
                    )

                    MyText(
                        text = String.format(stringResource(R.string.passed), passed),
                        modifier = Modifier.padding(top = 3.dp, bottom = 15.dp),
                        fontSize = 23.sp
                    )
                }

                VerticalDivider(
                    modifier = Modifier.padding(vertical = 20.dp),
                    color = DividerDefaults.color.copy(alpha = 0.2f)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    MyText(
                        text = stringResource(R.string.next_prayer),
                        modifier = Modifier.padding(3.dp),
                        fontSize = 20.sp
                    )

                    MyText(
                        text = nextPrayerName,
                        modifier = Modifier.padding(3.dp),
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )

                    MyText(
                        text = nextPrayerTimeText,
                        modifier = Modifier.padding(3.dp),
                        fontSize = 23.sp
                    )

                    MyText(
                        text = String.format(stringResource(R.string.remaining), remaining),
                        modifier = Modifier.padding(top = 3.dp, bottom = 15.dp),
                        fontSize = 23.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayWerdCard(werdPage: String, isWerdDone: Boolean, onTodayWerdClick: () -> Unit) {
    MySurface {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onTodayWerdClick)
                .padding(vertical = 16.dp, horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MyText(
                text = stringResource(R.string.today_werd),
                modifier = Modifier.padding(end = 16.dp),
                fontSize = 20.sp
            )

            MyText(
                text = "${stringResource(R.string.page)} $werdPage",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )

            if (isWerdDone) {
                AnimatedVisibility(visible = isWerdDone, enter = scaleIn()) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.already_read_description),
                        tint = Positive,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            else Box(Modifier.size(40.dp))
        }
    }
}

@Composable
private fun RecordsCard(
    recitationsRecord: String,
    quranPagesRecord: String,
    isLeaderboardEnabled: Boolean,
    onLeaderboardClick: () -> Unit
) {
    MySurface {
        MyColumn {
            MyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MyText(
                    text = stringResource(R.string.recitations_time_record_title),
                    modifier = Modifier.weight(1f),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Start
                )

                MyText(
                    text = recitationsRecord,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            MyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MyText(
                    text = stringResource(R.string.quran_pages_record_title),
                    modifier = Modifier.weight(1f),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Start
                )

                MyText(
                    text = quranPagesRecord,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            TextButton(
                onClick = onLeaderboardClick,
                enabled = isLeaderboardEnabled,
                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Leaderboard,
                    contentDescription = stringResource(R.string.leaderboard)
                )

                Spacer(Modifier.width(6.dp))

                MyText(
                    text = stringResource(R.string.leaderboard),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}