package com.example.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.PhrasebookRepository
import com.example.data.PreferencesManager
import com.example.service.LiveKitAgentCoordinator
import com.example.ui.components.PrimaryButton
import com.example.ui.components.SecondaryButton
import com.example.ui.components.SectionHeader
import com.example.ui.i18n.I18nManager
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
import java.util.Locale

@Composable
fun SettingsDialog(
    prefs: PreferencesManager,
    coordinator: LiveKitAgentCoordinator,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var name by remember { mutableStateOf(prefs.driverName) }
    var selectedLang by remember { mutableStateOf(prefs.helperLanguage) }
    var isMock by remember { mutableStateOf(prefs.isMockMode) }
    var backendUrl by remember { mutableStateOf(prefs.backendUrl) }
    var showPhrasebookSection by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var testInProgress by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        var ttsEngine: TextToSpeech? = null
        ttsEngine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsEngine?.language = Locale("mr", "IN")
            }
        }
        tts = ttsEngine
        onDispose {
            ttsEngine?.stop()
            ttsEngine?.shutdown()
        }
    }

    val scrollState = rememberScrollState()
    val isNotHttps = backendUrl.isNotEmpty() && !backendUrl.startsWith("https://")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(26.dp))
                .testTag("settings_dialog"),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header with Close Button
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
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = RickshawYellow,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "सेटिंग्ज (Settings)",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = RickshawYellow
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = PureWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 1. PROFILE SECTION
                Surface(
                    color = DarkSurfaceElevated,
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader(title = "प्रोफाइल (Profile)", icon = Icons.Default.Person)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("चालक का नाम (Driver Name)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_driver_name"),
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
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "मददगार भाषा (Helper Language):",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Language pills row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            I18nManager.SUPPORTED_LANGUAGES.take(4).forEach { lang ->
                                val isSelected = selectedLang == lang.code
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) RickshawYellow else DarkSurface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) RickshawYellowLight else DarkBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedLang = lang.code }
                                ) {
                                    Text(
                                        text = lang.nativeName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) RickshawBlack else PureWhite,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. BACKEND CONFIGURATION
                Surface(
                    color = DarkSurfaceElevated,
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader(title = "बॅकएंड (Backend URL)", icon = Icons.Default.Lock)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = backendUrl,
                            onValueChange = {
                                backendUrl = it.trimEnd('/')
                                testResult = null
                            },
                            label = { Text("LiveKit Token Endpoint") },
                            placeholder = { Text("https://my-backend.example.com") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_backend_url"),
                            singleLine = true,
                            isError = isNotHttps,
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
                            shape = RoundedCornerShape(14.dp)
                        )

                        if (isNotHttps) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "चेतावणी: URL 'https://' ने सुरू व्हायला हवी.",
                                fontSize = 12.sp,
                                color = AmberAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Room: ${prefs.stableRoomName}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )

                            Button(
                                onClick = {
                                    testInProgress = true
                                    testResult = null
                                    coordinator.testBackendHealth(backendUrl) { ok, msg ->
                                        testInProgress = false
                                        testResult = if (ok) "✓ Backend Connected" else "✗ $msg"
                                    }
                                },
                                enabled = !testInProgress && !isNotHttps && backendUrl.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (testInProgress) "Testing..." else "Test Connection",
                                    fontSize = 12.sp,
                                    color = if (testInProgress) TextMuted else PureWhite
                                )
                            }
                        }

                        if (testResult != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val isSuccess = testResult?.startsWith("✓") == true
                            Surface(
                                color = if (isSuccess) RickshawGreenSurface else RickshawAmberSurface,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSuccess) RickshawGreenBorder else RickshawAmberBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = testResult ?: "",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSuccess) GreenText else AmberText,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. MODE: MOCK TOGGLE SWITCH
                Surface(
                    color = DarkSurfaceElevated,
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "मॉक मोड (Mock Mode)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "बॅकएंडशिवाय ऑन-डिव्हाइस चाचणीसाठी चालू करा",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }

                        Switch(
                            checked = isMock,
                            onCheckedChange = { isMock = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = RickshawBlack,
                                checkedTrackColor = RickshawYellow,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = DarkSurface
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. PHRASEBOOK SECTION (Reference)
                Surface(
                    color = DarkSurfaceElevated,
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPhrasebookSection = !showPhrasebookSection }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = null,
                                    tint = RickshawYellow,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "वाक्यसंग्रह (Phrasebook)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            }
                            Text(
                                text = if (showPhrasebookSection) "लपवा ▲" else "पहा ▼",
                                fontSize = 12.sp,
                                color = RickshawYellow
                            )
                        }

                        if (showPhrasebookSection) {
                            Spacer(modifier = Modifier.height(12.dp))
                            val phrases = PhrasebookRepository.PHRASES.take(6)
                            phrases.forEach { phrase ->
                                Surface(
                                    color = DarkSurface,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = phrase.marathi,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PureWhite
                                            )
                                            Text(
                                                text = phrase.meaningHindi,
                                                fontSize = 13.sp,
                                                color = TextSecondary
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                tts?.speak(
                                                    phrase.marathi,
                                                    TextToSpeech.QUEUE_FLUSH,
                                                    null,
                                                    phrase.id.toString()
                                                )
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.VolumeUp,
                                                contentDescription = "Listen",
                                                tint = RickshawYellow,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5. DATA PRIVACY: "मेरा डेटा मिटाओ"
                Surface(
                    color = DarkSurfaceElevated,
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "डेटा आणि गोपनीयता (Privacy)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "तुमच्या सराव फेऱ्यांचा डेटा मिटवण्यासाठी खालील पर्याय वापरा.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        SecondaryButton(
                            text = "मेरा डेटा मिटाओ (Clear Data)",
                            icon = Icons.Default.Delete,
                            onClick = { showDeleteConfirmDialog = true },
                            testTag = "btn_clear_data"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // SAVE & APPLY BUTTON
                PrimaryButton(
                    text = "सेव्ह करा (Save)",
                    icon = Icons.Default.Save,
                    onClick = {
                        prefs.driverName = name.ifBlank { "ड्राइवर" }
                        prefs.helperLanguage = selectedLang
                        prefs.isMockMode = isMock
                        if (backendUrl.isNotBlank()) {
                            prefs.backendUrl = backendUrl
                        }
                        onDismiss()
                    },
                    testTag = "btn_save_settings"
                )
            }
        }
    }

    // Calm, styled Confirmation Dialog for "मेरा डेटा मिटाओ"
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "डेटा मिटवायचा आहे का?",
                    fontWeight = FontWeight.Black,
                    color = PureWhite
                )
            },
            text = {
                Text(
                    text = "तुमचा स्थानिक सराव इतिहास आणि सर्व फेऱ्यांचे स्कोअर हटवले जातील. ही क्रिया परत करता येत नाही.",
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        coordinator.deleteDriverData { }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("होय, मिटवा", color = RickshawBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("रद्द करा", color = PureWhite)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(22.dp)
        )
    }
}
