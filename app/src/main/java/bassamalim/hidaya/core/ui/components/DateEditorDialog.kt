package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.theme.AppTheme

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
                fontSize = 22.sp
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                MyIconButton(
                    iconId = R.drawable.ic_left_arrow,
                    tint = AppTheme.colors.text,
                    onClick = { onPreviousDay() }
                )

                MyText(
                    dateText,
                    fontSize = 22.sp
                )

                MyIconButton(
                    iconId = R.drawable.ic_right_arrow,
                    tint = AppTheme.colors.text,
                    onClick = { onNextDay() }
                )
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MySquareButton(
                    text = stringResource(R.string.save)
                ) {
                    onSubmit()
                }

                MySquareButton(
                    text = stringResource(R.string.cancel)
                ) {
                    onCancel()
                }
            }
        }
    }
}