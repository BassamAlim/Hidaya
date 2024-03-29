package bassamalim.hidaya.features.leaderboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.ErrorScreen
import bassamalim.hidaya.core.ui.components.LoadingScreen
import bassamalim.hidaya.core.ui.components.MyColumn
import bassamalim.hidaya.core.ui.components.MyHorizontalDivider
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyRow
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MySurface
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.TabLayout
import bassamalim.hidaya.core.ui.theme.AppTheme

@Composable
fun LeaderboardUI(
    vm: LeaderboardVM
) {
    val st by vm.uiState.collectAsStateWithLifecycle()

    MyScaffold(stringResource(R.string.leaderboard)) {
        if (st.loading) LoadingScreen()
        else if (st.errorMessage.isNotEmpty()) ErrorScreen(st.errorMessage)
        else {
            TabLayout(
                pageNames = listOf(
                    stringResource(R.string.by_reading),
                    stringResource(R.string.by_listening)
                )
            ) { page ->
                val rankBy = RankType.entries[page]
                val items = vm.getSortedItems(rankBy)

                MyColumn {
                    UserPositionCard(
                        userId = st.userId,
                        userPosition = vm.getUserPosition(items)
                    )

                    MyHorizontalDivider(thickness = 1.dp)

                    UsersList(
                        items = items,
                        rankType = rankBy
                    )
                }
            }
        }
    }
}

@Composable
fun UserPositionCard(
    userId: String,
    userPosition: String
) {
    MySurface(
        Modifier.padding(top = 6.dp, bottom = 2.dp),
        cornerRadius = 15.dp
    ) {
        MyRow(
            modifier = Modifier.padding(16.dp)
        ) {
            MyText(
                text = userId,
                fontSize = 20.sp,
                textColor = when (userPosition) {
                    "1" -> bassamalim.hidaya.core.ui.theme.Gold
                    "2" -> bassamalim.hidaya.core.ui.theme.Silver
                    "3" -> bassamalim.hidaya.core.ui.theme.Bronze
                    else -> AppTheme.colors.text
                },
                modifier = Modifier.fillMaxWidth(0.4f)
            )

            MyRow(
                modifier = Modifier.fillMaxWidth(0.6f),
                horizontalArrangement = Arrangement.Center
            ) {
                MyText(
                    stringResource(R.string.your_position),
                    textColor = when (userPosition) {
                        "1" -> bassamalim.hidaya.core.ui.theme.Gold
                        "2" -> bassamalim.hidaya.core.ui.theme.Silver
                        "3" -> bassamalim.hidaya.core.ui.theme.Bronze
                        else -> AppTheme.colors.text
                    }
                )

                Spacer(Modifier.width(10.dp))

                MyText(
                    text = userPosition,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textColor = when (userPosition) {
                        "1" -> bassamalim.hidaya.core.ui.theme.Gold
                        "2" -> bassamalim.hidaya.core.ui.theme.Silver
                        "3" -> bassamalim.hidaya.core.ui.theme.Bronze
                        else -> AppTheme.colors.text
                    }
                )
            }
        }
    }
}

@Composable
fun UsersList(
    items: List<LeaderboardItem>,
    rankType: RankType
) {
    MyLazyColumn(lazyList = {
        itemsIndexed(items) { idx, item ->
            ItemCard(
                item = item,
                rank = idx + 1,
                rankType = rankType
            )
        }
    })
}

@Composable
fun ItemCard(
    item: LeaderboardItem,
    rank: Int,
    rankType: RankType
) {
    MySurface(
        Modifier.heightIn(min = 80.dp)
    ) {
        MyRow {
            MyText(
                text = "$rank.",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textColor = when (rank) {
                    1 -> bassamalim.hidaya.core.ui.theme.Gold
                    2 -> bassamalim.hidaya.core.ui.theme.Silver
                    3 -> bassamalim.hidaya.core.ui.theme.Bronze
                    else -> AppTheme.colors.text
                },
                modifier = Modifier.fillMaxWidth(0.2f)
            )

            MyText(
                text = item.userId,
                fontWeight =
                    if (rank <= 3) FontWeight.Bold
                    else FontWeight.Normal,
                fontSize = 20.sp,
                textColor = when (rank) {
                    1 -> bassamalim.hidaya.core.ui.theme.Gold
                    2 -> bassamalim.hidaya.core.ui.theme.Silver
                    3 -> bassamalim.hidaya.core.ui.theme.Bronze
                    else -> AppTheme.colors.text
                },
                modifier = Modifier.fillMaxWidth(0.4f)
            )

            MyText(
                text = when (rankType) {
                    RankType.BY_READING -> "${item.readingRecord} ${stringResource(R.string.pages)}"
                    RankType.BY_LISTENING -> item.listeningRecord
                },
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textColor = when (rank) {
                    1 -> bassamalim.hidaya.core.ui.theme.Gold
                    2 -> bassamalim.hidaya.core.ui.theme.Silver
                    3 -> bassamalim.hidaya.core.ui.theme.Bronze
                    else -> AppTheme.colors.text
                },
                modifier = Modifier.fillMaxWidth(0.4f)
            )
        }
    }
}