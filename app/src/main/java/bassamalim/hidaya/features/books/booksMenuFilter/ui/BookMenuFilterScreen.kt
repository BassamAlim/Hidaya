package bassamalim.hidaya.features.books.booksMenuFilter.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
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
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyRectangleButton
import bassamalim.hidaya.core.ui.components.MySquareButton
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.theme.AppTheme

@Composable
fun BooksMenuFilterDialog(
    viewModel: BooksMenuFilterViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) return

    MyDialog(
        shown = true,
        onDismiss = viewModel::onDismiss
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MyText(
                text = stringResource(R.string.choose_books),
                modifier = Modifier.padding(vertical = 10.dp),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            MyLazyColumn(
                Modifier.heightIn(0.dp, 300.dp),
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
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MyText(
                    stringResource(R.string.select_all),
                    textColor = AppTheme.colors.accent,
                    modifier = Modifier.clickable(onClick = viewModel::onSelectAll)
                )

                MyText(
                    stringResource(R.string.unselect_all),
                    modifier = Modifier.clickable(onClick = viewModel::onUnselectAll),
                    textColor = AppTheme.colors.accent
                )
            }

            MyRectangleButton(
                text = stringResource(R.string.select),
                modifier = Modifier.padding(horizontal = 10.dp),
                onClick = viewModel::onSave
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
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = AppTheme.colors.accent,
                uncheckedColor = AppTheme.colors.text
            )
        )

        MyText(title)
    }
}