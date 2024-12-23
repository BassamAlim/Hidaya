package bassamalim.hidaya.core.data.dataSources.preferences

import bassamalim.hidaya.core.enums.Language as Languages
import bassamalim.hidaya.core.enums.LocationType as LocationTypes
import bassamalim.hidaya.core.enums.Theme as Themes
import bassamalim.hidaya.core.enums.TimeFormat as TimeFormats
import bassamalim.hidaya.features.quran.reader.ui.QuranViewType as QuranViewTypes

sealed class Preference(val key: String, val default: Any) {

    data object AthanId : Preference("athan_voice", "1")

    data object RemembrancesTextSize : Preference("athkar_text_size_key", 15f)

    data object VerseReciter : Preference("aya_reciter", "13")

    data object BooksTextSize : Preference("books_text_size_key", 15f)

    data object BookmarkedPage : Preference("bookmarked_page", -1)

    data object CountryID : Preference("country_id", -1)

    data object CityID : Preference("city_id", -1)

    data object DateOffset : Preference("date_offset", 0)

    data object FavoriteSuras : Preference("favorite_suar", "")

    data object RemembranceFavorites : Preference("favorite_athkar", "")

    data object FavoriteReciters : Preference("favorite_reciters", "")

    data object FirstTime : Preference("new_user", true)

    data object Language : Preference("language_key", Languages.ARABIC.name)

    data object LocationType : Preference("location_type", LocationTypes.AUTO.name)

    data object LastDBVersion : Preference("last_db_version", 1)

    data object LastPlayedMediaId : Preference("last_played_media_id", "")

    data object NumeralsLanguage : Preference("numerals_language_key", Languages.ARABIC.name)

    data object PrayerTimesCalculationMethod : Preference(
        "prayer_times_calc_method_key",
        "MECCA"
    )

    data object PrayerTimesJuristicMethod : Preference("juristic_method_key", "SHAFII")

    data object PrayerTimesAdjustment : Preference("high_lat_adjustment_key", "NONE")

    data object QuranPagesRecord : Preference("quran_pages_record", 0)

    data object QuranTextSize : Preference("quran_text_size", 30f)

    data object QuranViewType : Preference("quran_view_type", QuranViewTypes.PAGE.name)

    data object StopOnSuraEnd : Preference("stop_on_sura_end", false)

    data object StopOnPageEnd : Preference("stop_on_page_end", false)

    data object ShowBooksTutorial : Preference("show_books_tutorial", true)

    data object ShowQuranTutorial : Preference("show_quran_tutorial", true)

    data object ShowQuranViewerTutorial : Preference("show_quran_viewer_tutorial", true)

    data object ShowPrayersTutorial : Preference("show_prayers_tutorial", true)

    data object StoredLocation : Preference("stored_location", "{}")

    data object RecitationsPlaybackRecord : Preference("telawat_playback_record", 0L)

    data object RecitationsRepeatMode : Preference("telawat_repeat_mode", 0)

    data object RecitationsShuffleMode : Preference("telawat_shuffle_mode", 0)

    data object TimeFormat : Preference("time_format_key", TimeFormats.TWELVE.name)

    data object Theme : Preference("theme_key", Themes.ORIGINAL.name)

    data object WerdPage : Preference("werd_page", 25)

    data object WerdDone : Preference("werd_done", false)

    data object LastRecitationProgress : Preference("last_telawa_progress", 0)

}