package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.engine.PolchakEngine
import com.example.model.ConnectorMode
import com.example.model.DictionaryEntry
import com.example.model.TranslationDirection
import com.example.model.TranslationRecord
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TranslationRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("polchak_translator_prefs", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val historyAdapter = moshi.adapter<List<StoredTranslationRecord>>(
        Types.newParameterizedType(List::class.java, StoredTranslationRecord::class.java)
    )

    private val dictAdapter = moshi.adapter<List<StoredDictionaryEntry>>(
        Types.newParameterizedType(List::class.java, StoredDictionaryEntry::class.java)
    )

    private val _history = MutableStateFlow<List<TranslationRecord>>(emptyList())
    val history: StateFlow<List<TranslationRecord>> = _history.asStateFlow()

    private val _dictionary = MutableStateFlow<List<DictionaryEntry>>(PolchakEngine.DEFAULT_DICTIONARY)
    val dictionary: StateFlow<List<DictionaryEntry>> = _dictionary.asStateFlow()

    private val _connectorMode = MutableStateFlow(ConnectorMode.CYCLE)
    val connectorMode: StateFlow<ConnectorMode> = _connectorMode.asStateFlow()

    init {
        loadHistory()
        loadDictionary()
        loadConnectorMode()
    }

    private fun loadHistory() {
        val json = prefs.getString(KEY_HISTORY, null)
        if (json != null) {
            try {
                val list = historyAdapter.fromJson(json)?.map { stored ->
                    TranslationRecord(
                        id = stored.id,
                        sourceText = stored.sourceText,
                        translatedText = stored.translatedText,
                        direction = if (stored.isPolchakToRussian) TranslationDirection.POLCHAK_TO_RUSSIAN else TranslationDirection.RUSSIAN_TO_POLCHAK,
                        timestamp = stored.timestamp,
                        isFavorite = stored.isFavorite
                    )
                } ?: emptyList()
                _history.value = list
            } catch (_: Exception) {
                _history.value = emptyList()
            }
        }
    }

    private fun saveHistory() {
        val stored = _history.value.map {
            StoredTranslationRecord(
                id = it.id,
                sourceText = it.sourceText,
                translatedText = it.translatedText,
                isPolchakToRussian = it.direction == TranslationDirection.POLCHAK_TO_RUSSIAN,
                timestamp = it.timestamp,
                isFavorite = it.isFavorite
            )
        }
        val json = historyAdapter.toJson(stored)
        prefs.edit().putString(KEY_HISTORY, json).apply()
    }

    fun addTranslation(source: String, translated: String, direction: TranslationDirection) {
        if (source.isBlank() || translated.isBlank()) return
        val existingIndex = _history.value.indexOfFirst { it.sourceText == source && it.direction == direction }
        val updated = _history.value.toMutableList()
        val isFav = if (existingIndex >= 0) updated[existingIndex].isFavorite else false

        if (existingIndex >= 0) {
            updated.removeAt(existingIndex)
        }

        updated.add(0, TranslationRecord(
            sourceText = source,
            translatedText = translated,
            direction = direction,
            isFavorite = isFav
        ))

        // Keep last 100 entries
        if (updated.size > 100) {
            val trimmed = updated.take(100)
            _history.value = trimmed
        } else {
            _history.value = updated
        }
        saveHistory()
    }

    fun toggleFavorite(recordId: String) {
        val list = _history.value.map {
            if (it.id == recordId) it.copy(isFavorite = !it.isFavorite) else it
        }
        _history.value = list
        saveHistory()
    }

    fun deleteRecord(recordId: String) {
        val list = _history.value.filterNot { it.id == recordId }
        _history.value = list
        saveHistory()
    }

    fun clearHistory() {
        _history.value = emptyList()
        saveHistory()
    }

    private fun loadDictionary() {
        val json = prefs.getString(KEY_DICTIONARY, null)
        if (json != null) {
            try {
                val custom = dictAdapter.fromJson(json)?.map {
                    DictionaryEntry(it.ruWord, it.polchakWord, isDefault = false, notes = it.notes)
                } ?: emptyList()
                _dictionary.value = PolchakEngine.DEFAULT_DICTIONARY + custom
            } catch (_: Exception) {
                _dictionary.value = PolchakEngine.DEFAULT_DICTIONARY
            }
        } else {
            _dictionary.value = PolchakEngine.DEFAULT_DICTIONARY
        }
    }

    fun addCustomDictionaryWord(ruWord: String, polchakWord: String, notes: String = "") {
        if (ruWord.isBlank() || polchakWord.isBlank()) return
        val currentCustom = _dictionary.value.filterNot { it.isDefault }.toMutableList()
        currentCustom.removeAll { it.ruWord.equals(ruWord, ignoreCase = true) }
        currentCustom.add(0, DictionaryEntry(ruWord.trim(), polchakWord.trim(), isDefault = false, notes = notes))

        val customStored = currentCustom.map {
            StoredDictionaryEntry(it.ruWord, it.polchakWord, it.notes)
        }
        prefs.edit().putString(KEY_DICTIONARY, dictAdapter.toJson(customStored)).apply()
        _dictionary.value = PolchakEngine.DEFAULT_DICTIONARY + currentCustom
    }

    fun removeCustomWord(ruWord: String) {
        val currentCustom = _dictionary.value.filterNot { it.isDefault }
            .filterNot { it.ruWord.equals(ruWord, ignoreCase = true) }
        val customStored = currentCustom.map {
            StoredDictionaryEntry(it.ruWord, it.polchakWord, it.notes)
        }
        prefs.edit().putString(KEY_DICTIONARY, dictAdapter.toJson(customStored)).apply()
        _dictionary.value = PolchakEngine.DEFAULT_DICTIONARY + currentCustom
    }

    fun resetDictionaryToDefaults() {
        prefs.edit().remove(KEY_DICTIONARY).apply()
        _dictionary.value = PolchakEngine.DEFAULT_DICTIONARY
    }

    private fun loadConnectorMode() {
        val modeName = prefs.getString(KEY_CONNECTOR_MODE, ConnectorMode.CYCLE.name)
        _connectorMode.value = try {
            ConnectorMode.valueOf(modeName ?: ConnectorMode.CYCLE.name)
        } catch (_: Exception) {
            ConnectorMode.CYCLE
        }
    }

    fun setConnectorMode(mode: ConnectorMode) {
        _connectorMode.value = mode
        prefs.edit().putString(KEY_CONNECTOR_MODE, mode.name).apply()
    }

    companion object {
        private const val KEY_HISTORY = "history_records"
        private const val KEY_DICTIONARY = "custom_dictionary"
        private const val KEY_CONNECTOR_MODE = "connector_mode"
    }
}

data class StoredTranslationRecord(
    val id: String,
    val sourceText: String,
    val translatedText: String,
    val isPolchakToRussian: Boolean,
    val timestamp: Long,
    val isFavorite: Boolean
)

data class StoredDictionaryEntry(
    val ruWord: String,
    val polchakWord: String,
    val notes: String = ""
)
