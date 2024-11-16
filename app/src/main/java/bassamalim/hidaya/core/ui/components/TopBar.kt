package bassamalim.hidaya.core.ui.components

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopBar(title: String = "", onBack: (() -> Unit)? = null) {
    CenterAlignedTopAppBar(
        title = {
            MyText(
                text = title,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            MyBackButton(onBack)
        }
    )
}