package bassamalim.hidaya.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R
import bassamalim.hidaya.ui.components.MyButton
import bassamalim.hidaya.ui.components.MyDialog
import bassamalim.hidaya.ui.components.MyImageButton
import bassamalim.hidaya.ui.components.MyText
import bassamalim.hidaya.ui.theme.AppTheme

@Composable
fun DateEditorDialog(
    shown: Boolean,
    offsetText: String,
    dateText: String,
    onNextDay: () -> Unit,
    onPreviousDay: () -> Unit,
    onCancel: () -> Unit,
    onSubmit: () -> Unit
) {
    MyDialog(shown) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MyText(
                stringResource(R.string.adjust_date),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            MyText(
                offsetText,
                fontSize = 22.sp,
                textColor = AppTheme.colors.accent
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                MyImageButton(
                    imageResId = R.drawable.ic_left_arrow
                ) {
                    onPreviousDay()
                }

                MyText(dateText, fontSize = 22.sp)

                MyImageButton(
                    imageResId = R.drawable.ic_right_arrow
                ) {
                    onNextDay()
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MyButton(
                    text = stringResource(R.string.save)
                ) {
                    onSubmit()
                }

                MyButton(
                    text = stringResource(R.string.cancel)
                ) {
                    onCancel()
                }
            }
        }
    }
}