package bassamalim.hidaya.state

data class QiblaState(
    val qiblaAngle: Float = 0F,
    val compassAngle: Float = 0F,
    val accuracy: Int = 0,
    val isOnPoint: Boolean = false,
    val calibrationDialogShown: Boolean = false
)
