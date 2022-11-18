package bassamalim.hidaya.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.ui.theme.nsp

@Composable
fun RadioGroup(
    options: List<String>,
    selection: MutableState<Int>,
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

@Composable
fun HorizontalRadioGroup(
    options: List<String>,
    selection: MutableState<Int>,
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
                    if (index == selection.value) AppTheme.colors.accent
                    else AppTheme.colors.text,
                innerPadding = PaddingValues(vertical = 1.dp),
                modifier =
                    if (index == selection.value)
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
                selection.value = index
                onSelect(index)
            }
        }
    }
}

@Composable
fun CustomRadioGroup(
    options: List<Pair<Int, Int>>,
    selection: MutableState<Int>,
    onSelect: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        options.forEachIndexed { index, pair ->
            val text = stringResource(pair.first)
            Box(
                Modifier.padding(vertical = 6.dp)
            ) {
                MyClickableSurface(
                    padding = PaddingValues(vertical = 0.dp),
                    modifier =
                        if (index == selection.value)
                            Modifier.border(
                                width = 3.dp,
                                color = AppTheme.colors.accent,
                                shape = RoundedCornerShape(10.dp)
                            )
                        else Modifier,
                    onClick = {
                        selection.value = index
                        onSelect(index)
                    }
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp, horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(pair.second),
                            contentDescription = text
                        )

                        MyText(
                            text,
                            textColor =
                            if (index == selection.value) AppTheme.colors.accent
                            else AppTheme.colors.text,
                            modifier = Modifier.padding(start = 20.dp)
                        )
                    }
                }
            }
        }
    }
}