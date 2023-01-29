package bassamalim.hidaya.view

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import bassamalim.hidaya.R
import bassamalim.hidaya.other.HijriDatePickerDialog
import bassamalim.hidaya.ui.components.MyButton
import bassamalim.hidaya.ui.components.MyScaffold
import bassamalim.hidaya.ui.components.MyText
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.viewmodel.DateConverterVM

@Composable
fun DateConverterUI(
    navController: NavController = rememberNavController(),
    viewModel: DateConverterVM = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    MyScaffold(stringResource(R.string.date_converter)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(bottom = 50.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column {
                MyButton(
                    text = stringResource(R.string.pick_hijri_date),
                    fontSize = 22.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 15.dp, horizontal = 30.dp),
                    innerPadding = PaddingValues(vertical = 10.dp, horizontal = 15.dp)
                ) {
                    viewModel.pickHijri()
                }

                MyButton(
                    text = stringResource(R.string.pick_gregorian_date),
                    fontSize = 22.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 15.dp, horizontal = 30.dp),
                    innerPadding = PaddingValues(vertical = 10.dp, horizontal = 15.dp)
                ) {
                    viewModel.pickGregorian()
                }
            }

            ResultSpace(stringResource(R.string.hijri_date), state.hijriValues)

            ResultSpace(stringResource(R.string.gregorian_date), state.gregorianValues)
        }

        HijriDatePickerDialog(
            LocalContext.current,
            state.hijriDatePickerShown,
            viewModel.hijriCalendar,
            onCancelClick = { viewModel.onHijriPickCancel() }
        ) {
            viewModel.onHijriPick(it)
        }.MyHijriDatePickerDialog()
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