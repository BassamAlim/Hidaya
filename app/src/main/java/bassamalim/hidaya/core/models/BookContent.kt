package bassamalim.hidaya.core.models

import com.google.gson.annotations.SerializedName

data class BookContent(
    @SerializedName("bookInfo") val info: Info,
    @SerializedName("chapters") val chapters: Array<Chapter>
) {

    data class Info(
        @SerializedName("bookId") val id: Int,
        @SerializedName("bookTitle") val title: String,
        @SerializedName("author") val author: String
    )

    data class Chapter(
        @SerializedName("chapterId") val id: Int,
        @SerializedName("chapterTitle") val title: String,
        @SerializedName("doors") val doors: Array<Door>
    ) {
        class Door(
            @SerializedName("doorId") val id: Int,
            @SerializedName("doorTitle") val title: String,
            @SerializedName("text") val text: String
        )

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Chapter

            if (id != other.id) return false
            if (title != other.title) return false
            if (!doors.contentEquals(other.doors)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id
            result = 31 * result + title.hashCode()
            result = 31 * result + doors.contentHashCode()
            return result
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BookContent

        if (info != other.info) return false
        if (!chapters.contentEquals(other.chapters)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = info.hashCode()
        result = 31 * result + chapters.contentHashCode()
        return result
    }

}