package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.service.LiveKitAgentCoordinator
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.PremiumCard
import com.example.ui.components.ReadinessRing
import com.example.ui.components.SectionHeader
import com.example.ui.components.ShimmerBox
import com.example.ui.components.StatCard
import com.example.ui.components.StatusChip
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberText
import com.example.ui.theme.DarkBackground
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    prefs: PreferencesManager,
    coordinator: LiveKitAgentCoordinator,
    onStartPractice: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val scrollState = rememberScrollState()
    val uiState by coordinator.uiState.collectAsState()
    val report = uiState.homeReport
    val driverName = report?.name ?: prefs.driverName

    val haptic = LocalHapticFeedback.current
    val talkInteractionSource = remember { MutableInteractionSource() }
    val isTalkPressed by talkInteractionSource.collectIsPressedAsState()

    // Soft pulsing glow around hero button
    val infiniteTransition = rememberInfiniteTransition(label = "hero_glow")
    val heroGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.32f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_glow_alpha"
    )

    val heroScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isTalkPressed) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "hero_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("home_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP APP BAR: Brand + Settings Icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, RickshawYellowLight),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Logo",
                            tint = RickshawYellow,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "METER CHALU",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = RickshawYellow,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "भाषा चालू, मीटर चालू!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = DarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.size(44.dp)
            ) {
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("btn_settings")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = PureWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // REFINED GREETING HEADER
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "नमस्ते $driverName भाई!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = PureWhite,
                textAlign = TextAlign.Center,
                lineHeight = 38.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "मराठी सराव करण्यासाठी तयार आहात का?",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }

        // "आपकी याद आई!" Warm Inviting Banner (when days_since_last >= 2)
        if (report != null && report.days_since_last >= 2) {
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                color = RickshawAmberSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, RickshawAmberBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Warm welcome",
                        tint = AmberAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "आपकी याद आई! आज एक फेरी बोलकर देखें.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // HERO "बात करें" BUTTON: Large Yellow Gradient, Pulsing Glow, Mic Icon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(heroScale),
            contentAlignment = Alignment.Center
        ) {
            // Soft pulsing glow
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(98.dp)
            ) {
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            RickshawYellow.copy(alpha = heroGlowAlpha),
                            Color.Transparent
                        ),
                        radius = size.width / 1.8f
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(28.dp.toPx())
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .testTag("btn_start_talk")
                    .clickable(
                        interactionSource = talkInteractionSource,
                        indication = null,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onStartPractice()
                        }
                    ),
                color = Color.Transparent,
                shape = RoundedCornerShape(26.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, RickshawYellowLight)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(YellowGradient)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = RickshawBlack,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Start Call",
                                    tint = RickshawYellow,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "बात करें (Start Talk)",
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Black,
                                color = RickshawBlack,
                                lineHeight = 28.sp
                            )
                            Text(
                                text = "हँड्स-फ्री मराठी सराव • बोलून शिका",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = RickshawBlack.copy(alpha = 0.8f),
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // REPORT / SKELETON LOADING / EMPTY STATE
        if (uiState.isReportLoading) {
            // Skeleton Shimmer Loading Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ShimmerBox(height = 100.dp, shape = RoundedCornerShape(20.dp))
                ShimmerBox(height = 80.dp, shape = RoundedCornerShape(18.dp))
                ShimmerBox(height = 140.dp, shape = RoundedCornerShape(18.dp))
            }
        } else if (report != null && (report.sessions > 0 || prefs.cachedReportJson != null || prefs.isMockMode)) {
            // SCROLLABLE REPORT STACK
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. READINESS CARD: Ring + Percent + Note
                PremiumCard(borderColor = DarkBorder) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReadinessRing(
                            percent = report.percent,
                            sizeDp = 96,
                            strokeWidthDp = 11
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "मराठी सराव तयारी",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite,
                                lineHeight = 24.sp
                            )
                            Text(
                                text = if (report.ready) "चाचणीसाठी तयार (Ready!)" else "सराव चालू ठेवा (Keep Going)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (report.ready) GreenAccent else RickshawYellow
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = report.note,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                // 2. "आज तक" STATS ROW: Sessions, Streak, Closeness
                PremiumCard(borderColor = DarkBorder) {
                    SectionHeader(
                        title = "आज तक (Overall Progress)",
                        accentColor = RickshawYellow
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = "फेऱ्या\n(Sessions)",
                            value = "${report.sessions}",
                            modifier = Modifier.weight(1f),
                            valueColor = PureWhite
                        )
                        StatCard(
                            title = "लगातार\n(Streak)",
                            value = "${report.streak_days}d",
                            icon = Icons.Default.LocalFireDepartment,
                            modifier = Modifier.weight(1f),
                            valueColor = AmberAccent
                        )
                        StatCard(
                            title = "उच्चार अचूकता\n(Accuracy)",
                            value = "${report.closeness_avg_recent}%",
                            trendIcon = if (report.closeness_trend > 0) Icons.Default.TrendingUp else Icons.Default.TrendingFlat,
                            trendPositive = report.closeness_trend > 0,
                            modifier = Modifier.weight(1.1f),
                            valueColor = GreenAccent
                        )
                    }
                }

                // 3. "इन्हें ठीक करो" (Phrases to Polish)
                if (report.fix_these.isNotEmpty()) {
                    PremiumCard(borderColor = DarkBorder) {
                        SectionHeader(
                            title = "इन्हें ठीक करो (Phrases to Polish):",
                            icon = Icons.Default.Lightbulb,
                            accentColor = RickshawYellow
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        report.fix_these.forEach { item ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                color = DarkSurfaceElevated,
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Marathi Phrase in large font
                                        Text(
                                            text = item.right,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Black,
                                            color = PureWhite,
                                            lineHeight = 30.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = RickshawAmberSurface,
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, RickshawAmberBorder)
                                        ) {
                                            Text(
                                                text = "${item.times}x",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AmberText,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "मतलब: ${item.meaning}",
                                        fontSize = 14.sp,
                                        color = TextSecondary,
                                        lineHeight = 20.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    // Pronunciation Tip in soft green card
                                    Surface(
                                        color = RickshawGreenSurface,
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, RickshawGreenBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "💡 सल्ला: ${item.tip}",
                                            fontSize = 13.sp,
                                            color = GreenText,
                                            fontWeight = FontWeight.Medium,
                                            lineHeight = 19.sp,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. "ठीक हो गया" (Fixed Celebratory Banner)
                if (report.fixed_count > 0) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = RickshawGreenSurface,
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RickshawGreenBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GreenAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "ठीक हो गया: तुम्ही ${report.fixed_count} वाक्य अचूक बोलणे शिकलात!",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // 5. "मज़बूत" (Strong in Scenarios as Green Chips)
                if (report.strong_in.isNotEmpty()) {
                    PremiumCard(borderColor = DarkBorder) {
                        SectionHeader(
                            title = "मज़बूत (Strong Scenarios):",
                            icon = Icons.Default.Star,
                            accentColor = RickshawYellow
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            report.strong_in.forEach { scId ->
                                val label = when (scId) {
                                    "dadar" -> "दादर स्टेशन (Dadar)"
                                    "fare" -> "भाडे ठरवणे (Fare)"
                                    "directions" -> "रस्ता दाखवणे (Directions)"
                                    "elderly" -> "ज्येष्ठ नागरिक (Elderly)"
                                    "rto" -> "आरटीओ मॉक (RTO)"
                                    else -> scId
                                }
                                StatusChip(
                                    text = label,
                                    isSuccess = true,
                                    icon = Icons.Default.Check
                                )
                            }
                        }
                    }
                }

                // 6. "पिछली बार" (Last Practice Note + Recent Sessions)
                PremiumCard(borderColor = DarkBorder) {
                    SectionHeader(
                        title = "पिछली बार (Last Practice):",
                        icon = Icons.Default.History,
                        accentColor = RickshawYellow
                    )

                    if (report.carry_forward.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "“${report.carry_forward}”",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RickshawYellow,
                            lineHeight = 22.sp
                        )
                    }

                    if (report.recent.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        report.recent.take(3).forEach { rec ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${rec.date} • ${rec.scenario}",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                                Surface(
                                    color = DarkSurfaceElevated,
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle)
                                ) {
                                    Text(
                                        text = "${rec.score}% गुण",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenAccent,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Friendly Empty State: "पहली बातचीत करते हैं!"
            EmptyStateCard(
                title = "पहली बातचीत करते हैं!",
                description = "वरती 'बात करें' बटणावर टॅप करा आणि साथीसोबत मराठी सराव सुरू करा."
            )
        }
    }
}
