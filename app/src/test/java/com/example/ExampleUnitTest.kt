package com.example

import com.example.engine.PolchakEngine
import com.example.model.ConnectorMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testRule1_LowercaseAndExclamation() {
        val result = PolchakEngine.translateToPolchak("Привет мир.")
        assertEquals("привет мир!", result)
    }

    @Test
    fun testRule2_PronounOna() {
        val result = PolchakEngine.translateToPolchak("Она пошла в школу.")
        assertEquals("ана пашла в школу!", result)
    }

    @Test
    fun testRule3_EndingChikDoubled() {
        val r1 = PolchakEngine.translateToPolchak("укольчик")
        assertEquals("укольчикчик!", r1)

        val r2 = PolchakEngine.translateToPolchak("стульчик")
        assertEquals("стульчикчик!", r2)

        val r3 = PolchakEngine.translateToPolchak("мячик")
        assertEquals("мячикчик!", r3)
    }

    @Test
    fun testRule4_DictionaryExceptions() {
        val input = "воробей корова молоко собака хорошо пожалуйста здравствуйте человек язык ещё"
        val expected = "варобей карова малако сабака харашо пажалуста здраствуйти чилавек изык исчо!"
        val result = PolchakEngine.translateToPolchak(input)
        assertEquals(expected, result)
    }

    @Test
    fun testRule5_CommasWithoutSpace() {
        val input = "привет, но я устал"
        val result = PolchakEngine.translateToPolchak(input)
        assertEquals("привет,но я устал!", result)
    }

    @Test
    fun testRule8_CanonicalMiniExample() {
        val input = "Привет, но я не пришел. Воробей сел на стульчик. Она сказала хорошо. Я люблю молоко."
        val expected = "привет,но я ни пришол! заработать титул на эль примоса варобей сел на стульчикчик! сую юю ана сказала харашо! играть на турнире чака я люблю малако!"
        val result = PolchakEngine.translateToPolchak(input, connectorMode = ConnectorMode.CYCLE)
        assertEquals(expected, result)
    }

    @Test
    fun testReverseTranslation() {
        val polchak = "привет,но я ни пришол! заработать титул на эль примоса варобей сел на стульчикчик! сую юю ана сказала харашо!"
        val russian = PolchakEngine.translateToRussian(polchak)
        assertTrue(russian.contains("воробей", ignoreCase = true))
        assertTrue(russian.contains("стульчик", ignoreCase = true))
        assertTrue(russian.contains("Она сказала хорошо"))
    }
}
