package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.PreferencesManager
import com.example.service.LiveKitAgentCoordinator
import com.example.ui.screens.ConversationScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MeterChaluTheme

enum class AppScreen {
    SPLASH,
    ONBOARDING,
    HOME,
    CONVERSATION
}

class MainActivity : ComponentActivity() {
    private var coordinator: LiveKitAgentCoordinator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val scope = rememberCoroutineScope()
            val prefs = remember { PreferencesManager(this@MainActivity) }
            val liveKitCoordinator = remember {
                LiveKitAgentCoordinator(this@MainActivity, prefs, scope).also {
                    coordinator = it
                }
            }

            var currentScreen by remember {
                mutableStateOf(AppScreen.SPLASH)
            }
            var showSettings by remember { mutableStateOf(false) }

            MeterChaluTheme(darkTheme = true) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        AppScreen.SPLASH -> {
                            SplashScreen(
                                onSplashFinished = {
                                    currentScreen = if (!prefs.isFirstLaunchCompleted) {
                                        AppScreen.ONBOARDING
                                    } else {
                                        AppScreen.HOME
                                    }
                                }
                            )
                        }

                        AppScreen.ONBOARDING -> {
                            OnboardingScreen(
                                prefs = prefs,
                                onComplete = {
                                    currentScreen = AppScreen.HOME
                                }
                            )
                        }

                        AppScreen.HOME -> {
                            HomeScreen(
                                prefs = prefs,
                                coordinator = liveKitCoordinator,
                                onStartPractice = {
                                    liveKitCoordinator.startConversation()
                                    currentScreen = AppScreen.CONVERSATION
                                },
                                onOpenSettings = {
                                    showSettings = true
                                }
                            )
                        }

                        AppScreen.CONVERSATION -> {
                            ConversationScreen(
                                coordinator = liveKitCoordinator,
                                helperLanguage = prefs.helperLanguage,
                                onEndCall = {
                                    currentScreen = AppScreen.HOME
                                }
                            )
                        }
                    }

                    if (showSettings) {
                        SettingsDialog(
                            prefs = prefs,
                            coordinator = liveKitCoordinator,
                            onDismiss = { showSettings = false }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coordinator?.release()
    }
}
