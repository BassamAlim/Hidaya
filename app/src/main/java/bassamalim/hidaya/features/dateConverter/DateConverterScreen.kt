package bassamalim.hidaya.features.dateConverter

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MySquareButton
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.theme.AppTheme

@Composable
fun DateConverterUI(
    vm: DateConverterVM
) {
    val st by vm.uiState.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    MyScaffold(stringResource(R.string.date_converter)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(bottom = 50.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column {
                MySquareButton(
                    text = stringResource(R.string.pick_hijri_date),
                    fontSize = 22.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 15.dp, horizontal = 30.dp),
                    innerPadding = PaddingValues(vertical = 10.dp, horizontal = 15.dp)
                ) {
                    vm.onPickHijriClk()
                }

                MySquareButton(
                    text = stringResource(R.string.pick_gregorian_date),
                    fontSize = 22.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 15.dp, horizontal = 30.dp),
                    innerPadding = PaddingValues(vertical = 10.dp, horizontal = 15.dp)
                ) {
                    vm.onPickGregorianClk(ctx)
                }
            }

            ResultSpace(stringResource(R.string.hijri_date), st.hijriValues)

            ResultSpace(stringResource(R.string.gregorian_date), st.gregorianValues)
        }
    }
}

@Composable
private fun ResultSpace(title: String, values: List<String>) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(
                width = 3.dp,
                color = AppTheme.colors.accent,
                shape = RoundedCornerShape(size = 14.dp)
            ),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MyText(
            text = title,
            modifier = Modifier.padding(10.dp),
            fontSize = 22.sp
        )

        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MyText(
                    text = stringResource(R.string.day),
                    Modifier.padding(10.dp)
                )

                MyText(
                    text = values[2],
                    Modifier.padding(10.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MyText(
                    text = stringResource(R.string.month),
                    Modifier.padding(10.dp)
                )

                MyText(
                    text = values[1],
                    Modifier.padding(10.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MyText(
                    text = stringResource(R.string.year),
                    Modifier.padding(10.dp)
                )

                MyText(
                    text = values[0],
                    Modifier.padding(10.dp)
                )
            }
        }
    }
}