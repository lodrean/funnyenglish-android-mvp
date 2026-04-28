package com.funnyenglish.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey val word: String,
    val phonetic: String?,
    val jsonMeanings: String,
    val audioUrl: String?,
    val isFavorite: Boolean = false,
    val searchedAt: Long = System.currentTimeMillis()
)
