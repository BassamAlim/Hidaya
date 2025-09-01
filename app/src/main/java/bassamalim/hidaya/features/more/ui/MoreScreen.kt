package bassamalim.hidaya.features.more.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.theme.nsp

@Composable
fun MoreScreen(viewModel: MoreViewModel, snackBarHostState: SnackbarHostState) {
    val context = LocalContext.current
    val unsupportedMessage = stringResource(R.string.feature_not_supported)

    val featuresItems = listOf(
        FeatureItem(
            title = stringResource(R.string.recitations),
            icon = Icons.Default.Headphones,
            onClick = { viewModel.onRecitationsClick(snackBarHostState, unsupportedMessage) },
            color = Color(0xFF6B73FF)
        ),
        FeatureItem(
            title = stringResource(R.string.qibla),
            drawableId = R.drawable.ic_qibla_compass,
            onClick = viewModel::onQiblaClick,
            color = Color(0xFF00B894)
        ),
        FeatureItem(
            title = stringResource(R.string.quiz_title),
            icon = Icons.AutoMirrored.Default.FactCheck,
            onClick = viewModel::onQuizClick,
            color = Color(0xFFE17055)
        ),
        FeatureItem(
            title = stringResource(R.string.hadeeth_books),
            drawableId = R.drawable.ic_books,
            onClick = viewModel::onBooksClick,
            color = Color(0xFF6C5CE7)
        ),
        FeatureItem(
            title = stringResource(R.string.tv_channels),
            icon = Icons.Default.LiveTv,
            onClick = viewModel::onTvClick,
            color = Color(0xFFFF7675)
        ),
        FeatureItem(
            title = stringResource(R.string.quran_radio),
            icon = Icons.Default.Radio,
            onClick = { viewModel.onRadioClick(snackBarHostState, unsupportedMessage) },
            color = Color(0xFF74B9FF)
        ),
        FeatureItem(
            title = stringResource(R.string.misbaha),
            drawableId = R.drawable.ic_prayer_beads,
            onClick = viewModel::onMisbahaClick,
            color = Color(0xFF00CEC9)
        ),
        FeatureItem(
            title = stringResource(R.string.date_converter),
            icon = Icons.Default.CalendarMonth,
            onClick = viewModel::onDateConverterClick,
            color = Color(0xFFE84393)
        )
    )

    val settingsItems = listOf(
        FeatureItem(
            title = stringResource(R.string.settings),
            icon = Icons.Default.Settings,
            onClick = viewModel::onSettingsClick,
            color = Color(0xFF636E72)
        ),
        FeatureItem(
            title = stringResource(R.string.contact),
            icon = Icons.Default.Mail,
            onClick = { viewModel.onContactClick(context) },
            color = Color(0xFF00B894)
        ),
        FeatureItem(
            title = stringResource(R.string.share_app),
            icon = Icons.Default.Share,
            onClick = { viewModel.onShareClick(context) },
            color = Color(0xFF0984E3)
        ),
        FeatureItem(
            title = stringResource(R.string.about),
            icon = Icons.Default.Info,
            onClick = viewModel::onAboutClick,
            color = Color(0xFFA29BFE)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        FeatureSection(title = stringResource(R.string.features), items = featuresItems)
        
        FeatureSection(title = stringResource(R.string.app_and_support), items = settingsItems)
        
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun FeatureSection(title: String, items: List<FeatureItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        MyText(
            text = title,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start
        )
        
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { item ->
                    FeatureCard(
                        item = item,
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(2 - rowItems.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(item: FeatureItem, modifier: Modifier = Modifier) {
    Card(
        onClick = item.onClick,
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp, pressedElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            (item.color ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.05f),
                            (item.color ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.15f)
                        )
                    )
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            color = (item.color ?: MaterialTheme.colorScheme.primary)
                                .copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        item.icon != null -> {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(24.dp),
                                tint = item.color ?: MaterialTheme.colorScheme.primary
                            )
                        }
                        item.drawableId != null -> {
                            Icon(
                                painter = painterResource(item.drawableId),
                                contentDescription = item.title,
                                modifier = Modifier.size(24.dp),
                                tint = item.color ?: MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                MyText(
                    text = item.title,
                    fontSize = 14.nsp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}