package com.funnyenglish.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WordDao {
    @Query("SELECT * FROM words WHERE word = :word LIMIT 1")
    suspend fun get(word: String): WordEntity?

    @Query("SELECT * FROM words WHERE isFavorite = 1 ORDER BY searchedAt DESC")
    suspend fun getFavorites(): List<WordEntity>

    @Query("SELECT * FROM words ORDER BY searchedAt DESC LIMIT 100")
    suspend fun getRecent(): List<WordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: WordEntity)

    @Query("UPDATE words SET isFavorite = :isFavorite WHERE word = :word")
    suspend fun setFavorite(word: String, isFavorite: Boolean)
}
