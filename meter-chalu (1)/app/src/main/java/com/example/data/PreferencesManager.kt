package com.example.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("meter_chalu_prefs", Context.MODE_PRIVATE)

    var isFirstLaunchCompleted: Boolean
        get() = prefs.getBoolean("first_launch_done", false)
        set(value) = prefs.edit().putBoolean("first_launch_done", value).apply()

    var driverName: String
        get() = prefs.getString("driver_name", "रमेश भाई") ?: "रमेश भाई"
        set(value) = prefs.edit().putString("driver_name", value).apply()

    var helperLanguage: String
        get() = prefs.getString("helper_language", "hi") ?: "hi"
        set(value) = prefs.edit().putString("helper_language", value).apply()

    // Room name = "driver-" + random 8-character lowercase hex id (ASCII ONLY, no Devanagari)
    val stableRoomName: String
        get() {
            var room = prefs.getString("stable_room_name", null)
            if (room == null || !room.startsWith("driver-") || room.length != 15) {
                val hexChars = "0123456789abcdef"
                val random = SecureRandom()
                val hex8 = (1..8).map { hexChars[random.nextInt(hexChars.length)] }.joinToString("")
                room = "driver-$hex8"
                prefs.edit().putString("stable_room_name", room).apply()
            }
            return room
        }

    // Single Backend URL: no trailing slash, no "/token"
    var backendUrl: String
        get() {
            val raw = prefs.getString("backend_url", "") ?: ""
            return cleanBackendUrl(raw)
        }
        set(value) {
            val cleaned = cleanBackendUrl(value)
            prefs.edit().putString("backend_url", cleaned).apply()
        }

    private fun cleanBackendUrl(url: String): String {
        var clean = url.trim().trimEnd('/')
        if (clean.endsWith("/token")) {
            clean = clean.substringBeforeLast("/token").trimEnd('/')
        }
        return clean
    }

    var isMockMode: Boolean
        get() = prefs.getBoolean("is_mock_mode", false)
        set(value) = prefs.edit().putBoolean("is_mock_mode", value).apply()

    var overallReadiness: Int
        get() = prefs.getInt("overall_readiness", 78)
        set(value) = prefs.edit().putInt("overall_readiness", value).apply()

    fun getScenarioScore(scenarioId: String): Int {
        return prefs.getInt("score_$scenarioId", 0)
    }

    fun setScenarioScore(scenarioId: String, score: Int) {
        val best = maxOf(getScenarioScore(scenarioId), score)
        prefs.edit().putInt("score_$scenarioId", best).apply()
    }

    fun getAllScores(): Map<String, Int> {
        return mapOf(
            "dadar" to getScenarioScore("dadar"),
            "fare" to getScenarioScore("fare"),
            "directions" to getScenarioScore("directions"),
            "elderly" to getScenarioScore("elderly"),
            "rto" to getScenarioScore("rto")
        )
    }

    fun updateFromReadiness(summary: ReadinessSummary) {
        overallReadiness = summary.percent
        summary.per_scenario.forEach { (sc, scScore) ->
            setScenarioScore(sc, scScore)
        }
    }

    // Cached Home Report JSON
    var cachedReportJson: String?
        get() = prefs.getString("cached_home_report", null)
        set(value) = prefs.edit().putString("cached_home_report", value).apply()

    // Forget Me: Clear report, readiness, and scenario scores. Keep name and language.
    fun clearDriverReportAndReadiness() {
        prefs.edit()
            .remove("cached_home_report")
            .remove("overall_readiness")
            .remove("score_dadar")
            .remove("score_fare")
            .remove("score_directions")
            .remove("score_elderly")
            .remove("score_rto")
            .apply()
    }

    fun resetData() {
        prefs.edit().clear().apply()
    }
}
