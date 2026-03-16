package com.example.alarm_jinxuan.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.alarm_jinxuan.model.LapRecord
import com.example.alarm_jinxuan.model.StopwatchState

@Database(entities = [LapRecord::class, StopwatchState::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun lapDao(): LapDao

    abstract fun stopWatch(): StopwatchStateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stopwatch_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}