package bassamalim.hidaya.features.more

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyColumn
import bassamalim.hidaya.core.ui.components.MyRow
import bassamalim.hidaya.core.ui.components.MySquareButton

@Composable
fun MoreUI(
    vm: MoreVM
) {
    val st by vm.uiState.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    MyColumn(
        Modifier.padding(top = 5.dp),
        scrollable = true
    ) {
        MyRow {
            MySquareButton(
                textResId = R.string.recitations,
                imageResId = R.drawable.ic_headphone
            ) {
                vm.gotoTelawat()
            }

            MySquareButton(
                textResId = R.string.qibla,
                imageResId = R.drawable.ic_qibla_compass
            ) {
                vm.gotoQibla()
            }
        }

        MyRow {
            MySquareButton(
                textResId = R.string.quiz_title,
                imageResId = R.drawable.ic_quiz
            ) {
                vm.gotoQuiz()
            }

            MySquareButton(
                textResId = R.string.hadeeth_books,
                imageResId = R.drawable.ic_books
            ) {
                vm.gotoBooks()
            }
        }

        MyRow {
            MySquareButton(
                textResId = R.string.tv_channels,
                imageResId = R.drawable.ic_television
            ) {
                vm.gotoTV()
            }

            MySquareButton(
                textResId = R.string.quran_radio,
                imageResId = R.drawable.ic_radio
            ) {
                vm.gotoRadio()
            }
        }

        MyRow {
            MySquareButton(
                textResId = R.string.date_converter,
                imageResId = R.drawable.ic_calendar
            ) {
                vm.gotoDateConverter()
            }

            MySquareButton(R.string.settings, R.drawable.ic_settings) {
                vm.gotoSettings()
            }
        }

        MyRow {
            MySquareButton(
                textResId = R.string.contact,
                imageResId = R.drawable.ic_mail
            ) {
                vm.contactMe(ctx)
            }

            MySquareButton(
                textResId = R.string.share_app,
                imageResId = R.drawable.ic_share
            ) {
                vm.shareApp(ctx)
            }
        }

        MyRow {
            MySquareButton(
                textResId = R.string.about,
                imageResId = R.drawable.ic_info
            ) {
                vm.gotoAbout()
            }
        }
    }

    if (st.shouldShowUnsupported)
        UnsupportedFeatureToast(ctx)
}

@Composable
private fun UnsupportedFeatureToast(ctx: Context) {
    LaunchedEffect(null) {
        Toast.makeText(
            ctx,
            ctx.getString(R.string.feature_not_supported),
            Toast.LENGTH_SHORT
        ).show()
    }
}