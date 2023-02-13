package bassamalim.hidaya.view

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.R
import bassamalim.hidaya.ui.components.MyButton
import bassamalim.hidaya.ui.components.MySquareButton
import bassamalim.hidaya.viewmodel.AthkarVM
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AthkarUI(
    navController: NavController = rememberAnimatedNavController(),
    viewModel: AthkarVM = hiltViewModel()
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
            viewModel.onAllAthkarClick(navController)
        }

        LargeBtn(R.string.favorite_athkar) {
            viewModel.onFavoriteAthkarClick(navController)
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MySquareButton(R.string.day_and_night_thikrs, R.drawable.ic_day_and_night) {
                viewModel.onCategoryClick(navController, category = 0)
            }
            MySquareButton(R.string.prayers_thikrs, R.drawable.ic_praying) {
                viewModel.onCategoryClick(navController, category = 1)
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MySquareButton(R.string.quran_thikrs, R.drawable.ic_closed_quran) {
                viewModel.onCategoryClick(navController, category = 2)
            }
            MySquareButton(R.string.actions_thikrs, R.drawable.ic_actions) {
                viewModel.onCategoryClick(navController, category = 3)
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MySquareButton(R.string.events_thikrs, R.drawable.ic_events) {
                viewModel.onCategoryClick(navController, category = 4)
            }
            MySquareButton(R.string.emotion_thikrs, R.drawable.ic_emotion) {
                viewModel.onCategoryClick(navController, category = 5)
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MySquareButton(R.string.places_thikrs, R.drawable.ic_going_out) {
                viewModel.onCategoryClick(navController, category = 6)
            }
            MySquareButton(R.string.title_more, R.drawable.ic_duaa_moon) {
                viewModel.onCategoryClick(navController, category = 7)
            }
        }
    }
}

@Composable
private fun LargeBtn(textResId: Int, onClick: () -> Unit) {
    MyButton(
        text = stringResource(textResId),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 24.dp),
        fontWeight = FontWeight.Bold,
        onClick = onClick
    )
}