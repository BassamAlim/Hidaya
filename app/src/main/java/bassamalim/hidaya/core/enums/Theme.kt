package bassamalim.hidaya.core.enums

enum class Theme {
    LIGHT,
    DARK;

    companion object {
        fun isDarkTheme(theme: Theme) = theme == DARK
    }
}