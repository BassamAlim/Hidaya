package bassamalim.hidaya.features.books.booksMenu.ui

import bassamalim.hidaya.core.enums.DownloadState

data class Book(
    val title: String,
    val author: String,
    val url: String,
    val isFavorite: Boolean,
    val downloadState: DownloadState
)
