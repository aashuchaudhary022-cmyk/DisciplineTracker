package com.disciplinetracker.data.repository

import com.disciplinetracker.data.dao.HabitDao
import com.disciplinetracker.data.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

class HabitRepository(
    private val habitDao: HabitDao
) {
    fun getHabits(): Flow<List<HabitEntity>> = habitDao.getHabits()

    suspend fun addHabit(name: String, description: String, targetPerWeek: Int) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return

        habitDao.insertHabit(
            HabitEntity(
                name = trimmedName,
                description = description.trim(),
                targetPerWeek = targetPerWeek.coerceAtLeast(1)
            )
        )
    }
}
