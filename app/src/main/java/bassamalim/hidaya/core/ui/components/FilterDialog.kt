package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.theme.AppTheme

@Composable
fun FilterDialog(
    shown: Boolean,
    title: String,
    itemTitles: List<String>,
    itemSelections: Array<Boolean>,
    onDismiss: (Array<Boolean>) -> Unit
) {
    val selections = itemSelections.toList().toMutableStateList()

    MyDialog(shown) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MyText(
                title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            MyLazyColumn(
                Modifier.heightIn(0.dp, 300.dp),
                lazyList = {
                    itemsIndexed(itemTitles) { index, _ ->
                        CheckboxListItem(
                            title = itemTitles[index],
                            isChecked = selections[index]
                        ) { isSelected ->
                            selections[index] = isSelected
                        }
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
                    modifier = Modifier.clickable { selections.fill(true) }
                )

                MyText(
                    stringResource(R.string.unselect_all),
                    modifier = Modifier.clickable { selections.fill(false) },
                    textColor = AppTheme.colors.accent
                )
            }

            MySquareButton(
                text = stringResource(R.string.select),
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {
                onDismiss(selections.toTypedArray())
            }
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
            onCheckedChange = { onCheckedChange(it) },
            colors = CheckboxDefaults.colors(
                checkedColor = AppTheme.colors.accent,
                uncheckedColor = AppTheme.colors.text
            )
        )

        MyText(title)
    }
}