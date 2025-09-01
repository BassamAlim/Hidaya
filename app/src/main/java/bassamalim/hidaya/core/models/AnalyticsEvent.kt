package bassamalim.hidaya.core.models

sealed class AnalyticsEvent(
    val name: String,
    val parameters: Map<String, Any> = emptyMap()
) {

    data class ScreenViewed(val screenName: String) : AnalyticsEvent(
        name = "screen_viewed",
        parameters = mapOf("screen_name" to screenName)
    )

    data object DailyWerdViewed : AnalyticsEvent(name = "daily_werd_viewed")

    data class QuranSuraViewed(val suraName: String) : AnalyticsEvent(
        name = "quran_sura_viewed",
        parameters = mapOf("sura_name" to suraName)
    )

    data class QuranPageRead(val pageNum: Int) : AnalyticsEvent(
        name = "quran_page_read",
        parameters = mapOf("page_num" to pageNum)
    )

    data class QuranPageViewed(val pageNum: Int) : AnalyticsEvent(
        name = "quran_page_viewed",
        parameters = mapOf("page_num" to pageNum)
    )

    data class QuranSearchPerformed(val query: String) : AnalyticsEvent(
        name = "quran_search_performed",
        parameters = mapOf("query" to query)
    )

    data class RemembranceViewed(
        val remembranceId: Int,
        val remembranceName: String
    ) : AnalyticsEvent(
        name = "remembrance_viewed",
        parameters = mapOf(
            "remembrance_id" to remembranceId,
            "remembrance_name" to remembranceName
        )
    )

    data class RecitationPlayed(
        val reciterId: Int,
        val narrationId: Int,
        val suraName: String
    ) : AnalyticsEvent(
        name = "recitation_played",
        parameters = mapOf(
            "reciter_id" to reciterId,
            "narration_id" to narrationId,
            "sura_name" to suraName
        )
    )

    data class QuizCategoryStarted(val category: String) : AnalyticsEvent(
        name = "quiz_category_started",
        parameters = mapOf("category" to category)
    )

    data class BookOpened(val bookId: Int) : AnalyticsEvent(
        name = "book_opened",
        parameters = mapOf("book_id" to bookId)
    )

    data class TvChannelViewed(val channel: String) : AnalyticsEvent(
        name = "tv_channel_viewed",
        parameters = mapOf("channel" to channel)
    )

}