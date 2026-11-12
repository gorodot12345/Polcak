package com.example.model

data class TranslationRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sourceText: String,
    val translatedText: String,
    val direction: TranslationDirection,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
