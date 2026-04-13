package bassamalim.hidaya.features.quiz.result

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MySurface
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.theme.Negative
import bassamalim.hidaya.core.ui.theme.Positive

@Composable
fun QuizResultScreen(viewModel: QuizResultViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) return

    MyScaffold(title = stringResource(R.string.quiz_result)) { padding ->
        MyLazyColumn(
            modifier = Modifier.padding(padding),
            lazyList = {
                item {
                    ScoreHeader(
                        score = state.score,
                        correctCount = state.questions.count {
                            it.chosenAnswerId == it.answers.indexOfFirst { a -> a.isCorrect }
                        },
                        totalCount = state.questions.size
                    )
                }

                item { Spacer(Modifier.height(16.dp)) }

                itemsIndexed(state.questions) { index, item ->
                    QuestionCard(
                        question = item,
                        questionIndex = index + 1
                    )
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        )
    }
}

@Composable
private fun ScoreHeader(score: String, correctCount: Int, totalCount: Int) {
    val scoreInt = score.toIntOrNull() ?: 0
    val scoreColor = when {
        scoreInt >= 80 -> Positive
        scoreInt >= 50 -> MaterialTheme.colorScheme.tertiary
        else -> Negative
    }

    var targetProgress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1000),
        label = "score_animation"
    )

    LaunchedEffect(scoreInt) {
        targetProgress = scoreInt / 100f
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.size(140.dp),
                color = scoreColor,
                strokeWidth = 12.dp,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                strokeCap = StrokeCap.Round
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MyText(
                    text = "${(animatedProgress * 100).toInt()}%",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            StatItem(
                label = stringResource(R.string.correct),
                value = correctCount.toString(),
                color = Positive
            )
            StatItem(
                label = stringResource(R.string.wrong),
                value = (totalCount - correctCount).toString(),
                color = Negative
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        MyText(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        MyText(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuestionCard(question: QuizResultQuestion, questionIndex: Int) {
    val isCorrect = question.chosenAnswerId == question.answers.indexOfFirst { it.isCorrect }

    MySurface(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCorrect) Positive.copy(alpha = 0.15f)
                                else Negative.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        MyText(
                            text = questionIndex.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCorrect) Positive else Negative
                        )
                    }

                    MyText(
                        text = stringResource(R.string.question),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (isCorrect) Positive else Negative
                )
            }

            Spacer(Modifier.height(12.dp))

            MyText(
                text = question.questionText,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start
            )

            Spacer(Modifier.height(16.dp))

            question.answers.forEachIndexed { index, answer ->
                AnswerItem(
                    answerText = answer.text,
                    isCorrectAnswer = answer.isCorrect,
                    isChosenAnswer = index == question.chosenAnswerId
                )

                if (index < question.answers.lastIndex) {
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun AnswerItem(
    answerText: String,
    isCorrectAnswer: Boolean,
    isChosenAnswer: Boolean
) {
    val backgroundColor = when {
        isCorrectAnswer -> Positive.copy(alpha = 0.12f)
        isChosenAnswer -> Negative.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MyText(
            text = answerText,
            modifier = Modifier.weight(1f),
            fontSize = 15.sp,
            textAlign = TextAlign.Start,
            color = when {
                isCorrectAnswer -> Positive
                isChosenAnswer -> Negative
                else -> MaterialTheme.colorScheme.onSurface
            }
        )

        if (isChosenAnswer || isCorrectAnswer) {
            Icon(
                imageVector = if (isCorrectAnswer) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(20.dp),
                tint = if (isCorrectAnswer) Positive else Negative
            )
        }
    }
}