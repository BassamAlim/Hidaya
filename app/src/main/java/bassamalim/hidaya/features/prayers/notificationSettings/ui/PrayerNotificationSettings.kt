package bassamalim.hidaya.features.prayers.notificationSettings.ui

import android.os.Parcel
import android.os.Parcelable
import bassamalim.hidaya.core.enums.NotificationType

data class PrayerNotificationSettings(
    val notificationType: NotificationType,
    val reminderOffset: Int = 0
): Parcelable {

    constructor(parcel: Parcel) : this(
        NotificationType.valueOf(parcel.readString() ?: ""),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(notificationType.name)
        parcel.writeInt(reminderOffset)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PrayerNotificationSettings> {
        override fun createFromParcel(parcel: Parcel): PrayerNotificationSettings {
            return PrayerNotificationSettings(parcel)
        }

        override fun newArray(size: Int): Array<PrayerNotificationSettings?> {
            return arrayOfNulls(size)
        }
    }

}
