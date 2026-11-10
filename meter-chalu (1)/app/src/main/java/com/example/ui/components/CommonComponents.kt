package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberText
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderHighlight
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
import com.example.ui.theme.RickshawDarkSurface
import com.example.ui.theme.RickshawGreen
import com.example.ui.theme.RickshawGreenBorder
import com.example.ui.theme.RickshawGreenSurface
import com.example.ui.theme.RickshawYellow
import com.example.ui.theme.RickshawYellowDark
import com.example.ui.theme.RickshawYellowLight
import com.example.ui.theme.RoadGray
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YellowGradient

// ============================================================================
// Premium Reusable Components (M3 Dark + Yellow Auto-Rickshaw Identity)
// ============================================================================

/**
 * High-touch tactile Primary Button with warm yellow gradient, press spring animation,
 * and haptic feedback. Minimum 56dp touch height.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    subText: String? = null,
    enabled: Boolean = true,
    testTag: String = "primary_button"
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "btn_press_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .height(if (subText != null) 68.dp else 58.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            )
            .testTag(testTag),
        color = Color.Transparent,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, RickshawYellowLight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (enabled) YellowGradient
                    else Brush.horizontalGradient(listOf(RoadGray, DarkSurface))
                )
                .padding(horizontal = 20.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) RickshawBlack else TextMuted,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = text,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = if (enabled) RickshawBlack else TextMuted,
                        lineHeight = 24.sp
                    )
                    if (subText != null) {
                        Text(
                            text = subText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (enabled) RickshawBlack.copy(alpha = 0.75f) else TextMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Secondary Button with layered dark background and subtle 1dp border.
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    testTag: String = "secondary_button"
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "sec_btn_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .height(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .testTag(testTag),
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )
        }
    }
}

/**
 * Card with clean 1dp border, soft depth, and rounded corners (20dp).
 */
@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    borderColor: Color = DarkBorder,
    backgroundColor: Color = DarkSurface,
    cornerRadius: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        color = backgroundColor,
        shape = RoundedCornerShape(cornerRadius),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}

/**
 * Animated Readiness Ring with smooth sweep and large centered percentage.
 */
@Composable
fun ReadinessRing(
    percent: Int,
    modifier: Modifier = Modifier,
    sizeDp: Int = 110,
    strokeWidthDp: Int = 12
) {
    val isReady = percent >= 70
    val activeColor = if (isReady) GreenAccent else RickshawYellow
    val animatedPercent by animateFloatAsState(
        targetValue = percent.coerceIn(0, 100) / 100f,
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "readiness_ring_anim"
    )

    Box(
        modifier = modifier.size(sizeDp.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp.dp)) {
            val stroke = Stroke(width = strokeWidthDp.dp.toPx(), cap = StrokeCap.Round)
            // Track background
            drawArc(
                color = RoadGray,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = stroke
            )
            // Dynamic Progress
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        activeColor.copy(alpha = 0.85f),
                        activeColor,
                        activeColor
                    )
                ),
                startAngle = 135f,
                sweepAngle = 270f * animatedPercent,
                useCenter = false,
                style = stroke
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$percent%",
                fontSize = (sizeDp * 0.28).sp,
                fontWeight = FontWeight.Black,
                color = PureWhite,
                lineHeight = (sizeDp * 0.32).sp
            )
            Text(
                text = if (isReady) "तयार" else "सराव",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = activeColor
            )
        }
    }
}

/**
 * Backward-compatible ReadinessGauge wrapping the modern ReadinessRing.
 */
@Composable
fun ReadinessGauge(
    percent: Int,
    modifier: Modifier = Modifier,
    sizeDp: Int = 180,
    strokeWidthDp: Int = 18
) {
    ReadinessRing(
        percent = percent,
        modifier = modifier.testTag("readiness_gauge"),
        sizeDp = sizeDp,
        strokeWidthDp = strokeWidthDp
    )
}

/**
 * Stat Card for "आज तक" with clear numbers and trend indicators.
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    trendIcon: ImageVector? = null,
    trendPositive: Boolean = true,
    valueColor: Color = PureWhite
) {
    Surface(
        modifier = modifier,
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = valueColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = valueColor
                )
                if (trendIcon != null) {
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = trendIcon,
                        contentDescription = null,
                        tint = if (trendPositive) GreenAccent else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Section Header with icon, clean title and subtle divider line.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accentColor: Color = RickshawYellow
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = PureWhite
        )
    }
}

/**
 * Status Chip with tinted containers (Green, Amber, or Dark Neutral).
 */
@Composable
fun StatusChip(
    text: String,
    modifier: Modifier = Modifier,
    isSuccess: Boolean = true,
    icon: ImageVector? = null
) {
    val bgColor = if (isSuccess) RickshawGreenSurface else RickshawAmberSurface
    val borderCol = if (isSuccess) RickshawGreenBorder else RickshawAmberBorder
    val textCol = if (isSuccess) GreenText else AmberText

    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderCol)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textCol,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = textCol
            )
        }
    }
}

/**
 * Shimmer Loading placeholder box for skeleton states.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    height: Dp = 20.dp,
    shape: RoundedCornerShape = RoundedCornerShape(10.dp)
) {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    val shimmerColors = listOf(
        DarkSurfaceElevated,
        DarkBorder,
        DarkSurfaceElevated
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 500f, translateAnim - 500f),
        end = Offset(translateAnim, translateAnim)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(brush)
    )
}

/**
 * Clean vector illustration of auto-rickshaw + microphone soundwaves for empty states.
 */
@Composable
fun StylizedAutoIllustration(
    modifier: Modifier = Modifier,
    sizeDp: Int = 110
) {
    Box(
        modifier = modifier.size(sizeDp.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)

            // Outer soft glow
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(RickshawYellow.copy(alpha = 0.15f), Color.Transparent),
                    center = center,
                    radius = size.width / 2f
                ),
                radius = size.width / 2f
            )

            // Stylized Auto Hood Arch
            drawArc(
                color = RickshawYellow,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            // Wheels
            drawCircle(
                color = RoadGray,
                radius = 12.dp.toPx(),
                center = Offset(size.width * 0.3f, size.height * 0.72f)
            )
            drawCircle(
                color = RickshawYellow,
                radius = 5.dp.toPx(),
                center = Offset(size.width * 0.3f, size.height * 0.72f)
            )
            drawCircle(
                color = RoadGray,
                radius = 12.dp.toPx(),
                center = Offset(size.width * 0.7f, size.height * 0.72f)
            )
            drawCircle(
                color = RickshawYellow,
                radius = 5.dp.toPx(),
                center = Offset(size.width * 0.7f, size.height * 0.72f)
            )
        }

        Icon(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = null,
            tint = RickshawYellow,
            modifier = Modifier.size((sizeDp * 0.42).dp)
        )
    }
}

/**
 * Welcoming empty state card with vector illustration and encouraging copy.
 */
@Composable
fun EmptyStateCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DarkSurface,
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StylizedAutoIllustration(sizeDp = 100)
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = PureWhite,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 15.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}

/**
 * Backward compatibility for LargeActionButton
 */
@Composable
fun LargeActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = DarkSurface,
    contentColor: Color = PureWhite,
    borderColor: Color = DarkBorder,
    testTag: String = "large_btn"
) {
    Surface(
        modifier = modifier
            .height(58.dp)
            .testTag(testTag)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
