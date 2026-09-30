package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelDao {
    @Query("SELECT * FROM level_progress ORDER BY levelId ASC")
    fun getAllLevels(): Flow<List<LevelEntity>>

    @Query("SELECT * FROM level_progress WHERE levelId = :levelId")
    fun getLevel(levelId: Int): Flow<LevelEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(level: LevelEntity)

    @Query("DELETE FROM level_progress")
    suspend fun clearAll()
}
