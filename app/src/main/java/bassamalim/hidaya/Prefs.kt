package bassamalim.hidaya

sealed class Prefs(val key: String, val default: Any) {
    object Language : Prefs(
        "language_key",
        bassamalim.hidaya.enum.Language.ARABIC.name
    )
    object NumeralsLanguage : Prefs(
        "numerals_language_key",
        bassamalim.hidaya.enum.Language.ARABIC.name
    )
    object TimeFormat : Prefs(
        "time_format_key",
        bassamalim.hidaya.enum.TimeFormat.TWELVE.name
    )
    object Theme : Prefs(
        "theme_key",
        bassamalim.hidaya.enum.Theme.LIGHT.name
    )
    object LocationType: Prefs(
        "location_type",
        bassamalim.hidaya.enum.LocationType.Auto.name
    )
    object FirstTime: Prefs("new_user", true)
    object LastDBVersion: Prefs("last_db_version", 1)
    object AthkarTextSize: Prefs("athkar_text_size_key", 15)
    object LastDailyUpdate: Prefs("last_daily_update", "No daily updates yet")
    object FavoriteSuras: Prefs("favorite_suras", "")
    object FavoriteAthkar: Prefs("favorite_athkar", "")
    object FavoriteReciters: Prefs("favorite_reciters", "")
    object DateOffset: Prefs("date_offset", 0)
    object CityID: Prefs("city_id", -1)
    object SelectedSearchBooks: Prefs("selected_search_books", "")
    object BookSearcherMaxMatchesIndex: Prefs("books_searcher_max_matches_index", 0)
    data class BookChaptersFavs(val bookId: Int): Prefs(
        "book${bookId}_favs",
        ""
    )
}