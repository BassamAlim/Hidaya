package bassamalim.hidaya.utils

import android.app.Activity
import bassamalim.hidaya.enum.ListType

object ActivityUtils {

    fun restartActivity(activity: Activity) {
        val intent = activity.intent
        activity.finish()
        activity.startActivity(intent)
    }

    fun getListType(ordinal: Int): ListType {
        return when (ordinal) {
            1 -> ListType.Favorite
            2 -> ListType.Downloaded
            else -> ListType.All
        }
    }

}