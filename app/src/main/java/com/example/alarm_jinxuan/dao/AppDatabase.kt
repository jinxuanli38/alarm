package com.example.alarm_jinxuan.dao

import android.content.Context
import android.os.Build
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.alarm_jinxuan.model.AlarmEntity
import com.example.alarm_jinxuan.model.LapRecord
import com.example.alarm_jinxuan.model.StopwatchState
import com.example.alarm_jinxuan.model.WorldClockEntity

@Database(entities = [LapRecord::class, StopwatchState::class, AlarmEntity::class, WorldClockEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun lapDao(): LapDao

    abstract fun stopWatch(): StopwatchStateDao

    abstract fun alarm(): AlarmDao

    abstract fun worldClock(): WorldClockDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // 获取设备保护存储上下文。
            val deviceContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.createDeviceProtectedStorageContext()
            } else {
                context
            }

            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    deviceContext,
                    AppDatabase::class.java,
                    "stopwatch_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}