package bassamalim.hidaya

sealed class Prefs(val key: String, val default: Any) {
    object Language : Prefs("language_key", "ar")
    object NumeralsLanguage : Prefs("numerals_language_key", "ar")
    object TimeFormat : Prefs("time_format_key", "12h")
    object Theme : Prefs("theme_key", "Light")
    object FirstTime: Prefs("new_user", true)
    object AthkarTextSize: Prefs("athkar_text_size_key", 15)
}