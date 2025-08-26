package bassamalim.hidaya.features.remembrances.categoriesMenu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyText

data class CategoryItem(
    val id: Int,
    val titleRes: Int,
    val iconRes: Int? = null,
    val imageVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    val isSpecial: Boolean = false
)

@Composable
fun RemembranceCategoriesScreen(viewModel: RemembranceCategoriesViewModel) {
    val categoryItems = listOf(
        CategoryItem(0, R.string.day_and_night_remembrances, R.drawable.ic_day_and_night),
        CategoryItem(1, R.string.prayers_remembrances, R.drawable.ic_praying),
        CategoryItem(2, R.string.quran_remembrances, R.drawable.ic_quran),
        CategoryItem(3, R.string.actions_remembrances, R.drawable.ic_moving),
        CategoryItem(4, R.string.events_remembrances, R.drawable.ic_events),
        CategoryItem(5, R.string.emotion_remembrances, R.drawable.ic_emotions),
        CategoryItem(6, R.string.places_remembrances,
            imageVector = Icons.AutoMirrored.Default.Logout),
        CategoryItem(7, R.string.title_more, R.drawable.ic_duaa_light_hands)
    )

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        SpecialCategoryCard(
            item = CategoryItem(
                id = -1,
                titleRes = R.string.all_remembrances,
                iconRes = null,
                imageVector = Icons.Default.ViewModule,
                isSpecial = true
            ),
            onClick = viewModel::onAllRemembrancesClick
        )

        SpecialCategoryCard(
            item = CategoryItem(
                id = -2,
                titleRes = R.string.favorite_remembrances,
                iconRes = null,
                imageVector = Icons.Default.Favorite,
                isSpecial = true
            ),
            onClick = viewModel::onFavoriteRemembrancesClick
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(top = 24.dp, bottom = 16.dp, start = 6.dp, end = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categoryItems) { item ->
                CategoryCard(
                    item = item,
                    onClick = { viewModel.onCategoryClick(categoryId = item.id) }
                )
            }
        }
    }
}

@Composable
private fun SpecialCategoryCard(item: CategoryItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(top = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.iconRes != null) {
                    Icon(
                        painter = painterResource(item.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                else if (item.imageVector != null) {
                    Icon(
                        imageVector = item.imageVector,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                MyText(
                    text = stringResource(item.titleRes),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(item: CategoryItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.aspectRatio(1f),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (item.iconRes != null) {
                        Icon(
                            painter = painterResource(item.iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    else if (item.imageVector != null) {
                        Icon(
                            imageVector = item.imageVector,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            MyText(
                text = stringResource(item.titleRes),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
        }
    }
}
