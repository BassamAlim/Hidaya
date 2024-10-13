package bassamalim.hidaya.core.utils

import bassamalim.hidaya.core.enums.Language

object LangUtils {

    private val enNums = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    private val arNums = arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

    fun translateNums(
        string: String,
        numeralsLanguage: Language,
        isTime: Boolean = false
    ) : String {
        if (string.isEmpty()) return string

        val str = if (isTime) cleanup(string, numeralsLanguage) else string

        return if (arNums.contains(string[0])) {
            when (numeralsLanguage) {
                Language.ARABIC -> str
                Language.ENGLISH -> arToEn(str)
            }
        }
        else {
            when (numeralsLanguage) {
                Language.ARABIC -> enToAr(str)
                Language.ENGLISH -> str
            }
        }
    }

    private fun enToAr(english: String): String {
        val temp = StringBuilder()
        for (i in english.indices) {
            val index = enNums.indexOf(english[i])
            if (index == -1) temp.append(english[i])
            else temp.append(arNums[index])
        }

        return temp
            .replace(Regex("am"), "ص")
            .replace(Regex("pm"), "م")
    }

    private fun arToEn(arabic: String): String {
        val temp = StringBuilder()
        for (i in arabic.indices) {
            val index = arNums.indexOf(arabic[i])
            if (index == -1) temp.append(arabic[i])
            else temp.append(enNums[index])
        }

        return temp
            .replace(Regex("ص"), "am")
            .replace(Regex("م"), "pm")
    }
    
    private fun cleanup(string: String, language: Language) : String {
        var str = string

        when (language) {
            Language.ARABIC -> {
                if (str.startsWith('0')) {
                    str = str.replaceFirst("0", "")
                    if (str.startsWith('0')) {
                        str = str.replaceFirst("0:", "")
                        if (str.startsWith('0') && !str.startsWith("00"))
                            str = str.replaceFirst("0", "")
                    }
                }
            }
            Language.ENGLISH -> {
                if (str.startsWith('٠')) {
                    str = str.replaceFirst("٠", "")
                    if (str.startsWith('٠')) {
                        str = str.replaceFirst("٠:", "")
                        if (str.startsWith('٠') && !str.startsWith("٠٠"))
                            str = str.replaceFirst("٠", "")
                    }
                }
            }
        }
        
        return str
    }

}