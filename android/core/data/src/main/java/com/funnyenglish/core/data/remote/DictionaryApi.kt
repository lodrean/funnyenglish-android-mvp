package com.funnyenglish.core.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

class DictionaryApi(private val client: HttpClient) {
    suspend fun getDefinition(word: String): List<WordDto> {
        return client.get("https://api.dictionaryapi.dev/api/v2/entries/en/$word").body()
    }
}

@Serializable
data class WordDto(
    val word: String,
    val phonetic: String? = null,
    val phonetics: List<PhoneticDto> = emptyList(),
    val meanings: List<MeaningDto> = emptyList()
)

@Serializable
data class PhoneticDto(
    val text: String? = null,
    val audio: String = ""
)

@Serializable
data class MeaningDto(
    val partOfSpeech: String,
    val definitions: List<DefinitionDto> = emptyList()
)

@Serializable
data class DefinitionDto(
    val definition: String,
    val example: String? = null
)
