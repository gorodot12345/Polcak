package com.example.model

data class DictionaryEntry(
    val ruWord: String,
    val polchakWord: String,
    val isDefault: Boolean = true,
    val notes: String = ""
)
