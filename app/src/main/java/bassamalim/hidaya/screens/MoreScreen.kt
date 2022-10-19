package bassamalim.hidaya.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bassamalim.hidaya.R
import bassamalim.hidaya.activities.*
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.ui.components.MySquareButton

class MoreScreen(
    private val context: Context
): NavigationScreen() {

    @Composable
    fun MoreUI() {
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = 5.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MySquareButton(R.string.recitations, R.drawable.ic_headphone) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        context.startActivity(Intent(context, TelawatActivity::class.java))
                    else
                        Toast.makeText(context, context.getString(R.string.feature_not_supported), Toast.LENGTH_SHORT).show()
                }

                MySquareButton(R.string.qibla, R.drawable.ic_qibla_compass) {
                    context.startActivity(Intent(context, QiblaActivity::class.java))
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MySquareButton(R.string.quiz_title, R.drawable.ic_quiz) {
                    context.startActivity(Intent(context, QuizLobbyActivity::class.java))
                }

                MySquareButton(R.string.hadeeth_books, R.drawable.ic_books) {
                    context.startActivity(Intent(context, BooksActivity::class.java))
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MySquareButton(R.string.tv_channels, R.drawable.ic_television) {
                    context.startActivity(Intent(context, TvActivity::class.java))
                }

                MySquareButton(R.string.quran_radio, R.drawable.ic_radio) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        context.startActivity(Intent(context, RadioClient::class.java))
                    else
                        Toast.makeText(context, context.getString(R.string.feature_not_supported), Toast.LENGTH_SHORT).show()
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MySquareButton(R.string.date_converter, R.drawable.ic_calendar) {
                    context.startActivity(Intent(context, DateConverter::class.java))
                }

                MySquareButton(R.string.settings, R.drawable.ic_settings) {
                    context.startActivity(Intent(context, Settings::class.java))
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MySquareButton(R.string.contact, R.drawable.ic_mail) {
                    val intent = Intent(
                        Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", Global.CONTACT_EMAIL, null
                        )
                    )
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Hidaya")
                    context.startActivity(Intent.createChooser(intent, "Choose an Email client :"))
                }

                MySquareButton(R.string.share_app, R.drawable.ic_share) {
                    val sharingIntent = Intent(Intent.ACTION_SEND)
                    sharingIntent.type = "text/plain"
                    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "App Share")
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, Global.PLAY_STORE_URL)
                    context.startActivity(Intent.createChooser(sharingIntent, "Share via"))
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MySquareButton(R.string.about, R.drawable.ic_info) {
                    context.startActivity(Intent(context, AboutActivity::class.java))
                }
            }
        }
    }

}