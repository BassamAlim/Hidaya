package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.theme.AppTheme

@Composable
fun MyDialog(
    shown: Boolean,
    easyDismiss: Boolean = true,
    onDismiss: () -> Unit = {},
    content: @Composable () -> Unit
) {
    if (shown) {
        Dialog(
            onDismissRequest = { onDismiss() },
            properties = DialogProperties(
                dismissOnBackPress = easyDismiss,
                dismissOnClickOutside = easyDismiss
            )
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
}

@Composable
fun InfoDialog(
    title: String,
    text: String,
    shown: Boolean,
    onDismiss: () -> Unit = {}
) {
    MyDialog(
        shown = shown,
        onDismiss = onDismiss
    ) {
        Column(
            Modifier.padding(top = 5.dp, bottom = 20.dp, start = 10.dp, end = 10.dp)
        ) {
            Box(Modifier.fillMaxWidth()) {
                MyCloseBtn(Modifier.align(Alignment.CenterStart)) { onDismiss() }

                MyText(
                    title,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            MyText(text, Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun TutorialDialog(
    shown: Boolean,
    text: String,
    onDismiss: (Boolean) -> Unit
) {
    var doNotShowAgain by remember { mutableStateOf(false) }

    if (shown) {
        Dialog(
            onDismissRequest = { onDismiss(doNotShowAgain) }
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
                    Column(
                        Modifier.padding(top = 5.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
                    ) {
                        MyCloseBtn(onClose = { onDismiss(doNotShowAgain) })

                        MyText(text)

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MyCheckbox(
                                isChecked = doNotShowAgain,
                                onCheckedChange = { doNotShowAgain = it }
                            )

                            MyText(
                                stringResource(R.string.do_not_show_again),
                                textColor = AppTheme.colors.accent
                            )
                        }
                    }
                }
            }
        }
    }
}