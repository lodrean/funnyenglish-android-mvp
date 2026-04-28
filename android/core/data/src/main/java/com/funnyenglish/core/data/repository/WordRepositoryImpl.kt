package com.funnyenglish.core.data.repository

import com.funnyenglish.core.data.local.WordDao
import com.funnyenglish.core.data.local.WordEntity
import com.funnyenglish.core.data.remote.DefinitionDto
import com.funnyenglish.core.data.remote.DictionaryApi
import com.funnyenglish.core.data.remote.MeaningDto
import com.funnyenglish.core.data.remote.WordDto
import com.funnyenglish.core.domain.model.Definition
import com.funnyenglish.core.domain.model.Meaning
import com.funnyenglish.core.domain.model.Word
import com.funnyenglish.core.domain.repository.WordRepository
import com.funnyenglish.core.domain.util.DataError
import com.funnyenglish.core.domain.util.Result
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WordRepositoryImpl(
    private val api: DictionaryApi,
    private val dao: WordDao
) : WordRepository {

    override suspend fun search(word: String): Result<Word, DataError> {
        dao.get(word.lowercase())?.let { cached ->
            return Result.Success(cached.toDomain())
        }

        return try {
            val dto = api.getDefinition(word.lowercase()).firstOrNull()
                ?: return Result.Error(DataError.NOT_FOUND)

            val domain = dto.toDomain()
            dao.insert(domain.toEntity())
            Result.Success(domain)
        } catch (e: Exception) {
            Result.Error(DataError.NETWORK_ERROR)
        }
    }

    override suspend fun getFavorites(): List<Word> {
        return dao.getFavorites().map { it.toDomain() }
    }

    override suspend fun toggleFavorite(word: String, isFavorite: Boolean) {
        dao.setFavorite(word.lowercase(), isFavorite)
    }
}

private fun WordDto.toDomain(): Word = Word(
    spelling = word,
    phonetic = phonetic,
    audioUrl = phonetics.firstOrNull { it.audio.isNotBlank() }?.audio,
    meanings = meanings.map { it.toDomain() }
)

private fun MeaningDto.toDomain(): Meaning = Meaning(
    partOfSpeech = partOfSpeech,
    definitions = definitions.map { def ->
        Definition(text = def.definition, example = def.example)
    }
)

private fun Word.toEntity(): WordEntity = WordEntity(
    word = spelling.lowercase(),
    phonetic = phonetic,
    audioUrl = audioUrl,
    jsonMeanings = Json.encodeToString(meanings.map { m ->
        MeaningDto(
            partOfSpeech = m.partOfSpeech,
            definitions = m.definitions.map { d ->
                DefinitionDto(definition = d.text, example = d.example)
            }
        )
    })
)

private fun WordEntity.toDomain(): Word = Word(
    spelling = word,
    phonetic = phonetic,
    audioUrl = audioUrl,
    meanings = try {
        Json.decodeFromString<List<MeaningDto>>(jsonMeanings).map { m ->
            Meaning(
                partOfSpeech = m.partOfSpeech,
                definitions = m.definitions.map { d ->
                    Definition(text = d.definition, example = d.example)
                }
            )
        }
    } catch (_: Exception) {
        emptyList()
    }
)
