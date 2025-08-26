package bassamalim.hidaya.features.misbaha.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyFilledTonalIconButton
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MyText
import kotlin.math.min

@Composable
fun MisbahaScreen(viewModel: MisbahaViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MyScaffold(title = stringResource(R.string.misbaha)) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MyText(
                text = state.countText.toString(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 64.dp),
                fontSize = 96.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                MyFilledTonalIconButton(
                    imageVector = Icons.Default.Replay,
                    description = stringResource(R.string.reset),
                    onClick = viewModel::onResetClick,
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.CenterStart)
                )
            }

            val configuration = LocalConfiguration.current
            val buttonSize = min(
                configuration.screenWidthDp.dp.value,
                configuration.screenHeightDp.dp.value
            ) * 0.75f

            Box(Modifier.fillMaxWidth()) {
                MyFilledTonalIconButton(
                    imageVector = Icons.Default.Add,
                    description = stringResource(R.string.add),
                    onClick = viewModel::onIncrementClick,
                    modifier = Modifier
                        .size(buttonSize.dp)
                        .align(Alignment.Center),
                    iconModifier = Modifier.size(buttonSize.dp * 0.4f)
                )
            }
        }
    }
}