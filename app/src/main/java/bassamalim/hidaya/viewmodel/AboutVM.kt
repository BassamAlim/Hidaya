package bassamalim.hidaya.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import bassamalim.hidaya.R
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.repository.AboutRepo
import bassamalim.hidaya.state.AboutState
import bassamalim.hidaya.utils.DBUtils
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AboutVM @Inject constructor(
    app: Application,
    private val repository: AboutRepo
): AndroidViewModel(app) {

    private val context = getApplication<Application>().applicationContext

    private val _uiState = MutableStateFlow(AboutState())
    val uiState = _uiState.asStateFlow()

    private var counter by mutableStateOf(0)

    init {
        _uiState.update { it.copy(
            lastDailyUpdate = repository.getLastUpdate()
        )}
    }

    private fun enableDevMode() {
        _uiState.update { it.copy(
            isDevModeOn = true
        )}
    }

    fun rebuildDatabase() {
        context.deleteDatabase("HidayaDB")

        Log.i(Global.TAG, "Database Rebuilt")

        DBUtils.reviveDB(context)

        Toast.makeText(
            context, context.getString(R.string.database_rebuilt),
            Toast.LENGTH_SHORT
        ).show()
    }

    fun quickUpdate() {
        val url = FirebaseRemoteConfig.getInstance().getString(Global.UPDATE_URL)
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        context.startActivity(i)
    }

    fun onTitleClick() {
        if (++counter == 5) enableDevMode()
    }

}