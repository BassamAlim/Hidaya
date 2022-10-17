package bassamalim.hidaya.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bassamalim.hidaya.R
import bassamalim.hidaya.activities.AthkarListActivity
import bassamalim.hidaya.ui.components.MyButton
import bassamalim.hidaya.ui.components.MySquareButton

class AthkarScreen(
    private val context: Context
): NavigationScreen() {

    private fun showThikrs(category: Int) {
        val intent = Intent(context, AthkarListActivity::class.java)
        intent.action = "category"
        intent.putExtra("category", category)
        context.startActivity(intent)
    }

    @Composable
    fun AthkarUI() {
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = 5.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LargeBtn(R.string.all_athkar) {
                val intent = Intent(context, AthkarListActivity::class.java)
                intent.action = "all"
                context.startActivity(intent)
            }

            LargeBtn(R.string.favorite_athkar) {
                val intent = Intent(context, AthkarListActivity::class.java)
                intent.action = "favorite"
                context.startActivity(intent)
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MySquareButton(R.string.day_and_night_thikrs, R.drawable.ic_day_and_night) {
                    showThikrs(0)
                }
                MySquareButton(R.string.prayers_thikrs, R.drawable.ic_praying) {
                    showThikrs(1)
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MySquareButton(R.string.quran_thikrs, R.drawable.ic_closed_quran) {
                    showThikrs(2)
                }
                MySquareButton(R.string.actions_thikrs, R.drawable.ic_actions) {
                    showThikrs(3)
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MySquareButton(R.string.events_thikrs, R.drawable.ic_events) {
                    showThikrs(4)
                }
                MySquareButton(R.string.emotion_thikrs, R.drawable.ic_emotion) {
                    showThikrs(5)
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MySquareButton(R.string.places_thikrs, R.drawable.ic_going_out) {
                    showThikrs(6)
                }
                MySquareButton(R.string.title_more, R.drawable.ic_duaa_moon) {
                    showThikrs(7)
                }
            }
        }
    }

    @Composable
    private fun LargeBtn(textResId: Int, onClick: () -> Unit) {
        MyButton(
            text = stringResource(id = textResId),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp, horizontal = 24.dp),
            fontWeight = FontWeight.Bold,
            onClick = onClick
        )
    }
}