package bassamalim.hidaya.core.ui.components.tutorial

/**
 * Shape of the spotlight cutout drawn around a tutorial target.
 */
enum class TutorialShape { RoundedRect, Circle }

/**
 * A single step in an interactive coach-mark tutorial.
 *
 * @param targetKey key of the [tutorialTarget] to spotlight, or `null` for a
 *   centered step with no highlighted element (e.g. a general tip).
 * @param text the instruction shown in the callout bubble.
 * @param shape the shape of the spotlight cutout.
 */
data class TutorialStep(
    val text: String,
    val targetKey: String? = null,
    val shape: TutorialShape = TutorialShape.RoundedRect
)
