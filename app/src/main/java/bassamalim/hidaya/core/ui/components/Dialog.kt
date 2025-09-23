package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import bassamalim.hidaya.R

@Composable
fun MyDialog(
    shown: Boolean,
    onDismiss: () -> Unit = {},
    content: @Composable () -> Unit
) {
    if (shown) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Surface(color = Color.Transparent) {
                Box(
                    Modifier.background(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface
                    )
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun FullScreenDialog(
    shown: Boolean,
    onDismiss: () -> Unit = {},
    content: @Composable () -> Unit
) {
    if (shown) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface
                    )
            ) {
                content()
            }
        }
    }
}

@Composable
fun TutorialDialog(shown: Boolean, text: String, onDismissRequest: (Boolean) -> Unit) {
    if (!shown) return

    var doNotShowAgain by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { onDismissRequest(doNotShowAgain) },
        confirmButton = {
            DialogDismissButton(
                text = stringResource(R.string.hide),
                onDismiss = { onDismissRequest(doNotShowAgain) }
            )
        },
        text = {
            Column {
                MyText(text)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MyCheckbox(
                        isChecked = doNotShowAgain,
                        onCheckedChange = { doNotShowAgain = it }
                    )

                    MyText(
                        text = stringResource(R.string.do_not_show_again),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    )
}

@Composable
fun DialogTitle(title: String) {
    MyText(text = title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
}

@Composable
fun DialogSubmitButton(text: String = stringResource(R.string.select), onSubmit: () -> Unit) {
    TextButton(onClick = onSubmit) {
        MyText(text = text, modifier = Modifier.padding(horizontal = 6.dp))
    }
}

@Composable
fun DialogDismissButton(text: String = stringResource(R.string.cancel), onDismiss: () -> Unit) {
    TextButton(onClick = onDismiss) {
        MyText(text = text, modifier = Modifier.padding(horizontal = 6.dp))
    }
}