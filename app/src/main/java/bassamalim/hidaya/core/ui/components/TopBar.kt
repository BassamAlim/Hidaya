package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopBar(
    title: String = "",
    onBack: (() -> Unit)? = null
) {
    CenterAlignedTopAppBar(
        title = {
            MyText(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                textColor = MaterialTheme.colorScheme.onSurface
            )
        },
        modifier = Modifier.fillMaxWidth(),
        navigationIcon = {
            MyBackButton(onBack)
        }
    )
}