package bassamalim.hidaya.features.more.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
                textResId = R.string.recitations,
                imagePainter = R.drawable.ic_headphone,
                onClick = viewModel::onRecitationsClick
            )

            MySquareButton(
                textResId = R.string.qibla,
                imagePainter = R.drawable.ic_qibla_compass,
                onClick = viewModel::onQiblaClick
            )
        }

        MyRow {
            MySquareButton(
                textResId = R.string.quiz_title,
                imagePainter = R.drawable.ic_quiz,
                onClick = viewModel::onQuizClick
            )

            MySquareButton(
                textResId = R.string.hadeeth_books,
                imagePainter = R.drawable.ic_books,
                onClick = viewModel::onBooksClick
            )
        }

        MyRow {
            MySquareButton(
                textResId = R.string.tv_channels,
                imagePainter = R.drawable.ic_television,
                onClick = viewModel::onTvClick
            )

            MySquareButton(
                textResId = R.string.quran_radio,
                imagePainter = R.drawable.ic_radio,
                onClick = viewModel::onRadioClick
            )
        }

        MyRow {
            MySquareButton(
                textResId = R.string.date_converter,
                imagePainter = R.drawable.ic_calendar,
                onClick = viewModel::onDateConverterClick
            )

            MySquareButton(
                textResId = R.string.settings,
                imagePainter = R.drawable.ic_settings,
                onClick = viewModel::onSettingsClick
            )
        }

        MyRow {
            MySquareButton(
                textResId = R.string.contact,
                imagePainter = R.drawable.ic_mail,
                onClick = { viewModel.onContactClick(context) }
            )

            MySquareButton(
                textResId = R.string.share_app,
                imagePainter = R.drawable.ic_share,
                onClick = { viewModel.onShareClick(context) }
            )
        }

        MyRow {
            MySquareButton(
                textResId = R.string.about,
                imagePainter = R.drawable.ic_info,
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