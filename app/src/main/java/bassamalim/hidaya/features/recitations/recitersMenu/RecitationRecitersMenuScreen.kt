package bassamalim.hidaya.features.recitations.recitersMenu

import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.ui.components.CustomSearchBar
import bassamalim.hidaya.core.ui.components.MyDownloadButton
import bassamalim.hidaya.core.ui.components.MyFavoriteButton
import bassamalim.hidaya.core.ui.components.MyIconButton
import bassamalim.hidaya.core.ui.components.MyIconPlayerButton
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MySurface
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.TabLayout
import bassamalim.hidaya.features.quran.surasMenu.RecitationInfo
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

    MyScaffold(
        title = stringResource(R.string.recitations),
        onBack = viewModel::onBackPressed
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
        ) {
            TabLayout(
                pageNames = listOf(
                    stringResource(R.string.all),
                    stringResource(R.string.favorites),
                    stringResource(R.string.downloaded)
                ),
                searchComponent = {
                    Column {
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

                            BadgedBox(
                                badge = {
                                    if (state.isFiltered) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                modifier = Modifier.padding(end = 10.dp)
                            ) {
                                MyIconButton(
                                    imageVector = Icons.Default.FilterAlt,
                                    description = stringResource(R.string.filter_search_description),
                                    iconModifier = Modifier.size(32.dp),
                                    iconColor =
                                        if (state.isFiltered) MaterialTheme.colorScheme.secondary
                                        else MaterialTheme.colorScheme.outline,
                                    onClick = viewModel::onFilterClick
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = state.isFiltered,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    MyText(
                                        text = stringResource(R.string.filtered),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            ) { page ->
                Tab(
                    itemsFlow = viewModel.getItems(page),
                    expandedReciterIds = state.expandedReciterIds,
                    onReciterExpandToggle = viewModel::onReciterExpandToggle,
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
    expandedReciterIds: Set<Int>,
    onReciterExpandToggle: (Int) -> Unit,
    onFavoriteClick: (Int, Boolean) -> Unit,
    onNarrationClick: (Int, Int) -> Unit,
    onDownloadNarrationClick: (Int, Recitation.Narration, String) -> Unit
) {
    val items by itemsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    if (items.isEmpty()) {
        EmptyStateView()
    } else {
        MyLazyColumn(
            lazyList = {
                items(
                    items = items,
                    key = { it.reciterId }
                ) { item ->
                    ReciterCard(
                        reciter = item,
                        isExpanded = expandedReciterIds.contains(item.reciterId),
                        onExpandToggle = { onReciterExpandToggle(item.reciterId) },
                        onFavoriteClick = onFavoriteClick,
                        onNarrationClick = onNarrationClick,
                        onDownloadNarrationClick = onDownloadNarrationClick,
                        modifier = Modifier.animateItem()
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        )
    }
}

@Composable
private fun EmptyStateView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(16.dp))

            MyText(
                text = stringResource(R.string.no_recitations_found),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun ReciterCard(
    reciter: Recitation,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onFavoriteClick: (Int, Boolean) -> Unit,
    onNarrationClick: (Int, Int) -> Unit,
    onDownloadNarrationClick: (Int, Recitation.Narration, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 150),
        label = "expandArrowRotation"
    )

    MySurface(
        modifier = modifier.animateContentSize(
            animationSpec = tween(durationMillis = 150)
        ),
        padding = PaddingValues(vertical = 6.dp, horizontal = 10.dp),
        cornerRadius = 12.dp,
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onExpandToggle)
                    .padding(top = 12.dp, bottom = 12.dp, start = 12.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with initial letter
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        MyText(
                            text = reciter.reciterName.firstOrNull()?.toString() ?: "",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    MyText(
                        text = reciter.reciterName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    MyText(
                        text = "${reciter.narrations.size} ${stringResource(R.string.narrations)}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                MyFavoriteButton(
                    isFavorite = reciter.isFavoriteReciter,
                    onClick = { onFavoriteClick(reciter.reciterId, reciter.isFavoriteReciter) }
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.expand),
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotationState),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(150)) + fadeIn(animationSpec = tween(150)),
                exit = shrinkVertically(animationSpec = tween(150)) + fadeOut(animationSpec = tween(150))
            ) {
                Column(Modifier.fillMaxWidth()) {
                    reciter.narrations.values.forEachIndexed { idx, narration ->
                        NarrationCard(
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
    }
}

@Composable
private fun NarrationCard(
    idx: Int,
    reciterId: Int,
    narration: Recitation.Narration,
    onNarrationClick: (Int, Int) -> Unit,
    onDownloadClick: (Int, Recitation.Narration, String) -> Unit
) {
    val suraString = stringResource(R.string.sura)

    val stripColor = when (narration.downloadState) {
        DownloadState.DOWNLOADED -> MaterialTheme.colorScheme.primary
        DownloadState.DOWNLOADING -> MaterialTheme.colorScheme.tertiary
        DownloadState.NOT_DOWNLOADED -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    val backgroundTint = when (narration.downloadState) {
        DownloadState.DOWNLOADED -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundTint)
            .clickable { onNarrationClick(reciterId, narration.id) }
    ) {
        // Leading color strip
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(64.dp)
                .background(stripColor)
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                MyText(
                    text = narration.name,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Start
                )

                MyText(
                    text = "${narration.availableSuras.size} ${stringResource(R.string.suras)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start
                )
            }

            AnimatedContent(
                targetState = narration.downloadState,
                label = "downloadButtonState",
                transitionSpec = {
                    scaleIn(animationSpec = tween(200)) togetherWith
                            scaleOut(animationSpec = tween(200))
                }
            ) { state ->
                MyDownloadButton(
                    state = state,
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
    AnimatedVisibility(
        visible = recitationInfo != null,
        modifier = Modifier.align(Alignment.BottomCenter),
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300))
    ) {
        if (recitationInfo != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shadowElevation = 8.dp,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Absolute.SpaceBetween
                ) {
                    // Play button area with colored background
                    Surface(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(68.dp),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 12.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 12.dp
                        ),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(onClick = onPlayPauseClick),
                            contentAlignment = Alignment.Center
                        ) {
                            MyIconPlayerButton(
                                state = playbackState,
                                onClick = onPlayPauseClick,
                                iconSize = 36.dp,
                                filled = false,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Track info
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(onClick = onContinueListeningClick)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        MyText(
                            text = "${stringResource(R.string.sura)} ${recitationInfo.suraName}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )

                        MyText(
                            text = "${stringResource(R.string.for_reciter)} ${recitationInfo.reciterName}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Forward arrow indicator
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.continue_listening),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
