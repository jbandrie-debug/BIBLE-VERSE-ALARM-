package com.example.data.repository

import com.example.data.dao.FavoriteDao
import com.example.data.dao.VerseDao
import com.example.data.entity.FavoriteVerseEntity
import com.example.data.entity.VerseEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class VerseRepository(
    private val verseDao: VerseDao,
    private val favoriteDao: FavoriteDao
) {
    val allVerses: Flow<List<VerseEntity>> = verseDao.getAllVerses()
    val favoriteVerses: Flow<List<VerseEntity>> = favoriteDao.getFavoriteVerses()
    val translations: Flow<List<String>> = verseDao.getAllTranslations()
    val categories: Flow<List<String>> = verseDao.getAllCategories()
    val books: Flow<List<String>> = verseDao.getAllBooks()

    suspend fun getVerseById(id: Long): VerseEntity? = verseDao.getVerseById(id)

    fun getVersesByTranslation(translation: String): Flow<List<VerseEntity>> =
        verseDao.getVersesByTranslation(translation)

    fun getVersesByCategory(category: String): Flow<List<VerseEntity>> =
        verseDao.getVersesByCategory(category)

    fun getVersesByCategoryAndTranslation(category: String, translation: String): Flow<List<VerseEntity>> =
        verseDao.getVersesByCategoryAndTranslation(category, translation)

    fun searchVerses(query: String): Flow<List<VerseEntity>> = verseDao.searchVerses(query)

    suspend fun getRandomVerse(): VerseEntity? = verseDao.getRandomVerse()

    suspend fun getRandomVerseByTranslation(translation: String): VerseEntity? =
        verseDao.getRandomVerseByTranslation(translation)

    suspend fun ensureVersesLoaded() {
        if (verseDao.getVerseCount() < com.example.util.InitialBibleVerses.verses.size) {
            verseDao.insertVerses(com.example.util.InitialBibleVerses.verses)
        }
    }

    suspend fun getDailyVerse(translation: String = "KJV"): VerseEntity? {
        ensureVersesLoaded()
        val total = verseDao.getVerseCount()
        if (total == 0) return defaultFallbackVerse()
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val verseId = (dayOfYear % total + 1).toLong()
        return verseDao.getVerseById(verseId) ?: verseDao.getRandomVerse() ?: defaultFallbackVerse()
    }

    fun defaultFallbackVerse() = VerseEntity(
        book = "Psalms", chapter = 118, verseNumber = 24,
        text = "This is the day which the LORD hath made; we will rejoice and be glad in it.",
        translation = "KJV", category = "Morning"
    )

    fun isFavorite(verseId: Long): Flow<Boolean> = favoriteDao.isFavorite(verseId)

    suspend fun toggleFavorite(verseId: Long) {
        if (favoriteDao.isFavoriteDirect(verseId)) {
            favoriteDao.removeFavorite(verseId)
        } else {
            favoriteDao.addFavorite(FavoriteVerseEntity(verseId = verseId))
        }
    }

    suspend fun addVerse(verse: VerseEntity): Long = verseDao.insertVerse(verse)
}
