package bassamalim.hidaya.features.books.booksMenuFilter.ui

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
import bassamalim.hidaya.core.ui.components.MyTextButton
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyText

@Composable
fun BooksMenuFilterDialog(viewModel: BooksMenuFilterViewModel) {
    AlertDialog(
        onDismissRequest = viewModel::onDismiss,
        dismissButton = {
            DialogDismissButton(viewModel::onDismiss)
        },
        confirmButton = {
            DialogSubmitButton {
                viewModel.onSave()
                viewModel.onDismiss()
            }
        },
        title = {
            MyText(
                text = stringResource(R.string.choose_books),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            DialogContent(viewModel)
        }
    )
}

@Composable
private fun DialogContent(viewModel: BooksMenuFilterViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MyLazyColumn(
            modifier = Modifier.heightIn(0.dp, 300.dp),
            lazyList = {
                items(state.options.toList()) { (id, item) ->
                    CheckboxListItem(
                        title = item.name,
                        isChecked = item.isSelected,
                        onCheckedChange = { viewModel.onSelection(id, it) }
                    )
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MyTextButton(
                text = stringResource(R.string.select_all),
                onClick = viewModel::onSelectAll,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp
            )

            MyTextButton(
                text = stringResource(R.string.unselect_all),
                onClick = viewModel::onUnselectAll,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp
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

        MyText(title)
    }
}