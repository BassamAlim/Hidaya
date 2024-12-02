package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReaderBottomBar(textSize: Float, onSeek: (Float) -> Unit) {
    var isSelected by remember { mutableStateOf(false) }

    BottomAppBar(
        Modifier.height(60.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            MyIconButton(
                imageVector = Icons.Default.FormatSize,
                iconModifier = Modifier.size(30.dp),
                contentColor = MaterialTheme.colorScheme.onPrimary,
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