package bassamalim.hidaya.features.more.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyColumn
import bassamalim.hidaya.core.ui.components.MyRow
import bassamalim.hidaya.core.ui.components.MySquareButton

@Composable
fun MoreScreen(viewModel: MoreViewModel, snackBarHostState: SnackbarHostState) {
    val context = LocalContext.current
    val unsupportedMessage = stringResource(R.string.feature_not_supported)

    MyColumn(
        modifier = Modifier.padding(top = 5.dp),
        scrollable = true
    ) {
        MyRow {
            MySquareButton(
                text = stringResource(R.string.recitations),
                drawableId = R.drawable.ic_headphone,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = { viewModel.onRecitationsClick(snackBarHostState, unsupportedMessage) }
            )

            MySquareButton(
                text = stringResource(R.string.qibla),
                drawableId = R.drawable.ic_qibla_compass,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = viewModel::onQiblaClick
            )
        }

        MyRow {
            MySquareButton(
                text = stringResource(R.string.quiz_title),
                drawableId = R.drawable.ic_exam,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = viewModel::onQuizClick
            )

            MySquareButton(
                text = stringResource(R.string.hadeeth_books),
                drawableId = R.drawable.ic_books,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = viewModel::onBooksClick
            )
        }

        MyRow {
            MySquareButton(
                text = stringResource(R.string.tv_channels),
                drawableId = R.drawable.ic_television,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = viewModel::onTvClick
            )

            MySquareButton(
                text = stringResource(R.string.quran_radio),
                drawableId = R.drawable.ic_radio,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = { viewModel.onRadioClick(snackBarHostState, unsupportedMessage) }
            )
        }

        MyRow {
            MySquareButton(
                text = stringResource(R.string.date_converter),
                drawableId = R.drawable.ic_calendar,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = viewModel::onDateConverterClick
            )

            MySquareButton(
                text = stringResource(R.string.settings),
                drawableId = R.drawable.ic_settings,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = viewModel::onSettingsClick
            )
        }

        MyRow {
            MySquareButton(
                text = stringResource(R.string.contact),
                drawableId = R.drawable.ic_mail,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = { viewModel.onContactClick(context) }
            )

            MySquareButton(
                text = stringResource(R.string.share_app),
                drawableId = R.drawable.ic_share,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = { viewModel.onShareClick(context) }
            )
        }

        MyRow {
            MySquareButton(
                text = stringResource(R.string.about),
                drawableId = R.drawable.ic_info,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = viewModel::onAboutClick
            )
        }
    }
}