package bassamalim.hidaya.features.athkar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MySquareButton
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination
@Composable
fun AthkarUI(
    viewModel: AthkarVM = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 5.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LargeBtn(R.string.all_athkar) {
            viewModel.onAllAthkarClick(navigator)
        }

        LargeBtn(R.string.favorite_athkar) {
            viewModel.onFavoriteAthkarClick(navigator)
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MySquareButton(R.string.day_and_night_thikrs, R.drawable.ic_day_and_night) {
                viewModel.onCategoryClick(navigator, category = 0)
            }
            MySquareButton(R.string.prayers_thikrs, R.drawable.ic_praying) {
                viewModel.onCategoryClick(navigator, category = 1)
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MySquareButton(R.string.quran_thikrs, R.drawable.ic_closed_quran) {
                viewModel.onCategoryClick(navigator, category = 2)
            }
            MySquareButton(R.string.actions_thikrs, R.drawable.ic_actions) {
                viewModel.onCategoryClick(navigator, category = 3)
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MySquareButton(R.string.events_thikrs, R.drawable.ic_events) {
                viewModel.onCategoryClick(navigator, category = 4)
            }
            MySquareButton(R.string.emotion_thikrs, R.drawable.ic_emotion) {
                viewModel.onCategoryClick(navigator, category = 5)
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MySquareButton(R.string.places_thikrs, R.drawable.ic_going_out) {
                viewModel.onCategoryClick(navigator, category = 6)
            }
            MySquareButton(R.string.title_more, R.drawable.ic_duaa_moon) {
                viewModel.onCategoryClick(navigator, category = 7)
            }
        }
    }
}

@Composable
private fun LargeBtn(textResId: Int, onClick: () -> Unit) {
    MySquareButton(
        text = stringResource(textResId),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 24.dp),
        fontWeight = FontWeight.Bold,
        onClick = onClick
    )
}