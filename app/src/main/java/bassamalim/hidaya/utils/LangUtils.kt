package bassamalim.hidaya.utils

import android.content.Context

object LangUtils {

    private val enNums = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'p')
    private val arNums = arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩', 'ص', 'م')

    fun translateNums(context: Context, string: String, timeFormat: Boolean = false) : String {
        val language = PrefUtils.getNumeralsLanguage(context)
        
        val str =
            if (timeFormat) cleanup(string, language)
            else string
        
        return if (arNums.contains(string[0])) {
            if (language == "ar") str
            else arToEn(str)
        }
        else {
            if (language == "en") str
            else enToAr(str)
        }
    }

    private fun enToAr(english: String): String {
        val temp = StringBuilder()
        for (i in english.indices) {
            val index = enNums.indexOf(english[i])
            if (index == -1) {
                if (english[i] != 'm') temp.append(english[i])
            }
            else temp.append(arNums[index])
        }

        return temp.toString()
    }

    private fun arToEn(arabic: String): String {
        val suffix = arabic.endsWith('ص') || arabic.endsWith('م')

        val temp = StringBuilder()
        for (i in arabic.indices) {
            val index = arNums.indexOf(arabic[i])
            if (index == -1) temp.append(arabic[i])
            else temp.append(enNums[index])
        }
        if (suffix) temp.append('m')

        return temp.toString()
    }
    
    private fun cleanup(string: String, language: String) : String {
        var str = string

        if (language == "en") {
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