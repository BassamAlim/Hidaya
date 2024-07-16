package bassamalim.hidaya.core.helpers

import android.annotation.SuppressLint
import android.app.Application
import android.provider.Settings

object OS {

    @SuppressLint("HardwareIds")
    fun getDeviceId(app: Application): String =
        Settings.Secure.getString(
            app.contentResolver,
            Settings.Secure.ANDROID_ID
        )

}