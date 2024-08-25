package bassamalim.hidaya.features.more.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyColumn
import bassamalim.hidaya.core.ui.components.MyRow
import bassamalim.hidaya.core.ui.components.MySquareButton

@Composable
fun MoreUI(
    viewModel: MoreViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    MyColumn(
        Modifier.padding(top = 5.dp),
        scrollable = true
    ) {
        MyRow {
            MySquareButton(
                text = stringResource(R.string.recitations),
                imagePainter = painterResource(R.drawable.ic_headphone),
                onClick = viewModel::onRecitationsClick
            )

            MySquareButton(
                text = stringResource(R.string.qibla),
                imagePainter = painterResource(R.drawable.ic_qibla_compass),
                onClick = viewModel::onQiblaClick
            )
        }

        MyRow {
            MySquareButton(
                text = stringResource(R.string.quiz_title),
                imagePainter = painterResource(R.drawable.ic_quiz),
                onClick = viewModel::onQuizClick
            )

            MySquareButton(
                text = stringResource(R.string.hadeeth_books),
                imagePainter = painterResource(R.drawable.ic_books),
                onClick = viewModel::onBooksClick
            )
        }

        MyRow {
            MySquareButton(
                text = stringResource(R.string.tv_channels),
                imagePainter = painterResource(R.drawable.ic_television),
                onClick = viewModel::onTvClick
            )

            MySquareButton(
                text = stringResource(R.string.quran_radio),
                imagePainter = painterResource(R.drawable.ic_radio),
                onClick = viewModel::onRadioClick
            )
        }

        MyRow {
            MySquareButton(
                text = stringResource(R.string.date_converter),
                imagePainter = painterResource(R.drawable.ic_calendar),
                onClick = viewModel::onDateConverterClick
            )

            MySquareButton(
                text = stringResource(R.string.settings),
                imagePainter = painterResource(R.drawable.ic_settings),
                onClick = viewModel::onSettingsClick
            )
        }

        MyRow {
            MySquareButton(
                text = stringResource(R.string.contact),
                imagePainter = painterResource(R.drawable.ic_mail),
                onClick = { viewModel.onContactClick(context) }
            )

            MySquareButton(
                text = stringResource(R.string.share_app),
                imagePainter = painterResource(R.drawable.ic_share),
                onClick = { viewModel.onShareClick(context) }
            )
        }

        MyRow {
            MySquareButton(
                text = stringResource(R.string.about),
                imagePainter = painterResource(R.drawable.ic_info),
                onClick = viewModel::onAboutClick
            )
        }
    }

    if (state.shouldShowUnsupported)
        UnsupportedFeatureToast(context)
}

@Composable
private fun UnsupportedFeatureToast(context: Context) {
    LaunchedEffect(null) {
        Toast.makeText(
            context,
            context.getString(R.string.feature_not_supported),
            Toast.LENGTH_SHORT
        ).show()
    }
}