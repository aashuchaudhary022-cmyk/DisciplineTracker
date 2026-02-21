package com.disciplinetracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val targetPerWeek: Int = 1,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)
