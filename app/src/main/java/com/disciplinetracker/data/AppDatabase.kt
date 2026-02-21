package com.disciplinetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.disciplinetracker.data.dao.HabitDao
import com.disciplinetracker.data.entity.HabitEntity

@Database(
    entities = [HabitEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "discipline_tracker.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
