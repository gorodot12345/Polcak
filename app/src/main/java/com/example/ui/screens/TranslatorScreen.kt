package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ui.components.LanguageBar
import com.example.ui.components.RulePillsRow
import com.example.ui.components.SamplePhrasesRow
import com.example.ui.components.TranslationInputCard
import com.example.ui.components.TranslationOutputCard
import com.example.viewmodel.TranslatorViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun TranslatorScreen(
    viewModel: TranslatorViewModel,
    onNavigateToRules: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val inputText by viewModel.inputText.collectAsState()
    val translatedText by viewModel.translatedText.collectAsState()
    val direction by viewModel.direction.collectAsState()
    val connectorMode by viewModel.connectorMode.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val currentPreset by viewModel.currentPreset.collectAsState()
    val appliedRules by viewModel.appliedRules.collectAsState()
    val isFavorite by viewModel.isCurrentFavorite.collectAsState()

    var showFullscreen by remember { mutableStateOf(false) }

    // Speech-to-text launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.onInputTextChanged(spokenText)
            }
        }
    }

    val clipboardManager = remember {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Google Translate Top Language Selector Bar
            LanguageBar(
                direction = direction,
                onSwap = { viewModel.swapLanguages() }
            )

            // Sample phrases chips for quick 1-tap testing
            SamplePhrasesRow(
                onSelectSample = { sample ->
                    viewModel.onInputTextChanged(sample)
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Input Card
            TranslationInputCard(
                text = inputText,
                onTextChanged = { viewModel.onInputTextChanged(it) },
                sourceLanguageName = direction.sourceName,
                onClear = { viewModel.clearInput() },
                onPaste = {
                    val clip = clipboardManager.primaryClip
                    if (clip != null && clip.itemCount > 0) {
                        val pasteText = clip.getItemAt(0).text?.toString() ?: ""
                        if (pasteText.isNotBlank()) {
                            viewModel.onInputTextChanged(pasteText)
                            scope.launch {
                                snackbarHostState.showSnackbar("Текст вставлен из буфера")
                            }
                        }
                    }
                },
                onSpeakInput = { viewModel.speakSource() },
                onMicClick = {
                    try {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale("ru", "RU"))
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Говорите текст для перевода…")
                        }
                        speechLauncher.launch(intent)
                    } catch (_: Exception) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Голосовой ввод недоступен на этом устройстве")
                        }
                    }
                }
            )

            // Real-time triggered Polchak rules indicator
            RulePillsRow(
                appliedRules = appliedRules,
                onRuleClick = { ruleNum -> onNavigateToRules(ruleNum) }
            )

            // Output Card with Speech Synthesis & Actions
            TranslationOutputCard(
                translatedText = translatedText,
                targetLanguageName = direction.targetName,
                direction = direction,
                isSpeaking = isSpeaking,
                currentPreset = currentPreset,
                connectorMode = connectorMode,
                isFavorite = isFavorite,
                onSpeakClick = {
                    if (isSpeaking) {
                        viewModel.stopSpeaking()
                    } else {
                        viewModel.speakTranslation()
                    }
                },
                onCopyClick = {
                    if (translatedText.isNotEmpty()) {
                        clipboardManager.setPrimaryClip(ClipData.newPlainText("Перевод", translatedText))
                        scope.launch {
                            snackbarHostState.showSnackbar("Перевод скопирован в буфер!")
                        }
                    }
                },
                onShareClick = {
                    if (translatedText.isNotEmpty()) {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, translatedText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Поделиться переводом"))
                    }
                },
                onToggleFavorite = { viewModel.toggleFavoriteCurrent() },
                onFullscreenClick = { showFullscreen = true },
                onPresetChange = { preset -> viewModel.setTtsPreset(preset) },
                onConnectorModeChange = { mode -> viewModel.setConnectorMode(mode) }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Fullscreen Translated Dialog
        if (showFullscreen) {
            FullScreenTranslateDialog(
                text = translatedText,
                languageName = direction.targetName,
                isSpeaking = isSpeaking,
                onSpeakClick = {
                    if (isSpeaking) viewModel.stopSpeaking() else viewModel.speakTranslation()
                },
                onCopyClick = {
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("Перевод", translatedText))
                    scope.launch {
                        snackbarHostState.showSnackbar("Скопировано в буфер!")
                    }
                },
                onDismiss = { showFullscreen = false }
            )
        }
    }
}
