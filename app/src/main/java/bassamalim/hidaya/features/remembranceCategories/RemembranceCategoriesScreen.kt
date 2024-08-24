package bassamalim.hidaya.features.remembranceCategories

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
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MySquareButton

@Composable
fun RemembranceScreen(
    vm: RemembranceCategoriesViewModel
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 5.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // all recitations button
        LargeBtn(R.string.all_remembrances) {
            vm.onAllAthkarClick()
        }
        // favorite recitations button
        LargeBtn(R.string.favorite_remembrances) {
            vm.onFavoriteAthkarClick()
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // day and night recitations button
            MySquareButton(R.string.day_and_night_remembrances, R.drawable.ic_day_and_night) {
                vm.onCategoryClick(category = 0)
            }
            // prayer recitations button
            MySquareButton(R.string.prayers_remembrances, R.drawable.ic_praying) {
                vm.onCategoryClick(category = 1)
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // quran recitations button
            MySquareButton(R.string.quran_remembrances, R.drawable.ic_closed_quran) {
                vm.onCategoryClick(category = 2)
            }
            // actions recitations button
            MySquareButton(R.string.actions_remembrances, R.drawable.ic_actions) {
                vm.onCategoryClick(category = 3)
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // events recitations button
            MySquareButton(R.string.events_remembrances, R.drawable.ic_events) {
                vm.onCategoryClick(category = 4)
            }
            // emotion recitations button
            MySquareButton(R.string.emotion_remembrances, R.drawable.ic_emotion) {
                vm.onCategoryClick(category = 5)
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // places recitations button
            MySquareButton(R.string.places_remembrances, R.drawable.ic_going_out) {
                vm.onCategoryClick(category = 6)
            }
            // more recitations button
            MySquareButton(R.string.title_more, R.drawable.ic_duaa_moon) {
                vm.onCategoryClick(category = 7)
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