package bassamalim.hidaya.features.home

import android.view.LayoutInflater
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import bassamalim.hidaya.R
import bassamalim.hidaya.core.other.AnalogClock
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
fun HomeUI(
    vm: HomeVM,
    nc: NavController
) {
    val st by vm.uiState.collectAsStateWithLifecycle()

    DisposableEffect(key1 = vm) {
        vm.onStart()
        onDispose { vm.onStop() }
    }

    MyParentColumn {
        UpcomingPrayerCard(vm, st)

        RecordsCard(vm, st, nc)

        TodayWerdCard(vm, st, nc)
    }
}

@Composable
fun UpcomingPrayerCard(
    vm: HomeVM,
    st: HomeState
) {
    MySurface(
        Modifier.padding(top = 3.dp)
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AndroidView(
                factory = { context ->
                    val view = LayoutInflater.from(context).inflate(
                        R.layout.clock_view, null, false
                    ) as AnalogClock
                    // do whatever you want...
                    view // return the view
                },
                update = { view ->
                    // Update the view
                    view.update(vm.pastTime, vm.upcomingTime, vm.remaining)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            )

            MyText(
                st.upcomingPrayerName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(3.dp)
            )

            MyText(
                text = st.upcomingPrayerTime,
                fontSize = 24.sp,
                modifier = Modifier.padding(3.dp)
            )

            MyText(
                text = String.format(
                    stringResource(R.string.remaining),
                    st.remainingTime
                ),
                fontSize = 24.sp,
                modifier = Modifier.padding(top = 3.dp, bottom = 15.dp)
            )
        }
    }
}

@Composable
fun RecordsCard(
    vm: HomeVM,
    st: HomeState,
    nc: NavController
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
                    stringResource(R.string.telawat_time_record_title),
                    Modifier.widthIn(1.dp, 200.dp)
                )

                MyText(
                    st.telawatRecord,
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
                    textAlign = TextAlign.Start,
                    modifier = Modifier.widthIn(1.dp, 280.dp)
                )

                MyText(
                    st.quranPagesRecord,
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
                            if (st.leaderboardEnabled) AppTheme.colors.accent
                            else Color.Gray
                    )
                },
                middlePadding = PaddingValues(vertical = 6.dp, horizontal = 8.dp),
                elevation = 0,
                enabled = st.leaderboardEnabled
            ) {
                vm.gotoLeaderboard(nc)
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TodayWerdCard(
    vm: HomeVM,
    st: HomeState,
    nc: NavController
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
                    "${stringResource(R.string.page)} ${st.todayWerdPage}",
                    fontSize = 22.sp
                )
            }

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MyClickableText(
                    stringResource(R.string.go_to_page),
                    textColor = AppTheme.colors.accent,
                    modifier = Modifier.padding(top = 10.dp, bottom = 5.dp)
                ) {
                    vm.onGotoTodayWerdClick(nc)
                }

                AnimatedVisibility(
                    visible = st.isWerdDone,
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