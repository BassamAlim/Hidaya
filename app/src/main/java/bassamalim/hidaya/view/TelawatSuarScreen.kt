package bassamalim.hidaya.view

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import bassamalim.hidaya.R
import bassamalim.hidaya.enum.DownloadState
import bassamalim.hidaya.models.ReciterSura
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.viewmodel.TelawatSuarVM
import java.util.*

@Composable
fun TelawatSuarUI(
    navController: NavController = rememberNavController(),
    viewModel: TelawatSuarVM = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    MyScaffold(
        state.title,
        onBack = { onBack() }
    ) {
        val textState = remember { mutableStateOf(TextFieldValue("")) }
        TabLayout(
            pageNames = listOf(getString(R.string.all), getString(R.string.favorite), getString(
                R.string.downloaded)),
            searchComponent = {
                SearchComponent(
                    value = textState,
                    hint = stringResource(R.string.search),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        ) { page ->
            Tab(items = getItems(ActivityUtils.getListType(page)), textState)
        }
    }
}

@Composable
private fun Tab(
    items: List<ReciterSura>,
    textState: MutableState<TextFieldValue>
) {
    MyLazyColumn(
        lazyList = {
            items(
                items = items.filter { item ->
                    item.searchName.contains(textState.value.text, ignoreCase = true)
                }
            ) { item ->
                SuraCard(sura = item)
            }
        }
    )
}

@Composable
private fun SuraCard(sura: ReciterSura) {
    MyClickableSurface(
        onClick = {
            val intent = Intent(this, TelawatClient::class.java)
            intent.action = "start"
            val rId = String.format(Locale.US, "%03d", reciterId)
            val vId = String.format(Locale.US, "%02d", versionId)
            val sId = String.format(Locale.US, "%03d", sura.num)
            val mediaId = rId + vId + sId
            intent.putExtra("media_id", mediaId)
            startActivity(intent)
        }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 10.dp, start = 20.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            MyDownloadBtn(
                state = downloadStates[sura.num],
                path = "$prefix${sura.num}.mp3",
                size = 28.dp,
                deleted = {
                    downloadStates[sura.num] = DownloadState.NotDownloaded
                }
            ) {
                download(sura)
            }

            MyText(
                text = sura.surahName,
                modifier = Modifier
                    .weight(1F)
                    .padding(10.dp)
            )

            MyFavBtn(favs[sura.num]) {
                if (favs[sura.num] == 0) {
                    favs[sura.num] = 1
                    db.suarDao().setFav(sura.num, 1)
                }
                else if (favs[sura.num] == 1) {
                    favs[sura.num] = 0
                    db.suarDao().setFav(sura.num, 0)
                }
                updateFavorites()
            }
        }
    }
}