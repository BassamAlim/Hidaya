package bassamalim.hidaya.utils

import android.app.Activity
import bassamalim.hidaya.enum.ListType

object ActivityUtils {

    fun restartActivity(activity: Activity) {
        val intent = activity.intent
        activity.finish()
        activity.startActivity(intent)
    }

}