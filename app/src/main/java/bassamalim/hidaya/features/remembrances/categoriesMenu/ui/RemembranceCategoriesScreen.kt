package bassamalim.hidaya.features.remembrances.categoriesMenu.ui

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MySquareButton

@Composable
fun RemembranceCategoriesScreen(
    viewModel: RemembranceCategoriesViewModel
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
        LargeButton(
            text = stringResource(R.string.all_remembrances),
            onClick = viewModel::onAllRemembrancesClick
        )

        // favorite recitations button
        LargeButton(
            text = stringResource(R.string.favorite_remembrances),
            onClick = viewModel::onFavoriteRemembrancesClick
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // day and night recitations button
            MySquareButton(
                text = stringResource(R.string.day_and_night_remembrances),
                imagePainter = painterResource(R.drawable.ic_day_and_night),
                onClick = { viewModel.onCategoryClick(category = 0) }
            )

            // prayer recitations button
            MySquareButton(
                text = stringResource(R.string.prayers_remembrances),
                imagePainter = painterResource(R.drawable.ic_praying),
                onClick = { viewModel.onCategoryClick(category = 1) }
            )
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // quran recitations button
            MySquareButton(
                text = stringResource(R.string.quran_remembrances),
                imagePainter = painterResource(R.drawable.ic_closed_quran),
                onClick = { viewModel.onCategoryClick(category = 2) }
            )
            // actions recitations button
            MySquareButton(
                text = stringResource(R.string.actions_remembrances),
                imagePainter = painterResource(R.drawable.ic_actions),
                onClick = { viewModel.onCategoryClick(category = 3) }
            )
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // events recitations button
            MySquareButton(
                text = stringResource(R.string.events_remembrances),
                imagePainter = painterResource(R.drawable.ic_events),
                onClick = { viewModel.onCategoryClick(category = 4) }
            )
            // emotion recitations button
            MySquareButton(
                text = stringResource(R.string.emotion_remembrances),
                imagePainter = painterResource(R.drawable.ic_emotion),
                onClick = { viewModel.onCategoryClick(category = 5) }
            )
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // places recitations button
            MySquareButton(
                text = stringResource(R.string.places_remembrances),
                imagePainter = painterResource(R.drawable.ic_going_out),
                onClick = { viewModel.onCategoryClick(category = 6) }
            )
            // more recitations button
            MySquareButton(
                text = stringResource(R.string.title_more),
                imagePainter = painterResource(R.drawable.ic_duaa_moon),
                onClick = { viewModel.onCategoryClick(category = 7) }
            )
        }
    }
}

@Composable
private fun LargeButton(text: String, onClick: () -> Unit) {
    MySquareButton(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 24.dp),
        fontWeight = FontWeight.Bold,
        onClick = onClick
    )
}