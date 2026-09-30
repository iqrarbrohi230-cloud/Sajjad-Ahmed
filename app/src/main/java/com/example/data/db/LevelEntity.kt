package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_progress")
data class LevelEntity(
    @PrimaryKey val levelId: Int,
    val completed: Boolean,
    val stars: Int,
    val bestMoves: Int,
    val timeSpentSeconds: Int
)
