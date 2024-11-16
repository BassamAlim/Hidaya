package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bassamalim.hidaya.R

@Composable
fun MyReadingBottomBar(
    textSize: Float,
    onSeek: (Float) -> Unit
) {
    var isSelected by remember { mutableStateOf(false) }

    BottomAppBar(
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
                tint = MaterialTheme.colorScheme.onPrimary,
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