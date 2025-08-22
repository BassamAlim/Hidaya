package bassamalim.hidaya.core.enums

enum class Theme {
    ORIGINAL,
    WHITE,
    BLACK,
    NEW;

    companion object {
        fun isDarkTheme(theme: Theme): Boolean {
            return theme == ORIGINAL || theme == BLACK
        }
    }
}