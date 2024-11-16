package bassamalim.hidaya.features.dateEditor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyDialog
import bassamalim.hidaya.core.ui.components.MyIconButton
import bassamalim.hidaya.core.ui.components.MyRectangleButton
import bassamalim.hidaya.core.ui.components.MyText

@Composable
fun DateEditorDialog(
    viewModel: DateEditorViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MyDialog(shown = true) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            MyText(
                stringResource(R.string.adjust_date),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            // Date offset
            MyText(
                if (state.isUnchanged) stringResource(R.string.unchanged)
                else state.dateOffsetText,
                fontSize = 22.sp
            )

            DateOffsetEditor(
                dateText = state.dateText,
                onPreviousDayClick = viewModel::onPreviousDayClick,
                onNextDayClick = viewModel::onNextDayClick
            )

            BottomBar(
                onSave = viewModel::onSave,
                onDismiss = viewModel::onDismiss
            )
        }
    }
}

@Composable
private fun DateOffsetEditor(
    dateText: String,
    onPreviousDayClick: () -> Unit,
    onNextDayClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        MyIconButton(
            iconId = R.drawable.ic_left_arrow,
            tint = MaterialTheme.colorScheme.onSurface,
            onClick = onPreviousDayClick
        )

        MyText(
            dateText,
            fontSize = 22.sp
        )

        MyIconButton(
            iconId = R.drawable.ic_right_arrow,
            tint = MaterialTheme.colorScheme.onSurface,
            onClick = onNextDayClick
        )
    }
}

@Composable
private fun BottomBar(
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MyRectangleButton(
            text = stringResource(R.string.save),
            onClick = onSave
        )

        MyRectangleButton(
            text = stringResource(R.string.cancel),
            onClick = onDismiss
        )
    }
}