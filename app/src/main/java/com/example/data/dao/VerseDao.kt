package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.VerseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VerseDao {
    @Query("SELECT * FROM verses ORDER BY book ASC, chapter ASC, verseNumber ASC")
    fun getAllVerses(): Flow<List<VerseEntity>>

    @Query("SELECT * FROM verses WHERE id = :id")
    suspend fun getVerseById(id: Long): VerseEntity?

    @Query("SELECT * FROM verses WHERE translation = :translation ORDER BY book ASC, chapter ASC, verseNumber ASC")
    fun getVersesByTranslation(translation: String): Flow<List<VerseEntity>>

    @Query("SELECT * FROM verses WHERE category = :category ORDER BY book ASC, chapter ASC, verseNumber ASC")
    fun getVersesByCategory(category: String): Flow<List<VerseEntity>>

    @Query("SELECT * FROM verses WHERE category = :category AND translation = :translation ORDER BY book ASC, chapter ASC, verseNumber ASC")
    fun getVersesByCategoryAndTranslation(category: String, translation: String): Flow<List<VerseEntity>>

    @Query("SELECT * FROM verses WHERE text LIKE '%' || :query || '%' OR book LIKE '%' || :query || '%' ORDER BY book ASC")
    fun searchVerses(query: String): Flow<List<VerseEntity>>

    @Query("SELECT DISTINCT translation FROM verses")
    fun getAllTranslations(): Flow<List<String>>

    @Query("SELECT DISTINCT category FROM verses")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM verses ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomVerse(): VerseEntity?

    @Query("SELECT * FROM verses WHERE translation = :translation ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomVerseByTranslation(translation: String): VerseEntity?

    @Query("SELECT * FROM verses WHERE category = :category ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomVerseByCategory(category: String): VerseEntity?

    @Query("SELECT * FROM verses WHERE book = :book ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomVerseByBook(book: String): VerseEntity?

    @Query("SELECT DISTINCT book FROM verses ORDER BY book ASC")
    fun getAllBooks(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM verses")
    suspend fun getVerseCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerses(verses: List<VerseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerse(verse: VerseEntity): Long
}
