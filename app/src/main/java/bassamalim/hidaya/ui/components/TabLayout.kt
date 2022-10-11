package bassamalim.hidaya.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import bassamalim.hidaya.ui.theme.AppTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.pagerTabIndicatorOffset
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TabLayout(
    pagerState: PagerState,
    pagesInfo: List<Pair<String, Painter>>,
    extraComponents: @Composable () -> Unit = {},
    tabsContent: @Composable (Int) -> Unit
) {
    Column {
        Tabs(
            pagerState = pagerState,
            pagesInfo = pagesInfo
        )

        extraComponents()

        TabsContent(
            pagerState = pagerState,
            content = tabsContent
        )
    }
}

@ExperimentalPagerApi
@Composable
fun Tabs(
    pagerState: PagerState,
    pagesInfo: List<Pair<String, Painter>>
) {
    // creating a variable for the scope.
    val scope = rememberCoroutineScope()
    // creating a row for our tab layout.
    TabRow(
        // specifying the selected index.
        selectedTabIndex = pagerState.currentPage,
        // on below line we are specifying background color.
        backgroundColor = AppTheme.colors.primary,
        // specifying content color.
        contentColor = Color.White,
        // specifying the indicator for the tab
        indicator = { tabPositions ->
            // specifying the styling for tab indicator by specifying height and color for the tab indicator.
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                height = 2.dp,
                color = AppTheme.colors.secondary
            )
        }
    ) {
        // specifying icon and text for the individual tab item
        pagesInfo.forEachIndexed { index, _ ->
            // creating a tab.
            Tab(
                // specifying icon for each tab item and we are calling image from the list which we have created.
                icon = {
                    Icon(
                        painter = pagesInfo[index].second,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = AppTheme.colors.onPrimary
                    )
                },
                // specifying the text for the each tab item and we are calling data from the list which we have created.
                text = {
                    MyText(
                        text = pagesInfo[index].first,
                        // specifying the text color for the text in that tab
                        textColor =
                            if (pagerState.currentPage == index) Color.White
                            else Color.LightGray
                    )
                },
                // specifying the tab which is selected.
                selected = pagerState.currentPage == index,
                // specifying the on click for the tab which is selected.
                onClick = {
                    // specifying the scope.
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        }
    }
}

// creating a tab content method in which we will be displaying the individual page of our tab.
@ExperimentalPagerApi
@Composable
fun TabsContent(
    pagerState: PagerState,
    content: @Composable (Int) -> Unit
) {
    // creating horizontal pager for our tab layout.
    HorizontalPager(
        state = pagerState,
        verticalAlignment = Alignment.Top
    ) { page ->
        content(page)
    }
}