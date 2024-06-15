package bassamalim.hidaya.core.utils

import bassamalim.hidaya.core.enums.Language

object LangUtils {

    private val enNums = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    private val arNums = arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

    fun translateNums(
        numeralsLanguage: Language,
        string: String,
        timeFormat: Boolean = false
    ) : String {
        val str = if (timeFormat) cleanup(string, numeralsLanguage) else string

        return if (arNums.contains(string[0])) {
            if (numeralsLanguage == Language.ARABIC) str
            else arToEn(str)
        }
        else {
            if (numeralsLanguage == Language.ENGLISH) str
            else enToAr(str)
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

        if (language == Language.ENGLISH) {
            if (str.startsWith('٠')) {
                str = str.replaceFirst("٠", "")
                if (str.startsWith('٠')) {
                    str = str.replaceFirst("٠:", "")
                    if (str.startsWith('٠') && !str.startsWith("٠٠"))
                        str = str.replaceFirst("٠", "")
                }
            }
        }
        else {
            if (str.startsWith('0')) {
                str = str.replaceFirst("0", "")
                if (str.startsWith('0')) {
                    str = str.replaceFirst("0:", "")
                    if (str.startsWith('0') && !str.startsWith("00"))
                        str = str.replaceFirst("0", "")
                }
            }
        }
        
        return str
    }

}