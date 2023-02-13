package bassamalim.hidaya.view

import android.view.LayoutInflater
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import bassamalim.hidaya.R
import bassamalim.hidaya.other.AnalogClock
import bassamalim.hidaya.ui.components.MyClickableText
import bassamalim.hidaya.ui.components.MySurface
import bassamalim.hidaya.ui.components.MyText
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.ui.theme.Positive
import bassamalim.hidaya.viewmodel.HomeVM

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeUI(
    nc: NavController = rememberNavController(),
    vm: HomeVM = hiltViewModel()
) {
    val st by vm.uiState.collectAsState()

    DisposableEffect(key1 = vm) {
        vm.onStart()
        onDispose { vm.onStop() }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
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

        MySurface {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
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
        }

        MySurface {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
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
        }

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
}