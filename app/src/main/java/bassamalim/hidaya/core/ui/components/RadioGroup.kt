package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
            MyButton(
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
fun HorizontalRadioGroup(
    options: List<String>,
    selection: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
    ) {
        options.forEachIndexed { index, text ->
            MyButton(
                text = text,
                fontSize = 20.nsp,
                textColor =
                    if (index == selection) AppTheme.colors.accent
                    else AppTheme.colors.text,
                innerPadding = PaddingValues(vertical = 1.dp),
                modifier =
                    if (index == selection)
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
                            .padding(horizontal = 5.dp)
            ) {
                onSelect(index)
            }
        }
    }
}