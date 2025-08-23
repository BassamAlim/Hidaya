package bassamalim.hidaya.features.recitations.recitersMenu.ui

import android.os.Build
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.compose.LocalActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.CustomSearchBar
import bassamalim.hidaya.core.ui.components.MyDownloadButton
import bassamalim.hidaya.core.ui.components.MyFavoriteButton
import bassamalim.hidaya.core.ui.components.MyHorizontalDivider
import bassamalim.hidaya.core.ui.components.MyIconButton
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.MyTopBar
import bassamalim.hidaya.core.ui.components.TabLayout
import bassamalim.hidaya.features.quran.surasMenu.ui.RecitationInfo
import bassamalim.hidaya.features.recitations.recitersMenu.domain.Recitation
import kotlinx.coroutines.flow.Flow

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecitationRecitersMenuScreen(viewModel: RecitationRecitersMenuViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current!!

    if (state.isLoading) return

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart(activity)
        onDispose(viewModel::onStop)
    }

    Scaffold(
        topBar = {
            MyTopBar(
                title = stringResource(R.string.recitations),
                onBack = viewModel::onBackPressed
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
        ) {
            TabLayout(
                pageNames = listOf(
                    stringResource(R.string.all),
                    stringResource(R.string.favorite),
                    stringResource(R.string.downloaded)
                ),
                searchComponent = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CustomSearchBar(
                            query = viewModel.searchText,
                            hint = stringResource(R.string.reciters_search_hint),
                            modifier = Modifier.weight(1F),
                            onQueryChange = viewModel::onSearchTextChange
                        )

                        MyIconButton(
                            imageVector = Icons.Default.FilterAlt,
                            modifier = Modifier.padding(end = 10.dp),
                            description = stringResource(R.string.filter_search_description),
                            iconModifier = Modifier.size(32.dp),
                            iconColor =
                                if (state.isFiltered) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.outline,
                            onClick = viewModel::onFilterClick
                        )
                    }
                }
            ) { page ->
                Tab(
                    itemsFlow = viewModel.getItems(page),
                    onFavoriteClick = viewModel::onFavoriteClick,
                    onNarrationClick = viewModel::onNarrationClick,
                    onDownloadNarrationClick = viewModel::onDownloadNarrationClick
                )
            }

            PlaybackBar(
                recitationInfo = state.playbackRecitationInfo,
                playbackState = state.playbackState,
                onContinueListeningClick = viewModel::onContinueListeningClick,
                onPlayPauseClick = viewModel::onPlayPauseClick
            )
        }
    }
}

@Composable
private fun Tab(
    itemsFlow: Flow<List<Recitation>>,
    onFavoriteClick: (Int, Boolean) -> Unit,
    onNarrationClick: (Int, Int) -> Unit,
    onDownloadNarrationClick: (Int, Recitation.Narration, String) -> Unit
) {
    val items by itemsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    MyLazyColumn(
        lazyList = {
            items(items) { item ->
                ReciterCard(
                    reciter = item,
                    onFavoriteClick = onFavoriteClick,
                    onNarrationClick = onNarrationClick,
                    onDownloadNarrationClick = onDownloadNarrationClick
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    )
}

@Composable
private fun ReciterCard(
    reciter: Recitation,
    onFavoriteClick: (Int, Boolean) -> Unit,
    onNarrationClick: (Int, Int) -> Unit,
    onDownloadNarrationClick: (Int, Recitation.Narration, String) -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                RoundedCornerShape(10.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 6.dp, start = 16.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MyText(
                text = reciter.reciterName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            MyFavoriteButton(
                isFavorite = reciter.isFavoriteReciter,
                onClick = { onFavoriteClick(reciter.reciterId, reciter.isFavoriteReciter) }
            )
        }

        MyHorizontalDivider(padding = PaddingValues(top = 5.dp))

        Column(Modifier.fillMaxWidth()) {
            reciter.narrations.values.forEachIndexed { idx, narration ->
                NarrationsCard(
                    idx = idx,
                    reciterId = reciter.reciterId,
                    narration = narration,
                    onNarrationClick = onNarrationClick,
                    onDownloadClick = onDownloadNarrationClick
                )
            }
        }
    }
}

@Composable
private fun NarrationsCard(
    idx: Int,
    reciterId: Int,
    narration: Recitation.Narration,
    onNarrationClick: (Int, Int) -> Unit,
    onDownloadClick: (Int, Recitation.Narration, String) -> Unit
) {
    val suraString = stringResource(R.string.sura)

    if (idx != 0)
        MyHorizontalDivider(padding = PaddingValues(0.dp))

    Box(
        Modifier
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable {
                onNarrationClick(reciterId, narration.id)
            }
    ) {
        Box(
            Modifier.padding(start = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MyText(
                    text = narration.name,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(1F)
                )

                MyDownloadButton(
                    state = narration.downloadState,
                    iconSize = 28.dp,
                    onClick = { onDownloadClick(reciterId, narration, suraString) }
                )
            }
        }
    }
}

@Composable
private fun BoxScope.PlaybackBar(
    recitationInfo: RecitationInfo?,
    playbackState: Int,
    onContinueListeningClick: () -> Unit,
    onPlayPauseClick: () -> Unit
) {
    if (recitationInfo == null) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
            .align(Alignment.BottomCenter)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 10.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 10.dp
                ))
                .clickable(onClick = onPlayPauseClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector =
                    if (playbackState == PlaybackStateCompat.STATE_PLAYING) Icons.Default.Pause
                    else Icons.Default.PlayArrow,
                contentDescription = stringResource(R.string.play_pause_btn_description),
            )
        }

        Column(
            Modifier
                .fillMaxHeight()
                .weight(1f)
                .clip(RoundedCornerShape(
                    topStart = 10.dp,
                    topEnd = 0.dp,
                    bottomStart = 10.dp,
                    bottomEnd = 0.dp
                ))
                .clickable(onClick = onContinueListeningClick)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            MyText(
                text = "${stringResource(R.string.sura)} ${recitationInfo.suraName}",
                fontWeight = FontWeight.SemiBold
            )

            MyText(
                text = "${stringResource(R.string.for_reciter)} ${recitationInfo.reciterName}",
                fontSize = 16.sp
            )
        }
    }
}