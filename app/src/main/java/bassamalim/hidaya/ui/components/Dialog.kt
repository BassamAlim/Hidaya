package bassamalim.hidaya.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import bassamalim.hidaya.ui.theme.AppTheme

@Composable
fun MyDialog(
    shown: MutableState<Boolean>,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = { shown.value = false }
    ) {
        Surface(
            color = Color.Transparent
        ) {
            Box(
                Modifier.background(
                    shape = RoundedCornerShape(16.dp),
                    color = AppTheme.colors.background
                )
            ) {
                content()
            }
        }
    }
}