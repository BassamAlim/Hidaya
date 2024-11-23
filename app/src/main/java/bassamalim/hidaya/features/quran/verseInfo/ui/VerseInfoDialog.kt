package bassamalim.hidaya.features.quran.verseInfo.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyCloseBtn
import bassamalim.hidaya.core.ui.components.MyDialog
import bassamalim.hidaya.core.ui.components.MyText

@Composable
fun VerseInfoDialog(
    viewModel: VerseInfoViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MyDialog(shown = true, onDismiss = viewModel::onDismiss) {
        Column(
            Modifier
                .padding(top = 5.dp, bottom = 20.dp, start = 10.dp, end = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Header(viewModel::onDismiss)

            // Interpretation
            MyText(
                text = state.interpretation,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun Header(onDismiss: () -> Unit) {
    Box(Modifier.fillMaxWidth()) {
        MyCloseBtn(Modifier.align(Alignment.CenterStart)) { onDismiss() }

        MyText(
            text = stringResource(R.string.interpretation),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}