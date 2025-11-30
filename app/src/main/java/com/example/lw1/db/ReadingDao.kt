package com.example.lw1.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.lw1.models.Reading
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingDao {
    @Query("SELECT * FROM readings")
    fun getAll(): Flow<List<Reading>>

    @Query("SELECT * FROM readings WHERE x = :x ORDER BY y")
    fun getColumn(x: Int): Flow<List<Reading>>

    @Query("SELECT * FROM readings WHERE x = :x AND y = :y LIMIT 1")
    fun getOne(x: Int, y: Int): Flow<Reading?>

    @Upsert
    suspend fun insertReading(reading: Reading)

    @Upsert
    suspend fun insertColumn(readings: List<Reading>)

    @Query("DELETE FROM readings")
    suspend fun deleteAll()

    @Query("DELETE FROM readings WHERE x = :x AND y = :y")
    suspend fun deleteOne(x: Int, y: Int)
}