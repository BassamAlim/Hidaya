package bassamalim.hidaya.state

import bassamalim.hidaya.R

data class QuizState(
    val questionNumText: String = "",
    val question: String = "",
    val answers: List<String> = emptyList(),
    val selection: Int = -1,
    val prevBtnEnabled: Boolean = false,
    val nextBtnEnabled: Boolean = true,
    val nextBtnTextResId: Int = R.string.next_question
)
