package bassamalim.hidaya.features.remembrances.categoriesMenu.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyText
import kotlinx.coroutines.delay

data class CategoryItem(
    val id: Int,
    val titleRes: Int,
    val iconRes: Int? = null,
    val imageVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    val isSpecial: Boolean = false
)

@Composable
fun RemembranceCategoriesScreen(viewModel: RemembranceCategoriesViewModel) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val specialItems = listOf(
        CategoryItem(
            id = -1,
            titleRes = R.string.all_remembrances,
            iconRes = null,
            imageVector = Icons.Default.ViewModule,
            isSpecial = true
        ),
        CategoryItem(
            id = -2,
            titleRes = R.string.favorite_remembrances,
            iconRes = null,
            imageVector = Icons.Default.Favorite,
            isSpecial = true
        )
    )

    val categoryItems = listOf(
        CategoryItem(0, R.string.day_and_night_remembrances, R.drawable.ic_day_and_night),
        CategoryItem(1, R.string.prayers_remembrances, R.drawable.ic_praying),
        CategoryItem(2, R.string.quran_remembrances, R.drawable.ic_quran),
        CategoryItem(3, R.string.actions_remembrances, R.drawable.ic_moving),
        CategoryItem(4, R.string.events_remembrances, R.drawable.ic_events),
        CategoryItem(5, R.string.emotion_remembrances, R.drawable.ic_emotions),
        CategoryItem(6, R.string.places_remembrances, imageVector = Icons.AutoMirrored.Default.Logout),
        CategoryItem(7, R.string.title_more, R.drawable.ic_duaa_light_hands)
    )

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                specialItems.forEach { item ->
                    SpecialCategoryCard(
                        item = item,
                        onClick = {
                            when (item.id) {
                                -1 -> viewModel.onAllRemembrancesClick()
                                -2 -> viewModel.onFavoriteRemembrancesClick()
                            }
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 300, delayMillis = 200)) + 
                    scaleIn(
                        initialScale = 0.9f,
                        animationSpec = tween(durationMillis = 300, delayMillis = 200)
                    ),
            exit = fadeOut() + scaleOut(targetScale = 0.9f)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(4.dp),
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
}

@Composable
private fun SpecialCategoryCard(item: CategoryItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
    var isPressed by remember { mutableStateOf(false) }
    
    Card(
        onClick = { 
            isPressed = true
            onClick()
        },
        modifier = Modifier.size(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPressed) MaterialTheme.colorScheme.surfaceVariant 
                           else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp,
            pressedElevation = 1.dp
        )
    ) {
        LaunchedEffect(isPressed) {
            if (isPressed) {
                delay(150)
                isPressed = false
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (item.iconRes != null) {
                Icon(
                    painter = painterResource(item.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            else if (item.imageVector != null) {
                Icon(
                    imageVector = item.imageVector,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            MyText(
                text = stringResource(item.titleRes),
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )
        }
    }
}