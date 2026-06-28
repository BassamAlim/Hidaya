package bassamalim.hidaya.core.ui.components.tutorial

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

/**
 * Holds the state of an interactive coach-mark tutorial: the ordered steps, the
 * current position, and the on-screen bounds of every registered target.
 *
 * Create with [rememberTutorialState], register targets with
 * [Modifier.tutorialTarget], and render with `TutorialOverlay`.
 */
@Stable
class TutorialState {

    /** Bounds (in root coordinates) of every target registered via [tutorialTarget]. */
    val targets = mutableStateMapOf<String, Rect>()

    var steps by mutableStateOf<List<TutorialStep>>(emptyList())
        private set

    var currentIndex by mutableIntStateOf(0)
        private set

    var isActive by mutableStateOf(false)
        private set

    private var onFinished: (() -> Unit)? = null

    val currentStep: TutorialStep?
        get() = steps.getOrNull(currentIndex)

    val isLastStep: Boolean
        get() = currentIndex >= steps.lastIndex

    /** Starts the tutorial. [onFinished] is invoked once when it ends (completed or skipped). */
    fun start(steps: List<TutorialStep>, onFinished: () -> Unit) {
        if (isActive || steps.isEmpty()) return
        this.steps = steps
        this.onFinished = onFinished
        currentIndex = 0
        isActive = true
    }

    /** Advances to the next step, or finishes if on the last one. */
    fun next() {
        if (!isActive) return
        if (isLastStep) finish()
        else currentIndex++
    }

    /** Ends the tutorial immediately (skip or done) and notifies the listener. */
    fun finish() {
        if (!isActive) return
        isActive = false
        currentIndex = 0
        onFinished?.invoke()
        onFinished = null
    }
}

@Composable
fun rememberTutorialState(): TutorialState = remember { TutorialState() }

/**
 * Registers this composable as a tutorial target under [key], reporting its
 * bounds so a spotlight can be drawn around it.
 */
fun Modifier.tutorialTarget(state: TutorialState, key: String): Modifier =
    this.onGloballyPositioned { coordinates ->
        state.targets[key] = coordinates.boundsInRoot()
    }
