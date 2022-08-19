package bassamalim.hidaya.utils

import android.content.Context

object LangUtils {

    fun translateNumbers(context: Context, english: String, timeFormat: Boolean = false): String {
        var eng = english
        if (timeFormat) {
            if (eng.startsWith('0')) {
                eng = eng.replaceFirst("0", "")
                if (eng.startsWith('0')) {
                    eng = eng.replaceFirst("0:", "")
                    if (eng.startsWith('0') && !eng.startsWith("00"))
                        eng = eng.replaceFirst("0", "")
                }
            }
        }

        if (PrefUtils.getNumeralsLanguage(context) == "en") return eng

        val map = HashMap<Char, Char>()
        map['0'] = '٠'
        map['1'] = '١'
        map['2'] = '٢'
        map['3'] = '٣'
        map['4'] = '٤'
        map['5'] = '٥'
        map['6'] = '٦'
        map['7'] = '٧'
        map['8'] = '٨'
        map['9'] = '٩'
        map['a'] = 'ص'
        map['p'] = 'م'

        val temp = StringBuilder()
        for (char in eng) {
            if (map.containsKey(char)) temp.append(map[char])
            else if (char != 'm') temp.append(char)
        }

        return temp.toString()
    }

}