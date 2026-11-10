package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.AgentCallState
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberText
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderHighlight
import com.example.ui.theme.GreenAccent
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RickshawAmber
import com.example.ui.theme.RickshawBlack
import com.example.ui.theme.RickshawGreen
import com.example.ui.theme.RickshawYellow
import com.example.ui.theme.RickshawYellowDark
import com.example.ui.theme.RickshawYellowLight
import com.example.ui.theme.RoadGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ConversationOrb(
    state: AgentCallState,
    audioLevel: Float,
    helperLanguage: String = "hi",
    modifier: Modifier = Modifier,
    orbSizeDp: Int = 144
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_infinite")

    // Slow rotation for thinking
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    // Breathing pulse for listening and connecting
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_pulse"
    )

    // Soft outer halo breathing
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_pulse"
    )

    // Audio-reactive scale smoothing
    val reactiveAudioScale by animateFloatAsState(
        targetValue = 1.0f + (audioLevel.coerceIn(0f, 1f) * 0.38f),
        animationSpec = tween(durationMillis = 100, easing = LinearEasing),
        label = "audio_reactive_scale"
    )

    val (stateWord, wordColor) = when (state) {
        AgentCallState.CONNECTING -> Pair(if (helperLanguage == "en") "Connecting..." else "जोडत आहे...", TextSecondary)
        AgentCallState.LISTENING -> Pair(if (helperLanguage == "en") "Listening" else "ऐकत आहे...", RickshawYellow)
        AgentCallState.THINKING -> Pair(if (helperLanguage == "en") "Thinking" else "विचार करत आहे...", AmberAccent)
        AgentCallState.SPEAKING -> Pair(if (helperLanguage == "en") "Speaking" else "बोलत आहे...", RickshawYellowLight)
        AgentCallState.ERROR -> Pair(if (helperLanguage == "en") "Check Connection" else "पुन्हा प्रयत्न करा", AmberText)
        else -> Pair(if (helperLanguage == "en") "Ready" else "तयार", TextPrimary)
    }

    Column(
        modifier = modifier.testTag("conversation_orb"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size((orbSizeDp * 1.5).dp),
            contentAlignment = Alignment.Center
        ) {
            // Ambient Radial Yellow Glow Behind the Orb
            Canvas(modifier = Modifier.size((orbSizeDp * 1.5).dp)) {
                val glowAlpha = when (state) {
                    AgentCallState.SPEAKING -> 0.28f + (audioLevel * 0.25f)
                    AgentCallState.LISTENING -> haloPulse
                    AgentCallState.THINKING -> 0.16f
                    AgentCallState.CONNECTING -> 0.12f
                    else -> 0.08f
                }
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            RickshawYellow.copy(alpha = glowAlpha),
                            RickshawYellowDark.copy(alpha = glowAlpha * 0.5f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = size.width / 2f
                    ),
                    radius = size.width / 2f
                )
            }

            when (state) {
                AgentCallState.LISTENING -> {
                    // Soft expanding concentric rings
                    Canvas(
                        modifier = Modifier
                            .size((orbSizeDp * 1.35).dp)
                            .scale(breathingPulse)
                    ) {
                        drawCircle(
                            color = RickshawYellow.copy(alpha = 0.12f),
                            radius = size.minDimension / 2f
                        )
                        drawCircle(
                            color = RickshawYellow.copy(alpha = 0.24f),
                            radius = size.minDimension / 2.5f
                        )
                    }

                    // Main Glowing Yellow Center Core
                    Box(
                        modifier = Modifier
                            .size(orbSizeDp.dp)
                            .scale(breathingPulse * reactiveAudioScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        RickshawYellowLight,
                                        RickshawYellow,
                                        RickshawYellowDark
                                    )
                                )
                            )
                    )
                }

                AgentCallState.THINKING -> {
                    // Smooth rotating sweep orbit
                    Canvas(
                        modifier = Modifier
                            .size((orbSizeDp * 1.15).dp)
                            .rotate(spinAngle)
                    ) {
                        val stroke = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(
                                    RickshawYellowLight,
                                    AmberAccent,
                                    Color.Transparent
                                )
                            ),
                            startAngle = 0f,
                            sweepAngle = 260f,
                            useCenter = false,
                            style = stroke
                        )
                    }

                    // Pulsating Warm Core
                    Box(
                        modifier = Modifier
                            .size((orbSizeDp * 0.78).dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(AmberAccent, RickshawAmber, Color(0xFFC67D00))
                                )
                            )
                    )
                }

                AgentCallState.SPEAKING -> {
                    // Dynamic ripple rings vibrating to WebRTC audio energy
                    Canvas(
                        modifier = Modifier
                            .size((orbSizeDp * 1.45).dp)
                            .scale(reactiveAudioScale)
                    ) {
                        val ringAlpha = (0.22f + (audioLevel * 0.35f)).coerceIn(0.1f, 0.6f)
                        drawCircle(
                            color = RickshawYellow.copy(alpha = ringAlpha * 0.6f),
                            radius = size.minDimension / 2f,
                            style = Stroke(width = (3 + (audioLevel * 6)).dp.toPx())
                        )
                        drawCircle(
                            color = RickshawYellowLight.copy(alpha = ringAlpha),
                            radius = size.minDimension / 2.4f,
                            style = Stroke(width = (4 + (audioLevel * 6)).dp.toPx())
                        )
                    }

                    // High-energy radiant core with subtle white specular highlight
                    Box(
                        modifier = Modifier
                            .size(orbSizeDp.dp)
                            .scale(reactiveAudioScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        PureWhite,
                                        RickshawYellowLight,
                                        RickshawYellow,
                                        RickshawYellowDark
                                    )
                                )
                            )
                    )
                }

                AgentCallState.CONNECTING -> {
                    // Gentle breathing connecting circle
                    Canvas(
                        modifier = Modifier
                            .size((orbSizeDp * 0.95).dp)
                            .scale(breathingPulse)
                    ) {
                        drawCircle(
                            color = DarkBorderHighlight,
                            radius = size.minDimension / 2f,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size((orbSizeDp * 0.7).dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(DarkBorder, RoadGray, RickshawBlack)
                                )
                            )
                    )
                }

                AgentCallState.ERROR -> {
                    Box(
                        modifier = Modifier
                            .size((orbSizeDp * 0.75).dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(AmberAccent, RickshawAmber, RickshawBlack)
                                )
                            )
                    )
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .size((orbSizeDp * 0.75).dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(RoadGray, RickshawBlack)
                                )
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // State indicator: concise, bold, calm typography
        Text(
            text = stateWord,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = wordColor,
            lineHeight = 28.sp
        )
    }
}
