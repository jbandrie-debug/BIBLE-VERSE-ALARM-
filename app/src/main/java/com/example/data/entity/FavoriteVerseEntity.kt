package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_verses")
data class FavoriteVerseEntity(
    @PrimaryKey val verseId: Long,
    val addedTimestamp: Long = System.currentTimeMillis()
)
