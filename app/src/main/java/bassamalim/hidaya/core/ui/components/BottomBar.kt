package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.theme.AppTheme

@Composable
fun MyReadingBottomBar(
    textSize: Float,
    onSeek: (Float) -> Unit
) {
    var isSelected by remember { mutableStateOf(false) }

    BottomAppBar(
        backgroundColor = AppTheme.colors.primary,
        elevation = 12.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .height(32.dp)
                .fillMaxSize()
                .padding(horizontal = 30.dp)
        ) {
            MyIconButton(
                iconId = R.drawable.ic_text_size,
                tint = AppTheme.colors.onPrimary,
                size = 38.dp,
                onClick = {
                    isSelected = !isSelected
                }
            )

            if (isSelected) {
                MySlider(
                    value = textSize,
                    valueRange = 1F..40F,
                    modifier = Modifier.padding(15.dp),
                    onValueChange = onSeek
                )
            }
        }
    }
}