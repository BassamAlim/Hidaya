package bassamalim.hidaya.features.recitations.recitersMenuFilter.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import bassamalim.hidaya.core.ui.components.DialogDismissButton
import bassamalim.hidaya.core.ui.components.DialogSubmitButton
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyText

@Composable
fun RecitersMenuFilterDialog(
    viewModel: RecitersMenuFilterViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) return

    AlertDialog(
        onDismissRequest = viewModel::onDismiss,
        dismissButton = {
            DialogDismissButton { viewModel.onDismiss() }
        },
        confirmButton = {
            DialogSubmitButton(text = stringResource(R.string.select)) {
                viewModel.onSave()
            }
        },
        title = {
            MyText(
                text = stringResource(R.string.choose_narration),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            DialogContent(viewModel, state)
        }
    )
}

@Composable
private fun DialogContent(
    viewModel: RecitersMenuFilterViewModel,
    state: RecitersMenuFilterUiState
) {
    Column {
        MyLazyColumn(
            modifier = Modifier.heightIn(0.dp, 300.dp),
            lazyList = {
                items(state.options.toList()) { (name, isSelected) ->
                    CheckboxListItem(
                        title = name,
                        isChecked = isSelected,
                        onCheckedChange = { viewModel.onSelection(name, it) }
                    )
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            MyText(
                text = stringResource(R.string.select_all),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = viewModel::onSelectAll)
            )

            MyText(
                text = stringResource(R.string.unselect_all),
                modifier = Modifier.clickable(onClick = viewModel::onUnselectAll),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CheckboxListItem(
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurface
            )
        )

        MyText(
            text = title,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}