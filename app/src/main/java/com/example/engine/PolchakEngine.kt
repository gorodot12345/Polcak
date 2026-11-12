package com.example.engine

import com.example.model.ConnectorMode
import com.example.model.DictionaryEntry
import java.util.Locale

object PolchakEngine {

    val DEFAULT_CONNECTOR_PHRASES = listOf(
        "заработать титул на эль примоса",
        "сую юю",
        "играть на турнире чака"
    )

    val DEFAULT_DICTIONARY: List<DictionaryEntry> = listOf(
        DictionaryEntry("воробей", "варобей", isDefault = true),
        DictionaryEntry("корова", "карова", isDefault = true),
        DictionaryEntry("молоко", "малако", isDefault = true),
        DictionaryEntry("собака", "сабака", isDefault = true),
        DictionaryEntry("хорошо", "харашо", isDefault = true),
        DictionaryEntry("пожалуйста", "пажалуста", isDefault = true),
        DictionaryEntry("здравствуйте", "здраствуйти", isDefault = true),
        DictionaryEntry("человек", "чилавек", isDefault = true),
        DictionaryEntry("язык", "изык", isDefault = true),
        DictionaryEntry("ещё", "исчо", isDefault = true),
        DictionaryEntry("еще", "исчо", isDefault = true),
        // Additional vocabulary from examples
        DictionaryEntry("пришел", "пришол", isDefault = true),
        DictionaryEntry("пришёл", "пришол", isDefault = true),
        DictionaryEntry("домой", "дамой", isDefault = true),
        DictionaryEntry("пошла", "пашла", isDefault = true),
        DictionaryEntry("не", "ни", isDefault = true)
    )

    /**
     * Translates Russian text into Polchak Language according to the 8 official rules.
     */
    fun translateToPolchak(
        input: String,
        connectorMode: ConnectorMode = ConnectorMode.CYCLE,
        customDictionary: List<DictionaryEntry> = DEFAULT_DICTIONARY,
        customConnectors: List<String> = DEFAULT_CONNECTOR_PHRASES
    ): String {
        if (input.isBlank()) return ""

        // Split text into individual sentences (by ., !, ?, ;, or newline)
        val rawSentences = splitIntoSentences(input)
        if (rawSentences.isEmpty()) return ""

        val connectors = if (customConnectors.isNotEmpty()) customConnectors else DEFAULT_CONNECTOR_PHRASES

        val translatedSentences = rawSentences.map { rawSentence ->
            processSingleSentenceToPolchak(rawSentence, customDictionary)
        }.filter { it.isNotBlank() }

        if (translatedSentences.isEmpty()) return ""

        // Join sentences with connector phrases
        val builder = StringBuilder()
        for (i in translatedSentences.indices) {
            builder.append(translatedSentences[i])

            // If not the last sentence, insert connector phrase
            if (i < translatedSentences.size - 1 && connectorMode != ConnectorMode.NONE) {
                val connector = when (connectorMode) {
                    ConnectorMode.CYCLE -> connectors[i % connectors.size]
                    ConnectorMode.RANDOM -> connectors.random()
                    ConnectorMode.SINGLE -> "сую юю"
                    ConnectorMode.NONE -> ""
                }
                if (connector.isNotBlank()) {
                    builder.append(" ").append(connector).append(" ")
                } else {
                    builder.append(" ")
                }
            }
        }

        return builder.toString().trim()
    }

    /**
     * Translates Polchak Language text back to Standard Russian.
     */
    fun translateToRussian(
        input: String,
        customDictionary: List<DictionaryEntry> = DEFAULT_DICTIONARY,
        customConnectors: List<String> = DEFAULT_CONNECTOR_PHRASES
    ): String {
        if (input.isBlank()) return ""

        var cleaned = input
        // 1. Remove all connector phrases
        val allConnectors = (customConnectors + DEFAULT_CONNECTOR_PHRASES).distinct()
        for (connector in allConnectors) {
            val pattern = Regex("(?i)\\s*${Regex.escape(connector)}\\s*")
            cleaned = cleaned.replace(pattern, " ")
        }

        // 2. Split into sentences by ! or .
        val sentences = splitIntoSentences(cleaned)
        if (sentences.isEmpty()) return ""

        val reconstructed = sentences.map { rawSentence ->
            processSingleSentenceToRussian(rawSentence, customDictionary)
        }.filter { it.isNotBlank() }

        return reconstructed.joinToString(" ")
    }

