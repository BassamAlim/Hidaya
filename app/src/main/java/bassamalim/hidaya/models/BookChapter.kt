package bassamalim.hidaya.models

import android.view.View

data class BookChapter(
    val chapterId: Int, val chapterTitle: String, var favorite: Boolean,
    val listener: View.OnClickListener
)