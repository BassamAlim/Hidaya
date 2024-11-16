package bassamalim.hidaya.features.dateConverter.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import bassamalim.hidaya.core.ui.components.MyRectangleButton
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MyText

@Composable
fun DateConverterScreen(viewModel: DateConverterViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    MyScaffold(stringResource(R.string.date_converter)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(bottom = 50.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column {
                MyRectangleButton(
                    text = stringResource(R.string.pick_hijri_date),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 15.dp, horizontal = 30.dp),
                    fontSize = 22.sp,
                    innerPadding = PaddingValues(vertical = 10.dp, horizontal = 15.dp),
                    onClick = viewModel::onPickHijriClick
                )

                MyRectangleButton(
                    text = stringResource(R.string.pick_gregorian_date),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 15.dp, horizontal = 30.dp),
                    fontSize = 22.sp,
                    innerPadding = PaddingValues(vertical = 10.dp, horizontal = 15.dp),
                    onClick = { viewModel.onPickGregorianClick(context) }
                )
            }

            ResultSpace(
                title = stringResource(R.string.hijri_date),
                date = state.hijriDate
            )

            ResultSpace(
                title = stringResource(R.string.gregorian_date),
                date = state.gregorianDate
            )
        }
    }
}

@Composable
private fun ResultSpace(
    title: String,
    date: Date
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(
                width = 3.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(size = 14.dp)
            ),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MyText(
            title,
            Modifier.padding(10.dp),
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
                    stringResource(R.string.day),
                    Modifier.padding(10.dp)
                )

                MyText(
                    date.day,
                    Modifier.padding(10.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MyText(
                    stringResource(R.string.month),
                    Modifier.padding(10.dp)
                )

                MyText(
                    date.month,
                    Modifier.padding(10.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MyText(
                    stringResource(R.string.year),
                    Modifier.padding(10.dp)
                )

                MyText(
                    date.year,
                    Modifier.padding(10.dp)
                )
            }
        }
    }
}