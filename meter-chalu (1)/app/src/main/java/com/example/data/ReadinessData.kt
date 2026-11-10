package com.example.data

data class ReadinessSummary(
    val percent: Int,
    val per_scenario: Map<String, Int> = emptyMap(),
    val ready: Boolean = false,
    val note: String = "Practice aid only. This is not an official test and does not certify anyone.",
    val scenario: String = "conversation",
    val score: Int = 80,
    val detected_language: String = "hi-IN"
)

data class TargetSentence(
    val marathi: String,
    val meaning: String
)

data class ClosenessWord(
    val w: String,
    val state: String // "great", "almost", "miss"
)

data class ClosenessData(
    val score: Int,
    val words: List<ClosenessWord>,
    val heard: String,
    val mastered: Boolean = false
)

data class FixItem(
    val wrong: String,
    val right: String,
    val meaning: String,
    val tip: String,
    val times: Int
)

data class RecentSession(
    val date: String,
    val scenario: String,
    val score: Int,
    val closeness: Int
)

data class HomeReport(
    val name: String = "चालक भाऊ",
    val percent: Int = 78,
    val per_scenario: Map<String, Int> = mapOf(
        "dadar" to 85,
        "fare" to 80,
        "directions" to 88,
        "elderly" to 75,
        "rto" to 80
    ),
    val ready: Boolean = true,
    val note: String = "Practice aid only. This is not an official test and does not certify anyone.",
    val sessions: Int = 12,
    val streak_days: Int = 3,
    val days_since_last: Int = 0,
    val closeness_avg_recent: Int = 82,
    val closeness_trend: Int = 1, // > 0 up arrow, <= 0 flat arrow
    val strong_in: List<String> = listOf("dadar", "directions"),
    val fix_these: List<FixItem> = listOf(
        FixItem(
            wrong = "दादर स्टेशन जाओगे?",
            right = "दादर स्टेशनला चलाल का?",
            meaning = "दादर स्टेशन चलोगे क्या?",
            tip = "'स्टेशनला' आणि 'चलाल का' वापरल्याने वाक्य नम्र बनते.",
            times = 3
        ),
        FixItem(
            wrong = "इधर रुको",
            right = "इथे डाव्या बाजूला थांबा",
            meaning = "यहाँ बाईं तरफ रुकिए",
            tip = "'थांबा' म्हणजे रुकिए, स्पष्ट दिशा द्या.",
            times = 2
        )
    ),
    val fixed_count: Int = 8,
    val recent: List<RecentSession> = listOf(
        RecentSession("आज", "dadar", 88, 85),
        RecentSession("काल", "fare", 80, 78),
        RecentSession("2 दिवस आधी", "directions", 90, 84)
    ),
    val carry_forward: String = "दहा रुपये सुट्टे आहेत का - हे वाक्य आज उत्तम बोललात!"
)

enum class MessageSender {
    AGENT,
    DRIVER,
    SUMMARY_CARD
}

data class ConversationMessage(
    val id: String,
    val sender: MessageSender,
    val textMarathi: String,
    val meaning: String? = null,
    val closeness: ClosenessData? = null,
    val readiness: ReadinessSummary? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class LiveKitTokenRequest(
    val roomName: String,
    val participantName: String,
    val agentName: String = "saathi",
    val metadata: Map<String, String>? = null
)

data class LiveKitTokenResponse(
    val serverUrl: String?,
    val participantToken: String?
)
