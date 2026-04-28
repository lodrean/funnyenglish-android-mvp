package com.funnyenglish.core.domain.model

data class Word(
    val spelling: String,
    val phonetic: String?,
    val audioUrl: String?,
    val meanings: List<Meaning>
)

data class Meaning(
    val partOfSpeech: String,
    val definitions: List<Definition>
)

data class Definition(
    val text: String,
    val example: String?
)
