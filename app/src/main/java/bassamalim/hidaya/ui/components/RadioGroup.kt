package bassamalim.hidaya.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bassamalim.hidaya.ui.theme.AppTheme

@Composable
fun CustomRadioGroup(
    options: List<String>,
    selection: MutableState<Int>,
    onSelect: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        options.forEachIndexed { index, text ->
            MyButton(
                text = text,
                textColor =
                    if (index == selection.value) AppTheme.colors.accent
                    else AppTheme.colors.text,
                innerPadding = PaddingValues(vertical = 10.dp),
                modifier =
                    if (index == selection.value)
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
                selection.value = index
                onSelect(index)
            }
        }
    }
}