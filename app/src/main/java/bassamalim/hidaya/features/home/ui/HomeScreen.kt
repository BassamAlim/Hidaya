package bassamalim.hidaya.features.home.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Icon
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.TimeOfDay
import bassamalim.hidaya.core.ui.components.AnalogClock
import bassamalim.hidaya.core.ui.components.MyClickableText
import bassamalim.hidaya.core.ui.components.MyColumn
import bassamalim.hidaya.core.ui.components.MyHorizontalButton
import bassamalim.hidaya.core.ui.components.MyParentColumn
import bassamalim.hidaya.core.ui.components.MyRow
import bassamalim.hidaya.core.ui.components.MySurface
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.ui.theme.Positive

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) return

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose { viewModel.onStop() }
    }

    MyParentColumn {
        PrayerCard(
            previousPrayerName = state.previousPrayerName,
            previousPrayerTimeText = state.previousPrayerTimeText,
            passed = state.passed,
            nextPrayerName = state.nextPrayerName,
            nextPrayerTimeText = state.nextPrayerTimeText,
            remaining = state.remaining,
            previousPrayerTime = state.previousPrayerTime,
            nextPrayerTime = state.nextPrayerTime,
            numeralsLanguage = state.numeralsLanguage
        )

        TodayWerdCard(
            werdPage = state.werdPage,
            isWerdDone = state.isWerdDone,
            onGoToWerdClick = viewModel::onGotoTodayWerdClick
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
    numeralsLanguage: Language
) {
    MySurface(
        Modifier.padding(top = 3.dp)
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnalogClock(
                previousPrayerTime = previousPrayerTime,
                nextPrayerTime = nextPrayerTime,
                numeralsLanguage = numeralsLanguage
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                        text = String.format(
                            stringResource(R.string.passed),
                            passed
                        ),
                        modifier = Modifier.padding(top = 3.dp, bottom = 15.dp),
                        fontSize = 23.sp
                    )
                }

                VerticalDivider(
                    Modifier.padding(vertical = 20.dp),
                    color = AppTheme.colors.shadow
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

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
                        text = String.format(
                            stringResource(R.string.remaining),
                            remaining
                        ),
                        modifier = Modifier.padding(top = 3.dp, bottom = 15.dp),
                        fontSize = 23.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayWerdCard(
    werdPage: String,
    isWerdDone: Boolean,
    onGoToWerdClick: () -> Unit
) {
    MySurface(
        Modifier.padding(bottom = 3.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MyText(
                    stringResource(R.string.today_werd),
                    fontSize = 22.sp
                )

                MyText(
                    "${stringResource(R.string.page)} $werdPage",
                    fontSize = 22.sp
                )
            }

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MyClickableText(
                    text = stringResource(R.string.go_to_page),
                    textColor = AppTheme.colors.accent,
                    modifier = Modifier.padding(top = 10.dp, bottom = 5.dp),
                    onClick = onGoToWerdClick
                )

                AnimatedVisibility(
                    visible = isWerdDone,
                    enter = scaleIn()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = stringResource(R.string.already_read_description),
                        tint = Positive,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
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
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MyText(
                    stringResource(R.string.recitations_time_record_title),
                    Modifier.widthIn(1.dp, 200.dp)
                )

                MyText(
                    recitationsRecord,
                    fontSize = 30.sp
                )
            }

            MyRow(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MyText(
                    stringResource(R.string.quran_pages_record_title),
                    Modifier.widthIn(1.dp, 280.dp),
                    textAlign = TextAlign.Start,
                )

                MyText(
                    quranPagesRecord,
                    fontSize = 30.sp
                )
            }

            MyHorizontalButton(
                text = stringResource(R.string.leaderboard),
                textColor = AppTheme.colors.accent,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_leaderboard),
                        contentDescription = stringResource(R.string.leaderboard),
                        tint =
                            if (isLeaderboardEnabled) AppTheme.colors.accent
                            else Color.Gray
                    )
                },
                middlePadding = PaddingValues(vertical = 6.dp, horizontal = 8.dp),
                elevation = 0,
                enabled = isLeaderboardEnabled,
                onClick = onLeaderboardClick
            )
        }
    }
}