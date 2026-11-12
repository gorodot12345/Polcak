package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TranslationRepository
import com.example.engine.PolchakEngine
import com.example.model.ConnectorMode
import com.example.model.DictionaryEntry
import com.example.model.TranslationDirection
import com.example.model.TranslationRecord
import com.example.tts.PolchakTtsManager
import com.example.tts.TtsVoicePreset
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TranslatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TranslationRepository(application)
    val ttsManager = PolchakTtsManager(application)

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _translatedText = MutableStateFlow("")
    val translatedText: StateFlow<String> = _translatedText.asStateFlow()

    private val _direction = MutableStateFlow(TranslationDirection.RUSSIAN_TO_POLCHAK)
    val direction: StateFlow<TranslationDirection> = _direction.asStateFlow()

    val connectorMode: StateFlow<ConnectorMode> = repository.connectorMode
    val dictionary: StateFlow<List<DictionaryEntry>> = repository.dictionary
    val history: StateFlow<List<TranslationRecord>> = repository.history

    val isSpeaking: StateFlow<Boolean> = ttsManager.isSpeaking
    val currentPreset: StateFlow<TtsVoicePreset> = ttsManager.currentPreset

    private val _appliedRules = MutableStateFlow<List<Int>>(emptyList())
    val appliedRules: StateFlow<List<Int>> = _appliedRules.asStateFlow()

    private var saveHistoryJob: Job? = null

    val isCurrentFavorite: StateFlow<Boolean> = combine(
        _inputText,
        _direction,
        history
    ) { text, dir, hist ->
        if (text.isBlank()) false
        else hist.firstOrNull { it.sourceText == text && it.direction == dir }?.isFavorite ?: false
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        // Pre-fill with canonical example so user sees instant live translation on first launch!
        val canonicalRussian = "Привет, но я не пришел. Воробей сел на стульчик. Она сказала хорошо. Я люблю молоко."
        onInputTextChanged(canonicalRussian)
    }

    fun onInputTextChanged(text: String) {
        _inputText.value = text
        performTranslation(text, _direction.value, connectorMode.value, dictionary.value)
    }

    private fun performTranslation(
        source: String,
        dir: TranslationDirection,
        connMode: ConnectorMode,
        dict: List<DictionaryEntry>
    ) {
        if (source.isBlank()) {
            _translatedText.value = ""
            _appliedRules.value = emptyList()
            return
        }

        val result = if (dir == TranslationDirection.RUSSIAN_TO_POLCHAK) {
            _appliedRules.value = PolchakEngine.analyzeRulesApplied(source)
            PolchakEngine.translateToPolchak(
                input = source,
                connectorMode = connMode,
                customDictionary = dict
            )
        } else {
            _appliedRules.value = emptyList()
            PolchakEngine.translateToRussian(
                input = source,
                customDictionary = dict
            )
        }

        _translatedText.value = result

        // Debounce saving translation into history after user finishes typing
        saveHistoryJob?.cancel()
        saveHistoryJob = viewModelScope.launch {
            delay(1200)
            if (source.isNotBlank() && result.isNotBlank()) {
                repository.addTranslation(source.trim(), result.trim(), dir)
            }
        }
    }

    fun swapLanguages() {
        val newDirection = _direction.value.swapped()
        val currentTranslated = _translatedText.value

        _direction.value = newDirection
        _inputText.value = currentTranslated

        performTranslation(currentTranslated, newDirection, connectorMode.value, dictionary.value)
    }

    fun clearInput() {
        _inputText.value = ""
        _translatedText.value = ""
        _appliedRules.value = emptyList()
        ttsManager.stop()
    }

    fun setConnectorMode(mode: ConnectorMode) {
        repository.setConnectorMode(mode)
        performTranslation(_inputText.value, _direction.value, mode, dictionary.value)
    }

    fun setTtsPreset(preset: TtsVoicePreset) {
        ttsManager.setPreset(preset)
    }

    fun speakSource() {
        if (_inputText.value.isNotBlank()) {
            ttsManager.speak(_inputText.value)
        }
    }

    fun speakTranslation() {
        if (_translatedText.value.isNotBlank()) {
            ttsManager.speak(_translatedText.value)
        }
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    fun toggleFavoriteCurrent() {
        val src = _inputText.value.trim()
        val trans = _translatedText.value.trim()
        if (src.isBlank() || trans.isBlank()) return

        val existing = history.value.firstOrNull { it.sourceText == src && it.direction == _direction.value }
        if (existing != null) {
            repository.toggleFavorite(existing.id)
        } else {
            repository.addTranslation(src, trans, _direction.value)
            val newlyAdded = history.value.firstOrNull { it.sourceText == src && it.direction == _direction.value }
            newlyAdded?.let { repository.toggleFavorite(it.id) }
        }
    }

    fun toggleFavoriteRecord(id: String) {
        repository.toggleFavorite(id)
    }

    fun deleteHistoryRecord(id: String) {
        repository.deleteRecord(id)
    }

    fun clearAllHistory() {
        repository.clearHistory()
    }

    fun addCustomDictionaryWord(ru: String, polchak: String, notes: String = "") {
        repository.addCustomDictionaryWord(ru, polchak, notes)
        performTranslation(_inputText.value, _direction.value, connectorMode.value, repository.dictionary.value)
    }

    fun removeCustomDictionaryWord(ru: String) {
        repository.removeCustomWord(ru)
        performTranslation(_inputText.value, _direction.value, connectorMode.value, repository.dictionary.value)
    }

    fun resetDictionary() {
        repository.resetDictionaryToDefaults()
        performTranslation(_inputText.value, _direction.value, connectorMode.value, repository.dictionary.value)
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
