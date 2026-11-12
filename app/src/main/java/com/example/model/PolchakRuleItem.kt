package com.example.model

data class PolchakRuleItem(
    val number: Int,
    val title: String,
    val summary: String,
    val details: String,
    val examples: List<Pair<String, String>>, // Pair(Russian, Polchak)
    val tags: List<String>
)
