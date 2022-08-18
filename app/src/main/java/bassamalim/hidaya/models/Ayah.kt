package bassamalim.hidaya.models

import android.text.SpannableString
import android.widget.TextView
import java.io.Serializable

class Ayah: Serializable {

    private var id = 0
    private var juz = 0
    private val surahNum: Int
    private val ayahNum: Int
    private val surahName: String
    private var pageNum = 0
    private var text: String? = null
    private var translation: String? = null
    private val tafseer: String
    private var start = 0
    private var end = 0
    private var ss: SpannableString? = null
    private var index = 0
    private var screen: TextView? = null

    constructor(
        id: Int, juz: Int, surahNum: Int, ayahNum: Int, surahName: String,
        text: String?, translation: String?, tafseer: String
    ) {
        this.id = id
        this.juz = juz
        this.surahNum = surahNum
        this.ayahNum = ayahNum
        this.surahName = surahName
        this.text = text
        this.translation = translation
        this.tafseer = tafseer
    }

    constructor(
        surahNum: Int, surahName: String, pageNum: Int, ayahNum: Int,
        tafseer: String, ss: SpannableString?
    ) {
        this.surahNum = surahNum
        this.ayahNum = ayahNum
        this.pageNum = pageNum
        this.surahName = surahName
        this.tafseer = tafseer
        this.ss = ss
    }

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

    fun getSS(): SpannableString? {
        return ss
    }

    fun setSS(ss: SpannableString?) {
        this.ss = ss
    }

    fun getIndex(): Int {
        return index
    }

    fun setIndex(index: Int) {
        this.index = index
    }

    fun getScreen(): TextView? {
        return screen
    }

    fun setScreen(screen: TextView?) {
        this.screen = screen
    }
}