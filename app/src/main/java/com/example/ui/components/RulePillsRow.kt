package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PolchakBadge
import com.example.ui.theme.PolchakBadgeContainer

@Composable
fun RulePillsRow(
    appliedRules: List<Int>,
    onRuleClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = appliedRules.isNotEmpty(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Сработавшие правила языка Полчака:",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                appliedRules.forEach { ruleNum ->
                    val (title, icon) = when (ruleNum) {
                        1 -> "Пр. 1: строчные + !" to "🔤"
                        2 -> "Пр. 2: она → ана" to "🔄"
                        3 -> "Пр. 3: -чикчик" to "✨"
                        4 -> "Пр. 4: Исключения" to "📖"
                        5 -> "Пр. 5: Запятая-приклейка" to "🧩"
                        6 -> "Пр. 6: Фраза-связка" to "🔗"
                        7 -> "Пр. 7: Пунктуация" to "❗"
                        else -> "Правило $ruleNum" to "⚡"
                    }

                    Surface(
                        onClick = { onRuleClick(ruleNum) },
                        shape = RoundedCornerShape(12.dp),
                        color = PolchakBadgeContainer,
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = icon, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = PolchakBadge
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
