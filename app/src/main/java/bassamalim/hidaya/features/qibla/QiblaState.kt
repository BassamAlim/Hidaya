package bassamalim.hidaya.features.qibla

data class QiblaState(
    val error: Boolean = false,
    val errorMassageResId: Int = -1,
    val qiblaAngle: Float = 0F,
    val compassAngle: Float = 0F,
    val accuracy: Int = 0,
    val distanceToKaaba: String = "",
    val isOnPoint: Boolean = false,
    val calibrationDialogShown: Boolean = false
)
