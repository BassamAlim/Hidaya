package bassamalim.hidaya.ui.components

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.ui.theme.AppTheme

@Composable
fun MyDialog(
    shown: MutableState<Boolean>,
    onDismiss: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = {
            shown.value = false
            onDismiss()
        }
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

@Composable
fun InfoDialog(
    title: String,
    text: String,
    shown: MutableState<Boolean>
) {
    MyDialog(
        shown = shown
    ) {
        Column(
            Modifier.padding(top = 5.dp, bottom = 20.dp, start = 10.dp, end = 10.dp)
        ) {
            Box(Modifier.fillMaxWidth()) {
                MyCloseBtn(Modifier.align(Alignment.CenterStart)) { shown.value = false }

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
    textResId: Int,
    pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(LocalContext.current),
    prefKey: String
) {
    if (!pref.getBoolean(prefKey, true)) return

    val doNotShowAgain = remember { mutableStateOf(false) }
    val shown = remember { mutableStateOf(true) }
    val onDismiss = {
        shown.value = false
        if (doNotShowAgain.value)
            pref.edit()
                .putBoolean(prefKey, false)
                .apply()
    }

    if (shown.value) {
        MyDialog(
            shown = shown,
            onDismiss = onDismiss
        ) {
            Column(
                Modifier.padding(top = 5.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
            ) {
                MyCloseBtn(onClose = onDismiss)

                MyText(stringResource(textResId))

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MyCheckbox(state = doNotShowAgain)

                    MyText(
                        stringResource(R.string.do_not_show_again),
                        textColor = AppTheme.colors.accent
                    )
                }
            }
        }
    }
}