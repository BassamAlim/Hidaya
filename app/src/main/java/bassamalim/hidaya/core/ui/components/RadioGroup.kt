package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.ui.theme.nsp

@Composable
fun RadioGroup(
    options: List<String>,
    selection: Int,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        options.forEachIndexed { index, text ->
            MyRectangleButton(
                text = text,
                textColor =
                    if (index == selection) AppTheme.colors.accent
                    else AppTheme.colors.text,
                innerPadding = PaddingValues(vertical = 10.dp),
                modifier =
                    if (index == selection)
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 16.dp)
                            .border(
                                width = 3.dp,
                                color = AppTheme.colors.accent,
                                shape = RoundedCornerShape(10.dp)
                            )
                    else
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 16.dp)
            ) {
                onSelect(index)
            }
        }
    }
}

@Composable
fun <V> HorizontalRadioGroup(
    selection: V,
    items: List<V>,
    entries: Array<String>,
    onSelect: (V) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
    ) {
        entries.forEachIndexed { index, text ->
            val item = items[index]
            MyRectangleButton(
                text = text,
                fontSize = 20.nsp,
                textColor =
                    if (item == selection) AppTheme.colors.accent
                    else AppTheme.colors.text,
                innerPadding = PaddingValues(vertical = 1.dp),
                modifier =
                    if (item == selection)
                        Modifier
                            .weight(1F)
                            .padding(horizontal = 5.dp)
                            .border(
                                width = 3.dp,
                                color = AppTheme.colors.accent,
                                shape = RoundedCornerShape(10.dp)
                            )
                    else
                        Modifier
                            .weight(1F)
                            .padding(horizontal = 5.dp),
                onClick = { onSelect(items[index]) }
            )
        }
    }
}