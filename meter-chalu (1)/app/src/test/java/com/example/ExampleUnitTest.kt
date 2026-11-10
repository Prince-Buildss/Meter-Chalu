package com.example

import com.example.data.ClosenessData
import com.example.data.PhrasebookRepository
import com.example.data.ScenariosRepository
import com.example.ui.i18n.I18nManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testAllScenariosExist() {
        val requiredScenarios = listOf("dadar", "fare", "directions", "elderly", "rto")
        for (id in requiredScenarios) {
            val sc = ScenariosRepository.ALL[id]
            assertNotNull("Scenario $id must exist", sc)
            assertTrue("Scenario $id must have opening line", sc!!.openingMarathi.isNotEmpty())
            assertTrue("Scenario $id must have at least 3 turns", sc.turns.size >= 3)
        }
    }

    @Test
    fun testPhrasebookHas20Phrases() {
        assertEquals(20, PhrasebookRepository.PHRASES.size)
    }

    @Test
    fun testI18nFallback() {
        val hindiGreeting = I18nManager.getString("tagline", "hi")
        assertEquals("भाषा चालू, मीटर चालू!", hindiGreeting)

        val englishGreeting = I18nManager.getString("tagline", "en")
        assertEquals("भाषा चालू, मीटर चालू!", englishGreeting)
    }

    @Test
    fun testClosenessMastered() {
        val data = ClosenessData(score = 80, words = emptyList(), heard = "चलाल का", mastered = true)
        assertTrue(data.mastered)
    }
}
