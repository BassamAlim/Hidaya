package bassamalim.hidaya.core.data.dataSources.preferences

import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.enums.Language as Languages
import bassamalim.hidaya.core.enums.LocationType as LocationTypes
import bassamalim.hidaya.core.enums.NotificationType as NotificationTypes
import bassamalim.hidaya.core.enums.Theme as Themes
import bassamalim.hidaya.core.enums.TimeFormat as TimeFormats
import bassamalim.hidaya.features.quran.reader.ui.QuranViewType as QuranViewTypes

sealed class Preference(val key: String, val default: Any) {

    data object AthanId : bassamalim.hidaya.core.data.dataSources.preferences.Preference("athan_voice", "1")

    data object RemembrancesTextSize : bassamalim.hidaya.core.data.dataSources.preferences.Preference("athkar_text_size_key", 15f)

    data object VerseReciter : bassamalim.hidaya.core.data.dataSources.preferences.Preference("aya_reciter", "13")

    data object VerseRepeat : bassamalim.hidaya.core.data.dataSources.preferences.Preference("aya_repeat", 1f)  // float to use in slider

    data object BooksTextSize : bassamalim.hidaya.core.data.dataSources.preferences.Preference("books_text_size_key", 15f)

    data object BookmarkedPage : bassamalim.hidaya.core.data.dataSources.preferences.Preference("bookmarked_page", -1)

    data object BookmarkedSura : bassamalim.hidaya.core.data.dataSources.preferences.Preference("bookmarked_sura", -1)

    data object BookSearcherMaxMatchesIndex : bassamalim.hidaya.core.data.dataSources.preferences.Preference(
        "books_searcher_max_matches_index",
        0
    )

    data class BookChaptersFavs(val bookId: Int) : bassamalim.hidaya.core.data.dataSources.preferences.Preference(
        key = "book${bookId}_favs",
        default = ""
    )

    data object CountryID : bassamalim.hidaya.core.data.dataSources.preferences.Preference("country_id", -1)

    data object CityID : bassamalim.hidaya.core.data.dataSources.preferences.Preference("city_id", -1)

    data object DateOffset : bassamalim.hidaya.core.data.dataSources.preferences.Preference("date_offset", 0)

//    data class ExtraNotificationMinuteOfDay(val pid: PID) : Preference(
//        key = "${pid.name}_mod",
//        default = when (pid) {
//            PID.MORNING -> 300  // 5 am
//            PID.EVENING -> 960  // 4 pm
//            PID.DAILY_WERD -> 1260  // 9 pm
//            PID.FRIDAY_KAHF -> 600 // 10 am
//            else -> 0
//        }
//    )

    data class ExtraNotificationHour(val pid: PID) : bassamalim.hidaya.core.data.dataSources.preferences.Preference(
        key = "${pid.name}_hour",
        default = when (pid) {
            PID.MORNING -> 5
            PID.EVENING -> 16
            PID.DAILY_WERD -> 21
            PID.FRIDAY_KAHF -> 10
            else -> 0
        }
    )

    data class ExtraNotificationMinute(val pid: PID) : bassamalim.hidaya.core.data.dataSources.preferences.Preference(
        key = "${pid.name}_minute",
        default = 0
    )

    data object FavoriteSuras : bassamalim.hidaya.core.data.dataSources.preferences.Preference("favorite_suar", "")

    data object RemembranceFavorites : bassamalim.hidaya.core.data.dataSources.preferences.Preference("favorite_athkar", "")

    data object FavoriteReciters : bassamalim.hidaya.core.data.dataSources.preferences.Preference("favorite_reciters", "")

    data object FirstTime : bassamalim.hidaya.core.data.dataSources.preferences.Preference("new_user", true)

    data object Language : bassamalim.hidaya.core.data.dataSources.preferences.Preference("language_key", Languages.ARABIC.name)

    data object LastDailyUpdateDay : bassamalim.hidaya.core.data.dataSources.preferences.Preference("last_daily_update_day", 0)

    data object LocationType : bassamalim.hidaya.core.data.dataSources.preferences.Preference("location_type", LocationTypes.AUTO.name)

    data object LastDBVersion : bassamalim.hidaya.core.data.dataSources.preferences.Preference("last_db_version", 1)

    data class LastNotificationDate(val pid: PID) : bassamalim.hidaya.core.data.dataSources.preferences.Preference(
        key = "last_${pid.name}_notification_date",
        default = -1
    )

