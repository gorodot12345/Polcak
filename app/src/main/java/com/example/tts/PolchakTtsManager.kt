package com.example.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class TtsVoicePreset(
    val title: String,
    val icon: String,
    val pitch: Float,
    val rate: Float
) {
    DEFAULT("Обычный", "🗣️", 1.0f, 1.0f),
    CHAK("Чак-турнир", "🏆", 1.2f, 1.2f),
    EL_PRIMO("Эль Примо", "🥊", 0.7f, 0.9f),
    SUYU_YU("Сую юю", "⚡", 1.5f, 1.35f)
}

class PolchakTtsManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentPreset = MutableStateFlow(TtsVoicePreset.DEFAULT)
    val currentPreset: StateFlow<TtsVoicePreset> = _currentPreset.asStateFlow()

    private val _ttsError = MutableStateFlow<String?>(null)
    val ttsError: StateFlow<String?> = _ttsError.asStateFlow()

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val localeRu = Locale("ru", "RU")
                val result = tts?.setLanguage(localeRu)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Try generic Russian locale or default
                    tts?.setLanguage(Locale("ru"))
                }
                isInitialized = true
                applyPreset(_currentPreset.value)

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        _ttsError.value = "Ошибка синтеза речи"
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeaking.value = false
                        _ttsError.value = "Код ошибки TTS: $errorCode"
                    }
                })
            } else {
                _ttsError.value = "Не удалось инициализировать синтез речи"
            }
        }
    }

    fun setPreset(preset: TtsVoicePreset) {
        _currentPreset.value = preset
        applyPreset(preset)
    }

    private fun applyPreset(preset: TtsVoicePreset) {
        tts?.setPitch(preset.pitch)
        tts?.setSpeechRate(preset.rate)
    }

    fun speak(text: String) {
        if (!isInitialized || text.isBlank()) return
        stop()

        applyPreset(_currentPreset.value)
        val utteranceId = "polchak_utt_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
