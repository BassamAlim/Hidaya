package bassamalim.hidaya.features.books.booksMenu

import bassamalim.hidaya.core.enums.DownloadState

data class Book(
    val title: String,
    val author: String,
    val url: String,
    val isFavorite: Boolean,
    val downloadState: DownloadState
)
