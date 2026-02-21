package com.disciplinetracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.disciplinetracker.data.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Query("SELECT * FROM habit ORDER BY createdAtEpochMillis DESC")
    fun getHabits(): Flow<List<HabitEntity>>
}
