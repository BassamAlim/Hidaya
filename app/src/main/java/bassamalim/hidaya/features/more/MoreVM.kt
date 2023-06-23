package bassamalim.hidaya.features.more

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.features.destinations.AboutUIDestination
import bassamalim.hidaya.features.destinations.BooksUIDestination
import bassamalim.hidaya.features.destinations.DateConverterUIDestination
import bassamalim.hidaya.features.destinations.QiblaUIDestination
import bassamalim.hidaya.features.destinations.QuizLobbyUIDestination
import bassamalim.hidaya.features.destinations.RadioClientUIDestination
import bassamalim.hidaya.features.destinations.SettingsUIDestination
import bassamalim.hidaya.features.destinations.TelawatUIDestination
import bassamalim.hidaya.features.destinations.TvUIDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MoreVM @Inject constructor(): ViewModel() {

    private val _uiState = MutableStateFlow(MoreState())
    val uiState = _uiState.asStateFlow()

    fun gotoTelawat(navigator: DestinationsNavigator) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            navigator.navigate(TelawatUIDestination)
        }
        else {
            _uiState.update { it.copy(
                shouldShowUnsupported = true
            )}
        }
    }

    fun gotoQibla(navigator: DestinationsNavigator) {
        navigator.navigate(QiblaUIDestination)
    }

    fun gotoQuiz(navigator: DestinationsNavigator) {
        navigator.navigate(QuizLobbyUIDestination)
    }

    fun gotoBooks(navigator: DestinationsNavigator) {
        navigator.navigate(BooksUIDestination)
    }

    fun gotoTV(navigator: DestinationsNavigator) {
        navigator.navigate(TvUIDestination)
    }

    fun gotoRadio(navigator: DestinationsNavigator) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            navigator.navigate(RadioClientUIDestination)
        }
        else {
            _uiState.update { it.copy(
                shouldShowUnsupported = true
            )}
        }
    }

    fun gotoDateConverter(navigator: DestinationsNavigator) {
        navigator.navigate(DateConverterUIDestination)
    }

    fun gotoSettings(navigator: DestinationsNavigator) {
        navigator.navigate(SettingsUIDestination)
    }

    fun contactMe(ctx: Context) {
        val contactIntent = Intent(
            Intent.ACTION_SENDTO,
            Uri.fromParts("mailto", Global.CONTACT_EMAIL, null)
        )
        contactIntent.putExtra(Intent.EXTRA_SUBJECT, "Hidaya")
        contactIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        ctx.startActivity(
            Intent.createChooser(
                contactIntent,
                "Choose an Email client :"
            )
        )
    }

    fun shareApp(ctx: Context) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "App Share")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, Global.PLAY_STORE_URL)
        ctx.startActivity(
            Intent.createChooser(sharingIntent, "Share via")
        )
    }

    fun gotoAbout(navigator: DestinationsNavigator) {
        navigator.navigate(AboutUIDestination)
    }

}