package bassamalim.hidaya.features.about.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyHorizontalDivider
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MySquareButton
import bassamalim.hidaya.core.ui.components.MyText

@Composable
fun AboutScreen(viewModel: AboutViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MyScaffold(title = stringResource(R.string.about)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(it)
                .padding(horizontal = 5.dp)
        ) {
            ThankYouMessage(
                onTitleClick = viewModel::onTitleClick
            )

            SourcesList()

            HiddenArea(
                isDevModeOn = state.isDevModeEnabled,
                lastDailyUpdate = state.lastDailyUpdate,
                onRebuildDatabaseClick = viewModel::onRebuildDatabaseClick
            )
        }
    }

    // show a toast when the database is rebuilt
    if (state.shouldShowRebuilt != 0) {
        DatabaseRebuiltToast(
            shouldShowRebuilt = state.shouldShowRebuilt
        )
    }
}

@Composable
private fun ColumnScope.ThankYouMessage(
    onTitleClick: () -> Unit
) {
    MyText(
        text = stringResource(R.string.thanks),
        fontSize = 25.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(top = 15.dp, bottom = 20.dp)
            .align(Alignment.CenterHorizontally)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onTitleClick
            )
    )
}

@Composable
private fun ColumnScope.SourcesList() {
    Column(
        Modifier
            .weight(1F)
            .verticalScroll(rememberScrollState())
    ) {
        Source(R.string.quran_source)
        MyHorizontalDivider()
        Source(R.string.tafseer_source)
        MyHorizontalDivider()
        Source(R.string.hadeeth_source)
        MyHorizontalDivider()
        Source(R.string.athkar_source)
        MyHorizontalDivider()
        Source(R.string.quiz_source)
    }
}

@Composable
private fun Source(textResId: Int) {
    MyText(
        text = stringResource(textResId),
        modifier = Modifier.padding(10.dp),
        fontSize = 22.sp,
        textAlign = TextAlign.Start
    )
}

@Composable
private fun ColumnScope.HiddenArea(
    isDevModeOn: Boolean,
    lastDailyUpdate: String,
    onRebuildDatabaseClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = isDevModeOn,
        enter = expandVertically()
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // rebuild database button
            MySquareButton(
                text = stringResource(R.string.rebuild_database),
                Modifier.align(Alignment.CenterHorizontally),
                onClick = onRebuildDatabaseClick
            )

            // last daily update text
            MyText(
                lastDailyUpdate,
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(10.dp)
            )
        }
    }
}

@Composable
private fun DatabaseRebuiltToast(
    shouldShowRebuilt: Int
) {
    val ctx = LocalContext.current
    LaunchedEffect(key1 = shouldShowRebuilt) {
        Toast.makeText(
            ctx,
            ctx.getString(R.string.database_rebuilt),
            Toast.LENGTH_SHORT
        ).show()
    }
}