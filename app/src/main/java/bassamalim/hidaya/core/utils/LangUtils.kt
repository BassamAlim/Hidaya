package bassamalim.hidaya.core.utils

import bassamalim.hidaya.core.enums.Language
import java.util.Locale

object LangUtils {

    private val enNums = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    private val arNums = arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

    fun translateNums(string: String, numeralsLanguage: Language) : String {
        if (string.isEmpty()) return string

        return when (numeralsLanguage) {
            Language.ARABIC -> {
                val containsEnglish = string.any { enNums.contains(it) }
                if (containsEnglish) enToAr(string) else string
            }
            Language.ENGLISH -> {
                val containsArabic = string.any { arNums.contains(it) }
                if (containsArabic) arToEn(string) else string
            }
        }
    }

    fun translateTimeNums(
        string: String,
        language: Language,
        numeralsLanguage: Language,
        removeLeadingZeros: Boolean = true
    ) : String {
        if (string.isEmpty()) return string

        var str = string

        if (removeLeadingZeros)
            str = removeLeadingZeros(string)

        str = when (numeralsLanguage) {
            Language.ARABIC -> {
                val containsEnglish = str.any { enNums.contains(it) }
                if (containsEnglish) enToAr(str) else str
            }
            Language.ENGLISH -> {
                val containsArabic = str.any { arNums.contains(it) }
                if (containsArabic) arToEn(str) else str
            }
        }

        str = when (language) {
            Language.ARABIC -> suffixEnToAr(str)
            Language.ENGLISH -> suffixArToEn(str)
        }

        return str
    }

    private fun enToAr(english: String): String {
        val temp = StringBuilder()
        for (i in english.indices) {
            val index = enNums.indexOf(english[i])
            if (index == -1) temp.append(english[i])
            else temp.append(arNums[index])
        }
        return temp.toString()
    }

    private fun suffixEnToAr(english: String): String {
        return english
            .replace(Regex("am"), "ص")
            .replace(Regex("AM"), "ص")
            .replace(Regex("pm"), "م")
            .replace(Regex("PM"), "م")
    }

    private fun arToEn(arabic: String): String {
        val temp = StringBuilder()
        for (i in arabic.indices) {
            val index = arNums.indexOf(arabic[i])
            if (index == -1) temp.append(arabic[i])
            else temp.append(enNums[index])
        }
        return temp.toString()
    }

    private fun suffixArToEn(arabic: String): String {
        return arabic
            .replace(Regex("ص"), "am")
            .replace(Regex("م"), "pm")
    }
    
    private fun removeLeadingZeros(string: String) : String {
        val zeros = listOf("٠", "0")

        var str = string
        for (zero in zeros) {
            if (str.startsWith(zero)) {
                str = str.replaceFirst(zero, "")
                if (str.startsWith(zero)) {
                    str = str.replaceFirst("$zero:", "")
                    if (str.startsWith(zero) && !str.startsWith("$zero$zero"))
                        str = str.replaceFirst(zero, "")
                }
            }
        }
        return str
    }

    fun getLocale(language: Language): Locale {
        return Locale(
            when (language) {
                Language.ARABIC -> "ar"
                Language.ENGLISH -> "en"
            }
        )
    }

}