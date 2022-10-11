package bassamalim.hidaya.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.ui.components.MyButton
import bassamalim.hidaya.ui.components.MyScaffold
import bassamalim.hidaya.ui.components.MyText
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.ui.theme.Grey
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.DBUtils
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

class AboutActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.onActivityCreateSetLocale(this)

        setContent {
            AppTheme {
                UI()
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    private fun UI() {
        MyScaffold(
            title = stringResource(id = R.string.about),
            onBackPressed = { onBackPressedDispatcher.onBackPressed() }
        ) {
            val context = LocalContext.current
            var counter by remember { mutableStateOf(0) }

            Column(
                Modifier
                    .background(AppTheme.colors.background)
                    .padding(it)
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                MyText(
                    text = stringResource(id = R.string.thanks),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 15.dp, bottom = 30.dp)
                        .align(Alignment.CenterHorizontally)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            counter++
                            if (counter == 5)
                                Toast
                                    .makeText(
                                        context, getString(R.string.vip_welcome), Toast.LENGTH_SHORT
                                    )
                                    .show()
                        }
                )

                Source(
                    stringResource(id = R.string.quran_source)
                )
                Divider()
                Source(
                    stringResource(id = R.string.tafseer_source)
                )
                Divider()
                Source(
                    stringResource(id = R.string.hadeeth_source)
                )
                Divider()
                Source(
                    stringResource(id = R.string.athkar_source)
                )
                Divider()
                Source(
                    stringResource(id = R.string.quiz_source)
                )

                val shownModifier = Modifier.align(Alignment.CenterHorizontally)
                val hiddenModifier = shownModifier.alpha(0F)
                MyButton(
                    stringResource(id = R.string.rebuild_database),
                    if (counter < 5) hiddenModifier else shownModifier
                ) {
                    context.deleteDatabase("HidayaDB")

                    Log.i(Global.TAG, "Database Rebuilt")

                    DBUtils.reviveDB(context)

                    Toast.makeText(
                        context, context.getString(R.string.database_rebuilt),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                MyButton(
                    stringResource(id = R.string.quick_update),
                    if (counter < 5) hiddenModifier else shownModifier
                ) {
                    val url = FirebaseRemoteConfig.getInstance().getString(Global.UPDATE_URL)
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(url)
                    context.startActivity(i)
                }
                MyText(
                    text =
                        if (counter < 5) ""
                        else PreferenceManager.getDefaultSharedPreferences(context)
                            .getString("last_daily_update", "No daily updates yet")!!,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(10.dp)
                )
            }
        }
    }

    @Composable
    private fun Source(text: String) {
        MyText(
            text = text,
            modifier = Modifier
                .padding(10.dp),
            fontSize = 22.sp,
            textAlign = TextAlign.Start
        )
    }

    @Composable
    private fun Divider() {
        Divider(
            thickness = 1.dp,
            color = Grey,
            modifier = Modifier.padding(vertical = 5.dp)
        )
    }

}

