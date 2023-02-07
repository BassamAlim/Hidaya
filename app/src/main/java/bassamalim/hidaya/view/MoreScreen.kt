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
    navController: NavController = rememberNavController(),
    viewModel: MoreVM = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsState()
    val context = LocalContext.current

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
                viewModel.gotoTelawat(navController)
            }

            MySquareButton(
                textResId = R.string.qibla,
                imageResId = R.drawable.ic_qibla_compass
            ) {
                viewModel.gotoQibla(navController)
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
                viewModel.gotoQuiz(navController)
            }

            MySquareButton(
                textResId = R.string.hadeeth_books,
                imageResId = R.drawable.ic_books
            ) {
                viewModel.gotoBooks(navController)
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
                viewModel.gotoTV(navController)
            }

            MySquareButton(
                textResId = R.string.quran_radio,
                imageResId = R.drawable.ic_radio
            ) {
                viewModel.gotoRadio(navController)
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
                viewModel.gotoDateConverter(navController)
            }

            MySquareButton(R.string.settings, R.drawable.ic_settings) {
                viewModel.gotoSettings(navController)
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
                viewModel.contactMe()
            }

            MySquareButton(
                textResId = R.string.share_app,
                imageResId = R.drawable.ic_share
            ) {
                viewModel.shareApp()
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
                viewModel.gotoAbout(navController)
            }
        }
    }

    if (state.value.shouldShowUnsupported) {
        LaunchedEffect(null) {
            Toast.makeText(
                context,
                context.getString(R.string.feature_not_supported),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}