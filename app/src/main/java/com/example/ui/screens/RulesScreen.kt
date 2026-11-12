package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.PolchakEngine
import com.example.model.PolchakRuleItem
import com.example.ui.theme.PolchakBadge
import com.example.ui.theme.PolchakBadgeContainer
import com.example.viewmodel.TranslatorViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RulesScreen(
    viewModel: TranslatorViewModel,
    onTestInTranslator: (String) -> Unit,
    highlightedRuleNumber: Int? = null,
    modifier: Modifier = Modifier
) {
    val isSpeaking by viewModel.isSpeaking.collectAsState()

    // Interactive Sandbox state
    var sandboxText by remember { mutableStateOf("Воробей сел на стульчик. Она сказала хорошо.") }
    val sandboxResult = remember(sandboxText) {
        PolchakEngine.translateToPolchak(sandboxText)
    }
    val sandboxRules = remember(sandboxText) {
        PolchakEngine.analyzeRulesApplied(sandboxText)
    }

    val rulesList = remember {
        listOf(
            PolchakRuleItem(
                number = 1,
                title = "Общие принципы",
                summary = "Все предложения пишутся с маленькой буквы. В конце каждого предложения ставится восклицательный знак.",
                details = "В тексте намеренно делаются орфографические ошибки. Заглавных букв нет вообще, даже в именах собственных.",
                examples = listOf(
                    "Привет." to "привет!",
                    "Как дела?" to "как дела!"
                ),
                tags = listOf("Строчные буквы", "Восклицательный знак !")
            ),
            PolchakRuleItem(
                number = 2,
                title = "Замена местоимений",
                summary = "Вместо «она» всегда пишется «ана».",
                details = "Местоимение третьего лица женского рода трансформируется в 'ана'.",
                examples = listOf(
                    "Она пошла в школу." to "ана пашла в школу!"
                ),
                tags = listOf("она → ана")
            ),
            PolchakRuleItem(
                number = 3,
                title = "Окончание «-чик»",
                summary = "Если слово оканчивается на -чик, это окончание удваивается: -чикчик.",
                details = "Любые существительные с уменьшительно-ласкательным или корневым -чик на конце получают двойное окончание -чикчик.",
                examples = listOf(
                    "укольчик" to "укольчикчик!",
                    "стульчик" to "стульчикчик!",
                    "мячик" to "мячикчик!"
                ),
                tags = listOf("-чик → -чикчик")
            ),
            PolchakRuleItem(
                number = 4,
                title = "Обязательные «ошибочные» слова",
                summary = "Слова, которые пишутся нарочито неправильно и считаются нормой языка Полчака.",
                details = "Обязательные словарные исключения. Список стабилен и узнаваем:",
                examples = listOf(
                    "воробей" to "варобей",
                    "корова" to "карова",
                    "молоко" to "малако",
                    "собака" to "сабака",
                    "хорошо" to "харашо",
                    "пожалуйста" to "пажалуста",
                    "здравствуйте" to "здраствуйти",
                    "человек" to "чилавек",
                    "язык" to "изык",
                    "ещё" to "исчо"
                ),
                tags = listOf("Словарь", "10+ исключений")
            ),
            PolchakRuleItem(
                number = 5,
                title = "Запятые",
                summary = "Запятые ставятся, но приклеиваются к следующему слову без пробела.",
                details = "Запятая-приклейка ставится вплотную к следующему слову внутри предложения.",
                examples = listOf(
                    "привет, но я устал" to "привет,но я устал!"
                ),
                tags = listOf("Запятая-приклейка", "Без пробела")
            ),
            PolchakRuleItem(
                number = 6,
                title = "Вставные фразы между предложениями",
                summary = "Между предложениями по смыслу обязательно вставляются фразы-связки.",
                details = "Фразы-связки по выбору:\n• «заработать титул на эль примоса»\n• «играть на турнире чака»\n• «сую юю»",
                examples = listOf(
                    "Я пришел домой. Она спала." to "я пришол дамой! сую юю ана спала!"
                ),
                tags = listOf("Фразы-связки", "сую юю", "эль примоса", "турнир чака")
            ),
            PolchakRuleItem(
                number = 7,
                title = "Пунктуация и оформление",
                summary = "Восклицательный знак — в конце каждого предложения без исключений.",
                details = "Заглавных букв нет нигде (даже в именах). Запятая-приклейка работает только внутри предложения, перед следующим словом.",
                examples = listOf(
                    "Привет, как дела?" to "привет,как дела!"
                ),
                tags = listOf("Только '!'", "Без точек", "Без заглавных")
            ),
            PolchakRuleItem(
                number = 8,
                title = "Мини-пример текста на языке Полчака",
                summary = "Каноничный эталонный текст, объединяющий все правила.",
                details = "Исходный текст:\n«Привет, но я не пришел. Воробей сел на стульчик. Она сказала хорошо. Я люблю молоко.»",
                examples = listOf(
                    "Эталон" to "привет,но я ни пришол! заработать титул на эль примоса варобей сел на стульчикчик! сую юю ана сказала харашо! играть на турнире чака я люблю малако!"
                ),
                tags = listOf("Золотой канон", "Все правила вместе")
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            // Header Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📖 Язык Полчака — свод правил",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Официальные каноничные грамматические и орфографические правила языка Полчака с интерактивными примерами и озвучкой.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        // Interactive Sandbox item
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🧪 Интерактивный тренажёр правил",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Введите любой текст и сразу увидите примененные правила языка:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = sandboxText,
                        onValueChange = { sandboxText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rules_sandbox_input"),
                        label = { Text("Исходный текст") },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Result Box
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "По-полчацки:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = sandboxResult.ifEmpty { "..." },
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Rules triggered tags
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        sandboxRules.forEach { r ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PolchakBadgeContainer
                            ) {
                                Text(
                                    text = "Пр. $r",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = PolchakBadge,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { onTestInTranslator(sandboxText) }
                        ) {
                            Icon(imageVector = Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Перевести в основном окне")
                        }
                    }
                }
            }
        }

        // List of all 8 rules
        items(rulesList) { rule ->
            RuleCard(
                rule = rule,
                isHighlighted = highlightedRuleNumber == rule.number,
                isSpeaking = isSpeaking,
                onSpeak = { text -> viewModel.ttsManager.speak(text) },
                onStop = { viewModel.stopSpeaking() },
                onTestInTranslator = onTestInTranslator
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RuleCard(
    rule: PolchakRuleItem,
    isHighlighted: Boolean,
    isSpeaking: Boolean,
    onSpeak: (String) -> Unit,
    onStop: () -> Unit,
    onTestInTranslator: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rule_card_${rule.number}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighlighted) 4.dp else 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Rule Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${rule.number}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = rule.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                // Tags
                rule.tags.firstOrNull()?.let { firstTag ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PolchakBadgeContainer
                    ) {
                        Text(
                            text = firstTag,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PolchakBadge,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Summary text
            Text(
                text = rule.summary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            if (rule.details.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = rule.details,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Examples container
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Примеры применения:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    rule.examples.forEach { (src, res) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Правильно: $src",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Text(
                                    text = "По-полчацки: $res",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            Row {
                                // Speak button
                                IconButton(
                                    onClick = { onSpeak(res) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Озвучить",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // Test in translator button
                                IconButton(
                                    onClick = { onTestInTranslator(src) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Проверить",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
