package com.example.model

enum class TranslationDirection(
    val sourceName: String,
    val targetName: String
) {
    RUSSIAN_TO_POLCHAK("Русский", "Язык Полчака"),
    POLCHAK_TO_RUSSIAN("Язык Полчака", "Русский");

    fun swapped(): TranslationDirection = when (this) {
        RUSSIAN_TO_POLCHAK -> POLCHAK_TO_RUSSIAN
        POLCHAK_TO_RUSSIAN -> RUSSIAN_TO_POLCHAK
    }
}
