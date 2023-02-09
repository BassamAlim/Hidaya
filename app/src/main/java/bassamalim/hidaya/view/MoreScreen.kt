package bassamalim.hidaya.view

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import bassamalim.hidaya.R
import bassamalim.hidaya.ui.components.MySquareButton
import bassamalim.hidaya.viewmodel.MoreVM

@Composable
fun MoreUI(
    nc: NavController = rememberNavController(),
    vm: MoreVM = hiltViewModel()
) {
    val st = vm.uiState.collectAsState()
    val ctx = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 5.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MySquareButton(
                textResId = R.string.recitations,
                imageResId = R.drawable.ic_headphone
            ) {
                vm.gotoTelawat(nc)
            }

            MySquareButton(
                textResId = R.string.qibla,
                imageResId = R.drawable.ic_qibla_compass
            ) {
                vm.gotoQibla(nc)
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MySquareButton(
                textResId = R.string.quiz_title,
                imageResId = R.drawable.ic_quiz
            ) {
                vm.gotoQuiz(nc)
            }

            MySquareButton(
                textResId = R.string.hadeeth_books,
                imageResId = R.drawable.ic_books
            ) {
                vm.gotoBooks(nc)
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MySquareButton(
                textResId = R.string.tv_channels,
                imageResId = R.drawable.ic_television
            ) {
                vm.gotoTV(nc)
            }

            MySquareButton(
                textResId = R.string.quran_radio,
                imageResId = R.drawable.ic_radio
            ) {
                vm.gotoRadio(nc)
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MySquareButton(
                textResId = R.string.date_converter,
                imageResId = R.drawable.ic_calendar
            ) {
                vm.gotoDateConverter(nc)
            }

            MySquareButton(R.string.settings, R.drawable.ic_settings) {
                vm.gotoSettings(nc)
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MySquareButton(
                textResId = R.string.contact,
                imageResId = R.drawable.ic_mail
            ) {
                vm.contactMe(ctx)
            }

            MySquareButton(
                textResId = R.string.share_app,
                imageResId = R.drawable.ic_share
            ) {
                vm.shareApp(ctx)
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MySquareButton(
                textResId = R.string.about,
                imageResId = R.drawable.ic_info
            ) {
                vm.gotoAbout(nc)
            }
        }
    }

    if (st.value.shouldShowUnsupported) {
        LaunchedEffect(null) {
            Toast.makeText(
                ctx,
                ctx.getString(R.string.feature_not_supported),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}