package bassamalim.hidaya.features.more

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MySquareButton
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination
@Composable
fun MoreUI(
    vm: MoreVM = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    val st by vm.uiState.collectAsStateWithLifecycle()
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
                vm.gotoTelawat(navigator)
            }

            MySquareButton(
                textResId = R.string.qibla,
                imageResId = R.drawable.ic_qibla_compass
            ) {
                vm.gotoQibla(navigator)
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
                vm.gotoQuiz(navigator)
            }

            MySquareButton(
                textResId = R.string.hadeeth_books,
                imageResId = R.drawable.ic_books
            ) {
                vm.gotoBooks(navigator)
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
                vm.gotoTV(navigator)
            }

            MySquareButton(
                textResId = R.string.quran_radio,
                imageResId = R.drawable.ic_radio
            ) {
                vm.gotoRadio(navigator)
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
                vm.gotoDateConverter(navigator)
            }

            MySquareButton(R.string.settings, R.drawable.ic_settings) {
                vm.gotoSettings(navigator)
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
                vm.gotoAbout(navigator)
            }
        }
    }

    if (st.shouldShowUnsupported) {
        LaunchedEffect(null) {
            Toast.makeText(
                ctx,
                ctx.getString(R.string.feature_not_supported),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}