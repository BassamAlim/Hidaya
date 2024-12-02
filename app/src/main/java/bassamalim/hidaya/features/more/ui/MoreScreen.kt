package bassamalim.hidaya.features.more.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
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
                imageVector = Icons.Default.Headphones,
                tint = MaterialTheme.colorScheme.onSurface,
                iconSize = 56.dp,
                onClick = { viewModel.onRecitationsClick(snackBarHostState, unsupportedMessage) }
            )

            MySquareButton(
                text = stringResource(R.string.qibla),
                drawableId = R.drawable.ic_qibla_compass,
                tint = MaterialTheme.colorScheme.onSurface,
                iconSize = 56.dp,
                onClick = viewModel::onQiblaClick
            )
        }

        MyRow {
            MySquareButton(
                text = stringResource(R.string.quiz_title),
                imageVector = Icons.AutoMirrored.Default.FactCheck,
                tint = MaterialTheme.colorScheme.onSurface,
                iconSize = 56.dp,
                onClick = viewModel::onQuizClick
            )

            MySquareButton(
                text = stringResource(R.string.hadeeth_books),
                drawableId = R.drawable.ic_books,
                tint = MaterialTheme.colorScheme.onSurface,
                iconSize = 56.dp,
                onClick = viewModel::onBooksClick
            )
        }

        MyRow {
            MySquareButton(
                text = stringResource(R.string.tv_channels),
                imageVector = Icons.Default.LiveTv,
                tint = MaterialTheme.colorScheme.onSurface,
                iconSize = 56.dp,
                onClick = viewModel::onTvClick
            )

            MySquareButton(
                text = stringResource(R.string.quran_radio),
                imageVector = Icons.Default.Radio,
                tint = MaterialTheme.colorScheme.onSurface,
                iconSize = 56.dp,
                onClick = { viewModel.onRadioClick(snackBarHostState, unsupportedMessage) }
            )
        }

        MyRow {
            MySquareButton(
                text = stringResource(R.string.date_converter),
                imageVector = Icons.Default.CalendarMonth,
                tint = MaterialTheme.colorScheme.onSurface,
                iconSize = 56.dp,
                onClick = viewModel::onDateConverterClick
            )

            MySquareButton(
                text = stringResource(R.string.settings),
                imageVector = Icons.Default.Settings,
                tint = MaterialTheme.colorScheme.onSurface,
                iconSize = 56.dp,
                onClick = viewModel::onSettingsClick
            )
        }

        MyRow {
            MySquareButton(
                text = stringResource(R.string.contact),
                imageVector = Icons.Default.Mail,
                tint = MaterialTheme.colorScheme.onSurface,
                iconSize = 56.dp,
                onClick = { viewModel.onContactClick(context) }
            )

            MySquareButton(
                text = stringResource(R.string.share_app),
                imageVector = Icons.Default.Share,
                tint = MaterialTheme.colorScheme.onSurface,
                iconSize = 56.dp,
                onClick = { viewModel.onShareClick(context) }
            )
        }

        MyRow {
            MySquareButton(
                text = stringResource(R.string.about),
                imageVector = Icons.Default.Info,
                onClick = viewModel::onAboutClick,
                iconSize = 56.dp,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}