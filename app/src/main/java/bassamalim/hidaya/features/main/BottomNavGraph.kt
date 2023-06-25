package bassamalim.hidaya.features.main

import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph

@RootNavGraph
@NavGraph
annotation class BottomNavGraph(
    val start: Boolean = false
)