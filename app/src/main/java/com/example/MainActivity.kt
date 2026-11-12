package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.model.TranslationDirection
import com.example.ui.screens.DictionaryScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.RulesScreen
import com.example.ui.screens.TranslatorScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.TranslatorViewModel

enum class NavigationTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    TRANSLATOR("Перевод", Icons.Filled.Translate, Icons.Outlined.Translate, "nav_tab_translate"),
    RULES("Правила", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_tab_rules"),
    DICTIONARY("Словарь", Icons.Filled.Book, Icons.Outlined.Book, "nav_tab_dictionary"),
    HISTORY("История", Icons.Filled.History, Icons.Outlined.History, "nav_tab_history")
}

class MainActivity : ComponentActivity() {

    private val viewModel: TranslatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContainer(viewModel: TranslatorViewModel) {
    var currentTab by remember { mutableStateOf(NavigationTab.TRANSLATOR) }
    var highlightedRule by remember { mutableStateOf<Int?>(null) }

    // Android back handler: if not on Translate screen, back goes to Translate screen
    BackHandler(enabled = currentTab != NavigationTab.TRANSLATOR) {
        currentTab = NavigationTab.TRANSLATOR
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                NavigationTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (tab != NavigationTab.RULES) {
                                highlightedRule = null
                            }
                            currentTab = tab
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { paddingValues ->
        when (currentTab) {
            NavigationTab.TRANSLATOR -> {
                TranslatorScreen(
                    viewModel = viewModel,
                    onNavigateToRules = { ruleNum ->
                        highlightedRule = ruleNum
                        currentTab = NavigationTab.RULES
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            NavigationTab.RULES -> {
                RulesScreen(
                    viewModel = viewModel,
                    highlightedRuleNumber = highlightedRule,
                    onTestInTranslator = { exampleText ->
                        viewModel.onInputTextChanged(exampleText)
                        currentTab = NavigationTab.TRANSLATOR
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            NavigationTab.DICTIONARY -> {
                DictionaryScreen(
                    viewModel = viewModel,
                    onTestInTranslator = { word ->
                        viewModel.onInputTextChanged(word)
                        currentTab = NavigationTab.TRANSLATOR
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            NavigationTab.HISTORY -> {
                HistoryScreen(
                    viewModel = viewModel,
                    onSelectRecord = { record ->
                        viewModel.onInputTextChanged(record.sourceText)
                        currentTab = NavigationTab.TRANSLATOR
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}
