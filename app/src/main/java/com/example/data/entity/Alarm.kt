package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val time: Long = 0L,
    val enabled: Boolean = true,
    val label: String = "Alarm",
    val repeatDays: Int = 0
)
