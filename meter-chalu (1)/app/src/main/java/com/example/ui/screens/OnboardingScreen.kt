package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.ui.components.PrimaryButton
import com.example.ui.i18n.I18nManager
import com.example.ui.i18n.SupportedLanguage
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderHighlight
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RickshawBlack
import com.example.ui.theme.RickshawGreen
import com.example.ui.theme.RickshawYellow
import com.example.ui.theme.RickshawYellowLight
import com.example.ui.theme.RoadGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun OnboardingScreen(
    prefs: PreferencesManager,
    onComplete: () -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1: Language, 2: Name
    var selectedLang by remember { mutableStateOf(prefs.helperLanguage) }
    var enteredName by remember { mutableStateOf(prefs.driverName) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 16.dp)
            .testTag("onboarding_screen")
    ) {
        // Top Bar: Branding + Pill Progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, RickshawYellowLight),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = RickshawYellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "मीटर चालू",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = RickshawYellow
                    )
                    Text(
                        text = "भाषा चालू, मीटर चालू!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }
            }

            // Pill progress indicator
            Surface(
                color = DarkSurfaceElevated,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(RickshawYellow)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "पाऊल $step / 2",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large stylized vector illustration (rickshaw + speech wave elements)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(160.dp, 100.dp)) {
                // Background soft radial amber glow
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(RickshawYellow.copy(alpha = 0.16f), Color.Transparent),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = size.width / 2f
                    ),
                    radius = size.width / 2f
                )

                // Speech waves / sound arcs
                val arcStroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                drawArc(
                    color = RickshawYellow.copy(alpha = 0.5f),
                    startAngle = -45f,
                    sweepAngle = 90f,
                    useCenter = false,
                    style = arcStroke,
                    topLeft = Offset(size.width * 0.65f, size.height * 0.15f),
                    size = androidx.compose.ui.geometry.Size(40.dp.toPx(), 40.dp.toPx())
                )
                drawArc(
                    color = RickshawYellowLight,
                    startAngle = -45f,
                    sweepAngle = 90f,
                    useCenter = false,
                    style = arcStroke,
                    topLeft = Offset(size.width * 0.75f, size.height * 0.05f),
                    size = androidx.compose.ui.geometry.Size(60.dp.toPx(), 60.dp.toPx())
                )
            }

            Surface(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(2.dp, RickshawYellow, CircleShape),
                color = DarkSurfaceElevated
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (step == 1) Icons.Default.DirectionsCar else Icons.Default.Person,
                        contentDescription = null,
                        tint = RickshawYellow,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Animated smooth horizontal transition between steps
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> width } + fadeOut()
                }
            },
            modifier = Modifier.weight(1f),
            label = "onboarding_steps"
        ) { currentStep ->
            if (currentStep == 1) {
                // STEP 1: Helper Language
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "आपकी मददगार भाषा चुनें",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = PureWhite,
                        lineHeight = 34.sp
                    )
                    Text(
                        text = "साथी आपको इसी भाषा में समझाएगा (Hindi is pre-selected):",
                        fontSize = 15.sp,
                        color = TextSecondary,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(I18nManager.SUPPORTED_LANGUAGES) { lang ->
                            val isSelected = selectedLang == lang.code
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) DarkSurfaceElevated else DarkSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) RickshawYellow else DarkBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { selectedLang = lang.code }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = lang.nativeName,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) RickshawYellow else PureWhite
                                        )
                                        Text(
                                            text = lang.englishName,
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    if (isSelected) {
                                        Surface(
                                            shape = CircleShape,
                                            color = RickshawYellow,
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = RickshawBlack,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    PrimaryButton(
                        text = "आगे बढ़ें (Next)",
                        icon = Icons.Default.ArrowForward,
                        onClick = {
                            prefs.helperLanguage = selectedLang
                            step = 2
                        },
                        testTag = "btn_onboarding_next"
                    )
                }
            } else {
                // STEP 2: Driver Name
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "आपका नाम क्या है?",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = PureWhite,
                        lineHeight = 34.sp
                    )
                    Text(
                        text = "साथी आपको इसी नाम से बुलाएगा (उदा. राजू भाई):",
                        fontSize = 15.sp,
                        color = TextSecondary,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                    )

                    OutlinedTextField(
                        value = enteredName,
                        onValueChange = { enteredName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_driver_name"),
                        label = { Text("चालक का नाम (Driver Name)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RickshawYellow,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            focusedLabelColor = RickshawYellow,
                            unfocusedLabelColor = TextSecondary,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Surface(
                        color = DarkSurface,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "💡 टिप:",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = RickshawYellow
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "मराठी शिकताना 'नमस्ते भाई' अशा संभाषणाने सराव सुरू होईल. तुम्ही कधीही सेटिंग्जमध्ये नाव बदलू शकता.",
                                fontSize = 14.sp,
                                color = TextSecondary,
                                lineHeight = 21.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    PrimaryButton(
                        text = "सुरू करा (Start Practice)",
                        subText = "भाषा चालू, मीटर चालू!",
                        onClick = {
                            prefs.driverName = enteredName.ifBlank { "ड्राइवर" }
                            prefs.isFirstLaunchCompleted = true
                            onComplete()
                        },
                        testTag = "btn_onboarding_finish"
                    )
                }
            }
        }
    }
}
