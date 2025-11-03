package com.example.lw1.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.lw1.models.Reading

@Database(entities = [Reading::class], version = 1, exportSchema = false)
@TypeConverters(IntListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun readingDao(): ReadingDao
}
