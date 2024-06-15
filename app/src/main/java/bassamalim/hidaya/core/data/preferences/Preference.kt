package bassamalim.hidaya.core.data.preferences

import bassamalim.hidaya.core.enums.Language.ARABIC
import bassamalim.hidaya.core.enums.LocationType.Auto
import bassamalim.hidaya.core.enums.NotificationType.None
import bassamalim.hidaya.core.enums.NotificationType.Notification
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.enums.QuranViewTypes
import bassamalim.hidaya.core.enums.Theme.LIGHT
import bassamalim.hidaya.core.enums.TimeFormat.TWELVE

sealed class Preference(val key: String, val default: Any) {

    data object AthanVoice : Preference("athan_voice", "1")

    data object AthkarTextSize : Preference("athkar_text_size_key", 15f)

    data object AyaReciter : Preference("aya_reciter", "13")

    data object AyaRepeat : Preference("aya_repeat", 1f)  // float to use in slider

    data object BooksTextSize : Preference("books_text_size_key", 15f)

    data object BookmarkedPage : Preference("bookmarked_page", -1)

    data object BookmarkedSura : Preference("bookmarked_sura", -1)

    data object BookSearcherMaxMatchesIndex : Preference("books_searcher_max_matches_index", 0)

    data class BookChaptersFavs(val bookId: Int) : Preference(
        key = "book${bookId}_favs",
        default = ""
    )

    data object CountryID : Preference("country_id", -1)

    data object CityID : Preference("city_id", -1)

    data object DailyUpdateRecord : Preference("last_daily_update", "No daily updates yet")

    data object DateOffset : Preference("date_offset", 0)

    data class ExtraNotificationHour(val pid: PID) : Preference(
        key = "${pid.name}_hour",
        default = when (pid) {
            PID.MORNING -> 5
            PID.EVENING -> 16
            PID.DAILY_WERD -> 21
            PID.FRIDAY_KAHF -> 10
            else -> 0
        }
    )

    data class ExtraNotificationMinute(val pid: PID) : Preference(
        key = "${pid.name}_minute",
        default = 0
    )

    data object FavoriteSuar : Preference("favorite_suar", "")

    data object FavoriteAthkar : Preference("favorite_athkar", "")

    data object FavoriteReciters : Preference("favorite_reciters", "")

    data object FirstTime : Preference("new_user", true)

    data object Language : Preference("language_key", ARABIC.name)

    data object LastDailyUpdateDay : Preference("last_daily_update_day", 0)

    data object LocationType : Preference("location_type", Auto.name)

    data object LastDBVersion : Preference("last_db_version", 1)

    data class LastNotificationDate(val pid: PID) : Preference(
        key = "last_${pid.name}_notification_date",
        default = -1
    )

    data object LastPlayedMediaId : Preference("last_played_media_id", "")

    data object NumeralsLanguage : Preference("numerals_language_key", ARABIC.name)

    data class NotificationType(val pid: PID) : Preference(
        key = "${pid.name}_notification_type",
        default = if (pid == PID.SUNRISE) None.name else Notification.name
    )

    data class NotifyExtraNotification(val pid: PID) : Preference(
        key = "notify_${pid.name}",
        default = true
    )

    data object PrayerTimesCalculationMethod : Preference("prayer_times_calc_method_key", "MECCA")

    data object PrayerTimesJuristicMethod : Preference("juristic_method_key", "SHAFII")

    data object PrayerTimesAdjustment : Preference("high_lat_adjustment_key", "NONE")

    data object QuranSearcherMaxMatchesIndex : Preference("quran_searcher_max_matches_index", 0)

    data object QuranPagesRecord : Preference("quran_pages_record", 0)

    data object QuranTextSize : Preference("quran_text_size", 30f)

    data object QuranViewType : Preference("quran_view_type", QuranViewTypes.Page.name)

    data object StopOnSuraEnd : Preference("stop_on_sura_end", false)

    data object StopOnPageEnd : Preference("stop_on_page_end", false)

    data object ShowBooksTutorial : Preference("show_books_tutorial", true)

    data object ShowQuranTutorial : Preference("show_quran_tutorial", true)

    data object ShowQuranViewerTutorial : Preference("show_quran_viewer_tutorial", true)

    data object ShowPrayersTutorial : Preference("show_prayers_tutorial", true)

    data object StoredLocation : Preference("stored_location", "{}")

    data object SelectedSearchBooks : Preference("selected_search_books", "")

    data object SelectedRewayat : Preference("selected_rewayat", "")

    data object TelawatPlaybackRecord : Preference("telawat_playback_record", 0L)

    data object TelawatRepeatMode : Preference("telawat_repeat_mode", 0)

    data object TelawatShuffleMode : Preference("telawat_shuffle_mode", 0)

    data object TimeFormat : Preference("time_format_key", TWELVE.name)

    data object Theme : Preference("theme_key", LIGHT.name)

    data class TimeOffset(val pid: PID) : Preference(
        key = "${pid.name}_offset",
        default = 0
    )

    data class ReminderOffset(val pid: PID) : Preference(
        key = "${pid.name}_reminder_offset",
        default = 0
    )

    data object WerdPage : Preference("werd_page", 25)

    data object WerdDone : Preference("werd_done", false)

    data object LastTelawaProgress : Preference("last_telawa_progress", 0)

}