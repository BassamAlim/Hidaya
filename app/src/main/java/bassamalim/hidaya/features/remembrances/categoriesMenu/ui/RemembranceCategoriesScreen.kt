package bassamalim.hidaya.features.remembrances.categoriesMenu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MySquareButton

@Composable
fun RemembranceCategoriesScreen(viewModel: RemembranceCategoriesViewModel) {
    Column(
        modifier = Modifier
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
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // day and night recitations button
            MySquareButton(
                text = stringResource(R.string.day_and_night_remembrances),
                drawableId = R.drawable.ic_day_and_night,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = { viewModel.onCategoryClick(categoryId = 0) }
            )

            // prayer recitations button
            MySquareButton(
                text = stringResource(R.string.prayers_remembrances),
                drawableId = R.drawable.ic_praying,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = { viewModel.onCategoryClick(categoryId = 1) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // quran recitations button
            MySquareButton(
                text = stringResource(R.string.quran_remembrances),
                drawableId = R.drawable.ic_quran,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = { viewModel.onCategoryClick(categoryId = 2) }
            )
            // actions recitations button
            MySquareButton(
                text = stringResource(R.string.actions_remembrances),
                drawableId = R.drawable.ic_moving,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = { viewModel.onCategoryClick(categoryId = 3) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // events recitations button
            MySquareButton(
                text = stringResource(R.string.events_remembrances),
                drawableId = R.drawable.ic_events,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = { viewModel.onCategoryClick(categoryId = 4) }
            )
            // emotion recitations button
            MySquareButton(
                text = stringResource(R.string.emotion_remembrances),
                drawableId = R.drawable.ic_emotions,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = { viewModel.onCategoryClick(categoryId = 5) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // places recitations button
            MySquareButton(
                text = stringResource(R.string.places_remembrances),
                imageVector = Icons.AutoMirrored.Default.Logout,
                tint = MaterialTheme.colorScheme.onSurface,
                iconSize = 70.dp,
                onClick = { viewModel.onCategoryClick(categoryId = 6) }
            )
            // more recitations button
            MySquareButton(
                text = stringResource(R.string.title_more),
                drawableId = R.drawable.ic_duaa_light_hands,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = { viewModel.onCategoryClick(categoryId = 7) }
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