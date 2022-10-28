package bassamalim.hidaya.models

import androidx.compose.ui.text.AnnotatedString
import java.io.Serializable

data class Ayah(
    private var id: Int,
    private var juz: Int,
    private val surahNum: Int,
    private val ayahNum: Int,
    private val surahName: String,
    private var text: String?,
    private var translation: String?,
    private val tafseer: String
) : Serializable {

    private var pageNum = 0
    private var start = 0
    private var end = 0
    private var index = 0

    fun getId(): Int {
        return id
    }

    fun getJuz(): Int {
        return juz
    }

    fun getSurahNum(): Int {
        return surahNum
    }

    fun getAyahNum(): Int {
        return ayahNum
    }

    fun getSurahName(): String {
        return surahName
    }

    fun getPageNum(): Int {
        return pageNum
    }

    fun setText(text: String?) {
        this.text = text
    }

    fun getText(): String? {
        return text
    }

    fun getTranslation(): String? {
        return translation
    }

    fun getTafseer(): String {
        return tafseer
    }

    fun getStart(): Int {
        return start
    }

    fun setStart(start: Int) {
        this.start = start
    }

    fun getEnd(): Int {
        return end
    }

    fun setEnd(end: Int) {
        this.end = end
    }

    fun getIndex(): Int {
        return index
    }

    fun setIndex(index: Int) {
        this.index = index
    }

}