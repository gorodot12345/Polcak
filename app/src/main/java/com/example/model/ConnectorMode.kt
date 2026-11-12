package com.example.model

enum class ConnectorMode(val displayName: String, val description: String) {
    CYCLE("По очереди", "Связки чередуются по правилу языка Полчака"),
    RANDOM("Случайно", "Связка выбирается случайным образом"),
    SINGLE("Только 'сую юю'", "Используется короткая связка 'сую юю'"),
    NONE("Без связок", "Только прямой перевод текста без фраз-связок")
}
