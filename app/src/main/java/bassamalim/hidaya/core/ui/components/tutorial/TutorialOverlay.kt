package bassamalim.hidaya.core.ui.components.tutorial

import androidx.compose.animation.core.animateRectAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.MyTextButton
import kotlin.math.max

/**
 * Full-screen interactive coach-mark overlay. Place it as the last child of a
 * screen's root `Box` (so it draws above everything) and drive it with a
 * [TutorialState]. It dims the screen, spotlights the current step's target,
 * and shows a callout with the instruction plus navigation controls.
 */
@Composable
fun TutorialOverlay(
    state: TutorialState,
    modifier: Modifier = Modifier
) {
    if (!state.isActive) return
    val step = state.currentStep ?: return

    val density = LocalDensity.current

    // The overlay may be nested below a top app bar, while targets report their
    // bounds in the composition root. Track our own offset from root so we can
    // translate target bounds into the overlay's local coordinate space.
    var overlayOffset by remember { mutableStateOf(Offset.Zero) }
    val targetRectInRoot = step.targetKey?.let { state.targets[it] }
    val hasTarget = targetRectInRoot != null
    val targetRect = targetRectInRoot?.translate(-overlayOffset.x, -overlayOffset.y)

    val cutoutPadding = with(density) { 8.dp.toPx() }
    val cornerRadius = with(density) { 12.dp.toPx() }
    val scrimColor = Color.Black.copy(alpha = 0.78f)

    // Animate the spotlight as it moves between steps.
    val animatedRect by animateRectAsState(
        targetValue = targetRect ?: Rect.Zero,
        label = "spotlight"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { overlayOffset = it.positionInRoot() }
    ) {
        // Dim scrim with a spotlight cutout punched out of it.
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .pointerInput(state.currentIndex) {
                    detectTapGestures { state.next() }
                }
        ) {
            drawRect(color = scrimColor)

            if (hasTarget) {
                val inflated = Rect(
                    left = animatedRect.left - cutoutPadding,
                    top = animatedRect.top - cutoutPadding,
                    right = animatedRect.right + cutoutPadding,
                    bottom = animatedRect.bottom + cutoutPadding
                )
                when (step.shape) {
                    TutorialShape.Circle -> drawCircle(
                        color = Color.Transparent,
                        radius = max(inflated.width, inflated.height) / 2f,
                        center = inflated.center,
                        blendMode = BlendMode.Clear
                    )
                    TutorialShape.RoundedRect -> drawRoundRect(
                        color = Color.Transparent,
                        topLeft = inflated.topLeft,
                        size = inflated.size,
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        blendMode = BlendMode.Clear
                    )
                }
            }
        }

        Callout(state = state, targetRect = targetRect)
    }
}

@Composable
private fun Callout(state: TutorialState, targetRect: Rect?) {
    val density = LocalDensity.current
    val spacing = 24.dp

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val screenHeightPx = with(density) { maxHeight.toPx() }
        // Rough room the callout needs; if neither side has it, the target is
        // (near) full-screen, so we just anchor the card to the bottom edge.
        val minRoomPx = with(density) { 180.dp.toPx() }

        val alignment: Alignment
        val padding: Modifier
        when {
            targetRect == null -> {
                alignment = Alignment.Center
                padding = Modifier
            }
            (screenHeightPx - targetRect.bottom) >= minRoomPx &&
                (screenHeightPx - targetRect.bottom) >= targetRect.top -> {
                // More room below the target → place the callout below it.
                alignment = Alignment.TopCenter
                val top = with(density) { targetRect.bottom.toDp() } + spacing
                padding = Modifier.padding(top = top.coerceAtLeast(0.dp))
            }
            targetRect.top >= minRoomPx -> {
                // More room above the target → place the callout above it.
                alignment = Alignment.BottomCenter
                val bottom = with(density) { (screenHeightPx - targetRect.top).toDp() } + spacing
                padding = Modifier.padding(bottom = bottom.coerceAtLeast(0.dp))
            }
            else -> {
                // Target fills most of the screen → anchor to the bottom edge.
                alignment = Alignment.BottomCenter
                padding = Modifier.padding(bottom = spacing)
            }
        }

        Surface(
            modifier = Modifier
                .align(alignment)
                .padding(horizontal = 24.dp)
                .then(padding)
                .widthIn(max = 360.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 3.dp,
            shadowElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                MyText(
                    text = state.currentStep?.text.orEmpty(),
                    fontSize = 17.sp
                )

                StepIndicators(
                    count = state.steps.size,
                    current = state.currentIndex,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 14.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!state.isLastStep) {
                        MyTextButton(
                            text = stringResource(R.string.tutorial_skip),
                            textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = state::finish
                        )
                    } else {
                        // Keep the layout balanced when there is no Skip button.
                        Box(Modifier) {}
                    }

                    MyTextButton(
                        text = stringResource(
                            if (state.isLastStep) R.string.tutorial_done else R.string.next
                        ),
                        fontWeight = FontWeight.Bold,
                        onClick = state::next
                    )
                }
            }
        }
    }
}

@Composable
private fun StepIndicators(count: Int, current: Int, modifier: Modifier = Modifier) {
    if (count <= 1) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            val isCurrent = index == current
            Box(
                Modifier
                    .size(if (isCurrent) 10.dp else 8.dp)
                    .background(
                        color = if (isCurrent) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
            )
        }
    }
}
