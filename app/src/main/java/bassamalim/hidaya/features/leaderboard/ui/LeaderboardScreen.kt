package bassamalim.hidaya.features.leaderboard.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.ui.components.ErrorScreen
import bassamalim.hidaya.core.ui.components.LoadingScreen
import bassamalim.hidaya.core.ui.components.MyColumn
import bassamalim.hidaya.core.ui.components.MyHorizontalDivider
import bassamalim.hidaya.core.ui.components.MyRow
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.PaginatedLazyColumn
import bassamalim.hidaya.core.ui.components.TabLayout
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import kotlinx.collections.immutable.toPersistentList

@Composable
fun LeaderboardScreen(viewModel: LeaderboardViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MyScaffold(title = stringResource(R.string.leaderboard)) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) LoadingScreen()
            else if (state.isError)
                ErrorScreen(message = stringResource(R.string.error_fetching_data))
            else UsersList(
                userId = state.userId,
                userRankMap = state.userRanks,
                ranksMap = state.ranks,
                isLoadingItems = state.isLoadingItems,
                loadMoreItems = viewModel::loadMore,
                numeralsLanguage = viewModel.numeralsLanguage
            )
        }
    }
}

@Composable
private fun UsersList(
    userId: String,
    userRankMap: Map<RankType, String>,
    ranksMap: Map<RankType, List<Pair<String, String>>>,
    isLoadingItems: Map<RankType, Boolean>,
    loadMoreItems: (RankType) -> Unit,
    numeralsLanguage: Language
) {
    TabLayout(
        pageNames = listOf(
            stringResource(R.string.by_reading),
            stringResource(R.string.by_listening)
        )
    ) { page ->
        val rankBy = RankType.entries[page]
        val userRank = userRankMap[rankBy]!!
        val ranks = ranksMap[rankBy]!!

        MyColumn {
            UserRankCard(userId = userId, userRank = userRank)

            MyHorizontalDivider(thickness = 1.dp)

            UsersList(
                items = ranks,
                rankType = rankBy,
                listState = rememberLazyListState(),
                loadMoreItems = { loadMoreItems(rankBy) },
                isLoading = isLoadingItems[rankBy]!!,
                numeralsLanguage = numeralsLanguage
            )
        }
    }
}

@Composable
private fun UserRankCard(userId: String, userRank: String) {
    MyRow(
        Modifier
            .padding(16.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
            .padding(vertical = 12.dp , horizontal = 16.dp),
    ) {
        MyText(
            text = "${stringResource(R.string.user)} $userId",
            fontSize = 20.sp,
            color = when (userRank) {
                "1" -> bassamalim.hidaya.core.ui.theme.Gold
                "2" -> bassamalim.hidaya.core.ui.theme.Silver
                "3" -> bassamalim.hidaya.core.ui.theme.Bronze
                else -> MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.fillMaxWidth(0.4f)
        )

        MyRow(
            modifier = Modifier.fillMaxWidth(0.6f),
            horizontalArrangement = Arrangement.Center
        ) {
            MyText(
                text = stringResource(R.string.your_position),
                color = when (userRank) {
                    "1" -> bassamalim.hidaya.core.ui.theme.Gold
                    "2" -> bassamalim.hidaya.core.ui.theme.Silver
                    "3" -> bassamalim.hidaya.core.ui.theme.Bronze
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            Spacer(Modifier.width(10.dp))

            MyText(
                text = userRank,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = when (userRank) {
                    "1" -> bassamalim.hidaya.core.ui.theme.Gold
                    "2" -> bassamalim.hidaya.core.ui.theme.Silver
                    "3" -> bassamalim.hidaya.core.ui.theme.Bronze
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
private fun UsersList(
    items: List<Pair<String, String>>,
    rankType: RankType,
    listState: LazyListState,
    isLoading: Boolean,
    loadMoreItems: () -> Unit,
    numeralsLanguage: Language
) {
    PaginatedLazyColumn(
        items = items.toPersistentList(),
        loadMoreItems = loadMoreItems,
        listState = listState,
        isLoading = isLoading,
        itemComponent = { index, item -> ItemCard(item, index+1, rankType, numeralsLanguage) }
    )
}

@Composable
private fun ItemCard(
    item: Pair<String, String>,
    rank: Int,
    rankType: RankType,
    numeralsLanguage: Language
) {
    ListItem(
        headlineContent = {
            MyText(
                text = "${stringResource(R.string.user)} ${item.first}",
                fontWeight =
                    if (rank <= 3) FontWeight.Bold
                    else FontWeight.Normal,
                fontSize = 20.sp,
                color = when (rank) {
                    1 -> bassamalim.hidaya.core.ui.theme.Gold
                    2 -> bassamalim.hidaya.core.ui.theme.Silver
                    3 -> bassamalim.hidaya.core.ui.theme.Bronze
                    else -> MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.padding(start = 20.dp)
            )
        },
        modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp),
        leadingContent = {
            MyText(
                text = translateNums("$rank.", numeralsLanguage),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = when (rank) {
                    1 -> bassamalim.hidaya.core.ui.theme.Gold
                    2 -> bassamalim.hidaya.core.ui.theme.Silver
                    3 -> bassamalim.hidaya.core.ui.theme.Bronze
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        },
        trailingContent = {
            MyText(
                text = when (rankType) {
                    RankType.BY_READING -> "${item.second} ${stringResource(R.string.pages)}"
                    RankType.BY_LISTENING -> item.second
                },
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = when (rank) {
                    1 -> bassamalim.hidaya.core.ui.theme.Gold
                    2 -> bassamalim.hidaya.core.ui.theme.Silver
                    3 -> bassamalim.hidaya.core.ui.theme.Bronze
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    )

    HorizontalDivider(thickness = 0.3.dp)
}