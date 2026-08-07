package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.FavoriteVerseEntity
import com.example.data.entity.VerseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("""
        SELECT v.* FROM verses v
        INNER JOIN favorite_verses f ON v.id = f.verseId
        ORDER BY f.addedTimestamp DESC
    """)
    fun getFavoriteVerses(): Flow<List<VerseEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_verses WHERE verseId = :verseId)")
    fun isFavorite(verseId: Long): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_verses WHERE verseId = :verseId)")
    suspend fun isFavoriteDirect(verseId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteVerseEntity)

    @Query("DELETE FROM favorite_verses WHERE verseId = :verseId")
    suspend fun removeFavorite(verseId: Long)
}