    private fun processSingleSentenceToPolchak(
        sentence: String,
        dictionary: List<DictionaryEntry>
    ): String {
        // Rule 1 & 7: Everything in lowercase
        var text = sentence.trim().lowercase(Locale("ru", "RU"))

        // Rule 5: Commas stick to the next word without space: "слово, но" -> "слово,но"
        text = text.replace(Regex("\\s*,\\s*(?=\\S)"), ",")

        // Rule 4: Dictionary word replacements
        for (entry in dictionary) {
            val cyrillicPattern = Regex("(?<=^|[^а-яёa-z0-9])${Regex.escape(entry.ruWord.lowercase())}(?=[^а-яёa-z0-9]|$)")
            text = text.replace(cyrillicPattern, entry.polchakWord.lowercase())
        }

        // Rule 2: Pronoun "она" -> "ана"
        val onaPattern = Regex("(?<=^|[^а-яёa-z0-9])она(?=[^а-яёa-z0-9]|$)")
        text = text.replace(onaPattern, "ана")

        // Rule 3: Ending "-чик" doubled to "-чикчик"
        // E.g. укольчик -> укольчикчик, стульчик -> стульчикчик, мячик -> мячикчик
        // Don't double if already ends with "чикчик"
        val chikPattern = Regex("(?<=^|[^а-яёa-z0-9])([а-яёa-z0-9]+?)(?<!чик)чик(?=[^а-яёa-z0-9]|$)")
        text = text.replace(chikPattern, "$1чикчик")

        // Rule 1 & 7: Strip trailing punctuation and append exclamation mark
        text = text.trimEnd('.', '!', '?', ';', ',', ':', ' ')
        if (text.isBlank()) return ""

        return "$text!"
    }

    private fun processSingleSentenceToRussian(
        sentence: String,
        dictionary: List<DictionaryEntry>
    ): String {
        var text = sentence.trim()
        if (text.isBlank()) return ""

        // Rule 3 reverse: "-чикчик" -> "-чик"
        val chikchikPattern = Regex("(?<=^|[^а-яёa-z0-9])([а-яёa-z0-9]+?)чикчик(?=[^а-яёa-z0-9]|$)")
        text = text.replace(chikchikPattern, "$1чик")

        // Rule 2 reverse: "ана" -> "она"
        val anaPattern = Regex("(?<=^|[^а-яёa-z0-9])ана(?=[^а-яёa-z0-9]|$)")
        text = text.replace(anaPattern, "она")

        // Rule 4 reverse: Dictionary replacements
        for (entry in dictionary) {
            val polchakPattern = Regex("(?<=^|[^а-яёa-z0-9])${Regex.escape(entry.polchakWord.lowercase())}(?=[^а-яёa-z0-9]|$)")
            text = text.replace(polchakPattern, entry.ruWord.lowercase())
        }

        // Rule 5 reverse: Commas followed by space: ",слово" -> ", слово"
        text = text.replace(Regex(",(?=[а-яёa-z0-9])"), ", ")

        // Trim trailing exclamation mark
        text = text.trimEnd('!', ' ', '.')

        if (text.isEmpty()) return ""

        // Capitalize first letter for standard Russian
        text = text.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru", "RU")) else it.toString() }

        return "$text."
    }

    /**
     * Splits text into logical sentences while preserving internal commas.
     */
    private fun splitIntoSentences(text: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()

        var i = 0
        while (i < text.length) {
            val ch = text[i]
            if (ch == '.' || ch == '!' || ch == '?' || ch == ';' || ch == '\n') {
                // Check for ellipsis (e.g. "...")
                while (i + 1 < text.length && (text[i + 1] == '.' || text[i + 1] == '!' || text[i + 1] == '?')) {
                    i++
                }
                val s = current.toString().trim()
                if (s.isNotEmpty()) {
                    result.add(s)
                    current.clear()
                }
            } else {
                current.append(ch)
            }
            i++
        }

        val remaining = current.toString().trim()
        if (remaining.isNotEmpty()) {
            result.add(remaining)
        }

        return result
    }

    /**
     * Returns a list of which rules were applied to an input text (for the interactive rules sandbox).
     */
    fun analyzeRulesApplied(input: String): List<Int> {
        if (input.isBlank()) return emptyList()
        val triggered = mutableListOf<Int>()

        val lower = input.lowercase(Locale("ru", "RU"))

        // Rule 1: Always triggered (lowercase & '!' end)
        triggered.add(1)

        // Rule 2: "она"
        if (Regex("(?<=^|[^а-яёa-z0-9])она(?=[^а-яёa-z0-9]|$)").containsMatchIn(lower)) {
            triggered.add(2)
        }

        // Rule 3: word ending in "чик"
        if (Regex("(?<=^|[^а-яёa-z0-9])([а-яёa-z0-9]+?)(?<!чик)чик(?=[^а-яёa-z0-9]|$)").containsMatchIn(lower)) {
            triggered.add(3)
        }

        // Rule 4: Dictionary word exceptions
        val hasDictWord = DEFAULT_DICTIONARY.any { entry ->
            Regex("(?<=^|[^а-яёa-z0-9])${Regex.escape(entry.ruWord.lowercase())}(?=[^а-яёa-z0-9]|$)").containsMatchIn(lower)
        }
        if (hasDictWord) {
            triggered.add(4)
        }

        // Rule 5: Commas
        if (input.contains(",")) {
            triggered.add(5)
        }

        // Rule 6: Multiple sentences
        if (splitIntoSentences(input).size > 1) {
            triggered.add(6)
        }

        // Rule 7: Punctuation normalization
        triggered.add(7)

        return triggered.distinct().sorted()
    }
}
