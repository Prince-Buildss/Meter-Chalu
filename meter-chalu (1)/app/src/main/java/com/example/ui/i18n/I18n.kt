package com.example.ui.i18n

data class SupportedLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val flag: String
)

object I18nManager {
    val SUPPORTED_LANGUAGES = listOf(
        SupportedLanguage("hi", "हिन्दी", "Hindi (Default)", "🇮🇳"),
        SupportedLanguage("en", "English", "English", "🌐"),
        SupportedLanguage("mr", "मराठी", "Marathi", "🚩"),
        SupportedLanguage("gu", "ગુજરાતી", "Gujarati", "🇮🇳"),
        SupportedLanguage("bn", "বাংলা", "Bengali", "🇮🇳"),
        SupportedLanguage("te", "తెలుగు", "Telugu", "🇮🇳"),
        SupportedLanguage("ta", "தமிழ்", "Tamil", "🇮🇳"),
        SupportedLanguage("kn", "ಕನ್ನಡ", "Kannada", "🇮🇳"),
        SupportedLanguage("pa", "ਪੰਜਾਬੀ", "Punjabi", "🇮🇳"),
        SupportedLanguage("ml", "മലയാളം", "Malayalam", "🇮🇳")
    )

    private val hindiStrings = mapOf(
        "tagline" to "भाषा चालू, मीटर चालू!",
        "greeting" to "नमस्ते %s भाई!",
        "startPractice" to "प्रैक्टिस शुरू करें",
        "chooseSituation" to "सवारी / स्थिति चुनें",
        "readinessMeter" to "मराठी तैयारी मीटर",
        "practiceScoreLabel" to "सराव गुण (Practice score)",
        "disclaimer" to "अभ्यास के लिए केवल। यह कोई आधिकारिक टेस्ट या सर्टिफिकेट नहीं है।",
        "connecting" to "एजेंट से कनेक्ट हो रहा है...",
        "passengerSpeaking" to "सवारी बोल रही है...",
        "listening" to "सुन रहा है... (हाथ छुए बिना बोलिए)",
        "thinking" to "साथी सोच रहा है...",
        "replay" to "फिर से",
        "slow" to "धीरे बोलो",
        "meaning" to "मतलब?",
        "pause" to "रुको",
        "resume" to "शुरू करें",
        "stop" to "बस / बंद करो",
        "driverSpeaking" to "आपकी आवाज़:",
        "todayPhrases" to "आज के सीखे हुए वाक्य",
        "summaryTitle" to "प्रैक्टिस राउंड पूरा हुआ!",
        "practiceAgain" to "फिर से अभ्यास करें",
        "home" to "होम स्क्रीन",
        "streakDays" to "%d दिन की लड़ी",
        "phrasesLearned" to "%d वाक्य सीखे",
        "sessionsCompleted" to "%d फेरियां पूरी",
        "settings" to "सेटिंग्स",
        "phrasebook" to "जरूरी 20 वाक्य",
        "driverName" to "चालक का नाम",
        "helperLanguage" to "मददगार भाषा",
        "mockMode" to "मॉक मोड (बिना इंटरनेट टेस्ट)",
        "tokenEndpoint" to "LiveKit Token Endpoint",
        "serverUrl" to "LiveKit Server URL",
        "save" to "सहेजें",
        "shareWhatsApp" to "व्हाट्सएप पर शेयर करें",
        "great" to "बहुत बढ़िया! 70%+ तैयार",
        "almost" to "अच्छा प्रयास! थोड़ा और अभ्यास करें",
        "selectLanguageTitle" to "अपनी भाषा चुनें",
        "enterNameTitle" to "अपना नाम बताइए",
        "namePlaceholder" to "जैसे: रमेश भाई",
        "continueBtn" to "आगे बढ़ें",
        "tipNotice" to "साथी की सलाह:"
    )

    private val englishStrings = mapOf(
        "tagline" to "भाषा चालू, मीटर चालू!",
        "greeting" to "Namaste %s Bhai!",
        "startPractice" to "Start Practice",
        "chooseSituation" to "Choose Situation",
        "readinessMeter" to "Marathi Readiness Meter",
        "practiceScoreLabel" to "Practice score (सराव गुण)",
        "disclaimer" to "Practice aid only. This is not an official test and does not certify anyone.",
        "connecting" to "Connecting to agent...",
        "passengerSpeaking" to "Passenger is speaking...",
        "listening" to "Listening... (speak hands-free)",
        "thinking" to "Saathi is thinking...",
        "replay" to "Replay",
        "slow" to "Speak Slow",
        "meaning" to "Meaning?",
        "pause" to "Pause",
        "resume" to "Resume",
        "stop" to "Stop Ride",
        "driverSpeaking" to "Your voice:",
        "todayPhrases" to "Phrases Practised Today",
        "summaryTitle" to "Practice Round Finished!",
        "practiceAgain" to "Practice Again",
        "home" to "Home",
        "streakDays" to "%d Day Streak",
        "phrasesLearned" to "%d Phrases Mastered",
        "sessionsCompleted" to "%d Rides Practised",
        "settings" to "Settings",
        "phrasebook" to "Essential 20 Phrases",
        "driverName" to "Driver Name",
        "helperLanguage" to "Helper Language",
        "mockMode" to "Mock Mode (Offline Simulation)",
        "tokenEndpoint" to "LiveKit Token Endpoint",
        "serverUrl" to "LiveKit Server URL",
        "save" to "Save",
        "shareWhatsApp" to "Share on WhatsApp",
        "great" to "Great! 70%+ Ready",
        "almost" to "Almost Ready! Keep Practising",
        "selectLanguageTitle" to "Choose Helper Language",
        "enterNameTitle" to "Enter Your Name",
        "namePlaceholder" to "e.g. Ramesh",
        "continueBtn" to "Continue",
        "tipNotice" to "Saathi Coach Tip:"
    )

    fun getString(key: String, langCode: String, vararg args: Any): String {
        val dict = when (langCode) {
            "hi" -> hindiStrings
            else -> englishStrings
        }
        val template = dict[key] ?: englishStrings[key] ?: key
        return if (args.isNotEmpty()) {
            try {
                String.format(template, *args)
            } catch (e: Exception) {
                template
            }
        } else {
            template
        }
    }
}
