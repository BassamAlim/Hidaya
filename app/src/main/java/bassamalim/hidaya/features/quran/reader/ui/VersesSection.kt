package bassamalim.hidaya.features.quran.reader.ui

import bassamalim.hidaya.core.models.Verse

data class VersesSection(
    val verses: List<Verse>,
    var numOfLines: Int = 0
): Section()