    data object LastPlayedMediaId : bassamalim.hidaya.core.data.dataSources.preferences.Preference("last_played_media_id", "")

    data object NumeralsLanguage : bassamalim.hidaya.core.data.dataSources.preferences.Preference("numerals_language_key", Languages.ARABIC.name)

    data class NotificationType(val pid: PID) : bassamalim.hidaya.core.data.dataSources.preferences.Preference(
        key = "${pid.name}_notification_type",
        default =
            if (pid == PID.SUNRISE) NotificationTypes.NONE.name
            else NotificationTypes.NOTIFICATION.name
    )

    data class NotifyExtraNotification(val pid: PID) : bassamalim.hidaya.core.data.dataSources.preferences.Preference(
        key = "notify_${pid.name}",
        default = true
    )

    data object PrayerTimesCalculationMethod : bassamalim.hidaya.core.data.dataSources.preferences.Preference(
        "prayer_times_calc_method_key",
        "MECCA"
    )

    data object PrayerTimesJuristicMethod : bassamalim.hidaya.core.data.dataSources.preferences.Preference("juristic_method_key", "SHAFII")

    data object PrayerTimesAdjustment : bassamalim.hidaya.core.data.dataSources.preferences.Preference("high_lat_adjustment_key", "NONE")

    data object QuranSearcherMaxMatchesIndex : bassamalim.hidaya.core.data.dataSources.preferences.Preference(
        "quran_searcher_max_matches_index",
        0
    )

    data object QuranPagesRecord : bassamalim.hidaya.core.data.dataSources.preferences.Preference("quran_pages_record", 0)

    data object QuranTextSize : bassamalim.hidaya.core.data.dataSources.preferences.Preference("quran_text_size", 30f)

    data object QuranViewType : bassamalim.hidaya.core.data.dataSources.preferences.Preference("quran_view_type", QuranViewTypes.PAGE.name)

    data object StopOnSuraEnd : bassamalim.hidaya.core.data.dataSources.preferences.Preference("stop_on_sura_end", false)

    data object StopOnPageEnd : bassamalim.hidaya.core.data.dataSources.preferences.Preference("stop_on_page_end", false)

    data object ShowBooksTutorial : bassamalim.hidaya.core.data.dataSources.preferences.Preference("show_books_tutorial", true)

    data object ShowQuranTutorial : bassamalim.hidaya.core.data.dataSources.preferences.Preference("show_quran_tutorial", true)

    data object ShowQuranViewerTutorial : bassamalim.hidaya.core.data.dataSources.preferences.Preference("show_quran_viewer_tutorial", true)

    data object ShowPrayersTutorial : bassamalim.hidaya.core.data.dataSources.preferences.Preference("show_prayers_tutorial", true)

    data object StoredLocation : bassamalim.hidaya.core.data.dataSources.preferences.Preference("stored_location", "{}")

    data object SelectedSearchBooks : bassamalim.hidaya.core.data.dataSources.preferences.Preference("selected_search_books", "")

    data object SelectedNarrations : bassamalim.hidaya.core.data.dataSources.preferences.Preference("selected_rewayat", "")

    data object RecitationsPlaybackRecord : bassamalim.hidaya.core.data.dataSources.preferences.Preference("telawat_playback_record", 0L)

    data object RecitationsRepeatMode : bassamalim.hidaya.core.data.dataSources.preferences.Preference("telawat_repeat_mode", 0)

    data object RecitationsShuffleMode : bassamalim.hidaya.core.data.dataSources.preferences.Preference("telawat_shuffle_mode", 0)

    data object TimeFormat : bassamalim.hidaya.core.data.dataSources.preferences.Preference("time_format_key", TimeFormats.TWELVE.name)

    data object Theme : bassamalim.hidaya.core.data.dataSources.preferences.Preference("theme_key", Themes.LIGHT.name)

    data class TimeOffset(val pid: PID) : bassamalim.hidaya.core.data.dataSources.preferences.Preference(key = "${pid.name}_offset", default = 0)

    data class ReminderOffset(val pid: PID) : bassamalim.hidaya.core.data.dataSources.preferences.Preference(
        key = "${pid.name}_reminder_offset",
        default = 0
    )

    data object WerdPage : bassamalim.hidaya.core.data.dataSources.preferences.Preference("werd_page", 25)

    data object WerdDone : bassamalim.hidaya.core.data.dataSources.preferences.Preference("werd_done", false)

    data object LastRecitationProgress : bassamalim.hidaya.core.data.dataSources.preferences.Preference("last_telawa_progress", 0)

}