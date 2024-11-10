package bassamalim.hidaya.features.quran.reader.ui

import bassamalim.hidaya.core.models.Verse

data class VersesSection(
    val verses: List<Verse>,
    val numOfLines: Int = 0
): Section()