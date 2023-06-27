package bassamalim.hidaya.features.prayerSetting

import android.os.Parcel
import android.os.Parcelable
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID

data class PrayerSettings(
    val pid: PID,
    val notificationType: NotificationType,
    val timeOffset: Int,
    val reminderOffset: Int = 0
): Parcelable {

    constructor(parcel: Parcel) : this(
        PID.valueOf(parcel.readString() ?: ""),
        NotificationType.valueOf(parcel.readString() ?: ""),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(pid.name)
        parcel.writeString(notificationType.name)
        parcel.writeInt(timeOffset)
        parcel.writeInt(reminderOffset)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PrayerSettings> {
        override fun createFromParcel(parcel: Parcel): PrayerSettings {
            return PrayerSettings(parcel)
        }

        override fun newArray(size: Int): Array<PrayerSettings?> {
            return arrayOfNulls(size)
        }
    }

}
