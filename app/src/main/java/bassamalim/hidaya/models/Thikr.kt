package bassamalim.hidaya.models

import android.view.View

data class Thikr(
    private val id: Int,
    private val title: String?,
    private val text: String,
    private val textTranslation: String?,
    private val fadl: String?,
    private val reference: String?,
    private val repetition: String,
    private val referenceListener: View.OnClickListener
) {

    fun getId(): Int {
        return id
    }

    fun getTitle(): String? {
        return title
    }

    fun getText(): String {
        return text
    }

    fun getTextTranslation(): String? {
        return textTranslation
    }

    fun getFadl(): String? {
        return fadl
    }

    fun getReference(): String? {
        return reference
    }

    fun getRepetition(): String {
        return repetition
    }

    fun getReferenceListener(): View.OnClickListener {
        return referenceListener
    }
}