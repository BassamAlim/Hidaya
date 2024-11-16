package bassamalim.hidaya.features.more.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyColumn
import bassamalim.hidaya.core.ui.components.MyRow
import bassamalim.hidaya.core.ui.components.MySquareButton

@Composable
fun MoreScreen(viewModel: MoreViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    MyColumn(
        Modifier.padding(top = 5.dp),
        scrollable = true
    ) {
        MyRow {
            MySquareButton(
                text = stringResource(R.string.recitations),
                drawableId = R.drawable.ic_headphone,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = viewModel::onRecitationsClick
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
                onClick = viewModel::onRadioClick
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