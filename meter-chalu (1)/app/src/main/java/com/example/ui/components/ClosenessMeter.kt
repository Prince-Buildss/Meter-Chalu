package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClosenessData
import com.example.data.TargetSentence
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberText
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GreenAccent
import com.example.ui.theme.GreenText
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RickshawAmber
import com.example.ui.theme.RickshawAmberBorder
import com.example.ui.theme.RickshawAmberSurface
import com.example.ui.theme.RickshawBlack
import com.example.ui.theme.RickshawGreen
import com.example.ui.theme.RickshawGreenBorder
import com.example.ui.theme.RickshawGreenSurface
import com.example.ui.theme.RickshawYellow
import com.example.ui.theme.RickshawYellowLight
import com.example.ui.theme.RoadGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClosenessMeter(
    targetSentence: TargetSentence?,
    closenessData: ClosenessData?,
    isDriverSpeaking: Boolean,
    audioLevel: Float,
    modifier: Modifier = Modifier
) {
    if (targetSentence == null) return

    val haptic = LocalHapticFeedback.current
    val isMastered = closenessData != null && (closenessData.mastered || closenessData.score >= 75)

    // Trigger haptic tick on new mastered celebration
    LaunchedEffect(closenessData?.score) {
        if (isMastered) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val animatedScore by animateFloatAsState(
        targetValue = (closenessData?.score ?: 0).toFloat(),
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "closeness_score_anim"
    )

    // Sparkle transition for celebration
    val sparkleTransition = rememberInfiniteTransition(label = "sparkle_transition")
    val sparklePhase by sparkleTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle_anim"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("closeness_meter"),
        color = DarkSurface,
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isMastered) RickshawGreen else DarkBorder
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Success Sparkle Particles when mastered
            if (isMastered) {
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(22.dp))
                ) {
                    val colors = listOf(GreenAccent, RickshawYellow, PureWhite)
                    val particleOffsets = listOf(
                        Offset(size.width * 0.12f, size.height * 0.22f),
                        Offset(size.width * 0.88f, size.height * 0.18f),
                        Offset(size.width * 0.20f, size.height * 0.82f),
                        Offset(size.width * 0.82f, size.height * 0.85f),
                        Offset(size.width * 0.50f, size.height * 0.08f)
                    )
                    particleOffsets.forEachIndexed { i, pt ->
                        val radius = (3.dp.toPx() + (i * 1.5f * sparklePhase))
                        drawCircle(
                            color = colors[i % colors.size].copy(alpha = 0.6f * sparklePhase),
                            radius = radius,
                            center = pt
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // "Repeat this" label pill
                Surface(
                    color = DarkSurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(RickshawYellow)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "हे वाक्य बोला (Repeat this):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = RickshawYellow
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // The Marathi Target Sentence in largest type (28sp) with matra-safe line height (40sp)
                if (closenessData != null && closenessData.words.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        closenessData.words.forEach { word ->
                            val (bgColor, borderCol, textCol) = when (word.state) {
                                "great" -> Triple(RickshawGreenSurface, RickshawGreenBorder, GreenText)
                                "almost" -> Triple(RickshawAmberSurface, RickshawAmberBorder, AmberText)
                                else -> Triple(DarkSurfaceElevated, RoadGray, PureWhite) // miss = grey outline, never red!
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = bgColor,
                                border = androidx.compose.foundation.BorderStroke(1.dp, borderCol),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (word.state == "great") {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Great",
                                            tint = GreenText,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = word.w,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Black,
                                        color = textCol,
                                        lineHeight = 36.sp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = targetSentence.marathi,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = PureWhite,
                        lineHeight = 38.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Meaning in muted text below
                Text(
                    text = "मतलब: ${targetSentence.meaning}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                // Live Mic Equalizer when driver speaks
                if (isDriverSpeaking) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "माईक ऐकत आहे: ",
                            fontSize = 13.sp,
                            color = GreenAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        repeat(10) { idx ->
                            val heightFactor = ((idx % 4) + 1) / 4f
                            val barHeight = (6 + (audioLevel * 18 * heightFactor)).dp
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .width(3.5.dp)
                                    .height(barHeight)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(GreenAccent)
                            )
                        }
                    }
                }

                // Closeness score & progress bar
                if (closenessData != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkSurfaceElevated)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedScore / 100f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isMastered) Brush.horizontalGradient(listOf(GreenAccent, RickshawGreen))
                                    else Brush.horizontalGradient(listOf(RickshawYellowLight, AmberAccent))
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (isMastered) RickshawGreenSurface else RickshawAmberSurface,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isMastered) RickshawGreenBorder else RickshawAmberBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isMastered) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = GreenText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = if (isMastered) "शाबास! (${closenessData.score}%)" else "पुन्हा बोला (${closenessData.score}%)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMastered) GreenText else AmberText
                                )
                            }
                        }

                        if (closenessData.heard.isNotEmpty()) {
                            Text(
                                text = "ऐकले: “${closenessData.heard}”",
                                fontSize = 14.sp,
                                color = RickshawYellow,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
