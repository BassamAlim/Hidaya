package bassamalim.hidaya.features.about

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
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
fun AboutUI(
    vm: AboutVM
) {
    val st by vm.uiState.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    MyScaffold(stringResource(R.string.about)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(it)
                .padding(horizontal = 5.dp)
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
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        vm.onTitleClick()
                    }
            )

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

            AnimatedVisibility(
                visible = st.isDevModeOn,
                enter = expandVertically()
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MySquareButton(
                        stringResource(R.string.rebuild_database),
                        Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        vm.rebuildDatabase()
                    }

                    /*MyButton(
                        stringResource(R.string.quick_update),
                        Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        vm.quickUpdate()
                    }*/

                    MyText(
                        st.lastDailyUpdate,
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(10.dp)
                    )
                }
            }
        }
    }

    if (st.shouldShowRebuilt != 0) {
        LaunchedEffect(key1 = st.shouldShowRebuilt) {
            Toast.makeText(
                ctx, ctx.getString(R.string.database_rebuilt),
                Toast.LENGTH_SHORT
            ).show()
        }
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
